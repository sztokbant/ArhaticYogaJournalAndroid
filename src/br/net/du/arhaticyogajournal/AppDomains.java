package br.net.du.arhaticyogajournal;

public class AppDomains {
	private static final String FQDN = "arhaticnet.herokuapp.com";
	private static final String FQDN_BETA = "ayj-beta.herokuapp.com";
	private static final String FQDN_USPHC = "ayjournal.herokuapp.com";

	private static final String[] ALLOWED_FQDNS = { FQDN, FQDN_BETA, FQDN_USPHC, "arhaticyogajournal.com" };

	private final String defaultUrl = "https://" + FQDN;

	public String getDefaultUrl() {
		return defaultUrl;
	}

	public boolean isAllowed(final String url) {
		for (String fqdn : ALLOWED_FQDNS) {
			if (url.contains(fqdn)) {
				return true;
			}
		}
		return false;
	}
}
