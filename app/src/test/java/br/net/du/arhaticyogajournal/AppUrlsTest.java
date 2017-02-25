package br.net.du.arhaticyogajournal;

import android.content.Context;
import android.content.res.Resources;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

public class AppUrlsTest {
    private AppUrls appUrls;

    @Before
    public void setUp() throws Exception {
        final Resources resources = Mockito.mock(Resources.class);
        Mockito.when(resources.getString(R.string.prod_domain)).thenReturn("arhaticnet.herokuapp.com");
        Mockito.when(resources.getString(R.string.beta_domain)).thenReturn("ayj-beta.herokuapp.com");
        Mockito.when(resources.getString(R.string.usphc_domain)).thenReturn("ayjournal.herokuapp.com");
        Mockito.when(resources.getString(R.string.public_domain)).thenReturn("arhaticyogajournal.com");

        final Context context = Mockito.mock(Context.class);
        Mockito.when(context.getResources()).thenReturn(resources);

        appUrls = new AppUrls(context);
    }

    @Test
    public void getDefaultDomainTest() throws Exception {
        assertEquals("arhaticnet.herokuapp.com", appUrls.getDefaultDomain());
    }

    @Test
    public void getDefaultUrlTest() throws Exception {
        assertEquals("https://arhaticnet.herokuapp.com", appUrls.getDefaultUrl());
    }

    @Test
    public void isAllowedTest() throws Exception {
        assertTrue(appUrls.isAllowed("https://arhaticnet.herokuapp.com"));
        assertTrue(appUrls.isAllowed("https://arhaticnet.herokuapp.com/stats"));
        assertTrue(appUrls.isAllowed("https://arhaticnet.herokuapp.com/welcome"));

        assertTrue(appUrls.isAllowed("https://ayj-beta.herokuapp.com"));
        assertTrue(appUrls.isAllowed("https://ayj-beta.herokuapp.com/stats"));
        assertTrue(appUrls.isAllowed("https://ayj-beta.herokuapp.com/welcome"));

        assertTrue(appUrls.isAllowed("https://ayjournal.herokuapp.com"));
        assertTrue(appUrls.isAllowed("https://ayjournal.herokuapp.com/stats"));
        assertTrue(appUrls.isAllowed("https://ayjournal.herokuapp.com/welcome"));

        assertTrue(appUrls.isAllowed("http://arhaticyogajournal.com"));

        assertFalse(appUrls.isAllowed("http://www.instituteforinnerstudies.com.ph/"));
        assertFalse(appUrls.isAllowed("https://www.worldpranichealing.com/"));
        assertFalse(appUrls.isAllowed("http://www.pranichealing.com/course/arhatic-yoga"));
        assertFalse(appUrls.isAllowed("http://www.globalpranichealing.com/en/courses/8/"));
    }

    @Test
    public void isSignedOutUrlTest() throws Exception {
        assertFalse(appUrls.isSignedOutUrl("https://arhaticnet.herokuapp.com"));
        assertFalse(appUrls.isSignedOutUrl("https://arhaticnet.herokuapp.com/stats"));

        assertTrue(appUrls.isSignedOutUrl("https://arhaticnet.herokuapp.com/welcome"));
        assertTrue(appUrls.isSignedOutUrl("https://arhaticnet.herokuapp.com/welcome?login_email=user%40example.com"));

        assertTrue(appUrls.isSignedOutUrl("https://arhaticnet.herokuapp.com/users/pwext/1a2b7842207de53103d01fd8b54d7fda4d5bbc52e048532ef8c0fbeadc50edd0"));

        assertTrue(appUrls.isSignedOutUrl("https://arhaticnet.herokuapp.com/password_reset"));
    }

    @Test
    public void currentDomainTest() throws Exception {
        assertFalse(appUrls.isCurrentDomain(""));
        assertFalse(appUrls.isCurrentDomain("ayj-beta.herokuapp.com"));

        appUrls.setCurrentDomain("ayj-beta.herokuapp.com");
        assertTrue(appUrls.isCurrentDomain("ayj-beta.herokuapp.com"));
        assertFalse(appUrls.isCurrentDomain("arhaticnet.herokuapp.com"));

        appUrls.clearCurrentDomain();
        assertFalse(appUrls.isCurrentDomain("ayj-beta.herokuapp.com"));
    }
}