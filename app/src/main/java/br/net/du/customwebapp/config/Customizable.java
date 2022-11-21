package br.net.du.customwebapp.config;

import br.net.du.customwebapp.R;
import br.net.du.customwebapp.model.ButtonConfig;

public class Customizable {
    // Define buttons in floating action menu and their paths
    public static final ButtonConfig[] BUTTON_CONFIGS = {
        new ButtonConfig("Log Study", R.drawable.ic_study, "studies/new"),
        new ButtonConfig("Log Service", R.drawable.ic_service, "services/new"),
        new ButtonConfig("Log Tithing", R.drawable.ic_dollar, "tithings/new"),
        new ButtonConfig("Log Practice", R.drawable.ic_launcher, "practice_executions/multi")
    };

    // Define signed-out URL paths to prevent floating action menu from being displayed
    public static final String[] SIGNED_OUT_URL_PATTERNS =
            new String[] {"/about?s=0", "/password_reset", "/users/pwext", "/welcome"};

    // Define optional prefix, suffix for extra allowed domains
    public static final String GENERIC_DOMAIN_PREFIX = "ayj";
    public static final String GENERIC_DOMAIN_SUFFIX = ".herokuapp.com";
}
