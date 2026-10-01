package org.omegat.community.typography;

import java.awt.Desktop;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.net.URI;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.omegat.gui.preferences.BasePreferencesController;
import org.omegat.gui.preferences.IPreferencesController;
import org.omegat.gui.preferences.view.AutoCompleterController;
import org.omegat.util.Preferences;

public final class TypographyPreferencesController extends BasePreferencesController {
    private static final String CONTRIBUTING_URL =
            "https://github.com/emes81/Smart-Typography-Plugin-for-OmegaT/blob/main/CONTRIBUTING.md";

    private final JPanel panel = new JPanel(new GridBagLayout());
    private final JCheckBox enabled = new JCheckBox("Enable typography while typing");
    private final JComboBox<ProfileChoice> profile = new JComboBox<>();
    private final JCheckBox quotes = new JCheckBox("Smart quotes and apostrophes");
    private final JCheckBox dashes = new JCheckBox("Dashes: -- → – and --- → —");
    private final JCheckBox ellipsis = new JCheckBox("Ellipsis: ... → …");
    private final JCheckBox spacing = new JCheckBox("Language-specific punctuation spacing");

    public TypographyPreferencesController() {
        buildGui();
        loadPersisted();
    }

    @Override
    public String toString() {
        return "Typography";
    }

    @Override
    public JComponent getGui() {
        return panel;
    }

    @Override
    public Class<? extends IPreferencesController> getParentViewClass() {
        return AutoCompleterController.class;
    }

    @Override
    public void persist() {
        Preferences.setPreference(TypographyPreferences.ENABLED, enabled.isSelected());
        ProfileChoice choice = (ProfileChoice) profile.getSelectedItem();
        Preferences.setPreference(TypographyPreferences.PROFILE,
                choice == null ? TypographyPreferences.AUTOMATIC : choice.id);
        Preferences.setPreference(TypographyPreferences.QUOTES, quotes.isSelected());
        Preferences.setPreference(TypographyPreferences.DASHES, dashes.isSelected());
        Preferences.setPreference(TypographyPreferences.ELLIPSIS, ellipsis.isSelected());
        Preferences.setPreference(TypographyPreferences.SPACING, spacing.isSelected());
    }

    @Override
    public void restoreDefaults() {
        enabled.setSelected(TypographyPreferences.DEFAULT_ENABLED);
        selectProfile(TypographyPreferences.AUTOMATIC);
        quotes.setSelected(TypographyPreferences.DEFAULT_QUOTES);
        dashes.setSelected(TypographyPreferences.DEFAULT_DASHES);
        ellipsis.setSelected(TypographyPreferences.DEFAULT_ELLIPSIS);
        spacing.setSelected(TypographyPreferences.DEFAULT_SPACING);
        updateEnabledState();
    }

    @Override
    public boolean canRestoreDefaults() {
        return true;
    }

    private void buildGui() {
        panel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 2;
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        c.insets = new Insets(0, 0, 10, 0);
        panel.add(enabled, c);

        c.gridy++;
        c.gridwidth = 1;
        c.weightx = 0;
        c.insets = new Insets(0, 0, 8, 8);
        panel.add(new JLabel("Language profile:"), c);

        c.gridx = 1;
        c.weightx = 1;
        c.insets = new Insets(0, 0, 8, 0);
        profile.addItem(new ProfileChoice(TypographyPreferences.AUTOMATIC,
                "Automatic (project target language)"));
        for (TypographyProfile item : ProfileRegistry.all()) {
            profile.addItem(new ProfileChoice(item.id, item.name));
        }
        panel.add(profile, c);

        c.gridx = 0;
        c.gridy++;
        c.gridwidth = 2;
        c.weightx = 1;
        c.insets = new Insets(0, 20, 4, 0);
        panel.add(quotes, c);
        c.gridy++;
        panel.add(dashes, c);
        c.gridy++;
        panel.add(ellipsis, c);
        c.gridy++;
        panel.add(spacing, c);

        c.gridy++;
        c.insets = new Insets(12, 0, 0, 0);
        JButton contribute = new JButton("Contribute a language profile on GitHub");
        contribute.addActionListener(e -> openContributingPage());
        panel.add(contribute, c);

        c.gridy++;
        c.weighty = 1;
        c.fill = GridBagConstraints.BOTH;
        panel.add(new JPanel(), c);

        enabled.addActionListener(e -> updateEnabledState());
    }

    private void loadPersisted() {
        enabled.setSelected(TypographyPreferences.enabled());
        selectProfile(TypographyPreferences.profile());
        quotes.setSelected(TypographyPreferences.quotes());
        dashes.setSelected(TypographyPreferences.dashes());
        ellipsis.setSelected(TypographyPreferences.ellipsis());
        spacing.setSelected(TypographyPreferences.spacing());
        updateEnabledState();
    }

    private void selectProfile(String id) {
        for (int i = 0; i < profile.getItemCount(); i++) {
            ProfileChoice item = profile.getItemAt(i);
            if (item.id.equalsIgnoreCase(id)) {
                profile.setSelectedIndex(i);
                return;
            }
        }
        profile.setSelectedIndex(0);
    }

    private void updateEnabledState() {
        boolean state = enabled.isSelected();
        profile.setEnabled(state);
        quotes.setEnabled(state);
        dashes.setEnabled(state);
        ellipsis.setEnabled(state);
        spacing.setEnabled(state);
    }

    private static void openContributingPage() {
        if (!Desktop.isDesktopSupported()) {
            return;
        }
        try {
            Desktop.getDesktop().browse(URI.create(CONTRIBUTING_URL));
        } catch (Exception ignored) {
        }
    }

    private static final class ProfileChoice {
        final String id;
        final String label;

        ProfileChoice(String id, String label) {
            this.id = id;
            this.label = label;
        }

        @Override
        public String toString() {
            return label;
        }
    }
}
