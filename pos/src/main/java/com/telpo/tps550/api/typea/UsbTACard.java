package com.telpo.tps550.api.typea;

import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.util.Log;
import com.telpo.tps550.api.idcard.IdCard;
import com.telpo.tps550.api.util.StringUtil;
import java.util.Arrays;

public class UsbTACard {
    private final byte[] TSAM_HEADER = {-86, -86, -86, -106, 105};
    private UsbManager tUsbManager = null;
    private UsbDevice tcard_reader = null;

    public UsbTACard(UsbDevice tcard_reader2, UsbManager tUsbManager2) {
        this.tcard_reader = tcard_reader2;
        this.tUsbManager = tUsbManager2;
    }

    public TAInfo checkTACard() {
        byte[] cmd_read_uid = new byte[11];
        cmd_read_uid[0] = -86;
        cmd_read_uid[1] = -86;
        cmd_read_uid[2] = -86;
        cmd_read_uid[3] = -106;
        cmd_read_uid[4] = 105;
        cmd_read_uid[6] = 4;
        cmd_read_uid[7] = Byte.MIN_VALUE;
        cmd_read_uid[8] = 5;
        cmd_read_uid[9] = 16;
        cmd_read_uid[10] = -111;
        byte[] bArr = new byte[3];
        bArr[0] = 5;
        bArr[2] = -112;
        byte[] uid = requestUid(cmd_read_uid, bArr);
        if (uid == null) {
            return null;
        }
        String newuid = decodetcarduid(uid);
        TAInfo tinfo = new TAInfo();
        tinfo.setNum(newuid);
        return tinfo;
    }

    public TAInfo checkTACard(int timeout) {
        long startTime = System.currentTimeMillis();
        long endTime = System.currentTimeMillis();
        byte[] cmd_read_uid = new byte[11];
        cmd_read_uid[0] = -86;
        cmd_read_uid[1] = -86;
        cmd_read_uid[2] = -86;
        cmd_read_uid[3] = -106;
        cmd_read_uid[4] = 105;
        cmd_read_uid[6] = 4;
        cmd_read_uid[7] = Byte.MIN_VALUE;
        cmd_read_uid[8] = 5;
        cmd_read_uid[9] = 16;
        cmd_read_uid[10] = -111;
        byte[] uid = null;
        while (uid == null && endTime - startTime < ((long) timeout)) {
            byte[] bArr = new byte[3];
            bArr[0] = 5;
            bArr[2] = -112;
            uid = requestUid(cmd_read_uid, bArr);
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            endTime = System.currentTimeMillis();
        }
        if (uid == null) {
            return null;
        }
        String newuid = decodetcarduid(uid);
        TAInfo tinfo = new TAInfo();
        tinfo.setNum(newuid);
        return tinfo;
    }

    public byte[] requestUid(byte[] tcmd, byte[] sw) {
        if (this.tcard_reader == null || sw.length != 3) {
            return null;
        }
        UsbInterface tInterface = this.tcard_reader.getInterface(0);
        UsbEndpoint iEndpoint = tInterface.getEndpoint(0);
        UsbEndpoint oEndpoint = tInterface.getEndpoint(1);
        UsbDeviceConnection tconnection = this.tUsbManager.openDevice(this.tcard_reader);
        if (tconnection == null) {
            return null;
        }
        tconnection.claimInterface(tInterface, true);
        int bulkTransfer = tconnection.bulkTransfer(oEndpoint, tcmd, tcmd.length, 3000);
        try {
            Thread.sleep(500);
        } catch (InterruptedException a) {
            a.printStackTrace();
        }
        byte[] rec = new byte[IdCard.READER_VID_BIG];
        int bulkTransfer2 = tconnection.bulkTransfer(iEndpoint, rec, rec.length, 3000);
        for (int i = 0; i < this.TSAM_HEADER.length; i++) {
            if (rec[i] != this.TSAM_HEADER[i]) {
                tconnection.close();
                return null;
            }
        }
        for (int i2 = 0; i2 < sw.length; i2++) {
            if (rec[this.TSAM_HEADER.length + i2 + 2] != sw[i2]) {
                tconnection.close();
                return null;
            }
        }
        tconnection.close();
        return Arrays.copyOfRange(rec, this.TSAM_HEADER.length + 5, this.TSAM_HEADER.length + 9);
    }

    public String decodetcarduid(byte[] uid) {
        StringBuilder ret = new StringBuilder("");
        for (byte b : uid) {
            String hex = Integer.toHexString(b & 255);
            if (hex.length() == 1) {
                hex = String.valueOf('0') + hex;
            }
            ret.append(hex);
        }
        return ret.toString();
    }

