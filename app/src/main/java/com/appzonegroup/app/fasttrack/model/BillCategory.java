package com.appzonegroup.app.fasttrack.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Oto-obong on 02/08/2017.
 */

public class BillCategory {


    private String ID;

    private String Name;

    private String PropertyChanged;

    private String Description;

    private String IsAirtime;

    @SerializedName("BillerCategoryID")
    private String billerCategoryId;

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        this.Name = name;
    }

    public String getPropertyChanged() {
        return PropertyChanged;
    }

    public void setPropertyChanged(String propertyChanged) {
        PropertyChanged = propertyChanged;
    }

    public String getDescription() {
        return Description;
    }

    public void setDescription(String description) {
        Description = description;
    }

    public String getIsAirtime() {
        return IsAirtime;
    }

    public void setIsAirtime(String isAirtime) {
        IsAirtime = isAirtime;
    }

    public String getBillerCategoryId() {
        return billerCategoryId;
    }

    public void setBillerCategoryId(String billerCategoryId) {
        this.billerCategoryId = billerCategoryId;
    }
}
