package org.jumpmind.pos.update.provider;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.collections4.map.CaseInsensitiveMap;
import org.jumpmind.pos.update.versioning.IVersionFactory;
import org.jumpmind.pos.update.versioning.PackageVersioning;
import org.jumpmind.pos.update.versioning.Version;
import org.jumpmind.pos.update.versioning.Versioning;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.bind.BindContext;
import org.springframework.boot.context.properties.bind.BindHandler;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.ConfigurationPropertyName;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class SoftwareProvider {
    private final Map<String, ISoftwareProvider> packageProviders = new CaseInsensitiveMap<>();
    private final Map<String, PackageVersioning> versioningMap = new CaseInsensitiveMap<>();

    @Autowired
    public SoftwareProvider(
            Environment env,
            Map<String, ISoftwareProviderFactory> factories,
            Map<String, IVersionFactory<? extends Version>> versionFactories
    ) {
        if (MapUtils.isEmpty(factories)) {
            return;
        }

        if (MapUtils.isEmpty(factories)) {
            log.error("could not locate any version schemes; they are required in order to use the software providers");
            return;
        }

        final Binder binder = Binder.get(env);

        final Map<String, ?> packages = binder.bindOrCreate(
                "openpos.update.packages",
                Bindable.mapOf(String.class, HashMap.class),
                new BindHandler() {
                    @Override
                    public Object onFailure(ConfigurationPropertyName name, Bindable<?> target, BindContext context, Exception error) {
                        return new HashMap<String, HashMap<String, ?>>();
                    }
                }
        );

        packages.keySet().forEach(packageName -> {
            final SoftwareProviderConfiguration<?> providerConfig;

            try {
                providerConfig = binder.bindOrCreate(
                        "openpos.update.packages." + packageName,
                        SoftwareProviderConfiguration.class
                );
            } catch (Exception ex) {
                log.error("failed to decode configuration for software package '{}'; ignoring...", packageName, ex);
                return;
            }

            final String beanName = providerConfig.getProvider() + ".softwareprovider";
            final ISoftwareProviderFactory factory = factories.get(beanName);
            if (factory == null) {
                log.error("factory named '{}' was not found; ignoring software package '{}'", providerConfig.getProvider(), packageName);
                return;
            }

            final String versionBeanName = providerConfig.getVersioning() + ".versionfactory";
            final IVersionFactory<? extends Version> versionFactory = versionFactories.get(versionBeanName);
            if (versionFactory == null) {
                log.error("version factory named '{}' was not found; ignoring software package '{}'", providerConfig.getVersioning(), packageName);
                return;
            }

            final PackageVersioning versioning = new PackageVersioning(versionFactory);

            final String configKey = "openpos.update.packages." + packageName + ".config";

            try {
                final ISoftwareProvider provider = factory.createProvider(versioning, configKey);

                if (provider == null) {
                    throw new InstantiationException("failed to create provider");
                }

                packageProviders.put(packageName, provider);
                versioningMap.put(packageName, versioning);

                log.info("loaded '{}' for package '{}'", beanName, packageName);
            } catch (Exception ex) {
                log.error("failed to create provider for package '{}'; ignoring...", packageName, ex);
            }
        });
    }

    public ISoftwareProvider getSoftwareProvider(String packageName) {
        return packageProviders.get(packageName);
    }

    public PackageVersioning getVersioningFor(String packageName) {
        return versioningMap.get(packageName);
    }
}
