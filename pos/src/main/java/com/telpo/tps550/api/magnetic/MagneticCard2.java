package com.telpo.tps550.api.magnetic;

import android.content.Context;

import com.telpo.tps550.api.InternalErrorException;
import com.telpo.tps550.api.TelpoException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class MagneticCard2 {
    private static Context mContext = null;

    public static synchronized void open(Context context) throws TelpoException {
        synchronized (MagneticCard2.class) {
            mContext = context;
            try {
                Class<?> msr = Class.forName("com.common.sdk.magneticcard.MagneticCardServiceManager");
                msr.getMethod("open", new Class[0]).invoke(mContext.getSystemService("MagneticCard"), new Object[0]);
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
                throw new InternalErrorException();
            } catch (IllegalAccessException e2) {
                e2.printStackTrace();
                throw new InternalErrorException();
            } catch (InvocationTargetException e3) {
                throwException(e3);
            } catch (NoSuchMethodException e4) {
                e4.printStackTrace();
                throw new InternalErrorException();
            } catch (ClassNotFoundException e5) {
                e5.printStackTrace();
                throw new InternalErrorException();
            }
        }
    }

    public static synchronized void close() throws TelpoException {
        synchronized (MagneticCard2.class) {
            try {
                Class<?> msr = Class.forName("com.common.sdk.magneticcard.MagneticCardServiceManager");
                msr.getMethod("close", new Class[0]).invoke(mContext.getSystemService("MagneticCard"), new Object[0]);
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
                throw new InternalErrorException();
            } catch (IllegalAccessException e2) {
                e2.printStackTrace();
                throw new InternalErrorException();
            } catch (InvocationTargetException e3) {
                throwException(e3);
            } catch (NoSuchMethodException e4) {
                e4.printStackTrace();
                throw new InternalErrorException();
            } catch (ClassNotFoundException e5) {
                e5.printStackTrace();
                throw new InternalErrorException();
            }
            mContext = null;
        }
    }

    public static synchronized int check(int timeout) throws TelpoException {
        int result;
        synchronized (MagneticCard2.class) {
            result = 0;
            try {
                Class<?> msr = Class.forName("com.common.sdk.magneticcard.MagneticCardServiceManager");
                Object obj = mContext.getSystemService("MagneticCard");
                Method method = msr.getMethod("check", new Class[]{Integer.TYPE});
                result = ((Integer) method.invoke(obj, new Object[]{Integer.valueOf(timeout)})).intValue();
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
                throw new InternalErrorException();
            } catch (IllegalAccessException e2) {
                e2.printStackTrace();
                throw new InternalErrorException();
            } catch (InvocationTargetException e3) {
                throwException(e3);
            } catch (NoSuchMethodException e4) {
                e4.printStackTrace();
                throw new InternalErrorException();
            } catch (ClassNotFoundException e5) {
                e5.printStackTrace();
                throw new InternalErrorException();
            }
        }
        return result;
    }

    public static synchronized String[] read() throws TelpoException {
        String[] result = {};
        synchronized (MagneticCard2.class) {
            String[] strArr = new String[3];
            try {
                Class<?> msr = Class.forName("com.common.sdk.magneticcard.MagneticCardServiceManager");
                result = (String[]) msr.getMethod("read", new Class[0]).invoke(mContext.getSystemService("MagneticCard"), new Object[0]);
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
                throw new InternalErrorException();
            } catch (IllegalAccessException e2) {
                e2.printStackTrace();
                throw new InternalErrorException();
            } catch (InvocationTargetException e3) {
                throwException(e3);
            } catch (NoSuchMethodException e4) {
                e4.printStackTrace();
                throw new InternalErrorException();
            } catch (ClassNotFoundException e5) {
                e5.printStackTrace();
                throw new InternalErrorException();
            }
        }
        return result;
    }

    private static synchronized void throwException(InvocationTargetException e) throws TelpoException {
        synchronized (MagneticCard2.class) {
            throw new TelpoException();
        }
    }
}
