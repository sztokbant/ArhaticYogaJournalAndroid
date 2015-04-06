package br.net.du.arhaticyogajournal;

import android.app.Activity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class MainActivity extends Activity {

	private static final String URL = "http://www.arhaticyogajournal.com";
	private WebView webView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		webView = (WebView) findViewById(R.id.webview);
		WebSettings webSettings = webView.getSettings();
		webSettings.setJavaScriptEnabled(true);
		webView.setWebViewClient(new WebViewClient());

		if (savedInstanceState != null) {
			webView.restoreState(savedInstanceState);
		} else {
			webView.loadUrl(URL);
		}
	}

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

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		webView.saveState(outState);
	}
}
