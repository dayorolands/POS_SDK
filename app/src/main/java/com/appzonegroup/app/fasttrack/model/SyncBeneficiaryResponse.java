package com.appzonegroup.app.fasttrack.model;

/**
 * Created by Oto-obong on 24/08/2017.
 */

public class SyncBeneficiaryResponse {

    private Boolean Status;

    private String ResponseMessage;

    public Boolean getStatus() {
        return Status;
    }

    public void setStatus(Boolean status) {
        Status = status;
    }

    public String getResponseMessage() {
        return ResponseMessage;
    }

    public void setResponseMessage(String responseMessage) {
        ResponseMessage = responseMessage;
    }
}
