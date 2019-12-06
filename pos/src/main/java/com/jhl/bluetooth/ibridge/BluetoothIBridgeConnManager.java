package com.jhl.bluetooth.ibridge;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;

import com.jhl.bluetooth.ibridge.BluetoothIBridgeAdapter.DataReceiver;
import com.jhl.bluetooth.ibridge.BluetoothIBridgeDevice.BondStatus;
import com.jhl.bluetooth.ibridge.BluetoothIBridgeDevice.ConnectStatus;
import com.jhl.bluetooth.ibridge.Tools.SystemUtils;

import java.io.IOException;
import java.util.List;

final class BluetoothIBridgeConnManager implements BluetoothIBridgeConnectionListener.ConnectionReceiver {
    private boolean auth = true;
    /* access modifiers changed from: private */
    public boolean autoPair = true;
    String lastExceptionMsg = null;
    /* access modifiers changed from: private */
    public final BluetoothAdapter mAdapter = BluetoothAdapter.getDefaultAdapter();
    /* access modifiers changed from: private */
    public BluetoothIBridgeConnections mBluetoothIBridgeConnections;
    /* access modifiers changed from: private */
    public ConnectThread mConnectThread;
    private final BluetoothIBridgeAdapter.MyHandler mHandler;
    private BluetoothIBridgeConnectionListener mListener;
    private String mPincode = "1234";

    private class ConnectThread extends Thread {
        private boolean cancleBond = false;
        private final int mmBondTime;
        private final BluetoothIBridgeDevice mmDevice;
        private BluetoothSocket mmSocket;
        private final String name;

        protected ConnectThread(BluetoothIBridgeDevice device, int bondTime) {
            this.mmDevice = device;
            this.name = device.getDeviceName();
            this.mmBondTime = bondTime;
        }

