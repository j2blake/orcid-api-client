/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.libraries.orcidclient.context;

import static edu.cornell.libraries.orcidclient.context.OrcidClientContext.Setting.AUTHORIZED_API_BASE_URL;
import static edu.cornell.libraries.orcidclient.context.OrcidClientContext.Setting.OAUTH_AUTHORIZE_URL;
import static edu.cornell.libraries.orcidclient.context.OrcidClientContext.Setting.OAUTH_TOKEN_URL;
import static edu.cornell.libraries.orcidclient.context.OrcidClientContext.Setting.PUBLIC_API_BASE_URL;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

import edu.cornell.libraries.orcidclient.context.OrcidClientContext.Setting;

/**
 * When an ORCID platform is specified, it defines these four URLs.
 * 
 * A settings file can invoke one of these by name, with API_PLATFORM=SANDBOX,
 * for example, and not need to specify the four URLs.
 */
enum OrcidPlatformUrls {
	//
	SANDBOX("https://pub.sandbox.orcid.org/v2.1/",
			"https://api.sandbox.orcid.org/v2.1/",
			"https://sandbox.orcid.org/oauth/authorize",
			"https://sandbox.orcid.org/oauth/token"),
	//
	PRODUCTION("https://pub.orcid.org/v2.1/", //
			"https://api.orcid.org/v2.1/", //
			"https://orcid.org/oauth/authorize",
			"https://orcid.org/oauth/token");

	private OrcidPlatformUrls(String publicUrl, String memberUrl,
			String oauthUrl, String tokenUrl) {
		Map<Setting, String> map = new EnumMap<>(Setting.class);
		map.put(PUBLIC_API_BASE_URL, publicUrl);
		map.put(AUTHORIZED_API_BASE_URL, memberUrl);
		map.put(OAUTH_AUTHORIZE_URL, oauthUrl);
		map.put(OAUTH_TOKEN_URL, tokenUrl);
		this.urls = Collections.unmodifiableMap(map);
	}

	Map<Setting, String> urls;

	public Map<Setting, String> getUrls() {
		return urls;
	}
}
