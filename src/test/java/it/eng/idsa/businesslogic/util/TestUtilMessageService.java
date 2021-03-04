package it.eng.idsa.businesslogic.util;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.GregorianCalendar;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import de.fraunhofer.iais.eis.Action;
import de.fraunhofer.iais.eis.ArtifactRequestMessage;
import de.fraunhofer.iais.eis.ArtifactRequestMessageBuilder;
import de.fraunhofer.iais.eis.ArtifactResponseMessage;
import de.fraunhofer.iais.eis.ArtifactResponseMessageBuilder;
import de.fraunhofer.iais.eis.BinaryOperator;
import de.fraunhofer.iais.eis.Constraint;
import de.fraunhofer.iais.eis.ConstraintBuilder;
import de.fraunhofer.iais.eis.ContractAgreement;
import de.fraunhofer.iais.eis.ContractAgreementBuilder;
import de.fraunhofer.iais.eis.ContractAgreementMessageBuilder;
import de.fraunhofer.iais.eis.ContractOffer;
import de.fraunhofer.iais.eis.ContractOfferBuilder;
import de.fraunhofer.iais.eis.ContractOfferMessageBuilder;
import de.fraunhofer.iais.eis.DescriptionRequestMessage;
import de.fraunhofer.iais.eis.DescriptionRequestMessageBuilder;
import de.fraunhofer.iais.eis.LeftOperand;
import de.fraunhofer.iais.eis.Message;
import de.fraunhofer.iais.eis.Permission;
import de.fraunhofer.iais.eis.PermissionBuilder;
import de.fraunhofer.iais.eis.TextResource;
import de.fraunhofer.iais.eis.TextResourceBuilder;
import de.fraunhofer.iais.eis.ids.jsonld.Serializer;
import de.fraunhofer.iais.eis.util.ConstraintViolationException;
import de.fraunhofer.iais.eis.util.RdfResource;
import de.fraunhofer.iais.eis.util.Util;
import it.eng.idsa.multipart.processor.MultipartMessageProcessor;

/**
 * Util class used for testing
 * Create methods for generating classes when required
 */
public class TestUtilMessageService {

	private static final URI REQUESTED_ARTIFACT = URI.create("http://w3id.org/engrd/connector/artifact/1");
	private static final URI ISSUER_CONNECTOR = URI.create("http://w3id.org/engrd/connector");
	private static final URI PROVIDER = URI.create("http://w3id.org/engrd/provider");
	private static final URI CONSUMER = URI.create("http://w3id.org/engrd/consumer");
	private static final URI CORRELATION_MESSAGE = URI.create("http://industrialdataspace.org/ContractRequest/1a421b8c-3407-44a8-aeb9-253f145c869a");
	private static final URI TRANSFER_CONTRACT = URI.create("http://iais.fraunhofer.de/iais/eis/ids/1559059394287");
	private static final URI CONCTRAC_DOCUMENT = URI.create("https://creativecommons.org/licenses/by-nc/4.0/legalcode");

	private static final String MODEL_VERSION = "4.0.0";
	
	final static Serializer serializer;
	static {
		serializer = new Serializer();
	}

	public static XMLGregorianCalendar ISSUED;
	static {
		try {
			ISSUED = DatatypeFactory.newInstance().newXMLGregorianCalendar(new GregorianCalendar());
		} catch (DatatypeConfigurationException e) {
			e.printStackTrace();
		}
	}

	public static ArtifactRequestMessage getArtifactRequestMessage() {
		return new ArtifactRequestMessageBuilder()
				._issued_(ISSUED)
				._issuerConnector_(ISSUER_CONNECTOR)
				._modelVersion_(MODEL_VERSION)
				._requestedArtifact_(REQUESTED_ARTIFACT)
				.build();
	}

	public static ArtifactResponseMessage getArtifactResponseMessage() {
		return new ArtifactResponseMessageBuilder()
				._issued_(ISSUED)
				._issuerConnector_(ISSUER_CONNECTOR)
				._modelVersion_(MODEL_VERSION)
				.build();
	}

