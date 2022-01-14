package org.jumpmind.pos.persist;


import org.jumpmind.pos.util.model.IDeviceAttributes;

public interface ITagProvider {
    String getTagValue(IDeviceAttributes deviceAttributes, String tagName);
}
