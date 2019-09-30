package com.appzonegroup.app.fasttrack.model;

/**
 * Created by Joseph on 2/21/2018.
 */

public class BankOneAPICallBaseResponse {
    private boolean Status;
    private String StatusDetails;

    public boolean isStatus() {
        return Status;
    }

    public void setStatus(boolean status) {
        Status = status;
    }

    public String getStatusDetails() {
        return StatusDetails;
    }

    public void setStatusDetails(String statusDetails) {
        StatusDetails = statusDetails;
    }
}
