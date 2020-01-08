package com.telpo.tps550.api.idcard;

import java.io.Serializable;

public class IdentityMsg implements Serializable {
    private static final long serialVersionUID = -7696282392790000305L;
    private String address;
    private String apartment;
    private String born;
    private String cardSignal = null;
    private String card_type;
    private String cn_name;
    private String country;
    private byte[] head_photo;
    private String idcard_version;
    private String issuesNum = null;
    private String name;
    private String nation = null;
    private String no;
    private String passNum = null;
    private String period;
    private String reserve;
    private String sex;

    public String getName() {
        return this.name;
    }

    public void setName(String name2) {
        this.name = name2;
    }

    public String getSex() {
        return this.sex;
    }

    public void setSex(String sex2) {
        this.sex = sex2;
    }

    public String getNation() {
        return this.nation;
    }

    public void setNation(String nation2) {
        this.nation = nation2;
    }

    public String getBorn() {
        return this.born;
    }

    public void setBorn(String born2) {
        this.born = born2;
    }

    public String getAddress() {
        return this.address;
    }

    public void setAddress(String address2) {
        this.address = address2;
    }

    public String getApartment() {
        return this.apartment;
    }

    public void setApartment(String apartment2) {
        this.apartment = apartment2;
    }

    public String getPeriod() {
        return this.period;
    }

    public void setPeriod(String period2) {
        this.period = period2;
    }

    public String getNo() {
        return this.no;
    }

    public void setNo(String no2) {
        this.no = no2;
    }

    public byte[] getHead_photo() {
        return this.head_photo;
    }

    public void setHead_photo(byte[] head_photo2) {
        this.head_photo = head_photo2;
    }

    public String getCountry() {
        return this.country;
    }

    public void setCountry(String country2) {
        this.country = country2;
    }

    public String getCn_name() {
        return this.cn_name;
    }

    public void setCn_name(String cn_name2) {
        this.cn_name = cn_name2;
    }

    public String getIdcard_version() {
        return this.idcard_version;
    }

    public void setIdcard_version(String idcard_version2) {
        this.idcard_version = idcard_version2;
    }

    public String getCard_type() {
        return this.card_type;
    }

    public void setCard_type(String card_type2) {
        this.card_type = card_type2;
    }

    public String getReserve() {
        return this.reserve;
    }

    public void setReserve(String reserve2) {
        this.reserve = reserve2;
    }

    public String getPassNum() {
        return this.passNum;
    }

    public void setPassNum(String passNum2) {
        this.passNum = passNum2;
    }

    public String getIssuesNum() {
        return this.issuesNum;
    }

    public void setIssuesNum(String issuesNum2) {
        this.issuesNum = issuesNum2;
    }

    public String getCardSignal() {
        return this.cardSignal;
    }

    public void setCardSignal(String cardSignal2) {
        this.cardSignal = cardSignal2;
    }

    public static long getSerialversionuid() {
        return serialVersionUID;
    }
}
