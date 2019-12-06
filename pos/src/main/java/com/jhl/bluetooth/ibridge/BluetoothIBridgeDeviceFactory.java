package com.jhl.bluetooth.ibridge;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;

import java.util.ArrayList;
import java.util.List;

final class BluetoothIBridgeDeviceFactory {
    private static BluetoothIBridgeDeviceFactory INSTANCE = null;
    private static byte[] LOCK = new byte[0];
    private List<BluetoothIBridgeDevice> mList = new ArrayList();

    private BluetoothIBridgeDeviceFactory() {
    }

    public static BluetoothIBridgeDeviceFactory getDefaultFactory() {
        synchronized (LOCK) {
            if (INSTANCE == null) {
                INSTANCE = new BluetoothIBridgeDeviceFactory();
            }
        }
        return INSTANCE;
    }

    public BluetoothIBridgeDevice createDevice(BluetoothDevice device, int deviceTye) {
        if (device == null) {
            return null;
        }
        for (BluetoothIBridgeDevice idev : this.mList) {
            if (idev != null && idev.isSameDevice(device, deviceTye)) {
                return idev;
            }
        }
        BluetoothIBridgeDevice newDev = new BluetoothIBridgeDevice(device);
        newDev.setDeviceType(deviceTye);
        this.mList.add(newDev);
        return newDev;
    }

    public BluetoothIBridgeDevice createDevice(String address, int deivceType) {
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter != null) {
            return createDevice(adapter.getRemoteDevice(address), deivceType);
        }
        return null;
    }
}
