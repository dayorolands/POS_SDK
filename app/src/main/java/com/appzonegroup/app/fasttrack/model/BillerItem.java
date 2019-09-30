package com.appzonegroup.app.fasttrack.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by Oto-obong on 12/08/2017.
 */

public class BillerItem implements Serializable {
    @SerializedName("BillerId")
    private int merchantBillerIdField;
    @SerializedName("ID")
    private String billerItemIdField;
    @SerializedName("Code")
    private String paymentCodeField;
    private String principalAccountNumberField;
    private String surchargeAccountNumberField;
    private String narrationPrefixField;
    private boolean canModifyPriceField;
    @SerializedName("Amount")
    private Double amountField;
    private String customerFieldOneField;
    private String customerFieldTwoField;
    //    private int merchantBillerItemIdField;
    @SerializedName("Name")
    private String billerItemNameField;
    private String PropertyChanged;

    public int getMerchantBillerIdField() {
        return merchantBillerIdField;
    }

    public void setMerchantBillerIdField(int merchantBillerIdField) {
        this.merchantBillerIdField = merchantBillerIdField;
    }

    public String getBillerItemIdField() {
        return billerItemIdField;
    }

    public void setBillerItemIdField(String billerItemIdField) {
        this.billerItemIdField = billerItemIdField;
    }

    public String getPaymentCodeField() {
        return paymentCodeField;
    }

    public void setPaymentCodeField(String paymentCodeField) {
        this.paymentCodeField = paymentCodeField;
    }

    public String getPrincipalAccountNumberField() {
        return principalAccountNumberField;
    }

    public void setPrincipalAccountNumberField(String principalAccountNumberField) {
        this.principalAccountNumberField = principalAccountNumberField;
    }

    public String getSurchargeAccountNumberField() {
        return surchargeAccountNumberField;
    }

    public void setSurchargeAccountNumberField(String surchargeAccountNumberField) {
        this.surchargeAccountNumberField = surchargeAccountNumberField;
    }

    public String getNarrationPrefixField() {
        return narrationPrefixField;
    }

    public void setNarrationPrefixField(String narrationPrefixField) {
        this.narrationPrefixField = narrationPrefixField;
    }

    public boolean isCanModifyPriceField() {
        return canModifyPriceField;
    }

    public void setCanModifyPriceField(boolean canModifyPriceField) {
        this.canModifyPriceField = canModifyPriceField;
    }

    public double getAmountField() {
        return amountField;
    }

    public void setAmountField(double amountField) {
        this.amountField = amountField;
    }

    public String getCustomerFieldOneField() {
        return customerFieldOneField;
    }

    public void setCustomerFieldOneField(String customerFieldOneField) {
        this.customerFieldOneField = customerFieldOneField;
    }

    public String getCustomerFieldTwoField() {
        return customerFieldTwoField;
    }

    public void setCustomerFieldTwoField(String customerFieldTwoField) {
        this.customerFieldTwoField = customerFieldTwoField;
    }

//    public int getMerchantBillerItemIdField() {
//        return merchantBillerItemIdField;
//    }
//
//    public void setMerchantBillerItemIdField(int merchantBillerItemIdField) {
//        this.merchantBillerItemIdField = merchantBillerItemIdField;
//    }

    public String getBillerItemNameField() {
        return billerItemNameField;
    }

    public void setBillerItemNameField(String billerItemNameField) {
        this.billerItemNameField = billerItemNameField;
    }

    public String getPropertyChanged() {
        return PropertyChanged;
    }

    public void setPropertyChanged(String propertyChanged) {
        PropertyChanged = propertyChanged;
    }
}
