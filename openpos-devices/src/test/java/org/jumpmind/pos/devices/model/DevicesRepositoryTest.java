package org.jumpmind.pos.devices.model;

import org.apache.commons.collections4.CollectionUtils;
import org.jumpmind.pos.devices.DeviceNotFoundException;
import org.jumpmind.pos.devices.TestDevicesConfig;
import org.jumpmind.pos.persist.DBSession;
import org.jumpmind.pos.persist.ModelId;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles({"test", "test-business-unit-provider"})
@ContextConfiguration(classes = {TestDevicesConfig.class})
public class DevicesRepositoryTest {

    @InjectMocks
    DevicesRepository devicesRepository;

    @Mock
    DBSession devSession;

    @Mock
    VirtualDeviceRepository virtualDeviceRepository;

    String mockDeviceId = "00100-001";
    String mockPairedDeviceId = "00100-002";
    String mockBusinessUnitId = "00100";
    String mockAppId = "pos";
    String mockSecondaryAppId = "customerdisplay";
    String mockAuthToken = "830311d5-9550-4eaf-94ff-c661ed761067";

    @Test
    public void testFindDevice() {
        when(devSession.findByNaturalId(any(), (ModelId) any())).thenReturn(getMockDevice());
        DeviceModel device = devicesRepository.getDevice(mockDeviceId);
        assertEquals(mockDeviceId, device.getDeviceId());
        assertEquals("Store 100 Register 1", device.getDescription());
        assertEquals("N_AMERICA", device.getTagValue("REGION"));
        assertEquals("US", device.getTagValue("COUNTRY"));
        assertEquals("OH", device.getTagValue("STATE"));
        assertEquals("100", device.getTagValue("STORE_NUMBER"));
        assertEquals("REGULAR", device.getTagValue("STORE_TYPE"));
        assertEquals("WORKSTATION", device.getTagValue("APP_PROFILE"));
        assertEquals("Metl", device.getTagValue("PRICE_ZONE"));
    }

    @Test
    public void getDeviceCallsVirtualRepository() {
        String deviceId = "12345-678";
        when(virtualDeviceRepository.getByDeviceId(deviceId)).thenReturn(getMockDevice(deviceId));
        DeviceModel device = devicesRepository.getDevice(deviceId);
        assertEquals(deviceId, device.getDeviceId());
        verify(virtualDeviceRepository).getByDeviceId(deviceId);
    }

    @Test(expected = DeviceNotFoundException.class)
    public void getDeviceThrowsDeviceNotFoundException() {
        devicesRepository.getDevice("");
    }

    @Test
    public void testFindDevices() {
        Map<String, Object> params = new HashMap<>();
        params.put("businessUnitId", mockBusinessUnitId);
        when(devSession.findByFields(DeviceModel.class, params, 1000)).thenReturn(Collections.singletonList(getMockDevice()));
        List<DeviceModel> devices = devicesRepository.findDevices(mockBusinessUnitId);
        assertEquals(mockDeviceId, devices.get(0).getDeviceId());
    }

    @Test
    public void testGetUnpairedDevices() {
        when(devSession.findByFields(any(), anyMap(), anyInt())).thenReturn(Collections.singletonList(getMockDevice()));
        List<DeviceModel> devices = devicesRepository.getOrphanedDevices(mockBusinessUnitId);
        assertEquals(mockDeviceId, devices.get(0).getDeviceId());
    }

    @Test
    public void testGetUnpairedDevicesNoResults() {
        DeviceModel mockDevice = getMockDevice(mockDeviceId, mockPairedDeviceId);
        when(devSession.findByFields(any(), anyMap(), anyInt())).thenReturn(Collections.singletonList(mockDevice));
        List<DeviceModel> devices = devicesRepository.getOrphanedDevices(mockBusinessUnitId);
        assertTrue(CollectionUtils.isEmpty(devices));
    }

    @Test
    public void testGetUnpairedDevicesByAppId() {
        when(devSession.findByFields(any(), anyMap(), anyInt())).thenReturn(Collections.singletonList(getMockDevice()));
        List<DeviceModel> devices = devicesRepository.getOrphanedDevicesByAppId(mockBusinessUnitId, mockAppId);
        assertEquals(mockDeviceId, devices.get(0).getDeviceId());
        assertEquals(mockAppId, devices.get(0).getAppId());
    }

    @Test
    public void testGetUnpairedDevicesByAppIdNoResults() {
        DeviceModel mockDevice = getMockDevice(mockDeviceId, null, mockSecondaryAppId);
        when(devSession.findByFields(any(), anyMap(), anyInt())).thenReturn(Collections.singletonList(mockDevice));
        List<DeviceModel> devices = devicesRepository.getOrphanedDevicesByAppId(mockBusinessUnitId, mockAppId);
        assertTrue(CollectionUtils.isEmpty(devices));
    }

