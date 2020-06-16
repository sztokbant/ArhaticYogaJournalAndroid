package br.net.du.arhaticyogajournal;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;

public class AppUrls {
    private static final String CURRENT_DOMAIN_KEY = "br.net.du.arhaticyogajournal.currentDomain";

    private final String[] allowedDomains;
    private String currentDomain;

    private final String[] signedOutUrlPatterns;

    private final SharedPreferences sharedPreferences;

    public AppUrls(final Context context) {
        final Resources resources = context.getResources();

        final String prodDomain = resources.getString(R.string.prod_domain);
        final String betaDomain = resources.getString(R.string.beta_domain);
        final String gammaDomain = resources.getString(R.string.gamma_domain);
        final String usphcDomain = resources.getString(R.string.usphc_domain);
        final String publicDomain = resources.getString(R.string.public_domain);

        allowedDomains = new String[]{prodDomain, betaDomain, gammaDomain, usphcDomain, publicDomain};

        sharedPreferences = context.getSharedPreferences(CURRENT_DOMAIN_KEY, Context.MODE_PRIVATE);

        currentDomain = sharedPreferences.getString(CURRENT_DOMAIN_KEY, null);
        if (currentDomain == null) {
            setCurrentDomain(prodDomain);
        }

        signedOutUrlPatterns = new String[]{"/welcome", "/password_reset", "/users/pwext"};
    }

    public String getCurrentDomain() {
        return currentDomain;
    }

    public String getCurrentUrl() {
        return String.format("%s%s", "https://", currentDomain);
    }

    public boolean isAllowed(final String url) {
        return urlContainsAnyPattern(url, allowedDomains);
    }

    public boolean isSignedOutUrl(final String url) {
        return urlContainsAnyPattern(url, signedOutUrlPatterns);
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

    public void setCurrentDomain(final String currentDomain) {
        this.currentDomain = currentDomain;
        sharedPreferences.edit().putString(CURRENT_DOMAIN_KEY, currentDomain).apply();
    }
}
