package org.jumpmind.pos.wrapper.config;

import java.io.*;
import java.math.BigDecimal;
import java.nio.file.FileSystem;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

public class BasicFunctionLibrary {
    public static FunctionCollection collection() {
        final List<ExpressionFunction<?>> functions = new ArrayList<>();
        functions.add(new StringConcatFunction());
        functions.add(ExpressionFunction.make("sin", BigDecimal.class, BigDecimal.class, value -> BigDecimal.valueOf(Math.sin(value.doubleValue()))));
        functions.add(ExpressionFunction.make("cos", BigDecimal.class, BigDecimal.class, value -> BigDecimal.valueOf(Math.cos(value.doubleValue()))));
        functions.add(ExpressionFunction.make("tan", BigDecimal.class, BigDecimal.class, value -> BigDecimal.valueOf(Math.tan(value.doubleValue()))));
        functions.add(ExpressionFunction.make("abs", BigDecimal.class, BigDecimal.class, value -> BigDecimal.valueOf(Math.abs(value.doubleValue()))));
        functions.add(new GetExternalProperty());

        return new FunctionCollection(functions);
    }

    private static class StringConcatFunction extends ExpressionFunction<String> {
        public StringConcatFunction() {
            super("concat", String.class);
        }

        @Override
        public String execute(List<Object> args) {
            return args.stream().map(Object::toString).collect(Collectors.joining(""));
        }
    }

    private static class GetExternalProperty extends ExpressionFunction<String> {
        public GetExternalProperty() {
            super("extprop", String.class);
        }

        @Override
        public String execute(List<Object> args) {
            if (args.size() != 3) {
                throw new IllegalArgumentException("expected 3 arguments");
            }

            if (!args.stream().allMatch(s -> s.getClass() == String.class)) {
                throw new IllegalArgumentException("`extprop` arguments are expected to be strings");
            }

            final String path = (String) args.get(0);
            final String key = (String) args.get(1);
            final String defaultOnNotFound = (String) args.get(2);

            final Properties props = new Properties();

            try (final FileInputStream fs = new FileInputStream(path)) {
                props.load(fs);
            } catch (IOException ignored) {
                return defaultOnNotFound;
            }

            return props.getProperty(key, defaultOnNotFound);
        }
    }
}
