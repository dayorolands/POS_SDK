package com.telpo.tps550.api.hdmi;

import android.content.Context;
import java.lang.reflect.InvocationTargetException;

public class HdmiCtrl {
    public static int switchDisplay(Context context) {
        int ret = 0;
        try {
            Class<?> c = Class.forName("android.util.Vendor");
            c.getMethod("SwitchDisplay", new Class[]{Context.class}).invoke(c, new Object[]{context});
        } catch (Exception e) {
            ret = -1;
            if (e instanceof InvocationTargetException) {
                ((InvocationTargetException) e).getTargetException().printStackTrace();
            } else {
                e.printStackTrace();
            }
        }
        return ret;
    }
}
