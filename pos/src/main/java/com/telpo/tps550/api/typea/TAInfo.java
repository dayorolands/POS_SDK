package com.telpo.tps550.api.typea;

import android.content.Context;
import java.io.Serializable;

public class TAInfo implements Serializable {
    private static Context mContext = null;
    private String num;

    public static synchronized void open(Context context) {
        synchronized (TAInfo.class) {
            mContext = context;
        }
    }

    public String getNum() {
        return this.num;
    }

    public void setNum(String num2) {
        this.num = num2;
    }
}
