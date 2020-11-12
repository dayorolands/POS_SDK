package com.dspread.qpos.task;

import com.dspread.qpos.TRACE;
import com.dspread.xpos.QPOSService;

/**
 * Created by dsppc11 on 2019/5/16.
 */

public  class CardExistQueryThread extends Thread {

    private boolean concelFlag = false;
    private QPOSService pos;

    public void run() {

        while (!concelFlag) {
            TRACE.d("CardExistQueryThread" + isInterrupted());
            int i = 0;
            while (!concelFlag && i < 100) {
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                i++;
            }
            if (concelFlag)
                break;
            if(pos == null)
                return;
            pos.isCardExist(30);
        }
    }

    public void concelSelf() {
        concelFlag = true;
    }

    public void initPos(QPOSService pos) {
        this.pos = pos;
    }


}
