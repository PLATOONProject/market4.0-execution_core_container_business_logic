package it.eng.idsa.businesslogic.processor.sender.catalog;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import it.eng.idsa.businesslogic.service.RejectionMessageService;
import it.eng.idsa.businesslogic.util.IDSHeaders;
import it.eng.idsa.businesslogic.util.RejectionMessageType;

public class SenderCatalogRequestProcessorTest {
	
	@InjectMocks
	private SenderCatalogRequestProcessor senderCatalogRequestProcessor;
	
	@Mock
	private Exchange exchange;
	
	@Mock
	private Message camelMessageIn;
	
	@Mock
	private Message camelMessageOut;
	
	@Mock
	private RejectionMessageService rejectionMessageService;
	
	private Map<String, Object> headers = new HashMap<>();
	
	
	@BeforeEach
	public void init() {
		MockitoAnnotations.initMocks(this);
		ReflectionTestUtils.setField(senderCatalogRequestProcessor, "modelVersion", "4.0.0");
		headers.put(IDSHeaders.IDS_MODEL_VERSION.getName(), "4.0.0");
		headers.put(IDSHeaders.IDS_SENDER_AGENT.getName(), "http://example.org/some-participant");
		headers.put(IDSHeaders.IDS_RECIPIENT_AGENT.getName(), "http://example.org/some-participant");
		when(exchange.getIn()).thenReturn(camelMessageIn);
		when(exchange.getIn().getHeaders()).thenReturn(headers);
		when(exchange.getMessage()).thenReturn(camelMessageOut);
	}
	
	
	@Test
	public void missingModelVersionTest() throws Exception {
		headers.remove(IDSHeaders.IDS_MODEL_VERSION.getName());
		senderCatalogRequestProcessor.process(exchange);
		verify(rejectionMessageService).sendRejectionMessage(RejectionMessageType.REJECTION_MESSAGE_COMMON, null);
	}
	
	@Test
	public void wrongModelVersionTest() throws Exception {
		headers.replace(IDSHeaders.IDS_MODEL_VERSION.getName(), "1.0");
		senderCatalogRequestProcessor.process(exchange);
		verify(rejectionMessageService).sendRejectionMessage(RejectionMessageType.REJECTION_MESSAGE_COMMON, null);
	}
	
	@Test
	public void missingSenderAgentTest() throws Exception {
		headers.remove(IDSHeaders.IDS_SENDER_AGENT.getName());
		senderCatalogRequestProcessor.process(exchange);
		verify(rejectionMessageService).sendRejectionMessage(RejectionMessageType.REJECTION_MESSAGE_COMMON, null);
	}
	
	@Test
	public void missingRecipientAgentTest() throws Exception {
		headers.remove(IDSHeaders.IDS_RECIPIENT_AGENT.getName());
		senderCatalogRequestProcessor.process(exchange);
		verify(rejectionMessageService).sendRejectionMessage(RejectionMessageType.REJECTION_MESSAGE_COMMON, null);
	}

}
