package edu.cornell.library.orcidclient.auth;

import edu.cornell.library.orcidclient.actions.ApiScope;
import edu.cornell.library.orcidclient.exceptions.OrcidClientException;

/**
 * Store access tokens and retrieve them by scope.
 * 
 * There is no reference here to ORCID id or session ID, so subclasses must
 * implement that as needed.
 */
public interface AccessTokenCache {
	/**
	 * Add this access token to the cache, replacing any previous access token
	 * with the same scope.
	 */
	void addAccessToken(AccessToken accessToken) throws OrcidClientException;

	/**
	 * Get the access token that appies to this scope, if one is available.
	 * Otherwise, return null.
	 */
	AccessToken getToken(ApiScope scope) throws OrcidClientException;
}
