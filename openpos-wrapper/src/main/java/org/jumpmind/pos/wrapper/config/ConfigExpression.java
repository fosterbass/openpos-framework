package org.jumpmind.pos.wrapper.config;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.NoSuchElementException;

public final class ConfigExpression {
    public static ConfigExpression parse(ConfigExpressionLexer lexer) {
        ConfigExpressionLexer.Token token = lexer.getNextToken().orElse(null);

        final Deque<ExpressionStackFrame> frames = new ArrayDeque<>();
        frames.push(new ExpressionStackFrame());

        while (token != null) {
            final ExpressionStackFrame currentFrame = frames.peek();
            OperatorKind activeOperator = currentFrame.getActiveOperator();

            switch (token.getKind()) {
                case NUMBER:
                    final NumericLiteralNode right = new NumericLiteralNode(token.getDecimalValue());

                    // if start processing a new number, and we're expecting it to be an operand, then process it
                    // as an operand; otherwise push it on to the stack and wait for an operator to come along.
                    if (activeOperator != null) {
                        processOperator(currentFrame, right);
                    } else {
                        currentFrame.pushNode(right);
                    }
                    break;

                case PLUS:
                    currentFrame.setActiveOperator(OperatorKind.ADDITION);
                    break;

                case ASTERISK:
                    currentFrame.setActiveOperator(OperatorKind.MULTIPLICATION);
                    break;

                case SLASH:
                    currentFrame.setActiveOperator(OperatorKind.DIVISION);
                    break;

                case MINUS:
                    currentFrame.setActiveOperator(OperatorKind.SUBTRACTION);
                    break;

                case OPEN_PAREN:
                    frames.push(new ExpressionStackFrame());
                    break;

                case CLOSE_PAREN:
                    final ExpressionStackFrame parenFrame = frames.pop();
                    final ExpressionStackFrame parentFrame = frames.peek();

                    if (parentFrame.getActiveOperator() != null) {
                        final ExpressionNode<?> node = parenFrame.peekNode();

                        if (node.expectedResult() != BigDecimal.class) {
                            throw new IllegalStateException("expected numeric node");
                        }

                        //noinspection unchecked
                        processOperator(parentFrame, new ParenExpression<>((ExpressionNode<BigDecimal>) node));
                    } else {
                        parentFrame.pushNode(new ParenExpression<>(parenFrame.peekNode()));
                    }

                    break;
            }

            token = lexer.getNextToken().orElse(null);
        }

        return new ConfigExpression(frames.pop().popNode());
    }

    private final ExpressionNode<?> rootNode;

    private ConfigExpression(ExpressionNode<?> rootNode) {
        this.rootNode = rootNode;
    }

    private static void processOperator(ExpressionStackFrame currentFrame, ExpressionNode<BigDecimal> right) {
        final OperatorKind activeOperator = currentFrame.getActiveOperator();

        // the previous node must be a node that returns a numeric type (only math supported types)
        final ExpressionNode<BigDecimal> left = popNumericNodeFrom(currentFrame);

        // if the previous node was an operator, we need to inspect its precedence and ensure
        // that multiply/divisions get pushed further down the tree as we are going to execute in
        // depth-first manner.
        if (left instanceof NumericOperatorNode) {
            final NumericOperatorNode leftAsOperatorNode = (NumericOperatorNode) left;

            final int leftPrecedence = leftAsOperatorNode.getPrecedence();
            final int rightPrecedence = getOperatorPrecedence(activeOperator);

            if (leftPrecedence < rightPrecedence) {
                final ExpressionNode<BigDecimal> lowerRightNode = leftAsOperatorNode.getRight();
                final NumericOperatorNode newRightNode = makeOperator(activeOperator, lowerRightNode, right);

                leftAsOperatorNode.setRight(newRightNode);

                currentFrame.pushNode(left);
            } else {
                currentFrame.pushNode(makeOperator(activeOperator, left, right));
            }
        } else {
            currentFrame.pushNode(makeOperator(activeOperator, left, right));
        }

        currentFrame.setActiveOperator(null);
    }

    private static ExpressionNode<BigDecimal> popNumericNodeFrom(ExpressionStackFrame stack) {
        final ExpressionNode<?> node;

        try {
            node = stack.popNode();
        } catch (NoSuchElementException ex) {
            // todo: return error node
            throw ex;
        }

        if (node.expectedResult() != BigDecimal.class) {
            // todo: return error node
            throw new IllegalStateException();
        }

        //noinspection unchecked
        return (ExpressionNode<BigDecimal>) node;
    }

    private static int getOperatorPrecedence(OperatorKind kind) {
        switch (kind) {
            case ADDITION:
            case SUBTRACTION:
                return 2;

            case MULTIPLICATION:
            case DIVISION:
                return 4;

            default: throw new IllegalArgumentException("invalid operator kind");
        }
    }

