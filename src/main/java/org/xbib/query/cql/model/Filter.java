package org.xbib.query.cql.model;

import org.xbib.query.Breadcrumb;
import org.xbib.query.QueryFilter;
import org.xbib.query.cql.Comparitor;

public class Filter<V> implements QueryFilter<V> {

    private String name;
    private V value;
    private Comparitor op;
    private String label;

    public Filter(String name, V value, Comparitor op) {
        this.name = name;
        this.op = op;
        this.value = value;
    }

    public Filter(String name, V value, Comparitor op, String label) {
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

    public Comparitor getFilterOperation() {
        return op;
    }

    public String getLabel() {
        return label;
    }

    public String toCQL() {
        return CQLQueryModel.FILTER_INDEX_NAME + "." + name + " " + op.getToken() + " " + value;
    }

    public int compareTo(Breadcrumb o) {
        return toString().compareTo(((Filter<V>)o).toString());
    }

    @Override
    public String toString() {
        return name + " " + op + " " + value;
    }
}
