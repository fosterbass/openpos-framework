package org.jumpmind.pos.wrapper.config;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

public final class ConfigExpression {
    public static ConfigExpression parse(ConfigExpressionLexer lexer, FunctionCollection functionLookup, IdentifierCollection identifierLookup) {
        ConfigExpressionLexer.Token token = lexer.getNextToken().orElse(null);

        final Deque<ExpressionStackFrame> frames = new ArrayDeque<>();
        frames.push(new ExpressionStackFrame());

        ExpressionStackFrame currentFrame = null;

        // Its possible for an identifier to be the very last token in an expression but the compiler is still trying to
        // determine if the identifier is a function invocation or not. So if we receive no more tokens but we are still
        // waiting for an identifier to be processed, continue inside the loop so the identifier processing code can be
        // run.
        while (token != null || (currentFrame != null && (currentFrame.getWorkingIdentifier() != null || currentFrame.getWorkingFunction() != null))) {
            currentFrame = frames.peek();
            assert currentFrame != null;

            OperatorKind activeOperator = currentFrame.getActiveOperator();

            if (currentFrame.getWorkingIdentifier() != null) {
                if (token == null || token.getKind() != ConfigExpressionLexer.TokenKind.OPEN_PAREN) {
                    final IdentifierValue<?> value = identifierLookup.findIdentifier(currentFrame.getWorkingIdentifier());
                    final IdentifierNode<?> identNode = new IdentifierNode<>(value);

                    if (currentFrame.getActiveOperator() != null) {
                        if (identNode.expectedResult() != BigDecimal.class) {
                            throw new IllegalStateException("must be a number ident");
                        }

                        //noinspection unchecked
                        processOperator(currentFrame, (IdentifierNode<BigDecimal>) identNode);
                    } else {
                        currentFrame.pushNode(identNode);
                    }

                    currentFrame.setWorkingIdentifier(null);
                }
            }

            // we require a token by this point for evaluation.
            if (token == null) {
                continue;
            }

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

                case STRING_LITERAL:
                    if (activeOperator != null) {
                        // todo: consider string concat with `+`
                        throw new IllegalStateException("cannot operate on strings");
                    }

                    currentFrame.pushNode(new StringLiteralNode(token.getRawText()));
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

                case TRUE_KW:
                    currentFrame.pushNode(new BooleanNode(true));
                    break;

                case FALSE_KW:
                    currentFrame.pushNode(new BooleanNode(false));
                    break;

                case IDENTIFIER:
                    currentFrame.setWorkingIdentifier(token.getRawText());
                    break;

                case COMMA:
                    if (!currentFrame.isForFunctionArg()) {
                        throw new IllegalStateException("must be working on a function");
                    }

                    final ExpressionStackFrame argFrame = frames.pop();
                    final ExpressionStackFrame parent = frames.peek();

                    assert parent != null;

                    final FunctionNode<?> workingFunction = parent.getWorkingFunction();
                    if (workingFunction == null) {
                        throw new IllegalStateException("must be working on a function");
                    }

                    if (argFrame.peekNode() == null) {
                        throw new IllegalStateException("function argument cannot be evaluated");
                    }

                    workingFunction.addArgument(argFrame.popNode());
                    frames.push(new ExpressionStackFrame(true));

                    break;

                case OPEN_PAREN:
                    if (currentFrame.getWorkingIdentifier() != null) {
                        final ExpressionFunction<?> function = functionLookup.findFunction(currentFrame.workingIdentifier);

                        if (function == null) {
                            throw new IllegalStateException("function not found");
                        }

                        currentFrame.setWorkingFunction(new FunctionNode<>(function));
                        currentFrame.setWorkingIdentifier(null);
                        frames.push(new ExpressionStackFrame(true));
                    } else {
                        frames.push(new ExpressionStackFrame());
                    }
                    break;

                case CLOSE_PAREN:
                    final ExpressionStackFrame compiledFrame = frames.pop();
                    final ExpressionStackFrame parentFrame = frames.peek();

                    assert parentFrame != null;

                    if (parentFrame.getWorkingFunction() != null) {
                        final ExpressionNode<?> argExpression = compiledFrame.peekNode();

                        // close out the last argument if any
                        if (argExpression != null) {
                            parentFrame.getWorkingFunction().addArgument(compiledFrame.popNode());
                        }

                        final FunctionNode<?> functionNode = parentFrame.getWorkingFunction();
                        parentFrame.setWorkingFunction(null);

                        if (parentFrame.getActiveOperator() != null) {
                            if (functionNode.expectedResult() != BigDecimal.class) {
                                throw new IllegalStateException("expected numeric function result");
                            }

                            //noinspection unchecked
                            processOperator(parentFrame, (ExpressionNode<BigDecimal>) functionNode);
                        } else {
                            parentFrame.pushNode(functionNode);
                        }
                    } else if (parentFrame.getActiveOperator() != null) {
                        final ExpressionNode<?> node = compiledFrame.peekNode();

                        if (node.expectedResult() != BigDecimal.class) {
                            throw new IllegalStateException("expected numeric node");
                        }

                        //noinspection unchecked
                        processOperator(parentFrame, new ParenNode<>((ExpressionNode<BigDecimal>) node));
                    } else {
                        parentFrame.pushNode(new ParenNode<>(compiledFrame.peekNode()));
                    }

                    break;
            }

            token = lexer.getNextToken().orElse(null);
        }

