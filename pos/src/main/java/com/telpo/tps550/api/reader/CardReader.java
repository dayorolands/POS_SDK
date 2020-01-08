package com.telpo.tps550.api.reader;

import amlib.ccid.Reader;
import amlib.ccid.Reader4428;
import amlib.ccid.Reader4442;
import amlib.hw.HWType;
import amlib.hw.HardwareInterface;
import amlib.hw.ReaderHwException;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import com.telpo.tps550.api.TelpoException;
import com.telpo.tps550.api.collect.Collect;
import com.telpo.tps550.api.usb.UsbUtil;
import com.telpo.tps550.api.util.ShellUtils;
import com.telpo.tps550.api.util.StringUtil;
import com.telpo.tps550.api.util.SystemUtil;
import java.io.FileOutputStream;

public class CardReader {
    private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";
    public static final int CARDREADER_HUB = 1;
    public static final int CARD_TYPE_AT88SC153 = 4;
    public static final int CARD_TYPE_ISO7816 = 1;
    public static final int CARD_TYPE_MSC = 0;
    public static final int CARD_TYPE_SLE4428 = 3;
    public static final int CARD_TYPE_SLE4442 = 2;
    public static final int CARD_TYPE_UNKNOWN = -1;
    public static final int SLOT_STATUS_ICC_ABSENT = 2;
    public static final int SLOT_STATUS_ICC_ACTIVE = 0;
    public static final int SLOT_STATUS_ICC_INACTIVE = 1;
    private static final String TAG = "CardReader";
    protected int cardType = 1;
    protected Context context;
    protected boolean correct_psc_verification = false;
    private Handler handler;
    private HandlerThread handlerThread;
    /* access modifiers changed from: private */
    public Object lock = new Object();
    Reader4442 m4442 = null;
    protected byte[] mATR = null;
    protected ICCardReader mICCardReader;
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            Log.d(CardReader.TAG, "Broadcast Receiver");
            String action = intent.getAction();
            if (CardReader.ACTION_USB_PERMISSION.equals(action)) {
                synchronized (CardReader.this.lock) {
                    UsbDevice device = (UsbDevice) intent.getParcelableExtra("device");
                    if (!intent.getBooleanExtra("permission", false)) {
                        Log.d(CardReader.TAG, "Permission denied for device " + device.getDeviceName());
                        CardReader.this.lock.notify();
                    } else if (device != null && device.equals(CardReader.this.usbDev)) {
                        try {
                            if (CardReader.this.myDev.Init(CardReader.this.usbManager, CardReader.this.usbDev)) {
                                try {
                                    if (CardReader.this.cardType == 2) {
                                        CardReader.this.reader = new Reader4442(CardReader.this.myDev);
                                        Log.d("idcard demo", "reader = new Reader4442(myDev);\ndevice name:" + CardReader.this.usbDev.getDeviceName());
                                    } else if (CardReader.this.cardType == 3) {
                                        CardReader.this.reader = new Reader4428(CardReader.this.myDev);
                                    } else {
                                        CardReader.this.reader = new Reader(CardReader.this.myDev);
                                        CardReader.this.reader.switchMode((byte) 1);
                                    }
                                    Log.d("idcard demo", "reader.open:" + CardReader.this.reader.open());
                                    CardReader.this.reader.setSlot((byte) 0);
                                } catch (Exception e) {
                                    Log.e(CardReader.TAG, "Get Exception : " + e.getMessage());
                                    e.printStackTrace();
                                }
                            }
                        } catch (ReaderHwException e2) {
                            e2.printStackTrace();
                        }
                        CardReader.this.lock.notify();
                    }
                }
            } else if ("android.hardware.usb.action.USB_DEVICE_DETACHED".equals(action)) {
                Log.d(CardReader.TAG, "Device Detached");
                UsbDevice device2 = (UsbDevice) intent.getParcelableExtra("device");
                if (device2 != null && device2.equals(CardReader.this.usbDev)) {
                    if (CardReader.this.reader != null) {
                        CardReader.this.reader.close();
                    }
                    if (CardReader.this.myDev != null) {
                        CardReader.this.myDev.Close();
                    }
                }
            }
        }
    };
    protected int mSlot = 0;
    protected HardwareInterface myDev;
    private PendingIntent permissionIntent;
    String[] portNum = new String[20];
    String[] productNum = new String[20];
    protected Reader reader = null;
    String[] readerNum = new String[4];
    protected int reader_type = -1;
    protected UsbDevice usbDev;
    protected UsbManager usbManager;

    protected static native int close_device();

    private native int device_power(int i);

    protected static native int get_card_status();

    protected static native int icc_power_off();

    protected static native byte[] icc_power_on(int i);

    protected static native int open_device(int i, int i2);

    protected static native int psc_modify(int i, byte[] bArr);

    protected static native int psc_verify(int i, byte[] bArr);

    protected static native byte[] read_main_mem(int i, int i2, int i3);

    protected static native int send_apdu(byte[] bArr, byte[] bArr2);

    protected static native int switch_mode(int i);

    protected static native int telpo_switch_psam(int i);

    protected static native int update_main_mem(int i, int i2, byte[] bArr);

    public CardReader() {
    }

    public CardReader(Context context2) {
        this.context = context2;
    }

    public boolean open() {
        if (SystemUtil.getDeviceType() == StringUtil.DeviceModelEnum.TPS900.ordinal() || SystemUtil.getDeviceType() == StringUtil.DeviceModelEnum.TPS900MB.ordinal()) {
            try {
                this.mICCardReader.open(this.mSlot);
                return true;
            } catch (TelpoException e) {
                e.printStackTrace();
                return false;
            }
        } else {
            int type = SystemUtil.getICCReaderType();
            Log.d("idcard demo", "CardReader type is:" + type);
            Log.d("idcard demo", "mSlot is:" + this.mSlot);
            if ((this.mSlot == 0 && (type == 2 || type == 1 || type == 0)) || ((this.mSlot == 1 && (SystemUtil.getDeviceType() == StringUtil.DeviceModelEnum.TPS350_4G.ordinal() || SystemUtil.getDeviceType() == StringUtil.DeviceModelEnum.TPS350L.ordinal() || SystemUtil.getDeviceType() == StringUtil.DeviceModelEnum.TPS573.ordinal() || SystemUtil.getDeviceType() == StringUtil.DeviceModelEnum.TPS450.ordinal() || SystemUtil.getDeviceType() == StringUtil.DeviceModelEnum.TPS450C.ordinal() || SystemUtil.getDeviceType() == StringUtil.DeviceModelEnum.TPS365.ordinal() || SystemUtil.getDeviceType() == StringUtil.DeviceModelEnum.TPS360C.ordinal() || SystemUtil.getDeviceType() == StringUtil.DeviceModelEnum.TPS360IC.ordinal() || SystemUtil.getDeviceType() == StringUtil.DeviceModelEnum.TPS360A.ordinal() || SystemUtil.getDeviceType() == StringUtil.DeviceModelEnum.TPS470.ordinal())) || (this.mSlot == 2 && SystemUtil.getDeviceType() == StringUtil.DeviceModelEnum.TPS573.ordinal()))) {
                Log.e("idcard demo", "AU9560_GCS-------");
                if (open_device(this.cardType, this.mSlot) < 0) {
                    Log.d("idcard demo", "open_device(cardType, mSlot) < 0");
                    return false;
                }
            } else {
                if (SystemUtil.getDeviceType() == StringUtil.DeviceModelEnum.TPS390U.ordinal()) {
                    device_power(1);
                }
                type = 3;
                this.usbManager = (UsbManager) this.context.getSystemService("usb");
                this.usbDev = getUsbDevice();
                if (this.usbDev == null) {
                    Log.e(TAG, "get usb manager failed");
                    return false;
                }
                this.reader = null;
                this.myDev = new HardwareInterface(HWType.eUSB, this.context);
                this.handlerThread = new HandlerThread("card reader");
                this.handlerThread.start();
                this.handler = new Handler(this.handlerThread.getLooper());
                toRegisterReceiver();
                this.usbManager.requestPermission(this.usbDev, this.permissionIntent);
                synchronized (this.lock) {
                    if (this.reader == null) {
                        try {
                            this.lock.wait(30000);
                        } catch (InterruptedException e2) {
                            e2.printStackTrace();
                        }
                    }
                }
                this.context.unregisterReceiver(this.mReceiver);
                this.handlerThread.quit();
                this.handlerThread = null;
                if (this.reader == null) {
                    Log.e(TAG, "reader is null");
                    this.myDev.Close();
                    this.myDev = null;
                    this.usbManager = null;
                    this.usbDev = null;
                    return false;
                }
            }
            this.reader_type = type;
            return true;
        }
    }

    public boolean open(int isIC) {
        int type;
        if (SystemUtil.getDeviceType() == StringUtil.DeviceModelEnum.TPS900.ordinal() || SystemUtil.getDeviceType() == StringUtil.DeviceModelEnum.TPS900MB.ordinal() || SystemUtil.getDeviceType() == StringUtil.DeviceModelEnum.TPS360IC.ordinal()) {
            try {
                this.mICCardReader.open(this.mSlot);
                return true;
            } catch (TelpoException e) {
                e.printStackTrace();
                return false;
            }
        } else {
            if (isIC == 0) {
                type = SystemUtil.getICCReaderType();
            } else {
                type = 3;
            }
            Log.d("idcard demo", "CardReader type is:" + type);
            if ((this.mSlot == 0 && (type == 2 || type == 1 || type == 0)) || (this.mSlot == 1 && (SystemUtil.getDeviceType() == StringUtil.DeviceModelEnum.TPS350_4G.ordinal() || SystemUtil.getDeviceType() == StringUtil.DeviceModelEnum.TPS350L.ordinal() || SystemUtil.getDeviceType() == StringUtil.DeviceModelEnum.TPS450.ordinal() || SystemUtil.getDeviceType() == StringUtil.DeviceModelEnum.TPS365.ordinal() || SystemUtil.getDeviceType() == StringUtil.DeviceModelEnum.TPS360C.ordinal() || SystemUtil.getDeviceType() == StringUtil.DeviceModelEnum.TPS360A.ordinal() || SystemUtil.getDeviceType() == StringUtil.DeviceModelEnum.TPS470.ordinal()))) {
                Log.e("idcard demo", "AU9560_GCS-------");
                if (open_device(this.cardType, this.mSlot) < 0) {
                    Log.d("idcard demo", "open_device(cardType, mSlot) < 0");
                    return false;
                }
            } else {
                if (SystemUtil.getDeviceType() == StringUtil.DeviceModelEnum.TPS390U.ordinal()) {
                    device_power(1);
                }
                type = 3;
                this.usbManager = (UsbManager) this.context.getSystemService("usb");
                this.usbDev = getUsbDevice();
                if (this.usbDev == null) {
                    Log.e(TAG, "get usb manager failed");
                    return false;
                }
                this.reader = null;
                this.myDev = new HardwareInterface(HWType.eUSB, this.context);
                this.handlerThread = new HandlerThread("card reader");
                this.handlerThread.start();
                this.handler = new Handler(this.handlerThread.getLooper());
                toRegisterReceiver();
                this.usbManager.requestPermission(this.usbDev, this.permissionIntent);
                synchronized (this.lock) {
                    if (this.reader == null) {
                        try {
                            this.lock.wait(30000);
                        } catch (InterruptedException e2) {
                            e2.printStackTrace();
                        }
                    }
                }
                this.context.unregisterReceiver(this.mReceiver);
                this.handlerThread.quit();
                this.handlerThread = null;
                if (this.reader == null) {
                    Log.e(TAG, "reader is null");
                    this.myDev.Close();
                    this.myDev = null;
                    this.usbManager = null;
                    this.usbDev = null;
                    return false;
                }
            }
            this.reader_type = type;
            return true;
        }
    }

    public UsbDevice getAT88SC153Device() {
        if (this.usbManager == null) {
            this.usbManager = (UsbManager) this.context.getSystemService("usb");
        }
        UsbDevice mUsbDev = getUsbDevice();
        Log.d("idcard demo", "getAT88SC153Device:pid:" + mUsbDev.getProductId() + "device name:" + mUsbDev.getDeviceName());
        return mUsbDev;
    }

    public boolean close() {
        if (SystemUtil.getDeviceType() == StringUtil.DeviceModelEnum.TPS900.ordinal() || SystemUtil.getDeviceType() == StringUtil.DeviceModelEnum.TPS900MB.ordinal()) {
            try {
                this.mICCardReader.close(this.mSlot);
                return true;
            } catch (TelpoException e) {
                e.printStackTrace();
                return false;
            }
        } else {
            if (this.reader_type != 2 && this.reader_type != 1 && this.reader_type != 0) {
                if (this.reader != null) {
                    do {
                    } while (this.reader.close() == 3);
                }
                if (this.myDev != null) {
                    this.myDev.Close();
                }
                if (SystemUtil.getDeviceType() == StringUtil.DeviceModelEnum.TPS390U.ordinal()) {
                    device_power(0);
                }
            } else if (close_device() < 0) {
                return false;
            }
            this.reader_type = -1;
            return true;
        }
    }

    public boolean close(int ic360) {
        if (SystemUtil.getDeviceType() == StringUtil.DeviceModelEnum.TPS900.ordinal() || SystemUtil.getDeviceType() == StringUtil.DeviceModelEnum.TPS900MB.ordinal() || SystemUtil.getDeviceType() == StringUtil.DeviceModelEnum.TPS360IC.ordinal()) {
            try {
                this.mICCardReader.close(this.mSlot);
                return true;
            } catch (TelpoException e) {
                e.printStackTrace();
                return false;
            }
        } else {
            if (this.reader_type != 2 && this.reader_type != 1 && this.reader_type != 0) {
                if (this.reader != null) {
                    do {
                    } while (this.reader.close() == 3);
                }
                if (this.myDev != null) {
                    this.myDev.Close();
                }
                if (SystemUtil.getDeviceType() == StringUtil.DeviceModelEnum.TPS390U.ordinal()) {
                    device_power(0);
                }
            } else if (close_device() < 0) {
                return false;
            }
            this.reader_type = -1;
            return true;
        }
    }

    public boolean iccPowerOn() {
        if (SystemUtil.getDeviceType() == StringUtil.DeviceModelEnum.TPS900.ordinal() || SystemUtil.getDeviceType() == StringUtil.DeviceModelEnum.TPS900MB.ordinal()) {
            try {
                if (this.mICCardReader.detect(this.mSlot, 500) != 0) {
                    return false;
                }
                this.mICCardReader.power_on(this.mSlot);
                return true;
            } catch (TelpoException e) {
                e.printStackTrace();
                return false;
            }
        } else {
            if (this.reader_type == 2 || this.reader_type == 1 || this.reader_type == 0) {
                Log.d("idcard demo", "reader_type:" + this.reader_type);
                this.mATR = icc_power_on(this.cardType);
                if (this.mATR == null) {
                    return false;
                }
            } else {
                Log.d("idcard demo", "reader_type:" + this.reader_type);
                byte[] status = new byte[1];
                byte[] bArr = new byte[1];
                if (this.reader == null || this.reader.getCardStatus(status) != 0 || status[0] == 2) {
                    return false;
                }
                int result = this.reader.setPower(1);
                if (result != 0) {
                    Log.e(TAG, "setPower failed: " + result);
                    return false;
                }
            }
            if (this.mSlot == 0) {
                Collect.collectInfo(2, 1, (byte[]) null);
            } else {
                Collect.collectInfo(3, 1, (byte[]) null);
            }
            return true;
        }
    }

    public boolean iccPowerOn(int ic360) {
        if (SystemUtil.getDeviceType() == StringUtil.DeviceModelEnum.TPS900.ordinal() || SystemUtil.getDeviceType() == StringUtil.DeviceModelEnum.TPS900MB.ordinal() || SystemUtil.getDeviceType() == StringUtil.DeviceModelEnum.TPS360IC.ordinal()) {
            try {
                if (this.mICCardReader.detect(this.mSlot, 500) != 0) {
                    return false;
                }
                this.mICCardReader.power_on(this.mSlot);
                return true;
            } catch (TelpoException e) {
                e.printStackTrace();
                return false;
            }
        } else {
            if (this.reader_type == 2 || this.reader_type == 1 || this.reader_type == 0) {
                Log.d("idcard demo", "reader_type:" + this.reader_type);
                this.mATR = icc_power_on(this.cardType);
                if (this.mATR == null) {
                    return false;
                }
            } else {
                Log.d("idcard demo", "reader_type:" + this.reader_type);
                byte[] status = new byte[1];
                byte[] bArr = new byte[1];
                if (this.reader == null || this.reader.getCardStatus(status) != 0 || status[0] == 2) {
                    return false;
                }
                int result = this.reader.setPower(1);
                if (result != 0) {
                    Log.e(TAG, "setPower failed: " + result);
                    return false;
                }
            }
            if (this.mSlot == 0) {
                Collect.collectInfo(2, 1, (byte[]) null);
            } else {
                Collect.collectInfo(3, 1, (byte[]) null);
            }
            return true;
        }
    }

    public boolean iccPowerOff() {
        if (SystemUtil.getDeviceType() == StringUtil.DeviceModelEnum.TPS900.ordinal() || SystemUtil.getDeviceType() == StringUtil.DeviceModelEnum.TPS900MB.ordinal()) {
            try {
                this.mICCardReader.power_off(this.mSlot);
                return true;
            } catch (TelpoException e) {
                e.printStackTrace();
                return false;
            }
        } else {
            if (this.reader_type != 2 && this.reader_type != 1 && this.reader_type != 0) {
                byte[] status = new byte[1];
                if (this.reader == null || this.reader.getCardStatus(status) != 0 || status[0] == 2 || this.reader.setPower(-1) != 0) {
                    return false;
                }
            } else if (icc_power_off() < 0) {
                return false;
            }
            this.mATR = null;
            this.correct_psc_verification = false;
            return true;
        }
    }

    public boolean iccPowerOff(int ic360) {
        if (SystemUtil.getDeviceType() == StringUtil.DeviceModelEnum.TPS900.ordinal() || SystemUtil.getDeviceType() == StringUtil.DeviceModelEnum.TPS900MB.ordinal() || SystemUtil.getDeviceType() == StringUtil.DeviceModelEnum.TPS360IC.ordinal()) {
            try {
                this.mICCardReader.power_off(this.mSlot);
                return true;
            } catch (TelpoException e) {
                e.printStackTrace();
                return false;
            }
        } else {
            if (this.reader_type != 2 && this.reader_type != 1 && this.reader_type != 0) {
                byte[] status = new byte[1];
                if (this.reader == null || this.reader.getCardStatus(status) != 0 || status[0] == 2 || this.reader.setPower(-1) != 0) {
                    return false;
                }
            } else if (icc_power_off() < 0) {
                return false;
            }
            this.mATR = null;
            this.correct_psc_verification = false;
            return true;
        }
    }

    public int getICCStatus() {
        if (this.reader_type == 2 || this.reader_type == 1 || this.reader_type == 0) {
            int iccStatus = get_card_status();
            if (iccStatus < 0) {
                return 2;
            }
            return iccStatus;
        }
        byte[] status = new byte[1];
        if (this.reader.getCardStatus(status) != 0) {
            return 2;
        }
        if (status[0] == 0) {
            return 0;
        }
        if (status[0] == 1) {
            return 1;
        }
        return 2;
    }

    public boolean isICCPresent() {
        if (SystemUtil.getDeviceType() == StringUtil.DeviceModelEnum.TPS900.ordinal() || SystemUtil.getDeviceType() == StringUtil.DeviceModelEnum.TPS900MB.ordinal()) {
            try {
                if (this.mICCardReader.detect(this.mSlot, 500) == 0) {
                    return true;
                }
                return false;
            } catch (TelpoException e) {
                e.printStackTrace();
                return false;
            }
        } else {
            if (this.reader_type == 2 || this.reader_type == 1 || this.reader_type == 0) {
                int status = get_card_status();
                if (status == 0 || status == 1) {
                    return true;
                }
            } else {
                byte[] status2 = new byte[1];
                if (this.reader.getCardStatus(status2) == 0 && status2[0] != 2) {
                    return true;
                }
            }
            return false;
        }
    }

    public boolean isICCPresent(int ic360) {
        if (SystemUtil.getDeviceType() == StringUtil.DeviceModelEnum.TPS900.ordinal() || SystemUtil.getDeviceType() == StringUtil.DeviceModelEnum.TPS900MB.ordinal() || SystemUtil.getDeviceType() == StringUtil.DeviceModelEnum.TPS360IC.ordinal()) {
            try {
                if (this.mICCardReader.detect(this.mSlot, 500) == 0) {
                    return true;
                }
                return false;
            } catch (TelpoException e) {
                e.printStackTrace();
                return false;
            }
        } else {
            if (this.reader_type == 2 || this.reader_type == 1 || this.reader_type == 0) {
                int status = get_card_status();
                if (status == 0 || status == 1) {
                    return true;
                }
            } else {
                byte[] status2 = new byte[1];
                if (this.reader.getCardStatus(status2) == 0 && status2[0] != 2) {
                    return true;
                }
            }
            return false;
        }
    }

    public String getATRString() {
        if (SystemUtil.getDeviceType() == StringUtil.DeviceModelEnum.TPS900.ordinal() || SystemUtil.getDeviceType() == StringUtil.DeviceModelEnum.TPS900MB.ordinal()) {
            try {
                return this.mICCardReader.getAtr(this.mSlot);
            } catch (TelpoException e) {
                e.printStackTrace();
                return null;
            }
        } else if (this.reader_type != 2 && this.reader_type != 1 && this.reader_type != 0) {
            return this.reader.getAtrString();
        } else {
            if (this.mATR != null) {
                return StringUtil.toHexString(this.mATR);
            }
            return null;
        }
    }

    public String getATRString(int isic360) {
        if (SystemUtil.getDeviceType() == StringUtil.DeviceModelEnum.TPS900.ordinal() || SystemUtil.getDeviceType() == StringUtil.DeviceModelEnum.TPS900MB.ordinal() || SystemUtil.getDeviceType() == StringUtil.DeviceModelEnum.TPS360IC.ordinal()) {
            try {
                return this.mICCardReader.getAtr(this.mSlot);
            } catch (TelpoException e) {
                e.printStackTrace();
                return null;
            }
        } else if (this.reader_type != 2 && this.reader_type != 1 && this.reader_type != 0) {
            return this.reader.getAtrString();
        } else {
            if (this.mATR != null) {
                return StringUtil.toHexString(this.mATR);
            }
            return null;
        }
    }

    public int getCardType() {
        String atr = getATRString();
        if (atr == null) {
            return -1;
        }
        String atrString = atr.replace(" ", "");
        Log.i(TAG, "ATR: " + atrString);
        if (atrString.contains("A2131091")) {
            return 2;
        }
        if (atrString.contains("92231091")) {
            return 3;
        }
        return -1;
    }

    /* JADX WARNING: Removed duplicated region for block: B:11:0x0020 A[RETURN, SYNTHETIC] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean switchMode(int r5) {
        /*
            r4 = this;
            r3 = 3
            r2 = 2
            r0 = 1
            int r1 = r4.reader_type
            if (r1 == r2) goto L_0x000f
            int r1 = r4.reader_type
            if (r1 == r0) goto L_0x000f
            int r1 = r4.reader_type
            if (r1 != 0) goto L_0x0016
        L_0x000f:
            int r1 = switch_mode(r5)
            if (r1 != 0) goto L_0x0020
        L_0x0015:
            return r0
        L_0x0016:
            if (r5 != r0) goto L_0x0022
            amlib.ccid.Reader r1 = r4.reader
            int r1 = r1.switchMode(r0)
            if (r1 == 0) goto L_0x0015
        L_0x0020:
            r0 = 0
            goto L_0x0015
        L_0x0022:
            if (r5 != r2) goto L_0x002e
            amlib.ccid.Reader r1 = r4.reader
            r2 = 4
            int r1 = r1.switchMode(r2)
            if (r1 != 0) goto L_0x0020
            goto L_0x0015
        L_0x002e:
            if (r5 != r3) goto L_0x0020
            amlib.ccid.Reader r1 = r4.reader
            int r1 = r1.switchMode(r3)
            if (r1 != 0) goto L_0x0020
            goto L_0x0015
        */
        throw new UnsupportedOperationException("Method not decompiled: com.telpo.tps550.api.reader.CardReader.switchMode(int):boolean");
    }

    private UsbDevice getUsbDevice() {
        String deviceName;
        if (SystemUtil.getDeviceType() == StringUtil.DeviceModelEnum.TPS510A_NHW.ordinal() || SystemUtil.getDeviceType() == StringUtil.DeviceModelEnum.TPS510D.ordinal()) {
            searchAllIndex(ShellUtils.execCommand("cat /sys/kernel/debug/usb/devices", false).successMsg, "Port=", 1);
            searchAllIndex(ShellUtils.execCommand("cat /sys/kernel/debug/usb/devices", false).successMsg, "Product=", 2);
            checkPort(this.portNum, this.productNum);
            if ((this.readerNum[0] == null || !this.readerNum[0].equals("05")) && ((this.readerNum[1] == null || !this.readerNum[1].equals("05")) && ((this.readerNum[2] == null || !this.readerNum[2].equals("05")) && (this.readerNum[3] == null || !this.readerNum[3].equals("05"))))) {
                Log.d("idcard demo", "new usb_utils");
                deviceName = UsbUtil.getUsbDevicehub(this.mSlot, 1);
            } else {
                Log.d("idcard demo", "old usb_utils");
                deviceName = UsbUtil.getUsbDevice(this.mSlot);
            }
        } else {
            deviceName = UsbUtil.getUsbDevice(this.mSlot);
        }
        if (deviceName != null) {
            for (UsbDevice device : this.usbManager.getDeviceList().values()) {
                if (deviceName.equals(device.getDeviceName())) {
                    switchPsam();
                    return device;
                }
            }
        }
        return null;
    }

    private void searchAllIndex(String str, String key, int type) {
        int a = str.indexOf(key);
        int i = -1;
        while (a != -1) {
            i++;
            if (type == 1) {
                this.portNum[i] = str.substring(a + 5, a + 7);
            } else if (type == 2) {
                this.productNum[i] = str.substring(a + 8, a + 11);
            }
            a = str.indexOf(key, a + 1);
        }
    }

    private void checkPort(String[] port, String[] product) {
        int k = -1;
        for (int i = 0; i < 20; i++) {
            if (this.productNum[i] != null && this.productNum[i].equals("EMV")) {
                k++;
                this.readerNum[k] = this.portNum[i];
                Log.d("idcard demo", "readnum[]:" + this.readerNum[k]);
            }
        }
    }

    private void toRegisterReceiver() {
        this.permissionIntent = PendingIntent.getBroadcast(this.context, 0, new Intent(ACTION_USB_PERMISSION), 0);
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_USB_PERMISSION);
        filter.addAction("android.hardware.usb.action.USB_DEVICE_DETACHED");
        this.context.registerReceiver(this.mReceiver, filter, (String) null, this.handler);
    }

    private void readerPoweron() {
        String[] paths = {"/sys/devices/platform/battery/GPIO30_PIN", "/sys/devices/platform/battery/GPIO31_PIN", "/sys/devices/platform/battery/GPIO142_PIN", "/sys/devices/platform/battery/GPIO145_PIN"};
        int i = 0;
        while (i < paths.length) {
            try {
                FileOutputStream fon = new FileOutputStream(paths[i]);
                fon.write(49);
                fon.flush();
                fon.close();
                i++;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        try {
            Thread.sleep(100);
        } catch (InterruptedException e2) {
            e2.printStackTrace();
        }
    }

    private void readerPoweroff() {
        String[] paths = {"/sys/devices/platform/battery/GPIO30_PIN", "/sys/devices/platform/battery/GPIO31_PIN", "/sys/devices/platform/battery/GPIO142_PIN", "/sys/devices/platform/battery/GPIO145_PIN"};
        int i = 0;
        while (i < paths.length) {
            try {
                FileOutputStream fon = new FileOutputStream(paths[i]);
                fon.write(48);
                fon.flush();
                fon.close();
                i++;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        try {
            Thread.sleep(100);
        } catch (InterruptedException e2) {
            e2.printStackTrace();
        }
    }

    private int switchPsam() {
        if (SystemUtil.getDeviceType() == StringUtil.DeviceModelEnum.TPS510A.ordinal() || SystemUtil.getDeviceType() == StringUtil.DeviceModelEnum.TPS510A_NHW.ordinal() || SystemUtil.getDeviceType() == StringUtil.DeviceModelEnum.TPS510D.ordinal() || SystemUtil.getDeviceType() == StringUtil.DeviceModelEnum.TPS450C.ordinal()) {
            if (this.mSlot == 2) {
                return telpo_switch_psam(2);
            }
            if (this.mSlot == 3) {
                return telpo_switch_psam(3);
            }
            return 0;
        } else if (SystemUtil.getDeviceType() == StringUtil.DeviceModelEnum.TPS613.ordinal()) {
            if (this.mSlot == 1) {
                return telpo_switch_psam(1);
            }
            if (this.mSlot == 2) {
                return telpo_switch_psam(2);
            }
            return 0;
        } else if (SystemUtil.getDeviceType() == StringUtil.DeviceModelEnum.TPS390U.ordinal()) {
            return 0;
        } else {
            try {
                FileOutputStream fon = new FileOutputStream("/sys/class/hdxio/psam_select");
                if (this.mSlot == 2) {
                    if (SystemUtil.getDeviceType() == StringUtil.DeviceModelEnum.FFP2.ordinal()) {
                        fon.write(50);
                    } else {
                        fon.write(49);
                    }
                } else if (this.mSlot == 3) {
                    fon.write(51);
                } else {
                    fon.write(49);
                }
                fon.flush();
                fon.close();
                return 0;
            } catch (Exception e) {
                e.printStackTrace();
                Log.e(TAG, "switch slot " + this.mSlot + " failed");
                return -1;
            }
        }
    }

    static {
        if (SystemUtil.getDeviceType() != StringUtil.DeviceModelEnum.TPS900.ordinal() && SystemUtil.getDeviceType() != StringUtil.DeviceModelEnum.TPS900MB.ordinal()) {
            System.loadLibrary("card_reader");
        }
    }
}
