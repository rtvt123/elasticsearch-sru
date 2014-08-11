package org.xbib.query.cql.elasticsearch;

import org.elasticsearch.common.xcontent.XContentBuilder;
import org.xbib.query.cql.SyntaxException;
import org.xbib.query.cql.elasticsearch.ast.Expression;
import org.xbib.query.cql.elasticsearch.ast.Modifier;
import org.xbib.query.cql.elasticsearch.ast.Name;
import org.xbib.query.cql.elasticsearch.ast.Node;
import org.xbib.query.cql.elasticsearch.ast.Operator;
import org.xbib.query.cql.elasticsearch.ast.Token;

import java.io.IOException;
import java.util.Stack;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;

/**
 * Build sort in Elasticsearch JSON syntax from abstract syntax tree
 */
public class SortGenerator implements Visitor {

    private final XContentBuilder builder;

    private final Stack<Modifier> modifiers;

    public SortGenerator() throws IOException {
        this.builder = jsonBuilder();
        this.modifiers = new Stack<Modifier>();
    }

    public void start() throws IOException {
        builder.startArray();
    }

    public void end() throws IOException {
        builder.endArray();
    }

    public XContentBuilder getResult() {
        return builder;
    }

    @Override
    public void visit(Token node) {
    }

    @Override
    public void visit(Name node) {
        try {
            if (modifiers.isEmpty()) {
                builder.value(node.getName());
            } else {
                builder.startObject().field(node.getName()).startObject();
                while (!modifiers.isEmpty()) {
                    Modifier mod = modifiers.pop();
                    String s = mod.getName().toString();
                    switch (s) {
                        case "ascending":
                        case "sort.ascending": {
                            builder.field("order", "asc");
                            break;
                        }
                        case "descending":
                        case "sort.descending": {
                            builder.field("order", "desc");
                            break;
                        }
                        default: {
                            builder.field(mod.getName().toString(), mod.getTerm());
                            break;
                        }
                    }
                }
                // TODO ES 1.4 http://www.elasticsearch.org/guide/en/elasticsearch/reference/current/search-request-sort.html#_ignoring_unmapped_fields
                builder.field("ignore_unmapped", true);
                builder.field("missing", "_last");
                builder.endObject();
                builder.endObject();
            }
        } catch (IOException e) {
            throw new SyntaxException(e.getMessage(), e);
        }
    }

    @Override
    public void visit(Modifier node) {
        modifiers.push(node);
    }

    @Override
    public void visit(Operator node) {
    }

    @Override
    public void visit(Expression node) {
        Operator op = node.getOperator();
        if (op == Operator.SORT) {
            for (Node arg : node.getArgs()) {
                arg.accept(this);
            }
        }
    }

}
