package com.jhl.bluetooth.ibridge.Ancs;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattServerCallback;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.os.Build.VERSION;
import android.util.Log;

import java.util.UUID;

public class GattAncsServer {
    /* access modifiers changed from: private */
    public BluetoothGattCharacteristic ancsControlPoint = null;
    private BluetoothGattCharacteristic ancsDataSource = null;
    /* access modifiers changed from: private */
    public BluetoothGattServer ancsGattServer = null;
    private BluetoothGattService ancsGattService = null;
    private BluetoothGattCharacteristic ancsNotificationSource = null;
    /* access modifiers changed from: private */
    public BluetoothDevice connectedDeviceForAncs = null;
    /* access modifiers changed from: private */
    public GattAncsServerCallback gattAncsServerCallback = null;
    private BluetoothGattServerCallback mBluetoothGattServerCallback = null;
    /* access modifiers changed from: private */
    public int mMtu = 20;

    public interface GattAncsServerCallback {
        void onControlPointDataIn(byte[] bArr);
    }

    public void registerService(Context context) {
        if (VERSION.SDK_INT >= 18 && this.mBluetoothGattServerCallback == null) {
            this.mBluetoothGattServerCallback = new BluetoothGattServerCallback() {
                public void onConnectionStateChange(BluetoothDevice device, int status, int newState) {
                    Log.i("GATT Service", "onConnectionStateChange");
                    if (newState == 2) {
                        GattAncsServer.this.connectedDeviceForAncs = device;
                    } else if (newState == 0) {
                        GattAncsServer.this.connectedDeviceForAncs = null;
                    }
                }

                public void onServiceAdded(int status, BluetoothGattService service) {
                    Log.i("GATT Service", "onServiceAdded");
                }

                public void onCharacteristicReadRequest(BluetoothDevice device, int requestId, int offset, BluetoothGattCharacteristic characteristic) {
                    Log.i("GATT Service", "onCharacteristicReadRequest");
                }

                public void onCharacteristicWriteRequest(BluetoothDevice device, int requestId, BluetoothGattCharacteristic characteristic, boolean preparedWrite, boolean responseNeeded, int offset, byte[] value) {
                    Log.i("GATT Service", "onCharacteristicWriteRequest,requestId=" + requestId);
                    Log.i("GATT Service", "value:" + AncsUtils.getPacketString(value));
                    GattAncsServer.this.ancsGattServer.sendResponse(device, requestId, 0, offset, value);
                    if (GattAncsServer.this.gattAncsServerCallback != null && characteristic == GattAncsServer.this.ancsControlPoint) {
                        GattAncsServer.this.gattAncsServerCallback.onControlPointDataIn(value);
                    }
                }

                public void onDescriptorReadRequest(BluetoothDevice device, int requestId, int offset, BluetoothGattDescriptor descriptor) {
                    Log.i("GATT Service", "onDescriptorReadRequest");
                }

                public void onDescriptorWriteRequest(BluetoothDevice device, int requestId, BluetoothGattDescriptor descriptor, boolean preparedWrite, boolean responseNeeded, int offset, byte[] value) {
                    Log.i("GATT Service", "onDescriptorWriteRequest");
                }

                public void onExecuteWrite(BluetoothDevice device, int requestId, boolean execute) {
                    Log.i("GATT Service", "onExecuteWrite");
                    GattAncsServer.this.ancsGattServer.sendResponse(device, requestId, 0, 0, null);
                }

                public void onNotificationSent(BluetoothDevice device, int status) {
                    Log.i("GATT Service", "onNotificationSent");
                }

                public void onMtuChanged(BluetoothDevice device, int mtu) {
                    Log.i("GATT Service", "onMtuChanged");
                    GattAncsServer.this.mMtu = mtu;
                }
            };
            this.ancsGattServer = ((BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE)).openGattServer(context, this.mBluetoothGattServerCallback);
            if (this.ancsGattServer != null) {
                this.ancsGattService = new BluetoothGattService(UUID.fromString(AncsUtils.GATT_ANCS_SERVICE), 0);
                this.ancsNotificationSource = new BluetoothGattCharacteristic(UUID.fromString(AncsUtils.GATT_ANCS_NOTIFICATION_SOURCE), 16, 1);
                this.ancsDataSource = new BluetoothGattCharacteristic(UUID.fromString(AncsUtils.GATT_ANCS_DATA_SOURCE), 16, 1);
                this.ancsControlPoint = new BluetoothGattCharacteristic(UUID.fromString(AncsUtils.GATT_ANCS_CONTROL_POINT), 8, 16);
                this.ancsGattService.addCharacteristic(this.ancsNotificationSource);
                this.ancsGattService.addCharacteristic(this.ancsDataSource);
                this.ancsGattService.addCharacteristic(this.ancsControlPoint);
                this.ancsGattServer.addService(this.ancsGattService);
            }
        }
    }

    public void unregisterService() {
        if (this.ancsGattServer != null) {
            this.ancsGattServer.removeService(this.ancsGattService);
        }
    }

    public void registerCallback(GattAncsServerCallback gattAncsServerCallback2) {
        this.gattAncsServerCallback = gattAncsServerCallback2;
    }

    public void notifyAncsNotificationSource(byte[] packet) {
        Log.i("DeviceView", "notifyAncsNotificationSource packet:" + AncsUtils.getPacketString(packet));
        if (this.connectedDeviceForAncs != null) {
            this.ancsNotificationSource.setValue(packet);
            this.ancsGattServer.notifyCharacteristicChanged(this.connectedDeviceForAncs, this.ancsNotificationSource, false);
        }
    }

    public void notifyAncsDataSoure(byte[] packet) {
        Log.i("DeviceView", "notifyAncsDataSoure packet:" + AncsUtils.getPacketString(packet));
        if (this.connectedDeviceForAncs != null) {
            int i = 0;
            byte[] subPacket = new byte[this.mMtu];
            while (i < packet.length / this.mMtu) {
                System.arraycopy(packet, this.mMtu * i, subPacket, 0, this.mMtu);
                this.ancsDataSource.setValue(subPacket);
                this.ancsGattServer.notifyCharacteristicChanged(this.connectedDeviceForAncs, this.ancsDataSource, false);
                i++;
            }
            byte[] leftPacket = new byte[(packet.length % this.mMtu)];
            System.arraycopy(packet, this.mMtu * i, leftPacket, 0, packet.length % this.mMtu);
            this.ancsDataSource.setValue(leftPacket);
            this.ancsGattServer.notifyCharacteristicChanged(this.connectedDeviceForAncs, this.ancsDataSource, false);
        }
    }
}
