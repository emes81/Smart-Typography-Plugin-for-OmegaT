package org.omegat.community.typography;

import java.awt.Component;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

import org.omegat.core.Core;
import org.omegat.core.data.IProject;
import org.omegat.gui.editor.EditorTextArea3;
import org.omegat.util.Language;

final class TypographyDispatcher implements KeyEventDispatcher {
    private static final class PendingSingleQuote {
        final int offset;
        final String inserted;
        final String closing;

        PendingSingleQuote(int offset, String inserted, String closing) {
            this.offset = offset;
            this.inserted = inserted;
            this.closing = closing;
        }
    }

    private final WeakHashMap<EditorTextArea3, PendingSingleQuote> pending = new WeakHashMap<>();
    private final Set<EditorTextArea3> observed = Collections.newSetFromMap(new WeakHashMap<>());

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        Component owner = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
        if (!(owner instanceof EditorTextArea3)) {
            return false;
        }
        EditorTextArea3 editor = (EditorTextArea3) owner;
        observe(editor);

        if (event.getID() == KeyEvent.KEY_PRESSED) {
            if (isBoundaryKey(event.getKeyCode())) {
                finalisePendingAsClosing(editor);
            }
            return false;
        }
        if (event.getID() != KeyEvent.KEY_TYPED || !TypographyPreferences.enabled() || !editor.isEditable()) {
            return false;
        }

        char typed = event.getKeyChar();
        PendingSingleQuote p = pending.get(editor);
        if (p != null) {
            if (TypographyEngine.isWordContinuation(typed)) {
                pending.remove(editor);
            } else {
                finalisePendingAsClosing(editor);
            }
        }

        int caret = editor.getSelectionStart();
        if (!insideActiveTranslation(editor, caret)) {
            return false;
        }

