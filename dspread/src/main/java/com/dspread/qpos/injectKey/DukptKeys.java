package com.dspread.qpos.injectKey;//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//


public class DukptKeys extends Poskeys{
//    After the digital envelope is successfully updated, BDK is
//0123456789ABCDEFFEDCBA9876543210

    public   String trackipek = "FFC027508ECFDF198C39DFC73F9A5754";
    public   String emvipek = "FFC027508ECFDF198C39DFC73F9A5754";
    public   String pinipek = "FFC027508ECFDF198C39DFC73F9A5754";
    public   String trackksn = "00000510F3C360600000";
    public   String emvksn = "00000510F3C360600000";
    public   String pinksn = "00000510F3C360600000";
    public   String tmk = "5F8B2B8818966C5CD4CC393AF9FC7722";


    public DukptKeys() {
    }

    public DukptKeys(String trackipek, String emvipek, String pinipek, String trackksn, String emvksn, String pinksn, String tmk, String filePath) {
        trackipek = trackipek;
        emvipek = emvipek;
        pinipek = pinipek;
        trackksn = trackksn;
        emvksn = emvksn;
        pinksn = pinksn;
        tmk = tmk;
        filePath = filePath;
    }

    public   String getTrackipek() {
        return trackipek;
    }

    public   void setTrackipek(String trackipek) {
        trackipek = trackipek;
    }

    public   String getEmvipek() {
        return emvipek;
    }

    public   void setEmvipek(String emvipek) {
        emvipek = emvipek;
    }

    public   String getPinipek() {
        return pinipek;
    }

    public   void setPinipek(String pinipek) {
        pinipek = pinipek;
    }

    public   String getTrackksn() {
        return trackksn;
    }

    public   void setTrackksn(String trackksn) {
        trackksn = trackksn;
    }

    public   String getEmvksn() {
        return emvksn;
    }

    public   void setEmvksn(String emvksn) {
        emvksn = emvksn;
    }

    public   String getPinksn() {
        return pinksn;
    }

    public   void setPinksn(String pinksn) {
        pinksn = pinksn;
    }

    public   String getTmk() {
        return tmk;
    }

    public   void setTmk(String tmk) {
        tmk = tmk;
    }


}
