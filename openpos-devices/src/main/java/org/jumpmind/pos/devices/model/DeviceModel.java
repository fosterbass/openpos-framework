package org.jumpmind.pos.devices.model;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.map.CaseInsensitiveMap;

import static org.apache.logging.log4j.util.Strings.isNotBlank;

import org.jumpmind.pos.persist.*;
import org.jumpmind.pos.persist.model.ITaggedModel;
import org.jumpmind.pos.util.model.IDeviceAttributes;
import org.jumpmind.util.AppUtils;
import org.springframework.core.env.AbstractEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.MutablePropertySources;

@Tagged(includeTagsInPrimaryKey = false)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(onlyExplicitlyIncluded = true)
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
@TableDef(name = "device", description = "A device used to transaction commerce for a Business Unit",
        primaryKey = {"deviceId"})
public class DeviceModel extends AbstractModel implements ITaggedModel, IDeviceAttributes {

    @ToString.Include
    @EqualsAndHashCode.Include
    @ColumnDef(description = "A unique identifier for this Device")
    private String deviceId;

    @ToString.Include
    @EqualsAndHashCode.Include
    @ColumnDef
    private String appId;

    @ToString.Include
    @EqualsAndHashCode.Include
    @ColumnDef
    private String parentDeviceId;

    @ColumnDef(description = "The timezone offset under which this Device currently operates")
    @Builder.Default
    String timezoneOffset = AppUtils.getTimezoneOffset();

    @ColumnDef(description = "The Business Unit under which this Device currently operates")
    String businessUnitId;

    @ColumnDef(size = "255", description = "A user defined name for the Device")
    private String description;

    @ColumnDef
    private String installationId;

    @Builder.Default
    private Map<String, String> tags = new CaseInsensitiveMap<>();

    @ToString.Include
    @Builder.Default
    private String deviceMode = DEVICE_MODE_DEFAULT;

    private List<DeviceParamModel> deviceParamModels;

    public static final String DEVICE_MODE_DEFAULT  = "default";
    public static final String BRAND_DEFAULT = "default";
    public static final String DEVICE_MODE_TRAINING = "training";

    @Override
    public String getTagValue(String tagName) {
        return tags.get(tagName.toUpperCase());
    }

    @Override
    public void setTagValue(String tagName, String tagValue) {
        tags.put(tagName.toUpperCase(), tagValue);
    }

    @Override
    public void setTags(Map<String, String> tags) {
        this.tags.clear();
        this.tags.putAll(tags);
    }

    @Override
    public void clearTagValue(String tagName) {
        tags.remove(tagName);
    }

    @Override
    public Map<String, String> getTags() {
        return tags != null ? new CaseInsensitiveMap<>(tags) : new CaseInsensitiveMap<>();
    }

    public void updateTags(AbstractEnvironment env) {
        MutablePropertySources propSrcs = env.getPropertySources();
        StreamSupport.stream(propSrcs.spliterator(), false)
                .filter(EnumerablePropertySource.class::isInstance)
                .map(ps -> ((EnumerablePropertySource<?>) ps).getPropertyNames())
                .flatMap(Arrays::stream)
                .filter(propName -> propName.startsWith("openpos.tags"))
                .forEach(propName ->
                        tags.put(propName.substring("openpos.tags".length() + 1), env.getProperty(propName) != null ? env.getProperty(propName) : "*"));

    }

    public String withOutBusinessUnitId() {
        String withOutBusinessUnitId = deviceId;
        int index = deviceId.indexOf(businessUnitId);
        if (index == 0) {
            withOutBusinessUnitId = deviceId.substring(businessUnitId.length());
            if (withOutBusinessUnitId.startsWith("-")) {
                withOutBusinessUnitId = withOutBusinessUnitId.substring(1);
            }
        }
        return withOutBusinessUnitId;
    }

    @JsonIgnore
    public boolean isDefaultDeviceMode() {
        return deviceMode == null || DEVICE_MODE_DEFAULT.equals(deviceMode);
    }

    @JsonIgnore
    public void setDefaultDeviceMode() {
        deviceMode = DEVICE_MODE_DEFAULT;
    }

    @JsonIgnore
    public boolean isTrainingDeviceMode() {
        return DEVICE_MODE_TRAINING.equals(deviceMode);
    }

    @JsonIgnore
    public void setTrainingDeviceMode() {
        deviceMode = DEVICE_MODE_TRAINING;
    }

    @JsonIgnore
    @Override
    public Map<String,String> getDeviceParamsMap() {
        if (this.deviceParamModels != null) {
            return this.deviceParamModels.stream().collect(Collectors.toMap(DeviceParamModel::getParamName, DeviceParamModel::getParamValue));
        }

        return Collections.emptyMap();
    }

    public String getBrand() {
        if (CollectionUtils.isNotEmpty(deviceParamModels)) {
            return deviceParamModels.stream()
                    .filter(param -> "brandId".equals(param.getParamName()))
                    .findFirst()
                    .map(DeviceParamModel::getParamValue)
                    .orElse(BRAND_DEFAULT);
        }
        String brandTag = getTagValue("brand");
        return isNotBlank(brandTag) ? brandTag : BRAND_DEFAULT;
    }

    @JsonIgnore
    public void setBrand(String brand) {
        setTagValue("brand", brand);
        deviceParamModels
                .stream()
                .filter(param -> "brandId".equals(param.getParamName()))
                .findFirst()
                .ifPresent(existingBrand -> existingBrand.setParamValue(brand));
    }

}
