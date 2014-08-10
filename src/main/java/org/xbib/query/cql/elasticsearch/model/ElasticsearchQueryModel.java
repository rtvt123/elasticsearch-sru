package org.xbib.query.cql.elasticsearch.model;

import org.xbib.query.cql.elasticsearch.ast.Expression;
import org.xbib.query.cql.elasticsearch.ast.Name;
import org.xbib.query.cql.elasticsearch.ast.Node;
import org.xbib.query.cql.elasticsearch.ast.Operator;
import org.xbib.query.cql.elasticsearch.ast.TokenType;
import org.xbib.query.cql.model.CQLQueryModel;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

/**
 * Elasticsearch query model
 */
public final class ElasticsearchQueryModel {

    private final Map<String, String> options;

    private final Map<String, Expression> conjunctivefilters;

    private final Map<String, Expression> disjunctivefilters;

    private final Map<String, Expression> facets;

//    private Expression facetexpr;

    private Expression sortexpr;

    public ElasticsearchQueryModel() {
        this.options = new HashMap<String, String>();
        this.conjunctivefilters = new HashMap<String, Expression>();
        this.disjunctivefilters = new HashMap<String, Expression>();
        this.facets = new HashMap<String, Expression>();
    }

    /**
     * Determine if the key has a type. Default type is string.
     *
     * @param key the key to check
     * @return the type of the key
     */
    public TokenType getElasticsearchType(String key) {
        if ("datetime".equals(key)) {
            return TokenType.DATETIME;
        }
        if ("int".equals(key)) {
            return TokenType.INT;
        }
        if ("long".equals(key)) {
            return TokenType.INT;
        }
        if ("float".equals(key)) {
            return TokenType.FLOAT;
        }
        return TokenType.STRING;
    }

    /**
     * Get expression visibility of a given context
     * @param context the context
     * @return true if visible
     */
    public boolean getVisibility(String context) {
        return !CQLQueryModel.isFacetContext(context)
                && !CQLQueryModel.isFilterContext(context)
                && !CQLQueryModel.isOptionContext(context);
    }

    /**
     * Check if this context is the facet context
     * @param context the context
     * @return true if facet context
     */
    public boolean isFacetContext(String context) {
        return CQLQueryModel.isFacetContext(context);
    }

    /**
     * Check if this context is the filter context
     * @param context the context
     * @return true if filter context
     */
    public boolean isFilterContext(String context) {
        return CQLQueryModel.isFilterContext(context);
    }

    /**
     * Check if this context is the option context
     * @param context the context
     * @return if option context
     */
    public boolean isOptionContext(String context) {
        return CQLQueryModel.isOptionContext(context);
    }

    /**
     * Add option.
     * @param key the option key
     * @param value the option value
     */
    public void addOption(String key, String value) {
            options.put(key, value);
    }

    public boolean hasFacets() {
        return !facets.isEmpty();
    }

    public void addFacet(String key, String value) {
        ElasticsearchFacet<Node> facet = new ElasticsearchFacet<Node>(ElasticsearchFacet.Type.TERMS, key, new Name(value));
        facets.put(facet.getName(), new Expression(Operator.TERMS_FACET, facet.getValue()));
    }

    public void addFacet(ElasticsearchFacet<Node> facet) {
        facets.put(facet.getName(), new Expression(Operator.TERMS_FACET, facet.getValue()));
    }

    public Expression getFacetExpression() {
        return new Expression(Operator.TERMS_FACET, facets.values().toArray(new Node[facets.size()]));
    }

    public void addConjunctiveFilter(String name, Node value, Operator op) {
        addFilter(conjunctivefilters, new ElasticsearchFilter(name, value, op));
    }

    public void addConjunctiveFilter(ElasticsearchFilter<Node> filter) {
        addFilter(conjunctivefilters, filter);
    }

    public void addDisjunctiveFilter(String name, Node value, Operator op) {
        addFilter(disjunctivefilters, new ElasticsearchFilter(name, value, op));
    }

    public void addDisjunctiveFilter(ElasticsearchFilter<Node> filter) {
        addFilter(disjunctivefilters, filter);
    }

    public boolean hasFilter() {
        return !conjunctivefilters.isEmpty() && !disjunctivefilters.isEmpty();
    }

    /**
     * Get filter expression.
     * Only one filter expression is allowed per query.
     * First, build conjunctive and disjunctive filter terms.
     * If both are null, there is no filter at all.
     * Otherwise, combine conjunctive and disjunctive filter terms with a
     * disjunction, and apply filter function, and return this expression.
     *
     * @return a single filter expression or null if there are no filter terms
     */
    public Expression getFilterExpression() {
        Expression conjunctiveclause = null;
        if (!conjunctivefilters.isEmpty()) {
            conjunctiveclause = new Expression(Operator.AND,
                    conjunctivefilters.values().toArray(new Node[conjunctivefilters.size()]));
        }
        Expression disjunctiveclause = null;
        if (!disjunctivefilters.isEmpty()) {
            disjunctiveclause = new Expression(Operator.OR,
                    disjunctivefilters.values().toArray(new Node[disjunctivefilters.size()]));
        }
        if (conjunctiveclause == null && disjunctiveclause == null) {
            return null;
        }
        return new Expression(Operator.OR, conjunctiveclause, disjunctiveclause);
    }

    /**
     * Add sort expression
     *
     * @param indexAndModifier the index with modifiers
     */
    public void setSort(Stack<Node> indexAndModifier) {
        this.sortexpr = new Expression(Operator.SORT, reverse(indexAndModifier).toArray(new Node[indexAndModifier.size()]));
    }
    
    /**
     * Get sort expression
     * @return the sort expression
     */
    public Expression getSort() {
        return sortexpr;
    }

    /**
     * Helper method to add a filter
     * @param filters the filter list
     * @param filter the filter
     */
    private void addFilter(Map<String, Expression> filters, ElasticsearchFilter<Node> filter) {
        Name name = new Name(filter.getName());
        name.setType(getElasticsearchType(filter.getName()));
        filters.put(filter.getName(), new Expression(filter.getFilterOperation(), name, filter.getValue()));
    }


    /**
     * Helper method to reverse an expression stack 
     * @param in the stack to reverse
     * @return the reversed stack
     */
    private Stack<Node> reverse(Stack<Node> in) {
        Stack<Node> out = new Stack<Node>();
        while (!in.empty()) {
            out.push(in.pop());
        }
        return out;
    }    
}
