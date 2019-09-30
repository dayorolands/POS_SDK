package com.appzonegroup.app.fasttrack.model;

/**
 * Created by Oto-obong on 24/07/2017.
 */

public class LoanOriginationResponse {

    Integer Status;
    String ResponseMessage;

    public Integer getStatus() {
        return Status;
    }

    public void setStatus(Integer status) {
        Status = status;
    }

    public String getResponseMessage() {
        return ResponseMessage;
    }

    public void setResponseMessage(String responseMessage) {
        ResponseMessage = responseMessage;
    }
}