    public Boolean checkPW(byte[] Password) {
        if (Password.length > 7) {
            return false;
        }
        byte[] cmd_pwcheck = new byte[17];
        byte[] bArr = new byte[10];
        bArr[0] = -86;
        bArr[1] = -86;
        bArr[2] = -86;
        bArr[3] = -106;
        bArr[4] = 105;
        bArr[6] = 16;
        bArr[7] = Byte.MIN_VALUE;
        bArr[8] = 11;
        bArr[9] = 1;
        System.arraycopy(bArr, 0, cmd_pwcheck, 0, 10);
        for (int i = 0; i < Password.length; i++) {
            cmd_pwcheck[i + 10] = Password[i];
        }
        cmd_pwcheck[cmd_pwcheck.length - 1] = -102;
        Log.d("idcard demo", "cmd_pwcheck UsbTACard: " + StringUtil.toHexString(cmd_pwcheck));
        byte[] bArr2 = new byte[3];
        bArr2[0] = 11;
        bArr2[2] = -112;
        return requestpw(cmd_pwcheck, bArr2);
    }

    public Boolean requestpw(byte[] pcmd, byte[] psw) {
        if (this.tcard_reader == null || psw.length != 3) {
            return null;
        }
        UsbInterface pInterface = this.tcard_reader.getInterface(0);
        UsbEndpoint iEndpoint = pInterface.getEndpoint(0);
        UsbEndpoint oEndpoint = pInterface.getEndpoint(1);
        UsbDeviceConnection pconnection = this.tUsbManager.openDevice(this.tcard_reader);
        if (pconnection == null) {
            return null;
        }
        pconnection.claimInterface(pInterface, true);
        int bulkTransfer = pconnection.bulkTransfer(oEndpoint, pcmd, pcmd.length, 3000);
        try {
            Thread.sleep(500);
        } catch (InterruptedException a) {
            a.printStackTrace();
        }
        byte[] prec = new byte[IdCard.READER_VID_BIG];
        int bulkTransfer2 = pconnection.bulkTransfer(iEndpoint, prec, prec.length, 3000);
        for (int i = 0; i < this.TSAM_HEADER.length; i++) {
            if (prec[i] != this.TSAM_HEADER[i]) {
                pconnection.close();
                return null;
            }
        }
        for (int i2 = 0; i2 < psw.length; i2++) {
            if (prec[this.TSAM_HEADER.length + i2 + 2] != psw[i2]) {
                pconnection.close();
                return false;
            }
        }
        pconnection.close();
        return true;
    }

    public TASectorInfo readData() {
        byte[] cmd_read_data = new byte[13];
        cmd_read_data[0] = -86;
        cmd_read_data[1] = -86;
        cmd_read_data[2] = -86;
        cmd_read_data[3] = -106;
        cmd_read_data[4] = 105;
        cmd_read_data[6] = 6;
        cmd_read_data[7] = Byte.MIN_VALUE;
        cmd_read_data[8] = 13;
        cmd_read_data[9] = 1;
        cmd_read_data[10] = 1;
        cmd_read_data[11] = 16;
        cmd_read_data[12] = -101;
        Log.d("idcard demo", "readData UsbTACard: " + StringUtil.toHexString(cmd_read_data));
        byte[] bArr = new byte[3];
        bArr[0] = 13;
        bArr[2] = -112;
        byte[] data = requestData(cmd_read_data, bArr);
        if (data == null) {
            Log.d("idcard demo", "readData data is null");
            return null;
        }
        String newdata = decodetcarduid(data);
        TASectorInfo tsectorinfo = new TASectorInfo();
        tsectorinfo.setSectorData(newdata);
        return tsectorinfo;
    }

    public byte[] requestData(byte[] dcmd, byte[] dsw) {
        if (this.tcard_reader == null || dsw.length != 3) {
            return null;
        }
        UsbInterface dInterface = this.tcard_reader.getInterface(0);
        UsbEndpoint iEndpoint = dInterface.getEndpoint(0);
        UsbEndpoint oEndpoint = dInterface.getEndpoint(1);
        UsbDeviceConnection dconnection = this.tUsbManager.openDevice(this.tcard_reader);
        if (dconnection == null) {
            return null;
        }
        dconnection.claimInterface(dInterface, true);
        int bulkTransfer = dconnection.bulkTransfer(oEndpoint, dcmd, dcmd.length, 3000);
        try {
            Thread.sleep(500);
        } catch (InterruptedException a) {
            a.printStackTrace();
        }
        byte[] drec = new byte[IdCard.READER_VID_BIG];
        Log.d("idcard demo", "requestData is: " + StringUtil.toHexString(Arrays.copyOfRange(drec, 0, dconnection.bulkTransfer(iEndpoint, drec, drec.length, 3000) - 1)));
        for (int i = 0; i < this.TSAM_HEADER.length; i++) {
            if (drec[i] != this.TSAM_HEADER[i]) {
                dconnection.close();
                return null;
            }
        }
        for (int i2 = 0; i2 < dsw.length; i2++) {
            if (drec[this.TSAM_HEADER.length + i2 + 2] != dsw[i2]) {
                dconnection.close();
                return null;
            }
        }
        dconnection.close();
        return Arrays.copyOfRange(drec, this.TSAM_HEADER.length + 5, this.TSAM_HEADER.length + 21);
    }

