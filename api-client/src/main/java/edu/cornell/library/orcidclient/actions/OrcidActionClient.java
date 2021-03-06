package edu.cornell.library.orcidclient.actions;

import edu.cornell.library.orcidclient.actions.read.ReadRecordAction;
import edu.cornell.library.orcidclient.actions.read.ReadWorkDetailsAction;
import edu.cornell.library.orcidclient.actions.read.ReadWorksSummariesAction;
import edu.cornell.library.orcidclient.auth.AccessToken;
import edu.cornell.library.orcidclient.context.OrcidClientContext;
import edu.cornell.library.orcidclient.exceptions.OrcidClientException;
import edu.cornell.library.orcidclient.http.HttpWrapper;

/**
 * So far, a facade for creating Action objects, and for testing the "liveness"
 * of access tokens.
 */
public class OrcidActionClient {
	private final OrcidClientContext context;
	private final HttpWrapper httpWrapper;

	public OrcidActionClient(OrcidClientContext context,
			HttpWrapper httpWrapper) {
		this.context = context;
		this.httpWrapper = httpWrapper;
	}

	public void checkConnection() throws OrcidClientException {
		new ActionConnectionChecker(context, httpWrapper).check();
	}

	public boolean isAccessTokenValid(AccessToken accessToken)
			throws OrcidClientException {
		return new AccessTokenValidator(context, httpWrapper)
				.isValid(accessToken);
	}

	public ReadRecordAction createReadRecordAction() {
		return new ReadRecordAction(context, httpWrapper);
	}

	public ReadWorksSummariesAction createReadWorksSummariesAction() {
		return new ReadWorksSummariesAction(context, httpWrapper);
	}

	public ReadWorkDetailsAction createReadWorkDetailsAction() {
		return new ReadWorkDetailsAction(context, httpWrapper);
	}

	public ExternalIdsEditAction createEditExternalIdsAction() {
		return new ExternalIdsEditAction(context, httpWrapper);
	}

	public WorksEditAction createEditWorksAction() {
		return new WorksEditAction(context, httpWrapper);
	}

}
