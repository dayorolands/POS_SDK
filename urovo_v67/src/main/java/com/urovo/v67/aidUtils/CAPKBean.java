package com.urovo.v67.aidUtils;

import android.content.ContentValues;
import android.database.Cursor;

public class CAPKBean {
    public String IndexID;
    public String RID = "";
    public String CA_PKIndex = "";
    public String CA_HashAlgoIndicator = "";
    public String CA_PKAlgoIndicator = "";
    public String LengthOfCAPKModulus = "";
    public String CAPKModulus = "";
    public String LengthOfCAPKExponent = "";
    public String CAPKExponent = "";
    public String ChecksumHash = "";
    public String CAPKExpDate = "";

    public String getIndexID() {
        return IndexID;
    }

    public void setIndexID(String indexID) {
        IndexID = indexID;
    }

    public String getRID() {
        return RID;
    }

    public void setRID(String RID) {
        this.RID = RID;
    }

    public String getCA_PKIndex() {
        return CA_PKIndex;
    }

    public void setCA_PKIndex(String CA_PKIndex) {
        this.CA_PKIndex = CA_PKIndex;
    }

    public String getCA_HashAlgoIndicator() {
        return CA_HashAlgoIndicator;
    }

    public void setCA_HashAlgoIndicator(String CA_HashAlgoIndicator) {
        this.CA_HashAlgoIndicator = CA_HashAlgoIndicator;
    }

    public String getCA_PKAlgoIndicator() {
        return CA_PKAlgoIndicator;
    }

    public void setCA_PKAlgoIndicator(String CA_PKAlgoIndicator) {
        this.CA_PKAlgoIndicator = CA_PKAlgoIndicator;
    }

    public String getLengthOfCAPKModulus() {
        return LengthOfCAPKModulus;
    }

    public void setLengthOfCAPKModulus(String lengthOfCAPKModulus) {
        LengthOfCAPKModulus = lengthOfCAPKModulus;
    }

    public String getCAPKModulus() {
        return CAPKModulus;
    }

    public void setCAPKModulus(String CAPKModulus) {
        this.CAPKModulus = CAPKModulus;
    }

    public String getLengthOfCAPKExponent() {
        return LengthOfCAPKExponent;
    }

    public void setLengthOfCAPKExponent(String lengthOfCAPKExponent) {
        LengthOfCAPKExponent = lengthOfCAPKExponent;
    }

    public String getCAPKExponent() {
        return CAPKExponent;
    }

    public void setCAPKExponent(String CAPKExponent) {
        this.CAPKExponent = CAPKExponent;
    }

    public String getChecksumHash() {
        return ChecksumHash;
    }

    public void setChecksumHash(String checksumHash) {
        ChecksumHash = checksumHash;
    }

    public String getCAPKExpDate() {
        return CAPKExpDate;
    }

    public void setCAPKExpDate(String CAPKExpDate) {
        this.CAPKExpDate = CAPKExpDate;
    }

    public ContentValues toContentValues() {
        ContentValues values = new ContentValues();
        values.put("IndexID", getIndexID());
        values.put("RID", getRID());
        values.put("CA_PKIndex", getCA_PKIndex());
        values.put("CA_HashAlgoIndicator", CA_HashAlgoIndicator);
        values.put("CA_PKAlgoIndicator", getCA_PKAlgoIndicator());
        values.put("LengthOfCAPKModulus", getLengthOfCAPKModulus());
        values.put("CAPKModulus", getCAPKModulus());
        values.put("LengthOfCAPKExponent", getLengthOfCAPKExponent());
        values.put("CAPKExponent", getCAPKExponent());
        values.put("ChecksumHash", getChecksumHash());
        values.put("CAPKExpDate", getCAPKExpDate());

        return values;
    }

    public void fillByCursor(Cursor cur) {
        setIndexID(cur.getString(cur.getColumnIndexOrThrow("IndexID")));
        setRID(cur.getString(cur.getColumnIndexOrThrow("RID")));
        setCA_PKIndex(cur.getString(cur.getColumnIndexOrThrow("CA_PKIndex")));
        setCA_HashAlgoIndicator(cur.getString(cur.getColumnIndexOrThrow("CA_HashAlgoIndicator")));
        setCA_PKAlgoIndicator(cur.getString(cur.getColumnIndexOrThrow("CA_PKAlgoIndicator")));
        setLengthOfCAPKModulus(cur.getString(cur.getColumnIndexOrThrow("LengthOfCAPKModulus")));
        setLengthOfCAPKModulus(cur.getString(cur.getColumnIndexOrThrow("LengthOfCAPKModulus")));
        setCAPKModulus(cur.getString(cur.getColumnIndexOrThrow("CAPKModulus")));
        setLengthOfCAPKExponent(cur.getString(cur.getColumnIndexOrThrow("LengthOfCAPKExponent")));
        setCAPKExponent(cur.getString(cur.getColumnIndexOrThrow("CAPKExponent")));
        setChecksumHash(cur.getString(cur.getColumnIndexOrThrow("ChecksumHash")));
        setCAPKExpDate(cur.getString(cur.getColumnIndexOrThrow("CAPKExpDate")));
    }
}