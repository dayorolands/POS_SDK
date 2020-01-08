package com.telpo.tps550.api.typea;

import android.util.Log;
import com.telpo.tps550.api.TelpoException;
import com.telpo.tps550.api.util.StringUtil;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;

public class SerialTypeACard {
    private static final byte[] TSAM_HEADER = {-86, -86, -86, -106, 105};
    private byte[] Block;
    private byte[] Newwritedata;
    private byte[] Password;
    private int block_;
    byte[] currect;
    private OutputStream mOutputStream;
    private int section_;
    private byte[] tbuffer = new byte[20];
    byte[] test;
    private Boolean writeright = false;

    public SerialTypeACard(OutputStream mOutputStream2) throws TelpoException {
        byte[] bArr = new byte[28];
        bArr[0] = -86;
        bArr[1] = -86;
        bArr[2] = -86;
        bArr[3] = -106;
        bArr[4] = 105;
        bArr[6] = 10;
        bArr[7] = 5;
        bArr[9] = -112;
        bArr[10] = -80;
        bArr[11] = 13;
        bArr[12] = 56;
        bArr[13] = 30;
        bArr[16] = 4;
        bArr[17] = -86;
        bArr[18] = -86;
        bArr[19] = -86;
        bArr[20] = -106;
        bArr[21] = 105;
        bArr[23] = 3;
        bArr[24] = 11;
        bArr[26] = -112;
        bArr[27] = -104;
        this.test = bArr;
        byte[] bArr2 = new byte[11];
        bArr2[0] = -86;
        bArr2[1] = -86;
        bArr2[2] = -86;
        bArr2[3] = -106;
        bArr2[4] = 105;
        bArr2[6] = 3;
        bArr2[7] = 11;
        bArr2[9] = -112;
        bArr2[10] = -104;
        this.currect = bArr2;
        this.mOutputStream = mOutputStream2;
    }

    public void transmitBlock(byte[] Block2) throws TelpoException {
        this.Block = Block2;
    }

    public void transmittbuffer(byte[] tbuffer2) throws TelpoException {
        this.tbuffer = tbuffer2;
    }

    public void transmitpassword(byte[] Password2) throws TelpoException {
        this.Password = Password2;
    }

    public void transmitNewwritedata(byte[] Newwritedata2) throws TelpoException {
        this.Newwritedata = Newwritedata2;
    }

