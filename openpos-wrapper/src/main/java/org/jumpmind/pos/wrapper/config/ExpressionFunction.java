package org.jumpmind.pos.wrapper.config;

import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

public abstract class ExpressionFunction<R> {
    private final String identifier;
    private final Class<R> returnType;

    public static <R> ExpressionFunction<R> make(String identifier, Class<R> returnType, Supplier<R> supplier) {
        return new FromSupplier<>(identifier, returnType, supplier);
    }

    public static <A, R> ExpressionFunction<R> make(String identifier, Class<A> argType, Class<R> returnType, Function<A, R> func) {
        return new FromFunction<>(identifier, argType, returnType, func);
    }

    protected ExpressionFunction(String identifier, Class<R> returnType) {
        this.identifier = identifier;
        this.returnType = returnType;
    }

    public abstract R execute(List<Object> args);

    public String getIdentifier() {
        return identifier;
    }

    public Class<R> getReturnType() {
        return returnType;
    }

    private static class FromSupplier<R> extends ExpressionFunction<R> {
        final Supplier<R> supplier;

        public FromSupplier(String identifier, Class<R> returnType, Supplier<R> supplier) {
            super(identifier, returnType);
            this.supplier = supplier;
        }

        @Override
        public R execute(List<Object> args) {
            return supplier.get();
        }
    }

    private static class FromFunction<A, R> extends ExpressionFunction<R> {
        final Function<A, R> func;
        final Class<A> argType;

        public FromFunction(String identifier, Class<A> argType, Class<R> returnType, Function<A, R> func) {
            super(identifier, returnType);
            this.func = func;
            this.argType = argType;
        }

        @Override
        public R execute(List<Object> args) {
            if (args.size() != 1) {
                throw new IllegalArgumentException("expected a single argument");
            }

            if (args.get(0).getClass() != argType) {
                throw new IllegalArgumentException("argument types are incompatible");
            }

            //noinspection unchecked
            return func.apply((A) args.get(0));
        }
    }
}
