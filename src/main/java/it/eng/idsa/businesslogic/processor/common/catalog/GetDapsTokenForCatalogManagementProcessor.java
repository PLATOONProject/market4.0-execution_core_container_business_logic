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
public class GetDapsTokenForCatalogManagementProcessor implements Processor {

	private static final Logger logger = LogManager.getLogger(GetDapsTokenForCatalogManagementProcessor.class);

	@Autowired
	private RejectionMessageService rejectionMessageService;

	@Autowired
	private DapsService dapsService;
	
	@Value("${application.isEnabledDapsInteraction}")
    private boolean isEnabledDapsInteraction;

	@Override
	public void process(Exchange exchange) throws Exception {
		
		if (!isEnabledDapsInteraction) {
            logger.info("Daps interaction not configured - continued with flow");
            return;
        }
		String token = dapsService.getJwtToken();

		if (token == null) {
			logger.error("Can not get the token from the DAPS server");
			rejectionMessageService.sendRejectionMessage(RejectionMessageType.REJECTION_COMMUNICATION_LOCAL_ISSUES,
					null);
		}

		if (token.isEmpty()) {
			logger.error("The token from the DAPS server is empty");
			rejectionMessageService.sendRejectionMessage(RejectionMessageType.REJECTION_TOKEN_LOCAL_ISSUES, null);
		}

		exchange.getMessage().setHeader("IDS-SecurityToken", token);

	}

}
