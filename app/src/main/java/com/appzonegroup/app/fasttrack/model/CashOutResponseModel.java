package com.appzonegroup.app.fasttrack.model;

/**
 * Created by Joseph on 1/18/2018.
 */

public class CashOutResponseModel {

    private boolean Status;
    private String Message;
    private String ResponseDetails;

    public boolean isStatus() {
        return Status;
    }

    public void setStatus(boolean status) {
        Status = status;
    }

    public String getMessage() {
        return Message;
    }

    public void setMessage(String message) {
        Message = message;
    }

    public String getResponseDetails() {
        return ResponseDetails;
    }

    public void setResponseDetails(String responseDetails) {
        ResponseDetails = responseDetails;
    }
}
