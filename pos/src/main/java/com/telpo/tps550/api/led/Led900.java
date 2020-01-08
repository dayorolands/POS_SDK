package com.telpo.tps550.api.led;

import android.content.Context;
import com.telpo.tps550.api.InternalErrorException;
import com.telpo.tps550.api.TelpoException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class Led900 {
    private Context mContext = null;

    public Led900(Context context) {
        this.mContext = context;
    }

    public synchronized void on(int num) throws TelpoException {
        try {
            Class<?> led = Class.forName("com.common.sdk.led.LEDServiceManager");
            Object obj = this.mContext.getSystemService("LED");
            Method method = led.getMethod("on", new Class[]{Integer.TYPE});
            method.invoke(obj, new Object[]{Integer.valueOf(num)});
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            throw new InternalErrorException();
        } catch (IllegalAccessException e2) {
            e2.printStackTrace();
            throw new InternalErrorException();
        } catch (InvocationTargetException e3) {
            throw new TelpoException();
        } catch (NoSuchMethodException e4) {
            e4.printStackTrace();
            throw new InternalErrorException();
        } catch (ClassNotFoundException e5) {
            e5.printStackTrace();
            throw new InternalErrorException();
        }
    }

    public synchronized void off(int num) throws TelpoException {
        try {
            Class<?> led = Class.forName("com.common.sdk.led.LEDServiceManager");
            Object obj = this.mContext.getSystemService("LED");
            Method method = led.getMethod("off", new Class[]{Integer.TYPE});
            method.invoke(obj, new Object[]{Integer.valueOf(num)});
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            throw new InternalErrorException();
        } catch (IllegalAccessException e2) {
            e2.printStackTrace();
            throw new InternalErrorException();
        } catch (InvocationTargetException e3) {
            throw new TelpoException();
        } catch (NoSuchMethodException e4) {
            e4.printStackTrace();
            throw new InternalErrorException();
        } catch (ClassNotFoundException e5) {
            e5.printStackTrace();
            throw new InternalErrorException();
        }
    }

    public synchronized void blink(int num, int period) throws TelpoException {
        try {
            Class<?> led = Class.forName("com.common.sdk.led.LEDServiceManager");
            Object obj = this.mContext.getSystemService("LED");
            Method method = led.getMethod("blink", new Class[]{Integer.TYPE, Integer.TYPE});
            method.invoke(obj, new Object[]{Integer.valueOf(num), Integer.valueOf(period)});
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            throw new InternalErrorException();
        } catch (IllegalAccessException e2) {
            e2.printStackTrace();
            throw new InternalErrorException();
        } catch (InvocationTargetException e3) {
            throw new TelpoException();
        } catch (NoSuchMethodException e4) {
            e4.printStackTrace();
            throw new InternalErrorException();
        } catch (ClassNotFoundException e5) {
            e5.printStackTrace();
            throw new InternalErrorException();
        }
    }
}
