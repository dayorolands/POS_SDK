package com.telpo.tps550.api.reader;

import android.content.Context;
import android.util.Log;

import java.security.InvalidParameterException;
import java.util.Arrays;

import amlib.ccid.Reader4428;

public class SLE4428Reader extends CardReader {
    private static final String TAG = "SLE4428Reader";

    public SLE4428Reader() {
        this.cardType = 3;
    }

    public SLE4428Reader(Context context) {
        this.context = context;
        this.cardType = 3;
    }

    public byte[] readMainMemory(int addr, int num) throws InvalidParameterException {
        if (addr < 0 || addr > 1023 || num < 0 || num > 1024 || addr + num > 1024) {
            Log.e(TAG, "readMainMemory invalid parameter");
            return null;
        } else if (this.reader_type == 2 || this.reader_type == 1 || this.reader_type == 0) {
            return read_main_mem(this.cardType, addr, num);
        } else {
            Reader4428 reader4428 = (Reader4428) this.reader;
            byte[] readData = new byte[num];
            int[] returnLen = new int[1];
            int a = addr;
            byte[] tempData = new byte[123];
            int readLen = 0;
            int cnt = num / 123;
            int left = num % 123;
            int i = 0;
            while (i < cnt) {
                int result = reader4428.SLE4428Cmd_Read8Bits(a, 123, tempData, returnLen);
                if (result == 0 && returnLen[0] == 123) {
                    System.arraycopy(tempData, 0, readData, readLen, 123);
                    readLen += 123;
                    a += 123;
                    i++;
                } else {
                    Log.e(TAG, "4428 read 8 bits failed: " + result);
                    return null;
                }
            }
            if (left == 0) {
                return readData;
            }
            int result2 = reader4428.SLE4428Cmd_Read8Bits(a, left, tempData, returnLen);
            if (result2 == 0 && returnLen[0] == left) {
                System.arraycopy(tempData, 0, readData, readLen, left);
                int readLen2 = readLen + left;
                int a2 = a + left;
                return readData;
            }
            Log.e(TAG, "4428 read 8 bits failed: " + result2);
            return null;
        }
    }

    public boolean pscVerify(byte[] psc) throws InvalidParameterException, NullPointerException {
        if (psc == null) {
            throw new NullPointerException();
        } else if (psc.length != 2) {
            throw new InvalidParameterException();
        } else {
            if (this.reader_type != 2 && this.reader_type != 1 && this.reader_type != 0) {
                int result = ((Reader4428) this.reader).SLE4428Cmd_VerifyPSCAndEraseErrorCounter(psc[0], psc[1], new int[1]);
                if (result != 0) {
                    Log.e(TAG, "4428 verify psc and erase error counter failed: " + result);
                    return false;
                }
            } else if (psc_verify(this.cardType, psc) < 0) {
                return false;
            }
            this.correct_psc_verification = true;
            return true;
        }
    }

    public boolean updateMainMemory(int addr, byte[] data) throws InvalidParameterException, NullPointerException {
        if (data == null) {
            throw new NullPointerException();
        } else if (addr < 0 || addr > 1020 || data.length < 0 || data.length > 1021 || data.length + addr > 1021) {
            throw new InvalidParameterException();
        } else if (!this.correct_psc_verification) {
            return false;
        } else {
            if (this.reader_type != 2 && this.reader_type != 1 && this.reader_type != 0) {
                Reader4428 reader4428 = (Reader4428) this.reader;
                byte[] readData = new byte[data.length];
                byte[] readPB = new byte[data.length];
                int[] returnLen = new int[1];
                if (reader4428.SLE4428Cmd_Read9Bits(addr, data.length, readData, readPB, returnLen) == 0) {
                    for (int i = 0; i < returnLen[0]; i++) {
                        if (readPB[i] == 0) {
                            Log.e(TAG, "The 4428 protected data byte can not be changed");
                            return false;
                        }
                    }
                }
                int i2 = 0;
                int a = addr;
                while (i2 < data.length) {
                    int result = reader4428.SLE4428Cmd_WriteEraseWithoutPB(a, data[i2]);
                    i2++;
                    a++;
                }
            } else if (update_main_mem(this.cardType, addr, data) != data.length) {
                return false;
            }
            byte[] data_r = readMainMemory(addr, data.length);
            if (data_r != null && Arrays.equals(data, data_r)) {
                return true;
            }
            Log.e(TAG, "The read data is not consistent with the writen data");
            return false;
        }
    }

    public boolean pscModify(byte[] pscNew) throws InvalidParameterException, NullPointerException {
        if (pscNew == null) {
            throw new NullPointerException();
        } else if (pscNew.length != 2) {
            throw new InvalidParameterException();
        } else if (!this.correct_psc_verification) {
            return false;
        } else {
            if (this.reader_type != 2 && this.reader_type != 1 && this.reader_type != 0) {
                Reader4428 reader4428 = (Reader4428) this.reader;
                int i = 0;
                int addr = 1022;
                while (i < 2) {
                    int result = reader4428.SLE4428Cmd_WriteEraseWithoutPB(addr, pscNew[i]);
                    i++;
                    addr++;
                }
                byte[] data_r = readMainMemory(1022, 2);
                if (data_r == null || !Arrays.equals(pscNew, data_r)) {
                    Log.e(TAG, "The read data is not consistent with the writen data");
                    return false;
                }
            } else if (psc_modify(this.cardType, pscNew) != 0) {
                return false;
            }
            return true;
        }
    }

    public byte[] getUserCode() {
        byte[] userCode = readMainMemory(21, 6);
        if (userCode == null || userCode.length != 6) {
            return null;
        }
        return userCode;
    }
}
