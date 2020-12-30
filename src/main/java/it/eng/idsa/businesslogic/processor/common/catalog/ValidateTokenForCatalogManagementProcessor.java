package it.eng.idsa.businesslogic.processor.common.catalog;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import it.eng.idsa.businesslogic.service.DapsService;
import it.eng.idsa.businesslogic.service.RejectionMessageService;
import it.eng.idsa.businesslogic.util.RejectionMessageType;

@Component
public class ValidateTokenForCatalogManagementProcessor implements Processor {

	private static final Logger logger = LogManager.getLogger(ValidateTokenForCatalogManagementProcessor.class);

	@Autowired
	private DapsService dapsService;

	@Autowired
	private RejectionMessageService rejectionMessageService;
	
	@Value("${application.isEnabledDapsInteraction}")
    private boolean isEnabledDapsInteraction;

	@Override
	public void process(Exchange exchange) throws Exception {
		
		if (!isEnabledDapsInteraction) {
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

	}

}
