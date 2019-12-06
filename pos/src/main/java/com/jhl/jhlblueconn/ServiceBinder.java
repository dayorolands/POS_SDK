package com.jhl.jhlblueconn;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import com.jhl.bluetooth.ibridge.BluetoothIBridgeAdapter;
import com.jhl.jhlblueconn.BluetoothCommmanager.LocalBinder;

import java.util.ArrayList;
import java.util.List;

public class ServiceBinder {
    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            BluetoothIBridgeAdapter adapter = ((LocalBinder) service).getBluetoothAdapter();
            for (BluetoothAdapterListener l : ServiceBinder.this.mListeners) {
                if (l != null) {
                    l.onBluetoothAdapterCreated(adapter);
                }
            }
        }

        public void onServiceDisconnected(ComponentName name) {
            for (BluetoothAdapterListener l : ServiceBinder.this.mListeners) {
                if (l != null) {
                    l.onBluetoothAdapterDestroyed();
                }
            }
        }
    };
    private Context mContext;
    private boolean mIsBound;
    /* access modifiers changed from: private */
    public List<BluetoothAdapterListener> mListeners;

    public interface BluetoothAdapterListener {
        void onBluetoothAdapterCreated(BluetoothIBridgeAdapter bluetoothIBridgeAdapter);

        void onBluetoothAdapterDestroyed();
    }

    public ServiceBinder(Context context) {
        this.mContext = context;
        this.mListeners = new ArrayList();
    }

    /* access modifiers changed from: 0000 */
    public void doBindService() {
        this.mContext.bindService(new Intent(this.mContext, BluetoothCommmanager.class), this.mConnection, 1);
        this.mIsBound = true;
    }

    /* access modifiers changed from: 0000 */
    public void doUnbindService() {
        if (this.mIsBound) {
            this.mContext.unbindService(this.mConnection);
            this.mIsBound = false;
        }
    }

    public void registerBluetoothAdapterListener(BluetoothAdapterListener listener) {
        synchronized (this.mListeners) {
            this.mListeners.add(listener);
        }
    }

    public void unregisterBluetoothAdapterListener(BluetoothAdapterListener listener) {
        synchronized (this.mListeners) {
            this.mListeners.remove(listener);
        }
    }
}
