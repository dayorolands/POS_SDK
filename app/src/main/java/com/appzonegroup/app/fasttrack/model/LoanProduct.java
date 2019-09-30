package com.appzonegroup.app.fasttrack.model;

/**
 * Created by Joseph on 6/3/2016.
 */
public class LoanProduct {

    private long ID ;
    private String Name ;
    private double MinimumAmount;
    private double MaximumAmount;
    private String InstitutionCode;

    public long getID() {
        return ID;
    }

    public void setID(long ID) {
        this.ID = ID;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public double getMinimumAmount() {
        return MinimumAmount;
    }

    public void setMinimumAmount(double minimumAmount) {
        MinimumAmount = minimumAmount;
    }

    public double getMaximumAmount() {
        return MaximumAmount;
    }

    public void setMaximumAmount(double maximumAmount) {
        MaximumAmount = maximumAmount;
    }

    @Override
    public String toString(){
        return getName() + " (N" + getMinimumAmount() + " - N" + getMaximumAmount() + ")";
    }

    public String getInstitutionCode() {
        return InstitutionCode;
    }

    public void setInstitutionCode(String institutionCode) {
        InstitutionCode = institutionCode;
    }
}
