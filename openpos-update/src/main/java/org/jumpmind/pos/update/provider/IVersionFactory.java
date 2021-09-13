package org.jumpmind.pos.update.provider;

public interface IVersionFactory<T extends Version> {
    T fromString(String version);
}
