package it.eng.idsa.businesslogic.processor.common;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;

import org.apache.camel.Exchange;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import de.fraunhofer.iais.eis.Message;
import it.eng.idsa.businesslogic.service.ClearingHouseService;
import it.eng.idsa.multipart.domain.MultipartMessage;

public class RegisterTransactionToCHProcessorTest {

	private static final String PAYLOAD = "payload";

	@InjectMocks
	private RegisterTransactionToCHProcessor processor;
	
	@Mock
	private ClearingHouseService clearingHouseService;
	
	@Mock
	private Exchange exchange;
	@Mock
	private org.apache.camel.Message camelMessage;
	@Mock
	private MultipartMessage multipartMessage;
	
	private Message message;
	private Map<String, Object> headers = new HashMap<>();
	
	@BeforeEach
	public void setup() {
		MockitoAnnotations.initMocks(this);
	}
	
	@Test
	public void clearingHouseDisabled() throws Exception {
		ReflectionTestUtils.setField(processor, "isEnabledClearingHouse", false);
		
		processor.process(exchange);
		
		verify(clearingHouseService, times(0)).registerTransaction(any(Message.class), any(String.class));
	}
	
	@Test
	public void registerTransactionToCHSuccessfull() throws Exception {
		mockExchangeHeaderAndBody();
		ReflectionTestUtils.setField(processor, "isEnabledClearingHouse", true);
		
		when(clearingHouseService.registerTransaction(message, PAYLOAD)).thenReturn(true);
		
		processor.process(exchange);

		verify(clearingHouseService).registerTransaction(message, PAYLOAD);
		verify(camelMessage).setHeaders(headers);
		verify(camelMessage).setBody(multipartMessage);
	}
	
	@Test
	// Does the same thing like above test, at the moment, we do not have some specific logic to handle if CH registration
	// was successful or not, except log response
	public void registerTransactionToCHFailed() throws Exception {
		mockExchangeHeaderAndBody();
		ReflectionTestUtils.setField(processor, "isEnabledClearingHouse", true);
		
		when(clearingHouseService.registerTransaction(message, PAYLOAD)).thenReturn(false);
		
		processor.process(exchange);

		verify(clearingHouseService).registerTransaction(message, PAYLOAD);
		verify(camelMessage).setHeaders(headers);
		verify(camelMessage).setBody(multipartMessage);
	}
	
	private void mockExchangeHeaderAndBody() {
		when(exchange.getMessage()).thenReturn(camelMessage);
		when(camelMessage.getBody(MultipartMessage.class)).thenReturn(multipartMessage);
		when(camelMessage.getHeaders()).thenReturn(headers);
		when(multipartMessage.getHeaderContent()).thenReturn(message);
		when(multipartMessage.getPayloadContent()).thenReturn(PAYLOAD);
	}
	
}
