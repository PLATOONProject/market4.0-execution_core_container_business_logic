package it.eng.idsa.businesslogic.processor.receiver.catalog;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import it.eng.idsa.businesslogic.service.DapsService;
import it.eng.idsa.businesslogic.service.RejectionMessageService;
import it.eng.idsa.businesslogic.util.RejectionMessageType;

public class ReceiverCatalogValidateTokenProcessor implements Processor {

	private static final Logger logger = LogManager.getLogger(ReceiverCatalogValidateTokenProcessor.class);

	@Value("${application.isEnabledDapsInteraction}")
	private boolean isEnabledDapsInteraction;

	@Autowired
	DapsService dapsService;

	@Autowired
	private RejectionMessageService rejectionMessageService;

	@Override
	public void process(Exchange exchange) throws Exception {

		if (!isEnabledDapsInteraction) {
			exchange.getOut().setHeaders(exchange.getIn().getHeaders());
			exchange.getOut().setBody(exchange.getIn().getBody());
			logger.info("Daps interaction not configured - continued with flow");
			return;
		}

		String token = (String) exchange.getIn().getHeader("IDS-SecurityToken");

		logger.info("token: {}", token);

		// Check is "token" valid
		boolean isTokenValid = dapsService.validateToken(token);

		logger.info("is token valid: " + isTokenValid);
		if (!isTokenValid) {
			rejectionMessageService.sendRejectionMessage(RejectionMessageType.REJECTION_TOKEN, null);
		}

		exchange.getIn().removeHeader("IDS-SecurityToken");

		exchange.getOut().setHeaders(exchange.getIn().getHeaders());
		exchange.getOut().setBody(exchange.getIn().getBody());

	}

}
