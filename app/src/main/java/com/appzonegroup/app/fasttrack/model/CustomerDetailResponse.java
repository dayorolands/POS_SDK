package com.appzonegroup.app.fasttrack.model;

import java.io.Serializable;

/**
 * Created by Oto-obong on 20/10/2017.
 */

public class CustomerDetailResponse implements Serializable {


    Boolean Status;
    String StatusMessage;

    public Boolean getStatus() {
        return Status;
    }

    public void setStatus(Boolean status) {
        Status = status;
    }

    public String getStatusMessage() {
        return StatusMessage;
    }

    public void setStatusMessage(String statusMessage) {
        StatusMessage = statusMessage;
    }
}
