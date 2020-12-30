package it.eng.idsa.businesslogic.processor.sender.catalog;

import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import it.eng.idsa.businesslogic.service.RejectionMessageService;
import it.eng.idsa.businesslogic.util.IDSHeaders;
import it.eng.idsa.businesslogic.util.RejectionMessageType;

@Component
public class SenderCatalogRequestProcessor implements Processor {
	
	private static final Logger logger = LogManager.getLogger(SenderCatalogRequestProcessor.class);
	
	@Autowired
	private RejectionMessageService rejectionMessageService;
	
	
	@Value("${information.model.version}")
	String modelVersion;

	@Override
	public void process(Exchange exchange) throws Exception {
		
		Map<String, Object> headers = exchange.getIn().getHeaders();
		//TODO check valid URI
		if (headers.get(IDSHeaders.IDS_MODEL_VERSION.getName()) == null) {
			logger.error("{} is missing", IDSHeaders.IDS_MODEL_VERSION.getName());
			rejectionMessageService.sendRejectionMessage(RejectionMessageType.REJECTION_MESSAGE_COMMON, null);
			return;
		}
		
		if (!headers.get(IDSHeaders.IDS_MODEL_VERSION.getName()).equals(modelVersion)) {
			logger.error("Wrong {}. Currently supported model version is {} ", IDSHeaders.IDS_MODEL_VERSION.getName(), modelVersion);
			rejectionMessageService.sendRejectionMessage(RejectionMessageType.REJECTION_MESSAGE_COMMON, null);
		}
		
		if (headers.get(IDSHeaders.IDS_SENDER_AGENT.getName()) == null) {
			logger.error("{} is missing", IDSHeaders.IDS_SENDER_AGENT.getName());
			rejectionMessageService.sendRejectionMessage(RejectionMessageType.REJECTION_MESSAGE_COMMON, null);
		}
		
		if (headers.get(IDSHeaders.IDS_RECIPIENT_AGENT.getName()) == null) {
			logger.error("{} is missing", IDSHeaders.IDS_RECIPIENT_AGENT.getName());
			rejectionMessageService.sendRejectionMessage(RejectionMessageType.REJECTION_MESSAGE_COMMON, null);
		}
		
		
	}

}
