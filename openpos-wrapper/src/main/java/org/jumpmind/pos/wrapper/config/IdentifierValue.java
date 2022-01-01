package org.jumpmind.pos.wrapper.config;

public abstract class IdentifierValue<T> {
    private final String identifier;
    private final Class<T> valueType;

    public static <T> IdentifierValue<T> fromConstant(String identifier, T value) {
        return new ConstantIdentifierValue<>(identifier, value);
    }

    public IdentifierValue(String identifier, Class<T> valueType) {
        this.identifier = identifier;
        this.valueType = valueType;
    }

    abstract T getValue();

    public String getIdentifier() {
        return identifier;
    }

    public Class<T> getValueType() {
        return valueType;
    }

    private static final class ConstantIdentifierValue<T> extends IdentifierValue<T> {
        private final T value;

        public ConstantIdentifierValue(String identifier, T value) {
            //noinspection unchecked
            super(identifier, (Class<T>) value.getClass());
            this.value = value;
        }

        @Override
        T getValue() {
            return value;
        }
    }
}
