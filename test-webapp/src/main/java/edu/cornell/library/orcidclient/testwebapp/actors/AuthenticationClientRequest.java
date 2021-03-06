package edu.cornell.library.orcidclient.testwebapp.actors;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.cornell.library.orcidclient.actions.ApiScope;
import edu.cornell.library.orcidclient.auth.OauthProgress;
import edu.cornell.library.orcidclient.auth.OrcidAuthorizationClient;
import edu.cornell.library.orcidclient.exceptions.OrcidClientException;

/**
 * The user has asked for client-based authentication. Start the dance.
 */
public class AuthenticationClientRequest extends AbstractActor {
	private final OrcidAuthorizationClient authClient;

	public AuthenticationClientRequest(HttpServletRequest req,
			HttpServletResponse resp) throws OrcidClientException {
		super(req, resp);
		this.authClient = getAuthorizationClient();
	}

	public void exec() throws IOException, OrcidClientException {
		ApiScope scope = getScopeFromRequest();
		OauthProgress progress = authClient.createProgressObject(scope,
				callbackUrl(), callbackUrl(), callbackUrl());

		resp.sendRedirect(authClient.buildAuthorizationCall(progress));
	}

	private ApiScope getScopeFromRequest() {
		return ApiScope.valueOf(req.getParameter("scope"));
	}

}