    public Boolean writeInData(String newwritedata) {
        byte[] Newwritedata = hexStringToBytes(newwritedata);
        byte[] cmd_write_data = new byte[29];
        byte[] bArr = new byte[12];
        bArr[0] = -86;
        bArr[1] = -86;
        bArr[2] = -86;
        bArr[3] = -106;
        bArr[4] = 105;
        bArr[6] = 22;
        bArr[7] = Byte.MIN_VALUE;
        bArr[8] = 14;
        bArr[9] = 1;
        bArr[10] = 1;
        bArr[11] = 16;
        System.arraycopy(bArr, 0, cmd_write_data, 0, 12);
        if (Newwritedata == null) {
            return null;
        }
        if (Newwritedata == null) {
            return null;
        }
        for (int i = 0; i < Newwritedata.length; i++) {
            cmd_write_data[i + 12] = Newwritedata[i];
        }
        if (Newwritedata.length < 16) {
            int repairzero = 16 - Newwritedata.length;
            for (int d = 0; d < repairzero; d++) {
                cmd_write_data[Newwritedata.length + 12 + d] = 0;
            }
        }
        byte[] cmd_checksum = new byte[23];
        byte[] bArr2 = new byte[7];
        bArr2[1] = 22;
        bArr2[2] = Byte.MIN_VALUE;
        bArr2[3] = 14;
        bArr2[4] = 1;
        bArr2[5] = 1;
        bArr2[6] = 16;
        System.arraycopy(bArr2, 0, cmd_checksum, 0, 7);
        for (int i2 = 0; i2 < Newwritedata.length; i2++) {
            cmd_checksum[i2 + 7] = Newwritedata[i2];
        }
        cmd_write_data[cmd_write_data.length - 1] = crc(cmd_checksum);
        Log.d("idcard demo", "cmd write data is: " + StringUtil.toHexString(cmd_write_data));
        byte[] bArr3 = new byte[3];
        bArr3[0] = 14;
        bArr3[2] = -112;
        Boolean writefeeback = requestwrite(cmd_write_data, bArr3);
        Boolean bool = writefeeback;
        return writefeeback;
    }

    private byte[] hexStringToBytes(String hexString) {
        if (hexString == null || hexString.equals("")) {
            return null;
        }
        String hexString2 = hexString.toUpperCase();
        int length = hexString2.length() / 2;
        char[] hexChars = hexString2.toCharArray();
        byte[] d = new byte[length];
        for (int i = 0; i < length; i++) {
            int pos = i * 2;
            d[i] = (byte) ((charToByte(hexChars[pos]) << 4) | charToByte(hexChars[pos + 1]));
        }
        return d;
    }

    private byte charToByte(char c) {
        return (byte) "0123456789ABCDEF".indexOf(c);
    }

    private byte crc(byte[] data) {
        byte crc = 0;
        for (byte b : data) {
            crc = (byte) (crc ^ b);
        }
        return crc;
    }

    public Boolean requestwrite(byte[] wcmd, byte[] wsw) {
        if (this.tcard_reader == null || wsw.length != 3) {
            return null;
        }
        UsbInterface wInterface = this.tcard_reader.getInterface(0);
        UsbEndpoint iEndpoint = wInterface.getEndpoint(0);
        UsbEndpoint oEndpoint = wInterface.getEndpoint(1);
        UsbDeviceConnection wconnection = this.tUsbManager.openDevice(this.tcard_reader);
        if (wconnection == null) {
            return null;
        }
        wconnection.claimInterface(wInterface, true);
        int bulkTransfer = wconnection.bulkTransfer(oEndpoint, wcmd, wcmd.length, 3000);
        try {
            Thread.sleep(500);
        } catch (InterruptedException a) {
            a.printStackTrace();
        }
        byte[] wrec = new byte[IdCard.READER_VID_BIG];
        int bulkTransfer2 = wconnection.bulkTransfer(iEndpoint, wrec, wrec.length, 3000);
        for (int i = 0; i < this.TSAM_HEADER.length; i++) {
            if (wrec[i] != this.TSAM_HEADER[i]) {
                wconnection.close();
                return null;
            }
        }
        for (int i2 = 0; i2 < wsw.length; i2++) {
            if (wrec[this.TSAM_HEADER.length + i2 + 2] != wsw[i2]) {
                wconnection.close();
                return false;
            }
        }
        wconnection.close();
        return true;
    }
}
