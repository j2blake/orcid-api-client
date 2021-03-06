package edu.cornell.library.orcidclient.auth;

import static edu.cornell.library.orcidclient.actions.ApiScope.READ_PUBLIC;
import static edu.cornell.library.orcidclient.auth.OauthProgress.FailureCause.BAD_ACCESS_TOKEN;
import static edu.cornell.library.orcidclient.auth.OauthProgress.FailureCause.ERROR_STATUS;
import static edu.cornell.library.orcidclient.auth.OauthProgress.FailureCause.INVALID_STATE;
import static edu.cornell.library.orcidclient.auth.OauthProgress.FailureCause.NO_AUTH_CODE;
import static edu.cornell.library.orcidclient.auth.OauthProgress.FailureCause.UNKNOWN;
import static edu.cornell.library.orcidclient.auth.OauthProgress.State.DENIED;
import static edu.cornell.library.orcidclient.auth.OauthProgress.State.FAILURE;
import static edu.cornell.library.orcidclient.auth.OauthProgress.State.SEEKING_AUTHORIZATION;
import static edu.cornell.library.orcidclient.auth.OauthProgress.State.SUCCESS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import edu.cornell.library.orcidclient.actions.ApiScope;
import edu.cornell.library.orcidclient.auth.OauthProgress.FailureCause;
import edu.cornell.library.orcidclient.auth.OauthProgress.State;
import edu.cornell.library.orcidclient.exceptions.OrcidClientException;
import edu.cornell.library.orcidclient.testing.AbstractTestClass;
import edu.cornell.library.orcidclient.util.ParameterMap;

/**
 */
public class OrcidAuthorizationClientTest extends AbstractTestClass {
	private static final URI SUCCESS_URL = newURI("http://this.test/success");
	private static final URI FAILURE_URL = newURI("http://this.test/failure");
	private static final URI DENIED_URL = newURI("http://this.test/denied");

	private static final String VALID_TOKEN_STRING = "" //
			+ "{" //
			+ "\"access_token\":\"83db60e2-cee6-4ab8-a0cc-6fc139c233cb\"," //
			+ "\"token_type\":\"bearer\"," //
			+ "\"refresh_token\":\"7b0967c7-e1df-4686-8768-1004b51f2b06\"," //
			+ "\"expires_in\":3599," //
			+ "\"scope\":\"/read-limited\"," //
			+ "\"name\":\"Beau Zeaux\"," //
			+ "\"orcid\":\"0000-0003-0550-2950\"" //
			+ "}";

	private static final String INVALID_TOKEN_STRING = "" //
			+ "{" //
			+ "\"token_type\":\"bearer\"," //
			+ "\"refresh_token\":\"7b0967c7-e1df-4686-8768-1004b51f2b06\"," //
			+ "\"expires_in\":3599," //
			+ "\"scope\":\"/read-limited\"," //
			+ "\"name\":\"Beau Zeaux\"," //
			+ "\"orcid\":\"0000-0003-0550-2950\"" //
			+ "}";

	private OrcidAuthorizationClient client;
	private StubHttpWrapper httpClient;
	private StubOrcidAuthorizationClientContext context;
	private StubOauthProgressCache progressCache;
	private StubAccessTokenCache tokenCache;

	private OauthProgress progress;
	private ParameterMap parameters;
	private String redirectUrl;
	private StringWriter clientLog;

	@Before
	public void setup() {
		context = new StubOrcidAuthorizationClientContext();
		context.setAccessTokenRequestUrl("http://test/access_token");

		httpClient = new StubHttpWrapper();

		progressCache = new StubOauthProgressCache();
		tokenCache = new StubAccessTokenCache();
		client = new OrcidAuthorizationClient(context, progressCache,
				tokenCache, httpClient);

		clientLog = new StringWriter();
		captureLogOutput(OrcidAuthorizationClient.class, clientLog, true);
	}

	@Test
	public void createProgressObject_writesToCache()
			throws OrcidClientException {
		progress = client.createProgressObject(ApiScope.ACTIVITIES_UPDATE,
				SUCCESS_URL, FAILURE_URL, DENIED_URL);
		assertNotNull(progress);
		assertEquals(1, progressCache.getList().size());
	}

	@Test
	public void noStateParameter_throwsException() throws OrcidClientException {
		// Can't update status in cache -- no ID to match against
		expectException(OrcidClientException.class,
				"did not contain a 'state'");

		parameters = new Parameters().toMap();
		client.processAuthorizationResponse(parameters);
	}

	@Test
	public void stateDoesntMatchExistingProgress_throwsException()
			throws OrcidClientException {
		// Shouldn't update status in cache -- ID doesn't match anything
		expectException(OrcidClientException.class,
				"Not seeking authorization");

		parameters = new Parameters() //
				.add("state", "BOGUS_ID").toMap();
		client.processAuthorizationResponse(parameters);
	}

	@Test
	public void stateMatchesExistingSuccess_recordFailure()
			throws OrcidClientException {
		progress = basicProgress(SUCCESS);
		progressCache.set(progress);

		parameters = new Parameters() //
				.add("state", progress.getId()).toMap();
		redirectUrl = client.processAuthorizationResponse(parameters);

		assertFailureRecordInCache(INVALID_STATE);
	}

	@Test
	public void errorWithDescription_recordFailure()
			throws OrcidClientException {
		progress = basicProgress(SEEKING_AUTHORIZATION);
		progressCache.set(progress);

		parameters = new Parameters() //
				.add("state", progress.getId()) //
				.add("error", "the_error_code") //
				.add("error_description", "the_description").toMap();
		redirectUrl = client.processAuthorizationResponse(parameters);

		assertFailureRecordInCache(ERROR_STATUS);
	}

