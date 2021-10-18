package org.jumpmind.pos.update.versioning.basic;

import org.jumpmind.pos.update.versioning.Version;

public class Basic extends Version {
    
    private String basicVersion;
    
    public Basic(String value){
        basicVersion = value;
    }
    
    @Override
    public String getVersionString() {
        return basicVersion;
    }

    @Override
    public boolean equals(Version other) {
        if(other == null){
            return false;
        }
        return basicVersion.equals(other.getVersionString());
    }

    @Override
    public int compareTo(Version version) {
        return basicVersion.compareTo(version.getVersionString());
    }
}
