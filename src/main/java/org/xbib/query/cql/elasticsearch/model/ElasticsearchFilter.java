package org.xbib.query.cql.elasticsearch.model;

import org.xbib.query.Breadcrumb;
import org.xbib.query.QueryFilter;
import org.xbib.query.cql.elasticsearch.ast.Operator;

public class ElasticsearchFilter<V> implements QueryFilter<V> {

    private String name;

    private V value;

    private Operator op;

    private String label;

    public ElasticsearchFilter(String name, V value, Operator op) {
        this.name = name;
        this.op = op;
        this.value = value;
    }

    public ElasticsearchFilter(String name, V value, Operator op, String label) {
        this.name = name;
        this.op = op;
        this.value = value;
        this.label = label;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setValue(V value) {
        this.value = value;
    }

    public V getValue() {
        return value;
    }

    public Operator getFilterOperation() {
        return op;
    }

    public String getLabel() {
        return label;
    }

    public int compareTo(Breadcrumb o) {
        return toString().compareTo(((ElasticsearchFilter<V>)o).toString());
    }

    @Override
    public String toString() {
        return name + " " + op + " " + value;
    }
}
