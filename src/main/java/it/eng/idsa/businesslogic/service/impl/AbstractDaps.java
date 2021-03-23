package it.eng.idsa.businesslogic.service.impl;

import java.io.IOException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.interfaces.RSAPublicKey;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.auth0.jwk.Jwk;
import com.auth0.jwk.JwkException;
import com.auth0.jwk.JwkProvider;
import com.auth0.jwk.UrlJwkProvider;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.SignatureVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.annotations.VisibleForTesting;

import it.eng.idsa.businesslogic.service.DapsService;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public abstract class AbstractDaps  implements DapsService{
	
	private static final Logger logger = LoggerFactory.getLogger(AbstractDaps.class);
	
	@Autowired
	private OkHttpClient client;

	private String token = null;

	@Value("${application.dapsUrl}")
	private String dapsUrl;
	
	@Value("${application.dapsJWKSUrl}")
	private String dapsJWKSUrl;

	abstract String getJws();
	
	
	

	abstract RequestBody getFormBody(String jws);

	@VisibleForTesting
	String getJwTokenInternal() {

		Response jwtResponse = null;
		try {
			logger.info("Retrieving Dynamic Attribute Token...");

			String jws = getJws();
			logger.info("Request token: " + jws);

			// build form body to embed client assertion into post request
			RequestBody formBody = getFormBody(jws);

			Request request = new Request.Builder()
					.url(dapsUrl)
					.post(formBody)
					.build();
			jwtResponse = client.newCall(request).execute();
			if (!jwtResponse.isSuccessful()) {
				throw new IOException("Unexpected code " + jwtResponse);
			}
			var responseBody = jwtResponse.body();
			if (responseBody == null) {
				throw new Exception("JWT response is null.");
			}
			var jwtString = responseBody.string();
			logger.info("Response body of token request:\n{}", jwtString);
			ObjectNode node = new ObjectMapper().readValue(jwtString, ObjectNode.class);

			if (node.has("access_token")) {
				token = node.get("access_token").asText();
				logger.debug("access_token: {}", token.toString());
			} else {
				logger.info("jwtResponse: {}", jwtResponse.toString());
			}
		} catch (KeyStoreException | NoSuchAlgorithmException | CertificateException | UnrecoverableKeyException e) {
			logger.error("Cannot acquire token:", e);
		} catch (IOException e) {
			logger.error("Error retrieving token:", e);
		} catch (Exception e) {
			logger.error("Something else went wrong:", e);
		} finally {
			if (jwtResponse != null) {
				jwtResponse.close();
			}
		}
		return token;
	}
	
	@Override
	public boolean validateToken(String tokenValue) {
		boolean valid = false;
		DecodedJWT jwt = JWT.decode(tokenValue);
		try {
			Algorithm algorithm = provideAlgorithm(tokenValue);
			algorithm.verify(jwt);
			valid = true;
			if (jwt.getExpiresAt().before(new Date())) {
				valid = false;
				logger.warn("Token expired");
			}
		} catch (SignatureVerificationException e) {
			logger.info("Token did not verified, {}", e);
		}
		return valid;
	}
	
	@Override
	public String getJwtToken() {

		token = getJwTokenInternal();

		if (StringUtils.isNotBlank(token) && validateToken(token)) {
			logger.info("Token is valid: " + token);
		} else {
			logger.info("Token is invalid");
			return null;
		}
		return token;
	}
	
	private Algorithm provideAlgorithm(String tokenValue) {
    	DecodedJWT jwt = JWT.decode(tokenValue);
    	JwkProvider provider = new UrlJwkProvider(dapsJWKSUrl);
		Jwk jwk;
		Algorithm algorithm = null;
		try {
			jwk = provider.get(jwt.getKeyId());
			algorithm = Algorithm.RSA256((RSAPublicKey) jwk.getPublicKey(), null);
		} catch (JwkException e) {
			logger.error("Error while trying to validate token {}", e);
		}
		return algorithm;
    }

}
