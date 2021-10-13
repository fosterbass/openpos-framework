package org.jumpmind.pos.wrapper.config;

import java.util.List;
import java.util.function.Supplier;

public abstract class ExpressionFunction<T> {
    private final String identifier;
    private final Class<T> returnType;

    public static <T> ExpressionFunction<T> make(String identifier, Class<T> returnType, Supplier<T> supplier) {
        return new FromSupplier<>(identifier, returnType, supplier);
    }

    protected ExpressionFunction(String identifier, Class<T> returnType) {
        this.identifier = identifier;
        this.returnType = returnType;
    }

    public abstract T execute(List<Object> args);

    public String getIdentifier() {
        return identifier;
    }

    public Class<T> getReturnType() {
        return returnType;
    }

    private static class FromSupplier<T> extends ExpressionFunction<T> {
        final Supplier<T> supplier;

        public FromSupplier(String identifier, Class<T> returnType, Supplier<T> supplier) {
            super(identifier, returnType);
            this.supplier = supplier;
        }

        @Override
        public T execute(List<Object> args) {
            return supplier.get();
        }
    }
}
