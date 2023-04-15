package com.dspread.qpos.injectKey;

/**
 * Created by dsppc11 on 2019/9/11.
 */

public class HashInject extends Poskeys {

    public String getHASHKEY() {
        return HASHKEY;
    }

    public void setHASHKEY(String HASHKEY) {
        this.HASHKEY = HASHKEY;
    }

    private String HASHKEY = "0123456789ABCDEFFEDCBA9876543210";

}
