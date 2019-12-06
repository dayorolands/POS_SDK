package com.jhl.jhlblueconn;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Binder;
import android.text.TextUtils;
import android.util.Log;

import com.jhl.bluetooth.ibridge.BluetoothIBridgeAdapter;
import com.jhl.bluetooth.ibridge.BluetoothIBridgeAdapter.DataReceiver;
import com.jhl.bluetooth.ibridge.BluetoothIBridgeAdapter.EventReceiver;
import com.jhl.bluetooth.ibridge.BluetoothIBridgeDevice;
import com.jhl.jhlblueconn.ServiceBinder.BluetoothAdapterListener;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.Base64;

import java.nio.charset.Charset;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Formatter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class BluetoothCommmanager implements DataReceiver, EventReceiver {
    public static final int BLUETOOTH_DATA_DATAERROR = 73;
    public static final int BLUE_BONDED = 6;
    public static final int BLUE_BONDING = 5;
    public static final int BLUE_CONNECT_FAIL = 0;
    public static final int BLUE_CONNECT_SUCESS = 1;
    public static final int BLUE_DEVICE_ERROR = 72;
    public static final int BLUE_DISCONNECTD = 2;
    public static final int BLUE_POWER_OFF = 70;
    public static final int BLUE_POWER_STATE_OFF = 4;
    public static final int BLUE_POWER_STATE_ON = 3;
    public static final int BLUE_SCAN_NODEVICE = -1;
    public static final int BLUE_SDK_ERROR = 71;
    public static final int BLUE_WRITE_FAILD = 7;
    public static final int BRUSHDATA = 18;
    public static final int Battery = 69;
    public static final int CHECK_IC = 36;
    public static final int CHOICECMD_CA_PUBLIC_KEY_GET_LIST_RID = 96;
    public static final int CLEARAID = 58;
    public static final int CMD_CA_PUBLIC_KEY_ADD = 85;
    public static final int CMD_CA_PUBLIC_KEY_DELETE = 86;
    public static final int CMD_CA_PUBLIC_KEY_GET_PARAMS = 97;
    public static final int CMD_CA_PUBLIC_KEY_MODIFY = 87;
    public static final int ClEARPUBKEY = 57;
    public static final String DEVICE_NAME = "device_name";
    public static final int EXCHANGE_NOT_OPEN = -1;
    private static ArrayList<String> FilterList = new ArrayList<>();
    public static final int GETCARDDATA = 32;
    public static final int GETENCARDDATA = 255;
    public static final int GETMAC = 55;
    public static final int GETSNVERSION = 64;
    public static final int GETTERNO = 65;
    public static final int GETTRACKDATA_CMD = 34;
    public static final int ICBRUSH = 24;
    public static final int IC_CLOSE = 21;
    public static final int IC_GETSTATUS = 19;
    public static final int IC_OPEN = 20;
    public static final int IC_WRITEAPDU = 22;
    private static ArrayList<String> List = new ArrayList<>();
    private static int MAC_3DES_DATA = 1;
    private static final int MAC_KEY_ID = 4;
    public static final int MAINKEY = 52;
    private static int MAINKEY_ENCRY_MODEM = 0;
    private static final int MAIN_KEY_ID = 1;
    private static int PASSWORD_ENCRY_MODEM = 0;
    private static final int PASSWORD_INPUT_FLAG = 25;
    public static boolean PrintLog = true;
    public static final int ProofIcParm = 35;
    public static final int RASOrder = 67;
    private static byte[] Readbuffer = new byte[1024];
    public static final int SETAIDPAMR = 51;
    public static final int SETPUBKEYPARM = 50;
    public static final int SETTERNO = 66;
    public static final int SUCCESS = 0;
    public static final int SWIPE_CANCEL = -31;
    public static final int SWIPE_DOWNGRADE = 176;
    public static final int SWIPE_GETMAC_ERROR = 78;
    public static final int SWIPE_ICCARD_INSETR = 177;
    public static final int SWIPE_ICCARD_SWINSETR = 178;
    public static final int SWIPE_IC_FAILD = -29;
    public static final int SWIPE_IC_REMOVE = -26;
    public static final int SWIPE_LOW_POWER = 76;
    public static final int SWIPE_NOICPARM = -28;
    public static final int SWIPE_STOP = 104;
    public static final int SWIPE_SUCESS = 0;
    public static final int SWIPE_TIMEOUT_STOP = -30;
    public static final int SWIPE_WAIT_BRUSH = 179;
    public static final int SWIPREAD = 160;
    public static final String TEST_ACTION = "TEST_ACTION";
    public static final String TOAST = "toast";
    private static final int TRACK_ENCRY_DATA = 0;
    private static int TRACK_ENCRY_MODEM = 0;
    private static int TRACK_MAC_DATA = 3;
    public static final int TRANSKEY = 49;
    private static int WAIT_SCANTIMEO = AsyncHttpClient.DEFAULT_SOCKET_TIMEOUT;
    private static int WAIT_TIMEOUT = 3000;
    public static final int WORKEY = 56;
    private static int WORK_ENCRY_MODEM = 0;
    static boolean bConnDevice = false;
    private static boolean bGetDeviceInfo = false;
    private static boolean bIsEncry = false;
    private static BluetoothIBridgeDevice connectDevice = null;
    private static String hexString = "0123456789ABCDEF";
    private static boolean isProssing = true;
    /* access modifiers changed from: private */
    public static BluetoothIBridgeAdapter mAdapter = null;
    private static ServiceBinder mBinder;
    /* access modifiers changed from: private */
    public static BlueStateListenerCallback mCallBackData = null;
    private static Context mContext;
    /* access modifiers changed from: private */
    public static ArrayList<BluetoothIBridgeDevice> mDevices = null;
    private static ArrayList<BluetoothIBridgeDevice> mDevicesList = null;
    /* access modifiers changed from: private */
    public static Timer mExTimer = null;
    /* access modifiers changed from: private */
    public static Timer mExchangeTimer = null;
    /* access modifiers changed from: private */
    public static BluetoothCommmanager mInstance;
    private static int mRxBytes = 0;
    private static Object mWaitLock = new Object();
    /* access modifiers changed from: private */
    public static int nSacnTypes = 0;
    private static int nTotallen = 0;
    private static BluetoothAdapterListener serviceListener = new BluetoothAdapterListener() {
        public void onBluetoothAdapterDestroyed() {
            BluetoothCommmanager.mInstance.setBluetoothAdapter(null);
        }

        public void onBluetoothAdapterCreated(BluetoothIBridgeAdapter adapter) {
            BluetoothCommmanager.mInstance.setBluetoothAdapter(adapter);
        }
    };
    private static SharedPreferences sp;
    private static int track1DataLen = 0;
    private static int track2DataLen = 0;
    private static int track3DataLen = 0;
    private static int wait_Time = 2;
    private String CeShiData;
    String[] buffer = new String[10];
    Editor editor;
    Map<String, String> myKVMap = new HashMap();

    public class LocalBinder extends Binder {
        public LocalBinder() {
        }

        /* access modifiers changed from: 0000 */
        public BluetoothIBridgeAdapter getBluetoothAdapter() {
            return BluetoothCommmanager.mAdapter;
        }
    }

    private static byte toByte(char c) {
        return (byte) "0123456789ABCDEF".indexOf(c);
    }

    public static String str2HexStr(String str) {
        char[] chars = "0123456789ABCDEF".toCharArray();
        StringBuilder sb = new StringBuilder("");
        byte[] bs = str.getBytes();
        for (int i = 0; i < bs.length; i++) {
            sb.append(chars[(bs[i] & 240) >> 4]);
            sb.append(chars[bs[i] & 15]);
            sb.append(' ');
        }
        return sb.toString().trim();
    }

    private static byte[] hexStringToByte(String hex) {
        int len = hex.length() / 2;
        byte[] result = new byte[len];
        char[] achar = hex.toCharArray();
        for (int i = 0; i < len; i++) {
            int pos = i * 2;
            result[i] = (byte) ((toByte(achar[pos]) << 4) | toByte(achar[pos + 1]));
        }
        return result;
    }

    private static byte[] hexStr2Bytes(String src) {
        int l = src.length() / 2;
        System.out.println(l);
        byte[] ret = new byte[l];
        for (int i = 0; i < l; i++) {
            int m = (i * 2) + 1;
            ret[i] = Integer.decode("0x" + src.substring(i * 2, m) + src.substring(m, m + 1)).byteValue();
        }
        return ret;
    }

    /* access modifiers changed from: private */
    public void setBluetoothAdapter(BluetoothIBridgeAdapter adapter) {
        if (adapter != null) {
            mAdapter.registerEventReceiver(mInstance);
        } else if (mAdapter != null) {
            mAdapter.unregisterEventReceiver(mInstance);
            mAdapter = null;
        }
    }

    public static BluetoothCommmanager getInstance(BlueStateListenerCallback cb, Context context) {
        mContext = context.getApplicationContext();
        if (mInstance == null) {
            mInstance = new BluetoothCommmanager();
        }
        mCallBackData = cb;
        mBinder = new ServiceBinder(mContext);
        mAdapter = BluetoothIBridgeAdapter.sharedInstance(mContext);
        if (!mAdapter.isEnabled()) {
            mAdapter.setEnabled(true);
        }
        mAdapter.registerDataReceiver(mInstance);
        mAdapter.registerEventReceiver(mInstance);
        if (mDevices == null) {
            mDevices = new ArrayList<>();
        }
        if (mDevicesList == null) {
            mDevicesList = new ArrayList<>();
        }
        mBinder.registerBluetoothAdapterListener(serviceListener);
        mBinder.doBindService();
        mRxBytes = 0;
        nTotallen = 0;
        Arrays.fill(Readbuffer, (byte) 0);
        sp = context.getSharedPreferences("tradeInfo", Context.MODE_PRIVATE);
        return mInstance;
    }

    public static boolean SetEncryMode(int CardEncryMode, int MacEncryMode, int MacType) {
        return true;
    }

    private void clearDevices() {
        if (mDevices != null) {
            ArrayList<BluetoothIBridgeDevice> newList = new ArrayList<>();
            Iterator<BluetoothIBridgeDevice> it = mDevices.iterator();
            while (it.hasNext()) {
                BluetoothIBridgeDevice d = (BluetoothIBridgeDevice) it.next();
                if (d != null && d.isConnected()) {
                    newList.add(d);
                }
            }
            if (newList != null) {
                synchronized (mDevices) {
                    mDevices = newList;
                }
                return;
            }
            return;
        }
        mDevices = new ArrayList<>();
    }

    public int scanDevice(String[] namFilter, int nScanType) {
        mRxBytes = 0;
        nTotallen = 0;
        Arrays.fill(Readbuffer, (byte) 0);
        if (mAdapter != null && !mAdapter.isEnabled()) {
            mAdapter.setEnabled(true);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if (mExTimer != null) {
            mExTimer.cancel();
            mExTimer.purge();
            mExTimer = null;
        }
        clearDevices();
        FilterList.clear();
        if (namFilter != null) {
            for (String str : namFilter) {
                FilterList.add(str);
            }
        }
        WAIT_SCANTIMEO = 5000;
        mExTimer = new Timer(true);
        mExTimer.schedule(new TimerTask() {
            public void run() {
                if (BluetoothCommmanager.nSacnTypes == 0 && BluetoothCommmanager.mCallBackData != null) {
                    BluetoothCommmanager.mCallBackData.onDeviceFound(BluetoothCommmanager.mDevices);
                }
                BluetoothCommmanager.mAdapter.stopDiscovery();
                if (BluetoothCommmanager.mExTimer != null) {
                    BluetoothCommmanager.mExTimer.cancel();
                    BluetoothCommmanager.mExTimer = null;
                }
            }
        }, (long) WAIT_SCANTIMEO);
        nSacnTypes = nScanType;
        if (mAdapter != null) {
            mAdapter.startDiscovery();
        }
        return 0;
    }

    public int StopScanDevice() {
        if (mAdapter != null) {
            mAdapter.stopDiscovery();
        }
        if (mExTimer != null) {
            mExTimer.cancel();
            mExTimer = null;
            if (nSacnTypes == 0 && mCallBackData != null) {
                mCallBackData.onDeviceFound(mDevices);
            }
        }
        return 0;
    }

    public int connectDevice(String address) {
        bIsEncry = false;
        if (bConnDevice) {
            if (mCallBackData != null) {
                mCallBackData.onBluetoothConnected();
            }
        } else if (mAdapter == null) {
            return -1;
        } else {
            if (mCallBackData != null) {
                mCallBackData.onBluetoothIng();
            }
            BluetoothIBridgeDevice device = BluetoothIBridgeDevice.createBluetoothIBridgeDevice(address, BluetoothIBridgeDevice.DEVICE_TYPE_CLASSIC);
            connectDevice = device;
            mAdapter.connectDevice(device);
        }
        return 0;
    }

    private boolean deviceExisted(BluetoothIBridgeDevice device) {
        if (device == null) {
            return false;
        }
        Iterator<BluetoothIBridgeDevice> it = mDevices.iterator();
        while (it.hasNext()) {
            BluetoothIBridgeDevice d = (BluetoothIBridgeDevice) it.next();
            if (d != null && d.equals(device)) {
                System.out.println("d+++++++" + d);
                return true;
            }
        }
        return false;
    }

    private boolean deviceFileter(BluetoothIBridgeDevice device) {
        if (device == null || FilterList.size() == 0 || FilterList == null) {
            return false;
        }
        Iterator it = FilterList.iterator();
        while (it.hasNext()) {
            String strName = (String) it.next();
            if (strName.length() <= 0) {
                return false;
            }
            if (device.getDeviceName() != null && device.getDeviceAddress() != null && device.getDeviceName().length() != 0 && device.getDeviceName().length() >= strName.length() && device != null && device.getDeviceName().toUpperCase().subSequence(0, strName.length()).equals(strName.toUpperCase())) {
                return false;
            }
        }
        return true;
    }

    public void onDiscoveryFinished() {
    }

    public int disConnectBluetoothDevice() {
        if (bConnDevice) {
            if (mAdapter != null) {
                byte[] cmd = {0, 2, -96, 0, -94};
                mAdapter.send(connectDevice, cmd, cmd.length);
                mAdapter.disconnectDevice(connectDevice);
            }
            bConnDevice = false;
        } else if (mCallBackData != null) {
            mCallBackData.onBluetoothDisconnected();
        }
        return 0;
    }

    public void onDeviceFound(BluetoothIBridgeDevice device) {
        if (PrintLog) {
            System.out.println("搜索到: ===" + device.getDeviceName() + "===" + device.getDeviceAddress());
        }
        if (!deviceExisted(device)) {
            synchronized (mDevices) {
                if (!deviceFileter(device) && device.getDeviceName() != null) {
                    mDevices.add(device);
                    if (nSacnTypes == 1) {
                        mDevicesList.clear();
                        mDevicesList.add(device);
                        if (mCallBackData != null) {
                            mCallBackData.onDeviceFound(mDevicesList);
                        }
                    }
                    if (PrintLog) {
                        System.out.println("增加到返回列表: ===" + device.getDeviceName() + "===" + device.getDeviceAddress());
                    }
                }
            }
            return;
        }
        if (device.getDeviceName() == null) {
        }
    }

    public void onDeviceConnected(BluetoothIBridgeDevice paramBluetoothIBridgeDevice) {
        connectDevice = paramBluetoothIBridgeDevice;
        if (PrintLog) {
            System.out.println("连接成功 " + paramBluetoothIBridgeDevice.getDeviceName() + "===" + paramBluetoothIBridgeDevice.getDeviceAddress());
        }
        if (mCallBackData != null) {
            bConnDevice = true;
            mCallBackData.onBluetoothConnected();
        }
    }

    public void onDeviceDisconnected(BluetoothIBridgeDevice paramBluetoothIBridgeDevice, String paramString) {
        if (mCallBackData != null) {
            mCallBackData.onBluetoothDisconnected();
            bConnDevice = false;
        }
        if (PrintLog) {
            System.out.println("设备断开 " + paramBluetoothIBridgeDevice.getDeviceName() + "===" + paramBluetoothIBridgeDevice.getDeviceAddress());
        }
        bConnDevice = false;
    }

    public void onDeviceConnectFailed(BluetoothIBridgeDevice paramBluetoothIBridgeDevice, String paramString) {
        if (PrintLog) {
            System.out.println("连接失败 " + paramBluetoothIBridgeDevice.getDeviceName() + "===" + paramBluetoothIBridgeDevice.getDeviceAddress());
        }
        if (mCallBackData != null) {
            bConnDevice = false;
            mCallBackData.onBluetoothConnectedFail();
        }
        bConnDevice = false;
    }

    public void onWriteFailed(BluetoothIBridgeDevice paramBluetoothIBridgeDevice, String paramString) {
        if (mCallBackData != null) {
            mCallBackData.onError(7, "Bluetooth data transmission failed!");
        }
    }

    public void onLeServiceDiscovered(BluetoothIBridgeDevice paramBluetoothIBridgeDevice, String paramString) {
    }

    public void onBluetoothOff() {
        if (mCallBackData != null) {
            mCallBackData.onBluetoothPowerOff();
        }
    }

    public void onBluetoothOn() {
        if (mCallBackData != null) {
            mCallBackData.onBluetoothPowerOn();
            if (mAdapter != null) {
                mAdapter.disconnectDevice(connectDevice);
            }
            bConnDevice = false;
        }
    }

    public void onDeviceBondNone(BluetoothIBridgeDevice arg0) {
    }

    public void onDeviceBonded(BluetoothIBridgeDevice arg0) {
    }

    public void onDeviceBonding(BluetoothIBridgeDevice arg0) {
    }

    public static String toStringHex(String s) {
        byte[] baKeyword = new byte[(s.length() / 2)];
        for (int i = 0; i < baKeyword.length; i++) {
            try {
                baKeyword[i] = (byte) (Integer.parseInt(s.substring(i * 2, (i * 2) + 2), 16) & 255);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        try {
            return new String(baKeyword, "utf-8");
        } catch (Exception e1) {
            e1.printStackTrace();
            return s;
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:31:0x014b  */
    /* JADX WARNING: Removed duplicated region for block: B:37:0x0171  */
    /* JADX WARNING: Removed duplicated region for block: B:45:0x01af  */
    public synchronized int StandbySALEBrushCard(byte[] dataTemp) {
        int i = 0;
        String sTemp;
        Map<String, String> hashmap = new HashMap<>();
        hashmap.clear();
        byte[] data = new byte[1024];
        Arrays.fill(data, (byte) 0);
        byte[] Tempdata = new byte[(dataTemp.length - 4)];
        byte[] Outdata = new byte[(dataTemp.length + 128)];
        String sTemp2 = "";
        String str = "";
        String str2 = "";
        String str3 = "";
        String str4 = "";
        String str5 = "";
        int nTotalLen = dataTemp.length;
        StringBuilder sb = new StringBuilder();
        if (!bIsEncry) {
            System.arraycopy(dataTemp, 0, data, 0, dataTemp.length);
            if (PrintLog) {
            }
            sb.setLength(0);
            int nIndex = 0 + 1 + 1;
            if (nTotalLen >= 4) {
            }
        } else if (!BlueEncryData.isReady) {
            if (mCallBackData != null) {
                mCallBackData.onError(71, "not allow to retransmit as is waiting for data reception!");
            }
            i = 0;
        } else {
            System.arraycopy(dataTemp, 0, data, 0, 2);
            Arrays.fill(Outdata, (byte) 0);
            System.arraycopy(dataTemp, 2, Tempdata, 0, dataTemp.length - 4);
            int nRet = BlueEncryData.EnDesData(Tempdata, Tempdata.length, Outdata, 255, 0);
            byte[] Bufferdata = new byte[nRet];
            System.arraycopy(Outdata, 0, Bufferdata, 0, nRet);
            System.arraycopy(Outdata, 0, data, 2, nRet);
            byte nLrc = LrcCompute(Bufferdata, nRet);
            if (PrintLog) {
                for (int i2 = 0; i2 < nRet; i2++) {
                    sb.append(String.format("%02x", new Object[]{Byte.valueOf(Bufferdata[i2])}));
                }
                Log.e("Bufferdata", sb.toString());
            }
            sb.setLength(0);
            if (nLrc != dataTemp[dataTemp.length - 2]) {
                if (mCallBackData != null) {
                    mCallBackData.onError(72, "device error as it is incompatible with the SDK");
                }
                i = 0;
            } else {
                System.arraycopy(dataTemp, dataTemp.length - 2, data, Outdata.length + 2, 2);
                if (PrintLog) {
                    for (byte valueOf : data) {
                        sb.append(String.format("%02x", new Object[]{Byte.valueOf(valueOf)}));
                    }
                    Log.e("StandbySALEBrushCard", sb.toString());
                }
                sb.setLength(0);
                int nIndex2 = 0 + 1 + 1;
                if (nTotalLen >= 4) {
                    i = 2;
                } else {
                    for (int i3 = 0; i3 < 2; i3++) {
                        sTemp2 = new StringBuilder(String.valueOf(sTemp2)).append(String.format("%02x", new Object[]{Byte.valueOf(data[i3 + 2])})).toString();
                    }
                    int nIndex3 = nIndex2 + 1 + 1;
                    String EntryMode = sTemp2;
                    String sTemp3 = "";
                    if (nTotalLen < 5) {
                        i = 2;
                    } else {
                        String sTemp4 = "";
                        if (nTotalLen < 5) {
                            i = 2;
                        } else {
                            track2DataLen = data[nIndex3] & 255;
                            track2DataLen <<= 8;
                            track2DataLen |= data[5] & 255;
                            int nIndex4 = nIndex3 + 1 + 1;
                            if (nTotalLen < track2DataLen + 6) {
                                i = 2;
                            } else {
                                for (int i4 = 0; i4 < track2DataLen; i4++) {
                                    sTemp4 = new StringBuilder(String.valueOf(sTemp4)).append(String.format("%02x", new Object[]{Byte.valueOf(data[i4 + 6])})).toString();
                                }
                                hashmap.put("EncryptTrack2", sTemp4);
                                String sTemp5 = "";
                                int nIndex5 = track2DataLen + 6;
                                if (nTotalLen < nIndex5 + 1) {
                                    i = 2;
                                } else {
                                    track3DataLen = data[nIndex5] & 255;
                                    track3DataLen <<= 8;
                                    track3DataLen |= data[nIndex5 + 1] & 255;
                                    int nIndex6 = nIndex5 + 1 + 1;
                                    if (nTotalLen < track3DataLen + nIndex6) {
                                        i = 2;
                                    } else {
                                        for (int i5 = 0; i5 < track3DataLen; i5++) {
                                            sTemp5 = new StringBuilder(String.valueOf(sTemp5)).append(String.format("%02x", new Object[]{Byte.valueOf(data[nIndex6 + i5])})).toString();
                                        }
                                        hashmap.put("EncryptTrack3", sTemp5);
                                        String sTemp6 = "";
                                        int nIndex7 = nIndex6 + track3DataLen;
                                        if (nTotalLen < nIndex7 + 2) {
                                            i = 2;
                                        } else {
                                            byte b = (byte) (((data[nIndex7] & 255) << 8) | (data[nIndex7 + 1] & 255));
                                            if (b > 1024) {
                                                i = 2;
                                            } else {
                                                int nIndex8 = nIndex7 + 1 + 1;
                                                if (nTotalLen < b + nIndex8) {
                                                    i = 2;
                                                } else {
                                                    for (int i6 = 0; i6 < b; i6++) {
                                                        sTemp6 = new StringBuilder(String.valueOf(sTemp6)).append(String.format("%02x", new Object[]{Byte.valueOf(data[nIndex8 + i6])})).toString();
                                                    }
                                                    hashmap.put("ICData55", sTemp6);
                                                    String sTemp7 = "";
                                                    int nIndex9 = nIndex8 + b;
                                                    byte b2 = (byte) (((data[nIndex9] & 255) << 8) | (data[nIndex9 + 1] & 255));
                                                    if (nTotalLen < nIndex9 + 9) {
                                                        i = 2;
                                                    } else if (b2 > 1024) {
                                                        i = 2;
                                                    } else {
                                                        int nIndex10 = nIndex9 + 1 + 1;
                                                        for (int i7 = 0; i7 < b2; i7++) {
                                                            sTemp7 = new StringBuilder(String.valueOf(sTemp7)).append(String.format("%02x", new Object[]{Byte.valueOf(data[nIndex10 + i7])})).toString();
                                                        }
                                                        if (EntryMode.equals("0210") || EntryMode.equals("0510") || EntryMode.equals("0710")) {
                                                            hashmap.put("PINBlock", sTemp7);
                                                        } else {
                                                            hashmap.put("PINBlock", "ffffffffffffffff");
                                                        }
                                                        if (EntryMode.subSequence(0, 2).equals("02")) {
                                                            hashmap.put("SzEntryMode", "0");
                                                        } else if (EntryMode.subSequence(0, 2).equals("05")) {
                                                            hashmap.put("SzEntryMode", "1");
                                                        } else if (EntryMode.subSequence(0, 2).equals("07")) {
                                                            hashmap.put("SzEntryMode", "2");
                                                        }
                                                        int nIndex11 = nIndex10 + b2;
                                                        if (nTotalLen < nIndex11 + 1) {
                                                            i = 2;
                                                        } else {
                                                            byte nDatalen = data[nIndex11];
                                                            int nIndex12 = nIndex11 + 1;
                                                            String sTemp8 = "";
                                                            for (int i8 = 0; i8 < nDatalen; i8++) {
                                                                sTemp8 = new StringBuilder(String.valueOf(sTemp8)).append(String.format("%02x", new Object[]{Byte.valueOf(data[nIndex12 + i8])})).toString();
                                                            }
                                                            if (sTemp8.toLowerCase().equals("ff")) {
                                                                hashmap.put("PanSeqNo", "f" + sTemp8);
                                                            } else {
                                                                hashmap.put("PanSeqNo", "0" + sTemp8);
                                                            }
                                                            int nIndex13 = nIndex12 + nDatalen;
                                                            if (nTotalLen < nIndex13 + 1) {
                                                                if (mCallBackData != null) {
                                                                    mCallBackData.onReadCardData(hashmap);
                                                                }
                                                                i = 2;
                                                            } else {
                                                                byte nDatalen2 = data[nIndex13];
                                                                int nIndex14 = nIndex13 + 1;
                                                                String sTemp9 = "";
                                                                if (nTotalLen < nIndex14 + nDatalen2) {
                                                                    if (mCallBackData != null) {
                                                                        mCallBackData.onReadCardData(hashmap);
                                                                    }
                                                                    i = 2;
                                                                } else {
                                                                    for (int i9 = 0; i9 < nDatalen2; i9++) {
                                                                        sTemp9 = new StringBuilder(String.valueOf(sTemp9)).append(String.format("%02x", new Object[]{Byte.valueOf(data[nIndex14 + i9])})).toString();
                                                                    }
                                                                    hashmap.put("Amount", new DecimalFormat("0.00").format((double) (Float.parseFloat(toStringHex(sTemp9)) / 100.0f)));
                                                                    int nIndex15 = nIndex14 + nDatalen2;
                                                                    if (nTotalLen < nIndex15 + 1) {
                                                                        if (mCallBackData != null) {
                                                                            mCallBackData.onReadCardData(hashmap);
                                                                        }
                                                                        i = 2;
                                                                    } else {
                                                                        String sTemp10 = "";
                                                                        for (int i10 = 0; i10 < 2; i10++) {
                                                                            sTemp10 = new StringBuilder(String.valueOf(sTemp10)).append(String.format("%02x", new Object[]{Byte.valueOf(data[nIndex15 + i10])})).toString();
                                                                        }
                                                                        String midStemp = sTemp10;
                                                                        int Midtrack2DataLen = Integer.valueOf(sTemp10).intValue();
                                                                        int Midtrack2DataLen1 = Integer.valueOf("0130").intValue() / 2;
                                                                        byte[] dat = {(byte) (Midtrack2DataLen1 / 100), (byte) (Midtrack2DataLen1 % 100)};
                                                                        StringBuffer sbuf = new StringBuffer();
                                                                        for (int i11 = 0; i11 < dat.length; i11++) {
                                                                            sbuf.append(dat[i11]);
                                                                        }
                                                                        String stringBuffer = sbuf.toString();
                                                                        int nIndex16 = nIndex15 + 1 + 1;
                                                                        for (int i12 = 0; i12 < Midtrack2DataLen; i12++) {
                                                                            sTemp10 = new StringBuilder(String.valueOf(sTemp10)).append(String.format("%02x", new Object[]{Byte.valueOf(data[nIndex16 + i12])})).toString();
                                                                        }
                                                                        hashmap.put("Tag59", new StringBuilder(String.valueOf(midStemp)).append(sTemp10.substring(4, sTemp10.length())).toString());
                                                                        int nIndex17 = nIndex16 + nDatalen2;
                                                                        if (nTotalLen < nIndex17 + 1) {
                                                                            if (mCallBackData != null) {
                                                                                mCallBackData.onReadCardData(hashmap);
                                                                            }
                                                                            i = 2;
                                                                        } else {
                                                                            if (PASSWORD_ENCRY_MODEM == 3) {
                                                                                byte nDatalen3 = data[nIndex17];
                                                                                int nIndex18 = nIndex17 + 1;
                                                                                String sTemp11 = "";
                                                                                for (int i13 = 0; i13 < nDatalen3; i13++) {
                                                                                    sTemp11 = new StringBuilder(String.valueOf(sTemp11)).append(String.format("%02x", new Object[]{Byte.valueOf(data[nIndex18 + i13])})).toString();
                                                                                }
                                                                                byte nPassLen = data[nIndex18];
                                                                                if (nPassLen > 0) {
                                                                                    sTemp = sTemp11.substring(2, nPassLen + 2);
                                                                                } else {
                                                                                    sTemp = "ffffffff";
                                                                                }
                                                                                hashmap.put("AsciiPwd", sTemp);
                                                                                int nIndex19 = nIndex18 + nDatalen3;
                                                                                if (nTotalLen < nIndex19 + 1) {
                                                                                    if (mCallBackData != null) {
                                                                                        mCallBackData.onReadCardData(hashmap);
                                                                                    }
                                                                                    i = 2;
                                                                                } else {
                                                                                    byte nDatalen4 = data[nIndex19];
                                                                                    int nIndex20 = nIndex19 + 1;
                                                                                    String sTemp12 = "";
                                                                                    for (int i14 = 0; i14 < nDatalen4; i14++) {
                                                                                        sTemp12 = new StringBuilder(String.valueOf(sTemp12)).append(String.format("%02x", new Object[]{Byte.valueOf(data[nIndex20 + i14])})).toString();
                                                                                    }
                                                                                    hashmap.put("SnData", toStringHex(sTemp12));
                                                                                    nIndex17 = nIndex20 + nDatalen4;
                                                                                    if (nTotalLen < nIndex17 + 1) {
                                                                                        if (mCallBackData != null) {
                                                                                            mCallBackData.onReadCardData(hashmap);
                                                                                        }
                                                                                        i = 2;
                                                                                    }
                                                                                }
                                                                            }
                                                                            if (TRACK_ENCRY_MODEM == 5 || TRACK_ENCRY_MODEM == 8) {
                                                                                byte nDatalen5 = data[nIndex17];
                                                                                int nIndex21 = nIndex17 + 1;
                                                                                String sTemp13 = "";
                                                                                for (int i15 = 0; i15 < nDatalen5; i15++) {
                                                                                    sTemp13 = new StringBuilder(String.valueOf(sTemp13)).append(String.format("%02x", new Object[]{Byte.valueOf(data[nIndex21 + i15])})).toString();
                                                                                }
                                                                                hashmap.put("Random", sTemp13);
                                                                                int nIndex22 = nIndex21 + nDatalen5;
                                                                            }
                                                                            if (mCallBackData != null) {
                                                                                mCallBackData.onReadCardData(hashmap);
                                                                            }
                                                                            i = 0;
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return i;
    }

    /* JADX WARNING: Removed duplicated region for block: B:31:0x015c  */
    /* JADX WARNING: Removed duplicated region for block: B:36:0x0178  */
    public synchronized int StandbySALEBrushCard1(byte[] dataTemp) {
        int i = 0;
        String sTemp;
        Map<String, String> hashmap = new HashMap<>();
        hashmap.clear();
        byte[] data = new byte[1024];
        Arrays.fill(data, (byte) 0);
        byte[] Tempdata = new byte[(dataTemp.length - 4)];
        byte[] Outdata = new byte[(dataTemp.length + 128)];
        String sTemp2 = "";
        String str = "";
        String str2 = "";
        String str3 = "";
        String str4 = "";
        String str5 = "";
        int nTotalLen = dataTemp.length;
        StringBuilder sb = new StringBuilder();
        if (!bIsEncry) {
            System.arraycopy(dataTemp, 0, data, 0, dataTemp.length);
            sb.setLength(0);
            int nIndex = 0 + 1 + 1;
            if (nTotalLen >= 4) {
            }
        } else if (!BlueEncryData.isReady) {
            if (mCallBackData != null) {
                mCallBackData.onError(71, "not allow to retransmit as is waiting for data reception!");
            }
            i = 0;
        } else {
            System.arraycopy(dataTemp, 0, data, 0, 2);
            Arrays.fill(Outdata, (byte) 0);
            System.arraycopy(dataTemp, 2, Tempdata, 0, dataTemp.length - 4);
            int nRet = BlueEncryData.EnDesData(Tempdata, Tempdata.length, Outdata, 255, 0);
            byte[] Bufferdata = new byte[nRet];
            System.arraycopy(Outdata, 0, Bufferdata, 0, nRet);
            System.arraycopy(Outdata, 0, data, 2, nRet);
            byte nLrc = LrcCompute(Bufferdata, nRet);
            if (PrintLog) {
                for (int i2 = 0; i2 < nRet; i2++) {
                    sb.append(String.format("%02x", new Object[]{Byte.valueOf(Bufferdata[i2])}));
                }
                Log.e("Bufferdata", sb.toString());
            }
            sb.setLength(0);
            if (nLrc != dataTemp[dataTemp.length - 2]) {
                if (mCallBackData != null) {
                    mCallBackData.onError(72, "device error as it is incompatible with the SDK");
                }
                i = 0;
            } else {
                System.arraycopy(dataTemp, dataTemp.length - 2, data, Outdata.length + 2, 2);
                sb.setLength(0);
                int nIndex2 = 0 + 1 + 1;
                if (nTotalLen >= 4) {
                    i = 2;
                } else {
                    for (int i3 = 0; i3 < 2; i3++) {
                        sTemp2 = new StringBuilder(String.valueOf(sTemp2)).append(String.format("%02x", new Object[]{Byte.valueOf(data[i3 + 2])})).toString();
                    }
                    int nIndex3 = nIndex2 + 1 + 1;
                    String EntryMode = sTemp2;
                    String sTemp3 = "";
                    if (nTotalLen < 5) {
                        i = 2;
                    } else {
                        String sTemp4 = "";
                        if (nTotalLen < 5) {
                            i = 2;
                        } else {
                            track2DataLen = data[nIndex3] & 255;
                            track2DataLen <<= 8;
                            track2DataLen |= data[5] & 255;
                            int nIndex4 = nIndex3 + 1 + 1;
                            if (nTotalLen < track2DataLen + 6) {
                                i = 2;
                            } else {
                                for (int i4 = 0; i4 < track2DataLen; i4++) {
                                    sTemp4 = new StringBuilder(String.valueOf(sTemp4)).append(String.format("%02x", new Object[]{Byte.valueOf(data[i4 + 6])})).toString();
                                }
                                hashmap.put("EncryptTrack2", toStringHex(sTemp4));
                                String sTemp5 = "";
                                int nIndex5 = track2DataLen + 6;
                                if (nTotalLen < nIndex5 + 1) {
                                    i = 2;
                                } else {
                                    track3DataLen = data[nIndex5] & 255;
                                    track3DataLen <<= 8;
                                    track3DataLen |= data[nIndex5 + 1] & 255;
                                    int nIndex6 = nIndex5 + 1 + 1;
                                    if (nTotalLen < track3DataLen + nIndex6) {
                                        i = 2;
                                    } else {
                                        for (int i5 = 0; i5 < track3DataLen; i5++) {
                                            sTemp5 = new StringBuilder(String.valueOf(sTemp5)).append(String.format("%02x", new Object[]{Byte.valueOf(data[nIndex6 + i5])})).toString();
                                        }
                                        hashmap.put("EncryptTrack3", toStringHex(sTemp5));
                                        String sTemp6 = "";
                                        int nIndex7 = nIndex6 + track3DataLen;
                                        if (nTotalLen < nIndex7 + 2) {
                                            i = 2;
                                        } else {
                                            byte b = (byte) (((data[nIndex7] & 255) << 8) | (data[nIndex7 + 1] & 255));
                                            if (b > 1024) {
                                                i = 2;
                                            } else {
                                                int nIndex8 = nIndex7 + 1 + 1;
                                                if (nTotalLen < b + nIndex8) {
                                                    i = 2;
                                                } else {
                                                    for (int i6 = 0; i6 < b; i6++) {
                                                        sTemp6 = new StringBuilder(String.valueOf(sTemp6)).append(String.format("%02x", new Object[]{Byte.valueOf(data[nIndex8 + i6])})).toString();
                                                    }
                                                    hashmap.put("ICData55", sTemp6);
                                                    String sTemp7 = "";
                                                    int nIndex9 = nIndex8 + b;
                                                    byte b2 = (byte) (((data[nIndex9] & 255) << 8) | (data[nIndex9 + 1] & 255));
                                                    if (nTotalLen < nIndex9 + 9) {
                                                        i = 2;
                                                    } else if (b2 > 1024) {
                                                        i = 2;
                                                    } else {
                                                        int nIndex10 = nIndex9 + 1 + 1;
                                                        for (int i7 = 0; i7 < b2; i7++) {
                                                            sTemp7 = new StringBuilder(String.valueOf(sTemp7)).append(String.format("%02x", new Object[]{Byte.valueOf(data[nIndex10 + i7])})).toString();
                                                        }
                                                        if (EntryMode.equals("0210") || EntryMode.equals("0510") || EntryMode.equals("0710")) {
                                                            hashmap.put("PINBlock", sTemp7);
                                                        } else {
                                                            hashmap.put("PINBlock", "ffffffffffffffff");
                                                        }
                                                        if (EntryMode.subSequence(0, 2).equals("02")) {
                                                            hashmap.put("SzEntryMode", "0");
                                                        } else if (EntryMode.subSequence(0, 2).equals("05")) {
                                                            hashmap.put("SzEntryMode", "1");
                                                        } else if (EntryMode.subSequence(0, 2).equals("07")) {
                                                            hashmap.put("SzEntryMode", "2");
                                                        }
                                                        int nIndex11 = nIndex10 + b2;
                                                        if (nTotalLen < nIndex11 + 1) {
                                                            i = 2;
                                                        } else {
                                                            byte nDatalen = data[nIndex11];
                                                            int nIndex12 = nIndex11 + 1;
                                                            String sTemp8 = "";
                                                            for (int i8 = 0; i8 < nDatalen; i8++) {
                                                                sTemp8 = new StringBuilder(String.valueOf(sTemp8)).append(String.format("%02x", new Object[]{Byte.valueOf(data[nIndex12 + i8])})).toString();
                                                            }
                                                            if (sTemp8.toLowerCase().equals("ff")) {
                                                                hashmap.put("PanSeqNo", "f" + sTemp8);
                                                            } else {
                                                                hashmap.put("PanSeqNo", "0" + sTemp8);
                                                            }
                                                            int nIndex13 = nIndex12 + nDatalen;
                                                            if (nTotalLen < nIndex13 + 1) {
                                                                if (mCallBackData != null) {
                                                                    mCallBackData.onReadCardData(hashmap);
                                                                }
                                                                i = 2;
                                                            } else {
                                                                byte nDatalen2 = data[nIndex13];
                                                                int nIndex14 = nIndex13 + 1;
                                                                String sTemp9 = "";
                                                                if (nTotalLen < nIndex14 + nDatalen2) {
                                                                    if (mCallBackData != null) {
                                                                        mCallBackData.onReadCardData(hashmap);
                                                                    }
                                                                    i = 2;
                                                                } else {
                                                                    for (int i9 = 0; i9 < nDatalen2; i9++) {
                                                                        sTemp9 = new StringBuilder(String.valueOf(sTemp9)).append(String.format("%02x", new Object[]{Byte.valueOf(data[nIndex14 + i9])})).toString();
                                                                    }
                                                                    hashmap.put("Amount", new DecimalFormat("0.00").format((double) (Float.parseFloat(toStringHex(sTemp9)) / 100.0f)));
                                                                    int nIndex15 = nIndex14 + nDatalen2;
                                                                    if (nTotalLen < nIndex15 + 1) {
                                                                        if (mCallBackData != null) {
                                                                            mCallBackData.onReadCardData(hashmap);
                                                                        }
                                                                        i = 2;
                                                                    } else {
                                                                        String sTemp10 = "";
                                                                        for (int i10 = 0; i10 < 2; i10++) {
                                                                            sTemp10 = new StringBuilder(String.valueOf(sTemp10)).append(String.format("%02x", new Object[]{Byte.valueOf(data[nIndex15 + i10])})).toString();
                                                                        }
                                                                        String midStemp = sTemp10;
                                                                        int Midtrack2DataLen = Integer.valueOf(sTemp10).intValue();
                                                                        int Midtrack2DataLen1 = Integer.valueOf("0130").intValue() / 2;
                                                                        byte[] dat = {(byte) (Midtrack2DataLen1 / 100), (byte) (Midtrack2DataLen1 % 100)};
                                                                        StringBuffer sbuf = new StringBuffer();
                                                                        for (int i11 = 0; i11 < dat.length; i11++) {
                                                                            sbuf.append(dat[i11]);
                                                                        }
                                                                        String stringBuffer = sbuf.toString();
                                                                        int nIndex16 = nIndex15 + 1 + 1;
                                                                        for (int i12 = 0; i12 < Midtrack2DataLen; i12++) {
                                                                            sTemp10 = new StringBuilder(String.valueOf(sTemp10)).append(String.format("%02x", new Object[]{Byte.valueOf(data[nIndex16 + i12])})).toString();
                                                                        }
                                                                        hashmap.put("Tag59", new StringBuilder(String.valueOf(midStemp)).append(sTemp10.substring(4, sTemp10.length())).toString());
                                                                        int nIndex17 = nIndex16 + nDatalen2;
                                                                        if (nTotalLen < nIndex17 + 1) {
                                                                            if (mCallBackData != null) {
                                                                                mCallBackData.onReadCardData(hashmap);
                                                                            }
                                                                            i = 2;
                                                                        } else {
                                                                            if (PASSWORD_ENCRY_MODEM == 3) {
                                                                                byte nDatalen3 = data[nIndex17];
                                                                                int nIndex18 = nIndex17 + 1;
                                                                                String sTemp11 = "";
                                                                                for (int i13 = 0; i13 < nDatalen3; i13++) {
                                                                                    sTemp11 = new StringBuilder(String.valueOf(sTemp11)).append(String.format("%02x", new Object[]{Byte.valueOf(data[nIndex18 + i13])})).toString();
                                                                                }
                                                                                byte nPassLen = data[nIndex18];
                                                                                if (nPassLen > 0) {
                                                                                    sTemp = sTemp11.substring(2, nPassLen + 2);
                                                                                } else {
                                                                                    sTemp = "ffffffff";
                                                                                }
                                                                                hashmap.put("AsciiPwd", sTemp);
                                                                                int nIndex19 = nIndex18 + nDatalen3;
                                                                                if (nTotalLen < nIndex19 + 1) {
                                                                                    if (mCallBackData != null) {
                                                                                        mCallBackData.onReadCardData(hashmap);
                                                                                    }
                                                                                    i = 2;
                                                                                } else {
                                                                                    byte nDatalen4 = data[nIndex19];
                                                                                    int nIndex20 = nIndex19 + 1;
                                                                                    String sTemp12 = "";
                                                                                    for (int i14 = 0; i14 < nDatalen4; i14++) {
                                                                                        sTemp12 = new StringBuilder(String.valueOf(sTemp12)).append(String.format("%02x", new Object[]{Byte.valueOf(data[nIndex20 + i14])})).toString();
                                                                                    }
                                                                                    hashmap.put("SnData", toStringHex(sTemp12));
                                                                                    nIndex17 = nIndex20 + nDatalen4;
                                                                                    if (nTotalLen < nIndex17 + 1) {
                                                                                        if (mCallBackData != null) {
                                                                                            mCallBackData.onReadCardData(hashmap);
                                                                                        }
                                                                                        i = 2;
                                                                                    }
                                                                                }
                                                                            }
                                                                            if (TRACK_ENCRY_MODEM == 5 || TRACK_ENCRY_MODEM == 8) {
                                                                                byte nDatalen5 = data[nIndex17];
                                                                                int nIndex21 = nIndex17 + 1;
                                                                                String sTemp13 = "";
                                                                                for (int i15 = 0; i15 < nDatalen5; i15++) {
                                                                                    sTemp13 = new StringBuilder(String.valueOf(sTemp13)).append(String.format("%02x", new Object[]{Byte.valueOf(data[nIndex21 + i15])})).toString();
                                                                                }
                                                                                hashmap.put("Random", sTemp13);
                                                                                int nIndex22 = nIndex21 + nDatalen5;
                                                                            }
                                                                            if (mCallBackData != null) {
                                                                                mCallBackData.onReadCardData(hashmap);
                                                                            }
                                                                            i = 0;
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return i;
    }

    private void GetDeviceInfo(byte[] data) {
        int nDataLen = data.length;
        Map<String, String> hashdata = new HashMap<>();
        String strDeviceSn = "";
        String strAppversion = "";
        String str = "";
        String str2 = "";
        if (data[1] != 0 || data.length < 2) {
            mCallBackData.onDeviceInfo(hashdata);
            return;
        }
        byte nLen = data[2];
        if (3 + nLen > nDataLen) {
            mCallBackData.onDeviceInfo(hashdata);
            return;
        }
        for (int i = 0; i < nLen; i++) {
            strDeviceSn = new StringBuilder(String.valueOf(strDeviceSn)).append(String.format("%02x", new Object[]{Byte.valueOf(data[3 + i])})).toString();
        }
        hashdata.put("SN", toStringHex(strDeviceSn));
        if (!bGetDeviceInfo) {
            mCallBackData.onDeviceInfo(hashdata);
            return;
        }
        int nIndex = 3 + nLen;
        byte nLen2 = data[nIndex];
        int nIndex2 = nIndex + 1;
        if (nIndex2 + nLen2 > nDataLen) {
            mCallBackData.onDeviceInfo(hashdata);
            return;
        }
        for (int i2 = 0; i2 < nLen2; i2++) {
            strAppversion = new StringBuilder(String.valueOf(strAppversion)).append(String.format("%02x", new Object[]{Byte.valueOf(data[nIndex2 + i2])})).toString();
        }
        hashdata.put("AppVersion", toStringHex(strAppversion));
        String strBootversion = "";
        int nIndex3 = nIndex2 + nLen2;
        if (nIndex3 + 2 > nDataLen) {
            hashdata.put("BootVersion", "");
            hashdata.put("Model", "");
            String strBootversion2 = "";
            String strModel = "";
        } else {
            byte nLen3 = data[nIndex3];
            int nIndex4 = nIndex3 + 1;
            if (nIndex4 + nLen3 > nDataLen) {
                mCallBackData.onDeviceInfo(hashdata);
                return;
            }
            for (int i3 = 0; i3 < nLen3; i3++) {
                strBootversion = new StringBuilder(String.valueOf(strBootversion)).append(String.format("%02x", new Object[]{Byte.valueOf(data[nIndex4 + i3])})).toString();
            }
            hashdata.put("Bootversion", toStringHex(strBootversion));
            String strModel2 = "";
            int nIndex5 = nIndex4 + nLen3;
            if (nIndex5 > nDataLen) {
                hashdata.put("Model", "");
                mCallBackData.onDeviceInfo(hashdata);
                return;
            } else if (nIndex5 + 2 > nDataLen) {
                String strModel3 = "";
                hashdata.put("Model", "");
            } else {
                byte nLen4 = data[nIndex5];
                int nIndex6 = nIndex5 + 1;
                if (nIndex6 + nLen4 > nDataLen) {
                    mCallBackData.onDeviceInfo(hashdata);
                    return;
                }
                for (int i4 = 0; i4 < nLen4; i4++) {
                    strModel2 = new StringBuilder(String.valueOf(strModel2)).append(String.format("%02x", new Object[]{Byte.valueOf(data[nIndex6 + i4])})).toString();
                }
                hashdata.put("Model", toStringHex(strModel2));
            }
        }
        mCallBackData.onDeviceInfo(hashdata);
    }

    public static String hexStr2Str(String hexStr) {
        String str = "0123456789ABCDEF";
        char[] hexs = hexStr.toCharArray();
        byte[] bytes = new byte[(hexStr.length() / 2)];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = (byte) (((str.indexOf(hexs[i * 2]) * 16) + str.indexOf(hexs[(i * 2) + 1])) & 255);
        }
        return new String(bytes);
    }

    private void DisCmdData(byte[] data) {
        String MsgData;
        String MsgData2 = "";
        String str = "";
        String str2 = "";
        switch (data[0]) {
            case -96:
                if (data[1] == 3) {
                    mCallBackData.onWaitingForCardSwipe();
                    return;
                } else if (data[1] == 2) {
                    bIsEncry = true;
                    return;
                } else {
                    bIsEncry = false;
                    return;
                }
            case -1:
                StandbySALEBrushCard(data);
                return;
            case BRUSHDATA /*18*/:
            case ICBRUSH /*24*/:
                byte nResut = data[1];
                if (data[1] == 0) {
                    byte nLen = data[2];
                    if (data.length >= nLen + 3) {
                        String strData = "";
                        for (int i = 0; i < nLen; i++) {
                            strData = new StringBuilder(String.valueOf(strData)).append(String.format("%02x", new Object[]{Byte.valueOf(data[i + 3])})).toString();
                        }
                        String strData2 = hexStr2Str(strData);
                        if (data[nLen + 3] != 0) {
                            mCallBackData.onError(SWIPE_DOWNGRADE, "downgrade");
                        }
                        mCallBackData.onSwipeCardSuccess(strData2);
                        return;
                    }
                    return;
                }
                byte nResut2 = data[1];
                mCallBackData.onError(SWIPE_STOP, "transaction terminates");
                return;
            case 19:
                byte nResut3 = data[1];
                return;
            case IC_OPEN /*20*/:
                String MsgData3 = "";
                if (data[1] == 0) {
                    String openATR = "";
                    byte len = data[2];
                    for (int i2 = 2; i2 < len + 3; i2++) {
                        openATR = new StringBuilder(String.valueOf(openATR)).append(String.format("%02x", new Object[]{Byte.valueOf(data[i2])})).toString();
                    }
                    String MsgData4 = openATR;
                    return;
                }
                return;
            case IC_CLOSE /*21*/:
                byte nResut4 = data[1];
                return;
            case IC_WRITEAPDU /*22*/:
                if (data[1] == 0) {
                    String RESP = "";
                    byte len2 = data[2];
                    for (int i3 = 2; i3 < len2 + 3; i3++) {
                        RESP = new StringBuilder(String.valueOf(RESP)).append(String.format("%02x", new Object[]{Byte.valueOf(data[i3])})).toString();
                    }
                    String MsgData5 = RESP;
                    return;
                }
                return;
            case ProofIcParm /*35*/:
                byte nResut5 = data[1];
                if (data[1] == 0) {
                    if (data.length == 2) {
                        MsgData = "";
                    } else {
                        byte b = (byte) (data[2] & 255);
                        StringBuilder sbBattery = new StringBuilder();
                        for (int i4 = 0; i4 < b; i4++) {
                            sbBattery.append(String.format("%02x", new Object[]{Byte.valueOf(data[i4 + 3])}));
                        }
                        MsgData = sbBattery.toString();
                    }
                    mCallBackData.onGoOnlineProcess(Boolean.valueOf(true), nResut5, MsgData);
                    return;
                }
                mCallBackData.onGoOnlineProcess(Boolean.valueOf(false), nResut5, "");
                return;
            case CHECK_IC /*36*/:
                if (data[1] == 0) {
                    mCallBackData.onDetectIC();
                    return;
                }
                return;
            case SETPUBKEYPARM /*50*/:
            case SETAIDPAMR /*51*/:
            case ClEARPUBKEY /*57*/:
            case CLEARAID /*58*/:
            case SETTERNO /*66*/:
                byte nResut6 = data[1];
                return;
            case MAINKEY /*52*/:
                String MsgData6 = "";
                if (data[1] == 0) {
                    mCallBackData.onLoadMasterKeySuccess(Boolean.valueOf(true));
                    return;
                } else {
                    mCallBackData.onLoadMasterKeySuccess(Boolean.valueOf(false));
                    return;
                }
            case 53:
            case WORKEY /*56*/:
                String MsgData7 = "";
                if (data[1] == 0) {
                    mCallBackData.onLoadWorkKeySuccess(Boolean.valueOf(true));
                    return;
                } else {
                    mCallBackData.onLoadWorkKeySuccess(Boolean.valueOf(false));
                    return;
                }
            case GETMAC /*55*/:
                String MsgData8 = "";
                StringBuilder sbData = new StringBuilder();
                byte nResut7 = data[1];
                sbData.setLength(0);
                if (data[1] == 0) {
                    for (int i5 = 2; i5 < 10; i5++) {
                        sbData.append(String.format("%02x", new Object[]{Byte.valueOf(data[i5])}));
                    }
                    mCallBackData.onGetMacSuccess(sbData.toString());
                    return;
                }
                mCallBackData.onError(78, "fail");
                return;
            case GETTERNO /*65*/:
                String MsgData9 = "";
                if (data[1] == 0) {
                    for (int i6 = 0; i6 < 23; i6++) {
                        MsgData9 = new StringBuilder(String.valueOf(MsgData9)).append(String.format("%02x", new Object[]{Byte.valueOf(data[i6 + 2])})).toString();
                    }
                    String MsgData10 = toStringHex(MsgData9);
                    return;
                }
                return;
            case RASOrder /*67*/:
                byte nResut8 = data[1];
                String MsgData11 = "";
                if (nResut8 == 0) {
                    mCallBackData.onUpdateRSAState(Boolean.valueOf(true), nResut8);
                    return;
                }
                mCallBackData.onUpdateRSAState(Boolean.valueOf(false), nResut8);
                return;
            case Battery /*69*/:
                if (data[1] == 0) {
                    StringBuilder sbBattery2 = new StringBuilder();
                    for (int i7 = 0; i7 < 1; i7++) {
                        sbBattery2.append(String.format("%02x", new Object[]{Byte.valueOf(data[2])}));
                    }
                    String MsgData12 = String.valueOf(Integer.parseInt(sbBattery2.toString(), 16));
                    mCallBackData.onGetBatterySuccess(Boolean.valueOf(true), MsgData12);
                    return;
                }
                mCallBackData.onGetBatterySuccess(Boolean.valueOf(false), MsgData2);
                return;
            case CMD_CA_PUBLIC_KEY_ADD /*85*/:
                byte resultCode = data[1];
                if (data[1] == 0) {
                    mCallBackData.onAddCAPublicKeySuccess();
                    return;
                } else if (data[1] == 2) {
                    mCallBackData.onAddCAPublicKeyFailure(4138, "more than 64 public keys");
                    return;
                } else if (data[1] == 3) {
                    mCallBackData.onAddCAPublicKeyFailure(4135, "CA public key list empty");
                    return;
                } else if (data[1] == 6) {
                    mCallBackData.onAddCAPublicKeyFailure(4140, "loaded CA public key error due to undefined tag or incorrect data length");
                    return;
                } else if (data[1] == 7) {
                    mCallBackData.onAddCAPublicKeyFailure(4141, "loaded CA public key verification failure due to checksum error");
                    return;
                } else {
                    mCallBackData.onAddCAPublicKeyFailure(resultCode, "");
                    return;
                }
            case CMD_CA_PUBLIC_KEY_DELETE /*86*/:
                byte nResult = data[1];
                if (data[1] == 0) {
                    mCallBackData.onDeleteCAPublicKeySuccess();
                    return;
                } else if (nResult == 1) {
                    mCallBackData.onDeleteCAPublicKeyFailure(4142, "CA public key to be deleted not exists");
                    return;
                } else if (nResult == 2) {
                    mCallBackData.onDeleteCAPublicKeyFailure(4143, "CA public key list empty");
                    return;
                } else if (nResult == 3) {
                    mCallBackData.onDeleteCAPublicKeyFailure(4135, "CA public key list empty");
                    return;
                } else if (nResult == 6) {
                    mCallBackData.onDeleteCAPublicKeyFailure(4140, "delete CA public key error due to undefined tag or incorrect data length");
                    return;
                } else {
                    mCallBackData.onDeleteCAPublicKeyFailure(nResult, "");
                    return;
                }
            case CMD_CA_PUBLIC_KEY_MODIFY /*87*/:
                byte nResult57 = data[1];
                if (data[1] == 0) {
                    mCallBackData.onModifyCAPublicKeySuccess();
                    return;
                } else if (nResult57 == 2) {
                    mCallBackData.onModifyCAPublicKeyFailure(4146, "public key to be modified not exists");
                    return;
                } else if (nResult57 == 3) {
                    mCallBackData.onModifyCAPublicKeyFailure(4135, "CA public key RID list empty");
                    return;
                } else if (nResult57 == 6) {
                    mCallBackData.onModifyCAPublicKeyFailure(4140, "modify CA public key error due to undefined tag or incorrect data length");
                    return;
                } else if (nResult57 == 7) {
                    mCallBackData.onModifyCAPublicKeyFailure(4141, "loaded CA public key verification failure due to checksum error");
                    return;
                } else {
                    return;
                }
            case CHOICECMD_CA_PUBLIC_KEY_GET_LIST_RID /*96*/:
                String MsgData13 = "";
                StringBuilder RidData = new StringBuilder();
                byte nResut9 = data[1];
                if (data[1] == 0) {
                    int k = 0;
                    int indexRid = 4;
                    int listIndex = 0;
                    byte b2 = (byte) (((data[2] & 255) << 8) | (data[3] & 255));
                    ArrayList list = new ArrayList();
                    RidData.setLength(0);
                    byte b3 = data[4];
                    while (k < b2) {
                        for (int i8 = 0; i8 < data[indexRid] + 1; i8++) {
                            RidData.append(String.format("%02x", new Object[]{Byte.valueOf(data[i8 + indexRid])}));
                        }
                        String MsgData14 = RidData.toString();
                        list.add(listIndex, MsgData14.substring(2, MsgData14.length()));
                        String MsgData15 = "";
                        RidData.setLength(0);
                        listIndex++;
                        indexRid = data[indexRid] + indexRid + 1;
                        k = indexRid;
                    }
                    String[] arrString = new String[list.size()];
                    for (int i9 = 0; i9 < list.size(); i9++) {
                        arrString[i9] = (String) list.get(i9);
                    }
                    mCallBackData.onGetCAPublicKeyListSuccess(arrString);
                    return;
                } else if (nResut9 == 3) {
                    mCallBackData.onGetCAPublicKeyListFailure(4135, "public key RID list empty");
                    return;
                } else {
                    mCallBackData.onGetCAPublicKeyListFailure(nResut9, "");
                    return;
                }
            case CMD_CA_PUBLIC_KEY_GET_PARAMS /*97*/:
                Boolean flag1 = Boolean.valueOf(true);
                Boolean flag2 = Boolean.valueOf(true);
                Boolean flag3 = Boolean.valueOf(true);
                Boolean flag4 = Boolean.valueOf(true);
                Boolean flag5 = Boolean.valueOf(true);
                Boolean flag6 = Boolean.valueOf(true);
                Boolean flag7 = Boolean.valueOf(true);
                Boolean finFlag = Boolean.valueOf(true);
                String MsgData16 = "";
                StringBuilder itemData = new StringBuilder();
                String str3 = "";
                StringBuilder RidItemData = new StringBuilder();
                byte nResut10 = data[1];
                if (data[1] == 0) {
                    byte b4 = (byte) (((data[2] & 255) << 8) | (data[3] & 255));
                    ArrayList listItem = new ArrayList();
                    RidItemData.setLength(0);
                    for (int i10 = 0; i10 < b4; i10++) {
                        RidItemData.append(String.format("%02x", new Object[]{Byte.valueOf(data[i10])}));
                    }
                    String MsgData17 = RidItemData.toString();
                    while (finFlag.booleanValue()) {
                        if (!MsgData17.contains("9f06") || !flag1.booleanValue()) {
                            listItem.add(0, "");
                        } else {
                            int paraIndex = MsgData17.indexOf("9f06") / 2;
                            for (int i11 = 0; i11 < data[paraIndex + 2]; i11++) {
                                itemData.append(String.format("%02x", new Object[]{Byte.valueOf(data[i11 + paraIndex + 3])}));
                            }
                            listItem.add(0, itemData.toString());
                            flag1 = Boolean.valueOf(false);
                            itemData.setLength(0);
                            String MsgItemData = "";
                        }
                        if (!MsgData17.contains("9f22") || !flag2.booleanValue()) {
                            listItem.add(1, "");
                        } else {
                            int paraIndex2 = MsgData17.indexOf("9f22") / 2;
                            for (int i12 = 0; i12 < data[paraIndex2 + 2]; i12++) {
                                itemData.append(String.format("%02x", new Object[]{Byte.valueOf(data[i12 + paraIndex2 + 3])}));
                            }
                            String MsgItemData2 = itemData.toString();
                            flag2 = Boolean.valueOf(false);
                            listItem.add(1, MsgItemData2);
                            itemData.setLength(0);
                            String MsgItemData3 = "";
                        }
                        if (!MsgData17.contains("df07") || !flag3.booleanValue()) {
                            listItem.add(2, "");
                        } else {
                            int paraIndex3 = MsgData17.indexOf("df07") / 2;
                            for (int i13 = 0; i13 < data[paraIndex3 + 2]; i13++) {
                                itemData.append(String.format("%02x", new Object[]{Byte.valueOf(data[i13 + 3 + paraIndex3])}));
                            }
                            String MsgItemData4 = itemData.toString();
                            flag3 = Boolean.valueOf(false);
                            listItem.add(2, MsgItemData4);
                            itemData.setLength(0);
                            String MsgItemData5 = "";
                        }
                        if (!MsgData17.contains("df04") || !flag4.booleanValue()) {
                            listItem.add(3, "");
                        } else {
                            int paraIndex4 = MsgData17.indexOf("df04") / 2;
                            for (int i14 = 0; i14 < data[paraIndex4 + 2]; i14++) {
                                itemData.append(String.format("%02x", new Object[]{Byte.valueOf(data[i14 + 3 + paraIndex4])}));
                            }
                            String MsgItemData6 = itemData.toString();
                            flag4 = Boolean.valueOf(false);
                            listItem.add(3, MsgItemData6);
                            itemData.setLength(0);
                            String MsgItemData7 = "";
                        }
                        if (!MsgData17.contains("df06") || !flag5.booleanValue()) {
                            listItem.add(4, "");
                        } else {
                            int paraIndex5 = MsgData17.indexOf("df06") / 2;
                            for (int i15 = 0; i15 < data[paraIndex5 + 2]; i15++) {
                                itemData.append(String.format("%02x", new Object[]{Byte.valueOf(data[i15 + 3 + paraIndex5])}));
                            }
                            String MsgItemData8 = itemData.toString();
                            flag5 = Boolean.valueOf(false);
                            listItem.add(4, MsgItemData8);
                            itemData.setLength(0);
                            String MsgItemData9 = "";
                        }
                        if (!MsgData17.contains("df03") || !flag6.booleanValue()) {
                            listItem.add(5, "");
                        } else {
                            int paraIndex6 = MsgData17.indexOf("df03") / 2;
                            for (int i16 = 0; i16 < data[paraIndex6 + 2]; i16++) {
                                itemData.append(String.format("%02x", new Object[]{Byte.valueOf(data[i16 + 3 + paraIndex6])}));
                            }
                            String MsgItemData10 = itemData.toString();
                            flag6 = Boolean.valueOf(false);
                            listItem.add(5, MsgItemData10);
                            itemData.setLength(0);
                            String MsgItemData11 = "";
                        }
                        if (!MsgData17.contains("df02") || !flag7.booleanValue()) {
                            listItem.add(6, "");
                        } else {
                            int paraIndex7 = MsgData17.indexOf("df02") / 2;
                            byte b5 = (byte) (data[paraIndex7 + 2] & 255);
                            for (int i17 = 0; i17 < b5; i17++) {
                                itemData.append(String.format("%02x", new Object[]{Byte.valueOf(data[i17 + 3 + paraIndex7])}));
                            }
                            String MsgItemData12 = itemData.toString();
                            flag7 = Boolean.valueOf(false);
                            listItem.add(6, MsgItemData12);
                            itemData.setLength(0);
                            String MsgItemData13 = "";
                            finFlag = Boolean.valueOf(false);
                        }
                    }
                    String[] arrString2 = new String[listItem.size()];
                    for (int i18 = 0; i18 < listItem.size(); i18++) {
                        arrString2[i18] = (String) listItem.get(i18);
                    }
                    String MsgData18 = MsgData17.substring(8, MsgData17.length());
                    Construction.setRidItemData(arrString2);
                    mCallBackData.onGetCAPublicKeyParamsSuccess(MsgData18);
                    return;
                } else if (nResut10 == 3) {
                    mCallBackData.onGetCAPublicKeyParamsFailure(4135, "public key RID list empty");
                    return;
                } else if (nResut10 == 4) {
                    mCallBackData.onGetCAPublicKeyParamsFailure(4137, "unable to find the corresponding RID");
                    return;
                } else {
                    mCallBackData.onGetCAPublicKeyParamsFailure(nResut10, "");
                    return;
                }
            default:
                return;
        }
    }

    public void onReceive(byte[] data) {
        isProssing = true;
        if (PrintLog) {
            StringBuilder sb = new StringBuilder();
            for (byte valueOf : data) {
                sb.append(String.format("%02x", new Object[]{Byte.valueOf(valueOf)}));
            }
            Log.e("onReceive Bluetooth", sb.toString());
        }
        synchronized (mWaitLock) {
            if (mCallBackData != null) {
                switch (data[0]) {
                    case -96:
                    case BRUSHDATA /*18*/:
                    case 19:
                    case IC_OPEN /*20*/:
                    case IC_CLOSE /*21*/:
                    case IC_WRITEAPDU /*22*/:
                    case ICBRUSH /*24*/:
                    case PASSWORD_INPUT_FLAG /*25*/:
                    case ProofIcParm /*35*/:
                    case CHECK_IC /*36*/:
                    case GETMAC /*55*/:
                    case GETTERNO /*65*/:
                    case Battery /*69*/:
                        DisCmdData(data);
                        break;
                    case -1:
                    case 32:
                        if (data[1] != 0) {
                            mCallBackData.onError(data[1], "Card swipe failed!");
                            break;
                        } else {
                            StandbySALEBrushCard(data);
                            break;
                        }
                    case GETTRACKDATA_CMD /*34*/:
                        if (data[1] != 0) {
                            if (data[1] != 70) {
                                byte nReult = data[1];
                                if (nReult != 176) {
                                    if (nReult != -31) {
                                        if (nReult != -30) {
                                            if (nReult != -29) {
                                                if (nReult != -28) {
                                                    if (nReult != 104) {
                                                        if (nReult != -26) {
                                                            if (nReult != 76) {
                                                                if (nReult != 70) {
                                                                    mCallBackData.onError(nReult, "Exception code");
                                                                    break;
                                                                } else {
                                                                    mCallBackData.onError(nReult, "power off");
                                                                    break;
                                                                }
                                                            } else {
                                                                mCallBackData.onError(nReult, "low power");
                                                                break;
                                                            }
                                                        } else {
                                                            mCallBackData.onError(nReult, "IC card removes");
                                                            break;
                                                        }
                                                    } else {
                                                        mCallBackData.onError(nReult, "transaction terminates");
                                                        break;
                                                    }
                                                } else {
                                                    mCallBackData.onError(nReult, "AID not supported, please use magnetic card");
                                                    break;
                                                }
                                            } else {
                                                mCallBackData.onError(nReult, "failure to handle IC card, please use magnetic card");
                                                break;
                                            }
                                        } else {
                                            mCallBackData.onError(nReult, "exit due to time expires");
                                            break;
                                        }
                                    } else {
                                        mCallBackData.onError(nReult, "cancel transaction");
                                        break;
                                    }
                                } else {
                                    mCallBackData.onError(nReult, "downgrade");
                                    break;
                                }
                            } else {
                                mCallBackData.onError(data[1], "power off");
                                bConnDevice = false;
                                disConnectBluetoothDevice();
                                break;
                            }
                        } else if (data[2] != 4) {
                            if (data[2] == 0) {
                                String title = this.buffer[1];
                                String head = title.substring(0, 4);
                                String title2 = title.substring(6, title.length());
                                String body = this.buffer[2];
                                StandbySALEBrushCard(hexStr2Bytes(new StringBuilder(String.valueOf(head)).append(title2).append(body.substring(6, body.length())).toString()));
                                break;
                            } else {
                                StringBuilder sb2 = new StringBuilder();
                                for (byte valueOf2 : data) {
                                    sb2.append(String.format("%02x", new Object[]{Byte.valueOf(valueOf2)}));
                                }
                                this.buffer[data[2]] = sb2.toString();
                                List.add(sb2.toString());
                                this.CeShiData = sb2.toString();
                                break;
                            }
                        } else {
                            StringBuilder sb3 = new StringBuilder();
                            for (byte valueOf3 : data) {
                                sb3.append(String.format("%02x", new Object[]{Byte.valueOf(valueOf3)}));
                            }
                            String title1 = sb3.toString();
                            String head1 = title1.substring(0, 4);
                            String body1 = title1.substring(6, title1.length());
                            System.out.println("明文数据组包后的参数===" + head1 + body1);
                            StandbySALEBrushCard1(hexStr2Bytes(new StringBuilder(String.valueOf(head1)).append(body1).toString()));
                            break;
                        }
                    case TRANSKEY /*49*/:
                    case SETPUBKEYPARM /*50*/:
                    case SETAIDPAMR /*51*/:
                    case MAINKEY /*52*/:
                    case 53:
                    case WORKEY /*56*/:
                    case ClEARPUBKEY /*57*/:
                    case CLEARAID /*58*/:
                    case SETTERNO /*66*/:
                        DisCmdData(data);
                        break;
                    case 64:
                        GetDeviceInfo(data);
                        break;
                    case RASOrder /*67*/:
                        DisCmdData(data);
                        break;
                    case CMD_CA_PUBLIC_KEY_ADD /*85*/:
                        DisCmdData(data);
                        break;
                    case CMD_CA_PUBLIC_KEY_DELETE /*86*/:
                        DisCmdData(data);
                        break;
                    case CMD_CA_PUBLIC_KEY_MODIFY /*87*/:
                        DisCmdData(data);
                        break;
                    case CHOICECMD_CA_PUBLIC_KEY_GET_LIST_RID /*96*/:
                        DisCmdData(data);
                        break;
                    case CMD_CA_PUBLIC_KEY_GET_PARAMS /*97*/:
                        DisCmdData(data);
                        break;
                }
            }
        }
    }

    public void onDataReceived(BluetoothIBridgeDevice device, byte[] buffer2, int len) {
        System.arraycopy(buffer2, 0, Readbuffer, mRxBytes, len);
        if (mRxBytes == 0) {
            nTotallen = buffer2[0] & 255;
            nTotallen <<= 8;
            nTotallen |= buffer2[1] & 255;
        }
        mRxBytes += len;
        if (nTotallen != 0 && nTotallen + 2 <= mRxBytes) {
            byte[] readDataBuf = new byte[(mRxBytes - 2)];
            System.arraycopy(Readbuffer, 2, readDataBuf, 0, mRxBytes - 2);
            onReceive(readDataBuf);
            mRxBytes = 0;
            nTotallen = 0;
            Arrays.fill(Readbuffer, (byte) 0);
            if (mExchangeTimer != null) {
                mExchangeTimer.cancel();
                mExchangeTimer = null;
            }
        }
    }

    public void closeResource() {
        Iterator<BluetoothIBridgeDevice> it = mDevices.iterator();
        while (it.hasNext()) {
            BluetoothIBridgeDevice d = (BluetoothIBridgeDevice) it.next();
            if (!(d == null || !d.isConnected() || mAdapter == null)) {
                byte[] cmd = {0, 2, -96, 0};
                mAdapter.send(d, cmd, cmd.length);
                mAdapter.disconnectDevice(d);
            }
        }
        if (mAdapter != null) {
            mAdapter.unregisterDataReceiver(this);
            mAdapter.unregisterEventReceiver(this);
        }
        disConnectBluetoothDevice();
        mAdapter.destroy();
        mBinder.doUnbindService();
        mBinder.unregisterBluetoothAdapterListener(serviceListener);
    }

    private static byte LrcCompute(byte[] data, int nDatalen) {
        byte nLrc = 0;
        for (byte b : data) {
            nLrc = (byte) (b ^ nLrc);
        }
        return nLrc;
    }

    public static synchronized int WriteCmdData(byte[] data) {
        int i = -1;
        synchronized (BluetoothCommmanager.class) {
            int nlen = data.length;
            byte[] SendData = new byte[(nlen + 2 + 1)];
            SendData[0] = (byte) ((nlen + 1) / 256);
            SendData[1] = (byte) ((nlen + 1) % 256);
            System.arraycopy(data, 0, SendData, 2, nlen);
            if (bConnDevice) {
                SendData[nlen + 2] = LrcCompute(SendData, SendData.length - 1);
                if (PrintLog) {
                    StringBuilder sb = new StringBuilder();
                    for (byte valueOf : SendData) {
                        sb.append(String.format("%02x", new Object[]{Byte.valueOf(valueOf)}));
                    }
                    Log.e("WriteCmdData", sb.toString());
                }
                if (mExchangeTimer != null) {
                    mExchangeTimer.cancel();
                    mExchangeTimer = null;
                }
                mRxBytes = 0;
                nTotallen = 0;
                Arrays.fill(Readbuffer, (byte) 0);
                if (connectDevice.isConnected()) {
                    if (mAdapter != null) {
                        isProssing = false;
                        mAdapter.send(connectDevice, SendData, SendData.length);
                        if (SendData.length > 0 && (SendData[2] & 255) == 34) {
                            WAIT_TIMEOUT *= 20;
                        } else if (SendData.length <= 0 || (SendData[2] & 255) != 64) {
                            WAIT_TIMEOUT = WAIT_TIMEOUT;
                        } else {
                            WAIT_TIMEOUT = WAIT_TIMEOUT;
                        }
                        mExchangeTimer = new Timer(true);
                        if (WAIT_TIMEOUT > 0) {
                            mExchangeTimer.schedule(new TimerTask() {
                                public void run() {
                                    if (BluetoothCommmanager.mExchangeTimer != null) {
                                        BluetoothCommmanager.mExchangeTimer.cancel();
                                        BluetoothCommmanager.mExchangeTimer = null;
                                    }
                                    if (BluetoothCommmanager.mCallBackData != null) {
                                        BluetoothCommmanager.mCallBackData.onTimeout();
                                    }
                                }
                            }, (long) WAIT_TIMEOUT);
                        } else if (PrintLog) {
                            Log.e("测试不允许重复点击", "");
                        }
                    }
                    i = 0;
                }
            }
        }
        return i;
    }

    public synchronized int GetSnVersion() {
        bGetDeviceInfo = false;
        return WriteCmdData(new byte[]{64});
    }

    public synchronized int getDeviceInfo() {
        bGetDeviceInfo = true;
        return WriteCmdData(new byte[]{64});
    }

    public synchronized int MagnAmountPasswordCard(long timeout, long amount) {
        byte[] SendData;
        byte[] bArr = new byte[12];
        byte[] bArr2 = new byte[3];
        SendData = new byte[PASSWORD_INPUT_FLAG];
        track2DataLen = 0;
        track3DataLen = 0;
        track1DataLen = 0;
        SendData[0] = 34;
        SendData[1] = 1;
        SendData[2] = 1;
        SendData[3] = 1;
        SendData[4] = (byte) TRACK_ENCRY_MODEM;
        SendData[5] = (byte) PASSWORD_ENCRY_MODEM;
        SendData[6] = 0;
        SendData[7] = 0;
        Formatter fmt = new Formatter();
        fmt.format("%012d", new Object[]{Long.valueOf(amount)});
        System.arraycopy(fmt.toString().getBytes(), 0, SendData, 8, 12);
        System.arraycopy(hexStringToByte(new SimpleDateFormat("yyMMdd").format(new Date())), 0, SendData, 20, 3);
        if (timeout < 20000 || timeout > 60000) {
            timeout = 60000;
        }
        SendData[23] = (byte) ((int) (timeout / 1000));
        SendData[24] = 33;
        if (PrintLog) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 24; i++) {
                sb.append(String.format("%02x", new Object[]{Byte.valueOf(SendData[i])}));
            }
            Log.e("MagnAmountPasswordCard", sb.toString());
        }
        return WriteCmdData(SendData);
    }

    public synchronized int MagnAmountNoPasswordCard(long timeout, long amount) {
        byte[] SendData;
        byte[] bArr = new byte[12];
        byte[] bArr2 = new byte[3];
        SendData = new byte[PASSWORD_INPUT_FLAG];
        track2DataLen = 0;
        track3DataLen = 0;
        SendData[0] = 34;
        SendData[1] = 1;
        SendData[2] = 0;
        SendData[3] = 1;
        SendData[4] = (byte) TRACK_ENCRY_MODEM;
        SendData[5] = (byte) PASSWORD_ENCRY_MODEM;
        SendData[6] = 0;
        SendData[7] = 0;
        Formatter fmt = new Formatter();
        fmt.format("%012d", new Object[]{Long.valueOf(amount)});
        System.arraycopy(fmt.toString().getBytes(), 0, SendData, 8, 12);
        System.arraycopy(hexStringToByte(new SimpleDateFormat("yyMMdd").format(new Date())), 0, SendData, 20, 3);
        if (timeout < 20000 || timeout > 60000) {
            timeout = 60000;
        }
        SendData[23] = (byte) ((int) (timeout / 1000));
        SendData[24] = 33;
        if (PrintLog) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 24; i++) {
                sb.append(String.format("%02x", new Object[]{Byte.valueOf(SendData[i])}));
            }
            Log.e("NoPasswordCard", sb.toString());
        }
        return WriteCmdData(SendData);
    }

    public synchronized int MagnNoAmountPasswordCard(long timeout, long amount) {
        byte[] SendData;
        byte[] bArr = new byte[12];
        byte[] bArr2 = new byte[3];
        SendData = new byte[PASSWORD_INPUT_FLAG];
        track2DataLen = 0;
        track3DataLen = 0;
        SendData[0] = 34;
        SendData[1] = 0;
        SendData[2] = 1;
        SendData[3] = 1;
        SendData[4] = (byte) TRACK_ENCRY_MODEM;
        SendData[5] = (byte) PASSWORD_ENCRY_MODEM;
        SendData[6] = 0;
        SendData[7] = 0;
        Formatter fmt = new Formatter();
        fmt.format("%012d", new Object[]{Long.valueOf(amount)});
        System.arraycopy(fmt.toString().getBytes(), 0, SendData, 8, 12);
        System.arraycopy(hexStringToByte(new SimpleDateFormat("yyMMdd").format(new Date())), 0, SendData, 20, 3);
        if (timeout < 20000 || timeout > 60000) {
            timeout = 60000;
        }
        SendData[23] = (byte) ((int) (timeout / 1000));
        SendData[24] = 33;
        if (PrintLog) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 24; i++) {
                sb.append(String.format("%02x", new Object[]{Byte.valueOf(SendData[i])}));
            }
            Log.e("PasswordCard", sb.toString());
        }
        return WriteCmdData(SendData);
    }

    public synchronized int MagnNoAmountNoPasswordCard(long timeout, long amount) {
        byte[] SendData;
        byte[] bArr = new byte[12];
        byte[] bArr2 = new byte[3];
        SendData = new byte[PASSWORD_INPUT_FLAG];
        track2DataLen = 0;
        track3DataLen = 0;
        SendData[0] = 34;
        SendData[1] = 0;
        SendData[2] = 0;
        SendData[3] = 1;
        SendData[4] = (byte) TRACK_ENCRY_MODEM;
        SendData[5] = (byte) PASSWORD_ENCRY_MODEM;
        SendData[6] = 0;
        SendData[7] = 0;
        Formatter fmt = new Formatter();
        fmt.format("%012d", new Object[]{Long.valueOf(amount)});
        System.arraycopy(fmt.toString().getBytes(), 0, SendData, 8, 12);
        System.arraycopy(hexStringToByte(new SimpleDateFormat("yyMMdd").format(new Date())), 0, SendData, 20, 3);
        if (timeout < 20000 || timeout > 60000) {
            timeout = 60000;
        }
        SendData[23] = (byte) ((int) (timeout / 1000));
        SendData[24] = 33;
        if (PrintLog) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 16; i++) {
                sb.append(String.format("%02x", new Object[]{Byte.valueOf(SendData[i])}));
            }
            Log.e("NoPasswordCard", sb.toString());
        }
        return WriteCmdData(SendData);
    }

    public synchronized int swipeCard(long timeout, long amount) {
        return MagnAmountPasswordCard(timeout, amount);
    }

    public synchronized int magnCancel() {
        return WriteCmdData(new byte[]{-103});
    }

    public static synchronized int inputPassword(String bPasskey, int nPasswordlen) {
        int WriteCmdData;
        synchronized (BluetoothCommmanager.class) {
            synchronized (mWaitLock) {
                byte[] data = new byte[10];
                byte[] bPass = new byte[8];
                byte[] bPassword = new byte[8];
                data[0] = 25;
                data[1] = 8;
                int nPasLen = nPasswordlen;
                if (nPasswordlen % 2 != 0) {
                    bPasskey = new StringBuilder(String.valueOf(bPasskey)).append("f").toString();
                    nPasLen++;
                }
                Arrays.fill(bPassword, (byte) -1);
                Arrays.fill(bPass, (byte) -1);
                byte[] bPassword2 = hexStr2Bytes(bPasskey);
                bPass[0] = (byte) nPasswordlen;
                System.arraycopy(bPassword2, 0, bPass, 1, nPasLen / 2);
                System.arraycopy(bPass, 0, data, 2, 8);
                if (PrintLog) {
                    StringBuilder sb = new StringBuilder();
                    for (byte valueOf : data) {
                        sb.append(String.format("%02x", new Object[]{Byte.valueOf(valueOf)}));
                    }
                    Log.e("InputPassword", sb.toString());
                    Log.e("====", new SimpleDateFormat("yyyy:MM:dd HH:mm:ss:SSS").format(Long.valueOf(System.currentTimeMillis())));
                }
                WriteCmdData = WriteCmdData(data);
            }
        }
        return WriteCmdData;
    }

    public synchronized int writeMainKey(byte[] bMainKey) {
        int WriteCmdData;
        synchronized (mWaitLock) {
            int len = bMainKey.length;
            byte[] data = new byte[(len + 5 + 1)];
            data[0] = 52;
            data[1] = 1;
            data[2] = (byte) len;
            System.arraycopy(bMainKey, 0, data, 3, len);
            if (PASSWORD_ENCRY_MODEM == 3) {
                data[len + 3] = -127;
            } else {
                data[len + 3] = 1;
            }
            data[len + 4] = (byte) MAINKEY_ENCRY_MODEM;
            data[len + 5] = 0;
            WriteCmdData = WriteCmdData(data);
        }
        return WriteCmdData;
    }

    public synchronized int WriteMainKey(byte index, byte[] bMainKey) {
        int WriteCmdData;
        synchronized (mWaitLock) {
            int len = bMainKey.length;
            byte[] data = new byte[(len + 5 + 1)];
            data[0] = 52;
            data[1] = index;
            data[2] = (byte) len;
            System.arraycopy(bMainKey, 0, data, 3, len);
            if (PASSWORD_ENCRY_MODEM == 3) {
                data[len + 3] = -127;
            } else {
                data[len + 3] = 1;
            }
            data[len + 4] = (byte) MAINKEY_ENCRY_MODEM;
            data[len + 5] = 0;
            WriteCmdData = WriteCmdData(data);
        }
        return WriteCmdData;
    }

    public synchronized int TransferMainKey(byte[] order) {
        int WriteCmdData;
        synchronized (mWaitLock) {
            int len = order.length;
            byte[] data = new byte[(len + 3 + 1)];
            data[0] = 49;
            data[1] = 1;
            data[2] = (byte) len;
            System.arraycopy(order, 0, data, 3, len);
            data[len + 3] = (byte) MAINKEY_ENCRY_MODEM;
            WriteCmdData = WriteCmdData(data);
        }
        return WriteCmdData;
    }

    public synchronized int writeWorkKey(byte[] bWorkKey) {
        int WriteCmdData;
        synchronized (mWaitLock) {
            int len = bWorkKey.length;
            byte[] data = new byte[(len + 1 + 1)];
            data[0] = 56;
            System.arraycopy(bWorkKey, 0, data, 1, len);
            data[len + 1] = (byte) WORK_ENCRY_MODEM;
            WriteCmdData = WriteCmdData(data);
        }
        return WriteCmdData;
    }

    public synchronized int getMAC(byte[] bDataKey) {
        int WriteCmdData;
        synchronized (mWaitLock) {
            int len = bDataKey.length;
            byte[] data = new byte[(len + 6)];
            data[0] = 55;
            data[1] = 4;
            data[2] = (byte) (len / 256);
            data[3] = (byte) (len % 256);
            System.arraycopy(bDataKey, 0, data, 4, len);
            data[len + 4] = (byte) TRACK_MAC_DATA;
            data[len + 5] = (byte) MAC_3DES_DATA;
            WriteCmdData = WriteCmdData(data);
        }
        return WriteCmdData;
    }

    public synchronized int WriteTernumber(byte[] bData) {
        int WriteCmdData;
        synchronized (mWaitLock) {
            byte[] data = new byte[24];
            data[0] = 66;
            System.arraycopy(bData, 0, data, 1, 23);
            WriteCmdData = WriteCmdData(data);
        }
        return WriteCmdData;
    }

    public synchronized int ReadTernumber() {
        int WriteCmdData;
        synchronized (mWaitLock) {
            WriteCmdData = WriteCmdData(new byte[]{65});
        }
        return WriteCmdData;
    }

    public synchronized int WriteEmvCapkParm(int len, byte[] bWorkKey) {
        int WriteCmdData;
        synchronized (mWaitLock) {
            byte[] data = new byte[(len + 1)];
            data[0] = 50;
            System.arraycopy(bWorkKey, 0, data, 1, len);
            WriteCmdData = WriteCmdData(data);
        }
        return WriteCmdData;
    }

    public synchronized int WriteEmvAidParm(int len, byte[] bWorkKey) {
        int WriteCmdData;
        synchronized (mWaitLock) {
            byte[] data = new byte[(len + 1)];
            data[0] = 51;
            System.arraycopy(bWorkKey, 0, data, 1, len);
            WriteCmdData = WriteCmdData(data);
        }
        return WriteCmdData;
    }

    public synchronized int ClearEmvAidParm() {
        int WriteCmdData;
        synchronized (mWaitLock) {
            byte[] data = new byte[2];
            data[0] = 58;
            WriteCmdData = WriteCmdData(data);
        }
        return WriteCmdData;
    }

    public synchronized int ClearCapkParm() {
        int WriteCmdData;
        synchronized (mWaitLock) {
            WriteCmdData = WriteCmdData(new byte[]{57});
        }
        return WriteCmdData;
    }

    public synchronized int ProofIcData(int len, byte[] bWorkKey) {
        String strData = null;
        int WriteCmdData;
        synchronized (mWaitLock) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < len; i++) {
                sb.append(String.format("%02x", new Object[]{Byte.valueOf(bWorkKey[i])}));
            }
            String strData2 = sb.toString();
            if (strData2.substring(0, 2).equals("00")) {
                strData2 = strData2.substring(2);
            }
            String strTag89 = "";
            String strTag91 = "";
            String strTag71 = "";
            String strTag72 = "";
            while (strData.length() > 2) {
                String strTemp = strData.substring(0, 2).toUpperCase();
                if (strTemp.equals("9F")) {
                    String strTemp2 = strData.substring(0, 4);
                    strData = strData.substring((Integer.parseInt(Integer.valueOf(strData.substring(4, 6), 16).toString()) * 2) + 6);
                } else if (strTemp.equals("91")) {
                    String strTemp3 = strData.substring(0, 2);
                    int nTagLen = Integer.parseInt(Integer.valueOf(strData.substring(2, 4), 16).toString());
                    strTag91 = strData.substring(2, (nTagLen * 2) + 4);
                    strData = strData.substring((nTagLen * 2) + 4);
                } else if (strTemp.equals("89")) {
                    String strTemp4 = strData.substring(0, 2);
                    int nTagLen2 = Integer.parseInt(Integer.valueOf(strData.substring(2, 4), 16).toString());
                    strTag89 = strData.substring(2, (nTagLen2 * 2) + 4);
                    strData = strData.substring((nTagLen2 * 2) + 4);
                } else if (strTemp.equals("71")) {
                    String strTemp5 = strData.substring(0, 2);
                    int nTagLen3 = Integer.parseInt(Integer.valueOf(strData.substring(2, 4), 16).toString());
                    strTag71 = strData.substring(2, (nTagLen3 * 2) + 4);
                    strData = strData.substring((nTagLen3 * 2) + 4);
                } else if (strTemp.equals("72")) {
                    String strTemp6 = strData.substring(0, 2);
                    int nTagLen4 = Integer.parseInt(Integer.valueOf(strData.substring(2, 4), 16).toString());
                    strTag72 = strData.substring(2, (nTagLen4 * 2) + 4);
                    strData = strData.substring((nTagLen4 * 2) + 4);
                } else if (strTemp.equals("95") || strTemp.equals("9A") || strTemp.equals("9C") || strTemp.equals("8A") || strTemp.equals("82")) {
                    String strTemp7 = strData.substring(0, 2);
                    strData = strData.substring((Integer.parseInt(Integer.valueOf(strData.substring(2, 4), 16).toString()) * 2) + 4);
                } else {
                    String strTemp8 = strData.substring(0, 4);
                    strData = strData.substring((Integer.parseInt(Integer.valueOf(strData.substring(4, 6), 16).toString()) * 2) + 6);
                }
            }
            if (strTag89.length() == 0) {
                strTag89 = "00";
            }
            if (strTag91.length() == 0) {
                strTag91 = "00";
            }
            if (strTag71.length() == 0) {
                strTag71 = "00";
            }
            if (strTag72.length() == 0) {
                strTag72 = "00";
            }
            byte[] sendBuf = hexStr2Bytes("3030" + strTag89 + strTag91 + strTag71 + strTag72);
            byte[] data = new byte[(sendBuf.length + 1)];
            data[0] = 35;
            System.arraycopy(sendBuf, 0, data, 1, sendBuf.length);
            WriteCmdData = WriteCmdData(data);
        }
        return WriteCmdData;
    }

    public synchronized int readBattery() {
        int WriteCmdData;
        synchronized (mWaitLock) {
            WriteCmdData = WriteCmdData(new byte[]{69});
        }
        return WriteCmdData;
    }

    @SuppressLint({"NewApi"})
    public static String base64Encode(String token) {
        byte[] bytes = token.getBytes();
        return new String(Base64.encode(token.getBytes(), 0), Charset.forName(AsyncHttpResponseHandler.DEFAULT_CHARSET));
    }

    public static String stringToAscii(String value) {
        StringBuffer sbu = new StringBuffer();
        char[] chars = value.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            if (i != chars.length - 1) {
                sbu.append(chars[i]).append(",");
            } else {
                sbu.append(chars[i]);
            }
        }
        return sbu.toString();
    }

    public synchronized int updateRSA(String RSAData) {
        int WriteCmdData;
        if (TextUtils.isEmpty(RSAData)) {
            synchronized (mWaitLock) {
                WriteCmdData = WriteCmdData(new byte[]{67, 0, 0});
            }
        } else {
            byte[] sendBuf = hexStr2Bytes(encodehex(base64Encode(RSAData).replaceAll("[\b\r\n\t]*", "")));
            synchronized (mWaitLock) {
                byte[] data = new byte[(sendBuf.length + 3)];
                data[0] = 67;
                data[1] = (byte) (sendBuf.length / 256);
                data[2] = (byte) (sendBuf.length % 256);
                System.arraycopy(sendBuf, 0, data, 3, sendBuf.length);
                WriteCmdData = WriteCmdData(data);
            }
        }
        return WriteCmdData;
    }

    public static String encodehex(String str) {
        byte[] bytes = str.getBytes();
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (int i = 0; i < bytes.length; i++) {
            sb.append(hexString.charAt((bytes[i] & 240) >> 4));
            sb.append(hexString.charAt((bytes[i] & 15) >> 0));
        }
        return sb.toString();
    }

    public static String isHex(String str) {
        String isHexString = "";
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if ((c < '0' || c > '9') && ((c < 'A' || c > 'F') && (c < 'a' || c > 'f'))) {
                String isHexString2 = " ";
                mCallBackData.onError(73, "wrong data");
                return isHexString2;
            }
            isHexString = str;
        }
        return isHexString;
    }

    public synchronized int goOnlineProcess(String Icdata1, String Icdata2) {
        int i = 1;
        synchronized (this) {
            synchronized (mWaitLock) {
                String Icdata12 = isHex(encodehex(Icdata1));
                String Icdata22 = isHex(Icdata2);
                if (!Icdata12.equalsIgnoreCase(" ") && !Icdata22.equalsIgnoreCase(" ")) {
                    byte[] IcData1 = hexStr2Bytes(Icdata12);
                    byte[] IcData2 = hexStr2Bytes(Icdata22);
                    int len1 = IcData1.length;
                    int len2 = IcData2.length;
                    if (Icdata12.length() == 0) {
                        byte[] data = new byte[(len1 + 3)];
                        data[0] = 35;
                        data[1] = (byte) (len1 / 256);
                        data[2] = (byte) (len1 % 256);
                        System.arraycopy(IcData1, 0, data, 3, len1);
                        i = WriteCmdData(data);
                    } else {
                        byte[] data2 = new byte[(len1 + 3 + len2)];
                        data2[0] = 35;
                        data2[1] = (byte) ((len1 + len2) / 256);
                        data2[2] = (byte) ((len1 + len2) % 256);
                        System.arraycopy(IcData1, 0, data2, 3, len1);
                        System.arraycopy(IcData2, 0, data2, len1 + 3, len2);
                        i = WriteCmdData(data2);
                    }
                }
            }
        }
        return i;
    }

    public synchronized int IC_GetStatus() {
        int WriteCmdData;
        synchronized (mWaitLock) {
            WriteCmdData = WriteCmdData(new byte[]{19});
        }
        return WriteCmdData;
    }

    public synchronized int IC_Open() {
        int WriteCmdData;
        synchronized (mWaitLock) {
            WriteCmdData = WriteCmdData(new byte[]{20});
        }
        return WriteCmdData;
    }

    public synchronized int IC_WriteApdu(byte[] bApdu) {
        int WriteCmdData;
        synchronized (mWaitLock) {
            byte[] data = new byte[(bApdu.length + 2)];
            data[0] = 22;
            data[1] = (byte) bApdu.length;
            System.arraycopy(bApdu, 0, data, 2, bApdu.length);
            WriteCmdData = WriteCmdData(data);
        }
        return WriteCmdData;
    }

    public synchronized int IC_Close() {
        int WriteCmdData;
        synchronized (mWaitLock) {
            WriteCmdData = WriteCmdData(new byte[]{21});
        }
        return WriteCmdData;
    }

    public synchronized boolean isBTConnected() {
        return bConnDevice;
    }

    public synchronized void setTimeout(int timeout) {
        WAIT_TIMEOUT = timeout;
    }

    public static byte[] hexStr2Bytess(String src) {
        int l = src.length() / 2;
        byte[] ret = new byte[l];
        for (int i = 0; i < l; i++) {
            int m = (i * 2) + 1;
            ret[i] = uniteBytes(src.substring(i * 2, m), src.substring(m, m + 1));
        }
        return ret;
    }

    private static byte uniteBytes(String src0, String src1) {
        return (byte) (((byte) (Byte.decode("0x" + src0).byteValue() << 4)) | Byte.decode("0x" + src1).byteValue());
    }

    public synchronized int addCAPublicKey(String CAPKData) {
        int WriteCmdData;
        synchronized (mWaitLock) {
            byte[] sendCAPKData = hexStr2Bytes(CAPKData);
            int ridLen = sendCAPKData.length;
            byte[] SendData = new byte[(ridLen + 3)];
            SendData[0] = 85;
            SendData[1] = (byte) (ridLen / 256);
            SendData[2] = (byte) (ridLen % 256);
            System.arraycopy(sendCAPKData, 0, SendData, 3, ridLen);
            WriteCmdData = WriteCmdData(SendData);
        }
        return WriteCmdData;
    }

    public synchronized int deleteCAPublicKey(String CAPKInfo) {
        int WriteCmdData;
        synchronized (mWaitLock) {
            byte[] sendCAPKInfo = hexStr2Bytes(CAPKInfo);
            int ridLen = sendCAPKInfo.length;
            byte[] SendData = new byte[(ridLen + 3)];
            SendData[0] = 86;
            SendData[1] = (byte) (ridLen / 256);
            SendData[2] = (byte) (ridLen % 256);
            System.arraycopy(sendCAPKInfo, 0, SendData, 3, ridLen);
            WriteCmdData = WriteCmdData(SendData);
        }
        return WriteCmdData;
    }

    public synchronized int modifyCAPublicKey(String CAPKData) {
        int WriteCmdData;
        synchronized (mWaitLock) {
            byte[] sendCAPKData = hexStr2Bytes(CAPKData);
            int ridLen = sendCAPKData.length;
            byte[] SendData = new byte[(ridLen + 3)];
            SendData[0] = 87;
            SendData[1] = (byte) (ridLen / 256);
            SendData[2] = (byte) (ridLen % 256);
            System.arraycopy(sendCAPKData, 0, SendData, 3, ridLen);
            WriteCmdData = WriteCmdData(SendData);
        }
        return WriteCmdData;
    }

    public synchronized int getCAPublicKeyList() {
        int WriteCmdData;
        synchronized (mWaitLock) {
            WriteCmdData = WriteCmdData(new byte[]{96});
        }
        return WriteCmdData;
    }

    public synchronized int getCAPublicKeyParams(String CAPKInfo) {
        int WriteCmdData;
        synchronized (mWaitLock) {
            byte[] sendCAPKInfo = hexStr2Bytess(CAPKInfo);
            int lensendCAPKInfo = sendCAPKInfo.length;
            byte[] SendData = new byte[(lensendCAPKInfo + 4)];
            SendData[0] = 97;
            SendData[1] = (byte) ((lensendCAPKInfo + 1) / 256);
            SendData[2] = (byte) ((lensendCAPKInfo + 1) % 256);
            SendData[3] = (byte) lensendCAPKInfo;
            System.arraycopy(sendCAPKInfo, 0, SendData, 4, lensendCAPKInfo);
            WriteCmdData = WriteCmdData(SendData);
        }
        return WriteCmdData;
    }
}
