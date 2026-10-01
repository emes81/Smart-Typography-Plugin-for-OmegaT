package org.omegat.community.typography;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

final class TypographyProfile {
    final String id;
    final String name;
    final List<String> languages;
    final String doubleOpen;
    final String doubleClose;
    final SpaceRule doubleInnerOpen;
    final SpaceRule doubleInnerClose;
    final String singleOpen;
    final String singleClose;
    final SpaceRule singleInnerOpen;
    final SpaceRule singleInnerClose;
    final String apostrophe;
    final SpaceRule beforeSemicolon;
    final SpaceRule beforeQuestion;
    final SpaceRule beforeExclamation;
    final SpaceRule beforeColon;

    private TypographyProfile(Properties p) {
        id = required(p, "id");
        name = required(p, "name");
        languages = parseList(required(p, "languages"));
        doubleOpen = required(p, "quotes.double.open");
        doubleClose = required(p, "quotes.double.close");
        doubleInnerOpen = SpaceRule.parse(p.getProperty("quotes.double.inner.open"));
        doubleInnerClose = SpaceRule.parse(p.getProperty("quotes.double.inner.close"));
        singleOpen = required(p, "quotes.single.open");
        singleClose = required(p, "quotes.single.close");
        singleInnerOpen = SpaceRule.parse(p.getProperty("quotes.single.inner.open"));
        singleInnerClose = SpaceRule.parse(p.getProperty("quotes.single.inner.close"));
        apostrophe = required(p, "apostrophe");
        beforeSemicolon = SpaceRule.parse(p.getProperty("spacing.before.semicolon"));
        beforeQuestion = SpaceRule.parse(p.getProperty("spacing.before.question"));
        beforeExclamation = SpaceRule.parse(p.getProperty("spacing.before.exclamation"));
        beforeColon = SpaceRule.parse(p.getProperty("spacing.before.colon"));
    }

    static TypographyProfile load(String resourceName) throws IOException {
        String path = "/profiles/" + resourceName;
        try (InputStream in = TypographyProfile.class.getResourceAsStream(path)) {
            if (in == null) {
                throw new IOException("Missing typography profile: " + path);
            }
            Properties p = new Properties();
            p.load(new InputStreamReader(in, StandardCharsets.UTF_8));
            return new TypographyProfile(p);
        }
    }

    SpaceRule spacingBefore(char punctuation) {
        switch (punctuation) {
        case ';':
            return beforeSemicolon;
        case '?':
            return beforeQuestion;
        case '!':
            return beforeExclamation;
        case ':':
            return beforeColon;
        default:
            return SpaceRule.NONE;
        }
    }

    boolean matchesExact(String language) {
        String candidate = normalise(language);
        for (String item : languages) {
            if (normalise(item).equals(candidate)) {
                return true;
            }
        }
        return false;
    }

    boolean matchesBase(String languageCode) {
        String candidate = normalise(languageCode);
        for (String item : languages) {
            String normalised = normalise(item);
            int dash = normalised.indexOf('-');
            String base = dash >= 0 ? normalised.substring(0, dash) : normalised;
            if (base.equals(candidate)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return name;
    }

    private static String required(Properties p, String key) {
        String value = p.getProperty(key);
        if (value == null || value.isEmpty()) {
            throw new IllegalArgumentException("Missing profile property: " + key);
        }
        return value;
    }

    private static List<String> parseList(String text) {
        List<String> values = new ArrayList<>();
        Arrays.stream(text.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .forEach(values::add);
        return Collections.unmodifiableList(values);
    }

    private static String normalise(String language) {
        return language == null ? "" : language.replace('_', '-').toLowerCase(Locale.ROOT);
    }
}
