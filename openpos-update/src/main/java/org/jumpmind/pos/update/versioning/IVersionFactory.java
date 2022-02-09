package org.jumpmind.pos.update.versioning;

public interface IVersionFactory<T extends Version> {
    T fromString(String version) throws IllegalArgumentException;
}
