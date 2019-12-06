package com.jhl.bluetooth.ibridge;

import android.bluetooth.BluetoothSocket;
import android.os.Message;

import com.jhl.bluetooth.ibridge.BluetoothIBridgeAdapter.DataReceiver;
import com.jhl.bluetooth.ibridge.BluetoothIBridgeDevice.ConnectStatus;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

final class BluetoothIBridgeConnections {
    private ArrayList<DataReceiver> mDataReceivers;
    private BluetoothIBridgeAdapter.MyHandler mHandler;
    private ConnectionList mList = new ConnectionList();

    private class ConnectionList {
        private byte[] LOCK;
        private List<BluetoothIBridgeConnectionThread> mConnectedDevices;

        private ConnectionList() {
            this.mConnectedDevices = new ArrayList();
            this.LOCK = new byte[0];
        }

        public void write(BluetoothIBridgeDevice device, byte[] buffer, int length) {
            if (device != null && buffer != null && length > 0) {
                BluetoothIBridgeConnectionThread found = foundDevice(device);
                if (found != null) {
                    found.write(buffer, length);
                }
            }
        }

        public void addConnection(BluetoothIBridgeConnectionThread connection) {
            BluetoothIBridgeConnectionThread found = foundDevice(connection.getDevice());
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
        public BluetoothIBridgeConnectionThread foundDevice(BluetoothIBridgeDevice device) {
            BluetoothIBridgeConnectionThread found = null;
            synchronized (this.LOCK) {
                Iterator i$ = this.mConnectedDevices.iterator();
                while (true) {
                    if (!i$.hasNext()) {
                        break;
                    }
                    BluetoothIBridgeConnectionThread ds = (BluetoothIBridgeConnectionThread) i$.next();
                    if (device.equals(ds.getDevice())) {
                        found = ds;
                        break;
                    }
                }
            }
            return found;
        }

        /* access modifiers changed from: 0000 */
        public void clear() {
            synchronized (this.LOCK) {
                this.mConnectedDevices.clear();
            }
        }

        /* access modifiers changed from: 0000 */
        public List<BluetoothIBridgeDevice> getCurrentConnectedDevice() {
            List<BluetoothIBridgeDevice> devicesList = new ArrayList<>();
            synchronized (this.LOCK) {
                for (BluetoothIBridgeConnectionThread ds : this.mConnectedDevices) {
                    BluetoothIBridgeDevice device = ds.getDevice();
                    if (device != null && !devicesList.contains(device)) {
                        devicesList.add(device);
                    }
                }
            }
            return devicesList;
        }

        /* access modifiers changed from: 0000 */
        public void releaseAllConnections() {
            synchronized (this.LOCK) {
                for (BluetoothIBridgeConnectionThread ds : this.mConnectedDevices) {
                    if (ds != null) {
                        ds.cancel();
                    }
                }
            }
            this.mConnectedDevices.clear();
        }
    }

    protected BluetoothIBridgeConnections(BluetoothIBridgeAdapter.MyHandler handler) {
        this.mHandler = handler;
        this.mList.clear();
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
    public void disconnect(BluetoothIBridgeDevice device) {
        BluetoothIBridgeConnectionThread found = this.mList.foundDevice(device);
        if (found != null) {
            if (device != null) {
                device.setConnectStatus(ConnectStatus.STATUS_DISCONNECTTING);
            }
            found.cancel();
        }
    }

    /* access modifiers changed from: 0000 */
    public void write(BluetoothIBridgeDevice device, byte[] buffer, int length) {
        this.mList.write(device, buffer, length);
    }

    /* access modifiers changed from: 0000 */
    public List<BluetoothIBridgeDevice> getCurrentConnectedDevice() {
        return this.mList.getCurrentConnectedDevice();
    }

    /* access modifiers changed from: 0000 */
    public void connected(BluetoothSocket socket, BluetoothIBridgeDevice device) {
        BluetoothIBridgeConnectionThread conn = new BluetoothIBridgeConnectionThread(socket, device, this.mHandler,
                this.mDataReceivers);
        conn.start();
        this.mList.addConnection(conn);
        if (device != null) {
            device.connected(true);
            device.setConnectStatus(ConnectStatus.STATUS_CONNECTED);
        }
        Message msg = this.mHandler.obtainMessage(6);
        msg.obj = device;
        this.mHandler.sendMessage(msg);
    }

    /* access modifiers changed from: 0000 */
    public void disconnectAll() {
        this.mList.releaseAllConnections();
    }
}
