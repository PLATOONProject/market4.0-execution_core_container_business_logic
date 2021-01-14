package it.eng.idsa.businesslogic.processor.sender.catalog;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.http.Header;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import de.fraunhofer.iais.eis.Message;
import it.eng.idsa.businesslogic.service.RejectionMessageService;
import it.eng.idsa.businesslogic.service.impl.SendDataToBusinessLogicServiceImpl;
import it.eng.idsa.businesslogic.util.RejectionMessageType;

@Component
public class SenderCatalogSendDataToBusinessLogicProcessor implements Processor {

	private static final Logger logger = LogManager.getLogger(SenderCatalogSendDataToBusinessLogicProcessor.class);

	@Autowired
	private SendDataToBusinessLogicServiceImpl sendDataToBusinessLogicService;

	@Autowired
	private RejectionMessageService rejectionMessageService;

	@Override
	public void process(Exchange exchange) throws Exception {

		Map<String, Object> headerParts = exchange.getMessage().getHeaders();
		String payload = exchange.getMessage().getBody(String.class);

		// Get header, payload and message
		Message message = null;

		CloseableHttpResponse response = null;

		response = sendDataToBusinessLogicService.sendCatalogData((String) headerParts.get("Forward-To"), headerParts,
				payload);

		// Handle response
		handleResponse(exchange, message, response, (String) headerParts.get("Forward-To"));

		if (response != null) {
			response.close();
		}

	}

	private void handleResponse(Exchange exchange, Message message, CloseableHttpResponse response,
			String openApiDataAppAddress) throws UnsupportedOperationException, IOException {
		if (response == null) {
			logger.info("...communication error with: " + openApiDataAppAddress);
			rejectionMessageService.sendRejectionMessage(RejectionMessageType.REJECTION_COMMUNICATION_LOCAL_ISSUES,
					message);
		} else {
			String responseString = new String(response.getEntity().getContent().readAllBytes());
			logger.info("response received from the DataAPP=" + responseString);

			int statusCode = response.getStatusLine().getStatusCode();
			logger.info("status code of the response message is: " + statusCode);
			if (statusCode >= 300) {
				logger.info("data sent to destination: " + openApiDataAppAddress);
				rejectionMessageService.sendRejectionMessage(RejectionMessageType.REJECTION_MESSAGE_COMMON, message);
			} else {
				logger.info("data sent to destination: " + openApiDataAppAddress);
//				logger.info("Successful response from DataApp: " + responseString);

				exchange.getMessage().setHeaders(returnHeadersAsMap(response.getAllHeaders()));
				exchange.getMessage().setBody(responseString);
			}

		}

	}

	private Map<String, Object> returnHeadersAsMap(Header[] allHeaders) {
		Map<String, Object> headersMap = new HashMap<>();
		for (Header header : allHeaders) {
			headersMap.put(header.getName(), header.getValue());
		}
		return headersMap;
	}
}