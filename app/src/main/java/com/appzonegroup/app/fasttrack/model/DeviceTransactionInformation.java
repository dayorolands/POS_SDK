package com.appzonegroup.app.fasttrack.model;

import android.content.Context;

import com.appzonegroup.app.fasttrack.utility.LocalStorage;
import com.appzonegroup.app.fasttrack.utility.Misc;

/**
 * Created by Joseph on 12/15/2017.
 */

public class DeviceTransactionInformation {

    private int ID;
    private String DateReceivedString;
    private String DateEndedString;
    private String SessionID;
    private String InstitutionCode;
    private String AgentPhoneNumber;
    private int RequestCount;
    private int SuccessCount;
    private int NoInternet;
    private int NoResponse;
    private int ErrorResponse;
    private String RamSize;
    private float PercentageLeftOver;
    private String MemorySpace;
    private String MemorySpaceLeft;

    public String getAppName() {
        return AppName;
    }

    public void setAppName(String appName){
        this.AppName = appName;
    }

    private String AppName;


    private static DeviceTransactionInformation information;

    public DeviceTransactionInformation(){}

    public static DeviceTransactionInformation getInstance(Context context){
        if (information == null) {
            information = new DeviceTransactionInformation();
            information.setAgentPhoneNumber(LocalStorage.GetValueFor(AppConstants.AGENT_PHONE, context));
            information.setInstitutionCode(LocalStorage.GetValueFor(AppConstants.INSTITUTION_CODE, context));
        }
        //Get Current sessionID
        information.setSessionID(LocalStorage.GetValueFor(AppConstants.getSessionID(), context));
        information.setDateReceived(Misc.getCurrentDateLongString());
        information.setMemorySpace(Misc.getTotalMemory());
        information.setMemorySpaceLeft(Misc.getAvailableMemory());
        long[] RAMInfo = Misc.getRAM(context);
        information.setRamSize(Misc.formatMemorySize(RAMInfo[0]));
        information.setPercentageLeftOver((float)RAMInfo[0]/(float) RAMInfo[1] * (float) 100);
        information.setNoInternet(Misc.getTransactionMonitorCounter(context, AppConstants.getNoInternetCount()));
        information.setErrorResponse(Misc.getTransactionMonitorCounter(context, AppConstants.getErrorResponseCount()));
        information.setNoResponse(Misc.getTransactionMonitorCounter(context, AppConstants.getNoResponseCount()));
        information.setRequestCount(Misc.getTransactionMonitorCounter(context, AppConstants.getRequestCount()));
        information.setSuccessCount(Misc.getTransactionMonitorCounter(context, AppConstants.getSuccessCount()));
        return information;
    }

    public static DeviceTransactionInformation getInstance(Context context, String sessionID){

        if (information == null) {
            information = new DeviceTransactionInformation();
            information.setAgentPhoneNumber(LocalStorage.getPhoneNumber(context));
            information.setInstitutionCode(LocalStorage.getInstitutionCode(context));
        }
        //Get Current sessionID
        information.setSessionID(sessionID);
        information.setDateReceived(Misc.getCurrentDateLongString());
        information.setMemorySpace(Misc.getTotalMemory());
        information.setMemorySpaceLeft(Misc.getAvailableMemory());
        long[] RAMInfo = Misc.getRAM(context);
        information.setRamSize(Misc.formatMemorySize(RAMInfo[0]));
        information.setPercentageLeftOver((float)RAMInfo[0]/(float) RAMInfo[1] * (float) 100);
        information.setNoInternet(Misc.getTransactionMonitorCounter(context, AppConstants.getNoInternetCount()));
        information.setErrorResponse(Misc.getTransactionMonitorCounter(context, AppConstants.getErrorResponseCount()));
        information.setNoResponse(Misc.getTransactionMonitorCounter(context, AppConstants.getNoResponseCount()));
        information.setRequestCount(Misc.getTransactionMonitorCounter(context, AppConstants.getRequestCount()));
        information.setSuccessCount(Misc.getTransactionMonitorCounter(context, AppConstants.getSuccessCount()));
        return information;
    }

    public String getDateEnded() {
        return DateEndedString;
    }

    public void setDateEnded() {
        this.DateEndedString = Misc.getCurrentDateLongString();
    }

    public int getID() {
        return ID;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public String getDateReceived() {
        return DateReceivedString;
    }

    public void setDateReceived(String dateReceived) {
        DateReceivedString = dateReceived;
    }

    public String getSessionID() {
        return SessionID;
    }

    public void setSessionID(String sessionID) {
        SessionID = sessionID;
    }

    public String getInstitutionCode() {
        return InstitutionCode;
    }

    public void setInstitutionCode(String institutionCode) {
        InstitutionCode = institutionCode;
    }

    public String getAgentPhoneNumber() {
        return AgentPhoneNumber;
    }

    public void setAgentPhoneNumber(String agentPhoneNumber) {
        AgentPhoneNumber = agentPhoneNumber;
    }

    public int getRequestCount() {
        return RequestCount;
    }

    public void setRequestCount(int requestCount) {
        RequestCount = requestCount;
        this.setDateEnded();
    }

    public int getSuccessCount() {
        return SuccessCount;
    }

    public void setSuccessCount(int successCount) {
        SuccessCount = successCount;
        this.setDateEnded();
    }

    public int getNoInternet() {
        return NoInternet;
    }

    public void setNoInternet(int noInternet) {
        NoInternet = noInternet;
        this.setDateEnded();
    }

    public int getNoResponse() {
        return NoResponse;
    }

    public void setNoResponse(int noResponse) {
        NoResponse = noResponse;
        this.setDateEnded();
    }

    public int getErrorResponse() {
        return ErrorResponse;
    }

    public void setErrorResponse(int errorResponse) {
        ErrorResponse = errorResponse;
        this.setDateEnded();
    }

    public String getRamSize() {
        return RamSize;
    }

    public void setRamSize(String ramSize) {
        RamSize = ramSize;
    }

    public float getPercentageLeftOver() {
        return PercentageLeftOver;
    }

    public void setPercentageLeftOver(float percentageLeftOver) {
        PercentageLeftOver = percentageLeftOver;
    }

    public String getMemorySpace() {
        return MemorySpace;
    }

    public void setMemorySpace(String memorySpace) {
        MemorySpace = memorySpace;
    }

    public String getMemorySpaceLeft() {
        return MemorySpaceLeft;
    }

    public void setMemorySpaceLeft(String memorySpaceLeft) {
        MemorySpaceLeft = memorySpaceLeft;
    }

}
