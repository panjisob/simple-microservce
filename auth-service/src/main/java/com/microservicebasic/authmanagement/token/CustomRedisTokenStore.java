package com.microservicebasic.authmanagement.token;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.common.ExpiringOAuth2RefreshToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2RefreshToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.AuthenticationKeyGenerator;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.redis.JdkSerializationStrategy;
import org.springframework.security.oauth2.provider.token.store.redis.RedisTokenStore;
import org.springframework.security.oauth2.provider.token.store.redis.RedisTokenStoreSerializationStrategy;
import org.springframework.stereotype.Service;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

import com.microservicebasic.authmanagement.constant.AuthorizationConstant;
import com.microservicebasic.authmanagement.exception.AlreadyLoginException;

@Service
public class CustomRedisTokenStore implements TokenStore {

    @Autowired
    @Qualifier("redisOperator1")
    private HashOperations<String, Object, Object> redisOperator1;

    private static final String ACCESS = "access:";
    private static final String AUTH_TO_ACCESS = "auth_to_access:";
    private static final String AUTH = "auth:";
    private static final String REFRESH_AUTH = "refresh_auth:";
    private static final String ACCESS_TO_REFRESH = "access_to_refresh:";
    private static final String REFRESH = "refresh:";
    private static final String REFRESH_TO_ACCESS = "refresh_to_access:";
    private static final String CLIENT_ID_TO_ACCESS = "client_id_to_access:";
    private static final String UNAME_TO_ACCESS = "uname_to_access:";

    private static final boolean IS_SPRING_DATA_REDIS_2_0 = ClassUtils.isPresent(
            "org.springframework.data.redis.connection.RedisStandaloneConfiguration",
            RedisTokenStore.class.getClassLoader());

    private final RedisConnectionFactory connectionFactory;
    private AuthenticationKeyGenerator authenticationKeyGenerator = new CustomAuthenticationKeyGenerator();
    private RedisTokenStoreSerializationStrategy serializationStrategy = new JdkSerializationStrategy();

    private String prefix = "";

    private Method redisConnectionSet2;

    public CustomRedisTokenStore(RedisConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
        if (IS_SPRING_DATA_REDIS_2_0) {
            this.loadRedisConnectionMethods2();
        }
    }

    public void setAuthenticationKeyGenerator(AuthenticationKeyGenerator authenticationKeyGenerator) {
        this.authenticationKeyGenerator = authenticationKeyGenerator;
    }

