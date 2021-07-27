package org.jumpmind.pos.util;

import java.lang.reflect.Field;

@FunctionalInterface
public interface ObjectSearchVisitor {
    void visit(Object parentObject, Object object, Field location);
}
