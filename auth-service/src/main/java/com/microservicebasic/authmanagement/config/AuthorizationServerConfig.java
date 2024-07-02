package com.microservicebasic.authmanagement.config;

import com.microservicebasic.authmanagement.exception.CustomResponseExceptionTranslator;
import com.microservicebasic.authmanagement.token.CustomRedisTokenStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.password.Pbkdf2PasswordEncoder;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.token.TokenStore;

@Configuration
@EnableAuthorizationServer
public class AuthorizationServerConfig extends AuthorizationServerConfigurerAdapter {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JedisConnectionFactory jedisConnectionFactory;

    @Value("${oauth2.clientId}")
    private String oauth2ClientId;

    @Value("${oauth2.clientId2}")
    private String oauth2ClientId2;

    @Value("${oauth2.clientSecret}")
    private String oauth2ClientSecret;

    @Value("${oauth2.clientSecret2}")
    private String oauth2ClientSecret2;

    @Autowired
    @Qualifier("redisOperator1")
    private HashOperations<String, Object, Object> redisOperator1;

    PasswordEncoder passwordEncoder = new Pbkdf2PasswordEncoder();

    @Override
    @SuppressWarnings("unchecked")
    public void configure(AuthorizationServerEndpointsConfigurer endpoints) {
        endpoints.authenticationManager(authenticationManager)
                .exceptionTranslator(new CustomResponseExceptionTranslator())
                .tokenStore(tokenStore(jedisConnectionFactory));
    }

    @Override
    public void configure(AuthorizationServerSecurityConfigurer security) {
        security.allowFormAuthenticationForClients()
                .passwordEncoder(passwordEncoder)
                .checkTokenAccess("isAuthenticated()");
    }

    @Override
    public void configure(ClientDetailsServiceConfigurer clients) throws Exception {

        int tokenTimeout = 300;
        int tokenTimeout2 = 900;

        clients.inMemory()
                .withClient(oauth2ClientId)
                .authorizedGrantTypes("client_credentials")
                .scopes("read", "write", "trust")
                .secret(passwordEncoder.encode(oauth2ClientSecret))
                .accessTokenValiditySeconds(tokenTimeout)
                .and()
                .withClient(oauth2ClientId2)
                .authorizedGrantTypes("client_credentials")
                .scopes("read", "write", "trust")
                .secret(passwordEncoder.encode(oauth2ClientSecret2))
                .accessTokenValiditySeconds(tokenTimeout2);

    }

    @Bean
    public TokenStore tokenStore(RedisConnectionFactory redisConnectionFactory) {
        return new CustomRedisTokenStore(redisConnectionFactory);
    }

}
