package org.xbib.query.cql.elasticsearch;

import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;

import java.io.IOException;

public class SourceGenerator {

    private final XContentBuilder builder;

    public SourceGenerator() throws IOException {
        this.builder = XContentFactory.jsonBuilder();
    }

    public void build(QueryGenerator query,
            int from, int size) throws IOException {
        build(query, from, size,  null, null);
    }

    public void build(QueryGenerator query, int from, int size, XContentBuilder sort, XContentBuilder facets) throws IOException {
        builder.startObject();
        builder.field("from", from);
        builder.field("size", size);
        builder.rawField("query", query.getResult().bytes());
        if (sort != null && sort.bytes().length() > 0) {
            builder.rawField("sort", sort.bytes());
        }
        if (facets != null && facets.bytes().length() > 0) {
            builder.rawField("aggregations", facets.bytes() );
        }
        builder.endObject();
        builder.close();
    }

    public XContentBuilder getResult() {
        return builder;
    }
}
