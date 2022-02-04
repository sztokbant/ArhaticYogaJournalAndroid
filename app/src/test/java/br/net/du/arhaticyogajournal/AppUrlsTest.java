package br.net.du.arhaticyogajournal;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class AppUrlsTest {
    private static final String CURRENT_DOMAIN_KEY = "currentDomain";

    private static final String PROD_DOMAIN = "arhaticnet.herokuapp.com";
    private static final String BETA_DOMAIN = "ayj-beta.herokuapp.com";
    private static final String GAMMA_DOMAIN = "ayj-gamma.herokuapp.com";
    private static final String USPHC_DOMAIN = "ayjournal.herokuapp.com";
    private static final String PUBLIC_DOMAIN = "arhaticyogajournal.com";

    private static final String PROD_URL = "https://" + PROD_DOMAIN;
    private static final String BETA_URL = "https://" + BETA_DOMAIN;
    private static final String GAMMA_URL = "https://" + GAMMA_DOMAIN;
    private static final String USPHC_URL = "https://" + USPHC_DOMAIN;
    private static final String PUBLIC_URL = "http://" + PUBLIC_DOMAIN;

    private static final String NOT_ALLOWED_URL = "https://www.amazon.com";

    @Mock private Resources resources;

    @Mock private Context context;

    @Mock private SharedPreferences.Editor editor;

    @Mock private SharedPreferences sharedPreferences;

    private AppUrls appUrls;

    @Before
    public void setUp() {
        mockResources();
        mockSharedPreferences();

        when(context.getResources()).thenReturn(resources);
        when(context.getSharedPreferences(anyString(), anyInt())).thenReturn(sharedPreferences);

        appUrls = new AppUrls(context);
    }

    private void mockResources() {
        when(resources.getString(R.string.prod_domain)).thenReturn(PROD_DOMAIN);
        when(resources.getString(R.string.public_domain)).thenReturn(PUBLIC_DOMAIN);
    }

    private void mockSharedPreferences() {
        when(sharedPreferences.getString(eq(CURRENT_DOMAIN_KEY), anyString())).thenReturn(null);

        when(editor.putString(anyString(), anyString())).thenReturn(editor);
        when(sharedPreferences.edit()).thenReturn(editor);
    }

    @Test
    public void isAllowed_ayjSomething_true() {
        assertTrue(appUrls.isAllowed("https://ayjsomething.herokuapp.com"));
        assertTrue(appUrls.isAllowed("https://ayj-something.herokuapp.com"));
    }

    @Test
    public void isAllowed_prodUrl_true() {
        assertTrue(appUrls.isAllowed(PROD_URL));
        assertTrue(appUrls.isAllowed(PROD_URL + "/stats"));
        assertTrue(appUrls.isAllowed(PROD_URL + "/welcome"));
    }

    @Test
    public void isAllowed_betaUrl_true() {
        assertTrue(appUrls.isAllowed(BETA_URL));
        assertTrue(appUrls.isAllowed(BETA_URL + "/stats"));
        assertTrue(appUrls.isAllowed(BETA_URL + "/welcome"));
    }

    @Test
    public void isAllowed_gammaUrl_true() {
        assertTrue(appUrls.isAllowed(GAMMA_URL));
        assertTrue(appUrls.isAllowed(GAMMA_URL + "/stats"));
        assertTrue(appUrls.isAllowed(GAMMA_URL + "/welcome"));
    }

    @Test
    public void isAllowed_usphcUrl_true() {
        assertTrue(appUrls.isAllowed(USPHC_URL));
        assertTrue(appUrls.isAllowed(USPHC_URL + "/stats"));
        assertTrue(appUrls.isAllowed(USPHC_URL + "/welcome"));
    }

    @Test
    public void isAllowed_publicUrl_true() {
        assertTrue(appUrls.isAllowed(PUBLIC_URL));
        assertTrue(appUrls.isAllowed(PUBLIC_URL + "/stats"));
        assertTrue(appUrls.isAllowed(PUBLIC_URL + "/welcome"));
    }

    @Test
    public void isAllowed_externalUrls_false() {
        assertFalse(appUrls.isAllowed("http://www.instituteforinnerstudies.com.ph/"));
        assertFalse(appUrls.isAllowed("https://www.worldpranichealing.com/"));
        assertFalse(appUrls.isAllowed("http://www.pranichealing.com/course/arhatic-yoga"));
        assertFalse(appUrls.isAllowed("http://www.globalpranichealing.com/en/courses/8/"));
    }

    @Test
    public void isAllowed_somethingAyj_false() {
        assertFalse(appUrls.isAllowed("https://somethingayj.herokuapp.com"));
        assertFalse(appUrls.isAllowed("https://something-ayj.herokuapp.com"));
        assertFalse(appUrls.isAllowed("https://something-ayj-something.herokuapp.com"));
    }

    @Test
    public void isAllowed_ayj_false() {
        assertFalse(appUrls.isAllowed("https://ayj.herokuapp.com"));
    }

    @Test
    public void isAllowed_aboutBlank_false() {
        assertFalse(appUrls.isAllowed("about:blank"));
    }

    @Test
    public void isSignedOutUrl_baseUrlAndStats_false() {
        assertFalse(appUrls.isSignedOutUrl(PROD_URL));
        assertFalse(appUrls.isSignedOutUrl(PROD_URL + "/stats"));
    }

    @Test
    public void isSignedOutUrl_welcome_true() {
        assertTrue(appUrls.isSignedOutUrl(PROD_URL + "/welcome"));
        assertTrue(appUrls.isSignedOutUrl(PROD_URL + "/welcome?login_email=user%40example.com"));
    }

    @Test
    public void isSignedOutUrl_pwext_true() {
        assertTrue(
                appUrls.isSignedOutUrl(
                        PROD_URL
                                + "/users/pwext"
                                + "/1a2b7842207de53103d01fd8b54d7fda4d5bbc52e048532ef8c0fbeadc50edd0"));
    }

    @Test
    public void isSignedOutUrl_passwordReset_true() {
        assertTrue(appUrls.isSignedOutUrl(PROD_URL + "/password_reset"));
    }

    @Test
    public void getCurrentDomain_appStart_defaultToProdDomain() {
        // WHEN
        final String currentDomain = appUrls.getCurrentDomain();

        // THEN
        assertEquals(PROD_DOMAIN, currentDomain);
        verify(editor, times(1)).putString(eq(CURRENT_DOMAIN_KEY), eq(PROD_DOMAIN));
        verify(editor, times(1)).apply();
    }

    @Test
    public void getCurrentUrl_appStart_defaultToProdUrl() {
        // WHEN
        final String currentUrl = appUrls.getCurrentUrl();

        // THEN
        assertEquals(PROD_URL, currentUrl);
        verify(editor, times(1)).putString(eq(CURRENT_DOMAIN_KEY), eq(PROD_DOMAIN));
        verify(editor, times(1)).apply();
    }

    @Test
    public void setCurrentDomain_betaDomain_updateCurrentDomainAndProperty() {
        // GIVEN
        assertTrue(appUrls.isCurrentDomain(PROD_DOMAIN));

        // WHEN
        appUrls.setCurrentDomain(BETA_DOMAIN);

        // THEN
        assertTrue(appUrls.isCurrentDomain(BETA_DOMAIN));
        assertFalse(appUrls.isCurrentDomain(PROD_DOMAIN));

        verify(editor, times(1)).putString(eq(CURRENT_DOMAIN_KEY), eq(PROD_DOMAIN));
        verify(editor, times(1)).putString(eq(CURRENT_DOMAIN_KEY), eq(BETA_DOMAIN));
        verify(editor, times(2)).apply();
    }

    @Test
    public void isDownloadable_validDomainZipFile_returnTrue() {
        assertTrue(appUrls.isDownloadable(PROD_URL + "/export_data_download.zip"));
    }

    @Test
    public void isDownloadable_validDomainHtmlFile_returnFalse() {
        assertFalse(appUrls.isDownloadable(PROD_URL + "/export_data_download.html"));
    }

    @Test
    public void isDownloadable_invalidDomainZipFile_returnFalse() {
        assertFalse(appUrls.isDownloadable(NOT_ALLOWED_URL + "/export_data_download.zip"));
    }

    @Test
    public void isDownloadable_invalidDomainHtmlFile_returnFalse() {
        assertFalse(appUrls.isDownloadable(NOT_ALLOWED_URL + "/export_data_download.html"));
    }
}