    public void setSerializationStrategy(RedisTokenStoreSerializationStrategy serializationStrategy) {
        this.serializationStrategy = serializationStrategy;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    private void loadRedisConnectionMethods2() {
        this.redisConnectionSet2 = ReflectionUtils.findMethod(
                RedisConnection.class, "set", byte[].class, byte[].class);
    }

    private RedisConnection getConnection() {
        return connectionFactory.getConnection();
    }

    private byte[] serialize(Object object) {
        return serializationStrategy.serialize(object);
    }

    private byte[] serializeKey(String object) {
        return serialize(prefix + object);
    }

    private OAuth2AccessToken deserializeAccessToken(byte[] bytes) {
        return serializationStrategy.deserialize(bytes, OAuth2AccessToken.class);
    }

    private OAuth2Authentication deserializeAuthentication(byte[] bytes) {
        return serializationStrategy.deserialize(bytes, OAuth2Authentication.class);
    }

    private OAuth2RefreshToken deserializeRefreshToken(byte[] bytes) {
        return serializationStrategy.deserialize(bytes, OAuth2RefreshToken.class);
    }

    private byte[] serialize(String string) {
        return serializationStrategy.serialize(string);
    }

    private String deserializeString(byte[] bytes) {
        return serializationStrategy.deserializeString(bytes);
    }

    @Override
    public OAuth2AccessToken getAccessToken(OAuth2Authentication authentication) {

        String key = authenticationKeyGenerator.extractKey(authentication);
        byte[] serializedKey = serializeKey(AUTH_TO_ACCESS + key);
        byte[] bytes = null;
        RedisConnection conn = getConnection();
        try {
            bytes = conn.get(serializedKey);
        } finally {
            conn.close();
        }
        OAuth2AccessToken accessToken = deserializeAccessToken(bytes);
        if (accessToken != null) {
            OAuth2Authentication storedAuthentication = readAuthentication(accessToken.getValue());
            if ((storedAuthentication == null || !key.equals(authenticationKeyGenerator.extractKey(storedAuthentication)))) {
                // Keep the stores consistent (maybe the same user is
                // represented by this authentication but the details have
                // changed)
                storeAccessToken(accessToken, authentication);
            } else {
                accessToken = verifyRequestParameter(authentication, accessToken.getValue());
            }
        }
        return accessToken;
    }

    public OAuth2AccessToken verifyRequestParameter(OAuth2Authentication authentication, String tokenValue) {
        Map<String, String> requestParameters = authentication.getOAuth2Request().getRequestParameters();
        String renewSession = requestParameters.get("renew_session");

        if ("true".equals(renewSession)) {
            String channel = requestParameters.get("channel");

            int oauth2AccessTokenTimeoutSec;
            if (AuthorizationConstant.CHANNEL_ADMIN.equals(channel)) {
                oauth2AccessTokenTimeoutSec = 900;
            } else {
                oauth2AccessTokenTimeoutSec = 300;
            }

            var defaultOAuth2AccessToken = new DefaultOAuth2AccessToken(UUID.randomUUID().toString());
            defaultOAuth2AccessToken.setExpiration(new Date(System.currentTimeMillis() + (oauth2AccessTokenTimeoutSec * 1000L)));
            defaultOAuth2AccessToken.setScope(authentication.getOAuth2Request().getScope());
            removeAccessToken(tokenValue);
            return defaultOAuth2AccessToken;
        } else {
            throw new AlreadyLoginException("Already login");
        }
    }

    @Override
    public OAuth2Authentication readAuthentication(OAuth2AccessToken token) {
        return readAuthentication(token.getValue());
    }

    @Override
    public OAuth2Authentication readAuthentication(String token) {
        byte[] bytes = null;
        RedisConnection conn = getConnection();
        try {
            bytes = conn.get(serializeKey(AUTH + token));
        } finally {
            conn.close();
        }
        return deserializeAuthentication(bytes);
    }

    @Override
    public OAuth2Authentication readAuthenticationForRefreshToken(OAuth2RefreshToken token) {
        return readAuthenticationForRefreshToken(token.getValue());
    }

    public OAuth2Authentication readAuthenticationForRefreshToken(String token) {
        RedisConnection conn = getConnection();
        try {
            byte[] bytes = conn.get(serializeKey(REFRESH_AUTH + token));
            return deserializeAuthentication(bytes);
        } finally {
            conn.close();
        }
    }

    @Override
    public void storeAccessToken(OAuth2AccessToken token, OAuth2Authentication authentication) {
        byte[] serializedAccessToken = serialize(token);
        byte[] serializedAuth = serialize(authentication);
        byte[] accessKey = serializeKey(ACCESS + token.getValue());
        byte[] authKey = serializeKey(AUTH + token.getValue());
        byte[] authToAccessKey = serializeKey(AUTH_TO_ACCESS + authenticationKeyGenerator.extractKey(authentication));
        byte[] approvalKey = serializeKey(UNAME_TO_ACCESS + getApprovalKey(authentication));

        RedisConnection conn = getConnection();
        try {
            conn.openPipeline();
            if (IS_SPRING_DATA_REDIS_2_0) {
                try {
                    this.redisConnectionSet2.invoke(conn, accessKey, serializedAccessToken);
                    this.redisConnectionSet2.invoke(conn, authKey, serializedAuth);
                    this.redisConnectionSet2.invoke(conn, authToAccessKey, serializedAccessToken);
                } catch (Exception ex) {
                    throw new RuntimeException(ex.toString());
                }
            } else {
                conn.set(accessKey, serializedAccessToken);
                conn.set(authKey, serializedAuth);
                conn.set(authToAccessKey, serializedAccessToken);
            }
            if (!authentication.isClientOnly()) {
                conn.sAdd(approvalKey, serializedAccessToken);
            }
            if (token.getExpiration() != null) {
                int seconds = token.getExpiresIn();
                conn.expire(accessKey, seconds);
                conn.expire(authKey, seconds);
                conn.expire(authToAccessKey, seconds);
                conn.expire(approvalKey, seconds);
            }
            OAuth2RefreshToken refreshToken = token.getRefreshToken();
            if (refreshToken != null && refreshToken.getValue() != null) {
                byte[] refresh = serialize(token.getRefreshToken().getValue());
                byte[] auth = serialize(token.getValue());
                byte[] refreshToAccessKey = serializeKey(REFRESH_TO_ACCESS + token.getRefreshToken().getValue());
                byte[] accessToRefreshKey = serializeKey(ACCESS_TO_REFRESH + token.getValue());
                if (IS_SPRING_DATA_REDIS_2_0) {
                    try {
                        this.redisConnectionSet2.invoke(conn, refreshToAccessKey, auth);
                        this.redisConnectionSet2.invoke(conn, accessToRefreshKey, refresh);
                    } catch (Exception ex) {
                    	throw new RuntimeException(ex.toString());
                    }
                } else {
                    conn.set(refreshToAccessKey, auth);
                    conn.set(accessToRefreshKey, refresh);
                }
                if (refreshToken instanceof ExpiringOAuth2RefreshToken) {
                    ExpiringOAuth2RefreshToken expiringRefreshToken = (ExpiringOAuth2RefreshToken) refreshToken;
                    Date expiration = expiringRefreshToken.getExpiration();
                    if (expiration != null) {
                        int seconds = (int) ((expiration.getTime() - System.currentTimeMillis()) / 1000);
                        conn.expire(refreshToAccessKey, seconds);
                        conn.expire(accessToRefreshKey, seconds);
                    }
                }
            }
            conn.closePipeline();
        } finally {
            conn.close();
        }
    }

    private static String getApprovalKey(OAuth2Authentication authentication) {
        String userName = authentication.getUserAuthentication() == null ? ""
                : authentication.getUserAuthentication().getName();
        return getApprovalKey(authentication.getOAuth2Request().getClientId(), userName);
    }

    private static String getApprovalKey(String clientId, String userName) {
        return clientId + (userName == null ? "" : ":" + userName);
    }

    @Override
    public void removeAccessToken(OAuth2AccessToken accessToken) {
        removeAccessToken(accessToken.getValue());
    }

    @Override
    public OAuth2AccessToken readAccessToken(String tokenValue) {
        byte[] key = serializeKey(ACCESS + tokenValue);
        byte[] bytes = null;
        RedisConnection conn = getConnection();
        try {
            bytes = conn.get(key);
        } finally {
            conn.close();
        }
        return deserializeAccessToken(bytes);
    }

    public void removeAccessToken(String tokenValue) {
        byte[] accessKey = serializeKey(ACCESS + tokenValue);
        byte[] authKey = serializeKey(AUTH + tokenValue);
        byte[] accessToRefreshKey = serializeKey(ACCESS_TO_REFRESH + tokenValue);
        RedisConnection conn = getConnection();
        try {
            conn.openPipeline();
            conn.get(accessKey);
            conn.get(authKey);
            conn.del(accessKey);
            conn.del(accessToRefreshKey);
            // Don't remove the refresh token - it's up to the caller to do that
            conn.del(authKey);
            List<Object> results = conn.closePipeline();
            byte[] access = (byte[]) results.get(0);
            byte[] auth = (byte[]) results.get(1);

            OAuth2Authentication authentication = deserializeAuthentication(auth);
            if (authentication != null) {
                String key = authenticationKeyGenerator.extractKey(authentication);
                byte[] authToAccessKey = serializeKey(AUTH_TO_ACCESS + key);
                byte[] unameKey = serializeKey(UNAME_TO_ACCESS + getApprovalKey(authentication));
                conn.openPipeline();
                conn.del(authToAccessKey);
                conn.sRem(unameKey, access);
                conn.del(serialize(ACCESS + key));
                conn.closePipeline();
            }
        } finally {
            conn.close();
        }
    }

    @Override
    public void storeRefreshToken(OAuth2RefreshToken refreshToken, OAuth2Authentication authentication) {
        byte[] refreshKey = serializeKey(REFRESH + refreshToken.getValue());
        byte[] refreshAuthKey = serializeKey(REFRESH_AUTH + refreshToken.getValue());
        byte[] serializedRefreshToken = serialize(refreshToken);
        RedisConnection conn = getConnection();
        try {
            conn.openPipeline();
            if (IS_SPRING_DATA_REDIS_2_0) {
                try {
                    this.redisConnectionSet2.invoke(conn, refreshKey, serializedRefreshToken);
                    this.redisConnectionSet2.invoke(conn, refreshAuthKey, serialize(authentication));
                } catch (Exception ex) {
                	throw new RuntimeException(ex.toString());
                }
            } else {
                conn.set(refreshKey, serializedRefreshToken);
                conn.set(refreshAuthKey, serialize(authentication));
            }
            if (refreshToken instanceof ExpiringOAuth2RefreshToken) {
                ExpiringOAuth2RefreshToken expiringRefreshToken = (ExpiringOAuth2RefreshToken) refreshToken;
                Date expiration = expiringRefreshToken.getExpiration();
                if (expiration != null) {
                    int seconds = (int) ((expiration.getTime() - System.currentTimeMillis()) / 1000);
                    conn.expire(refreshKey, seconds);
                    conn.expire(refreshAuthKey, seconds);
                }
            }
            conn.closePipeline();
        } finally {
            conn.close();
        }
    }

    @Override
    public OAuth2RefreshToken readRefreshToken(String tokenValue) {
        byte[] key = serializeKey(REFRESH + tokenValue);
        byte[] bytes = null;
        RedisConnection conn = getConnection();
        try {
            bytes = conn.get(key);
        } finally {
            conn.close();
        }
        return deserializeRefreshToken(bytes);
    }

    @Override
    public void removeRefreshToken(OAuth2RefreshToken refreshToken) {
        removeRefreshToken(refreshToken.getValue());
    }

    public void removeRefreshToken(String tokenValue) {
        byte[] refreshKey = serializeKey(REFRESH + tokenValue);
        byte[] refreshAuthKey = serializeKey(REFRESH_AUTH + tokenValue);
        byte[] refresh2AccessKey = serializeKey(REFRESH_TO_ACCESS + tokenValue);
        byte[] access2RefreshKey = serializeKey(ACCESS_TO_REFRESH + tokenValue);
        RedisConnection conn = getConnection();
        try {
            conn.openPipeline();
            conn.del(refreshKey);
            conn.del(refreshAuthKey);
            conn.del(refresh2AccessKey);
            conn.del(access2RefreshKey);
            conn.closePipeline();
        } finally {
            conn.close();
        }
    }

    @Override
    public void removeAccessTokenUsingRefreshToken(OAuth2RefreshToken refreshToken) {
        removeAccessTokenUsingRefreshToken(refreshToken.getValue());
    }

    private void removeAccessTokenUsingRefreshToken(String refreshToken) {
        byte[] key = serializeKey(REFRESH_TO_ACCESS + refreshToken);
        List<Object> results = null;
        RedisConnection conn = getConnection();
        try {
            conn.openPipeline();
            conn.get(key);
            conn.del(key);
            results = conn.closePipeline();
        } finally {
            conn.close();
        }
        
        byte[] bytes = (byte[]) results.get(0);
        String accessToken = deserializeString(bytes);
        if (accessToken != null) {
            removeAccessToken(accessToken);
        }
    }

    private List<byte[]> getByteLists(byte[] approvalKey, RedisConnection conn) {
        List<byte[]> byteList = new ArrayList<>();
        Cursor<byte[]> cursor = conn.sScan(approvalKey, ScanOptions.NONE);
        while (cursor.hasNext()) {
            byteList.add(cursor.next());
        }
        return byteList;
    }

    @Override
    public Collection<OAuth2AccessToken> findTokensByClientIdAndUserName(String clientId, String userName) {
        byte[] approvalKey = serializeKey(UNAME_TO_ACCESS + getApprovalKey(clientId, userName));
        List<byte[]> byteList = null;
        RedisConnection conn = getConnection();
        try {
            byteList = getByteLists(approvalKey, conn);
        } finally {
            conn.close();
        }
        if (CollectionUtils.isEmpty(byteList)) {
            return Collections.<OAuth2AccessToken>emptySet();
        }
        List<OAuth2AccessToken> accessTokens = new ArrayList<>(byteList.size());
        for (byte[] bytes : byteList) {
            OAuth2AccessToken accessToken = deserializeAccessToken(bytes);
            accessTokens.add(accessToken);
        }
        return Collections.<OAuth2AccessToken>unmodifiableCollection(accessTokens);
    }

    @Override
    public Collection<OAuth2AccessToken> findTokensByClientId(String clientId) {
        byte[] key = serializeKey(CLIENT_ID_TO_ACCESS + clientId);
        List<byte[]> byteList = null;
        RedisConnection conn = getConnection();
        try {
            byteList = getByteLists(key, conn);
        } finally {
            conn.close();
        }
        if (CollectionUtils.isEmpty(byteList)) {
            return Collections.<OAuth2AccessToken>emptySet();
        }
        List<OAuth2AccessToken> accessTokens = new ArrayList<>(byteList.size());
        for (byte[] bytes : byteList) {
            OAuth2AccessToken accessToken = deserializeAccessToken(bytes);
            accessTokens.add(accessToken);
        }
        return Collections.<OAuth2AccessToken>unmodifiableCollection(accessTokens);
    }

}
