package br.net.du.arhaticyogajournal;

import android.content.Context;
import android.content.res.Resources;

public class AppDomains {
	private final String[] allowedDomains;
	private final String defaultUrl;

	public AppDomains(final Context context) {
		final Resources resources = context.getResources();

		final String prodDomain = resources.getString(R.string.prod_domain);
		final String betaDomain = resources.getString(R.string.beta_domain);
		final String usphcDomain = resources.getString(R.string.usphc_domain);
		final String publicDomain = resources.getString(R.string.public_domain);

		allowedDomains = new String[] { prodDomain, betaDomain, usphcDomain, publicDomain };
		defaultUrl = "https://" + prodDomain;
	}

	public String getDefaultUrl() {
		return defaultUrl;
	}

	public boolean isAllowed(final String url) {
		for (final String domain : allowedDomains) {
			if (url.contains(domain)) {
				return true;
			}
		}
		return false;
	}
}