	public static DescriptionRequestMessage getDescriptionRequestMessage() {
		return new DescriptionRequestMessageBuilder()
				._issued_(ISSUED)
				._issuerConnector_(ISSUER_CONNECTOR)
				._modelVersion_(MODEL_VERSION)
				.build();
	}
	
	/**
	 * Generates Contract agreement message - used in header 
	 * @return
	 */
	public static Message getContractAgreementMessage() {
			return new ContractAgreementMessageBuilder()
					._issued_(ISSUED)
					._modelVersion_(MODEL_VERSION)
					._issuerConnector_(ISSUER_CONNECTOR)
					._correlationMessage_(CORRELATION_MESSAGE)
					._transferContract_(TRANSFER_CONTRACT)
					.build();
	}
	
	/**
	 * Generates Contract agreement - used in payload
	 * @param provider
	 * @param consumer
	 * @param permission
	 * @return
	 * @throws ConstraintViolationException
	 * @throws URISyntaxException
	 */
	public static ContractAgreement getContractAgreement(ArrayList<Permission> permissions) {
		return new ContractAgreementBuilder()
				._provider_(PROVIDER)
				._consumer_(CONSUMER)
//				._contractDocument_(getContractDocument())
				._permission_(permissions)
				.build();
	}
	
	/**
	 * Generates Contract offer message - used in header 
	 * @param issuerConnector
	 * @param correlationMessage
	 * @param transferContract
	 * @return
	 * @throws DatatypeConfigurationException
	 */
	public static Message getContractOfferMessage() {
		Message contractOffer = new ContractOfferMessageBuilder()
				._issued_(ISSUED)
				._modelVersion_(MODEL_VERSION)
				._issuerConnector_(ISSUER_CONNECTOR)
				._correlationMessage_(CORRELATION_MESSAGE)
				._transferContract_(TRANSFER_CONTRACT)
				.build();
		return contractOffer;
	}
	
	/**
	 * Generate Contract offer - used in payload
	 * @param provider
	 * @param consumer
	 * @param permission
	 * @param contractDocument
	 * @return
	 */
	public static ContractOffer getContractOffer(Permission permission, TextResource contractDocument ) {
		return new ContractOfferBuilder()
				._provider_(PROVIDER)
				._consumer_(CONSUMER)
				._permission_(Util.asList(permission))
				._contractDocument_(contractDocument)
				.build();
	}
	
	public static Constraint generateConstraint(LeftOperand leftOperand, BinaryOperator operator, RdfResource rightOperand) {
		Constraint constraint = new ConstraintBuilder()
				._leftOperand_(leftOperand)
				._operator_(operator)
				._rightOperand_(rightOperand)
//				._pipEndpoint_(URI.create("pipEndpoint"))
//				._unit_(URI.create("http://dbpedia.org/resource/Euro"))
				.build();
		return constraint;
	}
	
	public static Permission generatePermission(URI target, Action action, ArrayList<Constraint> constraints) {
		Permission permission = new PermissionBuilder()
				._action_(Util.asList(action))
				._target_(target)
				._constraint_(constraints)
				.build();
		return permission;
	}
	
	
	public static TextResource getContractDocument() {
		return new TextResourceBuilder(CONCTRAC_DOCUMENT).build();
	}
	
	public static String getMessageAsString(Message message) {
		try {
			return MultipartMessageProcessor.serializeToJsonLD(message);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static String getObjectAsStringLD(Object toSerialize) {
		try {
			return MultipartMessageProcessor.serializeToJsonLD(toSerialize);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static Permission systemUsePermission(Action action, URI rightOperandReference, URI pipEndpoint) {
		return new PermissionBuilder()
				._action_(Util.asList(action))
				._constraint_(Util.asList(
						new ConstraintBuilder()
						._leftOperand_(LeftOperand.SYSTEM)
						._operator_(BinaryOperator.SAME_AS)
						._rightOperandReference_(rightOperandReference)
						._pipEndpoint_(pipEndpoint)
						.build()
						))
				.build();
	}
	
	public static Permission intervalRestrictDataUSage(Action action) {
		return new PermissionBuilder()
				._action_(Util.asList(action))
				
				
				
				
				
				
				.build();
	}
}
