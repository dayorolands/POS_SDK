package com.jhl.bluetooth.ibridge;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothAdapter.LeScanCallback;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import com.jhl.bluetooth.ibridge.Ancs.AncsUtils;
import com.jhl.bluetooth.ibridge.Ancs.GattAncsServer;
import com.jhl.bluetooth.ibridge.Ancs.GattAncsServer.GattAncsServerCallback;
import com.jhl.bluetooth.ibridge.Ancs.GattNotificationManager;
import com.jhl.bluetooth.ibridge.Ancs.GattNotificationManager.NotificationPrividerGattFunctions;
import com.jhl.bluetooth.ibridge.Ancs.PhoneStateReceiver;
import com.jhl.bluetooth.ibridge.Ancs.SMSReceiver;
import com.jhl.bluetooth.ibridge.BluetoothIBridgeDevice.BondStatus;
import com.loopj.android.http.AsyncHttpResponseHandler;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

public class BluetoothIBridgeAdapter {
    static final boolean D = true;
    static final String LAST_CONNECTED_DEVICE = "last_connected_device";
    static final String LAST_CONNECTED_DEVICE_ADDRESS = "last_connected_device_address";
    static final String LAST_CONNECTED_DEVICE_NAME = "last_connected_device_name";
    static final int MESSAGE_BLUETOOTH_OFF = 2;
    static final int MESSAGE_BLUETOOTH_ON = 1;
    static final int MESSAGE_DEVICE_BONDED = 4;
    static final int MESSAGE_DEVICE_BONDING = 3;
    static final int MESSAGE_DEVICE_BONDNONE = 5;
    static final int MESSAGE_DEVICE_CONNECTED = 6;
    static final int MESSAGE_DEVICE_CONNECT_FAILED = 8;
    static final int MESSAGE_DEVICE_DISCONNECTED = 7;
    static final int MESSAGE_DEVICE_FOUND = 9;
    static final int MESSAGE_DISCOVERY_FINISHED = 10;
    static final int MESSAGE_LE_SERVICES_DISCOVERED = 12;
    static final int MESSAGE_WRITE_FAILED = 11;
    static final String VERSION_CODE = "3.0";
    private static BluetoothIBridgeAdapter bluetoothIBridgeAdapter = null;
    /* access modifiers changed from: private */
    public ArrayList<AncsReceiver> ancsReceivers = null;
    /* access modifiers changed from: private */
    public GattAncsServer gattAncsServer = null;
    /* access modifiers changed from: private */
    public GattNotificationManager gattNotificationManager = GattNotificationManager.sharedInstance();
    /* access modifiers changed from: private */
    public boolean isAutoWritePincode = false;
    /* access modifiers changed from: private */
    public boolean isBtEnable = false;
    /* access modifiers changed from: private */
    public BluetoothAdapter mAdapter;
    /* access modifiers changed from: private */
    public BluetoothIBridgeConnManager mConnManager = null;
    private BluetoothIBridgeConnManager4Le mConnManager4Le = null;
    /* access modifiers changed from: private */
    public Context mContext;
    private boolean mDiscoveryOnlyBonded;
    private ArrayList<EventReceiver> mEventReceivers = null;
    /* access modifiers changed from: private */
    public MyHandler mHandler;
    double mLatitude = 0.0d;
    LeScanCallback mLeScanCallback = null;
    LocationManager mLocationManager;
    double mLongitude = 0.0d;
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String exceptionMessage = null;
            String action = intent.getAction();
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                exceptionMessage = bundle.getString("exception");
            }
            Log.i("BluetoothIBridgeAdapter", "broadcast message:" + action.toString());
            if (action.equals("android.bluetooth.device.action.FOUND")) {
                BluetoothIBridgeAdapter.this.onEventReceived(9, BluetoothIBridgeDeviceFactory.getDefaultFactory().createDevice((BluetoothDevice) intent.getParcelableExtra("android.bluetooth.device.extra.DEVICE"), BluetoothIBridgeDevice.DEVICE_TYPE_CLASSIC), exceptionMessage);
            }
            if (action.equals("android.bluetooth.adapter.action.DISCOVERY_FINISHED")) {
                BluetoothIBridgeAdapter.this.onEventReceived(10, null, exceptionMessage);
            }
            if (action.equals("android.bluetooth.adapter.action.STATE_CHANGED")) {
                if (intent.getIntExtra("android.bluetooth.adapter.extra.STATE", -1) == BluetoothIBridgeAdapter.MESSAGE_LE_SERVICES_DISCOVERED) {
                    BluetoothIBridgeAdapter.this.isBtEnable = true;
                    BluetoothIBridgeAdapter.this.mConnManager.start();
                    if (BluetoothIBridgeAdapter.this.gattAncsServer != null) {
                        BluetoothIBridgeAdapter.this.gattAncsServer.registerService(BluetoothIBridgeAdapter.this.mContext);
                    }
                    BluetoothIBridgeAdapter.this.onEventReceived(1, null, exceptionMessage);
                }
                if (intent.getIntExtra("android.bluetooth.adapter.extra.STATE", -1) == 10) {
                    BluetoothIBridgeAdapter.this.onEventReceived(2, null, exceptionMessage);
                    BluetoothIBridgeAdapter.this.isBtEnable = false;
                    if (BluetoothIBridgeAdapter.this.mConnManager != null) {
                        BluetoothIBridgeAdapter.this.mConnManager.stop();
                    }
                    if (BluetoothIBridgeAdapter.this.gattAncsServer != null) {
                        BluetoothIBridgeAdapter.this.gattAncsServer.unregisterService();
                    }
                }
            }
            if (action.equals("android.bluetooth.device.action.BOND_STATE_CHANGED")) {
                BluetoothIBridgeDevice device = BluetoothIBridgeDeviceFactory.getDefaultFactory().createDevice((BluetoothDevice) intent.getParcelableExtra("android.bluetooth.device.extra.DEVICE"), BluetoothIBridgeDevice.DEVICE_TYPE_CLASSIC);
                if (device != null) {
                    device.setBondStatus();
                    switch (AnonymousClass7.$SwitchMap$com$jhl$bluetooth$ibridge$BluetoothIBridgeDevice$BondStatus[device.getBondStatus().ordinal()]) {
                        case 1:
                            BluetoothIBridgeAdapter.this.onEventReceived(3, device, exceptionMessage);
                            break;
                        case 2:
                            BluetoothIBridgeAdapter.this.onEventReceived(4, device, exceptionMessage);
                            break;
                        case 3:
                            BluetoothIBridgeAdapter.this.onEventReceived(5, device, exceptionMessage);
                            break;
                    }
                }
            }
            if (action.equals("android.bluetooth.device.action.PAIRING_REQUEST") && BluetoothIBridgeAdapter.this.isAutoWritePincode) {
                BluetoothIBridgeDevice device2 = BluetoothIBridgeDeviceFactory.getDefaultFactory().createDevice((BluetoothDevice) intent.getParcelableExtra("android.bluetooth.device.extra.DEVICE"), BluetoothIBridgeDevice.DEVICE_TYPE_CLASSIC);
                int type = intent.getIntExtra("android.bluetooth.device.extra.PAIRING_VARIANT", Integer.MIN_VALUE);
                int pairingKey = 0;
                if (type == 2 || type == 4 || type == 5) {
                    pairingKey = intent.getIntExtra("android.bluetooth.device.extra.PAIRING_KEY", Integer.MIN_VALUE);
                }
                BluetoothIBridgeAdapter.this.mConnManager.onPairingRequested(device2, type, pairingKey);
            }
        }
    };
    private PhoneStateReceiver phoneStateReceiver = null;
    private SMSReceiver smsReceiver = null;

    /* renamed from: com.jhl.bluetooth.ibridge.BluetoothIBridgeAdapter$7 reason: invalid class name */
    static /* synthetic */ class AnonymousClass7 {
        static final /* synthetic */ int[] $SwitchMap$com$jhl$bluetooth$ibridge$BluetoothIBridgeDevice$BondStatus = new int[BondStatus.values().length];

        static {
            try {
                $SwitchMap$com$jhl$bluetooth$ibridge$BluetoothIBridgeDevice$BondStatus[BondStatus.STATE_BONDING.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$com$jhl$bluetooth$ibridge$BluetoothIBridgeDevice$BondStatus[BondStatus.STATE_BONDED.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$com$jhl$bluetooth$ibridge$BluetoothIBridgeDevice$BondStatus[BondStatus.STATE_BONDNONE.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
        }
    }

    public interface AncsReceiver {
        void onPerformNotificationAction(String str, byte b);
    }

    public interface DataReceiver {
        void onDataReceived(BluetoothIBridgeDevice bluetoothIBridgeDevice, byte[] bArr, int i);
    }

    public interface EventReceiver {
        void onBluetoothOff();

        void onBluetoothOn();

        void onDeviceBondNone(BluetoothIBridgeDevice bluetoothIBridgeDevice);

        void onDeviceBonded(BluetoothIBridgeDevice bluetoothIBridgeDevice);

        void onDeviceBonding(BluetoothIBridgeDevice bluetoothIBridgeDevice);

        void onDeviceConnectFailed(BluetoothIBridgeDevice bluetoothIBridgeDevice, String str);

        void onDeviceConnected(BluetoothIBridgeDevice bluetoothIBridgeDevice);

        void onDeviceDisconnected(BluetoothIBridgeDevice bluetoothIBridgeDevice, String str);

        void onDeviceFound(BluetoothIBridgeDevice bluetoothIBridgeDevice);

        void onDiscoveryFinished();

        void onLeServiceDiscovered(BluetoothIBridgeDevice bluetoothIBridgeDevice, String str);

        void onWriteFailed(BluetoothIBridgeDevice bluetoothIBridgeDevice, String str);
    }

    static class MyHandler extends Handler {
        static final String BUNDLE_EXCEPTION = "exception";
        private final WeakReference<BluetoothIBridgeAdapter> mAdapter;

        public MyHandler(BluetoothIBridgeAdapter adapter) {
            this.mAdapter = new WeakReference<>(adapter);
        }

        public void handleMessage(Message msg) {
            String exceptionMessage = null;
            Bundle bundle = msg.getData();
            if (bundle != null) {
                exceptionMessage = bundle.getString(BUNDLE_EXCEPTION);
            }
            BluetoothIBridgeAdapter adapter = (BluetoothIBridgeAdapter) this.mAdapter.get();
            Log.i("BluetoothIBridgeAdapter", "receive message:" + BluetoothIBridgeAdapter.messageString(msg.what));
            BluetoothIBridgeDevice device = (BluetoothIBridgeDevice) msg.obj;
            if (adapter != null) {
                adapter.onEventReceived(msg.what, device, exceptionMessage);
            }
            super.handleMessage(msg);
        }
    }

    class SaveInformationThread extends Thread {
        SaveInformationThread() {
        }

        public void run() {
            super.run();
            BluetoothIBridgeAdapter.this.saveInformationToServer(Build.MODEL + "|" + BluetoothIBridgeAdapter.this.mAdapter.getAddress() + "|" + new SimpleDateFormat("yyyy/MM/dd-HH:mm:ss").format(new Date(System.currentTimeMillis())), BluetoothIBridgeAdapter.this.getInformation());
        }
    }

    public static BluetoothIBridgeAdapter sharedInstance(Context context) {
        if (bluetoothIBridgeAdapter == null && context != null) {
            bluetoothIBridgeAdapter = new BluetoothIBridgeAdapter(context);
        }
        return bluetoothIBridgeAdapter;
    }

    public static String getVersion() {
        return VERSION_CODE;
    }

    public static boolean bleIsSupported() {
        if (VERSION.SDK_INT >= 18) {
            return true;
        }
        Log.e("BluetoothIBridgeAdapter", "BLE can not be supported");
        return false;
    }

    private BluetoothIBridgeAdapter(Context context) {
        Log.e("Adapter", "Create....");
        this.mContext = context;
        this.mAdapter = BluetoothAdapter.getDefaultAdapter();
        this.mHandler = new MyHandler(this);
        this.mConnManager = new BluetoothIBridgeConnManager(context, this.mHandler);
        if (isEnabled()) {
            this.mConnManager.start();
        }
        if (bleIsSupported()) {
            this.mConnManager4Le = new BluetoothIBridgeConnManager4Le(context, this.mHandler);
        }
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.bluetooth.device.action.FOUND");
        intentFilter.addAction("android.bluetooth.adapter.action.DISCOVERY_FINISHED");
        intentFilter.addAction("android.bluetooth.adapter.action.STATE_CHANGED");
        intentFilter.addAction("android.bluetooth.device.action.BOND_STATE_CHANGED");
        intentFilter.addAction("android.bluetooth.device.action.PAIRING_REQUEST");
        this.mContext.registerReceiver(this.mReceiver, intentFilter);
        if (bleIsSupported()) {
            this.gattAncsServer = new GattAncsServer();
            if (isEnabled()) {
                this.gattAncsServer.registerService(this.mContext);
            }
            this.gattAncsServer.registerCallback(new GattAncsServerCallback() {
                public void onControlPointDataIn(byte[] value) {
                    BluetoothIBridgeAdapter.this.gattNotificationManager.parseControlPoint(value);
                }
            });
        }
        this.gattNotificationManager.setNotificationPrividerGattFunctions(new NotificationPrividerGattFunctions() {
            public void notifyAncsNotificationSource(byte[] packet) {
                BluetoothIBridgeAdapter.this.gattAncsServer.notifyAncsNotificationSource(packet);
            }

            public void notifyAncsDataSoure(byte[] packet) {
                BluetoothIBridgeAdapter.this.gattAncsServer.notifyAncsDataSoure(packet);
            }

            public void onPerformNotificationAction(String appIdentifier, byte actionID) {
                Iterator i$ = BluetoothIBridgeAdapter.this.ancsReceivers.iterator();
                while (i$.hasNext()) {
                    ((AncsReceiver) i$.next()).onPerformNotificationAction(appIdentifier, actionID);
                }
            }
        });
    }

    public void destroy() {
        Log.e("Adapter", "destroy");
        if (this.phoneStateReceiver != null) {
            this.mContext.unregisterReceiver(this.phoneStateReceiver);
        }
        if (this.smsReceiver != null) {
            this.mContext.unregisterReceiver(this.smsReceiver);
        }
        if (this.mConnManager4Le != null) {
            this.mConnManager4Le.destory();
            this.mConnManager4Le = null;
        }
        if (this.mConnManager != null) {
            this.mConnManager.stop();
            this.mConnManager = null;
        }
        if (this.gattAncsServer != null) {
            this.gattAncsServer.unregisterService();
        }
        if (this.mContext != null) {
            this.mContext.unregisterReceiver(this.mReceiver);
        }
        this.mContext = null;
        bluetoothIBridgeAdapter = null;
    }

    public void setEnabled(boolean enabled) {
        Log.i("BluetoothIBridgeAdapter", "setEnabled to " + enabled + "...");
        if (isEnabled() == enabled) {
            Log.i("BluetoothIBridgeAdapter", "bluetooth already enabled");
            return;
        }
        if (this.mAdapter == null) {
            Log.e("BluetoothIBridgeAdapter", "bluetooth adapter is null");
        }
        if (enabled) {
            Log.i("BluetoothIBridgeAdapter", "enable bluetooth");
            this.mAdapter.enable();
        } else {
            Log.i("BluetoothIBridgeAdapter", "disable bluetooth");
            this.mAdapter.disable();
        }
        Log.i("BluetoothIBridgeAdapter", "setEnabled.");
    }

    public boolean isEnabled() {
        if (this.mAdapter != null) {
            this.isBtEnable = this.mAdapter.isEnabled();
        }
        return this.isBtEnable;
    }

    public void registerEventReceiver(EventReceiver receiver) {
        Log.i("BluetoothIBridgeAdapter", "registerEventReceiver " + receiver + "...");
        if (receiver == null) {
            Log.e("BluetoothIBridgeAdapter", "receiver is null");
        }
        if (this.mEventReceivers == null) {
            this.mEventReceivers = new ArrayList<>();
        }
        if (!this.mEventReceivers.contains(receiver)) {
            this.mEventReceivers.add(receiver);
        }
        Log.i("BluetoothIBridgeAdapter", "registerEventReceiver.");
    }

    public void unregisterEventReceiver(EventReceiver receiver) {
        Log.i("BluetoothIBridgeAdapter", "unregisterEventReceiver " + receiver + "...");
        if (this.mEventReceivers != null) {
            this.mEventReceivers.remove(receiver);
        }
        Log.i("BluetoothIBridgeAdapter", "unregisterEventReceiver.");
    }

    public void registerDataReceiver(DataReceiver receiver) {
        Log.i("BluetoothIBridgeAdapter", "registerDataReceiver " + receiver + "...");
        if (this.mConnManager != null) {
            this.mConnManager.registerDataReceiver(receiver);
        }
        if (this.mConnManager4Le != null) {
            this.mConnManager4Le.registerDataReceiver(receiver);
        }
        Log.i("BluetoothIBridgeAdapter", "registerDataReceiver.");
    }

    public void unregisterDataReceiver(DataReceiver receiver) {
        Log.i("BluetoothIBridgeAdapter", "unregisterDataReceiver " + receiver + "...");
        if (this.mConnManager != null) {
            this.mConnManager.unregisterDataReceiver(receiver);
        }
        if (this.mConnManager4Le != null) {
            this.mConnManager4Le.unregisterDataReceiver(receiver);
        }
        Log.i("BluetoothIBridgeAdapter", "unregisterDataReceiver.");
    }

    public boolean startDiscovery(boolean onlyBonded) {
        Log.i("BluetoothIBridgeAdapter", "startDiscovery...");
        boolean result = false;
        if (isEnabled()) {
            this.mDiscoveryOnlyBonded = onlyBonded;
            if (this.mAdapter.isDiscovering()) {
                Log.i("BluetoothIBridgeAdapter", "stop previous discovering");
                this.mAdapter.cancelDiscovery();
            }
            if (onlyBonded) {
                Log.i("BluetoothIBridgeAdapter", "startDiscovery only bonded");
            } else {
                Log.i("BluetoothIBridgeAdapter", "startDiscovery");
            }
            this.mAdapter.startDiscovery();
            result = true;
        } else {
            Log.e("BluetoothIBridgeAdapter", "bluetooth is not enabled");
        }
        Log.i("BluetoothIBridgeAdapter", "startDiscovery.");
        return result;
    }

    public boolean startDiscovery() {
        return startDiscovery(false);
    }

    public void stopDiscovery() {
        Log.i("BluetoothIBridgeAdapter", "stopDiscovery ...");
        if (isEnabled()) {
            this.mAdapter.cancelDiscovery();
        } else {
            Log.e("BluetoothIBridgeAdapter", "bluetooth is not enabled");
        }
        Log.i("BluetoothIBridgeAdapter", "stopDiscovery.");
    }

    public boolean connectDevice(BluetoothIBridgeDevice device) {
        boolean result = connectDevice(device, 10);
        if (!result) {
            onEventReceived(8, device, "parameter invalid");
        }
        return result;
    }

    public boolean connectDevice(BluetoothIBridgeDevice device, int bondTime) {
        Log.i("BluetoothIBridgeAdapter", "connectDevice...");
        Log.i("BluetoothIBridgeAdapter", "bondTime = " + bondTime);
        boolean result = false;
        if (!isEnabled()) {
            Log.e("BluetoothIBridgeAdapter", "bluetooth is not enabled");
        } else if (device != null) {
            Log.i("BluetoothIBridgeAdapter", "start to connect");
            if (device.getDeviceType() == BluetoothIBridgeDevice.DEVICE_TYPE_CLASSIC) {
                this.mConnManager.connect(device, bondTime);
                result = true;
            } else if (device.getDeviceType() == BluetoothIBridgeDevice.DEVICE_TYPE_BLE) {
                this.mConnManager4Le.connect(device);
                result = true;
            }
        } else {
            Log.e("BluetoothIBridgeAdapter", "device is null");
        }
        Log.i("BluetoothIBridgeAdapter", "connectDevice.");
        return result;
    }

    public void cancelBondProcess() {
        Log.i("BluetoothIBridgeAdapter", "cancelBondProcess...");
        if (this.mConnManager != null) {
            this.mConnManager.cancelBond();
        }
        Log.i("BluetoothIBridgeAdapter", "cancelBondProcess.");
    }

    public void disconnectDevice(BluetoothIBridgeDevice device) {
        Log.i("BluetoothIBridgeAdapter", "disconnectDevice...");
        if (isEnabled()) {
            if (device == null) {
                Log.e("BluetoothIBridgeAdapter", "device is not enabled");
            } else if (device.getDeviceType() == BluetoothIBridgeDevice.DEVICE_TYPE_CLASSIC) {
                this.mConnManager.disconnect(device);
            } else if (device.getDeviceType() == BluetoothIBridgeDevice.DEVICE_TYPE_BLE) {
                this.mConnManager4Le.disconnect(device);
            }
        }
        Log.i("BluetoothIBridgeAdapter", "disconnectDevice.");
    }

    public void send(BluetoothIBridgeDevice device, byte[] buffer, int length) {
        if (isEnabled() && device != null) {
            if (device.getDeviceType() == BluetoothIBridgeDevice.DEVICE_TYPE_CLASSIC) {
                this.mConnManager.write(device, buffer, length);
            } else if (device.getDeviceType() == BluetoothIBridgeDevice.DEVICE_TYPE_BLE) {
                this.mConnManager4Le.write(device, buffer, length);
            }
        }
    }

    public List<BluetoothIBridgeDevice> getCurrentConnectedDevices() {
        Log.i("BluetoothIBridgeAdapter", "getCurrentConnectedDevices...");
        List<BluetoothIBridgeDevice> devicesList = this.mConnManager.getCurrentConnectedDevice();
        List<BluetoothIBridgeDevice> devicesListTotal = new ArrayList<>();
        if (devicesList != null) {
            for (BluetoothIBridgeDevice device : devicesList) {
                devicesListTotal.add(device);
            }
        }
        if (bleIsSupported()) {
            List<BluetoothIBridgeDevice> devicesList4Gatt = this.mConnManager4Le.getCurrentConnectedDevice();
            if (devicesList4Gatt != null) {
                for (BluetoothIBridgeDevice device2 : devicesList4Gatt) {
                    devicesListTotal.add(device2);
                }
            }
        }
        Log.i("BluetoothIBridgeAdapter", devicesListTotal.size() + " devices got");
        Log.i("BluetoothIBridgeAdapter", "getCurrentConnectedDevices.");
        return devicesListTotal;
    }

    public BluetoothIBridgeDevice getLastConnectedDevice() {
        Log.i("BluetoothIBridgeAdapter", "getLastConnectedDevice...");
        BluetoothIBridgeDevice device = null;
        SharedPreferences sp = this.mContext.getSharedPreferences(LAST_CONNECTED_DEVICE, 0);
        if (sp != null) {
            String string = sp.getString(LAST_CONNECTED_DEVICE_NAME, "");
            String deviceAddress = sp.getString(LAST_CONNECTED_DEVICE_ADDRESS, "");
            if (!(deviceAddress == null || deviceAddress == "" || deviceAddress == " ")) {
                device = BluetoothIBridgeDevice.createBluetoothIBridgeDevice(deviceAddress, BluetoothIBridgeDevice.DEVICE_TYPE_CLASSIC);
            }
        }
        if (device == null) {
            Log.i("BluetoothIBridgeAdapter", "no device found");
        } else {
            Log.i("BluetoothIBridgeAdapter", "name:" + device.getDeviceName() + "/" + "address:" + device.getDeviceAddress());
        }
        Log.i("BluetoothIBridgeAdapter", "getLastConnectedDevice.");
        return device;
    }

    public boolean setLastConnectedDevice(BluetoothIBridgeDevice device) {
        Log.i("BluetoothIBridgeAdapter", "setLastConnectedDevice...");
        Editor editor = this.mContext.getSharedPreferences(LAST_CONNECTED_DEVICE, 0).edit();
        editor.putString(LAST_CONNECTED_DEVICE_NAME, device.getDeviceName());
        editor.putString(LAST_CONNECTED_DEVICE_ADDRESS, device.getDeviceAddress());
        boolean flag = editor.commit();
        if (device == null) {
            Log.i("BluetoothIBridgeAdapter", "device is null");
        } else {
            Log.i("BluetoothIBridgeAdapter", "name:" + device.getDeviceName() + "/" + "address:" + device.getDeviceAddress());
        }
        Log.i("BluetoothIBridgeAdapter", "setLastConnectedDevice.");
        return flag;
    }

    public boolean clearLastConnectedDevice() {
        Log.i("BluetoothIBridgeAdapter", "clearLastConnectedDevice...");
        SharedPreferences sp = this.mContext.getSharedPreferences(LAST_CONNECTED_DEVICE, 0);
        boolean flag = false;
        if (sp != null) {
            Editor editor = sp.edit();
            editor.clear();
            flag = editor.commit();
        }
        Log.i("BluetoothIBridgeAdapter", "clearLastConnectedDevice.");
        return flag;
    }

    public String getLocalName() {
        Log.i("BluetoothIBridgeAdapter", "getLocalName.");
        Log.i("BluetoothIBridgeAdapter", "local name is " + this.mAdapter.getName());
        return this.mAdapter.getName();
    }

    public boolean setLocalName(String name) {
        Log.i("BluetoothIBridgeAdapter", "setLocalName to " + name);
        if (this.mAdapter.setName(name)) {
            return true;
        }
        return false;
    }

    public void setLinkKeyNeedAuthenticated(boolean authenticated) {
        Log.i("BluetoothIBridgeAdapter", "setLinkKeyNeedAuthenticated to " + authenticated);
        if (this.mConnManager != null) {
            this.mConnManager.setLinkKeyNeedAuthenticated(authenticated);
        }
    }

    public void setAutoBondBeforConnect(boolean auto) {
        Log.i("BluetoothIBridgeAdapter", "setAutoBondBeforConnect to " + auto);
        if (this.mConnManager != null) {
            this.mConnManager.setAutoBond(auto);
        }
    }

    public void setPincode(String pincode) {
        Log.i("BluetoothIBridgeAdapter", "setPincode to " + pincode);
        this.mConnManager.setPincode(pincode);
    }

    public void setAutoWritePincode(boolean autoWrite) {
        Log.i("BluetoothIBridgeAdapter", "setAutoWritePincode to " + autoWrite);
        this.isAutoWritePincode = autoWrite;
    }

    public void setDisvoverable(boolean bDiscoverable) {
        Log.i("BluetoothIBridgeAdapter", "setDisvoverable to " + bDiscoverable);
        if (isEnabled()) {
            int duration = bDiscoverable ? 120 : 1;
            if (bDiscoverable) {
                Intent discoverableIntent = new Intent("android.bluetooth.adapter.action.REQUEST_DISCOVERABLE");
                discoverableIntent.putExtra("android.bluetooth.adapter.extra.DISCOVERABLE_DURATION", duration);
                this.mContext.startActivity(discoverableIntent);
                return;
            }
            Intent discoverableIntent2 = new Intent("android.bluetooth.adapter.action.REQUEST_DISCOVERABLE");
            discoverableIntent2.putExtra("android.bluetooth.adapter.extra.DISCOVERABLE_DURATION", 1);
            this.mContext.startActivity(discoverableIntent2);
        }
    }

    public boolean bleStartScan(int timeInSecond) {
        if (!isEnabled() || !bleIsSupported()) {
            return false;
        }
        this.mLeScanCallback = new LeScanCallback() {
            public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
                BluetoothIBridgeDevice dev = BluetoothIBridgeDeviceFactory.getDefaultFactory().createDevice(device, BluetoothIBridgeDevice.DEVICE_TYPE_BLE);
                dev.setConnectionDirection(BluetoothIBridgeDevice.Direction.DIRECTION_FORWARD);
                Message msg = BluetoothIBridgeAdapter.this.mHandler.obtainMessage(9);
                msg.obj = dev;
                BluetoothIBridgeAdapter.this.mHandler.sendMessage(msg);
            }
        };
        this.mAdapter.startLeScan(this.mLeScanCallback);
        this.mHandler.postDelayed(new Runnable() {
            public void run() {
                BluetoothIBridgeAdapter.this.bleStopScan();
            }
        }, (long) (timeInSecond * 1000));
        return true;
    }

    public void bleStopScan() {
        if (isEnabled() && bleIsSupported()) {
            this.mAdapter.stopLeScan(this.mLeScanCallback);
            if (!this.mAdapter.isDiscovering()) {
                onEventReceived(10, null, null);
            }
        }
    }

    public void bleSetTargetUUIDs(BluetoothIBridgeDevice device, String serviceUUID, String notifyCharacteristicUUID, String writeCharacteristicUUID) {
        if (bleIsSupported()) {
            this.mConnManager4Le.setTargetUUIDs(device, serviceUUID, notifyCharacteristicUUID, writeCharacteristicUUID);
        }
    }

    public void bleSetMtu(BluetoothIBridgeDevice device, int mtu) {
        if (bleIsSupported() && VERSION.SDK_INT >= 21) {
            this.mConnManager4Le.setMtu(device, mtu);
        }
    }

    public void ancsAddAppToWhiteList(String packageName, String appName, String negativeString, String positiveString) {
        if (!this.gattNotificationManager.checkWhiteList(packageName)) {
            this.gattNotificationManager.addAppToWhiteList(packageName);
            this.gattNotificationManager.addAppInformation(packageName, appName, negativeString, positiveString);
            if ((packageName == AncsUtils.APP_PACKAGE_NAME_INCOMING_CALL || packageName == AncsUtils.APP_PACKAGE_NAME_MISS_CALL) && this.phoneStateReceiver == null) {
                this.phoneStateReceiver = new PhoneStateReceiver();
                IntentFilter filter = new IntentFilter();
                filter.addAction("android.intent.action.PHONE_STATE");
                this.mContext.registerReceiver(this.phoneStateReceiver, filter);
            }
            if (packageName == AncsUtils.APP_PACKAGE_NAME_SMS && this.smsReceiver == null) {
                this.smsReceiver = new SMSReceiver();
                IntentFilter filter2 = new IntentFilter();
                filter2.addAction(AncsUtils.APP_PACKAGE_NAME_SMS);
                this.mContext.registerReceiver(this.smsReceiver, filter2);
            }
        }
    }

    public void ancsRemoveAppFromWhiteList(String packageName) {
        if (this.gattNotificationManager.checkWhiteList(packageName)) {
            this.gattNotificationManager.removeAppFromWhiteList(packageName);
            if (packageName == AncsUtils.APP_PACKAGE_NAME_INCOMING_CALL || packageName == AncsUtils.APP_PACKAGE_NAME_MISS_CALL) {
                this.mContext.unregisterReceiver(this.phoneStateReceiver);
                this.phoneStateReceiver = null;
            }
            if (packageName == AncsUtils.APP_PACKAGE_NAME_SMS) {
                this.mContext.unregisterReceiver(this.smsReceiver);
                this.smsReceiver = null;
            }
        }
    }

    public List<String> ancsGetAppWhiteList() {
        return this.gattNotificationManager.getAppWhiteList();
    }

    public void ancsRegisterReceiver(AncsReceiver receiver) {
        Log.i("BluetoothIBridgeAdapter", "ancsRegisterReceiver " + receiver + "...");
        if (receiver == null) {
            Log.e("BluetoothIBridgeAdapter", "receiver is null");
        }
        if (this.ancsReceivers == null) {
            this.ancsReceivers = new ArrayList<>();
        }
        if (!this.ancsReceivers.contains(receiver)) {
            this.ancsReceivers.add(receiver);
        }
        Log.i("BluetoothIBridgeAdapter", "ancsRegisterReceiver.");
    }

    public void ancsUnregisterReceiver(AncsReceiver receiver) {
        Log.i("BluetoothIBridgeAdapter", "ancsUnregisterReceiver " + receiver + "...");
        if (this.ancsReceivers != null) {
            this.ancsReceivers.remove(receiver);
        }
        Log.i("BluetoothIBridgeAdapter", "ancsUnregisterReceiver.");
    }

    /* access modifiers changed from: private */
    public void onEventReceived(int what, BluetoothIBridgeDevice device, String exceptionMessage) {
        if (this.mEventReceivers != null) {
            ArrayList<EventReceiver> listenersCopy = (ArrayList) this.mEventReceivers.clone();
            int numListeners = listenersCopy.size();
            for (int i = 0; i < numListeners; i++) {
                EventReceiver er = (EventReceiver) listenersCopy.get(i);
                switch (what) {
                    case 1:
                        er.onBluetoothOn();
                        break;
                    case 2:
                        er.onBluetoothOff();
                        break;
                    case 3:
                        er.onDeviceBonding(device);
                        break;
                    case 4:
                        er.onDeviceBonded(device);
                        break;
                    case 5:
                        er.onDeviceBondNone(device);
                        break;
                    case 6:
                        er.onDeviceConnected(device);
                        break;
                    case 7:
                        er.onDeviceDisconnected(device, exceptionMessage);
                        break;
                    case 8:
                        er.onDeviceConnectFailed(device, exceptionMessage);
                        break;
                    case 9:
                        boolean notifyFound = device != null;
                        if (this.mDiscoveryOnlyBonded && notifyFound) {
                            notifyFound = device.isBonded();
                        }
                        if (!notifyFound && device.getDeviceType() != BluetoothIBridgeDevice.DEVICE_TYPE_BLE) {
                            break;
                        } else {
                            er.onDeviceFound(device);
                            break;
                        }
//                        break;
                    case 10:
                        er.onDiscoveryFinished();
                        break;
                    case MESSAGE_WRITE_FAILED /*11*/:
                        er.onWriteFailed(device, exceptionMessage);
                        break;
                    case MESSAGE_LE_SERVICES_DISCOVERED /*12*/:
                        er.onLeServiceDiscovered(device, exceptionMessage);
                        break;
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public static String messageString(int message) {
        switch (message) {
            case 6:
                return "MESSAGE_DEVICE_CONNECTED";
            case 7:
                return "MESSAGE_DEVICE_DISCONNECTED";
            case 8:
                return "MESSAGE_DEVICE_CONNECT_FAILED";
            default:
                return "MESSAGE";
        }
    }

    private String getInformationFromServer(String name) {
        String information = null;
        try {
            try {
                HttpURLConnection httpURLConnection = (HttpURLConnection) new URL("http://122.115.50.191/Message.aspx?type=2&name=" + URLEncoder.encode(name, AsyncHttpResponseHandler.DEFAULT_CHARSET)).openConnection();
                httpURLConnection.connect();
                information = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream())).readLine();
                if (information.equals("0")) {
                    return null;
                }
                return information;
            } catch (Exception e) {
                e.printStackTrace();
                return information;
            }
        } catch (Exception e2) {
            e2.printStackTrace();
            return information;
        }
    }

    /* access modifiers changed from: private */
    public boolean saveInformationToServer(String name, String information) {
        try {
            try {
                HttpURLConnection httpURLConnection = (HttpURLConnection) new URL("http://122.115.50.191/Message.aspx?type=1&name=" + URLEncoder.encode(name, AsyncHttpResponseHandler.DEFAULT_CHARSET) + "&" + "location=" + URLEncoder.encode(information, AsyncHttpResponseHandler.DEFAULT_CHARSET)).openConnection();
                httpURLConnection.connect();
                if (new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream())).readLine().equals("OK")) {
                    return true;
                }
                return false;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        } catch (Exception e2) {
            e2.printStackTrace();
            return false;
        }
    }

    private void startLocationManager() {
        this.mLocationManager = (LocationManager) this.mContext.getSystemService(Context.LOCATION_SERVICE);
        LocationListener locationListener = new LocationListener() {
            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            public void onProviderEnabled(String provider) {
            }

            public void onProviderDisabled(String provider) {
            }

            public void onLocationChanged(Location location) {
                if (location != null) {
                    BluetoothIBridgeAdapter.this.mLatitude = location.getLatitude();
                    BluetoothIBridgeAdapter.this.mLongitude = location.getLongitude();
                }
            }
        };
        try {
            LocationManager locationManager = this.mLocationManager;
            LocationManager locationManager2 = this.mLocationManager;
            if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            locationManager.requestLocationUpdates("network", 30000, 0.0f, locationListener);
        } catch (Exception e) {
        }
        try {
            this.mLocationManager.requestLocationUpdates("gps", 30000, 0.0f, locationListener);
        } catch (Exception e2) {
        }
    }

    /* access modifiers changed from: private */
    public String getInformation() {
        if (this.mLatitude == 0.0d && this.mLongitude == 0.0d) {
            try {
                if (this.mLocationManager.isProviderEnabled("gps")) {
                    if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        return "";
                    }
                    Location location = this.mLocationManager.getLastKnownLocation("gps");
                    if (location != null) {
                        this.mLatitude = location.getLatitude();
                        this.mLongitude = location.getLongitude();
                    }
                }
            } catch (Exception e) {
            }
        }
        if (this.mLatitude == 0.0d && this.mLongitude == 0.0d) {
            try {
                if (this.mLocationManager.isProviderEnabled("network")) {
                    Location location2 = this.mLocationManager.getLastKnownLocation("network");
                    if (location2 != null) {
                        this.mLatitude = location2.getLatitude();
                        this.mLongitude = location2.getLongitude();
                    }
                }
            } catch (Exception e2) {
            }
        }
        return this.mLatitude + "/" + this.mLongitude;
    }
}
