package br.net.du.arhaticyogajournal;

import android.view.View;
import android.webkit.WebView;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;

import java.net.MalformedURLException;
import java.net.URL;

public class FloatingActionMenuManager {
    private final FloatingActionMenu floatingActionMenu;
    private final WebView webView;
    private final AppUrls appUrls;

    private FloatingActionButton newPracticeExecutionButton;
    private FloatingActionButton newTithingButton;
    private FloatingActionButton newServiceButton;
    private FloatingActionButton newStudyButton;

    public FloatingActionMenuManager(final FloatingActionMenu floatingActionMenu,
                                     final WebView webView,
                                     final AppUrls appUrls) {
        this.floatingActionMenu = floatingActionMenu;
        this.webView = webView;
        this.appUrls = appUrls;

        newPracticeExecutionButton = (FloatingActionButton) floatingActionMenu.findViewById(R.id.new_practice_execution);
        newTithingButton = (FloatingActionButton) floatingActionMenu.findViewById(R.id.new_tithing);
        newServiceButton = (FloatingActionButton) floatingActionMenu.findViewById(R.id.new_service);
        newStudyButton = (FloatingActionButton) floatingActionMenu.findViewById(R.id.new_study);

        refresh();
    }

    public void refresh() {
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

    public void closeMenu() {
        floatingActionMenu.close(false);
    }
}
