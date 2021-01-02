package com.appzonegroup.app.fasttrack.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by Oto-obong on 21/07/2017.
 */

public class Response implements Serializable {

    @SerializedName("ReponseMessage")
    private String ReponseMessage ;

    @SerializedName("IsSuccessful")
    private boolean IsSuccessful ;

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