        public void run() {
            Log.i("ConnManager", "connect thread run...");
            setName("ConnectThread" + this.name);
            if (BluetoothIBridgeConnManager.this.mAdapter.isDiscovering()) {
                Log.i("ConnManager", "cancel previous discovering");
                BluetoothIBridgeConnManager.this.mAdapter.cancelDiscovery();
            }
            if (this.mmDevice != null) {
                this.mmDevice.setConnectStatus(ConnectStatus.STATUS_CONNECTTING);
            } else {
                Log.e("ConnManager", "device is null");
            }
            if (BluetoothIBridgeConnManager.this.autoPair) {
                Log.i("ConnManager", "auto pair is enable");
                Log.i("ConnManager", "do bond process");
                doBondProcess();
            }
            Log.i("ConnManager", "connect refcomm socket");
            boolean connectResult = connectRfcommSocket();
            if (!connectResult) {
                if (this.mmDevice.getBondStatus().equals(BondStatus.STATE_BONDED)) {
                    try {
                        sleep(300);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    try {
                        if (this.mmSocket != null) {
                            this.mmSocket.close();
                        }
                    } catch (IOException e2) {
                        Log.e("ConnManager", "unable to close socket:" + e2.getMessage());
                    }
                    Log.i("ConnManager", "connect with channel 6");
                    connectResult = connectWithChannel(6);
                }
                if (!connectResult) {
                    try {
                        sleep(300);
                    } catch (InterruptedException e3) {
                        e3.printStackTrace();
                    }
                    try {
                        if (this.mmSocket != null) {
                            this.mmSocket.close();
                        }
                    } catch (IOException e4) {
                        Log.e("ConnManager", "unable to close socket:" + e4.getMessage());
                    }
                    BluetoothIBridgeConnManager.this.connectionFailed(this.mmDevice, BluetoothIBridgeConnManager.this.lastExceptionMsg);
                    Log.i("ConnManager", "connect thread run.");
                    return;
                }
            }
            synchronized (BluetoothIBridgeConnManager.this) {
                BluetoothIBridgeConnManager.this.mConnectThread = null;
            }
            if (this.mmDevice != null) {
                this.mmDevice.setConnectionDirection(BluetoothIBridgeDevice.Direction.DIRECTION_FORWARD);
                this.mmDevice.setBondStatus();
            }
            BluetoothIBridgeConnManager.this.mBluetoothIBridgeConnections.connected(this.mmSocket, this.mmDevice);
            Log.i("ConnManager", "connected");
            Log.i("ConnManager", "connect thread run.");
        }

        /* JADX WARNING: Removed duplicated region for block: B:32:0x009d  */
        private boolean connectRfcommSocket() {
            boolean result = false;
            int max_retry_count = 2;
            Log.i("ConnManager", "connectRfcommSocket...");
            this.mmSocket = this.mmDevice.createSocket();
            if (SystemUtils.isMediatekPlatform()) {
                try {
                    Log.i("ConnManager", "it is MTK platform");
                    sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            while (true) {
                try {
                    if (this.mmSocket == null) {
                        Log.e("ConnManager", "socket is null");
                        BluetoothIBridgeConnManager.this.lastExceptionMsg = "socket is null";
                        break;
                    }
                    Log.i("ConnManager", "socket connect");
                    this.mmSocket.connect();
                    result = true;
                    break;
                } catch (IOException e2) {
                    result = false;
                    if (e2.getMessage() != null && e2.getMessage().equals("Service discovery failed")) {
                        Log.e("ConnManager", "no service found");
                        if (max_retry_count <= 0) {
                            Log.e("ConnManager", "max retry count reached");
                            BluetoothIBridgeConnManager.this.lastExceptionMsg = e2.getMessage();
                            break;
                        }
                        Log.i("ConnManager", "retry");
                        max_retry_count--;
                        try {
                            sleep(300);
                        } catch (InterruptedException ie) {
                            ie.printStackTrace();
                        }
                    } else {
                        Log.e("ConnManager", "connect failed");
                        if (e2.getMessage() != null) {
                        }
                    }

                    Log.e("ConnManager", "connect failed");
                    if (e2.getMessage() != null) {
                        Log.e("ConnManager", "error is " + e2.getMessage());
                        BluetoothIBridgeConnManager.this.lastExceptionMsg = e2.getMessage();
                    }
                }
            }

            Log.i("ConnManager", "connectRfcommSocket.");
            return result;
        }

        private boolean connectWithChannel(int channel) {
            boolean result;
            Log.i("ConnManager", "connectWithChannel " + channel + "...");
            this.mmSocket = this.mmDevice.createSocketWithChannel(channel);
            try {
                this.mmSocket.connect();
                result = true;
            } catch (IOException e) {
                result = false;
                Log.e("ConnManager", "connect failed");
                if (e.getMessage() != null) {
                    Log.e("ConnManager", "error is " + e.getMessage());
                    BluetoothIBridgeConnManager.this.lastExceptionMsg = e.getMessage();
                }
            }
            Log.i("ConnManager", "connectWithChannel.");
            return result;
        }

        private void doBondProcess() {
            boolean isPaired = false;
            boolean bonding = false;
            int during = 0;
            Log.i("ConnManager", "doBondProcess...");
            while (true) {
                if (this.cancleBond || 0 != 0 || during >= this.mmBondTime * 2) {
                    break;
                }
                BluetoothDevice device = BluetoothIBridgeConnManager.this.mAdapter.getRemoteDevice(this.mmDevice.getDeviceAddress());
                if (device.getBondState() == 12) {
                    Log.i("ConnManager", "bond status is bonded");
                    isPaired = true;
                    this.mmDevice.setBondStatus(BondStatus.STATE_BONDED);
                    break;
                }
                if (device.getBondState() == 11) {
                    Log.i("ConnManager", "bond status is bonding");
                    this.mmDevice.setBondStatus(BondStatus.STATE_BONDING);
                } else if (device.getBondState() == 10) {
                    Log.i("ConnManager", "bond status is none");
                    if (!bonding) {
                        try {
                            Log.i("ConnManager", "start bond device");
                            this.mmDevice.createBond();
                            bonding = true;
                            this.mmDevice.setBondStatus(BondStatus.STATE_BONDING);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        Log.i("ConnManager", "bond failed");
                        this.mmDevice.setBondStatus(BondStatus.STATE_BONDFAILED);
                        bonding = false;
                    }
                }
                try {
                    sleep(500);
                } catch (InterruptedException e2) {
                    e2.printStackTrace();
                }
                during++;
            }
            if (this.cancleBond) {
                Log.i("ConnManager", "bond canceled");
                this.mmDevice.setBondStatus(BondStatus.STATE_BOND_CANCLED);
            } else if (!isPaired && during >= this.mmBondTime) {
                Log.i("ConnManager", "bond timeout");
                this.mmDevice.setBondStatus(BondStatus.STATE_BOND_OVERTIME);
            }
            Log.i("ConnManager", "doBondProcess.");
        }

        /* access modifiers changed from: 0000 */
        public void cancel() {
            Log.i("ConnManager", "cancel...");
            try {
                if (this.mmSocket != null) {
                    this.mmSocket.close();
                }
            } catch (IOException e) {
                Log.e("ConnManager", "close() of connect " + this.name + " socket failed", e);
            }
            Log.i("ConnManager", "cancel.");
        }

        /* access modifiers changed from: 0000 */
        public void cancelBondProcess() {
            Log.i("ConnManager", "cancelBondProcess...");
            this.cancleBond = true;
            Log.i("ConnManager", "cancelBondProcess.");
        }
    }

    protected BluetoothIBridgeConnManager(Context context, BluetoothIBridgeAdapter.MyHandler handler) {
        this.mHandler = handler;
        this.mBluetoothIBridgeConnections = new BluetoothIBridgeConnections(handler);
    }

    /* access modifiers changed from: protected */
    public synchronized void start() {
        if (this.mListener == null) {
            this.mListener = new BluetoothIBridgeConnectionListener(this, this.auth);
        }
        this.mListener.start();
        if (this.mConnectThread != null) {
            this.mConnectThread.cancel();
            this.mConnectThread = null;
        }
    }

    /* access modifiers changed from: protected */
    public synchronized void stop() {
        if (this.mListener != null) {
            this.mListener.stop();
            this.mListener = null;
        }
        if (this.mConnectThread != null) {
            this.mConnectThread.cancel();
            this.mConnectThread = null;
        }
        if (this.mBluetoothIBridgeConnections != null) {
            this.mBluetoothIBridgeConnections.disconnectAll();
        }
    }

    /* access modifiers changed from: protected */
    public void registerDataReceiver(DataReceiver receiver) {
        this.mBluetoothIBridgeConnections.registerDataReceiver(receiver);
    }

    /* access modifiers changed from: protected */
    public void unregisterDataReceiver(DataReceiver receiver) {
        this.mBluetoothIBridgeConnections.unregisterDataReceiver(receiver);
    }

    /* access modifiers changed from: 0000 */
    public synchronized void connect(BluetoothIBridgeDevice device, int bondTime) {
        Log.i("ConnManager", "connect...");
        if (this.mConnectThread != null) {
            Log.i("ConnManager", "cancel previous connecting");
            this.mConnectThread.cancel();
            this.mConnectThread = null;
        }
        device.setBondStatus();
        Log.i("ConnManager", "autoPair = " + this.autoPair + " bond status = " + device.getBondStatus());
        if (this.autoPair && device.getBondStatus().equals(BondStatus.STATE_BONDNONE)) {
            Log.i("ConnManager", "set bond status to bonding");
            device.setBondStatus(BondStatus.STATE_BONDING);
        }
        if (device == null || device.isConnected()) {
            Log.e("ConnManager", "device is connected or is null");
        } else {
            Log.i("ConnManager", "set connect status to connecting");
            device.setConnectStatus(ConnectStatus.STATUS_CONNECTTING);
            Log.i("ConnManager", "create thread to connect");
            this.mConnectThread = new ConnectThread(device, bondTime);
            this.mConnectThread.start();
        }
        Log.i("ConnManager", "connect.");
    }

    /* access modifiers changed from: 0000 */
    public synchronized void cancelBond() {
        if (this.mConnectThread != null) {
            this.mConnectThread.cancelBondProcess();
        }
    }

    /* access modifiers changed from: 0000 */
    public synchronized void disconnect(BluetoothIBridgeDevice device) {
        Log.i("ConnManager", "disconnect...");
        this.mBluetoothIBridgeConnections.disconnect(device);
        Log.i("ConnManager", "disconnect.");
    }

    /* access modifiers changed from: 0000 */
    public void write(BluetoothIBridgeDevice device, byte[] buffer, int length) {
        this.mBluetoothIBridgeConnections.write(device, buffer, length);
    }

    /* access modifiers changed from: 0000 */
    public List<BluetoothIBridgeDevice> getCurrentConnectedDevice() {
        return this.mBluetoothIBridgeConnections.getCurrentConnectedDevice();
    }

    /* access modifiers changed from: 0000 */
    public void setPincode(String pincode) {
        this.mPincode = pincode;
    }

    /* access modifiers changed from: 0000 */
    public void setAutoBond(boolean auto) {
        this.autoPair = auto;
    }

    /* access modifiers changed from: 0000 */
    public void setLinkKeyNeedAuthenticated(boolean authenticated) {
        if (this.mListener != null) {
            this.auth = authenticated;
            this.mListener.setLinkKeyNeedAuthenticated(authenticated);
        }
    }

    /* access modifiers changed from: private */
    public void connectionFailed(BluetoothIBridgeDevice device, String exceptionMsg) {
        if (device != null) {
            device.connected(false);
            device.setConnectStatus(ConnectStatus.STATUS_CONNECTFAILED);
        }
        Message msg = this.mHandler.obtainMessage(8);
        msg.obj = device;
        Bundle bundle = new Bundle();
        bundle.putString("exception", exceptionMsg);
        msg.setData(bundle);
        this.mHandler.sendMessage(msg);
        synchronized (this) {
            this.mConnectThread = null;
        }
    }

    /* access modifiers changed from: 0000 */
    public void onPairingRequested(BluetoothIBridgeDevice device, int type, int pairingKey) {
        switch (type) {
            case 0:
                device.setPin(this.mPincode.getBytes());
                return;
            case 2:
            case 3:
                device.setPairingConfirmation(true);
                return;
            case 4:
                String format = String.format("%06d", new Object[]{Integer.valueOf(pairingKey)});
                device.setPairingConfirmation(true);
                return;
            case 5:
                device.setPin(String.format("%04d", new Object[]{Integer.valueOf(pairingKey)}).getBytes());
                return;
            default:
                return;
        }
    }

    public void onConnectionEstablished(BluetoothSocket socket) {
        BluetoothIBridgeDevice device = BluetoothIBridgeDeviceFactory.getDefaultFactory().createDevice(socket.getRemoteDevice(), BluetoothIBridgeDevice.DEVICE_TYPE_CLASSIC);
        if (device != null) {
            device.setConnectionDirection(BluetoothIBridgeDevice.Direction.DIRECTION_BACKWARD);
            device.setBondStatus();
        }
        this.mBluetoothIBridgeConnections.connected(socket, device);
    }
}
