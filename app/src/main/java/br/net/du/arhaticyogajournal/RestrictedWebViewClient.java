package br.net.du.arhaticyogajournal;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.MailTo;
import android.net.Uri;
import android.webkit.WebView;
import android.webkit.WebViewClient;

/**
 * WebViewClient with external handling of "mailto:" URLs, ignoring "tel:" and URLs not allowed by AppDomains. It will
 * show SwipeRefreshLayout progress spinner when loading URL.
 * <p>
 * http://stackoverflow.com/questions/3623137/howto-handle-mailto-in-android-webview
 * http://stackoverflow.com/questions/17994750/open-external-links-in-the-browser-with-android-webview
 *
 * @return Customized WebViewClient
 */
final class RestrictedWebViewClient extends WebViewClient {
    private final MainActivity mainActivity;

    RestrictedWebViewClient(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    @Override
    public boolean shouldOverrideUrlLoading(final WebView view, final String url) {
        if (url.startsWith(WebView.SCHEME_MAILTO)) {
            final MailTo mailto = MailTo.parse(url);

            final Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
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
        } else if (mainActivity.getAppDomains().isAllowed(url)) {
            view.loadUrl(url);
        } else {
            final Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            mainActivity.startActivity(i);
        }

        return true;
    }

    @Override
    public void onPageStarted(final WebView view, final String url, final Bitmap favicon) {
        super.onPageStarted(view, url, favicon);
        mainActivity.getSwipeRefresh().setRefreshing(true);
    }

    @Override
    public void onPageFinished(final WebView view, final String url) {
        super.onPageFinished(view, url);
        mainActivity.getSwipeRefresh().setRefreshing(false);
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
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        return connectivityManager != null && connectivityManager.getActiveNetworkInfo() != null
                && connectivityManager.getActiveNetworkInfo().isAvailable()
                && connectivityManager.getActiveNetworkInfo().isConnected();
    }
}