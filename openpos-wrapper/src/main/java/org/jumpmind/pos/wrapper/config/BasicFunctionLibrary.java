package org.jumpmind.pos.wrapper.config;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class BasicFunctionLibrary {
    public static FunctionCollection collection() {
        final List<ExpressionFunction<?>> functions = new ArrayList<>();
        functions.add(new StringConcatFunction());

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
