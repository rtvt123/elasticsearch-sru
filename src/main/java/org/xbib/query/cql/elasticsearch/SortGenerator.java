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
 * Build sort in JSON syntax from abstract syntax tree
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
                builder.value(node.getName().toString());
            } else {
                builder.startObject().field(node.getName().toString()).startObject();
                while (!modifiers.isEmpty()) {
                    Modifier mod = modifiers.pop();
                    String s = mod.getName().toString();
                    if (s.equals("ascending")) {
                        builder.field("order", "asc");

                    } else if (s.equals("descending")) {
                        builder.field("order", "desc");

                    } else {
                        builder.field(mod.getName().toString(), mod.getTerm());

                    }
                }
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