        if (frames.size() != 1) {
            throw new IllegalArgumentException("expression could not be parsed");
        }

        final ExpressionStackFrame finalizedFrame = frames.pop();
        final ExpressionNode<?> rootNode = finalizedFrame.popNode();

        if (finalizedFrame.peekNode() != null) {
            throw new IllegalArgumentException("expression could not be parsed");
        }

        return new ConfigExpression(rootNode);
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
            case EQUALITY:
            case INEQUALITY:
                return 1;

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
        public final boolean isForFunctionArg;

        private String workingIdentifier;
        private FunctionNode<?> workingFunction;
        private OperatorKind activeOperator;
        private final Deque<ExpressionNode<?>> nodes = new ArrayDeque<>();

        public ExpressionStackFrame() {
            this(false);
        }

        public ExpressionStackFrame(boolean isForFunctionArg) {
            this.isForFunctionArg = isForFunctionArg;
        }

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

        public String getWorkingIdentifier() {
            return workingIdentifier;
        }

        public void setWorkingIdentifier(String value) {
            if (value == null) {
                workingIdentifier = null;
                return;
            }

            if (workingIdentifier != null) {
                throw new IllegalStateException("working identifier already assigned");
            }

            workingIdentifier = value;
        }

        public boolean isForFunctionArg() {
            return this.isForFunctionArg;
        }

        public FunctionNode<?> getWorkingFunction() {
            return workingFunction;
        }

        public void setWorkingFunction(FunctionNode<?> value) {
            if (value == null) {
                workingFunction = null;
                return;
            }

            if (workingFunction != null) {
                throw new IllegalStateException("working identifier already assigned");
            }

            workingFunction = value;
        }
    }

    public static final class StringLiteralNode implements ExpressionNode<String> {
        private final String value;

        public StringLiteralNode(String value) {
            this.value = value;
        }

        @Override
        public Class<String> expectedResult() {
            return String.class;
        }

        @Override
        public String evaluate() {
            return value;
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
        DIVISION,
        EQUALITY,
        INEQUALITY
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

    private static final class ParenNode<T> implements ExpressionNode<T> {
        private final ExpressionNode<T> expression;

        public ParenNode(ExpressionNode<T> expression) {
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

    private static final class FunctionNode<T> implements ExpressionNode<T> {
        private final ExpressionFunction<T> function;
        private final List<ExpressionNode<?>> arguments = new ArrayList<>();

        public FunctionNode(ExpressionFunction<T> function) {
            this.function = function;
        }

        @Override
        public Class<T> expectedResult() {
            return function.getReturnType();
        }

        @Override
        public T evaluate() {
            final List<Object> argumentValues = arguments.stream()
                    .map(ExpressionNode::evaluate)
                    .collect(Collectors.toList());

            return function.execute(argumentValues);
        }

        public <A> void addArgument(ExpressionNode<A> arg) {
            arguments.add(arg);
        }
    }

    private static final class IdentifierNode<T> implements ExpressionNode<T> {
        private final IdentifierValue<T> identifierValue;

        public IdentifierNode(IdentifierValue<T> identifierValue) {
            if (identifierValue == null) {
                throw new IllegalArgumentException("identifier value required");
            }

            this.identifierValue = identifierValue;
        }

        @Override
        public Class<T> expectedResult() {
            return identifierValue.getValueType();
        }

        @Override
        public T evaluate() {


            return identifierValue.getValue();
        }
    }

    private static final class BooleanNode implements ExpressionNode<Boolean> {
        private final Boolean value;

        public BooleanNode(Boolean value) {
            this.value = value;
        }

        @Override
        public Class<Boolean> expectedResult() {
            return Boolean.class;
        }

        @Override
        public Boolean evaluate() {
            return value;
        }
    }

    private static final class BooleanOperatorNode<LEFT, RIGHT> implements ExpressionNode<Boolean> {
        private ExpressionNode<LEFT> leftNode;
        private ExpressionNode<RIGHT> rightNode;

        private final boolean invert;

        public BooleanOperatorNode(ExpressionNode<LEFT> left, ExpressionNode<RIGHT> right, boolean inequalityComparison) {
            leftNode = left;
            rightNode = right;
            invert = inequalityComparison;
        }

        @Override
        public Class<Boolean> expectedResult() {
            return Boolean.class;
        }

        @Override
        public Boolean evaluate() {
            boolean result = leftNode.evaluate().equals(rightNode.evaluate());

            if (invert) {
                return !result;
            }

            return result;
        }
    }
}
