package org.jumpmind.pos.wrapper.config;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class BasicFunctionLibrary {
    public static FunctionCollection collection() {
        final List<ExpressionFunction<?>> functions = new ArrayList<>();
        functions.add(new StringConcatFunction());
        functions.add(ExpressionFunction.make("sin", BigDecimal.class, BigDecimal.class, value -> BigDecimal.valueOf(Math.sin(value.doubleValue()))));
        functions.add(ExpressionFunction.make("cos", BigDecimal.class, BigDecimal.class, value -> BigDecimal.valueOf(Math.cos(value.doubleValue()))));
        functions.add(ExpressionFunction.make("tan", BigDecimal.class, BigDecimal.class, value -> BigDecimal.valueOf(Math.tan(value.doubleValue()))));
        functions.add(ExpressionFunction.make("abs", BigDecimal.class, BigDecimal.class, value -> BigDecimal.valueOf(Math.abs(value.doubleValue()))));

        return new FunctionCollection(functions);
    }

    private static class StringConcatFunction extends ExpressionFunction<String> {
        protected StringConcatFunction() {
            super("concat", String.class);
        }

        @Override
        public String execute(List<Object> args) {
            return args.stream().map(Object::toString).collect(Collectors.joining(""));
        }
    }
}
