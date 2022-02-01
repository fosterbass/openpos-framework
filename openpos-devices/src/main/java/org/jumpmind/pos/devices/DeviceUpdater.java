package org.jumpmind.pos.devices;

import lombok.extern.slf4j.Slf4j;
import org.jumpmind.pos.devices.model.DeviceModel;
import org.jumpmind.pos.devices.model.DevicesRepository;
import org.jumpmind.pos.devices.service.strategy.IDeviceBusinessUnitIdStrategy;
import org.jumpmind.pos.persist.ITagProvider;
import org.jumpmind.pos.util.clientcontext.ClientContext;
import org.jumpmind.pos.util.event.DeviceConnectedEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.AbstractEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.StreamSupport;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Slf4j
@Component
public class DeviceUpdater implements ApplicationListener<DeviceConnectedEvent> {

    @Autowired
    DevicesRepository devicesRepository;

    @Resource(name = "${openpos.personalization.deviceBusinessUnitIdStrategy:GetBusinessUnitIdFromConfigStrategy}")
    protected IDeviceBusinessUnitIdStrategy deviceBusinessUnitIdStrategy;

    @Value("${openpos.installationId:'not set'}")
    String installationId;

    @Autowired(required = false)
    List<ITagProvider> tagProviders;

    @Autowired
    Environment env;

    @Autowired(required = false)
    CacheManager cacheManager;

    @Autowired
    ClientContext clientContext;

    synchronized public void updateDevice(DeviceModel deviceModel) {
        deviceModel.setTimezoneOffset(clientContext.get("timezoneOffset"));
        deviceModel.setInstallationId(installationId);
        // TODO check properties also before using default
        deviceModel.setLocale(Locale.getDefault().toString());
        deviceModel.setLastUpdateTime(new Date());
        deviceModel.setLastUpdateBy("personalization");
        deviceModel.updateTags((AbstractEnvironment) env);
        deviceModel.setBusinessUnitId(deviceBusinessUnitIdStrategy.getBusinessUnitId(deviceModel));

        if (this.tagProviders != null && tagProviders.size() > 0) {
            MutablePropertySources propSrcs = ((AbstractEnvironment) env).getPropertySources();
            StreamSupport.stream(propSrcs.spliterator(), false)
                    .filter(ps -> ps instanceof EnumerablePropertySource)
                    .map(ps -> ((EnumerablePropertySource<?>) ps).getPropertyNames())
                    .flatMap(Arrays::stream)
                    .filter(propName -> propName.startsWith("openpos.tagconfig.tags") && propName.contains("name"))
                    .forEach(propName -> {
                        for (ITagProvider tagProvider :
                                this.tagProviders) {
                            String name = env.getProperty(propName);
                            String value = tagProvider.getTagValue(deviceModel, name);
                            if (isNotBlank(value)) {
                                deviceModel.setTagValue(name, value);
                            }
                        }
                    });
        }

        devicesRepository.saveDevice(deviceModel);
    }

    @Override
    public void onApplicationEvent(DeviceConnectedEvent event) {
        try {
            if (!"central".equalsIgnoreCase(event.getAppId())) {
                updateDevice(devicesRepository.getDevice(event.getDeviceId()));
                log.info("A device just connected.  Updated the device model in the database. {}-{}", event.getDeviceId(), event.getAppId());
            } else {
                log.info("A virtual device just connected. {}-{}", event.getDeviceId(), event.getAppId());
            }

            if (cacheManager != null) {
                Objects.requireNonNull(cacheManager.getCache("/context/config")).clear();
                Objects.requireNonNull(cacheManager.getCache("/devices/device")).clear();
                Objects.requireNonNull(cacheManager.getCache("/context/buttons")).clear();
            }
        } catch (DeviceNotFoundException ex) {
            // ignore
        }
    }
}
