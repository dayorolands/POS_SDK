package com.appzonegroup.app.fasttrack.model;

/**
 * Created by Oto-obong on 18/07/2017.
 */

public class ResponseBody {

    String reference;
    String status;
    ModelResponse modelResponse;
    String accountNumber;


    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public ModelResponse getModelResponse() {
        return modelResponse;
    }

    public void setModelResponse(ModelResponse modelResponse) {
        this.modelResponse = modelResponse;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }
}
