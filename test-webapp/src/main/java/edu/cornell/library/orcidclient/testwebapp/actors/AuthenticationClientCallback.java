package edu.cornell.library.orcidclient.testwebapp.actors;

import static edu.cornell.library.orcidclient.auth.AuthorizationStateProgress.copy;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jtwig.JtwigModel;

import edu.cornell.library.orcidclient.auth.AuthorizationStateProgress;
import edu.cornell.library.orcidclient.auth.OrcidAuthorizationClient;
import edu.cornell.library.orcidclient.exceptions.OrcidClientException;
import edu.cornell.library.orcidclient.util.ParameterMap;

/**
 * When the client-based authentication is complete, show them the results.
 */
public class AuthenticationClientCallback extends AbstractActor {
	private AuthorizationStateProgress progressBefore;
	private AuthorizationStateProgress progressAfter;

	public AuthenticationClientCallback(HttpServletRequest req,
			HttpServletResponse resp) {
		super(req, resp);
	}

	public void exec() throws IOException, OrcidClientException {
		OrcidAuthorizationClient auth = getAuthorizationClient();
		String state = req.getParameter("state");

		progressBefore = copy(auth.getProgressById(state));
		auth.processAuthorizationResponse(new ParameterMap(req));
		progressAfter = copy(auth.getProgressById(state));

		render("/templates/authenticateClientCallback.twig.html", //
				JtwigModel.newModel() //
						.with("progressBefore", progressBefore) //
						.with("progressAfter", progressAfter) //
						.with("mainPageUrl", occ.getWebappBaseUrl()));
	}

}