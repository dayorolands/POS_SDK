package com.appzonegroup.app.fasttrack.model;

/**
 * Created by Joseph on 9/7/2018.
 */

public class AgentInfo {

    private String Message;
    private boolean Status;
    private String AgentName;
    private String PhoneNumber;

    public String getMessage() {
        return Message;
    }

    public void setMessage(String message) {
        Message = message;
    }

    public boolean isStatus() {
        return Status;
    }

    public void setStatus(boolean status) {
        Status = status;
    }

    public String getAgentName() {
        return AgentName;
    }

    public void setAgentName(String agentName) {
        AgentName = agentName;
    }

    public String getPhoneNumber() {
        return PhoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        PhoneNumber = phoneNumber;
    }
}
