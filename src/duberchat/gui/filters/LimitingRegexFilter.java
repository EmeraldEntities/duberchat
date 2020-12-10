package duberchat.gui.filters;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;

/**
 * This class is designed to enforce a text limit restriction in a JTextField,
 * as well as a regex string for character restrictions.
 * <p>
 * Created <b>2020-12-07</b>
 * 
 * @since 1.0.0
 * @version 1.0.0
 * @author Joseph Wang
 */
public class LimitingRegexFilter extends TextLengthFilter {
    protected String regexString;

    /**
     * Constructs a new {@code LimitingRegexFilter}.
     * 
     * @param maxLength   the max length of this filter.
     * @param regexString a regex string for the text to match.
     */
    public LimitingRegexFilter(int maxLength, String regexString) {
        super(maxLength);

        this.regexString = regexString;
    }

    @Override
    public void insertString(FilterBypass fb, int offs, String str, AttributeSet a) throws BadLocationException {
        String allText = fb.getDocument().getText(0, fb.getDocument().getLength());
        allText += str;

        if ((fb.getDocument().getLength() + str.length()) <= maxLength && allText.matches(regexString)) {
            super.insertString(fb, offs, str, a);
        }
    }

    @Override
    public void replace(FilterBypass fb, int offs, int length, String str, AttributeSet a) throws BadLocationException {
        String allText = fb.getDocument().getText(0, fb.getDocument().getLength());
        allText += str;

        if ((fb.getDocument().getLength() + (str.length() - length)) <= maxLength && allText.matches(regexString)) {
            super.replace(fb, offs, length, str, a);
        }
    }
}
