package org.xbib.query.cql.model;

import org.xbib.query.Breadcrumb;
import org.xbib.query.QueryOption;

public class Option<V> implements QueryOption<V> {

    private String name;
    private V value;

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setValue(V value) {
        this.value = value;
    }

    @Override
    public V getValue() {
        return value;
    }

    public String toCQL() {
        return CQLQueryModel.OPTION_INDEX_NAME + "." + name + " = " + value;
    }

    @Override
    public int compareTo(Breadcrumb o) {
        return name.compareTo(((Option<V>)o).getName());
    }

    @Override
    public String toString() {
        return toCQL();
    }

}
