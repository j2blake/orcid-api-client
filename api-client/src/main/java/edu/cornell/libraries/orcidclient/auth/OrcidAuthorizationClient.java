/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.libraries.orcidclient.auth;

import java.net.URI;

import javax.servlet.http.HttpServletRequest;

import edu.cornell.libraries.orcidclient.OrcidClientException;
import edu.cornell.libraries.orcidclient.actions.ApiScope;

/**
 * The tool that will help us walk through the 3-legged OAuth negotiation.
 * 
 * This is based on what has happened in this HTTP session, and what is
 * available from the persistent storage.
 */
public abstract class OrcidAuthorizationClient {
	/**
	 * Reflects the authorization status for a specified scope.
	 * 
	 * Note that we only try to get authorized if there is no AccessToken for
	 * this scope in the persistent storage.
	 */
	public enum AuthProcessResolution {
		/**
		 * We haven't tried to get authorized.
		 */
		NONE,

		/**
		 * We have an AccessToken.
		 */
		SUCCESS,

		/**
		 * The user denied authorization.
		 */
		DENIED,

		/**
		 * We tried to get authorization but encountered a system failure.
		 */
		FAILURE
	}

	/**
	 * Undo any progress that was made for this scope during this session.
	 */
	public abstract void resetProgress(ApiScope scope);

	/**
	 * Update the authorization status to reflect this respons from our
	 * authorization request.
	 * 
	 * Return the redirect URL for either success or failure, as appropriate.
	 * @throws OrcidClientException
	 */
	public abstract String processAuthorizationResponse(HttpServletRequest req)
			throws OrcidClientException;

	/**
	 * Find out where we stand for this scope.
	 */
	public abstract AuthProcessResolution getAuthProcessResolution(
			ApiScope scope);

	/**
	 * Create a progress object to use in seeking authorization for this scope.
	 * Add it to the cache, so it can be used to track the progress of the
	 * authorization proceess.
	 * 
	 * @param returnUrl
	 *            When the negotiation is complete, redirect the browser to this
	 *            URL.
	 * @throws OrcidClientException
	 */
	public abstract AuthorizationStateProgress createProgressObject(
			ApiScope scope, URI returnUrl) throws OrcidClientException;

	/**
	 * Create a progress object to use in seeking authorization for this scope.
	 * Add it to the cache, so it can be used to track the progress of the
	 * authorization proceess.
	 * 
	 * @param successUrl
	 *            If the negotiation succeeds, redirect the browser to this URL.
	 * @param failureUrl
	 *            If the negotiation fails, redirect the browser to this URL.
	 * @throws OrcidClientException
	 */
	public abstract AuthorizationStateProgress createProgressObject(
			ApiScope scope, URI successUrl, URI failureUrl)
			throws OrcidClientException;

	/**
	 * Create a URL with appropriate parameters to seek authorization for this
	 * authorization process.
	 * 
	 * The URL can be sent to the browser as a re-direct to kick off the OAuth
	 * negotiation.
	 * 
	 * @throws OrcidClientException
	 */
	public abstract String buildAuthorizationCall(
			AuthorizationStateProgress progress) throws OrcidClientException;

	/**
	 * Get the AccessToken for this scope. It might have been obtained during
	 * this session, or it might be from the persistent storage.
	 * 
	 * @throws IllegalStateException
	 *             If the resolution for this scope is not SUCCESS.
	 */
	public abstract AccessToken getAccessToken(ApiScope scope)
			throws IllegalStateException;

	/**
	 * Look in the cache for a progress indicator with this ID.
	 * 
	 * @returns the requested progress indicator, or null.
	 */
	public abstract AuthorizationStateProgress getProgressById(String id);
}
