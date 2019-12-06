package com.jhl.bluetooth.ibridge;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothSocket;
import android.os.Build.VERSION;
import android.os.Parcel;
import android.os.Parcelable;

import com.jhl.bluetooth.ibridge.Tools.SystemUtils;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.UUID;

public class BluetoothIBridgeDevice implements Parcelable {
    static final String ACTION_PAIRING_CANCEL = "android.bluetooth.device.action.PAIRING_CANCEL";
    static final String ACTION_PAIRING_REQUEST = "android.bluetooth.device.action.PAIRING_REQUEST";
    public static final Creator<BluetoothIBridgeDevice> CREATOR = new Creator<BluetoothIBridgeDevice>() {
        public BluetoothIBridgeDevice createFromParcel(Parcel source) {
            return new BluetoothIBridgeDevice(source);
        }

        public BluetoothIBridgeDevice[] newArray(int size) {
            return new BluetoothIBridgeDevice[size];
        }
    };
    public static int DEVICE_TYPE_BLE = 1;
    public static int DEVICE_TYPE_CLASSIC = 0;
    static final String EXTRA_PAIRING_KEY = "android.bluetooth.device.extra.PAIRING_KEY";
    static final String EXTRA_PAIRING_VARIANT = "android.bluetooth.device.extra.PAIRING_VARIANT";
    static final int PAIRING_VARIANT_CONSENT = 3;
    static final int PAIRING_VARIANT_DISPLAY_PASSKEY = 4;
    static final int PAIRING_VARIANT_DISPLAY_PIN = 5;
    static final int PAIRING_VARIANT_OOB_CONSENT = 6;
    static final int PAIRING_VARIANT_PASSKEY = 1;
    static final int PAIRING_VARIANT_PASSKEY_CONFIRMATION = 2;
    static final int PAIRING_VARIANT_PIN = 0;
    static final UUID SPPUUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
    byte[] buffer;
    int length;
    private BondStatus mBondStatus;
    private ConnectStatus mConnectStatus;
    public BluetoothDevice mDevice;
    private String mDeviceAddress;
    private int mDeviceClass;
    private String mDeviceName;
    private int mDeviceType;
    private Direction mDirection;
    private List<BluetoothGattService> mGattServices;
    private boolean mIsConnected;

    public enum BondStatus {
        STATE_BONDED,
        STATE_BONDING,
        STATE_BONDNONE,
        STATE_BONDFAILED,
        STATE_BOND_OVERTIME,
        STATE_BOND_CANCLED
    }

    public enum ConnectStatus {
        STATUS_DISCONNECTED,
        STATUS_CONNECTED,
        STATUS_DISCONNECTTING,
        STATUS_CONNECTTING,
        STATUS_CONNECTFAILED,
        STATE_BONDED,
        STATE_BONDING,
        STATE_BONDNONE
    }

    enum Direction {
        DIRECTION_NONE,
        DIRECTION_FORWARD,
        DIRECTION_BACKWARD
    }

    BluetoothIBridgeDevice(BluetoothDevice device) {
        this.mDirection = Direction.DIRECTION_NONE;
        this.mConnectStatus = ConnectStatus.STATUS_DISCONNECTED;
        this.mBondStatus = BondStatus.STATE_BONDNONE;
        this.mDeviceAddress = device.getAddress();
        this.mDevice = device;
        this.mDeviceName = this.mDevice.getName();
        BluetoothClass bluetoothClass = null;
        try {
            bluetoothClass = this.mDevice.getBluetoothClass();
        } catch (NullPointerException e) {
        }
        if (bluetoothClass != null) {
            this.mDeviceClass = this.mDevice.getBluetoothClass().getDeviceClass();
        } else {
            this.mDeviceClass = -1;
        }
    }

    public static BluetoothIBridgeDevice createBluetoothIBridgeDevice(String address, int deviceType) {
        return BluetoothIBridgeDeviceFactory.getDefaultFactory().createDevice(address, deviceType);
    }

    private BluetoothIBridgeDevice(Parcel source) {
        this.mDirection = Direction.DIRECTION_NONE;
        this.mConnectStatus = ConnectStatus.STATUS_DISCONNECTED;
        this.mBondStatus = BondStatus.STATE_BONDNONE;
        readFromParcel(source);
    }

    public String getDeviceName() {
        this.mDevice = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(this.mDeviceAddress);
        this.mDeviceName = this.mDevice.getName();
        return this.mDeviceName;
    }

    public String getDeviceAddress() {
        return this.mDeviceAddress;
    }

