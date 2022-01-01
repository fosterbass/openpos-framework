package org.jumpmind.pos.wrapper.config;

import java.util.ArrayList;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class FunctionCollection {
    private final Map<String, ExpressionFunction<?>> functionLookup;

    public static FunctionCollection empty() {
        return new FunctionCollection(new ArrayList<>());
    }

    public FunctionCollection(Iterable<ExpressionFunction<?>> functions) {
        functionLookup = StreamSupport.stream(functions.spliterator(), false)
                .collect(Collectors.toMap(
                        ExpressionFunction::getIdentifier,
                        Function.identity()
                ));
    }

    public ExpressionFunction<?> findFunction(String identifier) {
        return functionLookup.get(identifier);
    }

    public <T> ExpressionFunction<T> findFunction(String identifier, Class<T> returnType) {
        final ExpressionFunction<?> func = findFunction(identifier);

        // todo: consider implicit type conversions
        if (func == null || func.getReturnType() != returnType) {
            return null;
        }

        //noinspection unchecked
        return (ExpressionFunction<T>) func;
    }
}
