/*
 * Copyright 2012-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.security.oauth2.provider.token.store.jwk;

import org.springframework.security.jwt.crypto.sign.SignatureVerifier;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2RefreshToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;
import org.springframework.util.Assert;

import java.util.Collection;

/**
 * A {@link TokenStore} implementation that provides support for verifying the
 * JSON Web Signature (JWS) for a JSON Web Token (JWT) using a JSON Web Key (JWK).
 * <br>
 * <br>
 *
 * This {@link TokenStore} implementation is <b>exclusively</b> meant to be used by a <b>Resource Server</b> as
 * it's sole responsibility is to decode a JWT and verify it's signature (JWS) using the corresponding JWK.
 * <br>
 * <br>
 *
 * <b>NOTE:</b>
 * There are a few operations defined by {@link TokenStore} that are not applicable for a Resource Server.
 * In these cases, the method implementation will explicitly throw a
 * {@link JwkException} reporting <i>&quot;This operation is not supported&quot;</i>.
 * <br>
 * <br>
 *
 * The unsupported operations are as follows:
 * <ul>
 *     <li>{@link #storeAccessToken(OAuth2AccessToken, OAuth2Authentication)}</li>
 *     <li>{@link #removeAccessToken(OAuth2AccessToken)}</li>
 *     <li>{@link #storeRefreshToken(OAuth2RefreshToken, OAuth2Authentication)}</li>
 *     <li>{@link #readRefreshToken(String)}</li>
 *     <li>{@link #readAuthenticationForRefreshToken(OAuth2RefreshToken)}</li>
 *     <li>{@link #removeRefreshToken(OAuth2RefreshToken)}</li>
 *     <li>{@link #removeAccessTokenUsingRefreshToken(OAuth2RefreshToken)}</li>
 *     <li>{@link #getAccessToken(OAuth2Authentication)}</li>
 *     <li>{@link #findTokensByClientIdAndUserName(String, String)}</li>
 *     <li>{@link #findTokensByClientId(String)}</li>
 * </ul>
 * <br>
 *
 * This implementation delegates to an internal instance of a {@link JwtTokenStore} which uses a
 * specialized extension of {@link JwtAccessTokenConverter}, specifically, {@link JwkVerifyingJwtAccessTokenConverter}.
 * The {@link JwkVerifyingJwtAccessTokenConverter} is associated with a {@link JwkDefinitionSource} which is responsible
 * for fetching (and caching) the JWK Set (a set of JWKs) from the URL supplied to the constructor of this implementation.
 * <br>
 * <br>
 *
 * The {@link JwkVerifyingJwtAccessTokenConverter} will verify the JWS in the following step sequence:
 * <br>
 * <br>
 * <ol>
 *     <li>Extract the <b>&quot;kid&quot;</b> parameter from the JWT header.</li>
 *     <li>Find the matching {@link JwkDefinition} from the {@link JwkDefinitionSource} with the corresponding <b>&quot;kid&quot;</b> attribute.</li>
 *     <li>Obtain the {@link SignatureVerifier} associated with the {@link JwkDefinition} via the {@link JwkDefinitionSource} and verify the signature.</li>
 * </ol>
 * <br>
 * <b>NOTE:</b> The algorithms currently supported by this implementation are: RS256, RS384 and RS512.
 * <br>
 * <br>
 *
 * @see JwtTokenStore
 * @see JwkVerifyingJwtAccessTokenConverter
 * @see JwkDefinitionSource
 * @see JwkDefinition
 * @see <a target="_blank" href="https://tools.ietf.org/html/rfc7517">JSON Web Key (JWK)</a>
 * @see <a target="_blank" href="https://tools.ietf.org/html/rfc7519">JSON Web Token (JWT)</a>
 * @see <a target="_blank" href="https://tools.ietf.org/html/rfc7515">JSON Web Signature (JWS)</a>
 *
 * @author Joe Grandja
 */
public class JwkTokenStore implements TokenStore {
	private final JwtTokenStore delegate;

	/**
	 * Creates a new instance using the provided URL as the location for the JWK Set.
	 *
	 * @param jwkSetUrl the JWK Set URL
	 */
	public JwkTokenStore(String jwkSetUrl) {
		Assert.hasText(jwkSetUrl, "jwkSetUrl cannot be empty");
		JwkDefinitionSource jwkDefinitionSource = new JwkDefinitionSource(jwkSetUrl);
		JwkVerifyingJwtAccessTokenConverter accessTokenConverter =
				new JwkVerifyingJwtAccessTokenConverter(jwkDefinitionSource);
		this.delegate = new JwtTokenStore(accessTokenConverter);
	}