	@Test
	public void errorWithoutDescription_recordFailure()
			throws OrcidClientException {
		progress = basicProgress(SEEKING_AUTHORIZATION);
		progressCache.set(progress);

		parameters = new Parameters() //
				.add("state", progress.getId()) //
				.add("error", "the_error_code").toMap();
		redirectUrl = client.processAuthorizationResponse(parameters);

		assertFailureRecordInCache(ERROR_STATUS);
	}

	@Test
	public void missingCode_recordFailure() throws OrcidClientException {
		progress = basicProgress(SEEKING_AUTHORIZATION);
		progressCache.set(progress);

		parameters = new Parameters() //
				.add("state", progress.getId()).toMap();
		redirectUrl = client.processAuthorizationResponse(parameters);

		assertFailureRecordInCache(NO_AUTH_CODE);
	}

	@Test
	public void userDenies_recordDenied() throws OrcidClientException {
		progress = basicProgress(SEEKING_AUTHORIZATION);
		progressCache.set(progress);

		parameters = new Parameters() //
				.add("state", progress.getId()) //
				.add("error", "access_denied") //
				.add("error_description", "User denied access").toMap();
		redirectUrl = client.processAuthorizationResponse(parameters);

		assertDeniedRecordInCache();
	}

	@Test
	public void tokenRequestReturnsErrorCode_recordCodeAndFailure()
			throws OrcidClientException {
		progress = basicProgress(SEEKING_AUTHORIZATION);
		progressCache.set(progress);

		httpClient.setResponse(400, VALID_TOKEN_STRING);

		parameters = new Parameters() //
				.add("state", progress.getId()) //
				.add("code", "theCode").toMap();
		redirectUrl = client.processAuthorizationResponse(parameters);

		assertCodeRecordInCache("theCode");
		assertFailureRecordInCache(UNKNOWN);
	}

	@Test
	public void tokenRequestReturnsInvalidJson_recordCodeAndFailure()
			throws OrcidClientException {
		progress = basicProgress(SEEKING_AUTHORIZATION);
		progressCache.set(progress);

		httpClient.setResponse(200, INVALID_TOKEN_STRING);

		parameters = new Parameters() //
				.add("state", progress.getId()) //
				.add("code", "theCode").toMap();
		redirectUrl = client.processAuthorizationResponse(parameters);

		assertCodeRecordInCache("theCode");
		assertFailureRecordInCache(BAD_ACCESS_TOKEN);
	}

	@Test
	public void tokenRequestReturnsOK_recordCodeAndSuccess()
			throws OrcidClientException {
		progress = basicProgress(SEEKING_AUTHORIZATION);
		progressCache.set(progress);

		httpClient.setResponse(200, VALID_TOKEN_STRING);

		parameters = new Parameters() //
				.add("state", progress.getId()) //
				.add("code", "theCode").toMap();
		redirectUrl = client.processAuthorizationResponse(parameters);

		assertCodeRecordInCache("theCode");
		assertSuccessRecordInCache();
	}

	/**
	 * Test plan:
	 * 
	 * <pre>
	 * processAuthorizationResponse:
	 *   successful code writes to the cache - inspect the result
	 *   
	 *   failed token writes to the cache - inspect the result
	 *   successful token writes to the cache - inspect the result
	 *   
	 * getAccessTokenFromAuthCode:
	 *   result code not 200
	 *   JSON response is invalid
	 * </pre>
	 */

	// ----------------------------------------------------------------------
	// Helper methods
	// ----------------------------------------------------------------------

	private static URI newURI(String string) {
		try {
			return new URI(string);
		} catch (URISyntaxException e) {
			throw new RuntimeException();
		}
	}

	private OauthProgress basicProgress(State state) {
		OauthProgress p = new OauthProgress(READ_PUBLIC, SUCCESS_URL,
				FAILURE_URL, DENIED_URL);
		p.addState(state);
		return p;
	}

	private void assertCodeRecordInCache(String expectedCode)
			throws OrcidClientException {
		OauthProgress cached = progressCache.getByScope(READ_PUBLIC);
		assertEquals(expectedCode, cached.getAuthorizationCode());
	}

	private void assertFailureRecordInCache(FailureCause failureCause)
			throws OrcidClientException {
		// The change of state was a failure with the expected cause?
		assertEquals(FAILURE_URL.toString(), redirectUrl);
		OauthProgress cached = progressCache.getByScope(READ_PUBLIC);
		assertEquals(FAILURE, cached.getState());
		assertEquals(failureCause, cached.getFailureCause());
	}

	private void assertDeniedRecordInCache() throws OrcidClientException {
		// Returned a failure URL for redirecting?
		assertEquals(DENIED_URL.toString(), redirectUrl);
		assertEquals(DENIED, progressCache.getByScope(READ_PUBLIC).getState());
	}

	private void assertSuccessRecordInCache() throws OrcidClientException {
		// The change of state was a Success?
		assertEquals(SUCCESS_URL.toString(), redirectUrl);
		assertEquals(SUCCESS, progressCache.getByScope(READ_PUBLIC).getState());
	}

	// ----------------------------------------------------------------------
	// Helper classes
	// ----------------------------------------------------------------------

	private static class Parameters {
		private final Map<String, List<String>> map = new HashMap<>();

		public Parameters add(String key, String value) {
			if (!map.containsKey(key)) {
				map.put(key, new ArrayList<>());
			}
			map.get(key).add(value);
			return this;
		}

		public ParameterMap toMap() {
			return new ParameterMap(map);
		}
	}
}
