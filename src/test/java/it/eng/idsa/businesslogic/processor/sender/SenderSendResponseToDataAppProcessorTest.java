package it.eng.idsa.businesslogic.processor.sender;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.http.HttpEntity;
import org.apache.http.entity.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import it.eng.idsa.businesslogic.service.HttpHeaderService;
import it.eng.idsa.businesslogic.service.MultipartMessageService;
import it.eng.idsa.businesslogic.util.HeaderCleaner;
import it.eng.idsa.businesslogic.util.RouterType;
import it.eng.idsa.businesslogic.util.TestUtilMessageService;
import it.eng.idsa.multipart.builder.MultipartMessageBuilder;
import it.eng.idsa.multipart.domain.MultipartMessage;

public class SenderSendResponseToDataAppProcessorTest {
	
	private static final String PAYLOAD = "Payload response";

	@InjectMocks
	private SenderSendResponseToDataAppProcessor processor;

	@Mock
	private MultipartMessageService multipartMessageService;
	@Mock
	private HttpHeaderService httpHeaderService;
	@Mock
	private HeaderCleaner headerCleaner;
	
	@Mock
	private Exchange exchange;
	@Mock
	private org.apache.camel.Message camelMessage;
	@Mock
	private HttpEntity httpEntity;
	@Mock
	private org.apache.http.Header header;
	
	private MultipartMessage multipartMessage;
	private MultipartMessage multipartMessageWithoutToken;

	private Map<String, Object> headers = new HashMap<>();
	
	@BeforeEach
	public void setup() {
		MockitoAnnotations.initMocks(this);
		multipartMessage = new MultipartMessageBuilder()
				.withHeaderContent(TestUtilMessageService.getArtifactRequestMessageWithToken())
				.withPayloadContent(PAYLOAD).build();
		multipartMessageWithoutToken = new MultipartMessageBuilder()
				.withHeaderContent(TestUtilMessageService.getArtifactRequestMessage())
				.withPayloadContent(PAYLOAD).build();
	}
	
	@Test
	public void sendDataToMultipartMixDapsDisabled() throws Exception {
		ReflectionTestUtils.setField(processor, "openDataAppReceiverRouter", RouterType.MULTIPART_MIX);
		mockExchangeHeaderAndBody();
		
		processor.process(exchange);
		
		// need to do it with any(String) since json sting can have different ordering
		verify(camelMessage).setBody(any(String.class));
		verify(multipartMessageService, times(0)).removeTokenFromMultipart(multipartMessage);
		verify(httpHeaderService).removeTokenHeaders(exchange.getMessage().getHeaders());
    	verify(httpHeaderService).removeMessageHeadersWithoutToken(exchange.getMessage().getHeaders());
    	verify(headerCleaner).removeTechnicalHeaders(exchange.getMessage().getHeaders());
    	verify(headerCleaner).removeTechnicalHeaders(camelMessage.getHeaders());

	}
	
	@Test
	public void sendDataToMultipartMixDapsEnabled() throws Exception {
		ReflectionTestUtils.setField(processor, "openDataAppReceiverRouter", RouterType.MULTIPART_MIX);
		ReflectionTestUtils.setField(processor, "isEnabledDapsInteraction", true);

		mockExchangeHeaderAndBody();
		
		when(multipartMessageService.removeTokenFromMultipart(multipartMessage)).thenReturn(multipartMessageWithoutToken);
		
		processor.process(exchange);
		
		verify(multipartMessageService).removeTokenFromMultipart(multipartMessage);
		verify(camelMessage).setBody(any(String.class));
		verify(httpHeaderService).removeTokenHeaders(exchange.getMessage().getHeaders());
    	verify(httpHeaderService).removeMessageHeadersWithoutToken(exchange.getMessage().getHeaders());
    	verify(headerCleaner).removeTechnicalHeaders(exchange.getMessage().getHeaders());
    	verify(headerCleaner).removeTechnicalHeaders(camelMessage.getHeaders());

	}
	
	@Test
	public void sendDataToMultipartForm() throws Exception {
		ReflectionTestUtils.setField(processor, "openDataAppReceiverRouter", RouterType.MULTIPART_BODY_FORM);
		mockExchangeHeaderAndBody();

		when(multipartMessageService.createMultipartMessage(multipartMessage.getHeaderContentString(), 
						multipartMessage.getPayloadContent(),
						null, ContentType.APPLICATION_JSON)).thenReturn(httpEntity);
		when(httpEntity.getContentType()).thenReturn(header);
		when(header.getValue()).thenReturn("plain/text");
		
		processor.process(exchange);
		
		verify(httpHeaderService).removeTokenHeaders(exchange.getMessage().getHeaders());
    	verify(httpHeaderService).removeMessageHeadersWithoutToken(exchange.getMessage().getHeaders());
    	verify(camelMessage).setBody(httpEntity.getContent());
    	verify(headerCleaner).removeTechnicalHeaders(camelMessage.getHeaders());

	}
	
	@Test
	public void sendDataToHttpHeader() throws Exception {
		ReflectionTestUtils.setField(processor, "openDataAppReceiverRouter", RouterType.HTTP_HEADER);
		mockExchangeHeaderAndBody();

		processor.process(exchange);
		
		verify(camelMessage).setBody(multipartMessage.getPayloadContent());
		
		verify(httpHeaderService ,times(0)).removeTokenHeaders(exchange.getMessage().getHeaders());
    	verify(httpHeaderService, times(0)).removeMessageHeadersWithoutToken(exchange.getMessage().getHeaders());
    	verify(headerCleaner).removeTechnicalHeaders(camelMessage.getHeaders());
	}
	
	private void mockExchangeHeaderAndBody() {
		when(exchange.getMessage()).thenReturn(camelMessage);
		when(camelMessage.getHeaders()).thenReturn(headers);
		when(camelMessage.getBody(MultipartMessage.class)).thenReturn(multipartMessage);
	}

}