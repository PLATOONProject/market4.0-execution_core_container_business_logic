package it.eng.idsa.businesslogic.service.impl;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDateTime;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import de.fraunhofer.iais.eis.Action;
import de.fraunhofer.iais.eis.BinaryOperator;
import de.fraunhofer.iais.eis.Constraint;
import de.fraunhofer.iais.eis.ConstraintBuilder;
import de.fraunhofer.iais.eis.ContractAgreement;
import de.fraunhofer.iais.eis.DataRepresentationBuilder;
import de.fraunhofer.iais.eis.InstantBuilder;
import de.fraunhofer.iais.eis.Interval;
import de.fraunhofer.iais.eis.IntervalBuilder;
import de.fraunhofer.iais.eis.Language;
import de.fraunhofer.iais.eis.LeftOperand;
import de.fraunhofer.iais.eis.Message;
import de.fraunhofer.iais.eis.Permission;
import de.fraunhofer.iais.eis.Representation;
import de.fraunhofer.iais.eis.Resource;
import de.fraunhofer.iais.eis.TextResourceBuilder;
import de.fraunhofer.iais.eis.ids.jsonld.Serializer;
import de.fraunhofer.iais.eis.util.ConstraintViolationException;
import de.fraunhofer.iais.eis.util.RdfResource;
import de.fraunhofer.iais.eis.util.TypedLiteral;
import de.fraunhofer.iais.eis.util.Util;
import de.fraunhofer.iese.ids.odrl.policy.library.model.OdrlPolicy;
import de.fraunhofer.iese.ids.odrl.policy.library.model.tooling.IdsOdrlUtil;
import it.eng.idsa.businesslogic.service.ContractNegotiationService;
import it.eng.idsa.businesslogic.util.TestUtilMessageService;
import it.eng.idsa.multipart.processor.MultipartMessageProcessor;

public class ContractNegotiationServiceTest {
	
	private static final URI TARGET = URI.create("http://w3id.org/engrd/connector/artifact/1");

	private ContractNegotiationService service;
	
	private Message contractAgreementMessage;
	private ContractAgreement contractAgreement;
	private Interval interval;
	
	@BeforeEach
	public void setup() throws ConstraintViolationException, DatatypeConfigurationException, URISyntaxException {
		contractAgreementMessage = TestUtilMessageService.getContractAgreementMessage();
		
		interval = new IntervalBuilder()
				._begin_(new InstantBuilder()._dateTime_(TestUtilMessageService.ISSUED).build())
				._end_(new InstantBuilder()._dateTime_(TestUtilMessageService.ISSUED).build())
				.build();
		Constraint constraint = TestUtilMessageService.generateConstraint(
				LeftOperand.POLICY_EVALUATION_TIME, 
				BinaryOperator.TEMPORAL_EQUALS, 
				new RdfResource("2020-12-31T23:59:59.000+00:00", URI.create("xsd:datetimestamp"))
				);
		Constraint constraint2 = TestUtilMessageService.generateConstraint(
				LeftOperand.POLICY_EVALUATION_TIME, 
				BinaryOperator.BEFORE, 
				new RdfResource("2021-12-31T23:59:59.000+00:00", URI.create("xsd:datetimestamp")));
//		Constraint constraint2 = TestUtilMessageService.generateConstraint(LeftOperand.PAY_AMOUNT, 
//				BinaryOperator.EQ, new RdfResource("12", URI.create("http://www.w3.org/2001/XMLSchema#double")));
		Permission permission = TestUtilMessageService.generatePermission(TARGET, Action.USE, Util.asList(constraint, constraint2));
//		Permission permission2 = TestUtilMessageService.generatePermission(TARGET, Action.ANONYMIZE, Util.asList(constraint2));

		contractAgreement = TestUtilMessageService.getContractAgreement(Util.asList(permission));// , permission2
		
		service = new ContractNegotiationServiceImpl();
	}
	
