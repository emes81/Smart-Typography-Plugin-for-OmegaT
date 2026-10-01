package org.omegat.community.typography;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class TypographyEngineTest {
    @Test
    void dashSequence() {
        assertEquals("-", TypographyEngine.directDashReplacement("x"));
        assertEquals("–", TypographyEngine.directDashReplacement("-"));
        assertEquals("—", TypographyEngine.directDashReplacement("–"));
    }

    @Test
    void ellipsisRequiresConsecutiveDots() {
        assertTrue(TypographyEngine.shouldCollapseEllipsis(".."));
        assertFalse(TypographyEngine.shouldCollapseEllipsis(". ."));
    }

    @Test
    void openingContextRecognisesWhitespaceAndOpeningPunctuation() {
        assertTrue(TypographyEngine.isOpeningContext("", 0));
        assertTrue(TypographyEngine.isOpeningContext("word ", 5));
        assertTrue(TypographyEngine.isOpeningContext("(", 1));
        assertFalse(TypographyEngine.isOpeningContext("word", 4));
    }
}
