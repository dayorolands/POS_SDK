package com.telpo.tps550.api.reader;

import android.content.Context;
import android.util.Log;

import java.security.InvalidParameterException;
import java.util.Arrays;

import amlib.ccid.Reader4442;

public class SLE4442Reader extends CardReader {
    private static final String TAG = "SLE4442Reader";

    public SLE4442Reader() {
        this.cardType = 2;
    }

    public SLE4442Reader(Context context) {
        this.context = context;
        this.cardType = 2;
    }

    public byte[] readMainMemory(int addr, int num) {
        if (addr < 0 || addr > 255 || num < 0 || num > 256 || addr + num > 256) {
            Log.e(TAG, "readMainMemory invalid parameter");
            return null;
        } else if (this.reader_type == 2 || this.reader_type == 1 || this.reader_type == 0) {
            return read_main_mem(this.cardType, addr, num);
        } else {
            Reader4442 reader4442 = (Reader4442) this.reader;
            byte[] readData = new byte[num];
            int[] returnLen = new int[1];
            if (num == 256) {
                int result = reader4442.SLE4442Cmd_ReadMainMemory((byte) addr, (byte) -1, readData, returnLen);
                if (result != 0) {
                    Log.e(TAG, "read main memory failed: " + result);
                } else if (returnLen[0] != 255) {
                    return Arrays.copyOf(readData, returnLen[0]);
                } else {
                    byte[] temp = new byte[1];
                    if (reader4442.SLE4442Cmd_ReadMainMemory((byte) -1, (byte) 1, temp, returnLen) != 0) {
                        return readData;
                    }
                    readData[255] = temp[0];
                    return readData;
                }
            } else {
                int result2 = reader4442.SLE4442Cmd_ReadMainMemory((byte) addr, (byte) num, readData, returnLen);
                if (result2 == 0) {
                    return Arrays.copyOf(readData, returnLen[0]);
                }
                Log.e(TAG, "read main memory failed: " + result2);
            }
            return null;
        }
    }

    public boolean updateMainMemory(int addr, byte[] data) throws InvalidParameterException, NullPointerException {
        byte[] pm;
        int cnt;
        if (data == null) {
            throw new NullPointerException();
        } else if (addr < 0 || addr > 255 || data.length < 0 || data.length > 256 || data.length + addr > 256) {
            throw new InvalidParameterException();
        } else if (!this.correct_psc_verification) {
            return false;
        } else {
            if (this.reader_type != 2 && this.reader_type != 1 && this.reader_type != 0) {
                Reader4442 reader4442 = (Reader4442) this.reader;
                if (addr < 32 && (pm = readProtectionMemory()) != null && pm.length == 4) {
                    int pmInt = (pm[0] & 255) | ((pm[1] << 8) & 65280) | ((pm[2] << 16) & 16711680) | ((pm[3] << 24) & -16777216);
                    if (data.length + addr > 32) {
                        cnt = 32 - addr;
                    } else {
                        cnt = data.length;
                    }
                    int a = addr;
                    while (true) {
                        int a2 = a;
                        a = a2 + 1;
                        if (((pmInt >> a2) & 1) == 0) {
                            Log.e(TAG, "The 4442 protected data byte can not be changed");
                            return false;
                        }
                        cnt--;
                        if (cnt <= 0) {
                            break;
                        }
                    }
                }
                int i = 0;
                int a3 = addr;
                while (i < data.length) {
                    int result = reader4442.SLE4442Cmd_UpdateMainMemory((byte) a3, data[i]);
                    i++;
                    a3++;
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

    public boolean pscVerify(byte[] psc) throws InvalidParameterException, NullPointerException {
        if (psc == null) {
            throw new NullPointerException();
        } else if (psc.length != 3) {
            throw new InvalidParameterException();
        } else {
            if (this.reader_type != 2 && this.reader_type != 1 && this.reader_type != 0) {
                Reader4442 reader4442 = (Reader4442) this.reader;
                byte[] readData = new byte[1];
                int[] returnLen = new int[1];
                int result = reader4442.SLE4442Cmd_ReadSecurityMemory((byte) 1, readData, returnLen);
                if (result != 0) {
                    Log.e(TAG, "read 4442 error counter failed: " + result);
                    return false;
                }
                Log.i(TAG, "4442 error counter: " + readData[0]);
                if ((readData[0] & 7) == 0) {
                    Log.e(TAG, "4442 error counter no free bits");
                    return false;
                }
                byte error_counter = readData[0];
                int i = 0;
                while (true) {
                    if (i >= 3) {
                        break;
                    } else if (((error_counter >> i) & 1) == 1) {
                        int result2 = reader4442.SLE4442Cmd_UpdateSecurityMemory((byte) 0, (byte) (((1 << i) ^ -1) & error_counter));
                        if (result2 != 0) {
                            Log.e(TAG, "4442 error counter write free bit failed: " + result2);
                            return false;
                        }
                    } else {
                        i++;
                    }
                }
                reader4442.SLE4442Cmd_CompareVerificationData((byte) 1, psc[0]);
                reader4442.SLE4442Cmd_CompareVerificationData((byte) 2, psc[1]);
                reader4442.SLE4442Cmd_CompareVerificationData((byte) 3, psc[2]);
                reader4442.SLE4442Cmd_UpdateSecurityMemory((byte) 0, (byte) -1);
                readData[0] = 0;
                reader4442.SLE4442Cmd_ReadSecurityMemory((byte) 1, readData, returnLen);
                if ((readData[0] & 7) != 7) {
                    Log.e(TAG, "4442 psc verification failed");
                    return false;
                }
            } else if (psc_verify(this.cardType, psc) < 0) {
                return false;
            }
            this.correct_psc_verification = true;
            return true;
        }
    }

    public boolean pscModify(byte[] pscNew) throws InvalidParameterException, NullPointerException {
        if (pscNew == null) {
            throw new NullPointerException();
        } else if (pscNew.length != 3) {
            throw new InvalidParameterException();
        } else if (!this.correct_psc_verification) {
            return false;
        } else {
            if (this.reader_type != 2 && this.reader_type != 1 && this.reader_type != 0) {
                Reader4442 reader4442 = (Reader4442) this.reader;
                for (int i = 0; i < 3; i++) {
                    int result = reader4442.SLE4442Cmd_UpdateSecurityMemory((byte) (i + 1), pscNew[i]);
                    if (result != 0) {
                        Log.e(TAG, "4442 update psc failed: " + result);
                        return false;
                    }
                }
            } else if (psc_modify(this.cardType, pscNew) != 0) {
                return false;
            }
            return true;
        }
    }

    public byte[] readProtectionMemory() {
        if (this.reader_type == 2 || this.reader_type == 1 || this.reader_type == 0) {
            return null;
        }
        byte[] readData = new byte[4];
        int[] returnLen = new int[1];
        int result = ((Reader4442) this.reader).SLE4442Cmd_ReadProtectionMemory((byte) 4, readData, returnLen);
        if (result == 0) {
            return Arrays.copyOf(readData, returnLen[0]);
        }
        Log.e(TAG, "4442 read protection memory failed: " + result);
        return null;
    }

    public byte[] getUserCode() {
        byte[] userCode = readMainMemory(21, 6);
        if (userCode == null || userCode.length != 6) {
            return null;
        }
        return userCode;
    }
}
