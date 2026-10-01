package org.omegat.community.typography;

import org.omegat.util.Preferences;

final class TypographyPreferences {
    static final String PREFIX = "plugin.typography.";
    static final String ENABLED = PREFIX + "enabled";
    static final String PROFILE = PREFIX + "profile";
    static final String QUOTES = PREFIX + "quotes";
    static final String DASHES = PREFIX + "dashes";
    static final String ELLIPSIS = PREFIX + "ellipsis";
    static final String SPACING = PREFIX + "spacing";
    static final String AUTOMATIC = "automatic";

    static final boolean DEFAULT_ENABLED = true;
    static final boolean DEFAULT_QUOTES = true;
    static final boolean DEFAULT_DASHES = true;
    static final boolean DEFAULT_ELLIPSIS = false;
    static final boolean DEFAULT_SPACING = true;

    private TypographyPreferences() {
    }

    static boolean enabled() {
        return Preferences.isPreferenceDefault(ENABLED, DEFAULT_ENABLED);
    }

    static boolean quotes() {
        return Preferences.isPreferenceDefault(QUOTES, DEFAULT_QUOTES);
    }

    static boolean dashes() {
        return Preferences.isPreferenceDefault(DASHES, DEFAULT_DASHES);
    }

    static boolean ellipsis() {
        return Preferences.isPreferenceDefault(ELLIPSIS, DEFAULT_ELLIPSIS);
    }

    static boolean spacing() {
        return Preferences.isPreferenceDefault(SPACING, DEFAULT_SPACING);
    }

    static String profile() {
        return Preferences.getPreferenceDefault(PROFILE, AUTOMATIC);
    }
}
