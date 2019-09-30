package com.appzonegroup.app.fasttrack.model;

/**
 * Created by Joseph on 12/14/2017.
 */

public class CardValidationResponseModel
{
    private boolean Status;
    private String Message;
    private String ID;
    private boolean PhoneNumberExist;
    private boolean CardExist;
    private boolean BinExist;

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

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public boolean isPhoneNumberExist() {
        return PhoneNumberExist;
    }

    public void setPhoneNumberExist(boolean phoneNumberExist) {
        PhoneNumberExist = phoneNumberExist;
    }

    public boolean isCardExist() {
        return CardExist;
    }

    public void setCardExist(boolean cardExist) {
        CardExist = cardExist;
    }

    public boolean isBinExist() {
        return BinExist;
    }

    public void setBinExist(boolean binExist) {
        BinExist = binExist;
    }
}
