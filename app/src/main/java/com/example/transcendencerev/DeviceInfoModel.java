package com.example.transcendencerev;


public class DeviceInfoModel {

    private final String deviceName;
    private final String deviceHardwareAddress;

    public DeviceInfoModel(String deviceName, String deviceHardwareAddress) {
        this.deviceName = deviceName;
        this.deviceHardwareAddress = deviceHardwareAddress;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public String getDeviceHardwareAddress() {
        return deviceHardwareAddress;
    }

}