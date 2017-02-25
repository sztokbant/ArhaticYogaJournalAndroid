package br.net.du.arhaticyogajournal;

import android.content.Context;
import android.content.res.Resources;

public class AppUrls {
    private final String[] allowedDomains;
    private final String defaultDomain;
    private String currentDomain;

    private final String[] signedOutUrlPatterns;

    public AppUrls(final Context context) {
        final Resources resources = context.getResources();

        final String prodDomain = resources.getString(R.string.prod_domain);
        final String betaDomain = resources.getString(R.string.beta_domain);
        final String usphcDomain = resources.getString(R.string.usphc_domain);
        final String publicDomain = resources.getString(R.string.public_domain);

        allowedDomains = new String[]{prodDomain, betaDomain, usphcDomain, publicDomain};
        defaultDomain = prodDomain;
        currentDomain = "";

        signedOutUrlPatterns = new String[]{"/welcome", "/password_reset", "/users/pwext"};
    }

    public String getDefaultDomain() {
        return defaultDomain;
    }

    public String getDefaultUrl() {
        return "https://" + defaultDomain;
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
    }

    public void clearCurrentDomain() {
        this.currentDomain = "";
    }
}
