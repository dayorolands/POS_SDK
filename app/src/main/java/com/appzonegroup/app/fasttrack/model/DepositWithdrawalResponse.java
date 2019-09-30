package com.appzonegroup.app.fasttrack.model;

/**
 * Created by Oto-obong on 19/07/2017.
 */

public class DepositWithdrawalResponse {


   String reference;
    String userID;
    String status;
    String type;
    String description;
    ModelResponse modelResponse;


    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ModelResponse getModelResponse() {
        return modelResponse;
    }

    public void setModelResponse(ModelResponse modelResponse) {
        this.modelResponse = modelResponse;
    }
}
