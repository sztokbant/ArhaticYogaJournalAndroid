package br.net.du.arhaticyogajournal;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import java.net.MalformedURLException;
import java.net.URL;

public class AppUrls {
    // CustomWebApp: Define signed-out URL paths to prevent floating action menu from being
    // displayed
    private static final String[] SIGNED_OUT_URL_PATTERNS =
            new String[] {"/about?s=0", "/password_reset", "/users/pwext", "/welcome"};
    private static final String CURRENT_DOMAIN_KEY = "currentDomain";

    private static final String GENERIC_DOMAIN_PREFIX = "ayj";
    private static final String GENERIC_DOMAIN_SUFFIX = ".herokuapp.com";

    private final String[] allowedDomains;
    private String currentDomain;

    private final SharedPreferences appPreferences;

    public AppUrls(final Context context) {
        final Resources resources = context.getResources();

        final String prodDomain = resources.getString(R.string.prod_domain);
        final String publicDomain = resources.getString(R.string.public_domain);

        allowedDomains = new String[] {prodDomain, publicDomain};

        appPreferences =
                context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);

        currentDomain = appPreferences.getString(CURRENT_DOMAIN_KEY, null);
        if (currentDomain == null) {
            setCurrentDomain(prodDomain);
        }
    }

    public String getCurrentDomain() {
        return currentDomain;
    }

    public void setCurrentDomain(final String currentDomain) {
        this.currentDomain = currentDomain;
        appPreferences.edit().putString(CURRENT_DOMAIN_KEY, currentDomain).apply();
    }

    public String getCurrentUrl() {
        return String.format("%s%s", "https://", currentDomain);
    }

    public boolean isAllowed(final String url) {
        return urlContainsAnyPattern(url, allowedDomains) || isAllowedGeneric(url);
    }

    private boolean isAllowedGeneric(final String stringUrl) {
        String domain;
        try {
            final URL url = new URL(stringUrl);
            domain = url.getHost();
        } catch (final MalformedURLException e) {
            return false;
        }

        return domain.startsWith(GENERIC_DOMAIN_PREFIX)
                && domain.endsWith(GENERIC_DOMAIN_SUFFIX)
                && domain.length()
                        > GENERIC_DOMAIN_PREFIX.length() + GENERIC_DOMAIN_SUFFIX.length();
    }

    public boolean isSignedOutUrl(final String url) {
        return urlContainsAnyPattern(url, SIGNED_OUT_URL_PATTERNS);
    }

    private boolean urlContainsAnyPattern(final String url, final String[] patterns) {
        for (final String pattern : patterns) {
            if (url.contains(pattern)) {
                return true;
            }
        }
        return false;
    }

    public boolean isCurrentDomain(final String webViewUrl) {
        return !currentDomain.isEmpty() && webViewUrl.contains(currentDomain);
    }

    public boolean isDownloadable(final String url) {
        return isAllowed(url) && url.endsWith(".zip");
    }
}