    @Test
    public void testGetPairedDevices() {
        DeviceModel mockDevice = getMockDevice(mockDeviceId, mockPairedDeviceId);
        when(devSession.findByFields(any(), anyMap(), anyInt())).thenReturn(Collections.singletonList(mockDevice));
        List<DeviceModel> devices = devicesRepository.getAllChildDevices(mockBusinessUnitId);
        assertEquals(mockDeviceId, devices.get(0).getDeviceId());
        assertEquals(mockPairedDeviceId, devices.get(0).getParentDeviceId());
    }

    @Test
    public void testGetPairedDevicesNoResults() {
        when(devSession.findByFields(any(), anyMap(), anyInt())).thenReturn(Collections.singletonList(getMockDevice()));
        List<DeviceModel> devices = devicesRepository.getAllChildDevices(mockBusinessUnitId);
        assertTrue(CollectionUtils.isEmpty(devices));
    }

    @Test
    public void testGetPairedDevicesByAppId() {
        DeviceModel mockDevice = getMockDevice(mockDeviceId, mockPairedDeviceId);
        when(devSession.findByFields(any(), anyMap(), anyInt())).thenReturn(Collections.singletonList(mockDevice));
        List<DeviceModel> devices = devicesRepository.getAllChildDevicesByAppId(mockBusinessUnitId, mockAppId);
        assertEquals(mockDeviceId, devices.get(0).getDeviceId());
        assertEquals(mockAppId, devices.get(0).getAppId());
    }

    @Test
    public void testGetPairedDevicesByAppIdNoResults() {
        DeviceModel mockDevice = getMockDevice(mockDeviceId, mockPairedDeviceId, mockSecondaryAppId);
        when(devSession.findByFields(any(), anyMap(), anyInt())).thenReturn(Collections.singletonList(mockDevice));
        List<DeviceModel> devices = devicesRepository.getAllChildDevicesByAppId(mockBusinessUnitId, mockAppId);
        assertTrue(CollectionUtils.isEmpty(devices));
    }

    @Test
    public void testPairDevice() {
        when(devSession.findByNaturalId(any(), (ModelId) any()))
                .thenReturn(getMockDevice())
                .thenReturn(getMockDevice(mockPairedDeviceId));
        devicesRepository.pairDevice(mockDeviceId, mockPairedDeviceId);
        verify(devSession).save(getMockDevice(mockPairedDeviceId, mockDeviceId));
    }

    @Test
    public void testPairDeviceUnpairFirst() {
        String device3Id = "00100-003";

        when(devSession.findByNaturalId(any(), (ModelId) any()))
                .thenReturn(getMockDevice(mockDeviceId))
                .thenReturn(getMockDevice(mockPairedDeviceId, device3Id));

        devicesRepository.pairDevice(mockDeviceId, mockPairedDeviceId);

        verify(devSession).save(getMockDevice(mockPairedDeviceId, mockDeviceId));
    }

    @Test
    public void testUnpairDevice() {
        when(devSession.findByNaturalId(any(), (ModelId) any()))
                .thenReturn(getMockDevice(mockPairedDeviceId, mockDeviceId));

        devicesRepository.unpairDevice(mockPairedDeviceId);

        verify(devSession).save(getMockDevice(mockPairedDeviceId));
    }

    @Test
    public void testSetAppId() {
        when(devSession.findByNaturalId(any(), (ModelId) any())).thenReturn(getMockDeviceAuth());
        when(devSession.findFirstByFields(any(), anyMap(), anyInt())).thenReturn(getMockDeviceAuth()).thenReturn(getMockDevice());
        when(devSession.findByFields(any(), anyMap(), anyInt())).thenReturn(Collections.singletonList(getDeviceParams(mockDeviceId)));
        DeviceModel expectedDevice = getMockDevice(mockDeviceId, null, mockSecondaryAppId);
        devicesRepository.setAppId(mockDeviceId, mockSecondaryAppId);
        verify(devSession).save(expectedDevice);
        verify(devSession).save(getMockDeviceAuth());
    }

    @Test
    public void testGetDeviceAuth() {
        when(devSession.findByNaturalId(any(), (ModelId) any())).thenReturn(getMockDeviceAuth());
        assertEquals(mockAuthToken, devicesRepository.getDeviceAuth(mockDeviceId));
    }

    @Test(expected = DeviceNotFoundException.class)
    public void testGetDeviceAuthNotFound() {
        when(devSession.findByNaturalId(any(), (ModelId) any())).thenReturn(null);
        devicesRepository.getDeviceAuth(mockDeviceId);
    }

