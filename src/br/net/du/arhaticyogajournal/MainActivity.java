package br.net.du.arhaticyogajournal;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.KeyEvent;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

public class MainActivity extends Activity {
	private AppDomains appDomains;
	private SwipeRefreshLayout swipeRefresh;
	private WebView webView;

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		appDomains = new AppDomains(getBaseContext());

		buildSwipeRefreshLayout();
		buildWebView();
		populateWebView(savedInstanceState);
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
		webView.setWebViewClient(new RestrictedWebViewClient(this));
		webView.setWebChromeClient(buildWebChromeClient());
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
			webView.loadUrl(appDomains.getDefaultUrl());
		}
	}

	@Override
	protected void onNewIntent(final Intent intent) {
		super.onNewIntent(intent);
		populateWebView(intent);
	}

	private void populateWebView(final Intent intent) {
		if (Intent.ACTION_VIEW.equals(intent.getAction())) {
			webView.loadUrl(intent.getData().toString());
		}
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

	public SwipeRefreshLayout getSwipeRefresh() {
		return swipeRefresh;
	}

	public AppDomains getAppDomains() {
		return appDomains;
	}
}
