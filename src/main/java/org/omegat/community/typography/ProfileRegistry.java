package org.omegat.community.typography;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

final class ProfileRegistry {
    private static final List<TypographyProfile> PROFILES = loadProfiles();

    private ProfileRegistry() {
    }

    static List<TypographyProfile> all() {
        return PROFILES;
    }

    static TypographyProfile byId(String id) {
        if (id == null || id.isEmpty() || TypographyPreferences.AUTOMATIC.equals(id)) {
            return null;
        }
        for (TypographyProfile profile : PROFILES) {
            if (profile.id.equalsIgnoreCase(id)) {
                return profile;
            }
        }
        return null;
    }

    static TypographyProfile resolve(String fullLanguage, String languageCode) {
        for (TypographyProfile profile : PROFILES) {
            if (profile.matchesExact(fullLanguage)) {
                return profile;
            }
        }
        for (TypographyProfile profile : PROFILES) {
            if (profile.matchesBase(languageCode)) {
                return profile;
            }
        }
        return null;
    }

    private static List<TypographyProfile> loadProfiles() {
        List<TypographyProfile> result = new ArrayList<>();
        try (InputStream in = ProfileRegistry.class.getResourceAsStream("/profiles/index.txt")) {
            if (in == null) {
                return Collections.emptyList();
            }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String trimmed = line.trim();
                    if (!trimmed.isEmpty() && !trimmed.startsWith("#")) {
                        result.add(TypographyProfile.load(trimmed));
                    }
                }
            }
        } catch (IOException | RuntimeException e) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(result);
    }
}
