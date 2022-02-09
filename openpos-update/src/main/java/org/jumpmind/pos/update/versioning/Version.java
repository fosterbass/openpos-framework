package org.jumpmind.pos.update.versioning;

public abstract class Version implements Comparable<Version> {
    public abstract String getVersionString();

    public abstract boolean versionEquals(Version other);

    @Override
    public String toString() {
        return this.getVersionString();
    }

    public boolean isOlderThan(Version other) {
        return compareTo(other) < 0;
    }

    public boolean isNewerThan(Version other) {
        return compareTo(other) > 0;
    }
}
