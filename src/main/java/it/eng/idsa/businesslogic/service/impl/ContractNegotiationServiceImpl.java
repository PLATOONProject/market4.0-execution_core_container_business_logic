package it.eng.idsa.businesslogic.service.impl;

import de.fraunhofer.iais.eis.Constraint;
import de.fraunhofer.iais.eis.ContractAgreement;
import de.fraunhofer.iais.eis.Message;
import de.fraunhofer.iais.eis.Permission;
import de.fraunhofer.iais.eis.util.RdfResource;
import de.fraunhofer.iese.ids.odrl.policy.library.model.Condition;
import de.fraunhofer.iese.ids.odrl.policy.library.model.RightOperand;
import de.fraunhofer.iese.ids.odrl.policy.library.model.enums.LeftOperand;
import de.fraunhofer.iese.ids.odrl.policy.library.model.enums.Operator;
import de.fraunhofer.iese.ids.odrl.policy.library.model.enums.RightOperandType;
import it.eng.idsa.businesslogic.service.ContractNegotiationService;

public class ContractNegotiationServiceImpl implements ContractNegotiationService {

	@Override
	public void processContractAgreement(Message contractAgreementMessage, ContractAgreement contractAgreement) {

		for(Permission permission: contractAgreement.getPermission()) {
			for(Constraint constraint : permission.getConstraint()) {
				convertToPolicyCondition(constraint);
			}
		}
	}

	private Condition convertToPolicyCondition(Constraint constraint) {
		Condition condition = new Condition();
		
		switch (constraint.getLeftOperand()) {
		case POLICY_EVALUATION_TIME:
			condition.setLeftOperand(LeftOperand.DATETIME);
			break;

		default:
			break;
		}
		
		switch (constraint.getOperator()) {
		case EQUALS:
			condition.setOperator(Operator.EQUALS);
			break;
		case GTEQ:
			condition.setOperator(Operator.GREATER_EQUAL);
			break;
		case GT:
			condition.setOperator(Operator.GREATER);
			break;
		case LT:
			condition.setOperator(Operator.LESS);
			break;
		case LTEQ:
			condition.setOperator(Operator.LESS_EQUAL);
			break;
		default:
			break;
		}
		
		RightOperand rightOperand = new RightOperand();
		RdfResource rdfResource = constraint.getRightOperand();
		rightOperand.setType(RightOperandType.valueOf(rdfResource.getType()));
		rightOperand.setValue(rdfResource.getValue());
		
		condition.setRightOperand(rightOperand);
		
		return condition;
		
	}

}
