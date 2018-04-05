/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.libraries.orcidclient.testwebapp.actors;

import static edu.cornell.libraries.orcidclient.context.OrcidClientContext.Setting.CLIENT_ID;
import static org.jtwig.JtwigTemplate.classpathTemplate;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.client.utils.URIBuilder;
import org.jtwig.JtwigModel;

import edu.cornell.libraries.orcidclient.OrcidClientException;

/**
 * TODO
 */
public class AuthenticationRawOffer extends AbstractActor {
	public static final String CALLBACK_STATE = "AuthenticationRawCallback";

	public AuthenticationRawOffer(HttpServletRequest req,
			HttpServletResponse resp) {
		super(req, resp);
	}

	@Override
	public void exec()
			throws IOException, ServletException, OrcidClientException {
		try {
			URI requestUri = new URIBuilder(occ.getAuthCodeRequestUrl())
					.addParameter("client_id", occ.getSetting(CLIENT_ID))
					.addParameter("scope", "/authenticate")
					.addParameter("response_type", "code")
					.addParameter("redirect_uri", occ.getCallbackUrl())
					.addParameter("state", CALLBACK_STATE).build();

			JtwigModel model = JtwigModel.newModel().with("authRequestUrl",
					requestUri.toString());

			String path = "/templates/authenticateRaw.twig.html";
			ServletOutputStream outputStream = resp.getOutputStream();
			classpathTemplate(path).render(model, outputStream);
		} catch (URISyntaxException e) {
			throw new ServletException(e);
		}
	}

}
