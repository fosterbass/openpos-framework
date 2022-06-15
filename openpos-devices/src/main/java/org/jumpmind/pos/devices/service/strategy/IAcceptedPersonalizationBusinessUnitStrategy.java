package org.jumpmind.pos.devices.service.strategy;

import java.util.Set;

/**
 * An interface that implementors can use for providing an algorithm to
 * determine the business unit id for a given device.
 */
public interface IAcceptedPersonalizationBusinessUnitStrategy {
    Set<String> getAllowedBusinessUnits();
}
