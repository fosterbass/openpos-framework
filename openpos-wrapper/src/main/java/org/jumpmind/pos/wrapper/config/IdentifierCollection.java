package org.jumpmind.pos.wrapper.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class IdentifierCollection {
    private final Map<String, IdentifierValue<?>> identifierLookup;

    public static IdentifierCollection empty() {
        return new IdentifierCollection(new ArrayList<>());
    }

    public IdentifierCollection() {
        identifierLookup = new HashMap<>();
    }

    public IdentifierCollection(Iterable<IdentifierValue<?>> identifiers) {
        identifierLookup = StreamSupport.stream(identifiers.spliterator(), false)
                .collect(Collectors.toMap(
                        IdentifierValue::getIdentifier,
                        Function.identity()
                ));
    }

    public <T> void addIdentifier(IdentifierValue<T> value) {
        identifierLookup.putIfAbsent(value.getIdentifier(), value);
    }

    public IdentifierValue<?> findIdentifier(String identifier) {
        return identifierLookup.get(identifier);
    }

    public <T> IdentifierValue<T> findIdentifier(String identifier, Class<T> valueType) {
        final IdentifierValue<?> ident = findIdentifier(identifier);

        // todo: consider implicit type conversions
        if (ident == null || ident.getValueType() != valueType) {
            return null;
        }

        //noinspection unchecked
        return (IdentifierValue<T>) ident;
    }
}
