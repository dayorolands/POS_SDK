package com.appzonegroup.app.fasttrack.model;

/**
 * Created by Joseph on 3/13/2018.
 */

public class TransferResponse {

    private String ReponseMessage;
    private boolean IsSuccessful;

    public String getReponseMessage() {
        return ReponseMessage;
    }

    public void setReponseMessage(String reponseMessage) {
        ReponseMessage = reponseMessage;
    }

    public boolean isSuccessful() {
        return IsSuccessful;
    }

    public void setSuccessful(boolean successful) {
        IsSuccessful = successful;
    }
}
