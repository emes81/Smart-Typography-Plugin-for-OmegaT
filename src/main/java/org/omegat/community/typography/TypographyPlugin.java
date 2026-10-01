package org.omegat.community.typography;

import java.awt.KeyboardFocusManager;

import org.omegat.core.Core;
import org.omegat.gui.preferences.PreferencesControllers;

public final class TypographyPlugin {
    private static TypographyDispatcher dispatcher;

    private TypographyPlugin() {
    }

    public static void loadPlugins() {
        try {
            PreferencesControllers.addSupplier(TypographyPreferencesController::new);
            dispatcher = new TypographyDispatcher();
            KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(dispatcher);
        } catch (Throwable t) {
            Core.pluginLoadingError("Typography plugin could not be loaded: " + t.getClass().getSimpleName());
        }
    }

    public static void unloadPlugins() {
        if (dispatcher != null) {
            KeyboardFocusManager.getCurrentKeyboardFocusManager().removeKeyEventDispatcher(dispatcher);
            dispatcher = null;
        }
    }
}