        TypographyProfile profile = resolveProfile();
        try {
            switch (typed) {
            case '"':
                if (TypographyPreferences.quotes() && profile != null) {
                    typeDoubleQuote(editor, profile);
                    return true;
                }
                break;
            case '\'':
                if (TypographyPreferences.quotes() && profile != null) {
                    typeSingleQuoteOrApostrophe(editor, profile);
                    return true;
                }
                break;
            case '-':
                if (TypographyPreferences.dashes()) {
                    typeDash(editor);
                    return true;
                }
                break;
            case '.':
                if (TypographyPreferences.ellipsis() && typeEllipsisIfApplicable(editor)) {
                    return true;
                }
                break;
            case ';':
            case '?':
            case '!':
            case ':':
                if (TypographyPreferences.spacing() && profile != null) {
                    SpaceRule rule = profile.spacingBefore(typed);
                    if (rule != SpaceRule.NONE) {
                        typePunctuationWithSpacing(editor, typed, rule);
                        return true;
                    }
                }
                break;
            default:
                break;
            }
        } catch (BadLocationException ignored) {
            return false;
        }
        return false;
    }

    private void typeDoubleQuote(EditorTextArea3 editor, TypographyProfile profile) throws BadLocationException {
        int caret = editor.getSelectionStart();
        String text = documentText(editor);
        boolean opening = TypographyEngine.isOpeningContext(text, caret);
        if (opening) {
            editor.replaceSelection(profile.doubleOpen + profile.doubleInnerOpen.text());
        } else {
            ensureSpaceBefore(editor, profile.doubleInnerClose);
            editor.replaceSelection(profile.doubleClose);
        }
    }

    private void typeSingleQuoteOrApostrophe(EditorTextArea3 editor, TypographyProfile profile)
            throws BadLocationException {
        int caret = editor.getSelectionStart();
        String text = documentText(editor);
        boolean opening = TypographyEngine.isOpeningContext(text, caret);
        if (opening) {
            editor.replaceSelection(profile.singleOpen + profile.singleInnerOpen.text());
            return;
        }

        boolean previousIsWord = caret > 0 && TypographyEngine.isWordContinuation(text.charAt(caret - 1));
        boolean unmatchedOpener = TypographyEngine.hasUnmatchedSingleOpener(text, caret, profile);
        if (previousIsWord && unmatchedOpener && !profile.apostrophe.equals(profile.singleClose)) {
            int insertionOffset = editor.getSelectionStart();
            editor.replaceSelection(profile.apostrophe);
            pending.put(editor, new PendingSingleQuote(insertionOffset, profile.apostrophe, profile.singleClose));
            return;
        }
        if (previousIsWord) {
            editor.replaceSelection(profile.apostrophe);
            return;
        }

        ensureSpaceBefore(editor, profile.singleInnerClose);
        editor.replaceSelection(profile.singleClose);
    }

    private void typeDash(EditorTextArea3 editor) throws BadLocationException {
        int caret = editor.getSelectionStart();
        Document doc = editor.getDocument();
        if (caret > 0) {
            String previous = doc.getText(caret - 1, 1);
            String replacement = TypographyEngine.directDashReplacement(previous);
            if (!replacement.equals("-")) {
                editor.select(caret - 1, caret);
                editor.replaceSelection(replacement);
                return;
            }
        }
        editor.replaceSelection("-");
    }

    private boolean typeEllipsisIfApplicable(EditorTextArea3 editor) throws BadLocationException {
        int caret = editor.getSelectionStart();
        if (caret < 2) {
            return false;
        }
        String previous = editor.getDocument().getText(caret - 2, 2);
        if (!TypographyEngine.shouldCollapseEllipsis(previous)) {
            return false;
        }
        editor.select(caret - 2, caret);
        editor.replaceSelection("…");
        return true;
    }

    private void typePunctuationWithSpacing(EditorTextArea3 editor, char punctuation, SpaceRule rule)
            throws BadLocationException {
        ensureSpaceBefore(editor, rule);
        editor.replaceSelection(String.valueOf(punctuation));
    }

    private void ensureSpaceBefore(EditorTextArea3 editor, SpaceRule rule) throws BadLocationException {
        if (rule == SpaceRule.NONE) {
            return;
        }
        int caret = editor.getSelectionStart();
        if (caret <= 0) {
            return;
        }
        Document doc = editor.getDocument();
        char previous = doc.getText(caret - 1, 1).charAt(0);
        if (previous == '\n' || previous == '\r') {
            return;
        }
        if (SpaceRule.isManagedSpace(previous)) {
            editor.select(caret - 1, caret);
            editor.replaceSelection(rule.text());
        } else {
            editor.replaceSelection(rule.text());
        }
    }

    private void finalisePendingAsClosing(EditorTextArea3 editor) {
        PendingSingleQuote p = pending.remove(editor);
        if (p == null) {
            return;
        }
        try {
            Document doc = editor.getDocument();
            if (p.offset < 0 || p.offset + p.inserted.length() > doc.getLength()) {
                return;
            }
            String current = doc.getText(p.offset, p.inserted.length());
            if (!current.equals(p.inserted)) {
                return;
            }
            int oldStart = editor.getSelectionStart();
            int oldEnd = editor.getSelectionEnd();
            editor.select(p.offset, p.offset + p.inserted.length());
            editor.replaceSelection(p.closing);
            int delta = p.closing.length() - p.inserted.length();
            int newStart = oldStart > p.offset ? oldStart + delta : oldStart;
            int newEnd = oldEnd > p.offset ? oldEnd + delta : oldEnd;
            int length = editor.getDocument().getLength();
            editor.select(Math.min(newStart, length), Math.min(newEnd, length));
        } catch (BadLocationException ignored) {
        }
    }

    private TypographyProfile resolveProfile() {
        String selected = TypographyPreferences.profile();
        TypographyProfile explicit = ProfileRegistry.byId(selected);
        if (explicit != null) {
            return explicit;
        }
        if (!TypographyPreferences.AUTOMATIC.equals(selected)) {
            return null;
        }
        try {
            IProject project = Core.getProject();
            if (project == null || !project.isProjectLoaded()) {
                return null;
            }
            Language language = project.getProjectProperties().getTargetLanguage();
            if (language == null) {
                return null;
            }
            return ProfileRegistry.resolve(language.getLanguage(), language.getLanguageCode());
        } catch (RuntimeException e) {
            return null;
        }
    }

    private static boolean insideActiveTranslation(EditorTextArea3 editor, int position) {
        try {
            return editor.isInActiveTranslation(position)
                    || (position > 0 && editor.isInActiveTranslation(position - 1));
        } catch (RuntimeException e) {
            return false;
        }
    }

    private static String documentText(EditorTextArea3 editor) throws BadLocationException {
        Document doc = editor.getDocument();
        return doc.getText(0, doc.getLength());
    }

    private void observe(EditorTextArea3 editor) {
        if (!observed.add(editor)) {
            return;
        }
        editor.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                finalisePendingAsClosing(editor);
            }
        });
        editor.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                finalisePendingAsClosing(editor);
            }
        });
    }

    private static boolean isBoundaryKey(int keyCode) {
        switch (keyCode) {
        case KeyEvent.VK_ENTER:
        case KeyEvent.VK_TAB:
        case KeyEvent.VK_LEFT:
        case KeyEvent.VK_RIGHT:
        case KeyEvent.VK_UP:
        case KeyEvent.VK_DOWN:
        case KeyEvent.VK_HOME:
        case KeyEvent.VK_END:
        case KeyEvent.VK_PAGE_UP:
        case KeyEvent.VK_PAGE_DOWN:
        case KeyEvent.VK_ESCAPE:
            return true;
        default:
            return false;
        }
    }
}
