package com.jhl.bluetooth.ibridge;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.os.Build.VERSION;
import android.os.Message;
import android.util.Log;

import com.jhl.bluetooth.ibridge.BluetoothIBridgeAdapter.DataReceiver;
import com.jhl.bluetooth.ibridge.BluetoothIBridgeDevice.BondStatus;
import com.jhl.bluetooth.ibridge.BluetoothIBridgeDevice.ConnectStatus;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

final class BluetoothIBridgeConnManager4Le {
    /* access modifiers changed from: private */
    public int credit = 0;
    /* access modifiers changed from: private */
    public BluetoothManager mBluetoothManager;
    private Context mContext;
    private ArrayList<DataReceiver> mDataReceivers;
    private BluetoothIBridgeAdapter.MyHandler mHandler;
    /* access modifiers changed from: private */
    public ConnectionList mList;
    /* access modifiers changed from: private */
    public int mMtu = 20;

    private class ConnectionList {
        private byte[] LOCK;
        private List<GattConnection> mConnectedDevices;

        private ConnectionList() {
            this.mConnectedDevices = new ArrayList();
            this.LOCK = new byte[0];
        }

        public void write(BluetoothIBridgeDevice device, byte[] buffer, int length) {
            if (device != null && buffer != null && length > 0) {
                GattConnection found = foundDevice(device);
                if (found != null) {
                    found.write(buffer, length);
                }
            }
        }

        public void addConnection(GattConnection connection) {
            GattConnection found = foundDevice(connection.getDevice());
            if (found != null) {
                synchronized (this.LOCK) {
                    this.mConnectedDevices.remove(found);
                }
            }
            synchronized (this.LOCK) {
                this.mConnectedDevices.add(connection);
            }
        }

        /* access modifiers changed from: private */
        public GattConnection foundDevice(BluetoothIBridgeDevice device) {
            GattConnection found = null;
            synchronized (this.LOCK) {
                Iterator i$ = this.mConnectedDevices.iterator();
                while (true) {
                    if (!i$.hasNext()) {
                        break;
                    }
                    GattConnection ds = (GattConnection) i$.next();
                    if (device.equals(ds.getDevice())) {
                        found = ds;
                        break;
                    }
                }
            }
            return found;
        }

        public List<BluetoothIBridgeDevice> getCurrentConnectedDevice() {
            List<BluetoothIBridgeDevice> devicesList = new ArrayList<>();
            synchronized (this.LOCK) {
                for (GattConnection ds : this.mConnectedDevices) {
                    BluetoothIBridgeDevice device = ds.getDevice();
                    if (device != null && !devicesList.contains(device)) {
                        devicesList.add(device);
                    }
                }
            }
            return devicesList;
        }

        public void clear() {
            synchronized (this.LOCK) {
                this.mConnectedDevices.clear();
            }
        }

        public void releaseAllConnections() {
            synchronized (this.LOCK) {
                for (GattConnection ds : this.mConnectedDevices) {
                    if (ds != null) {
                        ds.close();
                    }
                }
            }
            this.mConnectedDevices.clear();
        }
    }

    class GattConnection {
        static final String CLIENT_CHARACTERISTIC_CONFIG = "00002902-0000-1000-8000-00805f9b34fb";
        private final BluetoothAdapter mBluetoothAdapter;
        /* access modifiers changed from: private */
        public BluetoothGatt mBluetoothGatt = null;
        private ArrayList<DataReceiver> mDataReceivers;
        private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
            public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                Log.i("IBridgeGatt", "onConnectionStateChange:" + newState);
                if (gatt.equals(GattConnection.this.mBluetoothGatt) && newState == 2) {
                    BluetoothIBridgeConnManager4Le.this.mList.addConnection(GattConnection.this.mGattConnection);
                    GattConnection.this.discoveryServices();
                    if (GattConnection.this.mHandler != null) {
                        Message msg = GattConnection.this.mHandler.obtainMessage(6);
                        msg.obj = GattConnection.this.mmDevice;
                        GattConnection.this.mmDevice.connected(true);
                        GattConnection.this.mmDevice.setConnectStatus(ConnectStatus.STATUS_CONNECTED);
                        GattConnection.this.mHandler.sendMessage(msg);
                    }
                } else if (gatt.equals(GattConnection.this.mBluetoothGatt) && newState == 0) {
                    Log.i("IBridgeGatt", "BluetoothGattCallback STATE_DISCONNECTED");
                    BluetoothIBridgeConnManager4Le.this.credit = 0;
                    GattConnection.this.mWriteCharacteristic = null;
                    GattConnection.this.mNotifyCharacteristic = null;
                    GattConnection.this.mMTUCharacteristic = null;
                    if (GattConnection.this.mHandler != null) {
                        Message msg2 = GattConnection.this.mHandler.obtainMessage(7);
                        msg2.obj = GattConnection.this.mmDevice;
                        GattConnection.this.mmDevice.connected(false);
                        GattConnection.this.mmDevice.setConnectStatus(ConnectStatus.STATUS_DISCONNECTED);
                        GattConnection.this.mHandler.sendMessage(msg2);
                    }
                    GattConnection.this.close();
                }
            }

