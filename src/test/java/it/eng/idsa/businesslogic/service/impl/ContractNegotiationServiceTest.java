package it.eng.idsa.businesslogic.service.impl;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.xml.datatype.DatatypeConfigurationException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import de.fraunhofer.iais.eis.Action;
import de.fraunhofer.iais.eis.BinaryOperator;
import de.fraunhofer.iais.eis.Constraint;
import de.fraunhofer.iais.eis.ContractAgreement;
import de.fraunhofer.iais.eis.LeftOperand;
import de.fraunhofer.iais.eis.Message;
import de.fraunhofer.iais.eis.Permission;
import de.fraunhofer.iais.eis.util.ConstraintViolationException;
import de.fraunhofer.iais.eis.util.RdfResource;
import de.fraunhofer.iais.eis.util.Util;
import it.eng.idsa.businesslogic.service.ContractNegotiationService;
import it.eng.idsa.businesslogic.util.TestUtilMessageService;
import it.eng.idsa.multipart.processor.MultipartMessageProcessor;

public class ContractNegotiationServiceTest {
	
	private static final URI TARGET = URI.create("http://w3id.org/engrd/connector/artifact/1");

	private ContractNegotiationService service;
	
	private Message contractAgreementMessage;
	private ContractAgreement contractAgreement;
	
	@BeforeEach
	public void setup() throws ConstraintViolationException, DatatypeConfigurationException, URISyntaxException {
		contractAgreementMessage = TestUtilMessageService.getContractAgreementMessage();
		
		Constraint constraint = TestUtilMessageService.generateConstraint(LeftOperand.POLICY_EVALUATION_TIME, 
				BinaryOperator.EQ, new RdfResource("2020-12-31T23:59:59.000+00:00", URI.create("xsd:datetime")));
		Constraint constraint2 = TestUtilMessageService.generateConstraint(LeftOperand.PAY_AMOUNT, 
				BinaryOperator.EQ, new RdfResource("12", URI.create("http://www.w3.org/2001/XMLSchema#double")));
		Permission permission = TestUtilMessageService.generatePermission(TARGET, Action.USE, Util.asList(constraint, constraint2));
		Permission permission2 = TestUtilMessageService.generatePermission(TARGET, Action.ANONYMIZE, Util.asList(constraint2));

		contractAgreement = TestUtilMessageService.getContractAgreement(Util.asList(permission, permission2));
		
		service = new ContractNegotiationServiceImpl();
	}
	
	@Test
	public void testContractNegotiation() throws IOException {
//		service.processContractAgreement(contractAgreementMessage, contractAgreement);
		String permissionString = MultipartMessageProcessor.serializeToPlainJson(contractAgreement.getPermission());
		System.out.println(permissionString);
	}
	
	@Test
	public void systemUsePermission() throws IOException {
		System.out.println(MultipartMessageProcessor.serializeToJsonLD(
				TestUtilMessageService.systemUsePermission(
						Action.USE,
						URI.create("http://systemUri.com"), 
						URI.create("http://pipEndpoint.com"))
				));
	}
}
