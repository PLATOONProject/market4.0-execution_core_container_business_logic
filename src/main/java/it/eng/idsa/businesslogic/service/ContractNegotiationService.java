package it.eng.idsa.businesslogic.service;

import de.fraunhofer.iais.eis.ContractAgreement;
import de.fraunhofer.iais.eis.Message;

public interface ContractNegotiationService {

	/**
	 * Translate ContractAgreement into OdlrPolicy
	 * @param contractAgreementMessage
	 * @param contractAgreement
	 */
	void processContractAgreement(Message contractAgreementMessage, ContractAgreement contractAgreement);
}