    public int getDeviceType() {
        return this.mDeviceType;
    }

    public void setDeviceType(int deviceType) {
        this.mDeviceType = deviceType;
    }

    public int getDeviceClass() {
        return this.mDeviceClass;
    }

    public List<BluetoothGattService> getGattServices() {
        return this.mGattServices;
    }

    public String toString() {
        return super.toString() + " [" + (this.mDeviceName == null ? "Device" : this.mDeviceName) + " - " + (this.mDeviceAddress == null ? "00:00:00:00:00:00" : this.mDeviceAddress) + "]";
    }

    public boolean isConnected() {
        return this.mIsConnected;
    }

    public void createBond() {
        try {
            this.mDevice.getClass().getMethod("createBond", null).invoke(this.mDevice, new Object[0]);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e2) {
            e2.printStackTrace();
        } catch (InvocationTargetException e3) {
            e3.printStackTrace();
        }
    }

    public void removeBond() {
        try {
            this.mDevice.getClass().getMethod("removeBond", null).invoke(this.mDevice, new Object[0]);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e2) {
            e2.printStackTrace();
        } catch (InvocationTargetException e3) {
            e3.printStackTrace();
        }
    }

    /* access modifiers changed from: 0000 */
    public boolean isValidDevice() {
        return true;
    }

    public boolean equals(Object o) {
        if (o == null || !(o instanceof BluetoothIBridgeDevice)) {
            return false;
        }
        BluetoothIBridgeDevice dev = (BluetoothIBridgeDevice) o;
        if (!(this.mDeviceAddress == null ? "00:00:00:00:00:00" : this.mDeviceAddress).equals(dev.mDeviceAddress == null ? "00:00:00:00:00:00" : dev.mDeviceAddress) || dev.getDeviceType() != this.mDeviceType) {
            return false;
        }
        return true;
    }

    /* access modifiers changed from: 0000 */
    public BluetoothSocket createSocket() {
        BluetoothSocket socket = null;
        if (VERSION.SDK_INT < 10 || SystemUtils.isMediatekPlatform()) {
            try {
                return this.mDevice.createRfcommSocketToServiceRecord(SPPUUID);
            } catch (IOException e) {
                e.printStackTrace();
                return socket;
            }
        } else {
            Method m = null;
            try {
                m = BluetoothDevice.class.getMethod("createInsecureRfcommSocketToServiceRecord", new Class[]{UUID.class});
            } catch (NoSuchMethodException e2) {
                e2.printStackTrace();
            }
            if (m == null) {
                return socket;
            }
            try {
                return (BluetoothSocket) m.invoke(this.mDevice, new Object[]{SPPUUID});
            } catch (IllegalArgumentException e3) {
                e3.printStackTrace();
                return socket;
            } catch (IllegalAccessException e4) {
                e4.printStackTrace();
                return socket;
            } catch (InvocationTargetException e5) {
                e5.printStackTrace();
                return socket;
            }
        }
    }

