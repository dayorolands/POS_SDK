package com.appzonegroup.app.fasttrack.model;

/**
 * Created by Joseph on 6/3/2016.
 */
public class ModelResponse {

    String responseCode;
    String transactionReference;
    String responseMessage;
    String responsemessage;
    public String isSuccessful;
    String ResponseMessage;

    public String getIsSuccessful() {
        return isSuccessful;
    }

    public void setIsSuccessful(String isSuccessful) {
        this.isSuccessful = isSuccessful;
    }

    public String getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(String responseCode) {
        this.responseCode = responseCode;
    }

    public String getTransactionReference() {
        return transactionReference;
    }

    public void setTransactionReference(String transactionReference) {
        this.transactionReference = transactionReference;
    }

    public String getResponseMessage() {
        return responseMessage;
    }

    public void setResponseMessage(String responseMessage) {
        this.responseMessage = responseMessage;
    }

    public String getResponsemessage() {
        return responsemessage;
    }

    public void setResponsemessage(String responsemessage) {
        this.responsemessage = responsemessage;
    }
}