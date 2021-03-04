package it.eng.idsa.businesslogic.service.impl;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.xml.datatype.DatatypeConfigurationException;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import de.fraunhofer.iais.eis.Action;
import de.fraunhofer.iais.eis.BinaryOperator;
import de.fraunhofer.iais.eis.Constraint;
import de.fraunhofer.iais.eis.Contract;
import de.fraunhofer.iais.eis.ContractAgreement;
import de.fraunhofer.iais.eis.LeftOperand;
import de.fraunhofer.iais.eis.Message;
import de.fraunhofer.iais.eis.Permission;
import de.fraunhofer.iais.eis.TextResource;
import de.fraunhofer.iais.eis.util.RdfResource;
import de.fraunhofer.iais.eis.util.Util;
import de.fraunhofer.iese.ids.odrl.policy.library.model.Condition;
import de.fraunhofer.iese.ids.odrl.policy.library.model.OdrlPolicy;
import de.fraunhofer.iese.ids.odrl.policy.library.model.Party;
import de.fraunhofer.iese.ids.odrl.policy.library.model.RightOperand;
import de.fraunhofer.iese.ids.odrl.policy.library.model.Rule;
import de.fraunhofer.iese.ids.odrl.policy.library.model.enums.ActionType;
import de.fraunhofer.iese.ids.odrl.policy.library.model.enums.ConditionType;
import de.fraunhofer.iese.ids.odrl.policy.library.model.enums.Operator;
import de.fraunhofer.iese.ids.odrl.policy.library.model.enums.PartyType;
import de.fraunhofer.iese.ids.odrl.policy.library.model.enums.PolicyType;
import de.fraunhofer.iese.ids.odrl.policy.library.model.enums.RightOperandType;
import de.fraunhofer.iese.ids.odrl.policy.library.model.enums.RuleType;
import it.eng.idsa.businesslogic.util.TestUtilMessageService;

public class ContrctOfferTest {

	private static final URI TARGET = URI.create("http://w3id.org/engrd/connector/artifact/1");

	@Test
	@DisplayName("generateContractOfferMesage")
	public void generateContractOfferMesage() throws DatatypeConfigurationException {
		Message contractOffer = TestUtilMessageService.getContractOfferMessage();
		System.out.println(TestUtilMessageService.getMessageAsString(contractOffer));
	}
	
	@Test
	@DisplayName("generateContractOffer")
	public void generateContractOffer() throws DatatypeConfigurationException {
		Constraint constraint = TestUtilMessageService.generateConstraint(LeftOperand.POLICY_EVALUATION_TIME, BinaryOperator.EQ, 
				new RdfResource("2019-01-01T00:00:00.000+00:00"));
		Permission permission = TestUtilMessageService.generatePermission(TARGET, Action.USE, Util.asList(constraint));
		TextResource contractDocument = TestUtilMessageService.getContractDocument();
		Contract contractOfferPayload = TestUtilMessageService.getContractOffer(permission, contractDocument);
		
		System.out.println(TestUtilMessageService.getObjectAsStringLD(contractOfferPayload));
	}
	
	@Test
	public void generateContractAgreementMessage() throws DatatypeConfigurationException {
		Message contractAgreementMessage = TestUtilMessageService.getContractAgreementMessage();
		System.out.println(TestUtilMessageService.getMessageAsString(contractAgreementMessage));
	}
	
	@Test
	public void generateContractAgreement() throws DatatypeConfigurationException {
		Constraint constraint = TestUtilMessageService.generateConstraint(LeftOperand.POLICY_EVALUATION_TIME, BinaryOperator.EQ, 
				new RdfResource("2019-01-01T00:00:00.000+00:00"));
		Permission permission = TestUtilMessageService.generatePermission(TARGET, Action.USE, Util.asList(constraint));
		ContractAgreement contractAgreement = TestUtilMessageService.getContractAgreement(Util.asList(permission));
		System.out.println(TestUtilMessageService.getObjectAsStringLD(contractAgreement));
	}
	
	@Test
	public void odrlPolicy() {
		OdrlPolicy policy = new OdrlPolicy();
		policy.setPolicyId(URI.create("http://example.com/policy/restrict-access-interval"));
		policy.setConsumer(new Party(PartyType.CONSUMER, URI.create("http://example.com/party/consumer-party")));
		policy.setProvider(new Party(PartyType.PROVIDER, URI.create("http://example.com/party/my-party")));
		policy.setProfile(URI.create("http://example.com/ids-profile"));
		policy.setType(PolicyType.AGREEMENT);
		policy.setProviderSide(true);
		policy.setTarget(TARGET);
		
		List<Rule> rules = new ArrayList<>();
		Rule r = new Rule();
		r.setType(RuleType.PERMISSION);
		de.fraunhofer.iese.ids.odrl.policy.library.model.Action action = new de.fraunhofer.iese.ids.odrl.policy.library.model.Action(ActionType.USE);
		r.setAction(action);
		
//		List<Condition> conditions = new ArrayList<>();
//		RightOperand rightOperand = new RightOperand("2020-10-01T00:00:00Z", RightOperandType.DATETIMESTAMP);
//		Condition c = createCondition(ConditionType.CONSTRAINT, de.fraunhofer.iese.ids.odrl.policy.library.model.enums.LeftOperand.DATETIME, Operator.EQUALS, rightOperand);
//		conditions.add(c);
		
//		r.setConstraints(conditions);
		rules.add(r);
//		policy.setRules(rules);

		System.out.println(policy);
	}
	
	@Test
	public void getIntervalRestrictedPolicy() throws JsonProcessingException {
		RdfResource rightOperand = new RdfResource();
		rightOperand.setType("ids:interval");
		RdfResource begin = new RdfResource("01.12.2020.");
		begin.setType("xsd:dateTime");
		RdfResource end = new RdfResource("31.12.2020.");
		end.setType("xsd:dateTime");
		String beginString = new ObjectMapper().writeValueAsString(begin);
		String endString = new ObjectMapper().writeValueAsString(end);
		rightOperand.setValue(beginString + "," + endString);
		Constraint constraint = TestUtilMessageService.generateConstraint(LeftOperand.POLICY_EVALUATION_TIME, 
				BinaryOperator.GT,
				rightOperand);
		Permission intervalRestrictedPermission = TestUtilMessageService.generatePermission(TARGET, Action.USE, Util.asList(constraint));
		
		System.out.println(getObjectAsString(intervalRestrictedPermission));
	}

	
	private Condition createCondition(ConditionType conditionType, de.fraunhofer.iese.ids.odrl.policy.library.model.enums.LeftOperand leftOperand, 
			Operator operator, RightOperand rightOperand) {
		Condition condition = new Condition(conditionType, "condition comment");
		condition.setLeftOperand(leftOperand);
		condition.setOperator(operator);
		condition.setRightOperand(rightOperand);
		return condition;
	}
	
	private String getObjectAsString(Object toSerialize) {
		String result = null;
		ObjectMapper mapper = new ObjectMapper();
	    mapper.setSerializationInclusion(Include.NON_NULL);
	    mapper.enable(SerializationFeature.INDENT_OUTPUT);
		try {
			result = mapper.writeValueAsString(toSerialize);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}
}
