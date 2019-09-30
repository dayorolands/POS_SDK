package com.appzonegroup.app.fasttrack.model.jsonbody;

import java.io.Serializable;

/**
 * Created by Oto-obong on 13/10/2017.
 */

public class AccountEnquiryResponseModel implements Serializable {

    private String accountname;
    private String accountnumber ;
    private String customerid ;
    private String isvalid ;
    private String isactive ;
    private String isstaffaccount ;
    private double availablebalance ;
    private String primaryemail ;
    private String primaryphone;
    private String accountproductcode;
    private String accountproductname;
    private String accountstatus;
    private String accountstatusdescription;
    private String accountName;

    public String getAccountname() {
        return accountname;
    }

    public void setAccountname(String accountname) {
        this.accountname = accountname;
    }

    public String getAccountnumber() {
        return accountnumber;
    }

    public void setAccountnumber(String accountnumber) {
        this.accountnumber = accountnumber;
    }

    public String getCustomerid() {
        return customerid;
    }

    public void setCustomerid(String customerid) {
        this.customerid = customerid;
    }

    public String getIsvalid() {
        return isvalid;
    }

    public void setIsvalid(String isvalid) {
        this.isvalid = isvalid;
    }

    public String getIsactive() {
        return isactive;
    }

    public void setIsactive(String isactive) {
        this.isactive = isactive;
    }

    public String getIsstaffaccount() {
        return isstaffaccount;
    }

    public void setIsstaffaccount(String isstaffaccount) {
        this.isstaffaccount = isstaffaccount;
    }

    public double getAvailablebalance() {
        return availablebalance;
    }

    public void setAvailablebalance(double availablebalance) {
        this.availablebalance = availablebalance;
    }

    public String getPrimaryemail() {
        return primaryemail;
    }

    public void setPrimaryemail(String primaryemail) {
        this.primaryemail = primaryemail;
    }

    public String getPrimaryphone() {
        return primaryphone;
    }

    public void setPrimaryphone(String primaryphone) {
        this.primaryphone = primaryphone;
    }

    public String getAccountproductcode() {
        return accountproductcode;
    }

    public void setAccountproductcode(String accountproductcode) {
        this.accountproductcode = accountproductcode;
    }

    public String getAccountproductname() {
        return accountproductname;
    }

    public void setAccountproductname(String accountproductname) {
        this.accountproductname = accountproductname;
    }

    public String getAccountstatus() {
        return accountstatus;
    }

    public void setAccountstatus(String accountstatus) {
        this.accountstatus = accountstatus;
    }

    public String getAccountstatusdescription() {
        return accountstatusdescription;
    }

    public void setAccountstatusdescription(String accountstatusdescription) {
        this.accountstatusdescription = accountstatusdescription;
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }
}
