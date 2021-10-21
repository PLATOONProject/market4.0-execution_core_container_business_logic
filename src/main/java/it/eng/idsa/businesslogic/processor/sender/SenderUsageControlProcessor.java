package it.eng.idsa.businesslogic.processor.sender;

import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.converter.stream.CachedOutputStream;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.google.gson.internal.LinkedTreeMap;

//import de.fraunhofer.dataspaces.iese.camel.interceptor.model.IdsMsgTarget;
//import de.fraunhofer.dataspaces.iese.camel.interceptor.model.IdsUseObject;
//import de.fraunhofer.dataspaces.iese.camel.interceptor.model.UsageControlObject;
//import de.fraunhofer.dataspaces.iese.camel.interceptor.service.UcService;
import de.fraunhofer.iais.eis.Message;
import it.eng.idsa.businesslogic.configuration.WebSocketServerConfigurationA;
import it.eng.idsa.businesslogic.processor.receiver.ReceiverUsageControlProcessor;
import it.eng.idsa.businesslogic.service.RejectionMessageService;
import it.eng.idsa.businesslogic.util.HeaderCleaner;
import it.eng.idsa.businesslogic.util.MessagePart;
import it.eng.idsa.businesslogic.util.RejectionMessageType;
import it.eng.idsa.businesslogic.util.UsageControlObjectToEnforce;
import it.eng.idsa.multipart.builder.MultipartMessageBuilder;
import it.eng.idsa.multipart.domain.MultipartMessage;

/**
 * @author Antonio Scatoloni and Gabriele De Luca
 */
@ComponentScan("de.fraunhofer.dataspaces.iese")
@Component
public class SenderUsageControlProcessor implements Processor {
	private Gson gson;
	private static final Logger logger = LoggerFactory.getLogger(SenderUsageControlProcessor.class);

	@Value("${application.isEnabledUsageControl:false}")
	private boolean isEnabledUsageControl;

	@Value("${spring.ids.ucapp.baseUrl}")
	private String ucBaseUrl;

	@Autowired
	private OAuth2RestTemplate oAuth2RestTemplate;

	// @Autowired
	// private UcService ucService;

	@Autowired
	private RejectionMessageService rejectionMessageService;

	@Value("${application.dataApp.websocket.isEnabled}")
	private boolean isEnabledWebSocket;

	@Autowired(required = false)
	WebSocketServerConfigurationA webSocketServerConfiguration;

	@Value("${application.eccHttpSendRouter}")
	private String eccHttpSendRouter;

	@Value("${application.openDataAppReceiverRouter}")
	private String openDataAppReceiverRouter;

	@Autowired
	private HeaderCleaner headerCleaner;

	public SenderUsageControlProcessor() {
		gson = ReceiverUsageControlProcessor.createGson();
	}

	@Override
	public void process(Exchange exchange) {
		if (!isEnabledUsageControl) {
			logger.info("Usage control not configured - continued with flow");
			return;
		}
		String payload = null;
		Message message = null;
		String header = null;

		MultipartMessage multipartMessageResponse = null;
		try {
			MultipartMessage multipartMessage = exchange.getMessage().getBody(MultipartMessage.class);
			payload = multipartMessage.getPayloadContent();
			header = multipartMessage.getHeaderContentString();
			message = multipartMessage.getHeaderContent();

			Map<String, Object> headerParts = exchange.getMessage().getHeaders();

			logger.info("from: " + exchange.getFromEndpoint());
			logger.debug("Message header: " + header);
			logger.debug("Message Body: " + payload);

			JsonElement transferedDataObject = getDataObject(payload);
			JsonElement headerDataObject = getDataObject(header);

			UsageControlObjectToEnforce ucObj = gson.fromJson(transferedDataObject, UsageControlObjectToEnforce.class);
			boolean isUsageControlObject = true;

			if (null != transferedDataObject) {
				logger.info("Proceeding with Usage control enforcement");
				String receiver = ucObj.getAssigner().toString();
				String sender = ucObj.getAssignee().toString();
				logger.info("Provider:" + receiver);
				logger.info("Consumer:" + sender);
				logger.info("payload:" + ucObj.getPayload());
				logger.info("artifactID:" + ucObj.getTargetArtifactId());

				// UC SERVICE invocation for enforcement
				String ucUrl = ucBaseUrl + "/platoontec/PlatoonDataUsage/1.0/enforce/usage/use?targetDataUri="
						+ ucObj.getTargetArtifactId() + "&providerUri=" + receiver + "&consumerUri=" + sender
						+ "&consuming=true";
//				RestTemplate restTemplate = new RestTemplate();

				HttpHeaders headers = new HttpHeaders();
				headers.setContentType(MediaType.APPLICATION_JSON);

				HttpEntity<String> request = new HttpEntity<String>(ucObj.getPayload(), headers);

				String objectToEnforceAsJsonStr = oAuth2RestTemplate.postForObject(ucUrl, request, String.class);

				// Prepare Response
				multipartMessageResponse = new MultipartMessageBuilder().withHeaderContent(message)
						.withPayloadContent(objectToEnforceAsJsonStr).build();

			} else {
				logger.info("Usage Control not applied - not ArtifactRequestMessage/ArtifactResponseMessage");
				multipartMessageResponse = multipartMessage;

			}
			headerCleaner.removeTechnicalHeaders(exchange.getMessage().getHeaders());
			exchange.getMessage().setBody(multipartMessageResponse);
			exchange.getMessage().setHeaders(exchange.getMessage().getHeaders());
			logger.info("Usage control policy enforcementd - completed");

		} catch (Exception e) {
			logger.error("Usage Control Enforcement has failed with MESSAGE: {}", e.getMessage());
			rejectionMessageService.sendRejectionMessage(RejectionMessageType.REJECTION_USAGE_CONTROL, message);
		}
	}

	/**
	 * Used for purpose PIP
	 * 
	 * @ActionDescription(methodName = "purpose") public String purpose(
	 * @ActionParameterDescription(name = "MsgTargetAppUri", mandatory = true)
	 *                                  String msgTargetAppUri) IdsMsgTarget.appUri
	 *                                  is translated to msgTargetAppUri
	 * @return
	 */
	/*
	 * public static IdsMsgTarget getIdsMsgTarget() { IdsMsgTarget idsMsgTarget =
	 * new IdsMsgTarget(); idsMsgTarget.setName("Anwendung A"); //
	 * idsMsgTarget.setAppUri(target.toString());
	 * idsMsgTarget.setAppUri("http://ziel-app"); return idsMsgTarget; }
	 */

	private JsonElement getDataObject(String s) {
		JsonElement obj = null;
		try {
			JsonElement jsonElement = gson.fromJson(s, JsonElement.class);
			if (null != jsonElement && !(jsonElement.isJsonArray() && jsonElement.getAsJsonArray().size() == 0)) {
				obj = jsonElement;
			}
		} catch (JsonSyntaxException jse) {
			obj = null;
		}
		return obj;
	}

	private String extractPayloadFromJson(JsonElement payload) {
		final JsonObject asJsonObject = payload.getAsJsonObject();
		JsonElement payloadInner = asJsonObject.get(MessagePart.PAYLOAD);
		if (null != payloadInner)
			return payloadInner.getAsString();
		return payload.toString();
	}
}
