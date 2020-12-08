package duberchat.frames.filters;

import javax.swing.text.DocumentFilter;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;

/**
 * This class is designed to enforce a text limit restriction in a JTextField.
 * <p>
 * Created <b>2020-12-07</b>
 * 
 * @since 1.0.0
 * @version 1.0.0
 * @author Joseph Wang
 */
public class TextLengthFilter extends DocumentFilter {
    protected int maxLength;

    /**
     * Constructs a new {@code TextLengthFilter}.
     * 
     * @param maxLength the max length of this filter.
     */
    public TextLengthFilter(int maxLength) {
        super();

        this.maxLength = maxLength;
    }

    @Override
    public void insertString(FilterBypass fb, int offs, String str, AttributeSet a) throws BadLocationException {
        if (str == null)
            return;

        if (fb.getDocument().getLength() + str.length() <= this.maxLength) {
            super.insertString(fb, offs, str, a);
        }
    }

    @Override
    public void replace(FilterBypass fb, int offs, int length, String str, AttributeSet a) throws BadLocationException {
        if ((fb.getDocument().getLength() + (str.length() - length)) <= this.maxLength) {
            super.replace(fb, offs, length, str, a);
        }
    }
}
