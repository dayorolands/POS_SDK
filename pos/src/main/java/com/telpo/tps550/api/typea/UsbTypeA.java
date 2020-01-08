package com.telpo.tps550.api.typea;

import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.util.Log;
import com.telpo.tps550.api.TelpoException;
import com.telpo.tps550.api.idcard.IdCard;
import com.telpo.tps550.api.util.StringUtil;
import java.util.Arrays;

public class UsbTypeA {
    private static final byte[] TSAM_HEADER = {-86, -86, -86, -106, 105};
    private byte[] Block;
    private byte[] Newwritedata;
    private byte[] Password;
    private int block_;
    private int section_;
    private UsbManager tUsbManager;
    private UsbDevice tcard_reader;

    public UsbTypeA(UsbManager manager, UsbDevice device) throws TelpoException {
        this.tUsbManager = manager;
        this.tcard_reader = device;
    }

    public TAInfo checkTACard() throws TelpoException {
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

    public TAInfo transmittinfo(TAInfo tinfo) {
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
        tconnection.claimInterface(tInterface, true);
        int bulkTransfer = tconnection.bulkTransfer(oEndpoint, tcmd, tcmd.length, 3000);
        try {
            Thread.sleep(500);
        } catch (InterruptedException a) {
            a.printStackTrace();
        }
        byte[] rec = new byte[IdCard.READER_VID_BIG];
        int bulkTransfer2 = tconnection.bulkTransfer(iEndpoint, rec, rec.length, 3000);
        for (int i = 0; i < TSAM_HEADER.length; i++) {
            if (rec[i] != TSAM_HEADER[i]) {
                tconnection.close();
                return null;
            }
        }
        for (int i2 = 0; i2 < sw.length; i2++) {
            if (rec[TSAM_HEADER.length + i2 + 2] != sw[i2]) {
                tconnection.close();
                return null;
            }
        }
        tconnection.close();
        return Arrays.copyOfRange(rec, TSAM_HEADER.length + 5, TSAM_HEADER.length + 9);
    }

    private static String decodetcarduid(byte[] uid) {
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

    public void transmitpassword(byte[] pwd) {
        this.Password = pwd;
    }

    public void transmitblock(byte[] blk) {
        this.Block = blk;
    }

    public Boolean checkPW() throws TelpoException {
        String xorString;
        byte[] psw_check_block_temp;
        byte[] cmd_pwcheck = new byte[17];
        byte[] bArr = new byte[9];
        bArr[0] = -86;
        bArr[1] = -86;
        bArr[2] = -86;
        bArr[3] = -106;
        bArr[4] = 105;
        bArr[6] = 10;
        bArr[7] = Byte.MIN_VALUE;
        bArr[8] = 11;
        System.arraycopy(bArr, 0, cmd_pwcheck, 0, 9);
        cmd_pwcheck[9] = this.Block[0];
        for (int i = 0; i < this.Password.length; i++) {
            cmd_pwcheck[i + 10] = this.Password[i];
        }
        byte[] cmd_checksum = new byte[11];
        byte[] bArr2 = new byte[4];
        bArr2[1] = 10;
        bArr2[2] = Byte.MIN_VALUE;
        bArr2[3] = 11;
        System.arraycopy(bArr2, 0, cmd_checksum, 0, 4);
        if (this.Block != null) {
            cmd_checksum[4] = this.Block[0];
        }
        for (int i2 = 0; i2 < this.Password.length; i2++) {
            cmd_checksum[i2 + 5] = this.Password[i2];
        }
        cmd_pwcheck[cmd_pwcheck.length - 1] = crc(cmd_checksum);
        String blockNum = StringUtil.toHexString(this.Block);
        if (blockNum.substring(0, 1).equals("0")) {
            blockNum = blockNum.substring(1, 2);
        }
        this.block_ = Integer.valueOf(blockNum).intValue() / 4;
        this.section_ = Integer.valueOf(blockNum).intValue() % 4;
        if (this.block_ < 10 || Integer.toHexString(this.block_).length() < 2) {
            xorString = "0010800B0" + Integer.toHexString(this.block_).toUpperCase() + StringUtil.toHexString(this.Password);
        } else {
            xorString = "0010800B" + Integer.toHexString(this.block_).toUpperCase() + StringUtil.toHexString(this.Password);
        }
        byte xorByte = getXor(hexStringToBytes(xorString));
        if (Integer.toHexString(Integer.valueOf(this.block_).intValue()).length() < 2) {
            psw_check_block_temp = hexStringToBytes("0" + Integer.toHexString(Integer.valueOf(this.block_).intValue()));
        } else {
            psw_check_block_temp = hexStringToBytes(Integer.toHexString(Integer.valueOf(this.block_).intValue()));
        }
        byte psw_check_block = psw_check_block_temp[0];
        byte[] cmd_pwcheck2 = new byte[17];
        cmd_pwcheck2[0] = -86;
        cmd_pwcheck2[1] = -86;
        cmd_pwcheck2[2] = -86;
        cmd_pwcheck2[3] = -106;
        cmd_pwcheck2[4] = 105;
        cmd_pwcheck2[6] = 16;
        cmd_pwcheck2[7] = Byte.MIN_VALUE;
        cmd_pwcheck2[8] = 11;
        cmd_pwcheck2[9] = psw_check_block;
        cmd_pwcheck2[10] = this.Password[0];
        cmd_pwcheck2[11] = this.Password[1];
        cmd_pwcheck2[12] = this.Password[2];
        cmd_pwcheck2[13] = this.Password[3];
        cmd_pwcheck2[14] = this.Password[4];
        cmd_pwcheck2[15] = this.Password[5];
        cmd_pwcheck2[16] = xorByte;
        byte[] bArr3 = new byte[3];
        bArr3[0] = 11;
        bArr3[2] = -112;
        return requestpw(cmd_pwcheck2, bArr3);
    }

    private Boolean requestpw(byte[] pcmd, byte[] psw) {
        if (this.tcard_reader == null || psw.length != 3) {
            return null;
        }
        UsbInterface pInterface = this.tcard_reader.getInterface(0);
        UsbEndpoint iEndpoint = pInterface.getEndpoint(0);
        UsbEndpoint oEndpoint = pInterface.getEndpoint(1);
        UsbDeviceConnection pconnection = this.tUsbManager.openDevice(this.tcard_reader);
        pconnection.claimInterface(pInterface, true);
        int bulkTransfer = pconnection.bulkTransfer(oEndpoint, pcmd, pcmd.length, 3000);
        try {
            Thread.sleep(500);
        } catch (InterruptedException a) {
            a.printStackTrace();
        }
        byte[] prec = new byte[IdCard.READER_VID_BIG];
        int bulkTransfer2 = pconnection.bulkTransfer(iEndpoint, prec, prec.length, 3000);
        for (int i = 0; i < 30; i++) {
            Log.e("pwcheck", new StringBuilder().append(prec[i]).toString());
        }
        for (int i2 = 0; i2 < TSAM_HEADER.length; i2++) {
            if (prec[i2] != TSAM_HEADER[i2]) {
                pconnection.close();
                return null;
            }
        }
        for (int i3 = 0; i3 < psw.length; i3++) {
            if (prec[TSAM_HEADER.length + i3 + 2] != psw[i3]) {
                pconnection.close();
                return false;
            }
        }
        pconnection.close();
        return true;
    }

    public TASectorInfo readData() throws TelpoException {
        String xorString;
        byte[] read_data_block_temp;
        byte[] cmd_read_data = new byte[13];
        byte[] bArr = new byte[9];
        bArr[0] = -86;
        bArr[1] = -86;
        bArr[2] = -86;
        bArr[3] = -106;
        bArr[4] = 105;
        bArr[6] = 6;
        bArr[7] = Byte.MIN_VALUE;
        bArr[8] = 13;
        System.arraycopy(bArr, 0, cmd_read_data, 0, 9);
        cmd_read_data[9] = this.Block[0];
        cmd_read_data[10] = 16;
        byte[] cmd_checksum = new byte[7];
        byte[] bArr2 = new byte[4];
        bArr2[1] = 6;
        bArr2[2] = Byte.MIN_VALUE;
        bArr2[3] = 13;
        System.arraycopy(bArr2, 0, cmd_checksum, 0, 4);
        cmd_checksum[4] = this.Block[0];
        cmd_checksum[5] = 16;
        cmd_read_data[cmd_read_data.length - 1] = crc(cmd_checksum);
        if (this.block_ < 10 || Integer.toHexString(this.block_).length() < 2) {
            xorString = "0006800D0" + Integer.toHexString(this.block_).toUpperCase() + "0" + this.section_ + "10";
        } else {
            xorString = "0006800D" + Integer.toHexString(this.block_).toUpperCase() + "0" + this.section_ + "10";
        }
        byte xorByte = getXor(hexStringToBytes(xorString));
        if (Integer.toHexString(Integer.valueOf(this.block_).intValue()).length() < 2) {
            read_data_block_temp = hexStringToBytes("0" + Integer.toHexString(Integer.valueOf(this.block_).intValue()));
        } else {
            read_data_block_temp = hexStringToBytes(Integer.toHexString(Integer.valueOf(this.block_).intValue()));
        }
        byte[] read_data_section_temp = hexStringToBytes("0" + Integer.toHexString(Integer.valueOf(this.section_).intValue()));
        byte psw_check_block = read_data_block_temp[0];
        byte psw_check_section = read_data_section_temp[0];
        byte[] cmd_read_data2 = new byte[13];
        cmd_read_data2[0] = -86;
        cmd_read_data2[1] = -86;
        cmd_read_data2[2] = -86;
        cmd_read_data2[3] = -106;
        cmd_read_data2[4] = 105;
        cmd_read_data2[6] = 6;
        cmd_read_data2[7] = Byte.MIN_VALUE;
        cmd_read_data2[8] = 13;
        cmd_read_data2[9] = psw_check_block;
        cmd_read_data2[10] = psw_check_section;
        cmd_read_data2[11] = 16;
        cmd_read_data2[12] = xorByte;
        byte[] bArr3 = new byte[3];
        bArr3[0] = 13;
        bArr3[2] = -112;
        byte[] data = requestData(cmd_read_data2, bArr3);
        if (data == null) {
            return null;
        }
        String newdata = decodetcarduid(data);
        TASectorInfo tsectorinfo = new TASectorInfo();
        tsectorinfo.setSectorData(newdata);
        return tsectorinfo;
    }

    public void sendAPDU(byte[] dcmd, byte[] dsw) {
        requestData(dcmd, dsw);
    }

    private byte[] requestData(byte[] dcmd, byte[] dsw) {
        if (this.tcard_reader == null || dsw.length != 3) {
            return null;
        }
        UsbInterface dInterface = this.tcard_reader.getInterface(0);
        UsbEndpoint iEndpoint = dInterface.getEndpoint(0);
        UsbEndpoint oEndpoint = dInterface.getEndpoint(1);
        UsbDeviceConnection dconnection = this.tUsbManager.openDevice(this.tcard_reader);
        dconnection.claimInterface(dInterface, true);
        int bulkTransfer = dconnection.bulkTransfer(oEndpoint, dcmd, dcmd.length, 3000);
        Log.d("idcard demo", "out:" + StringUtil.toHexString(dcmd));
        try {
            Thread.sleep(500);
        } catch (InterruptedException a) {
            a.printStackTrace();
        }
        byte[] drec = new byte[IdCard.READER_VID_BIG];
        int input = dconnection.bulkTransfer(iEndpoint, drec, drec.length, 3000);
        Log.d("idcard demo", "in size:" + input);
        Log.d("idcard demo", "in:" + StringUtil.toHexString(Arrays.copyOfRange(drec, 0, input - 1)));
        for (int i = 0; i < TSAM_HEADER.length; i++) {
            if (drec[i] != TSAM_HEADER[i]) {
                dconnection.close();
                return null;
            }
        }
        for (int i2 = 0; i2 < dsw.length; i2++) {
            if (drec[TSAM_HEADER.length + i2 + 2] != dsw[i2]) {
                dconnection.close();
                return null;
            }
        }
        dconnection.close();
        return Arrays.copyOfRange(drec, TSAM_HEADER.length + 5, TSAM_HEADER.length + 21);
    }

    public void transmitdata(byte[] data) {
        this.Newwritedata = data;
    }

    public Boolean writeInData(String newwritedata) {
        byte[] read_data_block_temp;
        byte[] LEN;
        byte[] LEN_DATA;
        String xorString;
        byte[] Newwritedata2 = hexStringToBytes(newwritedata);
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
        if (Newwritedata2 == null) {
            return null;
        }
        if (Newwritedata2 == null) {
            return null;
        }
        for (int i = 0; i < Newwritedata2.length; i++) {
            cmd_write_data[i + 12] = Newwritedata2[i];
        }
        if (Newwritedata2.length < 16) {
            int repairzero = 16 - Newwritedata2.length;
            for (int d = 0; d < repairzero; d++) {
                cmd_write_data[Newwritedata2.length + 12 + d] = 0;
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
        for (int i2 = 0; i2 < Newwritedata2.length; i2++) {
            cmd_checksum[i2 + 7] = Newwritedata2[i2];
        }
        cmd_write_data[cmd_write_data.length - 1] = crc(cmd_checksum);
        String write_data = StringUtil.toHexString(Newwritedata2);
        if (Integer.toHexString(Integer.valueOf(this.block_).intValue()).length() < 2) {
            read_data_block_temp = hexStringToBytes("0" + Integer.toHexString(Integer.valueOf(this.block_).intValue()));
        } else {
            read_data_block_temp = hexStringToBytes(Integer.toHexString(Integer.valueOf(this.block_).intValue()));
        }
        byte[] read_data_section_temp = hexStringToBytes("0" + Integer.toHexString(Integer.valueOf(this.section_).intValue()));
        byte psw_check_block = read_data_block_temp[0];
        byte psw_check_section = read_data_section_temp[0];
        int write_data_length = write_data.length();
        int len = (write_data_length / 2) + 6;
        int len_data = write_data_length / 2;
        if (Integer.toHexString(Integer.valueOf(len).intValue()).length() < 2) {
            LEN = hexStringToBytes("0" + Integer.toHexString(Integer.valueOf(len).intValue()));
        } else {
            LEN = hexStringToBytes(Integer.toHexString(Integer.valueOf(len).intValue()));
        }
        if (Integer.toHexString(Integer.valueOf(len_data).intValue()).length() < 2) {
            LEN_DATA = hexStringToBytes("0" + Integer.toHexString(Integer.valueOf(len_data).intValue()));
        } else {
            LEN_DATA = hexStringToBytes(Integer.toHexString(Integer.valueOf(len_data).intValue()));
        }
        if (this.block_ < 10 || Integer.toHexString(this.block_).length() < 2) {
            if (Integer.toHexString(Integer.valueOf(len).intValue()).length() < 2) {
                xorString = "00" + StringUtil.toHexString(LEN) + "800E" + "0" + Integer.toHexString(this.block_).toUpperCase() + "0" + this.section_ + "0" + Integer.toHexString(Integer.valueOf(len_data).intValue()) + write_data;
            } else {
                xorString = "00" + StringUtil.toHexString(LEN) + "800E" + "0" + Integer.toHexString(this.block_).toUpperCase() + "0" + this.section_ + Integer.toHexString(Integer.valueOf(len_data).intValue()) + write_data;
            }
        } else if (Integer.toHexString(Integer.valueOf(len).intValue()).length() < 2) {
            xorString = "00" + StringUtil.toHexString(LEN) + "800E" + Integer.toHexString(this.block_).toUpperCase() + "0" + this.section_ + "0" + Integer.toHexString(Integer.valueOf(len_data).intValue()) + write_data;
        } else {
            xorString = "00" + StringUtil.toHexString(LEN) + "800E" + Integer.toHexString(this.block_).toUpperCase() + "0" + this.section_ + Integer.toHexString(Integer.valueOf(len_data).intValue()) + write_data;
        }
        byte xorByte = getXor(hexStringToBytes(xorString));
        byte[] cmd_write_data2 = new byte[(len_data + 13)];
        cmd_write_data2[0] = -86;
        cmd_write_data2[1] = -86;
        cmd_write_data2[2] = -86;
        cmd_write_data2[3] = -106;
        cmd_write_data2[4] = 105;
        cmd_write_data2[5] = 0;
        cmd_write_data2[6] = LEN[0];
        cmd_write_data2[7] = Byte.MIN_VALUE;
        cmd_write_data2[8] = 14;
        cmd_write_data2[9] = psw_check_block;
        cmd_write_data2[10] = psw_check_section;
        cmd_write_data2[11] = LEN_DATA[0];
        cmd_write_data2[cmd_write_data2.length - 1] = xorByte;
        for (int i3 = 0; i3 < Newwritedata2.length; i3++) {
            cmd_write_data2[i3 + 12] = Newwritedata2[i3];
        }
        byte[] bArr3 = new byte[3];
        bArr3[0] = 14;
        bArr3[2] = -112;
        Boolean writefeeback = requestwrite(cmd_write_data2, bArr3);
        Boolean bool = writefeeback;
        return writefeeback;
    }

    private Boolean requestwrite(byte[] wcmd, byte[] wsw) {
        if (this.tcard_reader == null || wsw.length != 3) {
            return null;
        }
        UsbInterface wInterface = this.tcard_reader.getInterface(0);
        UsbEndpoint iEndpoint = wInterface.getEndpoint(0);
        UsbEndpoint oEndpoint = wInterface.getEndpoint(1);
        UsbDeviceConnection wconnection = this.tUsbManager.openDevice(this.tcard_reader);
        wconnection.claimInterface(wInterface, true);
        int bulkTransfer = wconnection.bulkTransfer(oEndpoint, wcmd, wcmd.length, 3000);
        try {
            Thread.sleep(500);
        } catch (InterruptedException a) {
            a.printStackTrace();
        }
        byte[] wrec = new byte[IdCard.READER_VID_BIG];
        int bulkTransfer2 = wconnection.bulkTransfer(iEndpoint, wrec, wrec.length, 3000);
        for (int i = 0; i < TSAM_HEADER.length; i++) {
            if (wrec[i] != TSAM_HEADER[i]) {
                wconnection.close();
                return null;
            }
        }
        for (int i2 = 0; i2 < wsw.length; i2++) {
            if (wrec[TSAM_HEADER.length + i2 + 2] != wsw[i2]) {
                wconnection.close();
                return false;
            }
        }
        wconnection.close();
        return true;
    }

    private byte crc(byte[] data) {
        byte crc = 0;
        for (byte b : data) {
            crc = (byte) (crc ^ b);
        }
        return crc;
    }

    public static byte[] hexStringToBytes(String hexString) {
        if (hexString == null || hexString.equals("")) {
            return null;
        }
        String hexString2 = hexString.toUpperCase().replace(" ", "");
        int length = hexString2.length() / 2;
        char[] hexChars = hexString2.toCharArray();
        byte[] d = new byte[length];
        for (int i = 0; i < length; i++) {
            int pos = i * 2;
            d[i] = (byte) ((charToByte(hexChars[pos]) << 4) | charToByte(hexChars[pos + 1]));
        }
        return d;
    }

    public static String bytesToHexString(byte[] src) {
        StringBuilder stringBuilder = new StringBuilder("");
        if (src == null || src.length <= 0) {
            return null;
        }
        for (byte b : src) {
            String hv = Integer.toHexString(b & 255);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv);
        }
        return stringBuilder.toString();
    }

    private static byte charToByte(char c) {
        return (byte) "0123456789ABCDEF".indexOf(c);
    }

    public static byte getXor(byte[] datas) {
        byte temp = datas[0];
        for (int i = 1; i < datas.length; i++) {
            temp = (byte) (datas[i] ^ temp);
        }
        return temp;
    }
}
