package br.net.du.arhaticyogajournal;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.MailTo;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.CookieSyncManager;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;

import java.net.MalformedURLException;
import java.net.URL;

public class MainActivity extends Activity {
    private AppUrls appUrls;
    private SwipeRefreshLayout swipeRefresh;
    private WebView webView;

    private FloatingActionMenu floatingActionMenu;
    private FloatingActionButton newPracticeExecutionButton;
    private FloatingActionButton newTithingButton;
    private FloatingActionButton newServiceButton;
    private FloatingActionButton newStudyButton;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        appUrls = new AppUrls(getBaseContext());

        buildSwipeRefreshLayout();

        CookieSyncManager.createInstance(getBaseContext());

        buildWebView();
        populateWebView(savedInstanceState);

        floatingActionMenu = (FloatingActionMenu) findViewById(R.id.floating_action_menu);
        newPracticeExecutionButton = (FloatingActionButton) findViewById(R.id.new_practice_execution);
        newTithingButton = (FloatingActionButton) findViewById(R.id.new_tithing);
        newServiceButton = (FloatingActionButton) findViewById(R.id.new_service);
        newStudyButton = (FloatingActionButton) findViewById(R.id.new_study);

        createFloatingActionMenu();
    }

    /**
     * Builds SwipeRefreshLayout object to reload the WebView on refresh.
     * https://developer.android.com/training/swipe/respond-refresh-request.html
     */
    private void buildSwipeRefreshLayout() {
        swipeRefresh = (SwipeRefreshLayout) findViewById(R.id.swiperefresh);
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // not calling reload() as it reposts a page if the request was POST
                webView.loadUrl(webView.getUrl());
            }
        });
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void buildWebView() {
        webView = (WebView) findViewById(R.id.webview);

        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(new RestrictedWebViewClient());
        webView.setWebChromeClient(buildWebChromeClient());

        webView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(final View view, final MotionEvent motionEvent) {
                floatingActionMenu.close(false);
                return false;
            }
        });

        // Disable auto-complete suggestions to prevent NullPointerException with AutofillPopup
        webView.getSettings().setSaveFormData(false);

        appendAppVersionToUserAgent();
    }

    private void appendAppVersionToUserAgent() {
        final String currentUserAgentString = webView.getSettings().getUserAgentString();

        int versionCode = 0;
        try {
            versionCode = getPackageManager().getPackageInfo(getPackageName(), 0).versionCode;
        } catch (final NameNotFoundException e) {
            // ignored
        }

        final String appVersion = getString(R.string.app_user_agent) + "-" + versionCode;

        if (!currentUserAgentString.endsWith(appVersion)) {
            webView.getSettings().setUserAgentString(String.format("%s %s", currentUserAgentString, appVersion));
        }
    }

    /**
     * Populates webView's contents, either from Intent (external link), savedInstanceState or default URL.
     */
    private void populateWebView(final Bundle savedInstanceState) {
        final Intent intent = getIntent();
        if (!Intent.ACTION_MAIN.equals(intent.getAction())) {
            onNewIntent(intent);
        } else if (savedInstanceState != null) {
            webView.restoreState(savedInstanceState);
        } else {
            webView.loadUrl(appUrls.getDefaultUrl());
        }
    }

    @Override
    protected void onNewIntent(final Intent intent) {
        super.onNewIntent(intent);
        if (Intent.ACTION_VIEW.equals(intent.getAction())) {
            webView.loadUrl(intent.getData().toString());
        }
    }

    void createFloatingActionMenu() {
        final String webViewUrl = webView.getUrl();

        if (appUrls.isSignedOutUrl(webViewUrl)) {
            floatingActionMenu.hideMenu(true);
            appUrls.clearCurrentDomain();
            return;
        }

        if (appUrls.isCurrentDomain(webViewUrl)) {
            return;
        }

        String currentDomain;
        try {
            final URL url = new URL(webViewUrl);
            currentDomain = url.getHost();
        } catch (final MalformedURLException e) {
            currentDomain = appUrls.getDefaultDomain();
        }

        appUrls.setCurrentDomain(currentDomain);
        final String baseUrl = String.format("%s%s", "https://", currentDomain);

        createOnClickListenerForFloatingActionButton(newPracticeExecutionButton, baseUrl, "practice_executions/multi");
        createOnClickListenerForFloatingActionButton(newTithingButton, baseUrl, "tithings/new");
        createOnClickListenerForFloatingActionButton(newServiceButton, baseUrl, "services/new");
        createOnClickListenerForFloatingActionButton(newStudyButton, baseUrl, "studies/new");

        floatingActionMenu.showMenu(true);
    }

    private void createOnClickListenerForFloatingActionButton(final FloatingActionButton floatingActionButton,
                                                              final String baseUrl,
                                                              final String path) {
        final String url = String.format("%s/%s", baseUrl, path);

        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                webView.loadUrl(url);
                floatingActionMenu.close(false);
            }
        });
    }

    /**
     * Builds a WebChromeClient enabled to handle a WebView confirm dialog
     * http://stackoverflow.com/questions/2726377/how-to-handle-a-webview-confirm-dialog/2726503
     *
     * @return Customized WebChromeClient
     */
    private WebChromeClient buildWebChromeClient() {
        return new WebChromeClient() {

            @Override
            public boolean onJsConfirm(final WebView view, final String url, final String message, final JsResult result) {
                new AlertDialog.Builder(MainActivity.this).setTitle(R.string.app_name).setMessage(message)
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(final DialogInterface dialog, final int which) {
                                result.confirm();
                            }
                        }).setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog, final int which) {
                        result.cancel();
                    }
                }).create().show();

                return true;
            }
        };
    }

    /**
     * Enables web page history navigation for WebView
     * https://developer.android.com/guide/webapps/webview.html#NavigatingHistory
     */
    @Override
    public boolean onKeyDown(final int keyCode, final KeyEvent event) {
        // Check if the key event was the Back button and if there's history
        if ((keyCode == KeyEvent.KEYCODE_BACK) && webView.canGoBack()) {
            webView.goBack();
            return true;
        }
        // If it wasn't the Back key or there's no web page history, bubble up to the default system behavior (probably
        // exit the activity)
        return super.onKeyDown(keyCode, event);
    }

    /**
     * Saves WebView state in order to prevent it from losing context on screen rotation
     */
    @Override
    protected void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);
        webView.saveState(outState);
    }

    /**
     * WebViewClient with external handling of "mailto:" URLs, ignoring "tel:" and URLs not allowed by AppUrls. It will
     * show SwipeRefreshLayout progress spinner when loading URL.
     * <p>
     * http://stackoverflow.com/questions/3623137/howto-handle-mailto-in-android-webview
     * http://stackoverflow.com/questions/17994750/open-external-links-in-the-browser-with-android-webview
     */
    private class RestrictedWebViewClient extends WebViewClient {

        @Override
        public boolean shouldOverrideUrlLoading(final WebView view, final String url) {
            if (url.startsWith(WebView.SCHEME_MAILTO)) {
                final MailTo mailto = MailTo.parse(url);

                final Intent emailIntent = new Intent(Intent.ACTION_SEND);
                emailIntent.setType("message/rfc822");
                emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{mailto.getTo()});
                emailIntent.putExtra(Intent.EXTRA_CC, mailto.getCc());
                final String subject = mailto.getSubject() != null ? mailto.getSubject() : view.getContext().getResources()
                        .getString(R.string.default_email_subject);
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
                emailIntent.putExtra(Intent.EXTRA_TEXT, mailto.getBody());

                view.getContext().startActivity(emailIntent);
            } else if (url.startsWith(WebView.SCHEME_TEL)) {
                // prevents accidental clicks on numbers to be interpreted as "tel:"
            } else if (appUrls.isAllowed(url)) {
                view.loadUrl(url);
            } else {
                final Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(i);
            }

            return true;
        }

        @Override
        public void onPageStarted(final WebView view, final String url, final Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            swipeRefresh.setRefreshing(true);
        }

        @Override
        public void onPageFinished(final WebView view, final String url) {
            super.onPageFinished(view, url);

            // Ensures session cookie will be quickly saved from RAM to storage
            // https://developer.android.com/reference/android/webkit/CookieSyncManager.html
            CookieSyncManager.getInstance().sync();

            createFloatingActionMenu();
            swipeRefresh.setRefreshing(false);
        }

        @Override
        public void onReceivedError(final WebView view, final int errorCode, final String description,
                                    final String failingUrl) {
            super.onReceivedError(view, errorCode, description, failingUrl);
            final Context context = view.getContext();
            if (!isConnected(context)) {
                final Resources resources = context.getResources();
                new AlertDialog.Builder(context).setTitle(resources.getString(R.string.error_dialog_title))
                        .setMessage(resources.getString(R.string.error_dialog_message))
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        }).setIcon(android.R.drawable.ic_dialog_alert).show();
            }
        }

        private boolean isConnected(final Context context) {
            final ConnectivityManager connectivityManager = (ConnectivityManager) context
                    .getSystemService(CONNECTIVITY_SERVICE);
            return connectivityManager != null && connectivityManager.getActiveNetworkInfo() != null
                    && connectivityManager.getActiveNetworkInfo().isAvailable()
                    && connectivityManager.getActiveNetworkInfo().isConnected();
        }
    }
}