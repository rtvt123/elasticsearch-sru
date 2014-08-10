package org.xbib.query.cql.elasticsearch.ast;

import org.xbib.query.DateUtil;
import org.xbib.query.Filter;
import org.xbib.query.QuotedStringTokenizer;
import org.xbib.query.UnterminatedQuotedStringException;
import org.xbib.query.cql.elasticsearch.Visitor;

import java.util.Date;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Elasticsearch query tokens
 *
 */
public class Token implements Node {

    public enum TokenClass {

        NORMAL, ALL, WILDCARD, BOUNDARY, PROTECTED
    }

    private TokenType type;

    private String value;

    private Boolean booleanvalue;

    private Long longvalue;

    private Double doublevalue;

    private Date datevalue;

    private List<Date> dates;

    private List<String> values;

    private final EnumSet<TokenClass> tokenClass;

    public Token(String value) {
        this.value = value;
        this.tokenClass = EnumSet.of(TokenClass.NORMAL);
        this.type = TokenType.STRING;
        // if this string is equal to true/false or on/off or yes/no, convert silently to bool
        if (value.equals("true") || value.equals("yes") || value.equals("on")) {
            this.booleanvalue = true;
            this.value = null;
            this.type = TokenType.BOOL;

        } else if (value.equals("false") || value.equals("no") || value.equals("off")) {
            this.booleanvalue = false;
            this.value = null;
            this.type = TokenType.BOOL;

        }
        if (this.value != null) {
            // protected?
            if (value.startsWith("\"") && value.endsWith("\"")) {
                this.values = parseQuot(this.value);
                tokenClass.add(TokenClass.PROTECTED);
            }
            // wildcard?
            if (this.value.indexOf('*') >= 0 || this.value.indexOf('?') >= 0) {
                tokenClass.add(TokenClass.WILDCARD);
                // all?
                if (this.value.length() == 1) {
                    tokenClass.add(TokenClass.ALL);
                }
            }
            // prefix?
            if (this.value.charAt(0) == '^') {
                tokenClass.add(TokenClass.BOUNDARY);
                this.value = this.value.substring(1);
            }
        }        
    }

    public Token(Boolean value) {
        this.booleanvalue = value;
        this.type = TokenType.BOOL;
        this.tokenClass = EnumSet.of(TokenClass.NORMAL);
    }

    public Token(Long value) {
        this.longvalue = value;
        this.type = TokenType.INT;
        this.tokenClass = EnumSet.of(TokenClass.NORMAL);
    }

    public Token(Double value) {
        this.doublevalue = value;
        this.type = TokenType.FLOAT;
        this.tokenClass = EnumSet.of(TokenClass.NORMAL);
    }

    public Token(Date value) {
        this.datevalue = value;
        // this will enforce dates to get formatted as long values (years)
        this.longvalue = Long.parseLong(DateUtil.formatDate(datevalue, "yyyy"));
        this.type = TokenType.DATETIME;
        this.tokenClass = EnumSet.of(TokenClass.NORMAL);
    }

    public String getString() {        
        return value;
    }

    public Boolean getBoolean() {
        return booleanvalue;
    }

    public Long getInteger() {
        return longvalue;
    }

    public Double getFloat() {
        return doublevalue;
    }

    public Date getDate() {
        return datevalue;
    }

    public List<Date> getDates() {
        return dates;
    }

    public List<String> getStringList() {
        return values;
    }

    @Override
    public TokenType getType() {
        return type;
    }

    @Override
    public boolean isVisible() {
        return true;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (booleanvalue != null) {
            sb.append(booleanvalue);
        } else if (longvalue != null) {
            sb.append(longvalue);
        } else if (doublevalue != null) {
            sb.append(doublevalue);
        } else if (datevalue != null) {
            sb.append(DateUtil.formatDateISO(datevalue));
        } else if (value != null) {
            sb.append(value);
        }
        return sb.toString();
    }

    public boolean isProtected() {
        return tokenClass.contains(TokenClass.PROTECTED);
    }

    public boolean isBoundary() {
        return tokenClass.contains(TokenClass.BOUNDARY);
    }

    public boolean isWildcard() {
        return tokenClass.contains(TokenClass.WILDCARD);
    }

    public boolean isAll() {
        return tokenClass.contains(TokenClass.ALL);
    }

    private List<String> parseQuot(String s) {
        LinkedList l = new LinkedList();
        try {
            Filter.filter(new QuotedStringTokenizer(s, " \t\n\r\f", "\"", '\\', false), l, isWordPred);
        } catch (UnterminatedQuotedStringException e) {
            // swallow exception
        }
        return l;
    }

    private static class IsWordPredicate implements Filter.Predicate<String, String> {

        @Override
        public String apply(String s) {
            return s == null || s.length() == 0 || word.matcher(s).matches() ? null : s;
        }
    }
    private final static IsWordPredicate isWordPred = new IsWordPredicate();
    private final static Pattern word = Pattern.compile("[\\P{IsWord}]");
}
