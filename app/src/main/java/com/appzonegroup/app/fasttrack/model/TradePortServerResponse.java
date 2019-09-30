package com.appzonegroup.app.fasttrack.model;

/**
 * Created by Joseph on 3/13/2018.
 */

public class TradePortServerResponse {
    private boolean Status;
    private String Message;

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

    /*public String getTerminalID() {
        return TerminalID;
    }

    public void setTerminalID(String terminalID) {
        TerminalID = terminalID;
    }*/
}
