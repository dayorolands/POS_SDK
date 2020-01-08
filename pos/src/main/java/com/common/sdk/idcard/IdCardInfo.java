package com.common.sdk.idcard;

import android.os.Parcel;
import android.os.Parcelable;
import com.telpo.tps550.api.idcard.IdCard;

public class IdCardInfo implements Parcelable {
    public static final Parcelable.Creator<IdCardInfo> CREATOR = new Parcelable.Creator<IdCardInfo>() {
        public IdCardInfo createFromParcel(Parcel source) {
            return new IdCardInfo(source);
        }

        public IdCardInfo[] newArray(int size) {
            return new IdCardInfo[size];
        }
    };
    private String adress;
    private String born;
    byte[] img = new byte[IdCard.READER_VID_BIG];
    private String name;
    private String nation;
    private String number;
    private String office;
    private String sex;
    private String term;

    public IdCardInfo() {
    }

    public IdCardInfo(Parcel in) {
        this.name = in.readString();
        this.sex = in.readString();
        this.nation = in.readString();
        this.born = in.readString();
        this.adress = in.readString();
        this.number = in.readString();
        this.office = in.readString();
        this.term = in.readString();
        in.readByteArray(this.img);
    }

    public String getName() {
        return this.name;
    }

    public String getSex() {
        return this.sex;
    }

    public String getNation() {
        return this.nation;
    }

    public String getBorn() {
        return this.born;
    }

    public String getAdress() {
        return this.adress;
    }

    public String getNumber() {
        return this.number;
    }

    public String getOffice() {
        return this.office;
    }

    public String getTerm() {
        return this.term;
    }

    public byte[] getImg() {
        return this.img;
    }

    public void setName(String name2) {
        this.name = name2;
    }

    public void setSex(String sex2) {
        this.sex = sex2;
    }

    public void setNation(String nation2) {
        this.nation = nation2;
    }

    public void setBorn(String born2) {
        this.born = born2;
    }

    public void setAdress(String adress2) {
        this.adress = adress2;
    }

    public void setNumber(String number2) {
        this.number = number2;
    }

    public void setOffice(String office2) {
        this.office = office2;
    }

    public void setTerm(String term2) {
        this.term = term2;
    }

    public void setImg(byte[] img2) {
        this.img = img2;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.name);
        dest.writeString(this.sex);
        dest.writeString(this.nation);
        dest.writeString(this.born);
        dest.writeString(this.adress);
        dest.writeString(this.number);
        dest.writeString(this.office);
        dest.writeString(this.term);
        dest.writeByteArray(this.img);
    }

    public int describeContents() {
        return 0;
    }
}
