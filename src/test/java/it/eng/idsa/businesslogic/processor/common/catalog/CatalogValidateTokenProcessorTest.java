package it.eng.idsa.businesslogic.processor.common.catalog;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import it.eng.idsa.businesslogic.processor.common.catalog.CatalogValidateTokenForProcessor;
import it.eng.idsa.businesslogic.service.DapsService;
import it.eng.idsa.businesslogic.service.RejectionMessageService;
import it.eng.idsa.businesslogic.util.RejectionMessageType;

public class CatalogValidateTokenProcessorTest {

	@Mock
	private Exchange exchange;
	@Mock
	DapsService dapsService;
	@Mock
	private Message messageOut;
	@Mock
	private Message message;
	@Mock
	private RejectionMessageService rejectionMessageService;

	@InjectMocks
	private CatalogValidateTokenForProcessor processor;

	private Map<String, Object> headerHeaders;
	private String token;
	private String bodyMessage;

	@BeforeEach
	public void setup() {
		token = "ABC";
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void processWithValidToken() throws Exception {
		ReflectionTestUtils.setField(processor, "isEnabledDapsInteraction", true);
		when(exchange.getIn()).thenReturn(message);
		when(message.getHeader("IDS-SecurityToken")).thenReturn(token);
		when(dapsService.validateToken(token)).thenReturn(true);
		when(exchange.getMessage()).thenReturn(messageOut);

		processor.process(exchange);

		verify(rejectionMessageService, times(0)).sendRejectionMessage(RejectionMessageType.REJECTION_TOKEN, null);

	}

	@Test
	public void processInvalidToken() throws Exception {
		ReflectionTestUtils.setField(processor, "isEnabledDapsInteraction", true);
		when(exchange.getIn()).thenReturn(message);
		when(message.getHeader("IDS-SecurityToken")).thenReturn(token);
		when(dapsService.validateToken(token)).thenReturn(false);
		when(exchange.getMessage()).thenReturn(messageOut);

		processor.process(exchange);

		verify(rejectionMessageService).sendRejectionMessage(RejectionMessageType.REJECTION_TOKEN, null);

	}

	@Test
	public void processWithoutDaps() throws Exception {
		ReflectionTestUtils.setField(processor, "isEnabledDapsInteraction", false);
		when(exchange.getIn()).thenReturn(message);
		when(message.getHeaders()).thenReturn(headerHeaders);
		when(message.getBody(String.class)).thenReturn(bodyMessage);
		when(exchange.getMessage()).thenReturn(messageOut);

		processor.process(exchange);

		verify(rejectionMessageService, times(0)).sendRejectionMessage(RejectionMessageType.REJECTION_TOKEN, null);
		verify(dapsService, times(0)).validateToken(token);

	}

}
