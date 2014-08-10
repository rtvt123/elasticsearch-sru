package org.xbib.query.cql.elasticsearch.model;

import org.xbib.query.Breadcrumb;
import org.xbib.query.QueryOption;

/**
 * Options for Elasticsearch query language
 * 
 * @param <V> the value class parameter
 */
public class ElasticsearchOption<V> implements QueryOption<V> {

    private String name;

    private V value;

    public ElasticsearchOption(String name, V value) {
        this.name = name;
        this.value = value;
    }

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

    @Override
    public int compareTo(Breadcrumb o) {
        return name.compareTo(((ElasticsearchOption<V>)o).getName());
    }

}
