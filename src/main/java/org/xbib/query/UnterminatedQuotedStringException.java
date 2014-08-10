package org.xbib.query;

/**
 * String tokenizer exception
 *
 */
public class UnterminatedQuotedStringException extends RuntimeException {
    /**
     * Creates a new String tokenizer object.
     */
    public UnterminatedQuotedStringException(String msg) {
        super(msg);
    }
}
