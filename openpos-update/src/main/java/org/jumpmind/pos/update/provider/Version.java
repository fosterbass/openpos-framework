package org.jumpmind.pos.update.provider;

public abstract class Version<T extends Version<?>> implements Comparable<T> {
    public abstract String getVersionString();

    public abstract boolean equals(T other);

    @Override
    public String toString() {
        return this.getVersionString();
    }

    public boolean isOlderThan(T other) {
        return compareTo(other) < 0;
    }

    public boolean isNewerThan(T other) {
        return compareTo(other) > 0;
    }
}