    @Test
    public void testGetDeviceAuthModel() {
        DeviceAuthModel deviceAuth = getMockDeviceAuth();
        when(devSession.findByNaturalId(any(), (ModelId) any())).thenReturn(deviceAuth);
        DeviceAuthModel actualDeviceAuth = devicesRepository.getDeviceAuthModel(mockDeviceId);
        assertEquals(deviceAuth, actualDeviceAuth);
    }

    @Test
    public void testGetDisconnectedDevices() {
        DeviceStatusModel connectedDevice = getConnectedDevice(mockDeviceId);
        when(devSession.query(any(), anyMap(), anyInt())).thenReturn(Collections.singletonList(connectedDevice));
        when(devSession.findByFields(any(), anyMap(), anyInt())).thenReturn(Arrays.asList(getMockDevice(), getMockDevice(mockPairedDeviceId)));
        when(devSession.findAll(DeviceAuthModel.class, 10000)).thenReturn(Arrays.asList(getMockDeviceAuth(), getMockDeviceAuth(mockPairedDeviceId)));
        List<DeviceAuthModel> disconnectedDevices = devicesRepository.getDisconnectedDevices(mockBusinessUnitId, mockDeviceId);
        assertEquals(1, disconnectedDevices.size());
        assertEquals(mockPairedDeviceId, disconnectedDevices.get(0).getDeviceId());
    }

    @Test
    public void testGetConnectedDeviceIds() {
        DeviceStatusModel deviceStatus = getConnectedDevice(mockDeviceId);
        when(devSession.query(any(), anyMap(), anyInt())).thenReturn(Arrays.asList(deviceStatus, deviceStatus, deviceStatus));
        Set<String> connectedDeviceIds = devicesRepository.getConnectedDeviceIds(mockBusinessUnitId, mockPairedDeviceId);
        assertEquals(1, connectedDeviceIds.size());
        assertTrue(connectedDeviceIds.contains(mockDeviceId));
    }

    @Test
    public void testGetDeviceByAuth() {
        Map<String, Object> deviceAuthParams = new HashMap<>();
        deviceAuthParams.put("authToken", mockAuthToken);
        when(devSession.findFirstByFields(DeviceAuthModel.class, deviceAuthParams, 1)).thenReturn(getMockDeviceAuth());
        Map<String, Object> deviceParams = new HashMap<>();
        deviceParams.put("deviceId", mockDeviceId);
        when(devSession.findFirstByFields(DeviceModel.class, deviceParams, 1)).thenReturn(getMockDevice());
        when(devSession.findByFields(any(), anyMap(), anyInt())).thenReturn(Collections.singletonList(getDeviceParams(mockDeviceId)));

        assertEquals(getMockDevice(), devicesRepository.getDeviceByAuth(mockAuthToken));
    }

    @Test(expected = DeviceNotFoundException.class)
    public void testGetDeviceByAuthNotFound() {
        when(devSession.findFirstByFields(any(), anyMap(), anyInt())).thenReturn(null);
        devicesRepository.getDeviceByAuth(mockAuthToken);
    }

    @Test(expected = DeviceNotFoundException.class)
    public void testGetDeviceByAuthDeviceNotFound() {
        when(devSession.findFirstByFields(any(), anyMap(), anyInt())).thenReturn(getMockDeviceAuth()).thenReturn(null);
        devicesRepository.getDeviceByAuth(mockAuthToken);
    }

    @Test
    public void testSaveDevice() {
        DeviceModel mockDevice = getMockDevice();
        DeviceParamModel deviceParam = DeviceParamModel.builder().build();
        mockDevice.setDeviceParamModels(Arrays.asList(deviceParam, deviceParam));
        devicesRepository.saveDevice(mockDevice);
        verify(devSession).save(mockDevice);
        verify(devSession, times(2)).save(DeviceParamModel.builder().deviceId(mockDeviceId).appId(mockAppId).build());
    }

    @Test
    public void testSaveDeviceNoParams() {
        DeviceModel mockDevice = getMockDevice();
        mockDevice.setDeviceParamModels(null);
        devicesRepository.saveDevice(mockDevice);
        verify(devSession).save(mockDevice);
        verify(devSession, never()).save(getDeviceParams(mockDeviceId));
    }

    @Test
    public void testSaveDeviceAuth() {
        devicesRepository.saveDeviceAuth(mockDeviceId, mockAuthToken);
        verify(devSession).save(DeviceAuthModel.builder().deviceId(mockDeviceId).authToken(mockAuthToken).build());
    }

