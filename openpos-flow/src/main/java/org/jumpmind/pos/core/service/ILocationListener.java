package org.jumpmind.pos.core.service;

import org.jumpmind.pos.util.model.LocationData;

public interface ILocationListener {

    public void locationChanged(LocationData locationData);

}
