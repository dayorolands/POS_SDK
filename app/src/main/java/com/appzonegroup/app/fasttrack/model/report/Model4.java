package com.appzonegroup.app.fasttrack.model.report;

import com.appzonegroup.app.fasttrack.model.ReportItem;

/**
 * Registration report model
 * Created by Joseph on 9/9/2018.
 */

public class Model4 extends Model1 {

    public Model4(){}

    public Model4(ReportItem reportItem)
    {
        setID(reportItem.getID() + "");
        setDate(reportItem.getDate());
        setFrom(reportItem.getFrom());
        setFromPhoneNumber(reportItem.getFromPhoneNumber());
        setTo(reportItem.getTo());
        setCustomerName(reportItem.getCustomerName());
        setCustomerPhone(reportItem.getCustomerPhone());
        setProductCode(reportItem.getProductCode());
        setProductName(reportItem.getProductName());
    }

    private String CustomerName;
    private String CustomerPhone;
    private String ProductCode;
    private String ProductName;

    public String getCustomerName() {
        return CustomerName;
    }

    public void setCustomerName(String customerName) {
        CustomerName = customerName;
    }

    public String getCustomerPhone() {
        return CustomerPhone;
    }

    public void setCustomerPhone(String customerPhone) {
        CustomerPhone = customerPhone;
    }

    public String getProductCode() {
        return ProductCode;
    }

    public void setProductCode(String productCode) {
        ProductCode = productCode;
    }

    public String getProductName() {
        return ProductName;
    }

    public void setProductName(String productName) {
        ProductName = productName;
    }
}
