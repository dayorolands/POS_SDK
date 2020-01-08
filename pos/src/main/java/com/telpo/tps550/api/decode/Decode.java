package com.telpo.tps550.api.decode;

public class Decode {
    private static native void decode_close();

    private static native int decode_open();

    private static native int decode_read(int i, byte[] bArr);

    public static synchronized void open() throws Exception {
        synchronized (Decode.class) {
            int ret = decode_open();
            if (ret != 0) {
                if (ret == -1) {
                    throw new Exception("unknown device");
                } else if (ret == -2) {
                    throw new Exception("invalid baudrate");
                } else if (ret == -3) {
                    throw new Exception("cannot open port");
                } else if (ret == -4) {
                    throw new Exception("tcgetattr() failed");
                } else if (ret == -5) {
                    throw new Exception("tcsetattr() failed");
                } else if (ret == -6) {
                    throw new Exception("externcard already opened");
                } else if (ret == -7) {
                    throw new Exception("cannot open externcard");
                } else if (ret == -8) {
                    throw new Exception("switch to qrcode error");
                }
            }
        }
    }

    public static synchronized void close() {
        synchronized (Decode.class) {
            decode_close();
        }
    }

    public static synchronized byte[] readWithFormat(int timeout) throws Exception {
        byte[] array1;
        synchronized (Decode.class) {
            byte[] array = new byte[2048];
            array1 = new byte[2048];
            int ret = decode_read(timeout, array);
            if (ret < 0) {
                if (ret == -2) {
                    throw new Exception("Read Timeout");
                }
                throw new Exception("Read Error");
            } else if (array[0] != 3) {
                throw new Exception("Invalid Format");
            } else {
                System.arraycopy(array, 5, array1, 0, (array[6] * 256) + array[7] + 3);
            }
        }
        return array1;
    }

    public static synchronized String read(int timeout) throws Exception {
        String data;
        synchronized (Decode.class) {
            byte[] array = new byte[2048];
            int ret = decode_read(timeout, array);
            if (ret >= 0) {
                data = new String(array, 0, ret, "UTF-8");
            } else if (ret == -2) {
                throw new Exception("Read Timeout");
            } else {
                throw new Exception("Read Error");
            }
        }
        return data;
    }

    static {
        System.loadLibrary("decode");
    }
}
