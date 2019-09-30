package com.appzonegroup.app.fasttrack.model;

/**
 * Created by Oto-obong on 02/08/2017.
 */

public class BillCategory {


    private String ID;

    private String Name;

    private String PropertyChanged;

    private String Description;

    private String IsAirtime;


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
}
