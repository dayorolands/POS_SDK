package com.urovo.v67.aidUtils;

import android.content.ContentValues;
import android.database.Cursor;

public class AIDBean {
    public String IndexID;
    public String AID = "";
    public String AIDLable = "";
    public String terminalAIDVersionNumber = "";
    public String exactOnlySelection = "";
    public String skipEMVProgressing = "";
    public String DefaultTDOL = "";
    public String DefaultDDOL = "";
    public String EMVAdditionalTags = "";
    public String denialActionCode = "";
    public String onlineActionCode = "";
    public String defaultActionCode = "";
    public String thresholdValue = "";
    public String targetPercebtage = "";
    public String maxiumTargetPercent = "";

    public String getIndexID() {
        return IndexID;
    }

    public void setIndexID(String indexID) {
        IndexID = indexID;
    }

    public String getAID() {
        return AID;
    }

    public void setAID(String AID) {
        this.AID = AID;
    }

    public String getAIDLable() {
        return AIDLable;
    }

    public void setAIDLable(String AIDLable) {
        this.AIDLable = AIDLable;
    }

    public String getTerminalAIDVersionNumber() {
        return terminalAIDVersionNumber;
    }

    public void setTerminalAIDVersionNumber(String terminalAIDVersionNumber) {
        this.terminalAIDVersionNumber = terminalAIDVersionNumber;
    }

    public String getExactOnlySelection() {
        return exactOnlySelection;
    }

    public void setExactOnlySelection(String exactOnlySelection) {
        this.exactOnlySelection = exactOnlySelection;
    }

    public String getSkipEMVProgressing() {
        return skipEMVProgressing;
    }

    public void setSkipEMVProgressing(String skipEMVProgressing) {
        this.skipEMVProgressing = skipEMVProgressing;
    }

    public String getDefaultTDOL() {
        return DefaultTDOL;
    }

    public void setDefaultTDOL(String defaultTDOL) {
        DefaultTDOL = defaultTDOL;
    }

    public String getDefaultDDOL() {
        return DefaultDDOL;
    }

    public void setDefaultDDOL(String defaultDDOL) {
        DefaultDDOL = defaultDDOL;
    }

    public String getEMVAdditionalTags() {
        return EMVAdditionalTags;
    }

    public void setEMVAdditionalTags(String EMVAdditionalTags) {
        this.EMVAdditionalTags = EMVAdditionalTags;
    }

    public String getDenialActionCode() {
        return denialActionCode;
    }

    public void setDenialActionCode(String denialActionCode) {
        this.denialActionCode = denialActionCode;
    }

    public String getOnlineActionCode() {
        return onlineActionCode;
    }

    public void setOnlineActionCode(String onlineActionCode) {
        this.onlineActionCode = onlineActionCode;
    }

    public String getDefaultActionCode() {
        return defaultActionCode;
    }

    public void setDefaultActionCode(String defaultActionCode) {
        this.defaultActionCode = defaultActionCode;
    }

    public String getThresholdValue() {
        return thresholdValue;
    }

    public void setThresholdValue(String thresholdValue) {
        this.thresholdValue = thresholdValue;
    }

    public String getTargetPercebtage() {
        return targetPercebtage;
    }

    public void setTargetPercebtage(String targetPercebtage) {
        this.targetPercebtage = targetPercebtage;
    }

    public String getMaxiumTargetPercent() {
        return maxiumTargetPercent;
    }

    public void setMaximumTargetPercent(String maxiumTargetPercent) {
        this.maxiumTargetPercent = maxiumTargetPercent;
    }

}
