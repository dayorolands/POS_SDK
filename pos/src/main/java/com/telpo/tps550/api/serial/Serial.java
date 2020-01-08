package com.telpo.tps550.api.serial;

import com.telpo.tps550.api.DeviceAlreadyOpenException;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class Serial {
    public static final int MODE_ICC = 1;
    public static final int MODE_PINPAD = 2;
    public static final int MODE_PRINTER = 0;
    private FileDescriptor mFd;
    private FileInputStream mFileInputStream;
    private FileOutputStream mFileOutputStream;

    private static native FileDescriptor open(String str, int i, int i2);

    private static native int switch_mode(int i);

    public native void close();

    public Serial(String path, int baud, int flags) throws FileNotFoundException, SecurityException, IOException, DeviceAlreadyOpenException {
        if (!new File(path).exists()) {
            throw new FileNotFoundException();
        }
        this.mFd = open(path, baud, flags);
        if (this.mFd == null) {
            throw new IOException();
        }
        this.mFileInputStream = new FileInputStream(this.mFd);
        this.mFileOutputStream = new FileOutputStream(this.mFd);
    }

    public InputStream getInputStream() {
        return this.mFileInputStream;
    }

    public OutputStream getOutputStream() {
        return this.mFileOutputStream;
    }

    public int switchMode(int mode) {
        return switch_mode(mode);
    }

    static {
        System.loadLibrary("telpo_serial");
    }
}
