package br.net.du.arhaticyogajournal;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

public class MainActivity extends Activity {
	private static final String URL = "http://www.arhaticyogajournal.com";
	private WebView webView;
	private ProgressBar progressBar;

	@SuppressLint("SetJavaScriptEnabled")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		webView = (WebView) findViewById(R.id.webview);

		webView.getSettings().setJavaScriptEnabled(true);
		webView.setWebViewClient(buildWebViewClient());
		webView.setWebChromeClient(buildWebChromeClient());

		if (savedInstanceState != null) {
			webView.restoreState(savedInstanceState);
		} else {
			webView.loadUrl(URL);
		}
	}

	/**
	 * Builds a WebViewClient with a spinner ProgressBar, external handling of "mailto:" URLs, ignoring "tel:" URLs
	 * http://www.technotalkative.com/android-load-webview-with-progressbar/
	 * http://stackoverflow.com/questions/3623137/howto-handle-mailto-in-android-webview
	 * 
	 * @return Customized WebViewClient
	 */
	private WebViewClient buildWebViewClient() {
		progressBar = (ProgressBar) findViewById(R.id.progressBar);

		return new WebViewClient() {

			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				if (url.startsWith(WebView.SCHEME_MAILTO)) {
					String[] dummyStringArray = new String[0];
					List<String> to = new ArrayList<String>();
					to.add(url.split(":")[1]);

					Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
					emailIntent.setType("message/rfc822");
					emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, to.toArray(dummyStringArray));
					emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Arhatic Yoga Journal Android");

					view.getContext().startActivity(emailIntent);
				} else if (url.startsWith(WebView.SCHEME_TEL)) {
					// prevents accidental clicks on numbers to be interpreted as "tel:"
				} else {
					progressBar.setVisibility(View.VISIBLE);
					view.loadUrl(url);
				}

				return true;
			}

			@Override
			public void onPageFinished(WebView view, String url) {
				super.onPageFinished(view, url);
				progressBar.setVisibility(View.GONE);
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
			public boolean onJsConfirm(WebView view, String url, String message, final JsResult result) {
				new AlertDialog.Builder(MainActivity.this).setTitle(R.string.app_name).setMessage(message)
						.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								result.confirm();
							}
						}).setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
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
	public boolean onKeyDown(int keyCode, KeyEvent event) {
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
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		webView.saveState(outState);
	}
}
