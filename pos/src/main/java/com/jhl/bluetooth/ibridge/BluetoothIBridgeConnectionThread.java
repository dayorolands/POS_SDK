package com.jhl.bluetooth.ibridge;

import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Message;

import com.jhl.bluetooth.ibridge.BluetoothIBridgeAdapter.DataReceiver;
import com.jhl.bluetooth.ibridge.BluetoothIBridgeDevice.ConnectStatus;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

class BluetoothIBridgeConnectionThread extends Thread {
    private static final int MAX_LEN = 65536;
    private byte[] buffer;
    private boolean isSocketReset = false;
    private ArrayList<DataReceiver> mDataReceivers;
    private final BluetoothIBridgeAdapter.MyHandler mHandler;
    private final BluetoothIBridgeDevice mmDevice;
    private final InputStream mmInStream;
    private final OutputStream mmOutStream;
    private final BluetoothSocket mmSocket;

    protected BluetoothIBridgeConnectionThread(BluetoothSocket socket, BluetoothIBridgeDevice device, BluetoothIBridgeAdapter.MyHandler handler, ArrayList<DataReceiver> receivers) {
        this.mmSocket = socket;
        this.mmDevice = device;
        InputStream tmpIn = null;
        OutputStream tmpOut = null;
        this.mHandler = handler;
        this.mDataReceivers = receivers;
        this.buffer = new byte[65536];
        try {
            tmpIn = socket.getInputStream();
            tmpOut = socket.getOutputStream();
        } catch (IOException e) {
        }
        this.mmInStream = tmpIn;
        this.mmOutStream = tmpOut;
        this.isSocketReset = false;
    }

    public void run() {
        byte[] buffer2 = new byte[1024];
        while (true) {
            try {
                int bytes = this.mmInStream.read(buffer2);
                this.mmDevice.buffer = buffer2;
                this.mmDevice.length = bytes;
                if (this.mDataReceivers != null) {
                    ArrayList<DataReceiver> listenersCopy = (ArrayList) this.mDataReceivers.clone();
                    int numListeners = listenersCopy.size();
                    for (int i = 0; i < numListeners; i++) {
                        DataReceiver er = (DataReceiver) listenersCopy.get(i);
                        if (this.mmDevice.isValidDevice() && er != null) {
                            er.onDataReceived(this.mmDevice, this.mmDevice.buffer, this.mmDevice.length);
                        }
                    }
                }
            } catch (IOException e) {
                connectionLost(e.getMessage());
                return;
            }
        }
    }

    private void connectionLost(String exceptionMsg) {
        if (!this.isSocketReset) {
            resetSocket(this.mmSocket);
        }
        if (this.mmDevice != null) {
            this.mmDevice.connected(false);
            this.mmDevice.setConnectStatus(ConnectStatus.STATUS_DISCONNECTED);
        }
        Message msg = this.mHandler.obtainMessage(7);
        msg.obj = this.mmDevice;
        Bundle bundle = new Bundle();
        bundle.putString("exception", exceptionMsg);
        msg.setData(bundle);
        this.mHandler.sendMessage(msg);
    }

    /* access modifiers changed from: 0000 */
    public void write(byte[] buf, int length) {
        try {
            System.arraycopy(buf, 0, this.buffer, 0, Math.min(length, 1024));
            this.mmOutStream.write(this.buffer, 0, length);
            this.mmOutStream.flush();
        } catch (IOException e) {
            Message msg = this.mHandler.obtainMessage(11);
            msg.obj = this.mmDevice;
            Bundle bundle = new Bundle();
            bundle.putString("exception", e.getMessage());
            msg.setData(bundle);
            this.mHandler.sendMessage(msg);
        }
    }

    /* access modifiers changed from: 0000 */
    public void cancel() {
        this.isSocketReset = true;
        resetSocket(this.mmSocket);
    }

    public boolean equals(Object o) {
        if (o != null && (o instanceof BluetoothIBridgeConnectionThread)) {
            return ((BluetoothIBridgeConnectionThread) o).mmDevice.equals(this.mmDevice);
        }
        return false;
    }

    /* access modifiers changed from: 0000 */
    public BluetoothIBridgeDevice getDevice() {
        return this.mmDevice;
    }

    static void resetSocket(BluetoothSocket sock) {
        if (sock != null) {
            try {
                InputStream is = sock.getInputStream();
                if (is != null) {
                    is.close();
                }
            } catch (IOException ie) {
                ie.printStackTrace();
            }
            try {
                OutputStream os = sock.getOutputStream();
                if (os != null) {
                    os.close();
                }
            } catch (IOException oe) {
                oe.printStackTrace();
            }
            try {
                sock.close();
            } catch (IOException se) {
                se.printStackTrace();
            }
        }
    }
}
