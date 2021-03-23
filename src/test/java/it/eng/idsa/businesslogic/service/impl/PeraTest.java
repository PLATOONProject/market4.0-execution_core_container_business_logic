package it.eng.idsa.businesslogic.service.impl;

import static org.mockito.Mockito.when;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.Key;
import java.time.Instant;
import java.util.Date;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

public class PeraTest {
	
	
	@Mock
	KeystoreProvider keystoreProvider;
	
	@Mock
	Key key;
	
	
	
	@BeforeEach
	public void setup() throws IOException, GeneralSecurityException {
		MockitoAnnotations.initMocks(this);
		
	}
	
	
	
	
	
	
	@Test
	public void pera() {
		String connectorUUID = "123";
		Date expiryDate = Date.from(Instant.now().plusSeconds(86400));
    	//@formatter:off
          JwtBuilder jwtb =
                  Jwts.builder()
                          .setIssuer(connectorUUID)
                          .setSubject(connectorUUID)
                          .claim("@context", "https://w3id.org/idsa/contexts/context.jsonld")
                          .claim("@type", "ids:DatRequestToken")
                          .setExpiration(expiryDate)
                          .setIssuedAt(Date.from(Instant.now()))
                          .setAudience("idsc:IDS_CONNECTORS_ALL")
                          .setNotBefore(Date.from(Instant.now()));

        //@formatter:on
          
          when(keystoreProvider.getPrivateKey()).thenReturn(key);
          String jws = jwtb.signWith(SignatureAlgorithm.RS256, keystoreProvider.getPrivateKey()).compact();
	}

}
