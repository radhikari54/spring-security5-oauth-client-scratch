package com.example.oauthsecurity5client.autoconfigure;

/**
 * @author <a href="mailto:josh@joshlong.com">Josh Long</a>
 */

import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.security.SecurityAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfiguration;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.configurers.oauth2.client.OAuth2LoginConfigurer;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;

import java.net.URI;
import java.util.Set;


/**
 * @author Joe Grandja
 */
@Configuration
@ConditionalOnWebApplication
@ConditionalOnClass(EnableWebSecurity.class)
@ConditionalOnMissingBean(WebSecurityConfiguration.class)
@ConditionalOnBean(ClientRegistrationRepository.class)
@AutoConfigureBefore(SecurityAutoConfiguration.class)
@AutoConfigureAfter(ClientRegistrationAutoConfiguration.class)
public class OAuth2LoginAutoConfiguration {

    private static final String USER_INFO_URI_PROPERTY = "user-info-uri";

    private static final String USER_NAME_ATTR_NAME_PROPERTY = "user-name-attribute-name";

    @EnableWebSecurity
    protected static class OAuth2LoginSecurityConfiguration extends WebSecurityConfigurerAdapter {

        private final Environment environment;

        protected OAuth2LoginSecurityConfiguration(Environment environment) {
            this.environment = environment;
        }

        // @formatter:off
        @Override
        protected void configure(HttpSecurity http) throws Exception {
            http
                    .authorizeRequests()
                    .antMatchers("/favicon.ico").permitAll()
                    .anyRequest().authenticated()
                    .and()
                    .oauth2Login();

            this.registerUserNameAttributeNames(http.oauth2Login());
        }
        // @formatter:on

        private void registerUserNameAttributeNames(OAuth2LoginConfigurer<HttpSecurity> oauth2LoginConfigurer) throws Exception {
            Set<String> clientPropertyKeys = ClientRegistrationAutoConfiguration.resolveClientPropertyKeys(this.environment);
            for (String clientPropertyKey : clientPropertyKeys) {
                String fullClientPropertyKey = ClientRegistrationAutoConfiguration.CLIENT_PROPERTY_PREFIX + "." + clientPropertyKey;
                if (!this.environment.containsProperty(fullClientPropertyKey + "." + ClientRegistrationAutoConfiguration.CLIENT_ID_PROPERTY)) {
                    continue;
                }
                String userInfoUriValue = this.environment.getProperty(fullClientPropertyKey + "." + USER_INFO_URI_PROPERTY);
                String userNameAttributeNameValue = this.environment.getProperty(fullClientPropertyKey + "." + USER_NAME_ATTR_NAME_PROPERTY);
                if (userInfoUriValue != null && userNameAttributeNameValue != null) {
                    oauth2LoginConfigurer.userInfoEndpoint().userNameAttributeName(userNameAttributeNameValue, URI.create(userInfoUriValue));
                }
            }
        }
    }
}
