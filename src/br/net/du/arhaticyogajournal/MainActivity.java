package br.net.du.arhaticyogajournal;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.KeyEvent;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class MainActivity extends Activity {
	private static final String URL = "http://www.arhaticyogajournal.com";
	private WebView webView;

	@SuppressLint("SetJavaScriptEnabled")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		webView = (WebView) findViewById(R.id.webview);
		webView.getSettings().setJavaScriptEnabled(true);
		webView.setWebViewClient(new WebViewClient());
		webView.setWebChromeClient(buildWebChromeClient());

		if (savedInstanceState != null) {
			webView.restoreState(savedInstanceState);
		} else {
			webView.loadUrl(URL);
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
		webView.saveState(outState);
	}
}
