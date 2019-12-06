package com.jhl.bluetooth.ibridge;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.os.Build.VERSION;
import android.util.Log;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.UUID;

final class BluetoothIBridgeConnectionListener {
    private static final boolean D = true;
    private static final String TAG = "BluetoothIBridgeConnectionListener";
    /* access modifiers changed from: private */
    public BluetoothAdapter mAdapter = BluetoothAdapter.getDefaultAdapter();
    /* access modifiers changed from: private */
    public boolean mAuthenticated;
    /* access modifiers changed from: private */
    public ConnectionReceiver mReceiver;
    private AcceptThread mThread;

    private class AcceptThread extends Thread {
        private static final String SERVICE_NAME = "IVT-IBridge";
        private BluetoothServerSocket mmServerSocket = null;
        private volatile boolean running = true;

        protected AcceptThread() {
            BluetoothServerSocket tmp = null;
            try {
                if (BluetoothIBridgeConnectionListener.this.mAuthenticated || VERSION.SDK_INT < 10) {
                    tmp = BluetoothIBridgeConnectionListener.this.mAdapter.listenUsingRfcommWithServiceRecord(SERVICE_NAME, BluetoothIBridgeDevice.SPPUUID);
                    Log.i("ConnListener", "secure rfcomm " + tmp);
                    this.mmServerSocket = tmp;
                }
                tmp = listenUsingInsecureRfcommWithServiceRecord(SERVICE_NAME, BluetoothIBridgeDevice.SPPUUID);
                Log.i("ConnListener", "insecure rfcomm " + tmp);
                this.mmServerSocket = tmp;
            } catch (IOException e) {
                Log.e("ConnListener", "Connection listen failed", e);
            }
        }

        public void run() {
            setName("AcceptThread");
            while (this.running) {
                try {
                    if (this.mmServerSocket != null) {
                        BluetoothSocket socket = this.mmServerSocket.accept();
                        if (!(socket == null || BluetoothIBridgeConnectionListener.this.mReceiver == null)) {
                            BluetoothIBridgeConnectionListener.this.mReceiver.onConnectionEstablished(socket);
                        }
                    } else {
                        return;
                    }
                } catch (IOException e) {
                    Log.i("ConnListener", "accept failed");
                    return;
                }
            }
        }

        /* access modifiers changed from: 0000 */
        public void cancel() {
            try {
                if (this.mmServerSocket != null) {
                    this.mmServerSocket.close();
                }
            } catch (IOException e) {
            }
        }

        private BluetoothServerSocket listenUsingInsecureRfcommWithServiceRecord(String serviceName, UUID serviceUUID) {
            BluetoothServerSocket socket = null;
            try {
                return (BluetoothServerSocket) BluetoothAdapter.class.getMethod("listenUsingInsecureRfcommWithServiceRecord", new Class[]{String.class, UUID.class}).invoke(BluetoothIBridgeConnectionListener.this.mAdapter, new Object[]{serviceName, serviceUUID});
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
                return socket;
            } catch (IllegalArgumentException e2) {
                e2.printStackTrace();
                return socket;
            } catch (IllegalAccessException e3) {
                e3.printStackTrace();
                return socket;
            } catch (InvocationTargetException e4) {
                e4.printStackTrace();
                return socket;
            }
        }
    }

    protected interface ConnectionReceiver {
        void onConnectionEstablished(BluetoothSocket bluetoothSocket);
    }

    protected BluetoothIBridgeConnectionListener(ConnectionReceiver receiver, boolean auth) {
        this.mReceiver = receiver;
        this.mAuthenticated = auth;
    }

    /* access modifiers changed from: 0000 */
    public void start() {
        if (this.mThread != null) {
            this.mThread.cancel();
        }
        this.mThread = new AcceptThread();
        this.mThread.start();
    }

    /* access modifiers changed from: 0000 */
    public void stop() {
        if (this.mThread != null) {
            this.mThread.cancel();
        }
    }

    /* access modifiers changed from: 0000 */
    public void setLinkKeyNeedAuthenticated(boolean authenticated) {
        if (this.mAuthenticated != authenticated) {
            this.mAuthenticated = authenticated;
            stop();
            start();
        }
    }
}