	/**
	 * Delegates to the internal instance {@link JwtTokenStore#readAuthentication(OAuth2AccessToken)}.
	 *
	 * @param token the access token
	 * @return the {@link OAuth2Authentication} representation of the access token
	 */
	@Override
	public OAuth2Authentication readAuthentication(OAuth2AccessToken token) {
		return this.delegate.readAuthentication(token);
	}

	/**
	 * Delegates to the internal instance {@link JwtTokenStore#readAuthentication(String)}.
	 *
	 * @param tokenValue the access token value
	 * @return the {@link OAuth2Authentication} representation of the access token
	 */
	@Override
	public OAuth2Authentication readAuthentication(String tokenValue) {
		return this.delegate.readAuthentication(tokenValue);
	}

	/**
	 * This operation is not applicable for a Resource Server
	 * and if called, will throw a {@link JwkException}.
	 *
	 * @throws JwkException reporting this operation is not supported
	 */
	@Override
	public void storeAccessToken(OAuth2AccessToken token, OAuth2Authentication authentication) {
		throw this.operationNotSupported();
	}

	/**
	 * Delegates to the internal instance {@link JwtTokenStore#readAccessToken(String)}.
	 *
	 * @param tokenValue the access token value
	 * @return the {@link OAuth2AccessToken} representation of the access token value
	 */
	@Override
	public OAuth2AccessToken readAccessToken(String tokenValue) {
		return this.delegate.readAccessToken(tokenValue);
	}

	/**
	 * This operation is not applicable for a Resource Server
	 * and if called, will throw a {@link JwkException}.
	 *
	 * @throws JwkException reporting this operation is not supported
	 */
	@Override
	public void removeAccessToken(OAuth2AccessToken token) {
		throw this.operationNotSupported();
	}

	/**
	 * This operation is not applicable for a Resource Server
	 * and if called, will throw a {@link JwkException}.
	 *
	 * @throws JwkException reporting this operation is not supported
	 */
	@Override
	public void storeRefreshToken(OAuth2RefreshToken refreshToken, OAuth2Authentication authentication) {
		throw this.operationNotSupported();
	}

	/**
	 * This operation is not applicable for a Resource Server
	 * and if called, will throw a {@link JwkException}.
	 *
	 * @throws JwkException reporting this operation is not supported
	 */
	@Override
	public OAuth2RefreshToken readRefreshToken(String tokenValue) {
		throw this.operationNotSupported();
	}

	/**
	 * This operation is not applicable for a Resource Server
	 * and if called, will throw a {@link JwkException}.
	 *
	 * @throws JwkException reporting this operation is not supported
	 */
	@Override
	public OAuth2Authentication readAuthenticationForRefreshToken(OAuth2RefreshToken token) {
		throw this.operationNotSupported();
	}

	/**
	 * This operation is not applicable for a Resource Server
	 * and if called, will throw a {@link JwkException}.
	 *
	 * @throws JwkException reporting this operation is not supported
	 */
	@Override
	public void removeRefreshToken(OAuth2RefreshToken token) {
		throw this.operationNotSupported();
	}

	/**
	 * This operation is not applicable for a Resource Server
	 * and if called, will throw a {@link JwkException}.
	 *
	 * @throws JwkException reporting this operation is not supported
	 */
	@Override
	public void removeAccessTokenUsingRefreshToken(OAuth2RefreshToken refreshToken) {
		throw this.operationNotSupported();
	}

	/**
	 * This operation is not applicable for a Resource Server
	 * and if called, will throw a {@link JwkException}.
	 *
	 * @throws JwkException reporting this operation is not supported
	 */
	@Override
	public OAuth2AccessToken getAccessToken(OAuth2Authentication authentication) {
		throw this.operationNotSupported();
	}

	/**
	 * This operation is not applicable for a Resource Server
	 * and if called, will throw a {@link JwkException}.
	 *
	 * @throws JwkException reporting this operation is not supported
	 */
	@Override
	public Collection<OAuth2AccessToken> findTokensByClientIdAndUserName(String clientId, String userName) {
		throw this.operationNotSupported();
	}

	/**
	 * This operation is not applicable for a Resource Server
	 * and if called, will throw a {@link JwkException}.
	 *
	 * @throws JwkException reporting this operation is not supported
	 */
	@Override
	public Collection<OAuth2AccessToken> findTokensByClientId(String clientId) {
		throw this.operationNotSupported();
	}

	private JwkException operationNotSupported() {
		return new JwkException("This operation is not supported.");
	}
}