            public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                if (!gatt.equals(GattConnection.this.mBluetoothGatt) || status != 0) {
                    Log.i("IBridgeGatt", "onGattServicesDiscoveredFailed");
                } else {
                    GattConnection.this.onServicesFound(GattConnection.this.getSupportedGattServices());
                }
            }

            public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                if (gatt.equals(GattConnection.this.mBluetoothGatt) && status == 0) {
                    GattConnection.this.onDataChanged(characteristic);
                }
            }

            public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
                if (gatt.equals(GattConnection.this.mBluetoothGatt)) {
                    GattConnection.this.onDataChanged(characteristic);
                }
            }

            public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
                if (GattConnection.this.mNotifyCharacteristicUUID.equals(descriptor.getCharacteristic().getUuid().toString()) && GattConnection.this.mMTUCharacteristic != null) {
                    GattConnection.this.setCharacteristicNotification(GattConnection.this.mMTUCharacteristic, true);
                }
            }

            public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
                if (!gatt.equals(GattConnection.this.mBluetoothGatt) || status != 0) {
                    Log.i("IBridgeGatt", "request mtu fail");
                    BluetoothIBridgeConnManager4Le.this.mMtu = 20;
                    return;
                }
                Log.i("IBridgeGatt", "mtu change to " + mtu);
                BluetoothIBridgeConnManager4Le.this.mMtu = mtu;
            }
        };
        /* access modifiers changed from: private */
        public final GattConnection mGattConnection = this;
        String mGattServiceUUID = "0000ff00-0000-1000-8000-00805f9b34fb";
        /* access modifiers changed from: private */
        public final BluetoothIBridgeAdapter.MyHandler mHandler;
        /* access modifiers changed from: private */
        public BluetoothGattCharacteristic mMTUCharacteristic;
        String mMTUCharacteristicUUID = "0000ff03-0000-1000-8000-00805f9b34fb";
        /* access modifiers changed from: private */
        public BluetoothGattCharacteristic mNotifyCharacteristic;
        String mNotifyCharacteristicUUID = "0000ff01-0000-1000-8000-00805f9b34fb";
        public BluetoothGattCharacteristic mWriteCharacteristic;
        String mWriteCharacteristicUUID = "0000ff02-0000-1000-8000-00805f9b34fb";
        public final BluetoothIBridgeDevice mmDevice;

        public GattConnection(Context context, BluetoothIBridgeDevice device, BluetoothIBridgeAdapter.MyHandler handler, ArrayList<DataReceiver> dataReceivers) {
            this.mHandler = handler;
            this.mDataReceivers = dataReceivers;
            this.mBluetoothAdapter = BluetoothIBridgeConnManager4Le.this.mBluetoothManager.getAdapter();
            this.mmDevice = device;
            BluetoothDevice dev = this.mBluetoothAdapter.getRemoteDevice(this.mmDevice.getDeviceAddress());
            if (VERSION.SDK_INT < 21) {
                this.mBluetoothGatt = dev.connectGatt(context, false, this.mGattCallback);
                return;
            }
            try {
                Method m = BluetoothDevice.class.getMethod("connectGatt", new Class[]{Context.class, Boolean.TYPE, BluetoothGattCallback.class, Integer.TYPE});
                if (m != null) {
                    try {
                        this.mBluetoothGatt = (BluetoothGatt) m.invoke(dev, new Object[]{context, Boolean.valueOf(false), this.mGattCallback, Integer.valueOf(2)});
                    } catch (IllegalArgumentException e) {
                        e.printStackTrace();
                    } catch (IllegalAccessException e2) {
                        e2.printStackTrace();
                    } catch (InvocationTargetException e3) {
                        e3.printStackTrace();
                    }
                }
            } catch (NoSuchMethodException e4) {
                e4.printStackTrace();
            }
        }

        public void setTargetUUIDs(String serviceUUID, String notifyCharacteristicUUID, String writeCharacteristicUUID) {
            this.mGattServiceUUID = serviceUUID;
            this.mWriteCharacteristicUUID = writeCharacteristicUUID;
            this.mNotifyCharacteristicUUID = notifyCharacteristicUUID;
            if (serviceUUID == "0000ff00-0000-1000-8000-00805f9b34fb") {
                this.mMTUCharacteristicUUID = "0000ff03-0000-1000-8000-00805f9b34fb";
            }
            for (BluetoothGattService gattService : this.mmDevice.getGattServices()) {
                String serviceUUIDString = gattService.getUuid().toString();
                if (serviceUUIDString != null && serviceUUIDString.equals(this.mGattServiceUUID)) {
                    for (BluetoothGattCharacteristic gattCharacteristic : gattService.getCharacteristics()) {
                        String characteristicUUIDString = gattCharacteristic.getUuid().toString();
                        if (characteristicUUIDString.equals(this.mWriteCharacteristicUUID)) {
                            this.mWriteCharacteristic = gattCharacteristic;
                        }
                        if (characteristicUUIDString.equals(this.mNotifyCharacteristicUUID)) {
                            this.mNotifyCharacteristic = gattCharacteristic;
                            setCharacteristicNotification(this.mNotifyCharacteristic, true);
                        }
                        if (characteristicUUIDString.equals(this.mMTUCharacteristicUUID)) {
                            this.mMTUCharacteristic = gattCharacteristic;
                            BluetoothIBridgeConnManager4Le.this.credit = 0;
                        }
                    }
                    return;
                }
            }
        }

        /* access modifiers changed from: 0000 */
        public void setMtu(int mtu) {
            if (this.mBluetoothGatt != null) {
                this.mBluetoothGatt.requestMtu(mtu);
            }
        }

        /* access modifiers changed from: 0000 */
        public void discoveryServices() {
            if (this.mBluetoothGatt != null) {
                this.mBluetoothGatt.discoverServices();
            }
        }

        /* access modifiers changed from: 0000 */
        public void disconnnect() {
            if (this.mBluetoothGatt != null) {
                this.mBluetoothGatt.disconnect();
            }
        }

        /* access modifiers changed from: 0000 */
        public void close() {
            if (this.mBluetoothGatt != null) {
                this.mBluetoothGatt.close();
                this.mBluetoothGatt = null;
            }
        }

        /* access modifiers changed from: 0000 */
        public void write(byte[] buf, int length) {
            byte[] buffer;
            if (this.mWriteCharacteristic != null) {
                int len = length;
                int off = 0;
                while (len > 0) {
                    if (len >= BluetoothIBridgeConnManager4Le.this.mMtu) {
                        buffer = new byte[BluetoothIBridgeConnManager4Le.this.mMtu];
                        System.arraycopy(buf, off, buffer, 0, BluetoothIBridgeConnManager4Le.this.mMtu);
                    } else {
                        buffer = new byte[len];
                        System.arraycopy(buf, off, buffer, 0, len);
                    }
                    if (this.mMTUCharacteristic == null) {
                        this.mWriteCharacteristic.setValue(buffer);
                        this.mWriteCharacteristic.setWriteType(1);
                        if (writeCharacteristic(this.mWriteCharacteristic)) {
                            off += buffer.length;
                            len -= buffer.length;
                        }
                    } else if (BluetoothIBridgeConnManager4Le.this.credit > 0) {
                        this.mWriteCharacteristic.setValue(buffer);
                        this.mWriteCharacteristic.setWriteType(1);
                        if (writeCharacteristic(this.mWriteCharacteristic)) {
                            off += buffer.length;
                            len -= buffer.length;
                            BluetoothIBridgeConnManager4Le.this.credit = BluetoothIBridgeConnManager4Le.this.credit - 1;
                        }
                    }
                }
            }
        }

        /* access modifiers changed from: 0000 */
        public void setCharacteristicNotification(BluetoothGattCharacteristic characteristic, boolean enabled) {
            if (this.mBluetoothGatt != null) {
                boolean characteristicNotification = this.mBluetoothGatt.setCharacteristicNotification(characteristic, enabled);
                if (this.mNotifyCharacteristicUUID.equals(characteristic.getUuid().toString()) || this.mMTUCharacteristicUUID.equals(characteristic.getUuid().toString())) {
                    characteristic.setWriteType(2);
                    BluetoothGattDescriptor descriptor = characteristic.getDescriptor(UUID.fromString(CLIENT_CHARACTERISTIC_CONFIG));
                    if (descriptor != null) {
                        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                    }
                    this.mBluetoothGatt.writeDescriptor(descriptor);
                }
            }
        }

        /* access modifiers changed from: 0000 */
        public void readCharacteristic(BluetoothGattCharacteristic characteristic) {
            if (this.mBluetoothGatt != null) {
                this.mBluetoothGatt.readCharacteristic(characteristic);
            }
        }

        /* access modifiers changed from: 0000 */
        public boolean writeCharacteristic(BluetoothGattCharacteristic characteristic) {
            if (this.mBluetoothGatt != null) {
                return this.mBluetoothGatt.writeCharacteristic(characteristic);
            }
            return false;
        }

        /* access modifiers changed from: 0000 */
        public List<BluetoothGattService> getSupportedGattServices() {
            if (this.mBluetoothGatt == null) {
                return null;
            }
            return this.mBluetoothGatt.getServices();
        }

        /* access modifiers changed from: 0000 */
        public void onServicesFound(List<BluetoothGattService> gattServices) {
            Message msg = this.mHandler.obtainMessage(12);
            this.mmDevice.setGattServices(gattServices);
            msg.obj = this.mmDevice;
            this.mHandler.sendMessage(msg);
            for (BluetoothGattService gattService : gattServices) {
                String serviceUUIDString = gattService.getUuid().toString();
                if (serviceUUIDString != null && serviceUUIDString.equals(this.mGattServiceUUID)) {
                    for (BluetoothGattCharacteristic gattCharacteristic : gattService.getCharacteristics()) {
                        String characteristicUUIDString = gattCharacteristic.getUuid().toString();
                        if (characteristicUUIDString.equals(this.mWriteCharacteristicUUID)) {
                            this.mWriteCharacteristic = gattCharacteristic;
                        }
                        if (characteristicUUIDString.equals(this.mNotifyCharacteristicUUID)) {
                            this.mNotifyCharacteristic = gattCharacteristic;
                            setCharacteristicNotification(this.mNotifyCharacteristic, true);
                        }
                        if (characteristicUUIDString.equals(this.mMTUCharacteristicUUID)) {
                            this.mMTUCharacteristic = gattCharacteristic;
                            BluetoothIBridgeConnManager4Le.this.credit = 0;
                        }
                    }
                    return;
                }
            }
        }

        /* access modifiers changed from: 0000 */
        public void onDataChanged(BluetoothGattCharacteristic characteristic) {
            if (this.mNotifyCharacteristicUUID.equals(characteristic.getUuid().toString())) {
                byte[] data = characteristic.getValue();
                if (data != null && data.length > 0) {
                    this.mmDevice.buffer = data;
                    this.mmDevice.length = data.length;
                    if (this.mDataReceivers != null) {
                        ArrayList<DataReceiver> listenersCopy = (ArrayList) this.mDataReceivers.clone();
                        int numListeners = listenersCopy.size();
                        for (int i = 0; i < numListeners; i++) {
                            DataReceiver er = (DataReceiver) listenersCopy.get(i);
                            if (this.mmDevice.isValidDevice()) {
                                er.onDataReceived(this.mmDevice, this.mmDevice.buffer, this.mmDevice.length);
                            }
                        }
                    }
                }
            }
            if (this.mMTUCharacteristicUUID.equals(characteristic.getUuid().toString())) {
                byte[] data2 = characteristic.getValue();
                byte d = data2[0];
                if (d == 1) {
                    BluetoothIBridgeConnManager4Le.access$312(BluetoothIBridgeConnManager4Le.this, data2[1]);
                } else if (d == 2) {
                    int i2 = data2[1] + (data2[2] << 8);
                }
            }
        }

        public boolean equals(Object o) {
            if (o != null && (o instanceof GattConnection)) {
                return ((GattConnection) o).mmDevice.equals(this.mmDevice);
            }
            return false;
        }

        /* access modifiers changed from: 0000 */
        public BluetoothIBridgeDevice getDevice() {
            return this.mmDevice;
        }
    }

    static /* synthetic */ int access$312(BluetoothIBridgeConnManager4Le x0, int x1) {
        int i = x0.credit + x1;
        x0.credit = i;
        return i;
    }

    public BluetoothIBridgeConnManager4Le(Context context, BluetoothIBridgeAdapter.MyHandler handler) {
        this.mContext = context;
        this.mHandler = handler;
        this.mList = new ConnectionList();
        this.mList.clear();
        if (this.mBluetoothManager == null) {
            this.mBluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
            if (this.mBluetoothManager == null) {
                Log.e("IBridgeGatt", "no bluetooth manager");
            }
        }
    }

    /* access modifiers changed from: 0000 */
    public void registerDataReceiver(DataReceiver receiver) {
        if (this.mDataReceivers == null) {
            this.mDataReceivers = new ArrayList<>();
        }
        if (!this.mDataReceivers.contains(receiver)) {
            this.mDataReceivers.add(receiver);
        }
    }

    /* access modifiers changed from: 0000 */
    public void unregisterDataReceiver(DataReceiver receiver) {
        if (this.mDataReceivers != null) {
            this.mDataReceivers.remove(receiver);
        }
    }

    /* access modifiers changed from: 0000 */
    public void destory() {
        this.mList.releaseAllConnections();
        this.mHandler = null;
        this.mDataReceivers = null;
    }

    /* access modifiers changed from: 0000 */
    public void connect(BluetoothIBridgeDevice device) {
        if (device == null || device.isConnected()) {
            Log.e("ConnManager", "device is connected or is null");
            return;
        }
        device.setBondStatus(BondStatus.STATE_BONDED);
        device.setConnectStatus(ConnectStatus.STATUS_CONNECTTING);
        new GattConnection(this.mContext, device, this.mHandler, this.mDataReceivers);
    }

    /* access modifiers changed from: 0000 */
    public void disconnect(BluetoothIBridgeDevice device) {
        GattConnection found = this.mList.foundDevice(device);
        Log.i("IBridgeGatt", "try to release gatt connection:" + found);
        if (found == null) {
            Log.e("IBridgeGatt", "The gatt device[" + device + "] may has been closed.");
        } else {
            found.disconnnect();
        }
    }

    /* access modifiers changed from: 0000 */
    public void write(BluetoothIBridgeDevice device, byte[] buffer, int length) {
        this.mList.write(device, buffer, length);
    }

    /* access modifiers changed from: 0000 */
    public void disconnectAll() {
        this.mList.releaseAllConnections();
    }

    public List<BluetoothIBridgeDevice> getCurrentConnectedDevice() {
        return this.mList.getCurrentConnectedDevice();
    }

    public void setTargetUUIDs(BluetoothIBridgeDevice device, String serviceUUID, String notifyCharacteristicUUID, String writeCharacteristicUUID) {
        GattConnection found = this.mList.foundDevice(device);
        if (found != null) {
            found.setTargetUUIDs(serviceUUID, notifyCharacteristicUUID, writeCharacteristicUUID);
        }
    }

    public void setMtu(BluetoothIBridgeDevice device, int mtu) {
        GattConnection found = this.mList.foundDevice(device);
        if (found != null) {
            found.setMtu(mtu);
        }
    }
}
