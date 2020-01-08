package com.telpo.tps550.api.iccard;

import android.content.Context;
import android.content.Intent;
import com.telpo.tps550.api.DeviceNotOpenException;
import com.telpo.tps550.api.InternalErrorException;
import com.telpo.tps550.api.NotSupportYetException;
import com.telpo.tps550.api.TelpoException;
import com.telpo.tps550.api.TimeoutException;
import com.telpo.tps550.api.collect.Collect;

public class Picc {
    public static final int PICC_CARD_TYPE_CPU = 1;
    public static final int PICC_CARD_TYPE_ERROER = -1;
    public static final int PICC_CARD_TYPE_M1 = 2;
    public static final int PICC_CARD_TYPE_UL = 3;
    public static final int PICC_M1_TYPE_A = 0;
    public static final int PICC_M1_TYPE_B = 1;

    private static native int check_card(byte[] bArr, int[] iArr);

    private static native int check_card_sak(byte[] bArr, int[] iArr, byte[] bArr2, byte[] bArr3);

    private static native int close();

    private static native int enter_cpu_model();

    private static native int get_last_error();

    private static native int get_reader_info(byte[] bArr);

    private static native int halt_card();

    private static native int m1_add_sub(int i, int i2, byte[] bArr);

    private static native int m1_authority(int i, int i2, byte[] bArr);

    private static native int m1_read(int i, byte[] bArr);

    private static native int m1_write(int i, byte[] bArr);

    private static native int open(int i);

    private static native int reset_card();

    private static native int set_baudrate(int i);

    private static native int transmit(byte[] bArr, int i, byte[] bArr2, int[] iArr);

    private static native int ultralight_read(int i, byte[] bArr);

    private static native int ultralight_write(int i, byte[] bArr);

    static {
        System.loadLibrary("picc");
    }

    private static TelpoException getException(int ret) {
        switch (get_last_error()) {
            case 0:
                switch (ret) {
                    case -10:
                        return new NotSupportYetException();
                    case -1:
                        return new DeviceNotOpenException();
                    default:
                        return new InternalErrorException();
                }
            case 1:
                return new RemovedCardException("There is no valid card found!");
            case 4:
            case Collect.TYPE_OCR:
                return new AuthorityFailException();
            case Collect.TYPE_QRCODE:
                return new NoAuthorityCardException();
            case 27:
                return new TimeoutException();
            default:
                return new CommunicationErrorException();
        }
    }

    public static void openReader() throws TelpoException {
        int ret = open(9600);
        if (ret != 0) {
            throw getException(ret);
        }
    }

    public static void openReader(int baudrate) throws TelpoException {
        if (baudrate == 4800 || baudrate == 9600 || baudrate == 19200 || baudrate == 38400 || baudrate == 57600 || baudrate == 115200) {
            int ret = open(baudrate);
            if (ret != 0) {
                throw getException(ret);
            }
            return;
        }
        throw new IllegalArgumentException();
    }

    public static void openReader(Context context) throws TelpoException {
        int ret = open(9600);
        if (ret != 0) {
            throw getException(ret);
        }
        context.sendBroadcast(new Intent("com.telpo.rfid.picc.start"));
    }

    public static void closeReader() {
        close();
    }

    public static void closeReader(Context context) {
        close();
        context.sendBroadcast(new Intent("com.telpo.rfid.picc.stop"));
    }

    public static int selectCard(byte[] sn, byte[] sak, byte[] tag) throws TelpoException {
        int[] recLen = new int[2];
        if (sn == null) {
            throw new IllegalArgumentException();
        }
        int ret = check_card_sak(sn, recLen, sak, tag);
        if (ret == 0) {
            return recLen[0];
        }
        throw getException(ret);
    }

    public static void haltCard() throws TelpoException {
        int ret = halt_card();
        if (ret != 0) {
            throw getException(ret);
        }
    }

    public static void resetReader() throws TelpoException {
        int ret = reset_card();
        if (ret != 0) {
            throw getException(ret);
        }
    }

    public static int command(byte[] snd, int sndLen, byte[] rcv) throws TelpoException {
        int[] recLen = new int[2];
        if (snd == null || rcv == null) {
            throw new IllegalArgumentException();
        }
        int ret = transmit(snd, sndLen, rcv, recLen);
        if (ret == 0) {
            return recLen[0];
        }
        throw getException(ret);
    }

    public static void m1Authority(int type, int sectorNo, byte[] password) throws TelpoException {
        if ((type == 0 || type == 1) && password != null) {
            int ret = m1_authority(type, sectorNo, password);
            if (ret != 0) {
                throw getException(ret);
            }
            return;
        }
        throw new IllegalArgumentException();
    }

    public static void m1Read(int blockNo, byte[] buffer) throws TelpoException {
        if (buffer == null) {
            throw new IllegalArgumentException();
        }
        int ret = m1_read(blockNo, buffer);
        if (ret != 0) {
            throw getException(ret);
        }
    }

    public static void m1Write(int blockNo, byte[] buffer) throws TelpoException {
        if (buffer == null) {
            throw new IllegalArgumentException();
        }
        int ret = m1_write(blockNo, buffer);
        if (ret != 0) {
            throw getException(ret);
        }
    }

    public static void m1Add(int blockNo, byte[] value) throws TelpoException {
        int ret = m1_add_sub(0, blockNo, value);
        if (ret != 0) {
            throw getException(ret);
        }
    }

    public static void m1Sub(int blockNo, byte[] value) throws TelpoException {
        int ret = m1_add_sub(1, blockNo, value);
        if (ret != 0) {
            throw getException(ret);
        }
    }

    public static void getReaderInfo(byte[] result) throws TelpoException {
        if (result == null) {
            throw new IllegalArgumentException();
        }
        int ret = get_reader_info(result);
        if (ret != 0) {
            throw getException(ret);
        }
    }

    public static void ultralightRead(int blockNo, byte[] buffer) throws TelpoException {
        if (buffer == null) {
            throw new IllegalArgumentException();
        }
        int ret = ultralight_read(blockNo, buffer);
        if (ret != 0) {
            throw getException(ret);
        }
    }

    public static void ultralightWrite(int blockNo, byte[] buffer) throws TelpoException {
        if (buffer == null) {
            throw new IllegalArgumentException();
        }
        int ret = ultralight_write(blockNo, buffer);
        if (ret != 0) {
            throw getException(ret);
        }
    }

    public static void setReaderBaudrate(int cnt) throws TelpoException {
        if (cnt == 0 || cnt == 1 || cnt == 2 || cnt == 3 || cnt == 4 || cnt == 5) {
            int ret = set_baudrate(cnt);
            if (ret != 0) {
                throw getException(ret);
            }
            return;
        }
        throw new IllegalArgumentException();
    }

    public static void enterCpuModel() throws TelpoException {
        int ret = enter_cpu_model();
        if (ret != 0) {
            throw getException(ret);
        }
    }
}