    public void checkTACard() throws TelpoException {
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
        for (int i = 0; i < cmd_read_uid.length; i++) {
            try {
                this.mOutputStream.write(cmd_read_uid[i]);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public TAInfo requestTACard() throws TelpoException {
        byte[] sw = new byte[3];
        sw[0] = 5;
        sw[2] = -112;
        for (int s = 0; s < TSAM_HEADER.length; s++) {
            if (this.tbuffer[s] != TSAM_HEADER[s]) {
                return null;
            }
        }
        for (int k = 0; k < sw.length; k++) {
            if (this.tbuffer[TSAM_HEADER.length + k + 2] != sw[k]) {
                return null;
            }
        }
        if (1 == 0) {
            return null;
        }
        byte[] uid2 = Arrays.copyOfRange(this.tbuffer, TSAM_HEADER.length + 5, TSAM_HEADER.length + 9);
        new String();
        String newuid2 = decodetcarduid(uid2);
        Log.e("newuid2", newuid2);
        TAInfo tinfo = new TAInfo();
        tinfo.setNum(newuid2);
        TAInfo tAInfo = tinfo;
        return tinfo;
    }

    public TAInfo checkTACard(int timeout) {
        TAInfo taInfo = null;
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
        while (taInfo == null && endTime - startTime < ((long) timeout)) {
            for (int i = 0; i < cmd_read_uid.length; i++) {
                try {
                    this.mOutputStream.write(cmd_read_uid[i]);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            try {
                taInfo = requestTACard();
            } catch (TelpoException e2) {
                e2.printStackTrace();
            }
            endTime = System.currentTimeMillis();
        }
        return taInfo;
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

    public void checkPW() throws TelpoException {
        String xorString;
        byte[] psw_check_block_temp;
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
        for (int i = 0; i < this.Password.length; i++) {
            cmd_pwcheck[i + 10] = this.Password[i];
        }
        cmd_pwcheck[cmd_pwcheck.length - 1] = -102;
        Log.d("idcard demo", "Block:" + StringUtil.toHexString(this.Block));
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
        Log.d("idcard demo", "psw_check_block:" + StringUtil.toHexString(new byte[]{psw_check_block}));
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
        Log.d("idcard demo", "cmd_pwcheck:" + StringUtil.toHexString(cmd_pwcheck2));
        for (int i2 = 0; i2 < cmd_pwcheck2.length; i2++) {
            try {
                this.mOutputStream.write(cmd_pwcheck2[i2]);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        Log.e("checkPW", "success");
    }

    private boolean isSame() {
        Log.e("idcard demo", "tbuffer is" + StringUtil.toHexString(this.tbuffer));
        for (int i = 17; i < 28; i++) {
            if (this.tbuffer[i] != this.test[i]) {
                Log.d("idcard demo", "isSame false");
                return false;
            }
        }
        return true;
    }

    public Boolean requestpw() {
        Log.e("idcard demo", "before isSame");
        if (isSame()) {
            Log.d("idcard demo", "isSame true");
            this.tbuffer = new byte[512];
            for (int i = 0; i < this.currect.length; i++) {
                this.tbuffer[i] = this.currect[i];
            }
        }
        byte[] sw = new byte[3];
        sw[0] = 11;
        sw[2] = -112;
        for (int s = 0; s < TSAM_HEADER.length; s++) {
            if (this.tbuffer[s] != TSAM_HEADER[s]) {
                Log.d("idcard demo", "false1");
                return false;
            }
        }
        for (int k = 0; k < sw.length; k++) {
            if (this.tbuffer[TSAM_HEADER.length + k + 2] != sw[k]) {
                Log.d("idcard demo", "false2");
                return false;
            }
        }
        return true;
    }

    public void readData() throws TelpoException {
        String xorString;
        byte[] read_data_block_temp;
        byte[] bArr = new byte[13];
        bArr[0] = -86;
        bArr[1] = -86;
        bArr[2] = -86;
        bArr[3] = -106;
        bArr[4] = 105;
        bArr[6] = 6;
        bArr[7] = Byte.MIN_VALUE;
        bArr[8] = 13;
        bArr[9] = 1;
        bArr[10] = 1;
        bArr[11] = 16;
        bArr[12] = -101;
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
        byte[] cmd_read_data = new byte[13];
        cmd_read_data[0] = -86;
        cmd_read_data[1] = -86;
        cmd_read_data[2] = -86;
        cmd_read_data[3] = -106;
        cmd_read_data[4] = 105;
        cmd_read_data[6] = 6;
        cmd_read_data[7] = Byte.MIN_VALUE;
        cmd_read_data[8] = 13;
        cmd_read_data[9] = psw_check_block;
        cmd_read_data[10] = psw_check_section;
        cmd_read_data[11] = 16;
        cmd_read_data[12] = xorByte;
        for (int i = 0; i < cmd_read_data.length; i++) {
            try {
                this.mOutputStream.write(cmd_read_data[i]);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public TASectorInfo requestData() {
        byte[] sw = new byte[3];
        sw[0] = 13;
        sw[2] = -112;
        Boolean requestsuccess = true;
        for (int s = 0; s < TSAM_HEADER.length; s++) {
            if (this.tbuffer[s] != TSAM_HEADER[s]) {
                return null;
            }
        }
        for (int k = 0; k < sw.length; k++) {
            if (this.tbuffer[TSAM_HEADER.length + k + 2] != sw[k]) {
                return null;
            }
        }
        if (!requestsuccess.booleanValue()) {
            return null;
        }
        byte[] bArr = new byte[0];
        byte[] data = Arrays.copyOfRange(this.tbuffer, TSAM_HEADER.length + 5, TSAM_HEADER.length + 21);
        new String();
        String newdata = decodetcarduid(data);
        Log.e("requestData", newdata);
        TASectorInfo tsectorinfo = new TASectorInfo();
        tsectorinfo.setSectorData(newdata);
        return tsectorinfo;
    }

    public Boolean writeInData() throws TelpoException {
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
        if (this.Newwritedata == null) {
            this.writeright = false;
            return this.writeright;
        } else if (this.Newwritedata == null) {
            return null;
        } else {
            for (int i = 0; i < this.Newwritedata.length; i++) {
                cmd_write_data[i + 12] = this.Newwritedata[i];
            }
            if (this.Newwritedata.length < 16) {
                int repairzero = 16 - this.Newwritedata.length;
                for (int d = 0; d < repairzero; d++) {
                    cmd_write_data[this.Newwritedata.length + 12 + d] = 0;
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
            for (int i2 = 0; i2 < this.Newwritedata.length; i2++) {
                cmd_checksum[i2 + 7] = this.Newwritedata[i2];
            }
            cmd_write_data[cmd_write_data.length - 1] = crc(cmd_checksum);
            for (int l = 0; l < cmd_write_data.length; l++) {
                try {
                    this.mOutputStream.write(cmd_write_data[l]);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            this.writeright = true;
            return true;
        }
    }

    public Boolean requestwrite() {
        byte[] sw = new byte[3];
        sw[0] = 14;
        sw[2] = -112;
        for (int s = 0; s < TSAM_HEADER.length; s++) {
            if (this.tbuffer[s] != TSAM_HEADER[s]) {
                return false;
            }
        }
        for (int k = 0; k < sw.length; k++) {
            if (this.tbuffer[TSAM_HEADER.length + k + 2] != sw[k]) {
                return false;
            }
        }
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
