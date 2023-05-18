package com.creditclub.pos.providers.newland.util;


import android.util.Log;

import com.newland.nsdk.core.api.common.exception.NSDKException;
import com.newland.sdk.emvl3.api.common.EmvL3Const.CardInterface;
import com.newland.sdk.emvl3.api.common.configuration.AIDEntry;
import com.newland.sdk.emvl3.api.common.configuration.CAPKEntry;
import com.newland.sdk.emvl3.api.common.util.BytesUtils;
import com.newland.sdk.emvl3.api.internal.configuration.AID;
import com.newland.sdk.emvl3.api.internal.configuration.CAPK;
import com.newland.sdk.emvl3.internal.configuration.AidImpl;
import com.newland.sdk.emvl3.internal.configuration.CapkImpl;

import java.util.ArrayList;
import java.util.Locale;

/**
 * This class provides methods to interact with external and internal EMVL3.
 * Includes downloading aid and capk, detecting card,
 * performing an emv process and so on.
 *
 * @author hlh
 */
public class EmvL3Configuration {

    // EMV L3 Configuration objects.
    private AID aidContact;
    private AID aidContactLess;
    private CAPK capkLoader;



    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }

    public static String byteToHex(byte num) {
        char[] hexDigits = new char[2];
        hexDigits[0] = Character.forDigit((num >> 4) & 0xF, 16);
        hexDigits[1] = Character.forDigit((num & 0xF), 16);
        return new String(hexDigits);
    }

    public static String ByteArrayToHexString(byte[] byteArray) {
        StringBuffer hexStringBuffer = new StringBuffer();
        for (int i = 0; i < byteArray.length; i++) {
            hexStringBuffer.append(byteToHex(byteArray[i]));
        }
        return hexStringBuffer.toString();
    }

    public EmvL3Configuration(){
        aidContact = new AidImpl(CardInterface.CONTACT);
        aidContactLess = new AidImpl(CardInterface.CONTACTLESS);
        capkLoader = new CapkImpl();

    }

    public void LoadL3EmvConfiguration() throws NSDKException {
        LoadTerminalConfig();
        LoadAidConfig();
        // Load  CAPK key
        LoadCAPKConfig();

    }



    private void LoadTerminalConfig() throws NSDKException {
        int retval;
        Log.d(getClass().getName(), "Load L3 terminal config data");
        byte[] termCfgCt = new byte[1024];
        byte[] termCfgCl = new byte[1024];

        termCfgCt = BytesUtils.hexToBytes(
                ("9F061000000000000000000000000000000000" +
                        "DF2407F4C0F0E8EF0E60" +
                        "5F2A020156" + "DF220400000000" +
                        "9F7A0101" + "9F350122" + "DF010101" + "9F3303E0F8C8" +
                        "9F4005FF80F0A001" + "9F0106123456789000" + "9F15021234" +
                        "9F160F313233343536373839303132333435" + "9F3C020826" + "5F360102" +
                        "9F3D0100" + "9F1A020156" + "9F1E083030303030303031" + "9F1C083132333435363738" +
                        "9F7B06000000050000" + "DF160100" + "DF170100" + "DF1504000001F4" + "9F1B0400000000" +
                        "DF44039F3704" + "DF450F9F02065F2A029A039C0195059F3704" + "9F09020002").toUpperCase(Locale.ROOT));

        termCfgCl = BytesUtils.hexToBytes(
                ("9F061000000000000000000000000000000000" +
                        "DF2407F4C0F0E8EF0E62" + "9F350122" + "9F3303E0F8C8" + "9F4005FF80F0A001" +
                        "9F0106123456789000" + "9F15021234" + "9F160F313233343536373839303132333435" +
                        "5F360102" + "9F3C020826" + "9F3D0102" + "9F1A020156" + "9F1E083030303030303031" +
                        "DF27011F" + "DF2006000099999999" + "DF1906000000020000" + "DF2106000000500000" +
                        "DF3A0101" + "DF390100" + "DF150400000000" + "9F09020002" +
                        "DF440B9f37049f47018f019f3201" + "DF45039F0802" + "DF010101" +
                        "5F2A020156" + "9F1B0400000000" + "1F81020100").toUpperCase(Locale.ROOT));

        retval = aidContact.loadTerminalConfig(termCfgCt);
        if (retval < 0) {
            Log.d(getClass().getName(), String.format("aidContact.loadTerminalConfig() returned %d", retval));
        }
        // Now load Contactless default configuration
        retval = aidContactLess.loadTerminalConfig(termCfgCl);
        if (retval < 0) {
            Log.d(getClass().getName(), String.format("aidContactLess.loadTerminalConfig() returned %d", retval));
        }
    }

    private void GetTerminalConfig() throws NSDKException {
        byte[] ctconfig = new byte[2000];
        byte[] clssconfig = new byte[2000];

        ctconfig = aidContact.getTerminalConfig();
        if (ctconfig == null) {
            Log.d(getClass().getName(), "aidContact.getTerminalConfig() fasle");
        }
        Log.d(getClass().getName(), ByteArrayToHexString(ctconfig));

        // Now load Contactless default configuration
        clssconfig = aidContactLess.getTerminalConfig();
        if (clssconfig == null) {
            Log.d(getClass().getName(), "aidContactLess.getTerminalConfig() false");
        }
        Log.d(getClass().getName(), ByteArrayToHexString(clssconfig));
    }

    private void LoadAidConfig() throws NSDKException {
        LoadVisaAidConfig();
        LoadPbocAidConfig();
        LoadMasterCardAidConfig();
        LoadAmexAidConfig();
        LoadDiscoverAidConfig();
        LoadJcbAidConfig();
        LoadPureAidConfig();
        LoadRupayAidConfig();
        LoadInteracAidConfig();

    }

    private void GetAidConfig() throws NSDKException {

        Log.d(getClass().getName(), "Get L3 AID config data");
        byte[] ctconfig = new byte[2000];
        byte[] clssconfig = new byte[2000];

        byte[] VisaAidCfgCt = new byte[1024];
        byte[] VisaAidCfgCl = new byte[1024];

        AIDEntry entryct = new AIDEntry();
        AIDEntry entrycl = new AIDEntry();

        //contact param set
        entryct.setAid(hexStringToByteArray("A0000000031010"));
        entryct.setAidLen(7);

        //contactless param set
        entrycl.setAid(hexStringToByteArray("A0000000031010"));
        entrycl.setAidLen(7);
        entrycl.setKernelId(hexStringToByteArray("0300000000000000"));

        // Get Visa AID Contact configuration
        ctconfig = aidContact.getAID(entryct);
        if (ctconfig == null) {
            Log.d(getClass().getName(), "aidContact.getAID(entryct) false");
        }
        assert ctconfig != null;
        Log.d(getClass().getName(), ByteArrayToHexString(ctconfig));
        // Get Visa AID Contactless configuration
        clssconfig = aidContactLess.getAID(entrycl);
        if (clssconfig == null) {
            Log.d(getClass().getName(), "aidContactLess.getAID(entrycl) false");
        }
        assert clssconfig != null;
        Log.d(getClass().getName(), ByteArrayToHexString(clssconfig));
    }

    private void DeleteOneAidConfig() throws NSDKException {

        Log.d(getClass().getName(), "Remove one L3 AID config data");
        boolean ctconfig;
        boolean clssconfig;

        byte[] VisaAidCfgCt = new byte[1024];
        byte[] VisaAidCfgCl = new byte[1024];

        AIDEntry entryct = new AIDEntry();
        AIDEntry entrycl = new AIDEntry();

        //contact param set
        entryct.setAid(hexStringToByteArray("A0000000031010"));
        entryct.setAidLen(7);

        //contactless param set
        entrycl.setAid(hexStringToByteArray("A0000000031010"));
        entrycl.setAidLen(7);
        entrycl.setKernelId(hexStringToByteArray("0300000000000000"));

        // remove one Visa AID Contact configuration
        ctconfig = aidContact.remove(entryct);
        if (ctconfig == false) {
            Log.d(getClass().getName(), "aidContact.remove(entryct) false");
        }

        // remove one Visa AID Contactless configuration
        clssconfig = aidContactLess.remove(entrycl);
        if (clssconfig == false) {
            Log.d(getClass().getName(), "aidContactLess.remove(entrycl) false");
        }
    }

    private void DeleteAllAidConfig() throws NSDKException {

        Log.d(getClass().getName(), "Remove ALL L3 AID config data");
        boolean ctconfig;
        boolean clssconfig;

        // Remove all AID Contact configuration
        ctconfig = aidContact.flush();
        if (ctconfig == false) {
            Log.d(getClass().getName(), "aidContact.flush() false");
        }

        // Remove all AID Contactless configuration
        clssconfig = aidContactLess.flush();
        if (clssconfig == false) {
            Log.d(getClass().getName(), "aidContactLess.flush() false");
        }
    }

    //get all AID count,Not included terminal config
    private void GetAidCount() throws NSDKException {

        Log.d(getClass().getName(), "Get AID count");
        int ctaidcount;
        int clssaidcount;

        // Get CT Aid count
        ctaidcount = aidContact.getAidCount();

        Log.d(getClass().getName(), String.format("aidContactLess.getAidCount() ctaidcount %d", ctaidcount));

        // Get  Contactless Aid count
        clssaidcount = aidContactLess.getAidCount();

        Log.d(getClass().getName(), String.format("aidContactLess.getAidCount() clssaidcount %d", clssaidcount));
    }

    //Enumerate all of the aid configuration, include terminal configuration.
    private void GetAidList() throws NSDKException {

        Log.d(getClass().getName(), "Enumerate aid configuration");

        // Get Visa AID Contact configuration
        ArrayList<AIDEntry> aidContactEntries = aidContact.getAIDList();
        for (int i = 0; i < aidContactEntries.size(); i++){
            AIDEntry temp = aidContactEntries.get(i);
            Log.d(getClass().getName(), ByteArrayToHexString(temp.getAid()));
        }

        // Get Visa AID Contactless configuration
        ArrayList<AIDEntry> aidContactlessEntries = aidContactLess.getAIDList();
        for (int i = 0; i < aidContactlessEntries.size(); i++){
            AIDEntry temp = aidContactlessEntries.get(i);
            Log.d(getClass().getName(), ByteArrayToHexString(temp.getAid()));
        }
    }

    private void LoadCAPKConfig() throws NSDKException {
        // visa capk
        LoadVisa08();
        LoadVisa09();
        LoadVisa52();
        LoadVisa57();
        LoadVisa58();
        LoadVisa90();
        LoadVisa92();
        LoadVisa94();
        LoadVisa95();
        LoadVisa96();
        LoadVisa97();
        LoadVisa98();
        LoadVisa99();

        //Mastercard capk
        LoadMasterCard05();
        LoadMasterCard06();
        LoadMasterCardEF();
        LoadMasterCardF0();
        LoadMasterCardF1();
        LoadMasterCardF2();
        LoadMasterCardF3();
        LoadMasterCardF4();
        LoadMasterCardF8();
        LoadMasterCardFA();
        LoadMasterCardFE();

        //EuroCheque CAPK
        LoadEuroChequeF8();
        LoadEuroChequeFE();

        //JCB CAPK
        LoadJcb08();
        LoadJcb0F();
        LoadJcb11();
        LoadJcb12();
        LoadJcb13();
        LoadJcb14();
        LoadJcb10();

        //Pboc capk
        LoadPboc02();
        LoadPboc03();
        LoadPboc04();
        LoadPboc08();
        LoadPboc09();
        LoadPboc10();
        LoadPbocF8();

        //Amex capk
        LoadAmex0F();
        LoadAmex10();
        LoadAmex62();
        LoadAmex64();
        LoadAmex65();
        LoadAmex66();
        LoadAmex67();
        LoadAmex68();
        LoadAmex04();
        LoadAmexC8();
        LoadAmexC9();
        LoadAmexCA();

        //Rupay capk
        LoadRupay5A();
        LoadDinner5C();

        //InteracCAPK
        LoadInterac09();
        LoadInterac40();

    }

    private void GetCAPKConfig() throws NSDKException {
        CAPKEntry capkentry;
        CAPKEntry capk = new CAPKEntry();
        Log.d(getClass().getName(), "Get L3 CAPK keys");

        // get one key setted, just to test the API
        byte[] visa_rid = new byte[5];

        visa_rid = hexStringToByteArray("A000000003");

        capkentry = capkLoader.get(visa_rid, 0x90);

        if (capkentry == null) {
            Log.d(getClass().getName(), "capkLoader.get returned %d");
            return;
        }

        Log.d(getClass().getName(), ByteArrayToHexString(capkentry.getModulus()));

    }

    private void RemoveOneCAPKConfig() throws NSDKException {

        Log.d(getClass().getName(), "Remove ONE L3 CAPK keys");
        boolean isremovesucc;
        // remove one key setted, just to test the API
        byte[] visa_rid = new byte[5];

        visa_rid = hexStringToByteArray("A000000003");

        isremovesucc = capkLoader.remove(visa_rid, 0x90);

        if (isremovesucc == false) {
            Log.d(getClass().getName(), "capkLoader.get returned %d");
        }


    }

    private void RemoveAllCAPKConfig() throws NSDKException {

        Log.d(getClass().getName(), "Remove ALL L3 CAPK keys");
        boolean isremovesucc;
        // remove all key setted, just to test the API

        isremovesucc = capkLoader.flush();

        if (isremovesucc == false) {
            Log.d(getClass().getName(), "capkLoader.get returned %d");
        }

    }

    //get all CAPK count
    private void GetCapkCount() throws NSDKException {

        Log.d(getClass().getName(), "Get All CAPK Count");
        int capkcount;

        // Get Capk count
        capkcount = capkLoader.getCapkCount();

        Log.d(getClass().getName(), String.format("aidContactLess.getAidCount() ctaidcount %d", capkcount));



    }
    //Get the whole capk configuration(rid + index)
    private void GetCapkList() throws NSDKException {

        Log.d(getClass().getName(), "Get The Whole Capk Configuration(Rid + Index)");

        // Get capk configuration
        ArrayList<CAPKEntry> capkEntries = capkLoader.getCAPKList();

        for (int i = 0; i < capkEntries.size(); i++) {
            CAPKEntry temp = capkEntries.get(i);
            Log.d(getClass().getName(), String.format("capkLoader.getCAPKList() getIndex = %d", temp.getIndex()));
        }
    }



    //lOAD CAPK
    private void LoadVisa90(){
        int retval;
        CAPKEntry capk = new CAPKEntry();
        Log.d(getClass().getName(), "Load L3 CAPK keys");
        // Setup one key, just to test the API
        capk.setAlgorithmIndicator((byte)1);
        capk.setHashAlgorithm((byte)1);
        byte[] rid = new byte[5];
        byte[] key_modulus = new byte[248];
        byte[] key_hash = new byte[20];
        rid = hexStringToByteArray("A000000003");
        key_hash = hexStringToByteArray("B4BC56CC4E88324932CBC643D6898F6FE593B172");
        key_modulus = hexStringToByteArray("C26B3CB3833E42D8270DC10C8999B2DA18106838650DA0DBF154EFD51100AD144741B2A87D6881F8630E3348DEA3F78038E9B21A697EB2A6716D32CBF26086F1");
        capk.setRID(rid);
        capk.setHash(key_hash);
        capk.setIndex(0x90);
        capk.setExponent(new byte[]{0x00,0x00,0x03});
        capk.setModulus(key_modulus);
        capk.setModuleLen(64);
        retval = capkLoader.load(capk);
        if (retval < 0) {
            Log.d(getClass().getName(), String.format("capkLoader.load returned %d", retval));
        }

    }

    private void LoadVisa92(){
        int retval;
        CAPKEntry capk = new CAPKEntry();
        Log.d(getClass().getName(), "Load L3 CAPK keys");
        // Setup one key, just to test the API
        capk.setAlgorithmIndicator((byte)1);
        capk.setHashAlgorithm((byte)1);
        byte[] rid = new byte[5];
        byte[] key_modulus = new byte[248];
        byte[] key_hash = new byte[20];
        rid = hexStringToByteArray("A000000003");
        key_hash = hexStringToByteArray("429C954A3859CEF91295F663C963E582ED6EB253");
        key_modulus = hexStringToByteArray("996AF56F569187D09293C14810450ED8EE3357397B18A2458EFAA92DA3B6DF6514EC060195318FD43BE9B8F0CC669E3F844057CBDDF8BDA191BB64473BC8DC9A730DB8F6B4EDE3924186FFD9B8C7735789C23A36BA0B8AF65372EB57EA5D89E7D14E9C7B6B557460F10885DA16AC923F15AF3758F0F03EBD3C5C2C949CBA306DB44E6A2C076C5F67E281D7EF56785DC4D75945E491F01918800A9E2DC66F60080566CE0DAF8D17EAD46AD8E30A247C9F");
        capk.setRID(rid);
        capk.setHash(key_hash);
        capk.setIndex(0x92);
        capk.setExponent(new byte[]{0x00,0x00,0x03});
        capk.setModulus(key_modulus);
        capk.setModuleLen(key_modulus.length);
        retval = capkLoader.load(capk);
        if (retval < 0) {
            Log.d(getClass().getName(), String.format("capkLoader.load returned %d", retval));
        }

    }

    private void LoadVisa94(){
        int retval;
        CAPKEntry capk = new CAPKEntry();
        Log.d(getClass().getName(), "Load L3 CAPK keys");
        // Setup one key, just to test the API
        capk.setAlgorithmIndicator((byte)1);
        capk.setHashAlgorithm((byte)1);
        byte[] rid = new byte[5];
        byte[] key_modulus = new byte[248];
        byte[] key_hash = new byte[20];
        rid = hexStringToByteArray("A000000003");
        key_hash = hexStringToByteArray("C4A3C43CCF87327D136B804160E47D43B60E6E0F");
        key_modulus = hexStringToByteArray("ACD2B12302EE644F3F835ABD1FC7A6F62CCE48FFEC622AA8EF062BEF6FB8BA8BC68BBF6AB5870EED579BC3973E121303D34841A796D6DCBC41DBF9E52C4609795C0CCF7EE86FA1D5CB041071ED2C51D2202F63F1156C58A92D38BC60BDF424E1776E2BC9648078A03B36FB554375FC53D57C73F5160EA59F3AFC5398EC7B67758D65C9BFF7828B6B82D4BE124A416AB7301914311EA462C19F771F31B3B57336000DFF732D3B83DE07052D730354D297BEC728DCCF0E193F171ABA27EE464C6A97690943D59BDABB2A27EB71CEEBDAFA1176046478FD62FEC452D5CA393296530AA3F41927ADFE434A2DF2AE3054F8840657A26E0FC617");
        capk.setRID(rid);
        capk.setHash(key_hash);
        capk.setIndex(0x94);
        capk.setExponent(new byte[]{0x00,0x00,0x03});
        capk.setModulus(key_modulus);
        capk.setModuleLen(key_modulus.length);
        retval = capkLoader.load(capk);
        if (retval < 0) {
            Log.d(getClass().getName(), String.format("capkLoader.load returned %d", retval));
        }

    }

    private void LoadVisa95(){
        int retval;
        CAPKEntry capk = new CAPKEntry();
        Log.d(getClass().getName(), "Load L3 CAPK keys");
        // Setup one key, just to test the API
        capk.setAlgorithmIndicator((byte)1);
        capk.setHashAlgorithm((byte)1);
        byte[] rid = new byte[5];
        byte[] key_modulus = new byte[248];
        byte[] key_hash = new byte[20];
        rid = hexStringToByteArray("A000000003");
        key_hash = hexStringToByteArray("EE1511CEC71020A9B90443B37B1D5F6E703030F6");
        key_modulus = hexStringToByteArray("BE9E1FA5E9A803852999C4AB432DB28600DCD9DAB76DFAAA47355A0FE37B1508AC6BF38860D3C6C2E5B12A3CAAF2A7005A7241EBAA7771112C74CF9A0634652FBCA0E5980C54A64761EA101A114E0F0B5572ADD57D010B7C9C887E104CA4EE1272DA66D997B9A90B5A6D624AB6C57E73C8F919000EB5F684898EF8C3DBEFB330C62660BED88EA78E909AFF05F6DA627B");
        capk.setRID(rid);
        capk.setHash(key_hash);
        capk.setIndex(0x95);
        capk.setExponent(new byte[]{0x00,0x00,0x03});
        capk.setModulus(key_modulus);
        capk.setModuleLen(key_modulus.length);
        retval = capkLoader.load(capk);
        if (retval < 0) {
            Log.d(getClass().getName(), String.format("capkLoader.load returned %d", retval));
        }

    }

    private void LoadVisa96(){
        int retval;
        CAPKEntry capk = new CAPKEntry();
        Log.d(getClass().getName(), "Load L3 CAPK keys");
        // Setup one key, just to test the API
        capk.setAlgorithmIndicator((byte)1);
        capk.setHashAlgorithm((byte)1);
        byte[] rid = new byte[5];
        byte[] key_modulus = new byte[248];
        byte[] key_hash = new byte[20];
        rid = hexStringToByteArray("A000000003");
        key_hash = hexStringToByteArray("7616E9AC8BE014AF88CA11A8FB17967B7394030E");
        key_modulus = hexStringToByteArray("B74586D19A207BE6627C5B0AAFBC44A2ECF5A2942D3A26CE19C4FFAEEE920521868922E893E7838225A3947A2614796FB2C0628CE8C11E3825A56D3B1BBAEF783A5C6A81F36F8625395126FA983C5216D3166D48ACDE8A431212FF763A7F79D9EDB7FED76B485DE45BEB829A3D4730848A366D3324C3027032FF8D16A1E44D8D");
        capk.setRID(rid);
        capk.setHash(key_hash);
        capk.setIndex(0x96);
        capk.setExponent(new byte[]{0x00,0x00,0x03});
        capk.setModulus(key_modulus);
        capk.setModuleLen(key_modulus.length);
        retval = capkLoader.load(capk);
        if (retval < 0) {
            Log.d(getClass().getName(), String.format("capkLoader.load returned %d", retval));
        }

    }

    private void LoadVisa97(){
        int retval;
        CAPKEntry capk = new CAPKEntry();
        Log.d(getClass().getName(), "Load L3 CAPK keys");
        // Setup one key, just to test the API
        capk.setAlgorithmIndicator((byte)1);
        capk.setHashAlgorithm((byte)1);
        byte[] rid = new byte[5];
        byte[] key_modulus = new byte[248];
        byte[] key_hash = new byte[20];
        rid = hexStringToByteArray("A000000003");
        key_hash = hexStringToByteArray("8001CA76C1203955E2C62841CD6F201087E564BF");
        key_modulus = hexStringToByteArray("AF0754EAED977043AB6F41D6312AB1E22A6809175BEB28E70D5F99B2DF18CAE73519341BBBD327D0B8BE9D4D0E15F07D36EA3E3A05C892F5B19A3E9D3413B0D97E7AD10A5F5DE8E38860C0AD004B1E06F4040C295ACB457A788551B6127C0B29");
        capk.setRID(rid);
        capk.setHash(key_hash);
        capk.setIndex(0x97);
        capk.setExponent(new byte[]{0x00,0x00,0x03});
        capk.setModulus(key_modulus);
        capk.setModuleLen(key_modulus.length);
        retval = capkLoader.load(capk);
        if (retval < 0) {
            Log.d(getClass().getName(), String.format("capkLoader.load returned %d", retval));
        }

    }

    private void LoadVisa98(){
        int retval;
        CAPKEntry capk = new CAPKEntry();
        Log.d(getClass().getName(), "Load L3 CAPK keys");
        // Setup one key, just to test the API
        capk.setAlgorithmIndicator((byte)1);
        capk.setHashAlgorithm((byte)1);
        byte[] rid = new byte[5];
        byte[] key_modulus = new byte[248];
        byte[] key_hash = new byte[20];
        rid = hexStringToByteArray("A000000003");
        key_hash = hexStringToByteArray("E7AC9AA8EED1B5FF1BD532CF1489A3E5557572C1");
        key_modulus = hexStringToByteArray("CA026E52A695E72BD30AF928196EEDC9FAF4A619F2492E3FB31169789C276FFBB7D43116647BA9E0D106A3542E3965292CF77823DD34CA8EEC7DE367E08070895077C7EFAD939924CB187067DBF92CB1E785917BD38BACE0C194CA12DF0CE5B7A50275AC61BE7C3B436887CA98C9FD39");
        capk.setRID(rid);
        capk.setHash(key_hash);
        capk.setIndex(0x98);
        capk.setExponent(new byte[]{0x00,0x00,0x03});
        capk.setModulus(key_modulus);
        capk.setModuleLen(key_modulus.length);
        retval = capkLoader.load(capk);
        if (retval < 0) {
            Log.d(getClass().getName(), String.format("capkLoader.load returned %d", retval));
        }

    }

    private void LoadVisa99(){
        int retval;
        CAPKEntry capk = new CAPKEntry();
        Log.d(getClass().getName(), "Load L3 CAPK keys");
        // Setup one key, just to test the API
        capk.setAlgorithmIndicator((byte)1);
        capk.setHashAlgorithm((byte)1);
        byte[] rid = new byte[5];
        byte[] key_modulus = new byte[248];
        byte[] key_hash = new byte[20];
        rid = hexStringToByteArray("A000000003");
        key_hash = hexStringToByteArray("4ABFFD6B1C51212D05552E431C5B17007D2F5E6D");
        key_modulus = hexStringToByteArray("AB79FCC9520896967E776E64444E5DCDD6E13611874F3985722520425295EEA4BD0C2781DE7F31CD3D041F565F747306EED62954B17EDABA3A6C5B85A1DE1BEB9A34141AF38FCF8279C9DEA0D5A6710D08DB4124F041945587E20359BAB47B7575AD94262D4B25F264AF33DEDCF28E09615E937DE32EDC03C54445FE7E382777");
        capk.setRID(rid);
        capk.setHash(key_hash);
        capk.setIndex(0x99);
        capk.setExponent(new byte[]{0x00,0x00,0x03});
        capk.setModulus(key_modulus);
        capk.setModuleLen(key_modulus.length);
        retval = capkLoader.load(capk);
        if (retval < 0) {
            Log.d(getClass().getName(), String.format("capkLoader.load returned %d", retval));
        }

    }

    private void LoadVisa08(){
        int retval;
        CAPKEntry capk = new CAPKEntry();
        Log.d(getClass().getName(), "Load L3 CAPK keys");
        // Setup one key, just to test the API
        capk.setAlgorithmIndicator((byte)1);
        capk.setHashAlgorithm((byte)1);
        byte[] rid = new byte[5];
        byte[] key_modulus = new byte[248];
        byte[] key_hash = new byte[20];
        rid = hexStringToByteArray("A000000003");
        key_hash = hexStringToByteArray("20D213126955DE205ADC2FD2822BD22DE21CF9A8");
        key_modulus = hexStringToByteArray("D9FD6ED75D51D0E30664BD157023EAA1FFA871E4DA65672B863D255E81E137A51DE4F72BCC9E44ACE12127F87E263D3AF9DD9CF35CA4A7B01E907000BA85D24954C2FCA3074825DDD4C0C8F186CB020F683E02F2DEAD3969133F06F7845166ACEB57CA0FC2603445469811D293BFEFBAFAB57631B3DD91E796BF850A25012F1AE38F05AA5C4D6D03B1DC2E568612785938BBC9B3CD3A910C1DA55A5A9218ACE0F7A21287752682F15832A678D6E1ED0B");
        capk.setRID(rid);
        capk.setHash(key_hash);
        capk.setIndex(0x08);
        capk.setExponent(new byte[]{0x00,0x00,0x03});
        capk.setModulus(key_modulus);
        capk.setModuleLen(key_modulus.length);
        retval = capkLoader.load(capk);
        if (retval < 0) {
            Log.d(getClass().getName(), String.format("capkLoader.load returned %d", retval));
        }

    }

    private void LoadVisa09(){
        int retval;
        CAPKEntry capk = new CAPKEntry();
        Log.d(getClass().getName(), "Load L3 CAPK keys");
        // Setup one key, just to test the API
        capk.setAlgorithmIndicator((byte)1);
        capk.setHashAlgorithm((byte)1);
        byte[] rid = new byte[5];
        byte[] key_modulus = new byte[248];
        byte[] key_hash = new byte[20];
        rid = hexStringToByteArray("A000000003");
        key_hash = hexStringToByteArray("1FF80A40173F52D7D27E0F26A146A1C8CCB29046");
        key_modulus = hexStringToByteArray("9D912248DE0A4E39C1A7DDE3F6D2588992C1A4095AFBD1824D1BA74847F2BC4926D2EFD904B4B54954CD189A54C5D1179654F8F9B0D2AB5F0357EB642FEDA95D3912C6576945FAB897E7062CAA44A4AA06B8FE6E3DBA18AF6AE3738E30429EE9BE03427C9D64F695FA8CAB4BFE376853EA34AD1D76BFCAD15908C077FFE6DC5521ECEF5D278A96E26F57359FFAEDA19434B937F1AD999DC5C41EB11935B44C18100E857F431A4A5A6BB65114F174C2D7B59FDF237D6BB1DD0916E644D709DED56481477C75D95CDD68254615F7740EC07F330AC5D67BCD75BF23D28A140826C026DBDE971A37CD3EF9B8DF644AC385010501EFC6509D7A41");
        capk.setRID(rid);
        capk.setHash(key_hash);
        capk.setIndex(0x09);
        capk.setExponent(new byte[]{0x00,0x00,0x03});
        capk.setModulus(key_modulus);
        capk.setModuleLen(key_modulus.length);
        retval = capkLoader.load(capk);
        if (retval < 0) {
            Log.d(getClass().getName(), String.format("capkLoader.load returned %d", retval));
        }

    }

    private void LoadVisa52(){
        int retval;
        CAPKEntry capk = new CAPKEntry();
        Log.d(getClass().getName(), "Load L3 CAPK keys");
        // Setup one key, just to test the API
        capk.setAlgorithmIndicator((byte)1);
        capk.setHashAlgorithm((byte)1);
        byte[] rid = new byte[5];
        byte[] key_modulus = new byte[248];
        byte[] key_hash = new byte[20];
        rid = hexStringToByteArray("A000000003");
        key_hash = hexStringToByteArray("42D96E6E1217E5B59CC2079CE50C3D9F55B6FC1D");
        key_modulus = hexStringToByteArray("AFF740F8DBE763F333A1013A43722055C8E22F41779E219B0E1C409D60AFD45C8789C57EECD71EA4A269A675916CC1C5E1A05A35BD745A79F94555CE29612AC9338769665B87C3CA8E1AC4957F9F61FA7BFFE4E17631E937837CABF43DD6183D6360A228A3EBC73A1D1CDC72BF09953C81203AB7E492148E4CB774CDDFAAC3544D0DD4F8C8A0E9C70B877EA79F2C22E4CE52C69F3EF376F61B0F43A540FE96C63F586310C3B6E39C78C4D647CADB5933");
        capk.setRID(rid);
        capk.setHash(key_hash);
        capk.setIndex(0x52);
        capk.setExponent(new byte[]{0x00,0x00,0x03});
        capk.setModulus(key_modulus);
        capk.setModuleLen(key_modulus.length);
        retval = capkLoader.load(capk);
        if (retval < 0) {
            Log.d(getClass().getName(), String.format("capkLoader.load returned %d", retval));
        }

    }

    private void LoadVisa57(){
        int retval;
        CAPKEntry capk = new CAPKEntry();
        Log.d(getClass().getName(), "Load L3 CAPK keys");
        // Setup one key, just to test the API
        capk.setAlgorithmIndicator((byte)1);
        capk.setHashAlgorithm((byte)1);
        byte[] rid = new byte[5];
        byte[] key_modulus = new byte[248];
        byte[] key_hash = new byte[20];
        rid = hexStringToByteArray("A000000003");
        key_hash = hexStringToByteArray("251A5F5DE61CF28B5C6E2B5807C0644A01D46FF5");
        key_modulus = hexStringToByteArray("942B7F2BA5EA307312B63DF77C5243618ACC2002BD7ECB74D821FE7BDC78BF28F49F74190AD9B23B9713B140FFEC1FB429D93F56BDC7ADE4AC075D75532C1E590B21874C7952F29B8C0F0C1CE3AEEDC8DA25343123E71DCF86C6998E15F756E3");
        capk.setRID(rid);
        capk.setHash(key_hash);
        capk.setIndex(0x57);
        capk.setExponent(new byte[]{0x01,0x00,0x01});
        capk.setModulus(key_modulus);
        capk.setModuleLen(key_modulus.length);
        retval = capkLoader.load(capk);
        if (retval < 0) {
            Log.d(getClass().getName(), String.format("capkLoader.load returned %d", retval));
        }

    }

    private void LoadVisa58(){
        int retval;
        CAPKEntry capk = new CAPKEntry();
        Log.d(getClass().getName(), "Load L3 CAPK keys");
        // Setup one key, just to test the API
        capk.setAlgorithmIndicator((byte)1);
        capk.setHashAlgorithm((byte)1);
        byte[] rid = new byte[5];
        byte[] key_modulus = new byte[248];
        byte[] key_hash = new byte[20];
        rid = hexStringToByteArray("A000000003");
        key_hash = hexStringToByteArray("753ED0AA23E4CD5ABD69EAE7904B684A34A57C22");
        key_modulus = hexStringToByteArray("99552C4A1ECD68A0260157FC4151B5992837445D3FC57365CA5692C87BE358CDCDF2C92FB6837522842A48EB11CDFFE2FD91770C7221E4AF6207C2DE4004C7DEE1B6276DC62D52A87D2CD01FBF2DC4065DB52824D2A2167A06D19E6A0F781071CDB2DD314CB94441D8DC0E936317B77BF06F5177F6C5ABA3A3BC6AA30209C97260B7A1AD3A192C9B8CD1D153570AFCC87C3CD681D13E997FE33B3963A0A1C79772ACF991033E1B8397AD0341500E48A24770BC4CBE19D2CCF419504FDBF0389BC2F2FDCD4D44E61F");
        capk.setRID(rid);
        capk.setHash(key_hash);
        capk.setIndex(0x58);
        capk.setExponent(new byte[]{0x00,0x00,0x03});
        capk.setModulus(key_modulus);
        capk.setModuleLen(key_modulus.length);
        retval = capkLoader.load(capk);
        if (retval < 0) {
            Log.d(getClass().getName(), String.format("capkLoader.load returned %d", retval));
        }

    }

    private void LoadMasterCard05(){
        int retval;
        CAPKEntry capk = new CAPKEntry();
        Log.d(getClass().getName(), "Load L3 CAPK keys");
        // Setup one key, just to test the API
        capk.setAlgorithmIndicator((byte)1);
        capk.setHashAlgorithm((byte)1);
        byte[] rid = new byte[5];
        byte[] key_modulus = new byte[248];
        byte[] key_hash = new byte[20];
        rid = hexStringToByteArray("A000000004");
        key_hash = hexStringToByteArray("EBFA0D5D06D8CE702DA3EAE890701D45E274C845");
        key_modulus = hexStringToByteArray("B8048ABC30C90D976336543E3FD7091C8FE4800DF820ED55E7E94813ED00555B573FECA3D84AF6131A651D66CFF4284FB13B635EDD0EE40176D8BF04B7FD1C7BACF9AC7327DFAA8AA72D10DB3B8E70B2DDD811CB4196525EA386ACC33C0D9D4575916469C4E4F53E8E1C912CC618CB22DDE7C3568E90022E6BBA770202E4522A2DD623D180E215BD1D1507FE3DC90CA310D27B3EFCCD8F83DE3052CAD1E48938C68D095AAC91B5F37E28BB49EC7ED597");
        capk.setRID(rid);
        capk.setHash(key_hash);
        capk.setIndex(0x05);
        capk.setExponent(new byte[]{0x00,0x00,0x03});
        capk.setModulus(key_modulus);
        capk.setModuleLen(key_modulus.length);
        retval = capkLoader.load(capk);
        if (retval < 0) {
            Log.d(getClass().getName(), String.format("capkLoader.load returned %d", retval));
        }

    }

    private void LoadMasterCard06(){
        int retval;
        CAPKEntry capk = new CAPKEntry();
        Log.d(getClass().getName(), "Load L3 CAPK keys");
        // Setup one key, just to test the API
        capk.setAlgorithmIndicator((byte)1);
        capk.setHashAlgorithm((byte)1);
        byte[] rid = new byte[5];
        byte[] key_modulus = new byte[248];
        byte[] key_hash = new byte[20];
        rid = hexStringToByteArray("A000000004");
        key_hash = hexStringToByteArray("F910A1504D5FFB793D94F3B500765E1ABCAD72D9");
        key_modulus = hexStringToByteArray("CB26FC830B43785B2BCE37C81ED334622F9622F4C89AAE641046B2353433883F307FB7C974162DA72F7A4EC75D9D657336865B8D3023D3D645667625C9A07A6B7A137CF0C64198AE38FC238006FB2603F41F4F3BB9DA1347270F2F5D8C606E420958C5F7D50A71DE30142F70DE468889B5E3A08695B938A50FC980393A9CBCE44AD2D64F630BB33AD3F5F5FD495D31F37818C1D94071342E07F1BEC2194F6035BA5DED3936500EB82DFDA6E8AFB655B1EF3D0D7EBF86B66DD9F29F6B1D324FE8B26CE38AB2013DD13F611E7A594D675C4432350EA244CC34F3873CBA06592987A1D7E852ADC22EF5A2EE28132031E48F74037E3B34AB747F");
        capk.setRID(rid);
        capk.setHash(key_hash);
        capk.setIndex(0x06);
        capk.setExponent(new byte[]{0x00,0x00,0x03});
        capk.setModulus(key_modulus);
        capk.setModuleLen(key_modulus.length);
        retval = capkLoader.load(capk);
        if (retval < 0) {
            Log.d(getClass().getName(), String.format("capkLoader.load returned %d", retval));
        }

    }

    private void LoadMasterCardEF(){
        int retval;
        CAPKEntry capk = new CAPKEntry();
        Log.d(getClass().getName(), "Load L3 CAPK keys");
        // Setup one key, just to test the API
        capk.setAlgorithmIndicator((byte)1);
        capk.setHashAlgorithm((byte)1);
        byte[] rid = new byte[5];
        byte[] key_modulus = new byte[248];
        byte[] key_hash = new byte[20];
        rid = hexStringToByteArray("A000000004");
        key_hash = hexStringToByteArray("21766EBB0EE122AFB65D7845B73DB46BAB65427A");
        key_modulus = hexStringToByteArray("A191CB87473F29349B5D60A88B3EAEE0973AA6F1A082F358D849FDDFF9C091F899EDA9792CAF09EF28F5D22404B88A2293EEBBC1949C43BEA4D60CFD879A1539544E09E0F09F60F065B2BF2A13ECC705F3D468B9D33AE77AD9D3F19CA40F23DCF5EB7C04DC8F69EBA565B1EBCB4686CD274785530FF6F6E9EE43AA43FDB02CE00DAEC15C7B8FD6A9B394BABA419D3F6DC85E16569BE8E76989688EFEA2DF22FF7D35C043338DEAA982A02B866DE5328519EBBCD6F03CDD686673847F84DB651AB86C28CF1462562C577B853564A290C8556D818531268D25CC98A4CC6A0BDFFFDA2DCCA3A94C998559E307FDDF915006D9A987B07DDAEB3B");
        capk.setRID(rid);
        capk.setHash(key_hash);
        capk.setIndex(0xEF);
        capk.setExponent(new byte[]{0x00,0x00,0x03});
        capk.setModulus(key_modulus);
        capk.setModuleLen(key_modulus.length);
        retval = capkLoader.load(capk);
        if (retval < 0) {
            Log.d(getClass().getName(), String.format("capkLoader.load returned %d", retval));
        }

    }

    private void LoadMasterCardF0(){
        int retval;
        CAPKEntry capk = new CAPKEntry();
        Log.d(getClass().getName(), "Load L3 CAPK keys");
        // Setup one key, just to test the API
        capk.setAlgorithmIndicator((byte)1);
        capk.setHashAlgorithm((byte)1);
        byte[] rid = new byte[5];
        byte[] key_modulus = new byte[248];
        byte[] key_hash = new byte[20];
        rid = hexStringToByteArray("A000000004");
        key_hash = hexStringToByteArray("B8EA49169B54F3B7FF0DF3A8B6388C82A1DBE730");
        key_modulus = hexStringToByteArray("999EA2D430D60614E100706C7DA213E1C77AD18C11BD70BC42CEBD80A3C94EC5E736D345EA7ADE2B9E0BC8816E567D39412EB728C2B2CCE73DEBC9FA25D4919BF5420C986083FBC0750895AFBA6B9DAA62B1B7D8439CF29E720D085D5D0962A9443B1F738E6560EF0EED7572815EA87A1B07570F119867DD6CC5D4DE06AA5373847D17A610ECF932FA2C94234E68AF84A9E0DAA18116B326016B70136F493482FEAE98E4AE682BF96C59279752248DEC915ED6F9BB73F9206155D961B50865E1CA6D47322FCE22DCF1957182B6E99CBB");
        capk.setRID(rid);
        capk.setHash(key_hash);
        capk.setIndex(0xF0);
        capk.setExponent(new byte[]{0x00,0x00,0x03});
        capk.setModulus(key_modulus);
        capk.setModuleLen(key_modulus.length);
        retval = capkLoader.load(capk);
        if (retval < 0) {
            Log.d(getClass().getName(), String.format("capkLoader.load returned %d", retval));
        }

    }

    private void LoadMasterCardF1(){
        int retval;
        CAPKEntry capk = new CAPKEntry();
        Log.d(getClass().getName(), "Load L3 CAPK keys");
        // Setup one key, just to test the API
        capk.setAlgorithmIndicator((byte)1);
        capk.setHashAlgorithm((byte)1);
        byte[] rid = new byte[5];
        byte[] key_modulus = new byte[248];
        byte[] key_hash = new byte[20];
        rid = hexStringToByteArray("A000000004");
        key_hash = hexStringToByteArray("D8E68DA167AB5A85D8C3D55ECB9B0517A1A5B4BB");
        key_modulus = hexStringToByteArray("A0DCF4BDE19C3546B4B6F0414D174DDE294AABBB828C5A834D73AAE27C99B0B053A90278007239B6459FF0BBCD7B4B9C6C50AC02CE91368DA1BD21AAEADBC65347337D89B68F5C99A09D05BE02DD1F8C5BA20E2F13FB2A27C41D3F85CAD5CF6668E75851EC66EDBF98851FD4E42C44C1D59F5984703B27D5B9F21B8FA0D93279FBBF69E090642909C9EA27F898959541AA6757F5F624104F6E1D3A9532F2A6E51515AEAD1B43B3D7835088A2FAFA7BE7");
        capk.setRID(rid);
        capk.setHash(key_hash);
        capk.setIndex(0xF1);
        capk.setExponent(new byte[]{0x00,0x00,0x03});
        capk.setModulus(key_modulus);
        capk.setModuleLen(key_modulus.length);
        retval = capkLoader.load(capk);
        if (retval < 0) {
            Log.d(getClass().getName(), String.format("capkLoader.load returned %d", retval));
        }

    }

    private void LoadMasterCardF2(){
        int retval;
        CAPKEntry capk = new CAPKEntry();
        Log.d(getClass().getName(), "Load L3 CAPK keys");
        // Setup one key, just to test the API
        capk.setAlgorithmIndicator((byte)1);
        capk.setHashAlgorithm((byte)1);
        byte[] rid = new byte[5];
        byte[] key_modulus = new byte[248];
        byte[] key_hash = new byte[20];
        rid = hexStringToByteArray("A000000004");
        key_hash = hexStringToByteArray("0F3BA8CB5777DC4AA96C30BFB1FC267A382A4847");
        key_modulus = hexStringToByteArray("A2B9FF84F87FA108FF9A8B2E93FD5A37CBFDA184F189CEB3763090319CABBDD822EC4011EDA36989E5D0680666C225FC3E83FF0996D23E0F94F9F65D0FC21C3929B08E2FCFB6F5826020CF965050B0381D9B47BD930B9346A7E192B6FFB71BF458585E844FE504741A04C3DEFB1DC84CDDDE3F6686D622AEE3216E45FB77E7E4E48F5F3D8F9D9582685FD099CBD62873");
        capk.setRID(rid);
        capk.setHash(key_hash);
        capk.setIndex(0xF2);
        capk.setExponent(new byte[]{0x00,0x00,0x03});
        capk.setModulus(key_modulus);
        capk.setModuleLen(key_modulus.length);
        retval = capkLoader.load(capk);
        if (retval < 0) {
            Log.d(getClass().getName(), String.format("capkLoader.load returned %d", retval));
        }

    }

    private void LoadMasterCardF3(){
        int retval;
        CAPKEntry capk = new CAPKEntry();
        Log.d(getClass().getName(), "Load L3 CAPK keys");
        // Setup one key, just to test the API
        capk.setAlgorithmIndicator((byte)1);
        capk.setHashAlgorithm((byte)1);
        byte[] rid = new byte[5];
        byte[] key_modulus = new byte[248];
        byte[] key_hash = new byte[20];
        rid = hexStringToByteArray("A000000004");
        key_hash = hexStringToByteArray("A69AC7603DAF566E972DEDC2CB433E07E8B01A9A");
        key_modulus = hexStringToByteArray("98F0C770F23864C2E766DF02D1E833DFF4FFE92D696E1642F0A88C5694C6479D16DB1537BFE29E4FDC6E6E8AFD1B0EB7EA0124723C333179BF19E93F10658B2F776E829E87DAEDA9C94A8B3382199A350C077977C97AFF08FD11310AC950A72C3CA5002EF513FCCC286E646E3C5387535D509514B3B326E1234F9CB48C36DDD44B416D23654034A66F403BA511C5EFA3");
        capk.setRID(rid);
        capk.setHash(key_hash);
        capk.setIndex(0xF3);
        capk.setExponent(new byte[]{0x00,0x00,0x03});
        capk.setModulus(key_modulus);
        capk.setModuleLen(key_modulus.length);
        retval = capkLoader.load(capk);
        if (retval < 0) {
            Log.d(getClass().getName(), String.format("capkLoader.load returned %d", retval));
        }

    }

    private void LoadMasterCardF4(){
        int retval;
        CAPKEntry capk = new CAPKEntry();
        Log.d(getClass().getName(), "Load L3 CAPK keys");
        // Setup one key, just to test the API
        capk.setAlgorithmIndicator((byte)1);
        capk.setHashAlgorithm((byte)1);
        byte[] rid = new byte[5];
        byte[] key_modulus = new byte[248];
        byte[] key_hash = new byte[20];
        rid = hexStringToByteArray("A000000004");
        key_hash = hexStringToByteArray("98CEB9E0E8ED52ABDD8549FD50ACECA3BF51A786");
        key_modulus = hexStringToByteArray("9CFAD54B40297C1CDE23FCB3EF68D318341A4727AE1DAA2BEBE35872EF3DC90746297B066ED1CE3C07C1F234FF5490425E8B14674CC57E4397A51584FF5EBA6B5D54D99D2C9FC99D5E4CACB3487ABA790F28E304987AFA7F5F92E22D89FF510C1B581941166C7CCB11EFB08DE607460D");
        capk.setRID(rid);
        capk.setHash(key_hash);
        capk.setIndex(0xF4);
        capk.setExponent(new byte[]{0x00,0x00,0x03});
        capk.setModulus(key_modulus);
        capk.setModuleLen(key_modulus.length);
        retval = capkLoader.load(capk);
        if (retval < 0) {
            Log.d(getClass().getName(), String.format("capkLoader.load returned %d", retval));
        }

    }

    private void LoadMasterCardF8(){
        int retval;
        CAPKEntry capk = new CAPKEntry();
        Log.d(getClass().getName(), "Load L3 CAPK keys");
        // Setup one key, just to test the API
        capk.setAlgorithmIndicator((byte)1);
        capk.setHashAlgorithm((byte)1);
        byte[] rid = new byte[5];
        byte[] key_modulus = new byte[248];
        byte[] key_hash = new byte[20];
        rid = hexStringToByteArray("A000000004");
        key_hash = hexStringToByteArray("F06ECC6D2AAEBF259B7E755A38D9A9B24E2FF3DD");
        key_modulus = hexStringToByteArray("A1F5E1C9BD8650BD43AB6EE56B891EF7459C0A24FA84F9127D1A6C79D4930F6DB1852E2510F18B61CD354DB83A356BD190B88AB8DF04284D02A4204A7B6CB7C5551977A9B36379CA3DE1A08E69F301C95CC1C20506959275F41723DD5D2925290579E5A95B0DF6323FC8E9273D6F849198C4996209166D9BFC973C361CC826E1");
        capk.setRID(rid);
        capk.setHash(key_hash);
        capk.setIndex(0xF8);
        capk.setExponent(new byte[]{0x00,0x00,0x03});
        capk.setModulus(key_modulus);
        capk.setModuleLen(key_modulus.length);
        retval = capkLoader.load(capk);
        if (retval < 0) {
            Log.d(getClass().getName(), String.format("capkLoader.load returned %d", retval));
        }

    }

    private void LoadMasterCardFA(){
        int retval;
        CAPKEntry capk = new CAPKEntry();
        Log.d(getClass().getName(), "Load L3 CAPK keys");
        // Setup one key, just to test the API
        capk.setAlgorithmIndicator((byte)1);
        capk.setHashAlgorithm((byte)1);
        byte[] rid = new byte[5];
        byte[] key_modulus = new byte[248];
        byte[] key_hash = new byte[20];
        rid = hexStringToByteArray("A000000004");
        key_hash = hexStringToByteArray("5BED4068D96EA16D2D77E03D6036FC7A160EA99C");
        key_modulus = hexStringToByteArray("A90FCD55AA2D5D9963E35ED0F440177699832F49C6BAB15CDAE5794BE93F934D4462D5D12762E48C38BA83D8445DEAA74195A301A102B2F114EADA0D180EE5E7A5C73E0C4E11F67A43DDAB5D55683B1474CC0627F44B8D3088A492FFAADAD4F42422D0E7013536C3C49AD3D0FAE96459B0F6B1B6056538A3D6D44640F94467B108867DEC40FAAECD740C00E2B7A8852D");
        capk.setRID(rid);
        capk.setHash(key_hash);
        capk.setIndex(0xFA);
        capk.setExponent(new byte[]{0x00,0x00,0x03});
        capk.setModulus(key_modulus);
        capk.setModuleLen(key_modulus.length);
        retval = capkLoader.load(capk);
        if (retval < 0) {
            Log.d(getClass().getName(), String.format("capkLoader.load returned %d", retval));
        }

    }

    private void LoadMasterCardFE(){
        int retval;
        CAPKEntry capk = new CAPKEntry();
        Log.d(getClass().getName(), "Load L3 CAPK keys");
        // Setup one key, just to test the API
        capk.setAlgorithmIndicator((byte)1);
        capk.setHashAlgorithm((byte)1);
        byte[] rid = new byte[5];
        byte[] key_modulus = new byte[248];
        byte[] key_hash = new byte[20];
        rid = hexStringToByteArray("A000000004");
        key_hash = hexStringToByteArray("9A295B05FB390EF7923F57618A9FDA2941FC34E0");
        key_modulus = hexStringToByteArray("A653EAC1C0F786C8724F737F172997D63D1C3251C44402049B865BAE877D0F398CBFBE8A6035E24AFA086BEFDE9351E54B95708EE672F0968BCD50DCE40F783322B2ABA04EF137EF18ABF03C7DBC5813AEAEF3AA7797BA15DF7D5BA1CBAF7FD520B5A482D8D3FEE105077871113E23A49AF3926554A70FE10ED728CF793B62A1");
        capk.setRID(rid);
        capk.setHash(key_hash);
        capk.setIndex(0xFE);
        capk.setExponent(new byte[]{0x00,0x00,0x03});
        capk.setModulus(key_modulus);
        capk.setModuleLen(key_modulus.length);
        retval = capkLoader.load(capk);
        if (retval < 0) {
            Log.d(getClass().getName(), String.format("capkLoader.load returned %d", retval));
        }

    }

    private void LoadEuroChequeF8(){
        int retval;
        CAPKEntry capk = new CAPKEntry();
        Log.d(getClass().getName(), "Load L3 CAPK keys");
        // Setup one key, just to test the API
        capk.setAlgorithmIndicator((byte)1);
        capk.setHashAlgorithm((byte)1);
        byte[] rid = new byte[5];
        byte[] key_modulus = new byte[248];
        byte[] key_hash = new byte[20];
        rid = hexStringToByteArray("A000000010");
        key_hash = hexStringToByteArray("0F4488568CF8AFB0C12EC653CF7A04D2D46DCD34");
        key_modulus = hexStringToByteArray("A1F5E1C9BD8650BD43AB6EE56B891EF7459C0A24FA84F9127D1A6C79D4930F6DB1852E2510F18B61CD354DB83A356BD190B88AB8DF04284D02A4204A7B6CB7C5551977A9B36379CA3DE1A08E69F301C95CC1C20506959275F41723DD5D2925290579E5A95B0DF6323FC8E9273D6F849198C4996209166D9BFC973C361CC826E1");
        capk.setRID(rid);
        capk.setHash(key_hash);
        capk.setIndex(0xF8);
        capk.setExponent(new byte[]{0x00,0x00,0x03});
        capk.setModulus(key_modulus);
        capk.setModuleLen(key_modulus.length);
        retval = capkLoader.load(capk);
        if (retval < 0) {
            Log.d(getClass().getName(), String.format("capkLoader.load returned %d", retval));
        }

    }

    private void LoadEuroChequeFE(){
        int retval;
        CAPKEntry capk = new CAPKEntry();
        Log.d(getClass().getName(), "Load L3 CAPK keys");
        // Setup one key, just to test the API
        capk.setAlgorithmIndicator((byte)1);
        capk.setHashAlgorithm((byte)1);
        byte[] rid = new byte[5];
        byte[] key_modulus = new byte[248];
        byte[] key_hash = new byte[20];
        rid = hexStringToByteArray("A000000010");
        key_hash = hexStringToByteArray("2CFBB82409ED86A31973B0E0CEEA381BC43C8097");
        key_modulus = hexStringToByteArray("A653EAC1C0F786C8724F737F172997D63D1C3251C44402049B865BAE877D0F398CBFBE8A6035E24AFA086BEFDE9351E54B95708EE672F0968BCD50DCE40F783322B2ABA04EF137EF18ABF03C7DBC5813AEAEF3AA7797BA15DF7D5BA1CBAF7FD520B5A482D8D3FEE105077871113E23A49AF3926554A70FE10ED728CF793B62A1");
        capk.setRID(rid);
        capk.setHash(key_hash);
        capk.setIndex(0xFE);
        capk.setExponent(new byte[]{0x00,0x00,0x03});
        capk.setModulus(key_modulus);
        capk.setModuleLen(key_modulus.length);
        retval = capkLoader.load(capk);
        if (retval < 0) {
            Log.d(getClass().getName(), String.format("capkLoader.load returned %d", retval));
        }

    }

    private void LoadJcb08(){
        int retval;
        CAPKEntry capk = new CAPKEntry();
        Log.d(getClass().getName(), "Load L3 CAPK keys");
        // Setup one key, just to test the API
        capk.setAlgorithmIndicator((byte)1);
        capk.setHashAlgorithm((byte)1);
        byte[] rid = new byte[5];
        byte[] key_modulus = new byte[248];
        byte[] key_hash = new byte[20];
        rid = hexStringToByteArray("A000000065");
        key_hash = hexStringToByteArray("DD36D5896228C8C4900742F107E2F91FE50BC7EE");
        key_modulus = hexStringToByteArray("B74670DAD1DC8983652000E5A7F2F8B35DFD083EE593E5BA895C95729F2BADE9C8ABF3DD9CE240C451C6CEFFC768D83CBAC76ABB8FEA58F013C647007CFF7617BAC2AE3981816F25CC7E5238EF34C4F02D0B01C24F80C2C65E7E7743A4FA8E23206A23ECE290C26EA56DB085C5C5EAE26292451FC8292F9957BE8FF20FAD53E5");
        capk.setRID(rid);
        capk.setHash(key_hash);
        capk.setIndex(0x08);
        capk.setExponent(new byte[]{0x00,0x00,0x03});
        capk.setModulus(key_modulus);
        capk.setModuleLen(key_modulus.length);
        retval = capkLoader.load(capk);
        if (retval < 0) {
            Log.d(getClass().getName(), String.format("capkLoader.load returned %d", retval));
        }

    }

    private void LoadJcb0F(){
        int retval;
        CAPKEntry capk = new CAPKEntry();
        Log.d(getClass().getName(), "Load L3 CAPK keys");
        // Setup one key, just to test the API
        capk.setAlgorithmIndicator((byte)1);
        capk.setHashAlgorithm((byte)1);
        byte[] rid = new byte[5];
        byte[] key_modulus = new byte[248];
        byte[] key_hash = new byte[20];
        rid = hexStringToByteArray("A000000065");
        key_hash = hexStringToByteArray("2A1B82DE00F5F0C401760ADF528228D3EDE0F403");
        key_modulus = hexStringToByteArray("9EFBADDE4071D4EF98C969EB32AF854864602E515D6501FDE576B310964A4F7C2CE842ABEFAFC5DC9E26A619BCF2614FE07375B9249BEFA09CFEE70232E75FFD647571280C76FFCA87511AD255B98A6B577591AF01D003BD6BF7E1FCE4DFD20D0D0297ED5ECA25DE261F37EFE9E175FB5F12D2503D8CFB060A63138511FE0E125CF3A643AFD7D66DCF9682BD246DDEA1");
        capk.setRID(rid);
        capk.setHash(key_hash);
        capk.setIndex(0x0F);
        capk.setExponent(new byte[]{0x00,0x00,0x03});
        capk.setModulus(key_modulus);
        capk.setModuleLen(key_modulus.length);
        retval = capkLoader.load(capk);
        if (retval < 0) {
            Log.d(getClass().getName(), String.format("capkLoader.load returned %d", retval));
        }

    }

    private void LoadJcb11(){
        int retval;
        CAPKEntry capk = new CAPKEntry();
        Log.d(getClass().getName(), "Load L3 CAPK keys");
        // Setup one key, just to test the API
        capk.setAlgorithmIndicator((byte)1);
        capk.setHashAlgorithm((byte)1);
        byte[] rid = new byte[5];
        byte[] key_modulus = new byte[248];
        byte[] key_hash = new byte[20];
        rid = hexStringToByteArray("A000000065");
        key_hash = hexStringToByteArray("D9FD62C9DD4E6DE7741E9A17FB1FF2C5DB948BCB");
        key_modulus = hexStringToByteArray("A2583AA40746E3A63C22478F576D1EFC5FB046135A6FC739E82B55035F71B09BEB566EDB9968DD649B94B6DEDC033899884E908C27BE1CD291E5436F762553297763DAA3B890D778C0F01E3344CECDFB3BA70D7E055B8C760D0179A403D6B55F2B3B083912B183ADB7927441BED3395A199EEFE0DEBD1F5FC3264033DA856F4A8B93916885BD42F9C1F456AAB8CFA83AC574833EB5E87BB9D4C006A4B5346BD9E17E139AB6552D9C58BC041195336485");
        capk.setRID(rid);
        capk.setHash(key_hash);
        capk.setIndex(0x11);
        capk.setExponent(new byte[]{0x00,0x00,0x03});
        capk.setModulus(key_modulus);
        capk.setModuleLen(key_modulus.length);
        retval = capkLoader.load(capk);
        if (retval < 0) {
            Log.d(getClass().getName(), String.format("capkLoader.load returned %d", retval));
        }

    }

    private void LoadJcb13(){
        int retval;
        CAPKEntry capk = new CAPKEntry();
        Log.d(getClass().getName(), "Load L3 CAPK keys");
        // Setup one key, just to test the API
        capk.setAlgorithmIndicator((byte)1);
        capk.setHashAlgorithm((byte)1);
        byte[] rid = new byte[5];
        byte[] key_modulus = new byte[248];
        byte[] key_hash = new byte[20];
        rid = hexStringToByteArray("A000000065");
        key_hash = hexStringToByteArray("54CFAE617150DFA09D3F901C9123524523EBEDF3");
        key_modulus = hexStringToByteArray("A3270868367E6E29349FC2743EE545AC53BD3029782488997650108524FD051E3B6EACA6A9A6C1441D28889A5F46413C8F62F3645AAEB30A1521EEF41FD4F3445BFA1AB29F9AC1A74D9A16B93293296CB09162B149BAC22F88AD8F322D684D6B49A12413FC1B6AC70EDEDB18EC1585519A89B50B3D03E14063C2CA58B7C2BA7FB22799A33BCDE6AFCBEB4A7D64911D08D18C47F9BD14A9FAD8805A15DE5A38945A97919B7AB88EFA11A88C0CD92C6EE7DC352AB0746ABF13585913C8A4E04464B77909C6BD94341A8976C4769EA6C0D30A60F4EE8FA19E767B170DF4FA80312DBA61DB645D5D1560873E2674E1F620083F30180BD96CA589");
        capk.setRID(rid);
        capk.setHash(key_hash);
        capk.setIndex(0x13);
        capk.setExponent(new byte[]{0x00,0x00,0x03});
        capk.setModulus(key_modulus);
        capk.setModuleLen(key_modulus.length);
        retval = capkLoader.load(capk);
        if (retval < 0) {
            Log.d(getClass().getName(), String.format("capkLoader.load returned %d", retval));
        }

    }

    private void LoadJcb10(){
        int retval;
        CAPKEntry capk = new CAPKEntry();
        Log.d(getClass().getName(), "Load L3 CAPK keys");
        // Setup one key, just to test the API
        capk.setAlgorithmIndicator((byte)1);
        capk.setHashAlgorithm((byte)1);
        byte[] rid = new byte[5];
        byte[] key_modulus = new byte[248];
        byte[] key_hash = new byte[20];
        rid = hexStringToByteArray("A000000065");
        key_hash = hexStringToByteArray("C75E5210CBE6E8F0594A0F1911B07418CADB5BAB");
        key_modulus = hexStringToByteArray("99B63464EE0B4957E4FD23BF923D12B61469B8FFF8814346B2ED6A780F8988EA9CF0433BC1E655F05EFA66D0C98098F25B659D7A25B8478A36E489760D071F54CDF7416948ED733D816349DA2AADDA227EE45936203CBF628CD033AABA5E5A6E4AE37FBACB4611B4113ED427529C636F6C3304F8ABDD6D9AD660516AE87F7F2DDF1D2FA44C164727E56BBC9BA23C0285");
        capk.setRID(rid);
        capk.setHash(key_hash);
        capk.setIndex(0x10);
        capk.setExponent(new byte[]{0x00,0x00,0x03});
        capk.setModulus(key_modulus);
        capk.setModuleLen(key_modulus.length);
        retval = capkLoader.load(capk);
        if (retval < 0) {
            Log.d(getClass().getName(), String.format("capkLoader.load returned %d", retval));
        }

    }

    private void LoadJcb12(){
        int retval;
        CAPKEntry capk = new CAPKEntry();
        Log.d(getClass().getName(), "Load L3 CAPK keys");
        // Setup one key, just to test the API
        capk.setAlgorithmIndicator((byte)1);
        capk.setHashAlgorithm((byte)1);
        byte[] rid = new byte[5];
        byte[] key_modulus = new byte[248];
        byte[] key_hash = new byte[20];
        rid = hexStringToByteArray("A000000065");
        key_hash = hexStringToByteArray("874B379B7F607DC1CAF87A19E400B6A9E25163E8");
        key_modulus = hexStringToByteArray("ADF05CD4C5B490B087C3467B0F3043750438848461288BFEFD6198DD576DC3AD7A7CFA07DBA128C247A8EAB30DC3A30B02FCD7F1C8167965463626FEFF8AB1AA61A4B9AEF09EE12B009842A1ABA01ADB4A2B170668781EC92B60F605FD12B2B2A6F1FE734BE510F60DC5D189E401451B62B4E06851EC20EBFF4522AACC2E9CDC89BC5D8CDE5D633CFD77220FF6BBD4A9B441473CC3C6FEFC8D13E57C3DE97E1269FA19F655215B23563ED1D1860D8681");
        capk.setRID(rid);
        capk.setHash(key_hash);
        capk.setIndex(0x12);
        capk.setExponent(new byte[]{0x00,0x00,0x03});
        capk.setModulus(key_modulus);
        capk.setModuleLen(key_modulus.length);
        retval = capkLoader.load(capk);
        if (retval < 0) {
            Log.d(getClass().getName(), String.format("capkLoader.load returned %d", retval));
        }

    }

    private void LoadJcb14(){
        int retval;
        CAPKEntry capk = new CAPKEntry();
        Log.d(getClass().getName(), "Load L3 CAPK keys");
        // Setup one key, just to test the API
        capk.setAlgorithmIndicator((byte)1);
        capk.setHashAlgorithm((byte)1);
        byte[] rid = new byte[5];
        byte[] key_modulus = new byte[248];
        byte[] key_hash = new byte[20];
        rid = hexStringToByteArray("A000000065");
        key_hash = hexStringToByteArray("C0D15F6CD957E491DB56DCDD1CA87A03EBE06B7B");
        key_modulus = hexStringToByteArray("AEED55B9EE00E1ECEB045F61D2DA9A66AB637B43FB5CDBDB22A2FBB25BE061E937E38244EE5132F530144A3F268907D8FD648863F5A96FED7E42089E93457ADC0E1BC89C58A0DB72675FBC47FEE9FF33C16ADE6D341936B06B6A6F5EF6F66A4EDD981DF75DA8399C3053F430ECA342437C23AF423A211AC9F58EAF09B0F837DE9D86C7109DB1646561AA5AF0289AF5514AC64BC2D9D36A179BB8A7971E2BFA03A9E4B847FD3D63524D43A0E8003547B94A8A75E519DF3177D0A60BC0B4BAB1EA59A2CBB4D2D62354E926E9C7D3BE4181E81BA60F8285A896D17DA8C3242481B6C405769A39D547C74ED9FF95A70A796046B5EFF36682DC29");
        capk.setRID(rid);
        capk.setHash(key_hash);
        capk.setIndex(0x14);
        capk.setExponent(new byte[]{0x00,0x00,0x03});
        capk.setModulus(key_modulus);
        capk.setModuleLen(key_modulus.length);
        retval = capkLoader.load(capk);
        if (retval < 0) {
            Log.d(getClass().getName(), String.format("capkLoader.load returned %d", retval));
        }

    }

    private void LoadPboc08(){
        int retval;
        CAPKEntry capk = new CAPKEntry();
        Log.d(getClass().getName(), "Load L3 CAPK keys");
        // Setup one key, just to test the API
        capk.setAlgorithmIndicator((byte)1);
        capk.setHashAlgorithm((byte)1);
        byte[] rid = new byte[5];
        byte[] key_modulus = new byte[248];
        byte[] key_hash = new byte[20];
        rid = hexStringToByteArray("A000000333");
        key_hash = hexStringToByteArray("EE23B616C95C02652AD18860E48787C079E8E85A");
        key_modulus = hexStringToByteArray("B61645EDFD5498FB246444037A0FA18C0F101EBD8EFA54573CE6E6A7FBF63ED21D66340852B0211CF5EEF6A1CD989F66AF21A8EB19DBD8DBC3706D135363A0D683D046304F5A836BC1BC632821AFE7A2F75DA3C50AC74C545A754562204137169663CFCC0B06E67E2109EBA41BC67FF20CC8AC80D7B6EE1A95465B3B2657533EA56D92D539E5064360EA4850FED2D1BF");
        capk.setRID(rid);
        capk.setHash(key_hash);
        capk.setIndex(0x08);
        capk.setExponent(new byte[]{0x00,0x00,0x03});
        capk.setModulus(key_modulus);
        capk.setModuleLen(key_modulus.length);
        retval = capkLoader.load(capk);
        if (retval < 0) {
            Log.d(getClass().getName(), String.format("capkLoader.load returned %d", retval));
        }

    }

    private void LoadPboc09(){
        int retval;
        CAPKEntry capk = new CAPKEntry();
        Log.d(getClass().getName(), "Load L3 CAPK keys");
        // Setup one key, just to test the API
        capk.setAlgorithmIndicator((byte)1);
        capk.setHashAlgorithm((byte)1);
        byte[] rid = new byte[5];
        byte[] key_modulus = new byte[248];
        byte[] key_hash = new byte[20];
        rid = hexStringToByteArray("A000000333");
        key_hash = hexStringToByteArray("A075306EAB0045BAF72CDD33B3B678779DE1F527");
        key_modulus = hexStringToByteArray("EB374DFC5A96B71D2863875EDA2EAFB96B1B439D3ECE0B1826A2672EEEFA7990286776F8BD989A15141A75C384DFC14FEF9243AAB32707659BE9E4797A247C2F0B6D99372F384AF62FE23BC54BCDC57A9ACD1D5585C303F201EF4E8B806AFB809DB1A3DB1CD112AC884F164A67B99C7D6E5A8A6DF1D3CAE6D7ED3D5BE725B2DE4ADE23FA679BF4EB15A93D8A6E29C7FFA1A70DE2E54F593D908A3BF9EBBD760BBFDC8DB8B54497E6C5BE0E4A4DAC29E5");
        capk.setRID(rid);
        capk.setHash(key_hash);
        capk.setIndex(0x09);
        capk.setExponent(new byte[]{0x00,0x00,0x03});
        capk.setModulus(key_modulus);
        capk.setModuleLen(key_modulus.length);
        retval = capkLoader.load(capk);
        if (retval < 0) {
            Log.d(getClass().getName(), String.format("capkLoader.load returned %d", retval));
        }

    }

    private void LoadPboc10(){
        int retval;
        CAPKEntry capk = new CAPKEntry();
        Log.d(getClass().getName(), "Load L3 CAPK keys");
        // Setup one key, just to test the API
        capk.setAlgorithmIndicator((byte)1);
        capk.setHashAlgorithm((byte)1);
        byte[] rid = new byte[5];
        byte[] key_modulus = new byte[248];
        byte[] key_hash = new byte[20];
        rid = hexStringToByteArray("A000000333");
        key_hash = hexStringToByteArray("C88BE6B2417C4F941C9371EA35A377158767E4E3");
        key_modulus = hexStringToByteArray("B2AB1B6E9AC55A75ADFD5BBC34490E53C4C3381F34E60E7FAC21CC2B26DD34462B64A6FAE2495ED1DD383B8138BEA100FF9B7A111817E7B9869A9742B19E5C9DAC56F8B8827F11B05A08ECCF9E8D5E85B0F7CFA644EFF3E9B796688F38E006DEB21E101C01028903A06023AC5AAB8635F8E307A53AC742BDCE6A283F585F48EF");
        capk.setRID(rid);
        capk.setHash(key_hash);
        capk.setIndex(0x10);
        capk.setExponent(new byte[]{0x00,0x00,0x03});
        capk.setModulus(key_modulus);
        capk.setModuleLen(key_modulus.length);
        retval = capkLoader.load(capk);
        if (retval < 0) {
            Log.d(getClass().getName(), String.format("capkLoader.load returned %d", retval));
        }

    }

    private void LoadPbocF8(){
        int retval;
        CAPKEntry capk = new CAPKEntry();
        Log.d(getClass().getName(), "Load L3 CAPK keys");
        // Setup one key, just to test the API
        capk.setAlgorithmIndicator((byte)1);
        capk.setHashAlgorithm((byte)1);
        byte[] rid = new byte[5];
        byte[] key_modulus = new byte[248];
        byte[] key_hash = new byte[20];
        rid = hexStringToByteArray("A000000333");
        key_hash = hexStringToByteArray("AC5AAAFB6E66F763B7A5362A9E2D31848A46EB7F");
        key_modulus = hexStringToByteArray("A465B3836D63AD61BA68D146CA0F40C5AFD6F2BEABF96DD84400B170C1483BE879A549ED7040CC7FCDA984737F71BD9A3CD5E257CFAAEBFBD93BBD8887B274BA6C74C0C40CEE0F80EF2D939FD0BC98621B81B69A4BC61260BC6228AC6C10E7C7938D5E8D324BB3D2D36E94426E948B5D4B7EECA20C471EEF38E8DFB1D2C5B8256FE7AC6EDC22387E4EDDB0FC4E64B51D89FE883FC28290D0734348D10AFB27ADDA516C73EC3042B9C788D43B22E1C54CC302F19575A3BC725A5A5CACF8ABC3EB821C908FAB10ED6C32789BC3FDA66C3AB9033336FF40ACC96C46F514EC0E8F78C487297D8637850913D97A9E8E5344C896FB82397D662321");
        capk.setRID(rid);
        capk.setHash(key_hash);
        capk.setIndex(0xF8);
        capk.setExponent(new byte[]{0x00,0x00,0x03});
        capk.setModulus(key_modulus);
        capk.setModuleLen(key_modulus.length);
        retval = capkLoader.load(capk);
        if (retval < 0) {
            Log.d(getClass().getName(), String.format("capkLoader.load returned %d", retval));
        }

    }

    private void LoadPboc02(){
        int retval;
        CAPKEntry capk = new CAPKEntry();
        Log.d(getClass().getName(), "Load L3 CAPK keys");
        // Setup one key, just to test the API
        capk.setAlgorithmIndicator((byte)1);
        capk.setHashAlgorithm((byte)1);
        byte[] rid = new byte[5];
        byte[] key_modulus = new byte[248];
        byte[] key_hash = new byte[20];
        rid = hexStringToByteArray("A000000333");
        key_hash = hexStringToByteArray("03BB335A8549A03B87AB089D006F60852E4B8060");
        key_modulus = hexStringToByteArray("A3767ABD1B6AA69D7F3FBF28C092DE9ED1E658BA5F0909AF7A1CCD907373B7210FDEB16287BA8E78E1529F443976FD27F991EC67D95E5F4E96B127CAB2396A94D6E45CDA44CA4C4867570D6B07542F8D4BF9FF97975DB9891515E66F525D2B3CBEB6D662BFB6C3F338E93B02142BFC44173A3764C56AADD202075B26DC2F9F7D7AE74BD7D00FD05EE430032663D27A57");
        capk.setRID(rid);
        capk.setHash(key_hash);
        capk.setIndex(0x02);
        capk.setExponent(new byte[]{0x00,0x00,0x03});
        capk.setModulus(key_modulus);
        capk.setModuleLen(key_modulus.length);
        retval = capkLoader.load(capk);
        if (retval < 0) {
            Log.d(getClass().getName(), String.format("capkLoader.load returned %d", retval));
        }

    }

    private void LoadPboc03(){
        int retval;
        CAPKEntry capk = new CAPKEntry();
        Log.d(getClass().getName(), "Load L3 CAPK keys");
        // Setup one key, just to test the API
        capk.setAlgorithmIndicator((byte)1);
        capk.setHashAlgorithm((byte)1);
        byte[] rid = new byte[5];
        byte[] key_modulus = new byte[248];
        byte[] key_hash = new byte[20];
        rid = hexStringToByteArray("A000000333");
        key_hash = hexStringToByteArray("87F0CD7C0E86F38F89A66F8C47071A8B88586F26");
        key_modulus = hexStringToByteArray("B0627DEE87864F9C18C13B9A1F025448BF13C58380C91F4CEBA9F9BCB214FF8414E9B59D6ABA10F941C7331768F47B2127907D857FA39AAF8CE02045DD01619D689EE731C551159BE7EB2D51A372FF56B556E5CB2FDE36E23073A44CA215D6C26CA68847B388E39520E0026E62294B557D6470440CA0AEFC9438C923AEC9B2098D6D3A1AF5E8B1DE36F4B53040109D89B77CAFAF70C26C601ABDF59EEC0FDC8A99089140CD2E817E335175B03B7AA33D");
        capk.setRID(rid);
        capk.setHash(key_hash);
        capk.setIndex(0x03);
        capk.setExponent(new byte[]{0x00,0x00,0x03});
        capk.setModulus(key_modulus);
        capk.setModuleLen(key_modulus.length);
        retval = capkLoader.load(capk);
        if (retval < 0) {
            Log.d(getClass().getName(), String.format("capkLoader.load returned %d", retval));
        }

    }

    private void LoadPboc04(){
        int retval;
        CAPKEntry capk = new CAPKEntry();
        Log.d(getClass().getName(), "Load L3 CAPK keys");
        // Setup one key, just to test the API
        capk.setAlgorithmIndicator((byte)1);
        capk.setHashAlgorithm((byte)1);
        byte[] rid = new byte[5];
        byte[] key_modulus = new byte[248];
        byte[] key_hash = new byte[20];
        rid = hexStringToByteArray("A000000333");
        key_hash = hexStringToByteArray("F527081CF371DD7E1FD4FA414A665036E0F5E6E5");
        key_modulus = hexStringToByteArray("BC853E6B5365E89E7EE9317C94B02D0ABB0DBD91C05A224A2554AA29ED9FCB9D86EB9CCBB322A57811F86188AAC7351C72BD9EF196C5A01ACEF7A4EB0D2AD63D9E6AC2E7836547CB1595C68BCBAFD0F6728760F3A7CA7B97301B7E0220184EFC4F653008D93CE098C0D93B45201096D1ADFF4CF1F9FC02AF759DA27CD6DFD6D789B099F16F378B6100334E63F3D35F3251A5EC78693731F5233519CDB380F5AB8C0F02728E91D469ABD0EAE0D93B1CC66CE127B29C7D77441A49D09FCA5D6D9762FC74C31BB506C8BAE3C79AD6C2578775B95956B5370D1D0519E37906B384736233251E8F09AD79DFBE2C6ABFADAC8E4D8624318C27DAF1");
        capk.setRID(rid);
        capk.setHash(key_hash);
        capk.setIndex(0x04);
        capk.setExponent(new byte[]{0x00,0x00,0x03});
        capk.setModulus(key_modulus);
        capk.setModuleLen(key_modulus.length);
        retval = capkLoader.load(capk);
        if (retval < 0) {
            Log.d(getClass().getName(), String.format("capkLoader.load returned %d", retval));
        }

    }

    private void LoadAmex0F(){
        int retval;
        CAPKEntry capk = new CAPKEntry();
        Log.d(getClass().getName(), "Load L3 CAPK keys");
        // Setup one key, just to test the API
        capk.setAlgorithmIndicator((byte)1);
        capk.setHashAlgorithm((byte)1);
        byte[] rid = new byte[5];
        byte[] key_modulus = new byte[248];
        byte[] key_hash = new byte[20];
        rid = hexStringToByteArray("A000000025");
        key_hash = hexStringToByteArray("A73472B3AB557493A9BC2179CC8014053B12BAB4");
        key_modulus = hexStringToByteArray("C8D5AC27A5E1FB89978C7C6479AF993AB3800EB243996FBB2AE26B67B23AC482C4B746005A51AFA7D2D83E894F591A2357B30F85B85627FF15DA12290F70F05766552BA11AD34B7109FA49DE29DCB0109670875A17EA95549E92347B948AA1F045756DE56B707E3863E59A6CBE99C1272EF65FB66CBB4CFF070F36029DD76218B21242645B51CA752AF37E70BE1A84FF31079DC0048E928883EC4FADD497A719385C2BBBEBC5A66AA5E5655D18034EC5");
        capk.setRID(rid);
        capk.setHash(key_hash);
        capk.setIndex(0x0F);
        capk.setExponent(new byte[]{0x00,0x00,0x03});
        capk.setModulus(key_modulus);
        capk.setModuleLen(key_modulus.length);
        retval = capkLoader.load(capk);
        if (retval < 0) {
            Log.d(getClass().getName(), String.format("capkLoader.load returned %d", retval));
        }

    }

    private void LoadAmex10(){
        int retval;
        CAPKEntry capk = new CAPKEntry();
        Log.d(getClass().getName(), "Load L3 CAPK keys");
        // Setup one key, just to test the API
        capk.setAlgorithmIndicator((byte)1);
        capk.setHashAlgorithm((byte)1);
        byte[] rid = new byte[5];
        byte[] key_modulus = new byte[248];
        byte[] key_hash = new byte[20];
        rid = hexStringToByteArray("A000000025");
        key_hash = hexStringToByteArray("C729CF2FD262394ABC4CC173506502446AA9B9FD");
        key_modulus = hexStringToByteArray("CF98DFEDB3D3727965EE7797723355E0751C81D2D3DF4D18EBAB9FB9D49F38C8C4A826B99DC9DEA3F01043D4BF22AC3550E2962A59639B1332156422F788B9C16D40135EFD1BA94147750575E636B6EBC618734C91C1D1BF3EDC2A46A43901668E0FFC136774080E888044F6A1E65DC9AAA8928DACBEB0DB55EA3514686C6A732CEF55EE27CF877F110652694A0E3484C855D882AE191674E25C296205BBB599455176FDD7BBC549F27BA5FE35336F7E29E68D783973199436633C67EE5A680F05160ED12D1665EC83D1997F10FD05BBDBF9433E8F797AEE3E9F02A34228ACE927ABE62B8B9281AD08D3DF5C7379685045D7BA5FCDE58637");
        capk.setRID(rid);
        capk.setHash(key_hash);
        capk.setIndex(0x10);
        capk.setExponent(new byte[]{0x00,0x00,0x03});
        capk.setModulus(key_modulus);
        capk.setModuleLen(key_modulus.length);
        retval = capkLoader.load(capk);
        if (retval < 0) {
            Log.d(getClass().getName(), String.format("capkLoader.load returned %d", retval));
        }

    }

    private void LoadAmex62(){
        int retval;
        CAPKEntry capk = new CAPKEntry();
        Log.d(getClass().getName(), "Load L3 CAPK keys");
        // Setup one key, just to test the API
        capk.setAlgorithmIndicator((byte)1);
        capk.setHashAlgorithm((byte)1);
        byte[] rid = new byte[5];
        byte[] key_modulus = new byte[248];
        byte[] key_hash = new byte[20];
        rid = hexStringToByteArray("A000000025");
        key_hash = hexStringToByteArray("CCC7303FF295A9F35BA61BD31E27EABD59658265");
        key_modulus = hexStringToByteArray("BA29DE83090D8D5F4DFFCEB98918995A768F41D0183E1ACA3EF8D5ED9062853E4080E0D289A5CEDD4DD96B1FEA2C53428436CE15A2A1BFE69D46197D3F5A79BCF8F4858BFFA04EDB07FC5BE8560D9CE38F5C3CA3C742EDFDBAE3B5E6DDA45557");
        capk.setRID(rid);
        capk.setHash(key_hash);
        capk.setIndex(0x62);
        capk.setExponent(new byte[]{0x00,0x00,0x03});
        capk.setModulus(key_modulus);
        capk.setModuleLen(key_modulus.length);
        retval = capkLoader.load(capk);
        if (retval < 0) {
            Log.d(getClass().getName(), String.format("capkLoader.load returned %d", retval));
        }

    }

    private void LoadAmex64(){
        int retval;
        CAPKEntry capk = new CAPKEntry();
        Log.d(getClass().getName(), "Load L3 CAPK keys");
        // Setup one key, just to test the API
        capk.setAlgorithmIndicator((byte)1);
        capk.setHashAlgorithm((byte)1);
        byte[] rid = new byte[5];
        byte[] key_modulus = new byte[248];
        byte[] key_hash = new byte[20];
        rid = hexStringToByteArray("A000000025");
        key_hash = hexStringToByteArray("792B121D86D0F3A99582DB06974481F3B2E18454");
        key_modulus = hexStringToByteArray("B0DD551047DAFCD10D9A5E33CF47A9333E3B24EC57E8F066A72DED60E881A8AD42777C67ADDF0708042AB943601EE60248540B67E0637018EEB3911AE9C873DAD66CB40BC8F4DC77EB2595252B61C21518F79B706AAC29E7D3FD4D259DB72B6E6D446DD60386DB40F5FDB076D80374C993B4BB2D1DB977C3870897F9DFA454F5");
        capk.setRID(rid);
        capk.setHash(key_hash);
        capk.setIndex(0x64);
        capk.setExponent(new byte[]{0x00,0x00,0x03});
        capk.setModulus(key_modulus);
        capk.setModuleLen(key_modulus.length);
        retval = capkLoader.load(capk);
        if (retval < 0) {
            Log.d(getClass().getName(), String.format("capkLoader.load returned %d", retval));
        }

    }

    private void LoadAmex65(){
        int retval;
        CAPKEntry capk = new CAPKEntry();
        Log.d(getClass().getName(), "Load L3 CAPK keys");
        // Setup one key, just to test the API
        capk.setAlgorithmIndicator((byte)1);
        capk.setHashAlgorithm((byte)1);
        byte[] rid = new byte[5];
        byte[] key_modulus = new byte[248];
        byte[] key_hash = new byte[20];
        rid = hexStringToByteArray("A000000025");
        key_hash = hexStringToByteArray("894C5D08D4EA28BB79DC46CEAD998B877322F416");
        key_modulus = hexStringToByteArray("E53EB41F839DDFB474F272CD0CBE373D5468EB3F50F39C95BDF4D39FA82B98DABC9476B6EA350C0DCE1CD92075D8C44D1E57283190F96B3537D9E632C461815EBD2BAF36891DF6BFB1D30FA0B752C43DCA0257D35DFF4CCFC98F84198D5152EC61D7B5F74BD09383BD0E2AA42298FFB02F0D79ADB70D72243EE537F75536A8A8DF962582E9E6812F3A0BE02A4365400D");
        capk.setRID(rid);
        capk.setHash(key_hash);
        capk.setIndex(0x65);
        capk.setExponent(new byte[]{0x00,0x00,0x03});
        capk.setModulus(key_modulus);
        capk.setModuleLen(key_modulus.length);
        retval = capkLoader.load(capk);
        if (retval < 0) {
            Log.d(getClass().getName(), String.format("capkLoader.load returned %d", retval));
        }

    }

    private void LoadAmex66(){
        int retval;
        CAPKEntry capk = new CAPKEntry();
        Log.d(getClass().getName(), "Load L3 CAPK keys");
        // Setup one key, just to test the API
        capk.setAlgorithmIndicator((byte)1);
        capk.setHashAlgorithm((byte)1);
        byte[] rid = new byte[5];
        byte[] key_modulus = new byte[248];
        byte[] key_hash = new byte[20];
        rid = hexStringToByteArray("A000000025");
        key_hash = hexStringToByteArray("F367CB70F9C9B67B580F533819E302BAC0330090");
        key_modulus = hexStringToByteArray("BD1478877B9333612D257D9E3C9C23503E28336B723C71F47C25836670395360F53C106FD74DEEEA291259C001AFBE7B4A83654F6E2D9E8148E2CB1D9223AC5903DA18B433F8E3529227505DE84748F241F7BFCD2146E5E9A8C5D2A06D19097087A069F9AE3D610C7C8E1214481A4F27025A1A2EDB8A9CDAFA445690511DB805");
        capk.setRID(rid);
        capk.setHash(key_hash);
        capk.setIndex(0x66);
        capk.setExponent(new byte[]{0x00,0x00,0x03});
        capk.setModulus(key_modulus);
        capk.setModuleLen(key_modulus.length);
        retval = capkLoader.load(capk);
        if (retval < 0) {
            Log.d(getClass().getName(), String.format("capkLoader.load returned %d", retval));
        }

    }

    private void LoadAmex67(){
        int retval;
        CAPKEntry capk = new CAPKEntry();
        Log.d(getClass().getName(), "Load L3 CAPK keys");
        // Setup one key, just to test the API
        capk.setAlgorithmIndicator((byte)1);
        capk.setHashAlgorithm((byte)1);
        byte[] rid = new byte[5];
        byte[] key_modulus = new byte[248];
        byte[] key_hash = new byte[20];
        rid = hexStringToByteArray("A000000025");
        key_hash = hexStringToByteArray("52A2907300C8445BF54B970C894691FEADF2D28E");
        key_modulus = hexStringToByteArray("C687ADCCF3D57D3360B174E471EDA693AA555DFDC6C8CD394C74BA25CCDF8EABFD1F1CEADFBE2280C9E81F7A058998DC22B7F22576FE84713D0BDD3D34CFCD12FCD0D26901BA74103D075C664DABCCAF57BF789494051C5EC303A2E1D784306D3DB3EB665CD360A558F40B7C05C919B2F0282FE1ED9BF6261AA814648FBC263B14214491DE426D242D65CD1FFF0FBE4D4DAFF5CFACB2ADC7131C9B147EE791956551076270696B75FD97373F1FD7804F");
        capk.setRID(rid);
        capk.setHash(key_hash);
        capk.setIndex(0x67);
        capk.setExponent(new byte[]{0x00,0x00,0x03});
        capk.setModulus(key_modulus);
        capk.setModuleLen(key_modulus.length);
        retval = capkLoader.load(capk);
        if (retval < 0) {
            Log.d(getClass().getName(), String.format("capkLoader.load returned %d", retval));
        }

    }

    private void LoadAmex68(){
        int retval;
        CAPKEntry capk = new CAPKEntry();
        Log.d(getClass().getName(), "Load L3 CAPK keys");
        // Setup one key, just to test the API
        capk.setAlgorithmIndicator((byte)1);
        capk.setHashAlgorithm((byte)1);
        byte[] rid = new byte[5];
        byte[] key_modulus = new byte[248];
        byte[] key_hash = new byte[20];
        rid = hexStringToByteArray("A000000025");
        key_hash = hexStringToByteArray("415E5FE9EC966C835FBB3E6F766A9B1A4B8674C3");
        key_modulus = hexStringToByteArray("F4D198F2F0CF140E4D2D81B765EB4E24CED4C0834822769854D0E97E8066CBE465029B3F410E350F6296381A253BE71A4BBABBD516625DAE67D073D00113AAB9EA4DCECA29F3BB7A5D46C0D8B983E2482C2AD759735A5AB9AAAEFB31D3E718B8CA66C019ECA0A8BE312E243EB47A62300620BD51CF169A9194C17A42E51B34D83775A98E80B2D66F4F98084A448FE0507EA27C905AEE72B62A8A29438B6A4480FFF72F93280432A55FDD648AD93D82B9ECF01275C0914BAD8EB3AAF46B129F8749FEA425A2DCDD7E813A08FC0CA7841EDD49985CD8BC6D5D56F17AB9C67CEC50BA422440563ECCE21699E435C8682B6266393672C693D8B7");
        capk.setRID(rid);
        capk.setHash(key_hash);
        capk.setIndex(0x68);
        capk.setExponent(new byte[]{0x00,0x00,0x03});
        capk.setModulus(key_modulus);
        capk.setModuleLen(key_modulus.length);
        retval = capkLoader.load(capk);
        if (retval < 0) {
            Log.d(getClass().getName(), String.format("capkLoader.load returned %d", retval));
        }

    }

    private void LoadAmex04(){
        int retval;
        CAPKEntry capk = new CAPKEntry();
        Log.d(getClass().getName(), "Load L3 CAPK keys");
        // Setup one key, just to test the API
        capk.setAlgorithmIndicator((byte)1);
        capk.setHashAlgorithm((byte)1);
        byte[] rid = new byte[5];
        byte[] key_modulus = new byte[248];
        byte[] key_hash = new byte[20];
        rid = hexStringToByteArray("A000000025");
        key_hash = hexStringToByteArray("FDD7139EC7E0C33167FD61AD3CADBD68D66E91C5");
        key_modulus = hexStringToByteArray("D0F543F03F2517133EF2BA4A1104486758630DCFE3A883C77B4E4844E39A9BD6360D23E6644E1E071F196DDF2E4A68B4A3D93D14268D7240F6A14F0D714C17827D279D192E88931AF7300727AE9DA80A3F0E366AEBA61778171737989E1EE309");
        capk.setRID(rid);
        capk.setHash(key_hash);
        capk.setIndex(0x04);
        capk.setExponent(new byte[]{0x00,0x00,0x03});
        capk.setModulus(key_modulus);
        capk.setModuleLen(key_modulus.length);
        retval = capkLoader.load(capk);
        if (retval < 0) {
            Log.d(getClass().getName(), String.format("capkLoader.load returned %d", retval));
        }

    }

    private void LoadAmexCA(){
        int retval;
        CAPKEntry capk = new CAPKEntry();
        Log.d(getClass().getName(), "Load L3 CAPK keys");
        // Setup one key, just to test the API
        capk.setAlgorithmIndicator((byte)1);
        capk.setHashAlgorithm((byte)1);
        byte[] rid = new byte[5];
        byte[] key_modulus = new byte[248];
        byte[] key_hash = new byte[20];
        rid = hexStringToByteArray("A000000025");
        key_hash = hexStringToByteArray("6BDA32B1AA171444C7E8F88075A74FBFE845765F");
        key_modulus = hexStringToByteArray("C23ECBD7119F479C2EE546C123A585D697A7D10B55C2D28BEF0D299C01DC65420A03FE5227ECDECB8025FBC86EEBC1935298C1753AB849936749719591758C315FA150400789BB14FADD6EAE2AD617DA38163199D1BAD5D3F8F6A7A20AEF420ADFE2404D30B219359C6A4952565CCCA6F11EC5BE564B49B0EA5BF5B3DC8C5C6401208D0029C3957A8C5922CBDE39D3A564C6DEBB6BD2AEF91FC27BB3D3892BEB9646DCE2E1EF8581EFFA712158AAEC541C0BBB4B3E279D7DA54E45A0ACC3570E712C9F7CDF985CFAFD382AE13A3B214A9E8E1E71AB1EA707895112ABC3A97D0FCB0AE2EE5C85492B6CFD54885CDD6337E895CC70FB3255E3");
        capk.setRID(rid);
        capk.setHash(key_hash);
        capk.setIndex(0xCA);
        capk.setExponent(new byte[]{0x00,0x00,0x03});
        capk.setModulus(key_modulus);
        capk.setModuleLen(key_modulus.length);
        retval = capkLoader.load(capk);
        if (retval < 0) {
            Log.d(getClass().getName(), String.format("capkLoader.load returned %d", retval));
        }

    }

    private void LoadAmexC9(){
        int retval;
        CAPKEntry capk = new CAPKEntry();
        Log.d(getClass().getName(), "Load L3 CAPK keys");
        // Setup one key, just to test the API
        capk.setAlgorithmIndicator((byte)1);
        capk.setHashAlgorithm((byte)1);
        byte[] rid = new byte[5];
        byte[] key_modulus = new byte[248];
        byte[] key_hash = new byte[20];
        rid = hexStringToByteArray("A000000025");
        key_hash = hexStringToByteArray("8E8DFF443D78CD91DE88821D70C98F0638E51E49");
        key_modulus = hexStringToByteArray("B362DB5733C15B8797B8ECEE55CB1A371F760E0BEDD3715BB270424FD4EA26062C38C3F4AAA3732A83D36EA8E9602F6683EECC6BAFF63DD2D49014BDE4D6D603CD744206B05B4BAD0C64C63AB3976B5C8CAAF8539549F5921C0B700D5B0F83C4E7E946068BAAAB5463544DB18C63801118F2182EFCC8A1E85E53C2A7AE839A5C6A3CABE73762B70D170AB64AFC6CA482944902611FB0061E09A67ACB77E493D998A0CCF93D81A4F6C0DC6B7DF22E62DB");
        capk.setRID(rid);
        capk.setHash(key_hash);
        capk.setIndex(0xC9);
        capk.setExponent(new byte[]{0x00,0x00,0x03});
        capk.setModulus(key_modulus);
        capk.setModuleLen(key_modulus.length);
        retval = capkLoader.load(capk);
        if (retval < 0) {
            Log.d(getClass().getName(), String.format("capkLoader.load returned %d", retval));
        }

    }

    private void LoadAmexC8(){
        int retval;
        CAPKEntry capk = new CAPKEntry();
        Log.d(getClass().getName(), "Load L3 CAPK keys");
        // Setup one key, just to test the API
        capk.setAlgorithmIndicator((byte)1);
        capk.setHashAlgorithm((byte)1);
        byte[] rid = new byte[5];
        byte[] key_modulus = new byte[248];
        byte[] key_hash = new byte[20];
        rid = hexStringToByteArray("A000000025");
        key_hash = hexStringToByteArray("33BD7A059FAB094939B90A8F35845C9DC779BD50");
        key_modulus = hexStringToByteArray("BF0CFCED708FB6B048E3014336EA24AA007D7967B8AA4E613D26D015C4FE7805D9DB131CED0D2A8ED504C3B5CCD48C33199E5A5BF644DA043B54DBF60276F05B1750FAB39098C7511D04BABC649482DDCF7CC42C8C435BAB8DD0EB1A620C31111D1AAAF9AF6571EEBD4CF5A08496D57E7ABDBB5180E0A42DA869AB95FB620EFF2641C3702AF3BE0B0C138EAEF202E21D");
        capk.setRID(rid);
        capk.setHash(key_hash);
        capk.setIndex(0xC8);
        capk.setExponent(new byte[]{0x00,0x00,0x03});
        capk.setModulus(key_modulus);
        capk.setModuleLen(key_modulus.length);
        retval = capkLoader.load(capk);
        if (retval < 0) {
            Log.d(getClass().getName(), String.format("capkLoader.load returned %d", retval));
        }

    }

    private void LoadRupay5A(){
        int retval;
        CAPKEntry capk = new CAPKEntry();
        Log.d(getClass().getName(), "Load L3 CAPK keys");
        // Setup one key, just to test the API
        capk.setAlgorithmIndicator((byte)1);
        capk.setHashAlgorithm((byte)1);
        byte[] rid = new byte[5];
        byte[] key_modulus = new byte[248];
        byte[] key_hash = new byte[20];
        rid = hexStringToByteArray("A000000152");
        key_hash = hexStringToByteArray("CC9585E8E637191C10FCECB32B5AE1B9D410B52D");
        key_modulus = hexStringToByteArray("EDD8252468A705614B4D07DE3211B30031AEDB6D33A4315F2CFF7C97DB918993C2DC02E79E2FF8A2683D5BBD0F614BC9AB360A448283EF8B9CF6731D71D6BE939B7C5D0B0452D660CF24C21C47CAC8E26948C8EED8E3D00C016828D642816E658DC2CFC61E7E7D7740633BEFE34107C1FB55DEA7FAAEA2B25E85BED948893D07");
        capk.setRID(rid);
        capk.setHash(key_hash);
        capk.setIndex(0x5A);
        capk.setExponent(new byte[]{0x00,0x00,0x03});
        capk.setModulus(key_modulus);
        capk.setModuleLen(key_modulus.length);
        retval = capkLoader.load(capk);
        if (retval < 0) {
            Log.d(getClass().getName(), String.format("capkLoader.load returned %d", retval));
        }

    }

    private void LoadDinner5C(){
        int retval;
        CAPKEntry capk = new CAPKEntry();
        Log.d(getClass().getName(), "Load L3 CAPK keys");
        // Setup one key, just to test the API
        capk.setAlgorithmIndicator((byte)1);
        capk.setHashAlgorithm((byte)1);
        byte[] rid = new byte[5];
        byte[] key_modulus = new byte[248];
        byte[] key_hash = new byte[20];
        rid = hexStringToByteArray("A000000152");
        key_hash = hexStringToByteArray("60154098CBBA350F5F486CA31083D1FC474E31F8");
        key_modulus = hexStringToByteArray("833F275FCF5CA4CB6F1BF880E54DCFEB721A316692CAFEB28B698CAECAFA2B2D2AD8517B1EFB59DDEFC39F9C3B33DDEE40E7A63C03E90A4DD261BC0F28B42EA6E7A1F307178E2D63FA1649155C3A5F926B4C7D7C258BCA98EF90C7F4117C205E8E32C45D10E3D494059D2F2933891B979CE4A831B301B0550CDAE9B67064B31D8B481B85A5B046BE8FFA7BDB58DC0D7032525297F26FF619AF7F15BCEC0C92BCDCBC4FB207D115AA65CD04C1CF982191");
        capk.setRID(rid);
        capk.setHash(key_hash);
        capk.setIndex(0x5C);
        capk.setExponent(new byte[]{0x00,0x00,0x03});
        capk.setModulus(key_modulus);
        capk.setModuleLen(key_modulus.length);
        retval = capkLoader.load(capk);
        if (retval < 0) {
            Log.d(getClass().getName(), String.format("capkLoader.load returned %d", retval));
        }

    }

    private void LoadInterac09(){
        int retval;
        CAPKEntry capk = new CAPKEntry();
        Log.d(getClass().getName(), "Load L3 CAPK keys");
        // Setup one key, just to test the API
        capk.setAlgorithmIndicator((byte)1);
        capk.setHashAlgorithm((byte)1);
        byte[] rid = new byte[5];
        byte[] key_modulus = new byte[248];
        byte[] key_hash = new byte[20];
        rid = hexStringToByteArray("A000000277");
        key_hash = hexStringToByteArray("A2974D6B8F302A923740E2BC6217075202037B8B");
        key_modulus = hexStringToByteArray("F802C308544873AD2225A81943732A4B7CFFA4E3157D17CD5A7723F858F0B11E636D2930FA933778F27C7C49127E0CCA317021CFE8E0F773785EB3FF07587E98CE8ED4FE9E1CA1859F41A9CF2572D8A093C5465F5A29612A45B1700F4DA13814C3D4DF075EAADE8DB4BE4D7B3AE0256F7A0C12E34BD416CAC4F9250C38B7E13B");
        capk.setRID(rid);
        capk.setHash(key_hash);
        capk.setIndex(0x09);
        capk.setExponent(new byte[]{0x01,0x00,0x01});
        capk.setModulus(key_modulus);
        capk.setModuleLen(key_modulus.length);
        retval = capkLoader.load(capk);
        if (retval < 0) {
            Log.d(getClass().getName(), String.format("capkLoader.load returned %d", retval));
        }

    }

    private void LoadInterac40(){
        int retval;
        CAPKEntry capk = new CAPKEntry();
        Log.d(getClass().getName(), "Load L3 CAPK keys");
        // Setup one key, just to test the API
        capk.setAlgorithmIndicator((byte)1);
        capk.setHashAlgorithm((byte)1);
        byte[] rid = new byte[5];
        byte[] key_modulus = new byte[248];
        byte[] key_hash = new byte[20];
        rid = hexStringToByteArray("A000000277");
        key_hash = hexStringToByteArray("2015497BE4B86F104BBF337691825EED64E101CA");
        key_modulus = hexStringToByteArray("F802C308544873AD2225A81943732A4B7CFFA4E3157D17CD5A7723F858F0B11E636D2930FA933778F27C7C49127E0CCA317021CFE8E0F773785EB3FF07587E98CE8ED4FE9E1CA1859F41A9CF2572D8A093C5465F5A29612A45B1700F4DA13814C3D4DF075EAADE8DB4BE4D7B3AE0256F7A0C12E34BD416CAC4F9250C38B7E13B");
        capk.setRID(rid);
        capk.setHash(key_hash);
        capk.setIndex(0x40);
        capk.setExponent(new byte[]{0x01,0x00,0x01});
        capk.setModulus(key_modulus);
        capk.setModuleLen(key_modulus.length);
        retval = capkLoader.load(capk);
        if (retval < 0) {
            Log.d(getClass().getName(), String.format("capkLoader.load returned %d", retval));
        }

    }


    //Load AID configuration
    private void LoadVisaAidConfig() throws NSDKException {
        int retval;
        Log.d(getClass().getName(), "Load L3 AID config data");
        byte[] VisaAidCfgCt;
        byte[] VisaAidCfgCl;
        byte[] VisaDebitAidCfgCt;
        byte[] VisaDebitAidCfgCl;

        VisaAidCfgCt = hexStringToByteArray(
                "9F0607A0000000031010" +
                        "DF13050010000000" + "DF1205DC4004F800" + "DF1105DC4000A800" +
                        "9F0902008C" + "9F1B0400000000" + "9F530152" +
                        "1F811F0A9F0AFFFFFFFF02030320");
        VisaAidCfgCl = hexStringToByteArray(
                "9F0607A0000000031010" +
                        "DF37080300000000000000" + "9F660432204000" + "9F1A020826" +
                        "9F0902008C" + "9F1B0400000000" + "9F530152" +
                        "DF2006009999999999" + "DF1906000000000000" + "DF2106000000025001" /*+
                "DF3D0101" + "DF3F19053102682620AC999999999999000000000000000000005001"*/ );

        VisaDebitAidCfgCt = hexStringToByteArray(
                "9F0607A0000000032010" +
                        "DF13050010000000" + "DF1205DC4004F800" + "DF1105DC4000A800" +
                        "9F0902008C" + "9F1B0400000000" + "9F530152" +
                        "1F811F0A9F0AFFFFFFFF02030320");
        VisaDebitAidCfgCl = hexStringToByteArray(
                "9F0607A0000000032010" +
                        "DF37080300000000000000" + "9F660432204000" + "9F1A020826" +
                        "9F0902008C" + "9F1B0400000000" + "9F530152" +
                        "DF2006009999999999" + "DF1906000000000000" + "DF2106000000025001" /*+
                        "DF3D0101" + "DF3F19053102682620AC999999999999000000000000000000005001"*/ );

        byte[] VisaAid1CfgCt = hexStringToByteArray(
                "9F0607A0000000033010" +
                        "DF13050010000000" + "DF1205DC4004F800" + "DF1105DC4000A800" +
                        "9F0902008C" + "9F1B0400000000" + "9F530152" +
                        "1F811F0A9F0AFFFFFFFF02030320");

        byte[] VisaAid2CfgCt = hexStringToByteArray(
                "9F0607A0000000038010" +
                        "DF13050010000000" + "DF1205DC4004F800" + "DF1105DC4000A800" +
                        "9F0902008C" + "9F1B0400000000" + "9F530152" +
                        "1F811F0A9F0AFFFFFFFF02030320");

        // Remove all records
        // Load Visa AID Contact configuration
        retval = aidContact.loadAID(VisaAidCfgCt);
        if (retval < 0) {
            Log.d(getClass().getName(), String.format("aidContact.LoadAID() returned %d", retval));
        }

        retval = aidContact.loadAID(VisaDebitAidCfgCt);
        if (retval < 0) {
            Log.d(getClass().getName(), String.format("aidContact.LoadAID() returned %d", retval));
        }

        retval = aidContact.loadAID(VisaAid1CfgCt);
        retval = aidContact.loadAID(VisaAid2CfgCt);
        if (retval < 0) {
            Log.d(getClass().getName(), String.format("aidContact.LoadAID() returned %d", retval));
        }
        // Load Visa AID Contactless configuration
        retval = aidContactLess.loadAID(VisaAidCfgCl);

        if (retval < 0) {
            Log.d(getClass().getName(), String.format("aidContactLess.loadAID() returned %d", retval));
        }
        retval = aidContactLess.loadAID(VisaDebitAidCfgCl);
        if (retval < 0) {
            Log.d(getClass().getName(), String.format("aidContactLess.loadAID() returned %d", retval));
        }

    }

    private void LoadPbocAidConfig() throws NSDKException {
        int retval;

        byte[] PbocAidCfgCt;
        byte[] PbocAidCfgCl;

        PbocAidCfgCt = hexStringToByteArray(
                "9F0607A0000003330101" +
                        "DF13050010000000" + "DF1205DC4004F800" + "DF1105DC4000A800" +
                        "9F09020020");

        PbocAidCfgCl = hexStringToByteArray(
                "9F0607A0000003330101" +
                        "DF37080700000000000000" + "9F660436004080" + "5F2A020156" /*+
                "DF3D0101" + "DF3F19053102682620AC999999999999000000000000000000005001"*/ );

        retval = aidContact.loadAID(PbocAidCfgCt);
        if (retval < 0) {
            Log.d(getClass().getName(), String.format("aidContactLess.loadAID() returned %d", retval));
        }

        retval = aidContactLess.loadAID(PbocAidCfgCl);
        if (retval < 0) {
            Log.d(getClass().getName(), String.format("aidContactLess.loadAID() returned %d", retval));
        }

    }

    private void LoadMasterCardAidConfig() throws NSDKException {
        int retval = 0;



        byte[] MasterCard1AidCfgCt = hexStringToByteArray(
                "9F0607A0000000041010" +
                        "DF13050000000000" + "DF1205FC50BCF800" + "DF1105FC50BC2000" +
                        "9F09020002" + "9F1B0400000000" + "9F1D086C78000000000000");
        byte[] Paypass1AidCfgCl = hexStringToByteArray(
                "9F0607A0000000041010" +
                        "DF37080200000000000000" + "9F3303E0F8C8" + "DF480168" +
                        "DF420110" + "DF470100" +"DF2F0120" +"DF2B039F6A04" +"DF460101"+
                        "9F7B06000009999999" + "9F1D086C78000000000000" +
                        "DF2106000000025000" + "DF1906000000020000" +
                        "DF13050010000000" + "DF1205DC4004F800" + "DF1105DC4000A800" );

        byte[] MasterCard2AidCfgCt = hexStringToByteArray(
                "9F0607A0000000043060" +
                        "DF13050000800000" + "DF1205FC50BCF800" + "DF1105FC50BC2000" +
                        "9F09020002" + "9F1B0400002710" + "9F1D084C78800000000000");
        byte[] Paypass2AidCfgCl = hexStringToByteArray(
                "9F0607A0000000043060" +
                        "DF37080200000000000000" + "9F3303E0F8C8" + "DF480168" +
                        "DF420110" + "DF470100" +"DF2F0120" +"DF2B039F6A04" +"DF460101"+
                        "9F7B06000009999999" + "9F1D084C78800000000000" +
                        "DF2106000000025000" + "DF1906000000020000" +
                        "DF13050010000000" + "DF1205DC4004F800" + "DF1105DC4000A800" );

        byte[] MasterCard3AidCfgCt = hexStringToByteArray(
                "9F0607A0000000046000" +
                        "DF13050400000000" + "DF1205F850ACF800" + "DF1105FC50ACA000" +
                        "9F09020002" + "9F1D084C78800000000000");
        byte[] Paypass3AidCfgCl = hexStringToByteArray(
                "9F0607A0000000046000" +
                        "DF37080200000000000000" + "9F3303E0F8C8" + "DF480168" +
                        "DF420110" + "DF470100" +"DF2F0120" +"DF2B039F6A04" +"DF460101"+
                        "9F7B06000009999999" + "9F1D084000800000000000" +
                        "DF2106000000025000" + "DF1906000000020000" +
                        "DF13050010000000" + "DF1205DC4004F800" + "DF1105DC4000A800" );

        byte[] MasterCard4AidCfgCt = hexStringToByteArray(
                "9F0607A0000000046010" +
                        "DF13050400000000" + "DF1205F850ACF800" + "DF1105FC50ACA000" +
                        "9F09020002" + "9F1D084C78800000000000");
        byte[] Paypass4AidCfgCl = hexStringToByteArray(
                "9F0607A0000000042203" +
                        "DF37080200000000000000" + "9F3303E0F8C8" + "DF480168" +
                        "DF420110" + "DF470100" +"DF2F0120" +"DF2B039F6A04" +"DF460101"+
                        "9F7B06000009999999" + "9F1D084C78800000000000" +
                        "DF2106000000025000" + "DF1906000000020000" +
                        "DF13050010000000" + "DF1205DC4004F800" + "DF1105DC4000A800"  );
        // Remove all records
        // Load Visa AID Contact configuration
        retval = aidContact.loadAID(MasterCard1AidCfgCt);
        retval = aidContact.loadAID(MasterCard2AidCfgCt);
        retval = aidContact.loadAID(MasterCard3AidCfgCt);
        retval = aidContact.loadAID(MasterCard4AidCfgCt);
        if (retval < 0) {
            Log.d(getClass().getName(), String.format("aidContact.LoadAID() returned %d", retval));
        }

        retval = aidContactLess.loadAID(Paypass1AidCfgCl);
        retval = aidContactLess.loadAID(Paypass2AidCfgCl);
        retval = aidContactLess.loadAID(Paypass3AidCfgCl);
        retval = aidContactLess.loadAID(Paypass4AidCfgCl);
        if (retval < 0) {
            Log.d(getClass().getName(), String.format("aidContactLess.loadAID() returned %d", retval));
        }

    }

    private void LoadAmexAidConfig() throws NSDKException {
        int retval;

        byte[]TestAidCfgCt = hexStringToByteArray(
                "9F0607A0000000101030" +
                        "DF13050400000000" + "DF1205F850ACF800" + "DF1105FC50ACA000" +
                        "9F09020002" + "9F1D084C78800000000000");

        byte[]AmexAidCfgCt = hexStringToByteArray(
                "9F0606A00000002501" +
                        "DF13050000000000" + "DF12050000000000" + "DF11050000000000" +
                        "9F09020001" + "9F1B0400002710");

        byte[] AmexAidCfgCl = hexStringToByteArray(
                "9F0606A00000002501" +
                        "DF37080400000000000000" + "9F6604D8800000" + "DF4901C0" +
                        "DF4A013C" + "9F3303E0F8C8" + "DF2006000000001500" +
                        "DF1906000000001200" + "DF2106000000001000" +
                 "DF13050010000000" + "DF1205DE00FC9800" + "DF1105DC50FC9800" +"DF533F01000F00000000150000000000120000000000100001060F000000000700000000000400000000000200010B0F000000000300000000000100000000000200" );



        // Load Visa AID Contact configuration
        retval = aidContact.loadAID(TestAidCfgCt);
        retval = aidContact.loadAID(AmexAidCfgCt);
        if (retval < 0) {
            Log.d(getClass().getName(), String.format("aidContact.LoadAID() returned %d", retval));
        }

        // Load Visa AID Contactless configuration
        retval = aidContactLess.loadAID(AmexAidCfgCl);
        if (retval < 0) {
            Log.d(getClass().getName(), String.format("aidContactLess.loadAID() returned %d", retval));
        }


    }

    private void LoadDiscoverAidConfig() throws NSDKException {
        int retval;

        byte[]Discover1AidCfgCt = hexStringToByteArray(
                "9F0607A0000003241010");

        byte[]Discover2AidCfgCt = hexStringToByteArray(
                "9F0607A0000001524010");

        byte[]Discover3AidCfgCt = hexStringToByteArray(
                "9F0607A0000001523010" +
                        "9F1B0400003A98" );

        byte[] Dpass1AidCfgCl = hexStringToByteArray(
                "9F0607A0000003241010" +
                        "DF37080600000000000000" + "9F6604B6004000");

        byte[] Dpass2AidCfgCl = hexStringToByteArray(
                "9F0607A0000001523010" +
                        "DF37080600000000000000" + "9F6604B6004000");


        // Load Visa AID Contact configuration
        retval = aidContact.loadAID(Discover1AidCfgCt);
        retval = aidContact.loadAID(Discover2AidCfgCt);
        retval = aidContact.loadAID(Discover3AidCfgCt);
        if (retval < 0) {
            Log.d(getClass().getName(), String.format("aidContact.LoadAID() returned %d", retval));
        }

        // Load Visa AID Contactless configuration
        retval = aidContactLess.loadAID(Dpass1AidCfgCl);
        retval = aidContactLess.loadAID(Dpass2AidCfgCl);
        if (retval < 0) {
            Log.d(getClass().getName(), String.format("aidContactLess.loadAID() returned %d", retval));
        }


    }

    private void LoadJcbAidConfig() throws NSDKException {
        int retval;

        byte[]JcbAidCfgCt = hexStringToByteArray(
                "9F0607A0000000651010" + "DF13050010000000" + "DF1205FC60ACF800" + "DF1105FC60242800"
                        + "9F09020200" + "9F1B04000007D0");


        byte[] JcbAidCfgCl = hexStringToByteArray(
                "9F0607A0000000651010" +
                        "DF37080500000000000000" + "DF13050010000000" + "DF1205FC60ACF800" + "DF1105FC60242800"
                            +"9F5303708000" + "DF60027B00");




        // Load Visa AID Contact configuration
        retval = aidContact.loadAID(JcbAidCfgCt);
        if (retval < 0) {
            Log.d(getClass().getName(), String.format("aidContact.LoadAID() returned %d", retval));
        }

        // Load Visa AID Contactless configuration
        retval = aidContactLess.loadAID(JcbAidCfgCl);
        if (retval < 0) {
            Log.d(getClass().getName(), String.format("aidContactLess.loadAID() returned %d", retval));
        }


    }

    private void LoadPureAidConfig() throws NSDKException {
        int retval;

        byte[]PureAidCfgCt = hexStringToByteArray(
                "9F0607A0000006151010");

        byte[] PureAidCfgCl = hexStringToByteArray(
                "9F0607A0000006151010" +
                        "DF37082000000000000000" + "DF620536006043F9");

        // Load Visa AID Contact configuration
        retval = aidContact.loadAID(PureAidCfgCt);
        if (retval < 0) {
            Log.d(getClass().getName(), String.format("aidContact.LoadAID() returned %d", retval));
        }

        // Load Visa AID Contactless configuration
        retval = aidContactLess.loadAID(PureAidCfgCl);
        if (retval < 0) {
            Log.d(getClass().getName(), String.format("aidContactLess.loadAID() returned %d", retval));
        }


    }

    private void LoadRupayAidConfig() throws NSDKException {
        int retval;

        byte[]RupayAidCfgCt = hexStringToByteArray(
                "9F0607A0000005241010");

        byte[] RupayAidCfgCl = hexStringToByteArray(
                "9F0607A0000005241010" +
                        "DF37080D00000000000000" + "9F09020002");

        // Load Visa AID Contact configuration
        retval = aidContact.loadAID(RupayAidCfgCt);
        if (retval < 0) {
            Log.d(getClass().getName(), String.format("aidContact.LoadAID() returned %d", retval));
        }

        // Load Visa AID Contactless configuration
        retval = aidContactLess.loadAID(RupayAidCfgCl);
        if (retval < 0) {
            Log.d(getClass().getName(), String.format("aidContactLess.loadAID() returned %d", retval));
        }


    }

    private void LoadInteracAidConfig() throws NSDKException {
        int retval;
        byte[] IteracAidCfgCt = hexStringToByteArray(
                "9F0607A0000002771010" +
                        "DF13050010000000" + "DF1205DC4004F800" + "DF1105DC4000A800" +
                        "9F09020001" + "9F3303E0F8C8" + "9F1A020124" +
                        "5F24020124");

        byte[] EmvTestAidCfgCt = hexStringToByteArray(
                "9F0607A0000000999090" +
                        "DF13050010000000" + "DF1205D84004F800" + "DF1105D84000A800" +
                        "9F0902008C");

        byte[] InteracAidCfgCl = hexStringToByteArray(
                "9F0607A0000002771010" +
                        "DF37082100000000000000" + "9F580100" + "9F5903BC0700" +
                        "9F5E020000" + "9F1A020124" + "DF13050010000000" +
                        "DF1205DC4004F800" + "DF1105DC4000A800" + "5F24020124");

        byte[] MirAidCfgCl = hexStringToByteArray(
                "9F0607A0000006581010" +
                        "DF37088106430000000000" + "9F09020101" + "1F811A06000000030000" +
                        "1F811B06009999999999" + "1F811C06009999999999" + "1F811D02E800");

        // Remove all records
        // Load Visa AID Contact configuration
        retval = aidContact.loadAID(IteracAidCfgCt);
        retval = aidContact.loadAID(EmvTestAidCfgCt);
        if (retval < 0) {
            Log.d(getClass().getName(), String.format("aidContact.LoadAID() returned %d", retval));
        }


        // Load Visa AID Contactless configuration
        retval = aidContactLess.loadAID(InteracAidCfgCl);
        retval = aidContactLess.loadAID(MirAidCfgCl);
        if (retval < 0) {
            Log.d(getClass().getName(), String.format("aidContactLess.loadAID() returned %d", retval));
        }

    }
}
