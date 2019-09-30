package com.appzonegroup.app.fasttrack.model;

/**
 * Created by Joseph on 12/14/2017.
 */

public class CardTransactionResponse {

    private String ResponseDetails;
    private boolean Status;
    private String Message;
    private boolean TransactionExists;

    public String getResponseDetails() {
        return ResponseDetails;
    }

    public void setResponseDetails(String responseDetails) {
        ResponseDetails = responseDetails;
    }

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

    public boolean isTransactionExists() {
        return TransactionExists;
    }

    public void setTransactionExists(boolean transactionExists) {
        TransactionExists = transactionExists;
    }
}
