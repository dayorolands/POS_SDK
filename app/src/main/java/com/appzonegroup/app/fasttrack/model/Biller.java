package com.appzonegroup.app.fasttrack.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by Oto-obong on 02/08/2017.
 */

public class Biller implements Serializable {
    @SerializedName("ID")
    private int BillerID;
    @SerializedName("Name")
    private String billerNameField;
    @SerializedName("CategoryId")
    private String categoryIdField;
    private String categoryNameField;
    private int merchantCategoryIdField;
    private int Surcharge;
    private String CustomerField1;
    private String CustomerField2;
    private Boolean hasBillerItemsField;
    private int merchantBillerIdField;
    private int showOnWebField;
    private int showOnMobileField;
    private int showOnAtmField;
    private String accessBillerNameField;

    @SerializedName("BillerCategoryID")
    private String billerCategoryId;

    public int getBillerID() {
        return BillerID;
    }

    public void setBillerID(int billerID) {
        this.BillerID = billerID;
    }

    public String getBillerNameField() {
        return billerNameField;
    }

    public void setBillerNameField(String billerNameField) {
        this.billerNameField = billerNameField;
    }

    public String getCategoryIdField() {
        return categoryIdField;
    }

    public void setCategoryIdField(String categoryIdField) {
        this.categoryIdField = categoryIdField;
    }

    public String getCategoryNameField() {
        return categoryNameField;
    }

    public void setCategoryNameField(String categoryNameField) {
        this.categoryNameField = categoryNameField;
    }

    public int getMerchantCategoryIdField() {
        return merchantCategoryIdField;
    }

    public void setMerchantCategoryIdField(int merchantCategoryIdField) {
        this.merchantCategoryIdField = merchantCategoryIdField;
    }

    public int getSurcharge() {
        return Surcharge;
    }

    public void setSurcharge(int surcharge) {
        this.Surcharge = surcharge;
    }

    public String getCustomerField1() {
        return CustomerField1;
    }

    public void setCustomerField1(String customerField1) {
        this.CustomerField1 = customerField1;
    }

    public String getCustomerField2() {
        return CustomerField2;
    }

    public void setCustomerField2(String customerField2) {
        this.CustomerField2 = customerField2;
    }

    public Boolean getHasBillerItemsField() {
        return hasBillerItemsField;
    }

    public void setHasBillerItemsField(Boolean hasBillerItemsField) {
        this.hasBillerItemsField = hasBillerItemsField;
    }

    public int getMerchantBillerIdField() {
        return merchantBillerIdField;
    }

    public void setMerchantBillerIdField(int merchantBillerIdField) {
        this.merchantBillerIdField = merchantBillerIdField;
    }

    public int getShowOnWebField() {
        return showOnWebField;
    }

    public void setShowOnWebField(int showOnWebField) {
        this.showOnWebField = showOnWebField;
    }

    public int getShowOnMobileField() {
        return showOnMobileField;
    }

    public void setShowOnMobileField(int showOnMobileField) {
        this.showOnMobileField = showOnMobileField;
    }

    public int getShowOnAtmField() {
        return showOnAtmField;
    }

    public void setShowOnAtmField(int showOnAtmField) {
        this.showOnAtmField = showOnAtmField;
    }

    public String getAccessBillerNameField() {
        return accessBillerNameField;
    }

    public void setAccessBillerNameField(String accessBillerNameField) {
        this.accessBillerNameField = accessBillerNameField;
    }

    public String getBillerCategoryId() {
        return billerCategoryId;
    }

    public void setBillerCategoryId(String billerCategoryId) {
        this.billerCategoryId = billerCategoryId;
    }
}
