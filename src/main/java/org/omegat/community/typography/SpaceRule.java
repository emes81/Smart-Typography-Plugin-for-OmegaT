package org.omegat.community.typography;

enum SpaceRule {
    NONE(""),
    SPACE(" "),
    NBSP("\u00A0"),
    NNBSP("\u202F");

    private final String text;

    SpaceRule(String text) {
        this.text = text;
    }

    String text() {
        return text;
    }

    static SpaceRule parse(String value) {
        if (value == null) {
            return NONE;
        }
        switch (value.trim().toLowerCase()) {
        case "space":
            return SPACE;
        case "nbsp":
            return NBSP;
        case "nnbsp":
        case "narrow-nbsp":
            return NNBSP;
        default:
            return NONE;
        }
    }

    static boolean isManagedSpace(char ch) {
        return ch == ' ' || ch == '\u00A0' || ch == '\u202F';
    }
}
