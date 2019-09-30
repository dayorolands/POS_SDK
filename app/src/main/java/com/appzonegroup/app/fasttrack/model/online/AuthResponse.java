package com.appzonegroup.app.fasttrack.model.online;

import org.json.JSONObject;

/**
 * Created by fdamilola on 9/17/15.
 * Contact fdamilola@gmail.com or fdamilola@hextremelabs.com or fdamilola@echurch.ng
 */
public class AuthResponse {
    private String phoneNumber, sessionId, activationCode;
    public AuthResponse(String phoneNumber, String activationCode)
    {
        setPhoneNumber(phoneNumber);
        setActivationCode(activationCode);
    }

    public AuthResponse(JSONObject js){
        setPhoneNumber(js.optString("phone_number"));
        setSessionId(js.optString("session_id"));
        setActivationCode(js.optString("activationCode"));
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    private void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getActivationCode() {
        return activationCode;
    }

    public void setActivationCode(String activationCode) {
        this.activationCode = activationCode;
    }
}
