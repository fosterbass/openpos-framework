package org.jumpmind.pos.devices.service;

import org.jumpmind.pos.devices.service.model.*;
import org.jumpmind.pos.util.SuppressMethodLogging;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import static org.jumpmind.pos.util.RestApiSupport.REST_API_CONTEXT_PATH;

@Tag(name = "Devices Service")
@RestController("devices")
@RequestMapping(REST_API_CONTEXT_PATH + "/devices")
public interface IDevicesService {

    @RequestMapping("/personalizationConfig")
    PersonalizationConfigResponse getPersonalizationConfig();

    @PostMapping("/personalize")
    @ResponseBody
    PersonalizationResponse personalize(@RequestBody PersonalizationRequest request);

    @SuppressMethodLogging
    @PostMapping("/device")
    @ResponseBody
    GetDeviceResponse getDevice(@RequestBody GetDeviceRequest request);

    @SuppressMethodLogging
    @GetMapping("/myDevice")
    @ResponseBody
    GetDeviceResponse getMyDevice();

    @PostMapping("/authenticate")
    @ResponseBody
    AuthenticateDeviceResponse authenticateDevice(@RequestBody AuthenticateDeviceRequest request);

    @PostMapping("/disconnectDevice")
    void disconnectDevice(@RequestBody DisconnectDeviceRequest request);

    @SuppressMethodLogging
    @PostMapping("/connectedDeviceIds")
    GetConnectedDeviceIdsResponse getConnectedDeviceIds(@RequestBody GetConnectedDeviceIdsRequest request);

    @SuppressMethodLogging
    @PostMapping("/find")
    FindDevicesResponse findDevices(@RequestBody FindDevicesRequest request);

    @PostMapping("/unpaired")
    GetUnpairedDevicesResponse getUnpairedDevices(@RequestBody GetUnpairedDevicesRequest request);

    @PostMapping("/paired")
    GetPairedDevicesResponse getPairedDevices(@RequestBody GetPairedDevicesRequest request);

    @PostMapping("/pair")
    PairDeviceResponse pairDevice(@RequestBody PairDeviceRequest request);

    @PostMapping("/unpair")
    UnpairDeviceResponse unpairDevice(@RequestBody UnpairDeviceRequest request);

    @PostMapping("/setAppId")
    SetAppIdResponse setAppId(@RequestBody SetAppIdRequest request);

    @PutMapping("/setBrand")
    SetBrandResponse setBrand(@RequestBody SetBrandRequest request);
}
