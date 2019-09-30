package com.appzonegroup.app.fasttrack.model;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;

/**
 * Created by Joseph on 3/13/2018.
 */

public class Bank {

    private String Name;
    private String ShortName;
    private String BankCode;


    public static ArrayList<Bank> getBanks(){

        String jsonString = "[\n" +
                " {\n" +
                "   \"Name\": \"Access Bank Plc\",\n" +
                "   \"ShortName\": \"Access Bank\",\n" +
                "   \"BankCode\": \"000014\"\n" +
                " },\n" +
                " {\n" +
                "   \"Name\": \"Diamond Bank Plc\",\n" +
                "   \"ShortName\": \"Diamond Bank\",\n" +
                "   \"BankCode\": \"000005\"\n" +
                " },\n" +
                " {\n" +
                "   \"Name\": \"EcoBank Nigeria Plc\",\n" +
                "   \"ShortName\": \"Eco Bank\",\n" +
                "   \"BankCode\": \"000010\"\n" +
                " },\n" +
                " {\n" +
                "   \"Name\": \"Fidelity Bank Plc\",\n" +
                "   \"ShortName\": \"Fidelity Bank\",\n" +
                "   \"BankCode\": \"000007\"\n" +
                " },\n" +
                " {\n" +
                "   \"Name\": \"First Bank\",\n" +
                "   \"ShortName\": \"First Bank\",\n" +
                "   \"BankCode\": \"000016\"\n" +
                " },\n" +
                " {\n" +
                "   \"Name\": \"First City Monument Bank Plc\",\n" +
                "   \"ShortName\": \"FCMB\",\n" +
                "   \"BankCode\": \"000003\"\n" +
                " },\n" +
                " {\n" +
                "   \"Name\": \"Guaranty Trust Bank\",\n" +
                "   \"ShortName\": \"GT Bank\",\n" +
                "   \"BankCode\": \"000013\"\n" +
                " },\n" +
                " {\n" +
                "   \"Name\": \"Heritage Bank\",\n" +
                "   \"ShortName\": \"Heritage Bank\",\n" +
                "   \"BankCode\": \"000020\"\n" +
                " },\n" +
                " {\n" +
                "   \"Name\": \"Keystone Bank\",\n" +
                "   \"ShortName\": \"Keystone Bank\",\n" +
                "   \"BankCode\": \"000002\"\n" +
                " },\n" +
                " {\n" +
                "   \"Name\": \"Skye Bank\",\n" +
                "   \"ShortName\": \"Skye Bank\",\n" +
                "   \"BankCode\": \"000008\"\n" +
                " },\n" +
                " {\n" +
                "   \"Name\": \"Stanbic IBTC Bank\",\n" +
                "   \"ShortName\": \"Stanbic Bank\",\n" +
                "   \"BankCode\": \"000012\"\n" +
                " },\n" +
                " {\n" +
                "   \"Name\": \"Standard Chartered Bank Nigeria Limited\",\n" +
                "   \"ShortName\": \"Standard Chartered Bank\",\n" +
                "   \"BankCode\": \"000021\"\n" +
                " },\n" +
                " {\n" +
                "   \"Name\": \"Sterling Bank Plc\",\n" +
                "   \"ShortName\": \"Sterling Bank\",\n" +
                "   \"BankCode\": \"000001\"\n" +
                " },\n" +
                " {\n" +
                "   \"Name\": \"Union Bank Of Nigeria\",\n" +
                "   \"ShortName\": \"Union Bank\",\n" +
                "   \"BankCode\": \"000018\"\n" +
                " },\n" +
                " {\n" +
                "   \"Name\": \"United Bank For Africa Plc\",\n" +
                "   \"ShortName\": \"UBA\",\n" +
                "   \"BankCode\": \"000004\"\n" +
                " },\n" +
                " {\n" +
                "   \"Name\": \"Unity Bank Plc\",\n" +
                "   \"ShortName\": \"Unity Bank\",\n" +
                "   \"BankCode\": \"000011\"\n" +
                " },\n" +
                " {\n" +
                "   \"Name\": \"Wema Bank\",\n" +
                "   \"ShortName\": \"Wema Bank\",\n" +
                "   \"BankCode\": \"000017\"\n" +
                " },\n" +
                " {\n" +
                "   \"Name\": \"Zenith Bank Plc\",\n" +
                "   \"ShortName\": \"Zenith Bank\",\n" +
                "   \"BankCode\": \"000015\"\n" +
                " }\n" +
                "]";

        TypeToken<ArrayList<Bank>> typeToken = new TypeToken<ArrayList<Bank>>(){};
        return new Gson().fromJson(jsonString, typeToken.getType());

    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getShortName() {
        return ShortName;
    }

    public void setShortName(String shortName) {
        ShortName = shortName;
    }

    public String getBankCode() {
        return BankCode;
    }

    public void setBankCode(String bankCode) {
        BankCode = bankCode;
    }
}
