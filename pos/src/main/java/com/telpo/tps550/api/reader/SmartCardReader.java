package com.telpo.tps550.api.reader;

import android.content.Context;
import android.util.Log;
import com.telpo.tps550.api.TelpoException;
import com.telpo.tps550.api.util.StringUtil;
import com.telpo.tps550.api.util.SystemUtil;
import java.util.Arrays;

public class SmartCardReader extends CardReader {
    public static final int PROTOCOL_NA = 2;
    public static final int PROTOCOL_T0 = 0;
    public static final int PROTOCOL_T1 = 1;
    public static final int SLOT_ICC = 0;
    public static final int SLOT_PSAM1 = 1;
    public static final int SLOT_PSAM2 = 2;
    public static final int SLOT_PSAM3 = 3;
    private static final String TAG = "SmartCardReader";

    public SmartCardReader() {
        this.cardType = 1;
        this.mSlot = 0;
    }

    public SmartCardReader(Context context) {
        this.context = context;
        this.cardType = 1;
        this.mSlot = 0;
        this.mICCardReader = new ICCardReader(context);
    }

    public SmartCardReader(Context context, int slot) {
        this.context = context;
        this.cardType = 1;
        this.mSlot = slot;
        this.mICCardReader = new ICCardReader(context);
    }

    public byte[] transmit(byte[] apdu) throws NullPointerException {
        if (apdu == null) {
            throw new NullPointerException();
        } else if (SystemUtil.getDeviceType() == StringUtil.DeviceModelEnum.TPS900.ordinal() || SystemUtil.getDeviceType() == StringUtil.DeviceModelEnum.TPS900MB.ordinal()) {
            try {
                return this.mICCardReader.transmit(this.mSlot, apdu, apdu.length);
            } catch (TelpoException e) {
                e.printStackTrace();
                return null;
            }
        } else {
            byte[] pRecvRes = new byte[512];
            if (this.reader_type == 2 || this.reader_type == 1 || this.reader_type == 0) {
                int len = send_apdu(apdu, pRecvRes);
                if (len > 0) {
                    return Arrays.copyOf(pRecvRes, len);
                }
                return null;
            }
            int[] pRevAPDULen = new int[1];
            int result = this.reader.transmit(apdu, apdu.length, pRecvRes, pRevAPDULen);
            if (result == 0) {
                return Arrays.copyOf(pRecvRes, pRevAPDULen[0]);
            }
            Log.e(TAG, "send APDU failed: " + result);
            return null;
        }
    }

    public byte[] transmit(byte[] apdu, int ic360) throws NullPointerException {
        if (apdu == null) {
            throw new NullPointerException();
        } else if (SystemUtil.getDeviceType() == StringUtil.DeviceModelEnum.TPS900.ordinal() || SystemUtil.getDeviceType() == StringUtil.DeviceModelEnum.TPS900MB.ordinal() || SystemUtil.getDeviceType() == StringUtil.DeviceModelEnum.TPS360IC.ordinal()) {
            try {
                return this.mICCardReader.transmit(this.mSlot, apdu, apdu.length);
            } catch (TelpoException e) {
                e.printStackTrace();
                return null;
            }
        } else {
            byte[] pRecvRes = new byte[512];
            if (this.reader_type == 2 || this.reader_type == 1 || this.reader_type == 0) {
                int len = send_apdu(apdu, pRecvRes);
                if (len > 0) {
                    return Arrays.copyOf(pRecvRes, len);
                }
                return null;
            }
            int[] pRevAPDULen = new int[1];
            int result = this.reader.transmit(apdu, apdu.length, pRecvRes, pRevAPDULen);
            if (result == 0) {
                return Arrays.copyOf(pRecvRes, pRevAPDULen[0]);
            }
            Log.e(TAG, "send APDU failed: " + result);
            return null;
        }
    }

    public int getProtocol() {
        if (SystemUtil.getDeviceType() == StringUtil.DeviceModelEnum.TPS900.ordinal() || SystemUtil.getDeviceType() == StringUtil.DeviceModelEnum.TPS900MB.ordinal()) {
            return 0;
        }
        if (this.reader_type != 2 && this.reader_type != 1 && this.reader_type != 0) {
            byte[] proto = new byte[1];
            int result = this.reader.getProtocol(proto);
            if (result != 0) {
                Log.e(TAG, "get protocol failed: " + result);
            } else if (proto[0] == 1) {
                return 0;
            } else {
                if (proto[0] == 2) {
                    return 1;
                }
            }
        } else if (this.mATR != null) {
            if (((this.mATR[1] >> 7) & 1) != 1) {
                return 0;
            }
            int c = 0;
            for (int i = 4; i < 7; i++) {
                if (((this.mATR[1] >> i) & 1) != 0) {
                    c++;
                }
            }
            int t = this.mATR[c + 2] & 15;
            if (t == 0) {
                return 0;
            }
            if (t == 1) {
                return 1;
            }
        }
        return 2;
    }

    public int getProtocol(int ic360) {
        if (SystemUtil.getDeviceType() == StringUtil.DeviceModelEnum.TPS900.ordinal() || SystemUtil.getDeviceType() == StringUtil.DeviceModelEnum.TPS900MB.ordinal() || SystemUtil.getDeviceType() == StringUtil.DeviceModelEnum.TPS360IC.ordinal()) {
            return 0;
        }
        if (this.reader_type != 2 && this.reader_type != 1 && this.reader_type != 0) {
            byte[] proto = new byte[1];
            int result = this.reader.getProtocol(proto);
            if (result != 0) {
                Log.e(TAG, "get protocol failed: " + result);
            } else if (proto[0] == 1) {
                return 0;
            } else {
                if (proto[0] == 2) {
                    return 1;
                }
            }
        } else if (this.mATR != null) {
            if (((this.mATR[1] >> 7) & 1) != 1) {
                return 0;
            }
            int c = 0;
            for (int i = 4; i < 7; i++) {
                if (((this.mATR[1] >> i) & 1) != 0) {
                    c++;
                }
            }
            int t = this.mATR[c + 2] & 15;
            if (t == 0) {
                return 0;
            }
            if (t == 1) {
                return 1;
            }
        }
        return 2;
    }

    public int set_mode(int slot, int mode) {
        if (this.mICCardReader == null) {
            return -1;
        }
        try {
            return this.mICCardReader.set_mode(slot, mode);
        } catch (TelpoException e) {
            e.printStackTrace();
            return -1;
        }
    }
}
