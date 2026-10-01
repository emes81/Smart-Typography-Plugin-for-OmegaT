package org.omegat.community.typography;

final class TypographyEngine {
    private TypographyEngine() {
    }

    static boolean isOpeningContext(String text, int caret) {
        if (caret <= 0 || text.isEmpty()) {
            return true;
        }
        char previous = text.charAt(caret - 1);
        if (Character.isWhitespace(previous)) {
            return true;
        }
        switch (previous) {
        case '(':
        case '[':
        case '{':
        case '<':
        case '/':
        case '\\':
        case '—':
        case '–':
            return true;
        default:
            return false;
        }
    }

    static boolean hasUnmatchedSingleOpener(String text, int caret, TypographyProfile profile) {
        if (profile.singleOpen.equals(profile.singleClose)) {
            return false;
        }
        int start = Math.max(0, caret - 2000);
        String before = text.substring(start, Math.min(caret, text.length()));
        int opens = count(before, profile.singleOpen);
        int closes = count(before, profile.singleClose);
        return opens > closes;
    }

    static String directDashReplacement(String preceding) {
        if (preceding.endsWith("–")) {
            return "—";
        }
        if (preceding.endsWith("-")) {
            return "–";
        }
        return "-";
    }

    static boolean shouldCollapseEllipsis(String preceding) {
        return preceding.endsWith("..");
    }

    static boolean isWordContinuation(char ch) {
        int type = Character.getType(ch);
        return Character.isLetterOrDigit(ch)
                || type == Character.NON_SPACING_MARK
                || type == Character.COMBINING_SPACING_MARK;
    }

    private static int count(String text, String needle) {
        if (needle.isEmpty()) {
            return 0;
        }
        int count = 0;
        int index = 0;
        while ((index = text.indexOf(needle, index)) >= 0) {
            count++;
            index += needle.length();
        }
        return count;
    }
}