    private static NumericOperatorNode makeOperator(OperatorKind kind, ExpressionNode<BigDecimal> left, ExpressionNode<BigDecimal> right) {
        switch (kind) {
            case MULTIPLICATION: return new NumericMultNode(left, right);
            case ADDITION: return new NumericAddNode(left, right);
            case DIVISION: return new NumericDivNode(left, right);
            case SUBTRACTION: return new NumericSubNode(left, right);

            default: throw new IllegalStateException("invalid operator");
        }
    }

    public String process(/* identifier lookup */) {
        return this.rootNode.evaluate().toString();
    }

    private interface ExpressionNode<T> {
        Class<T> expectedResult();
        T evaluate();
    }

    private static final class ExpressionStackFrame {
        private OperatorKind activeOperator;
        private Deque<ExpressionNode<?>> nodes = new ArrayDeque<>();

        public ExpressionNode<?> popNode() throws NoSuchElementException {
            return nodes.pop();
        }

        public ExpressionNode<?> peekNode() {
            return nodes.peek();
        }

        public <T> void pushNode(ExpressionNode<T> node) {
            nodes.push(node);
        }

        public OperatorKind getActiveOperator() {
            return activeOperator;
        }

        public void setActiveOperator(OperatorKind kind) {
            if (kind == null) {
                activeOperator = null;
                return;
            }

            if (activeOperator != null) {
                throw new IllegalStateException("an operator is already active");
            }

            activeOperator = kind;
        }
    }

    private static final class NumericLiteralNode implements ExpressionNode<BigDecimal> {
        private final BigDecimal value;

        public NumericLiteralNode(BigDecimal value) {
            this.value = value;
        }

        @Override
        public Class<BigDecimal> expectedResult() {
            return BigDecimal.class;
        }

        @Override
        public BigDecimal evaluate() {
            return value;
        }
    }

    private enum OperatorKind {
        ADDITION,
        SUBTRACTION,
        MULTIPLICATION,
        DIVISION
    }

    private abstract static class NumericOperatorNode implements ExpressionNode<BigDecimal> {
        protected ExpressionNode<BigDecimal> left;
        protected ExpressionNode<BigDecimal> right;

        public NumericOperatorNode(ExpressionNode<BigDecimal> left, ExpressionNode<BigDecimal> right) {
            this.left = left;
            this.right = right;
        }

        @Override
        public final Class<BigDecimal> expectedResult() {
            return BigDecimal.class;
        }

        public abstract int getPrecedence();

        public ExpressionNode<BigDecimal> getLeft() {
            return this.left;
        }

        public void setLeft(ExpressionNode<BigDecimal> value) {
            this.left = value;
        }

        public ExpressionNode<BigDecimal> getRight() {
            return this.right;
        }

        public void setRight(ExpressionNode<BigDecimal> value) {
            this.right = value;
        }
    }

    private static final class NumericAddNode extends NumericOperatorNode {
        public NumericAddNode(ExpressionNode<BigDecimal> left, ExpressionNode<BigDecimal> right) {
            super(left, right);
        }

        @Override
        public BigDecimal evaluate() {
            return left.evaluate().add(right.evaluate());
        }

        @Override
        public int getPrecedence() {
            return getOperatorPrecedence(OperatorKind.ADDITION);
        }
    }

    private static final class NumericSubNode extends NumericOperatorNode {
        public NumericSubNode(ExpressionNode<BigDecimal> left, ExpressionNode<BigDecimal> right) {
            super(left, right);
        }

        @Override
        public BigDecimal evaluate() {

            return left.evaluate().subtract(right.evaluate());
        }

        @Override
        public int getPrecedence() {
            return getOperatorPrecedence(OperatorKind.SUBTRACTION);
        }
    }

    private static final class NumericMultNode extends NumericOperatorNode {
        public NumericMultNode(ExpressionNode<BigDecimal> left, ExpressionNode<BigDecimal> right) {
            super(left, right);
        }

        @Override
        public BigDecimal evaluate() {
            return left.evaluate().multiply(right.evaluate());
        }

        @Override
        public int getPrecedence() {
            return getOperatorPrecedence(OperatorKind.MULTIPLICATION);
        }
    }

    private static final class NumericDivNode extends NumericOperatorNode {
        public NumericDivNode(ExpressionNode<BigDecimal> left, ExpressionNode<BigDecimal> right) {
            super(left, right);
        }

        @Override
        public BigDecimal evaluate() {
            return left.evaluate().divide(right.evaluate(), RoundingMode.UNNECESSARY);
        }

        @Override
        public int getPrecedence() {
            return getOperatorPrecedence(OperatorKind.DIVISION);
        }
    }

    private static final class ParenExpression<T> implements ExpressionNode<T> {
        private final ExpressionNode<T> expression;

        public ParenExpression(ExpressionNode<T> expression) {
            this.expression = expression;
        }

        @Override
        public Class<T> expectedResult() {
            return this.expression.expectedResult();
        }

        @Override
        public T evaluate() {
            return this.expression.evaluate();
        }
    }
}
