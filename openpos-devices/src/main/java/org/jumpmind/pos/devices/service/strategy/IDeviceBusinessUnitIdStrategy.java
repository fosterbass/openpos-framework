package org.jumpmind.pos.devices.service.strategy;

import org.jumpmind.pos.devices.model.DeviceModel;

/**
 * An interface that implementors can use for providing an algorithm to
 * determine the business unit id for a given device.
 */
public interface IDeviceBusinessUnitIdStrategy {
    /**
     * Returns the businessUnitId that should be used for the given device
     * @param deviceModel The device whose business unit id needs to be determined
     * @return The business unit id calculated.  Can be empty or null if no business
     * unit id was successfully determined.
     */
    String getBusinessUnitId(DeviceModel deviceModel);
}