    /* access modifiers changed from: 0000 */
    public BluetoothSocket createSocketWithChannel(int channel) {
        BluetoothSocket socket = null;
        Method m = null;
        try {
            m = BluetoothDevice.class.getMethod("createRfcommSocket", new Class[]{Integer.TYPE});
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        if (m == null) {
            return socket;
        }
        try {
            return (BluetoothSocket) m.invoke(this.mDevice, new Object[]{Integer.valueOf(channel)});
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

    /* access modifiers changed from: 0000 */
    public void connected(boolean connected) {
        this.mIsConnected = connected;
    }

    /* access modifiers changed from: 0000 */
    public void setConnectionDirection(Direction d) {
        this.mDirection = d;
    }

    /* access modifiers changed from: 0000 */
    public Direction connectionDirection() {
        return this.mDirection;
    }

    /* access modifiers changed from: 0000 */
    public void setConnectStatus(ConnectStatus d) {
        this.mConnectStatus = d;
    }

    /* access modifiers changed from: 0000 */
    public ConnectStatus getConnectStatus() {
        return this.mConnectStatus;
    }

    /* access modifiers changed from: 0000 */
    public void setBondStatus() {
        if (this.mDevice.getBondState() == 12) {
            this.mBondStatus = BondStatus.STATE_BONDED;
        }
        if (this.mDevice.getBondState() == 11) {
            this.mBondStatus = BondStatus.STATE_BONDING;
        }
        if (this.mDevice.getBondState() == 10) {
            this.mBondStatus = BondStatus.STATE_BONDNONE;
        }
    }

    /* access modifiers changed from: 0000 */
    public void setBondStatus(BondStatus d) {
        this.mBondStatus = d;
    }

    /* access modifiers changed from: 0000 */
    public BondStatus getBondStatus() {
        return this.mBondStatus;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mDeviceName);
        dest.writeString(this.mDeviceAddress);
        dest.writeInt(this.mDeviceClass);
        dest.writeInt(this.mIsConnected ? 1 : 0);
        dest.writeInt(this.mDirection.ordinal());
        dest.writeInt(this.mConnectStatus.ordinal());
        dest.writeInt(this.mBondStatus.ordinal());
    }

    private void readFromParcel(Parcel source) {
        boolean z = true;
        this.mDeviceName = source.readString();
        this.mDeviceAddress = source.readString();
        this.mDeviceClass = source.readInt();
        if (source.readInt() != 1) {
            z = false;
        }
        this.mIsConnected = z;
        int i = source.readInt();
        if (i < Direction.values().length) {
            this.mDirection = Direction.values()[i];
        } else {
            this.mDirection = Direction.DIRECTION_NONE;
        }
        int j = source.readInt();
        ConnectStatus[] cs = ConnectStatus.values();
        if (i < cs.length) {
            this.mConnectStatus = ConnectStatus.values()[j];
        } else {
            this.mConnectStatus = ConnectStatus.STATUS_DISCONNECTED;
        }
        int readInt = source.readInt();
        BondStatus[] values = BondStatus.values();
        if (i < cs.length) {
            this.mBondStatus = BondStatus.values()[j];
        } else {
            this.mBondStatus = BondStatus.STATE_BONDNONE;
        }
        this.mDevice = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(this.mDeviceAddress);
    }

    /* access modifiers changed from: 0000 */
    public void setPin(byte[] pin) {
        try {
            Method setPin = Class.forName(this.mDevice.getClass().getName()).getMethod("setPin", new Class[]{byte[].class});
            setPin.setAccessible(true);
            setPin.invoke(this.mDevice, new Object[]{pin});
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e2) {
            e2.printStackTrace();
        } catch (InvocationTargetException e3) {
            e3.printStackTrace();
        } catch (ClassNotFoundException e4) {
            e4.printStackTrace();
        }
    }

    /* access modifiers changed from: 0000 */
    public void setPasskey(int passkey) {
        try {
            Method setPasskey = Class.forName(this.mDevice.getClass().getName()).getMethod("setPasskey", new Class[]{Integer.TYPE});
            setPasskey.setAccessible(true);
            setPasskey.invoke(this.mDevice, new Object[]{Integer.valueOf(passkey)});
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e2) {
            e2.printStackTrace();
        } catch (InvocationTargetException e3) {
            e3.printStackTrace();
        } catch (ClassNotFoundException e4) {
            e4.printStackTrace();
        }
    }

    /* access modifiers changed from: 0000 */
    public void setPairingConfirmation(boolean confirm) {
        try {
            Method setPairingConfirmation = Class.forName(this.mDevice.getClass().getName()).getMethod("setPairingConfirmation", new Class[]{Boolean.TYPE});
            setPairingConfirmation.setAccessible(true);
            setPairingConfirmation.invoke(this.mDevice, new Object[]{Boolean.valueOf(confirm)});
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e2) {
            e2.printStackTrace();
        } catch (InvocationTargetException e3) {
            e3.printStackTrace();
        } catch (ClassNotFoundException e4) {
            e4.printStackTrace();
        }
    }

    /* access modifiers changed from: 0000 */
    public void cancelPairingUserInput() {
        try {
            this.mDevice.getClass().getMethod("cancelPairingUserInput", null).invoke(this.mDevice, new Object[0]);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e2) {
            e2.printStackTrace();
        } catch (InvocationTargetException e3) {
            e3.printStackTrace();
        }
    }

    /* access modifiers changed from: 0000 */
    public boolean isBonded() {
        if (this.mDevice == null || this.mDevice.getBondState() != 12) {
            return false;
        }
        return true;
    }

    /* access modifiers changed from: 0000 */
    public boolean isSameDevice(BluetoothDevice device, int deviceTye) {
        return (this.mDeviceAddress == null ? "00:00:00:00:00:00" : this.mDeviceAddress).equals(device.getAddress() == null ? "00:00:00:00:00:00" : device.getAddress()) && deviceTye == this.mDeviceType;
    }

    /* access modifiers changed from: 0000 */
    public void setGattServices(List<BluetoothGattService> gattServices) {
        this.mGattServices = gattServices;
    }
}