	@Test
	public void testContractNegotiation() throws IOException {
//		service.processContractAgreement(contractAgreementMessage, contractAgreement);
//		String permissionString = MultipartMessageProcessor.serializeToPlainJson(contractAgreement.getPermission());
//		System.out.println(permissionString); 
//		String ca = MultipartMessageProcessor.serializeToPlainJson(contractAgreement);
		String caLD = MultipartMessageProcessor.serializeToJsonLD(contractAgreement);
		System.out.println(caLD);
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
	
	@Test
	public void getPolicy() {
		
		String policy1 =  "{\r\n" + 
				"  \"@context\" : \"https://w3id.org/idsa/contexts/context.jsonld\",\r\n" + 
				"  \"@type\" : \"ids:ContractAgreement\",\r\n" + 
				"  \"@id\" : \"https://mdm-connector.ids.isst.fraunhofer.de/examplecontract/bab-bayern-sample/\",\r\n" + 
				"  \"provider\" : \"https://mdm-connector.ids.isst.fraunhofer.de/\",\r\n" + 
				"  \"consumer\" : \"http://iais.fraunhofer.de/IDS/\",\r\n" + 
				"  \"permissions\" : [ {\r\n" + 
				"    \"@type\" : \"ids:Permission\",\r\n" + 
				"    \"actions\" : [ {\r\n" + 
				"      \"@id\" : \"https://w3id.org/idsa/code/action/USE\"\r\n" + 
				"    } ],       \r\n" + 
				"    \"target\" : { \r\n" + 
				"       \"@id\" : \"http://mdm-connector.ids.isst.fraunhofer.de/artifact/15\"\r\n" + 
				"    },\r\n" + 
				"    \"constraints\" : [ {\r\n" + 
				"      \"@type\" : \"ids:Constraint\",\r\n" + 
				"      \"leftOperand\" : {\r\n" + 
				"        \"@id\" : \"https://w3id.org/idsa/core/DATE_TIME\"\r\n" + 
				"      },\r\n" + 
				"      \"operator\" : {\r\n" + 
				"        \"@id\" : \"https://w3id.org/idsa/core/gt\"\r\n" + 
				"      },\r\n" + 
				"      \"rightOperand\" : {\r\n" + 
				"        \"@value\" : \"\\\"2019-01-01T00:00:00.000+00:00\\\"^^xsd:dateTime\"\r\n" + 
				"      }\r\n" + 
				"    }, {\r\n" + 
				"      \"@type\" : \"ids:Constraint\",\r\n" + 
				"      \"leftOperand\" : {\r\n" + 
				"        \"@id\" : \"https://w3id.org/idsa/core/DATE_TIME\"\r\n" + 
				"      },\r\n" + 
				"      \"operator\" : {\r\n" + 
				"        \"@id\" : \"https://w3id.org/idsa/core/lt\"\r\n" + 
				"      },\r\n" + 
				"      \"rightOperand\" : {\r\n" + 
				"        \"@value\" : \"\\\"2019-12-31T23:59:59.999+00:00\\\"^^xsd:dateTime\"\r\n" + 
				"      }\r\n" + 
				"    } ]\r\n" + 
				"  } ],\r\n" + 
				"  \"contractDocument\" : {\r\n" + 
				"    \"@type\" : \"ids:TextResource\",\r\n" + 
				"    \"@id\" : \"https://creativecommons.org/licenses/by-nc/4.0/legalcode\"\r\n" + 
				"  }\r\n" + 
				"}";
		
		OdrlPolicy odrlPolicy = IdsOdrlUtil.getOdrlPolicy(policy1);
		assertNotNull(odrlPolicy);
	}
	
	
	@Test
	public void createResource() throws IOException, DatatypeConfigurationException {
		Representation dataRepresentation = new DataRepresentationBuilder().build();
		LocalDateTime date =  LocalDateTime.now();
		XMLGregorianCalendar startDate = DatatypeFactory.newInstance().newXMLGregorianCalendar(date.toString());
		XMLGregorianCalendar endDate = DatatypeFactory.newInstance().newXMLGregorianCalendar(date.plusDays(10).toString());

		Interval interval = new IntervalBuilder()
				._begin_(new InstantBuilder()._dateTime_(startDate).build())
				._end_(new InstantBuilder()._dateTime_(endDate).build())
				.build();
		Resource sample = new TextResourceBuilder()
				._title_(Util.asList(new TypedLiteral("A Sample Title")))
				.build();
		Resource textResource = new TextResourceBuilder()
				._description_(Util.asList(new TypedLiteral("same interesting example data of the resource.")))
				._title_(Util.asList(new TypedLiteral("A Title")))
				._keyword_(Util.asList(new TypedLiteral("keyword1"), new TypedLiteral("keyword2")))
				._language_(Util.asList(Language.EN, Language.IT))
				._temporalCoverage_(Util.asList(interval))
				._representation_(Util.asList(dataRepresentation))
				._sample_(Util.asList(sample))
				.build();

		Serializer serializer = new Serializer();
		String serializePlainJson = serializer.serialize(textResource);
		System.out.println(serializePlainJson);
	}
	
	@Test
	public void deserializeResource() throws IOException {
		String resourceString = "{\r\n" + 
				"  \"@context\" : {\r\n" + 
				"    \"ids\" : \"https://w3id.org/idsa/core/\",\r\n" + 
				"    \"idsc\" : \"https://w3id.org/idsa/code/\"\r\n" + 
				"  },\r\n" + 
				"  \"@type\" : \"ids:TextResource\",\r\n" + 
				"  \"@id\" : \"https://w3id.org/idsa/autogen/textResource/59189d03-f98f-479b-a066-22f199ba9bd2\",\r\n" + 
				"  \"ids:description\" : [ {\r\n" + 
				"    \"@value\" : \"same interesting example data of the resource.\",\r\n" + 
				"    \"@type\" : \"http://www.w3.org/2001/XMLSchema#string\"\r\n" + 
				"  } ],\r\n" + 
				"  \"ids:title\" : [ {\r\n" + 
				"    \"@value\" : \"A Title\",\r\n" + 
				"    \"@type\" : \"http://www.w3.org/2001/XMLSchema#string\"\r\n" + 
				"  } ],\r\n" + 
				"  \"ids:sample\" : [ {\r\n" + 
				"    \"@type\" : \"ids:TextResource\",\r\n" + 
				"    \"@id\" : \"https://w3id.org/idsa/autogen/textResource/e71089f0-6976-4bff-b70d-a421212fd18f\",\r\n" + 
				"    \"ids:title\" : [ {\r\n" + 
				"      \"@value\" : \"A Sample Title\",\r\n" + 
				"      \"@type\" : \"http://www.w3.org/2001/XMLSchema#string\"\r\n" + 
				"    } ]\r\n" + 
				"  } ],\r\n" + 
				"  \"ids:representation\" : [ {\r\n" + 
				"    \"@type\" : \"ids:DataRepresentation\",\r\n" + 
				"    \"@id\" : \"https://w3id.org/idsa/autogen/dataRepresentation/c7f255d9-11bf-4cf0-b375-864f88e2de1d\"\r\n" + 
				"  } ],\r\n" + 
				"  \"ids:keyword\" : [ {\r\n" + 
				"    \"@value\" : \"keyword1\",\r\n" + 
				"    \"@type\" : \"http://www.w3.org/2001/XMLSchema#string\"\r\n" + 
				"  }, {\r\n" + 
				"    \"@value\" : \"keyword2\",\r\n" + 
				"    \"@type\" : \"http://www.w3.org/2001/XMLSchema#string\"\r\n" + 
				"  } ],\r\n" + 
				"  \"ids:temporalCoverage\" : [ {\r\n" + 
				"    \"@type\" : \"ids:Interval\",\r\n" + 
				"    \"@id\" : \"https://w3id.org/idsa/autogen/interval/064d6adb-fe63-4d7a-acf8-cf13ff700a86\",\r\n" + 
				"    \"ids:begin\" : {\r\n" + 
				"      \"@type\" : \"ids:Instant\",\r\n" + 
				"      \"@id\" : \"https://w3id.org/idsa/autogen/instant/335d3333-2bec-430b-8ab1-5c15be412bb0\",\r\n" + 
				"      \"ids:dateTime\" : {\r\n" + 
				"        \"@value\" : \"2020-12-23T10:04:07.013+01:00\",\r\n" + 
				"        \"@type\" : \"http://www.w3.org/2001/XMLSchema#dateTimeStamp\"\r\n" + 
				"      }\r\n" + 
				"    },\r\n" + 
				"    \"ids:end\" : {\r\n" + 
				"      \"@type\" : \"ids:Instant\",\r\n" + 
				"      \"@id\" : \"https://w3id.org/idsa/autogen/instant/a1bc854a-9cdf-4ab3-8ce7-467703d489d1\",\r\n" + 
				"      \"ids:dateTime\" : {\r\n" + 
				"        \"@value\" : \"2021-01-02T10:04:07.013+01:00\",\r\n" + 
				"        \"@type\" : \"http://www.w3.org/2001/XMLSchema#dateTimeStamp\"\r\n" + 
				"      }\r\n" + 
				"    }\r\n" + 
				"  } ],\r\n" + 
				"  \"ids:language\" : [ {\r\n" + 
				"    \"@id\" : \"idsc:EN\"\r\n" + 
				"  }, {\r\n" + 
				"    \"@id\" : \"idsc:IT\"\r\n" + 
				"  } ]\r\n" + 
				"}";
		
		Serializer serializer = new Serializer();
		Resource resource = serializer.deserialize(resourceString, Resource.class);
		assertNotNull(resource);
	}
	
	
	@Test
	public void igor( ) throws IOException {
		String contractAgreement = " {    \r\n" + 
				"   \"@context\": {\r\n" + 
				"      \"ids\":\"https://urldefense.com/v3/__https://w3id.org/idsa/core/__;!!LQkDIss!BU6_XKzId9burwjs-TBHxs_nNh4a_r5ancnXDT7UsboZv-xT-u8vslO8r4hLIKpi$ \",\r\n" + 
				"      \"idsc\" : \"https://urldefense.com/v3/__https://w3id.org/idsa/code/__;!!LQkDIss!BU6_XKzId9burwjs-TBHxs_nNh4a_r5ancnXDT7UsboZv-xT-u8vslO8r7G4Vsfn$ \"\r\n" + 
				"   },    \r\n" + 
				"  \"@type\": \"ids:ContractAgreement\",    \r\n" + 
				"  \"@id\": \"https://urldefense.com/v3/__https://w3id.org/idsa/autogen/contract/complex-policy__;!!LQkDIss!BU6_XKzId9burwjs-TBHxs_nNh4a_r5ancnXDT7UsboZv-xT-u8vslO8r6_BZzZ5$ \",    \r\n" + 
				"  \"profile\": \"https://urldefense.com/v3/__http://example.com/ids-profile__;!!LQkDIss!BU6_XKzId9burwjs-TBHxs_nNh4a_r5ancnXDT7UsboZv-xT-u8vslO8r2P8qCdf$ \",    \r\n" + 
				"  \"ids:target\": {\r\n" + 
				"      \"@id\":\"https://urldefense.com/v3/__http://mdm-connector.ids.isst.fraunhofer.de/artifact/15__;!!LQkDIss!BU6_XKzId9burwjs-TBHxs_nNh4a_r5ancnXDT7UsboZv-xT-u8vslO8r_pyBRpG$ \"\r\n" + 
				"   },    \r\n" + 
				"  \"ids:provider\": \"https://urldefense.com/v3/__http://example.com/party/my-party__;!!LQkDIss!BU6_XKzId9burwjs-TBHxs_nNh4a_r5ancnXDT7UsboZv-xT-u8vslO8rzPEpaMm$ \",    \r\n" + 
				"  \"ids:consumer\": \"https://urldefense.com/v3/__http://example.com/party/consumer-party__;!!LQkDIss!BU6_XKzId9burwjs-TBHxs_nNh4a_r5ancnXDT7UsboZv-xT-u8vslO8r-4U2UMb$ \",    \r\n" + 
				"  \"ids:permission\": [{    \r\n" + 
				"\"@type\" : \"ids:Permission\"," +
				"      \"ids:action\": [{\r\n" + 
				"        \"@id\":\"idsc:USE\"\r\n" + 
				"      }],     \r\n" + 
				"      \"ids:constraint\": [{    \r\n" + 
				"        \"@type\":\"ids:Constraint\",  \r\n" + 
				"        \"ids:leftOperand\": \"https://urldefense.com/v3/__https://w3id.org/idsa/core/absoluteSpatialPosition__;!!LQkDIss!BU6_XKzId9burwjs-TBHxs_nNh4a_r5ancnXDT7UsboZv-xT-u8vslO8r_-voThj$ \",  \r\n" + 
				"        \"ids:operator\": \"idsc:EQ\",  \r\n" + 
				"        \"ids:rightOperand\": { \"@value\": \"DE\", \"@type\": \"xsd:anyURI\"        }, \r\n" + 
				"        \"ids:pipEndpoint\": { \"@id\": \"https//pip.com/absolutespatialposition\" } \r\n" + 
				"      }     \r\n" + 
				",{    \r\n" + 
				"        \"@type\":\"ids:Constraint\",  \r\n" + 
				"        \"ids:leftOperand\": \"ids:system\",  \r\n" + 
				"        \"ids:operator\": \"idsc:EQ\",  \r\n" + 
				"        \"ids:rightOperand\": { \"@value\": \"Trusted\", \"@type\": \"xsd:anyURI\"        }, \r\n" + 
				"        \"ids:pipEndpoint\": { \"@id\": \"https//pip.com/system\" } \r\n" + 
				"      }     \r\n" + 
				",{    \r\n" + 
				"        \"@type\":\"ids:Constraint\",  \r\n" + 
				"        \"ids:leftOperand\": \"https://urldefense.com/v3/__https://w3id.org/idsa/core/purpose__;!!LQkDIss!BU6_XKzId9burwjs-TBHxs_nNh4a_r5ancnXDT7UsboZv-xT-u8vslO8ryqR1Qq0$ \",  \r\n" + 
				"        \"ids:operator\": \"idsc:EQ\",  \r\n" + 
				"        \"ids:rightOperand\": { \"@value\": \"https://urldefense.com/v3/__http://example.com/ids-purpose:Marketing__;!!LQkDIss!BU6_XKzId9burwjs-TBHxs_nNh4a_r5ancnXDT7UsboZv-xT-u8vslO8r7-9J8Uo$ \", \"@type\": \"xsd:anyURI\"        }, \r\n" + 
				"        \"ids:pipEndpoint\": { \"@id\": \"https//pip.com/purpose\" } \r\n" + 
				"      }     \r\n" + 
				",{    \r\n" + 
				"        \"@type\":\"ids:Constraint\",  \r\n" + 
				"        \"ids:leftOperand\": \"ids:event\",  \r\n" + 
				"        \"ids:operator\": \"idsc:EQ\",  \r\n" + 
				"        \"ids:rightOperand\": { \"@value\": \"Worldcup\", \"@type\": \"xsd:anyURI\"        }, \r\n" + 
				"        \"ids:pipEndpoint\": { \"@id\": \"https//pip.com/event\" } \r\n" + 
				"      }     \r\n" + 
				",{    \r\n" + 
				"        \"@type\":\"ids:Constraint\",  \r\n" + 
				"        \"ids:leftOperand\": \"ids:count\",  \r\n" + 
				"        \"ids:operator\": \"idsc:LTEQ\",  \r\n" + 
				"        \"ids:rightOperand\": { \"@value\": \"1\", \"@type\": \"xsd:decimal\"        }, \r\n" + 
				"        \"ids:pipEndpoint\": { \"@id\": \"https//pip.com/count\" } \r\n" + 
				"      }     \r\n" + 
				",{    \r\n" + 
				"        \"@type\":\"ids:Constraint\",  \r\n" + 
				"        \"ids:leftOperand\": \"idsc:POLICY_EVALUATION_TIME\",  \r\n" + 
				"        \"ids:operator\": \"idsc:TEMPORAL_EQUALS\",  \r\n" + 
				"        \"ids:rightOperand\": { \r\n" + 
				"         \"@type\": \"ids:interval\", \r\n" + 
				"         \"@value\": { \r\n" + 
				"             \"ids:begin\": {\r\n" + 
				"               \"@value\": \"2021-03-01T00:00:00Z\",\r\n" + 
				"               \"@type\": \"xsd:datetimeStamp\"\r\n" + 
				"            }, \r\n" + 
				"            \"ids:end\": {\r\n" + 
				"               \"@value\": \"2021-03-01T00:00:00Z\",\r\n" + 
				"               \"@type\": \"xsd:datetimeStamp\"\r\n" + 
				"            } \r\n" + 
				"         } \r\n" + 
				"        }, \r\n" + 
				"        \"ids:pipEndpoint\": { \"@id\": \"https//pip.com/policy_evaluation_time\" } \r\n" + 
				"      }     \r\n" + 
				"] \r\n" + 
				"  }] \r\n" + 
				"} ";
		Serializer serializer = new Serializer();
		ContractAgreement ca = serializer.deserialize(contractAgreement, ContractAgreement.class);
		assertNotNull(ca);
		
	}

}
