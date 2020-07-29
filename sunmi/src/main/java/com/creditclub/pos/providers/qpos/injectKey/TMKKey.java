package com.appzonegroup.creditclub.pos.provider.qpos.injectKey;

/**
 * Created by dsppc11 on 2019/2/25.
 */

public class TMKKey extends Poskeys{
    public String getTMKKEY() {
        return TMKKEY;
    }

    public void setTMKKEY(String TMKKEY) {
        this.TMKKEY = TMKKEY;
    }

    private String TMKKEY = "0123456789ABCDEFFEDCBA9876543210";
}