    @Test
    public void testFindDevicePersonalizationModel() {
        DevicePersonalizationModel devicePersonalization = DevicePersonalizationModel.builder()
                .deviceId(mockDeviceId)
                .deviceName(mockPairedDeviceId)
                .deviceParamModels(Collections.singletonList(getDeviceParams(mockDeviceId)))
                .build();
        when(devSession.findByNaturalId(any(), (ModelId) any())).thenReturn(devicePersonalization);
        when(devSession.findByFields(any(), anyMap(), anyInt())).thenReturn(Collections.singletonList(getDeviceParams(mockDeviceId)));
        assertEquals(devicePersonalization, devicesRepository.findDevicePersonalizationModel(mockPairedDeviceId));
    }

    @Test
    public void testFindDevicePersonalizationModelResetParams() {
        DevicePersonalizationModel devicePersonalization = DevicePersonalizationModel.builder()
                .deviceId(mockDeviceId)
                .deviceName(mockPairedDeviceId)
                .deviceParamModels(Collections.singletonList(getDeviceParams(mockDeviceId)))
                .build();
        when(devSession.findByNaturalId(any(), (ModelId) any())).thenReturn(devicePersonalization);
        when(devSession.findByFields(any(), anyMap(), anyInt())).thenReturn(null);
        DevicePersonalizationModel actualDevicePersonalization = devicesRepository.findDevicePersonalizationModel(mockPairedDeviceId);
        assertTrue(CollectionUtils.isEmpty(actualDevicePersonalization.getDeviceParamModels()));
    }

    @Test
    public void testUpdateDeviceStatus() {
        when(devSession.findByNaturalId(any(), (ModelId) any())).thenReturn(getConnectedDevice(mockDeviceId));
        devicesRepository.updateDeviceStatus(mockDeviceId, DeviceStatusConstants.DISCONNECTED);
        verify(devSession).save(getDisconnectedDevice(mockDeviceId));
    }

    @Test
    public void testUpdateDeviceStatusNotFound() {
        when(devSession.findByNaturalId(any(), (ModelId) any())).thenReturn(null);
        devicesRepository.updateDeviceStatus(mockDeviceId, DeviceStatusConstants.DISCONNECTED);
        verify(devSession).save(getDisconnectedDevice(mockDeviceId));
    }

    @Test
    public void testUpdateDeviceStatusDifferentDeviceId() {
        when(devSession.findByNaturalId(any(), (ModelId) any())).thenReturn(getConnectedDevice(mockPairedDeviceId));
        devicesRepository.updateDeviceStatus(mockDeviceId, DeviceStatusConstants.DISCONNECTED);
        verify(devSession).save(getDisconnectedDevice(mockDeviceId));
    }

    private DeviceModel getMockDevice() {
        return getMockDevice(mockDeviceId, null, mockAppId);
    }

    private DeviceModel getMockDevice(String deviceId) {
        return getMockDevice(deviceId, null);
    }

    private DeviceModel getMockDevice(String deviceId, String parentDeviceId) {
        return getMockDevice(deviceId, parentDeviceId, mockAppId);
    }

    private DeviceModel getMockDevice(String deviceId, String parentDeviceId, String appId) {
        DeviceModel mockDevice = DeviceModel.builder()
                .deviceId(deviceId)
                .businessUnitId(mockBusinessUnitId)
                .appId(appId)
                .parentDeviceId(parentDeviceId)
                .description("Store 100 Register 1")
                .build();
        mockDevice.setTagValue("REGION", "N_AMERICA");
        mockDevice.setTagValue("COUNTRY", "US");
        mockDevice.setTagValue("STATE", "OH");
        mockDevice.setTagValue("STORE_NUMBER", "100");
        mockDevice.setTagValue("STORE_TYPE", "REGULAR");
        mockDevice.setTagValue("APP_PROFILE", "WORKSTATION");
        mockDevice.setTagValue("PRICE_ZONE", "Metl");
        return mockDevice;
    }

    private DeviceAuthModel getMockDeviceAuth() {
        return getMockDeviceAuth(mockDeviceId);
    }

    private DeviceAuthModel getMockDeviceAuth(String deviceId) {
        return DeviceAuthModel.builder().deviceId(deviceId).authToken(mockAuthToken).build();
    }

    private DeviceStatusModel getConnectedDevice(String deviceId) {
        return DeviceStatusModel.builder().deviceId(deviceId).deviceStatus(DeviceStatusConstants.CONNECTED).build();
    }

    private DeviceStatusModel getDisconnectedDevice(String deviceId) {
        return DeviceStatusModel.builder().deviceId(deviceId).deviceStatus(DeviceStatusConstants.DISCONNECTED).build();
    }

    private DeviceParamModel getDeviceParams(String deviceId) {
        return DeviceParamModel.builder().deviceId(deviceId).build();
    }
}
