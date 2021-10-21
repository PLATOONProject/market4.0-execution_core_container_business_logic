package it.eng.idsa.businesslogic.configuration;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.resource.OAuth2ProtectedResourceDetails;


/**
 * 
 * @author Milan Karajovic and Gabriele De Luca
 *
 */

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

	 private static final Logger LOG = LoggerFactory.getLogger(WebSecurityConfigurerAdapter.class);
	 
	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		 

	}

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http.x509();
		
		/*http
		.csrf().disable()
		.requiresChannel()
        //.anyRequest().requiresInsecure()
		.antMatchers("/incoming-data-channel/**").requiresInsecure()
		.antMatchers("/incoming-data-app/**").requiresInsecure()
		.antMatchers("/outcoming-data-app/**").requiresInsecure();*/

        
	}
	
	@Configuration
	@ConditionalOnProperty(prefix = "security.oauth2.client", value = "grant-type", havingValue = "client_credentials")
	public static class OAuthRestTemplateConfigurer {

	  @Bean
	  public OAuth2RestTemplate oauth2RestTemplate(OAuth2ProtectedResourceDetails details) {
	    OAuth2RestTemplate oAuth2RestTemplate = new OAuth2RestTemplate(details);

	    LOG.debug("Begin OAuth2RestTemplate: getAccessToken");
	    /* To validate if required configurations are in place during startup */
	    oAuth2RestTemplate.getAccessToken();
	    LOG.debug("End OAuth2RestTemplate: getAccessToken");
	    return oAuth2RestTemplate;
	  }
	}
}

