package org.xbib.query.cql.elasticsearch.ast;

import org.xbib.query.cql.elasticsearch.Visitor;

/**
 * Elasticsearch expression
 */
public class Expression implements Node {

    private Operator op;

    private Node[] args;

    private TokenType type;

    private boolean visible;

    /**
     * Constructor for folding an expression.
     * Folding takes place when expressions are constructed on the stack
     * to unwrap binary operations into n-ary operations.
     *
     * @param expr the expression
     * @param arg the extra argument
     */
    public Expression(Expression expr, Node arg) {
        this.op = expr.getOperator();
        Node[] exprargs = expr.getArgs();
        this.args = new Node[exprargs.length + 1];
        // organization of the argument list is reverse, the latest arg is at position 0
        this.args[0] = arg;
        System.arraycopy(exprargs, 0, this.args, 1, exprargs.length);
        this.visible = true;
        for (Node node : args) {
            if (node instanceof Name) {
                this.visible = visible && node.isVisible();
                this.type = node.getType();
            }
        }
    }
    
    public Expression(Operator op, Node... args) {
        this.op = op;
        this.args = args;
        this.type = TokenType.EXPRESSION;
        this.visible = false;
        for (Node arg : args) {
            if (arg instanceof Name || arg instanceof Expression) {
                this.visible = visible || arg.isVisible();
            }
        }        
    }

    public Operator getOperator() {
        return op;
    }

    public Node[] getArgs() {
        return args;
    }
    
    public Node getArg1() {
        return args[0];
    }

    public Node getArg2() {
        return args[1];
    }

    @Override
    public boolean isVisible() {
        return visible;
    }

    @Override
    public TokenType getType() {
        return type;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        if (!visible) {
            return "";
        }
        StringBuilder sb = new StringBuilder(op.toString());
        sb.append('(');
        for (int i = 0; i < args.length; i++) {
            sb.append(args[i]);
            if (i < args.length - 1) sb.append(',');
        }
        sb.append(')');
        return sb.toString();
    }
}
