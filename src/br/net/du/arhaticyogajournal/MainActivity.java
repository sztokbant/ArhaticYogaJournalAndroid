package br.net.du.arhaticyogajournal;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.MailTo;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.KeyEvent;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class MainActivity extends Activity {
	private SwipeRefreshLayout swipeRefresh;
	private WebView webView;

	private AppDomains appDomains = new AppDomains();

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		buildSwipeRefreshLayout();
		buildWebView(savedInstanceState);
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
	private void buildWebView(final Bundle savedInstanceState) {
		webView = (WebView) findViewById(R.id.webview);

		webView.getSettings().setJavaScriptEnabled(true);
		webView.setWebViewClient(buildWebViewClient());
		webView.setWebChromeClient(buildWebChromeClient());

		if (savedInstanceState != null) {
			webView.restoreState(savedInstanceState);
		} else {
			webView.loadUrl(appDomains.getDefaultUrl());
		}
	}

	/**
	 * Builds a WebViewClient with external handling of "mailto:" URLs, ignoring "tel:" and external URLs. It will show
	 * SwipeRefreshLayout progress spinner when loading URL.
	 * 
	 * http://stackoverflow.com/questions/3623137/howto-handle-mailto-in-android-webview
	 * http://stackoverflow.com/questions/17994750/open-external-links-in-the-browser-with-android-webview
	 * 
	 * @return Customized WebViewClient
	 */
	private WebViewClient buildWebViewClient() {
		return new WebViewClient() {
			private static final String DEFAULT_EMAIL_SUBJECT = "Arhatic Yoga Journal Android";

			@Override
			public boolean shouldOverrideUrlLoading(final WebView view, final String url) {
				if (url.startsWith(WebView.SCHEME_MAILTO)) {
					final MailTo mailto = MailTo.parse(url);

					final Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
					emailIntent.setType("message/rfc822");
					emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[] { mailto.getTo() });
					emailIntent.putExtra(Intent.EXTRA_CC, mailto.getCc());
					final String subject = mailto.getSubject() != null ? mailto.getSubject() : DEFAULT_EMAIL_SUBJECT;
					emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
					emailIntent.putExtra(Intent.EXTRA_TEXT, mailto.getBody());

					view.getContext().startActivity(emailIntent);
				} else if (url.startsWith(WebView.SCHEME_TEL)) {
					// prevents accidental clicks on numbers to be interpreted as "tel:"
				} else if (appDomains.isAllowed(url)) {
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
				swipeRefresh.setRefreshing(false);
			}

			@Override
			public void onReceivedError(final WebView view, final int errorCode, final String description,
					final String failingUrl) {
				super.onReceivedError(view, errorCode, description, failingUrl);
				if (!isConnected(view.getContext())) {
					new AlertDialog.Builder(view.getContext()).setTitle("No Internet Connection")
							.setMessage("Please, check your Wi-Fi or cellular data network and swipe down to try again.")
							.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int which) {
								}
							}).setIcon(android.R.drawable.ic_dialog_alert).show();
				}
			}

			private boolean isConnected(final Context context) {
				final ConnectivityManager connectivityManager = (ConnectivityManager) context
						.getSystemService(Context.CONNECTIVITY_SERVICE);
				return connectivityManager != null && connectivityManager.getActiveNetworkInfo() != null
						&& connectivityManager.getActiveNetworkInfo().isAvailable()
						&& connectivityManager.getActiveNetworkInfo().isConnected();
			}
		};
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
}
