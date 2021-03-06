package edu.cornell.library.orcidclient.auth;

import static edu.cornell.library.orcidclient.actions.ApiScope.ACTIVITIES_UPDATE;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

import edu.cornell.library.orcidclient.exceptions.OrcidClientException;
import edu.cornell.library.orcidclient.testing.AbstractTestClass;

/**
 */
public class AccessTokenTest extends AbstractTestClass {
	private static final String SAMPLE_JSON = "" //
			+ "{ \n" //
			+ "  \"access_token\":\"f5af9f51-07e6-4332-8f1a-c0c11c1e3728\", \n" //
			+ "  \"token_type\":\"bearer\", \n" //
			+ "  \"refresh_token\":\"f725f747-3a65-49f6-a231-3e8944ce464d\", \n" //
			+ "  \"expires_in\":631138518, \n" //
			+ "  \"scope\":\"/activities/update\", \n" //
			+ "  \"name\":\"Sofia Garcia\", \n" //
			+ "  \"orcid\":\"0000-0001-2345-6789\" \n" //
			+ "}";

	private static final String MISSING_NAME = "" //
			+ "{ \n" //
			+ "  \"access_token\":\"f5af9f51-07e6-4332-8f1a-c0c11c1e3728\", \n" //
			+ "  \"token_type\":\"bearer\", \n" //
			+ "  \"refresh_token\":\"f725f747-3a65-49f6-a231-3e8944ce464d\", \n" //
			+ "  \"expires_in\":631138518, \n" //
			+ "  \"scope\":\"/activities/update\", \n" //
			+ "  \"orcid\":\"0000-0001-2345-6789\" \n" //
			+ "}";

	private AccessToken token;

	@Test
	public void sampleParser_works() throws OrcidClientException {
		token = AccessToken.parse(SAMPLE_JSON);
		assertEquals("f5af9f51-07e6-4332-8f1a-c0c11c1e3728", token.getToken());
		assertEquals("bearer", token.getType());
		assertEquals("f725f747-3a65-49f6-a231-3e8944ce464d",
				token.getRefreshToken());
		assertEquals(631138518L, token.getExpiresIn());
		assertEquals(ACTIVITIES_UPDATE, token.getScope());
		assertEquals("Sofia Garcia", token.getName());
		assertEquals("0000-0001-2345-6789", token.getOrcid());
		assertEquals(false, token.isShortTerm());
	}

	@Test
	public void missingValueInJson_throwsException()
			throws OrcidClientException {
		expectException(OrcidClientException.class, "no value for");
		token = AccessToken.parse(MISSING_NAME);
	}

}
