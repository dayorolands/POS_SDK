package com.urovo.v67.aidUtils;

import android.text.TextUtils;
import android.util.Log;

import com.urovo.i9000s.api.emv.CAPK;
import com.urovo.i9000s.api.emv.ContantPara;
import com.urovo.i9000s.api.emv.EmvNfcKernelApi;
import com.urovo.i9000s.api.emv.Funs;

import java.util.Hashtable;

public class EmvUtils {

    public static final String TAG = "EmvUtils";

    /**
     * 匹配Luhn算法：可用于检测银行卡卡号 / Matching Luhn algorithm: can be used to detect bank card number
     *
     * @param cardNo
     * @return
     */

    public static boolean validCardNo_MatchLuhn(String cardNo) {
        int[] cardNoArr = new int[cardNo.length()];
        for (int i = 0; i < cardNo.length(); i++) {
            cardNoArr[i] = Integer.valueOf(String.valueOf(cardNo.charAt(i)));
        }
        for (int i = cardNoArr.length - 2; i >= 0; i -= 2) {
            cardNoArr[i] <<= 1;
            cardNoArr[i] = cardNoArr[i] / 10 + cardNoArr[i] % 10;
        }
        int sum = 0;
        for (int i = 0; i < cardNoArr.length; i++) {
            sum += cardNoArr[i];
        }
        return sum % 10 == 0;
    }

    /**
     * 插卡，内卡非接 / Insert card, internal card contactless
     *
     * @param rid
     * @return
     */
    public static CAPK methodupdateRIDForContact(String rid) {

        CAPK capk = new CAPK();
        capk.rid = Funs.GetTLVStr(rid, "9F06");
        capk.index = Funs.GetTLVStr(rid, "9F22");
        capk.exponent = Funs.GetTLVStr(rid, "DF04");
        capk.modulus = Funs.GetTLVStr(rid, "DF02");
        String checkSum = Funs.GetTLVStr(rid, "DF03");
        capk.checksum = TextUtils.isEmpty(checkSum) ? "00000000000000000000" : checkSum;
        return capk;
    }

    /**
     * 外卡非接 / Wild card contactless
     *
     * @param rid
     * @return
     */
    public static Hashtable<String, Object> methodupdateRIDForVisa(String rid) {

        Hashtable<String, Object> data = new Hashtable<String, Object>();
        data.put("CardType", "NfcCard");
        data.put("Rid", Funs.GetTLVStr(rid, "9F06"));
        data.put("Index", Funs.GetTLVStr(rid, "9F22"));
        data.put("ExpirationDate", "20311223");
        data.put("Modulus", Funs.GetTLVStr(rid, "DF02"));
        data.put("Exponent", Funs.GetTLVStr(rid, "DF04"));
        return data;
    }


    /**
     * 更新插卡AID和银联卡AID（包含非接） / Update card AID and UnionPay card AID (including contactless)
     *
     * @param aid
     * @return
     */
    public static Hashtable<String, String> methodupdateAIDForContact(String aid) {

        Hashtable<String, String> aidData = new Hashtable<String, String>();
        aidData.put("aid", Funs.GetTLVStr(aid, "9F06"));
        aidData.put("appVersion", Funs.GetTLVStr(aid, "9F08")); // 008C
        String EmvTerminalFloorLimit = Funs.GetTLVStr(aid, "9F1B");
        if (TextUtils.isEmpty(EmvTerminalFloorLimit)) {
            EmvTerminalFloorLimit = "000000000000";
        }
        aidData.put("terminalFloorLimit", EmvTerminalFloorLimit);
        aidData.put("contactTACDefault", Funs.GetTLVStr(aid, "DF11"));
        aidData.put("contactTACDenial", Funs.GetTLVStr(aid, "DF13"));
        aidData.put("contactTACOnline", Funs.GetTLVStr(aid, "DF12"));
        aidData.put("defaultDDOL", Funs.GetTLVStr(aid, "DF14"));

        String contactlessTransactionLimit = Funs.GetTLVStr(aid, "DF20");
        if (TextUtils.isEmpty(contactlessTransactionLimit)) {
            contactlessTransactionLimit = "999999999999";
        }
        aidData.put("contactlessTransactionLimit", contactlessTransactionLimit);//9F92810D				//DF20
        String contactlessFloorLimit = Funs.GetTLVStr(aid, "DF19");
        if (TextUtils.isEmpty(contactlessFloorLimit)) {
            contactlessFloorLimit = "000000000000";
        }
        aidData.put("FloorLimit", contactlessFloorLimit);//9F92810F   //000000001200	//DF19

        String contactlessCVMRequiredLimit = Funs.GetTLVStr(aid, "DF21");
        if (TextUtils.isEmpty(contactlessCVMRequiredLimit)) {
            contactlessCVMRequiredLimit = "000000000000";
        }
        aidData.put("contactlessCVMRequiredLimit", contactlessCVMRequiredLimit);//9F92810E
        return aidData;
    }

    /**
     * 更新外卡AID，针对MADA卡，需要先写入master内核，再写入pure内核 / Update the AID of the wild card. For MADA cards, you need to write to the master kernel first, and then write to the pure kernel.
     *
     * @param aidBean
     * @return
     */
    public static Hashtable<String, String> methodupdateAIDForMasterMada(AIDBean aidBean) throws Exception {

        Hashtable<String, String> aidData = new Hashtable<String, String>();
        String aidbuf = aidBean.getAID().substring(0, 10);
        String contactlessTransactionLimit = "999999999999";
        String contactlessCVMRequiredLimit = "000000000000";
        String contactlessFloorLimit = "000000000000";

        if ((aidbuf.equals("A000000228")) && !TextUtils.equals("A0000002282010", aidBean.getAID())) {
            aidData.put("CardType", "MasterCard");
            aidData.put("ApplicationIdentifier", aidBean.getAID());//9F06
            aidData.put("ApplicationVersionNumber", "0002");//9F09
            aidData.put("CVMCapabilityPerCVMRequired", "60");//DF8118
            aidData.put("DefaultUDOL", "9F3704");
            aidData.put("ReaderContactlessFloorLimit", contactlessFloorLimit);//DF8123
            aidData.put("NoOnDeviceCVM", contactlessTransactionLimit);//DF8124
            aidData.put("OnDeviceCVM", contactlessTransactionLimit);
            aidData.put("ReaderCVMRequiredLimit", contactlessCVMRequiredLimit);
            aidData.put("TerminalActionCodesOnLine", aidBean.getOnlineActionCode());//DF8122
            aidData.put("TerminalActionCodesDenial", aidBean.getDenialActionCode());//DF8121
            aidData.put("TerminalActionCodesDefault", aidBean.getDefaultActionCode());//DF8120
            //针对UL案例Subset 8 PPC MCD02
            aidData.put("TerminalRiskManagement", "2C7A000000000000");
            aidData.put("TerminalRiskManagement", "6C7A000000000000");//9F1D
            String TerminalCountryCode = "0682";
            aidData.put("TerminalCountryCode", TerminalCountryCode);//9f1A
            aidData.put("KernelConfiguration", "20");//add by max 20180716,20 normal // 30 for RRP support
            aidData.put("SecurityCapability", "08");//DF811F,00:不支持CDA，08:支持CDA
        } else {
            return null;
        }
        return aidData;
    }

    public static boolean clearAIDForContactless() {
        Log.e(TAG, TAG + "===clearAIDForContactless");
        boolean status = false;
        try {
            status = EmvNfcKernelApi.getInstance().updateAID(ContantPara.Operation.CLEAR_NFC_AID, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.e(TAG, TAG + "===status:" + status);
        return status;
    }

    /**
     * 更新外卡AID
     *
     * @param aidBean
     * @return
     */
    public static boolean updateAIDForContactless(AIDBean aidBean) throws Exception {
        Log.e(TAG, TAG + "===updateAIDForContactless: " + aidBean.getAID());
        boolean status = false;
        Hashtable<String, String> aidData = new Hashtable<String, String>();
        String contactlessTransactionLimit = "999999999999";
        String contactlessCVMRequiredLimit = "000000000000";
        String contactlessFloorLimit = "000000000000";
        String terminalFloorLimit = "00000000";

        String aidbuf = aidBean.getAID().substring(0, 10);
        String aid = aidBean.getAID();
        String app_version = aidBean.getTerminalAIDVersionNumber();
        String TAC_online = aidBean.getOnlineActionCode();
        String TAC_denial = aidBean.getDenialActionCode();
        String TAC_default = aidBean.getDefaultActionCode();
        if (TextUtils.equals("A000000333", aidbuf)) { //UPI
            aidData.put("CardType", "IcCard");
            aidData.put("aid", aid);
            aidData.put("appVersion", app_version);  // 008C
            aidData.put("contactTACOnline", TAC_online);
            aidData.put("contactTACDenial", TAC_denial);
            aidData.put("contactTACDefault", TAC_default);
            aidData.put("terminalFloorLimit", terminalFloorLimit);
            aidData.put("ThresholdValue", "000000000000");
            aidData.put("TargetPercentage", "99");
            aidData.put("MaxTargetPercentage", "99");
            aidData.put("AppSelIndicator", "00");//default 00 -part match 01 -full match
            aidData.put("defaultDDOL", "9F3704");
            aidData.put("contactlessCVMRequiredLimit", contactlessCVMRequiredLimit);//for qPBOC
            aidData.put("contactlessTransactionLimit", contactlessTransactionLimit);//for qPBOC
            aidData.put("contactlessFloorLimit", contactlessFloorLimit);//for qPBOC
        } else if (TextUtils.equals("A000000004", aidbuf)) { //Master card
            aidData.put("CardType", "MasterCard");
            aidData.put("ApplicationIdentifier", aid);//9F06
            aidData.put("TerminalActionCodesOnLine", TAC_online);//DF8122
            aidData.put("TerminalActionCodesDenial", TAC_denial);//DF8121
            aidData.put("TerminalActionCodesDefault", TAC_default);//DF8120
            aidData.put("ApplicationVersionNumber", app_version);//9F09
            aidData.put("FloorLimit", contactlessFloorLimit);//DF8123
            aidData.put("NoOnDeviceCVM", contactlessTransactionLimit);//contactless transaction limit DF8124
            aidData.put("OnDeviceCVM", contactlessTransactionLimit);// contactless transaction limit
            aidData.put("ReaderCVMRequiredLimit", contactlessCVMRequiredLimit);//DF8126
            aidData.put("DefaultUDOL", "9F6A04");//DF811A
            aidData.put("CardDataInputCapability", "60");//DF8117  // 60
            aidData.put("CVMCapabilityPerCVMRequired", "60");//DF8118 //60 support online pin
            aidData.put("CVMCapabilityNoCVMRequired", "08");//DF8119
            aidData.put("MagStripeCVMCapabilityCVMRequired", "10");// DF811E = "10";
            aidData.put("SecurityCapability", "08");//DF811F
            aidData.put("MagStripeCVMCapabilityPerNoCVMRequired", "00");// DF812C = "00";
            aidData.put("TerminalRiskManagement", "2C7A800000000000");//9F1D
            aidData.put("KernelConfiguration", "30");//  20 normal // 30 for RRP support
            if (TextUtils.equals("A0000000043060", aid)) {
                aidData.put("TerminalRiskManagement", "4C7A800000000000");//9F1D
                aidData.put("KernelConfiguration", "B0");//Maestro card not support MS mode
            }
        } else if (TextUtils.equals("A000000003", aidbuf) || aid.equals("A0000002282010")) { //Visa card
            aidData.put("CardType", "VisaCard");
            aidData.put("ApplicationIdentifier", aid);//9F06
            aidData.put("TransactionLimit", contactlessTransactionLimit);//9F92810D
            aidData.put("CvmRequiredLimit", contactlessCVMRequiredLimit);//9F92810E   //000000030000
            aidData.put("TerminalTransactionQualifiers", "36004000");//9F66  //36004000    // 32004000 not support online PIN
            aidData.put("FloorLimit", contactlessFloorLimit);//9F92810F  //000000040000
            aidData.put("LimitSwitch", "FE00");//9F92810A
            aidData.put("EmvTerminalFloorLimit", terminalFloorLimit);//9F1B
            aidData.put("ProRestrictionDisable", "01");
        } else if (TextUtils.equals("A000000025", aidbuf)) { //Amex card
            aidData.put("CardType", "AmexCard");
            aidData.put("ApplicationIdentifier", aid);//9F06
            aidData.put("ApplicationVersion", app_version);//9f09
            aidData.put("TransactionLimit", contactlessTransactionLimit);//9F92810D
            aidData.put("CvmRequiredLimit", contactlessCVMRequiredLimit);//9F92810E
            aidData.put("TerminalActionCodesOnLine", TAC_online);//DF8122 //DE00FC9800
            aidData.put("TerminalActionCodesDenial", TAC_denial);//DF8121 //0010000000
            aidData.put("TerminalActionCodesDefault", TAC_default);//DF8120 //DC50FC9800
            aidData.put("TerminalTransactionQualifiers", "DCE00003");//9F6E  //58E00003  // D8E00003 support contact // Enhanced Contactless Reader Capabilities
            aidData.put("FloorLimit", contactlessFloorLimit);//9F92810F   //000000001200
            aidData.put("LimitSwitch", "6800");//9F92810A
            aidData.put("EmvTerminalFloorLimit", terminalFloorLimit);//9F1B
        } else if (TextUtils.equals("A000000065", aidbuf)) { //Jcb card
            aidData.put("CardType", "JcbCard");
            aidData.put("ApplicationIdentifier", aid);//9F06
            aidData.put("TransactionLimit", contactlessTransactionLimit);//9F92810D
            aidData.put("CvmRequiredLimit", contactlessCVMRequiredLimit);//9F92810E
            aidData.put("TerminalActionCodesOnLine", TAC_online);//DF8122
            aidData.put("TerminalActionCodesDenial", TAC_denial);//DF8121
            aidData.put("TerminalActionCodesDefault", TAC_default);//DF8120
            aidData.put("ApplicationVersion", app_version);//9f09
            aidData.put("ConfigurationCombinationOptions", "7B00");//
            aidData.put("StaticTerminalInterchangeProfile", "708000");//
            aidData.put("FloorLimit", contactlessFloorLimit);//9F92810F   //000000001200
            aidData.put("EmvTerminalFloorLimit", terminalFloorLimit);//9F1B
            aidData.put("ThresholdValue", "000000002000");
            aidData.put("TargetPercentage", "00");
            aidData.put("MaxTargetPercentage", "00");
            aidData.put("AcquirerIdentifier", "000000000010");
            aidData.put("MerchantCategoryCode", "7032");
            aidData.put("MerchantNameAndLocation", "5858204D45524348414E54205959204C4F434154494F4E");
        } else if (TextUtils.equals("A000000152", aidbuf)) { //Discover card
            aidData.put("CardType", "DiscoverCard");
            aidData.put("ApplicationIdentifier", aid);//9F06
            aidData.put("TransactionLimit", contactlessTransactionLimit);//9F92810D
            aidData.put("CvmRequiredLimit", contactlessCVMRequiredLimit);//9F92810E
            aidData.put("TerminalActionCodesOnLine", TAC_online);//DF8122 FC60ACF800
            aidData.put("TerminalActionCodesDenial", TAC_denial);//DF8121 0010000000
            aidData.put("TerminalActionCodesDefault", TAC_default);//DF8120 FC6024A800
            aidData.put("TerminalTransactionQualifiers", "B600C000");// TTQ
            aidData.put("FloorLimit", contactlessFloorLimit);//9F92810F   //000000000000
            aidData.put("EmvTerminalFloorLimit", terminalFloorLimit);//9F1B
            aidData.put("ApplicationVersion", app_version);//9f09
            aidData.put("ThresholdValue", "000000002000");
            aidData.put("TargetPercentage", "00");
            aidData.put("MaxTargetPercentage", "00");
        }
        //1ST-Master kernel,2ND-Pure kernel
        else if (aidbuf.equals("A000000228") && !TextUtils.equals("A0000002282010", aid)) {
            aidData.put("CardType", "MasterCard");
            aidData.put("ApplicationIdentifier", aidBean.getAID());//9F06
            aidData.put("ApplicationVersionNumber", "0002");//9F09
            aidData.put("CVMCapabilityPerCVMRequired", "60");//DF8118
            aidData.put("DefaultUDOL", "9F3704");
            aidData.put("ReaderContactlessFloorLimit", contactlessFloorLimit);//DF8123
            aidData.put("NoOnDeviceCVM", contactlessTransactionLimit);//DF8124
            aidData.put("OnDeviceCVM", contactlessTransactionLimit);
            aidData.put("ReaderCVMRequiredLimit", contactlessCVMRequiredLimit);
            aidData.put("TerminalActionCodesOnLine", aidBean.getOnlineActionCode());//DF8122
            aidData.put("TerminalActionCodesDenial", aidBean.getDenialActionCode());//DF8121
            aidData.put("TerminalActionCodesDefault", aidBean.getDefaultActionCode());//DF8120
            //针对UL案例Subset 8 PPC MCD02
            aidData.put("TerminalRiskManagement", "2C7A000000000000");
            aidData.put("TerminalRiskManagement", "6C7A000000000000");//9F1D
            String TerminalCountryCode = "0682";
            aidData.put("TerminalCountryCode", TerminalCountryCode);//9f1A
            aidData.put("KernelConfiguration", "20");//add by max 20180716,20 normal // 30 for RRP support
            aidData.put("SecurityCapability", "08");//DF811F,00:不支持CDA，08:支持CDA
            status = EmvNfcKernelApi.getInstance().updateAID(ContantPara.Operation.ADD, aidData);
            Log.e(TAG, TAG + "===updateAID: " + status);

            aidData.clear();
            aidData.put("CardType", "PureCard");
            aidData.put("ApplicationIdentifier", aid);//9F06
            aidData.put("Transactionlimit", contactlessTransactionLimit);//9F92810D
            aidData.put("FloorLimit", contactlessFloorLimit);//9F92810F   //000000001200
            aidData.put("CvmRequiredLimit", contactlessCVMRequiredLimit);//9F92810E
            aidData.put("LimitSwitch", "FE00");//9F92810A
            aidData.put("EmvTerminalFloorLimit", terminalFloorLimit);//9F1B
            aidData.put("ApplicationVersion", "0001");//9f09
            aidData.put("TerminalActionCodesOnLine", TAC_online);//DF8122
            aidData.put("TerminalActionCodesDenial", TAC_denial);//DF8121
            aidData.put("TerminalActionCodesDefault", TAC_default);//DF8120
            aidData.put("ContactlessApplicationCapabilities", "3400400399");
            aidData.put("ContactlessImplementationOptions", "08");
            aidData.put("MandatoryTagObjectList", "");
            aidData.put("AdditionalTagObjectList", "9F199F249F259F6E");
            aidData.put("AuthenticationTransactionDataTagObjectList", "");
            aidData.put("ThresholdValue", "000000002000");
            aidData.put("TargetPercentage", "00");
            aidData.put("MaxTargetPercentage", "00");
            aidData.put("DefaultDDOL", "9F3704");
            status = EmvNfcKernelApi.getInstance().updateAID(ContantPara.Operation.ADD, aidData);
            Log.e(TAG, TAG + "===updateAID: " + status);
            return status;
        } else if (aidbuf.equals("A000000228")) {
            aidData.put("CardType", "PureCard");
            aidData.put("ApplicationIdentifier", aid);//9F06
            aidData.put("Transactionlimit", contactlessTransactionLimit);//9F92810D
            aidData.put("FloorLimit", contactlessFloorLimit);//9F92810F   //000000001200
            aidData.put("CvmRequiredLimit", contactlessCVMRequiredLimit);//9F92810E
            aidData.put("LimitSwitch", "FE00");//9F92810A
            aidData.put("EmvTerminalFloorLimit", terminalFloorLimit);//9F1B
            aidData.put("ApplicationVersion", "0001");//9f09
            aidData.put("TerminalActionCodesOnLine", TAC_online);//DF8122
            aidData.put("TerminalActionCodesDenial", TAC_denial);//DF8121
            aidData.put("TerminalActionCodesDefault", TAC_default);//DF8120
            aidData.put("ContactlessApplicationCapabilities", "3400400399");
            aidData.put("ContactlessImplementationOptions", "08");
            aidData.put("MandatoryTagObjectList", "");
            aidData.put("AdditionalTagObjectList", "9F199F249F259F6E");
            aidData.put("AuthenticationTransactionDataTagObjectList", "");
            aidData.put("ThresholdValue", "000000002000");
            aidData.put("TargetPercentage", "00");
            aidData.put("MaxTargetPercentage", "00");
            aidData.put("DefaultDDOL", "9F3704");
        }
        status = EmvNfcKernelApi.getInstance().updateAID(ContantPara.Operation.ADD, aidData);
        Log.e(TAG, TAG + "===updateAID: " + status);
        return status;
    }

    public static boolean clearAIDForContact() {
        Log.e(TAG, TAG + "===clearAIDForContact");
        boolean status = false;
        try {
            status = EmvNfcKernelApi.getInstance().updateAID(ContantPara.Operation.CLEAR_EMV_AID, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.e(TAG, TAG + "===status:" + status);
        return status;
    }

    public static boolean updateAIDForContact(AIDBean aidBean) {
        Log.e(TAG, TAG + "===updateAIDForContact: " + aidBean.getAID());
        boolean status = false;
        try {
            String aid = aidBean.getAID();
            String floorLimit_Str = "00000000";
            String CVM_required_limit_Str = "000000000000";
            String app_version = aidBean.getTerminalAIDVersionNumber();
            String target_percentage = "00";
            String max_target_percentage = "00";
            String threshold = "000000000000";
            String default_TDOL = "9F0206";
            String default_DDOL = "9F3704";
            String TAC_default = "0000000000";
            String TAC_denial = "0000000000";
            String TAC_online = "DC4004F800";

            Hashtable<String, String> data = new Hashtable<String, String>();
            data.put("CardType", "IcCard");
            data.put("aid", aid);
            data.put("appVersion", app_version);
            data.put("terminalFloorLimit", floorLimit_Str);
            data.put("contactTACDefault", TAC_default);
            data.put("contactTACDenial", TAC_denial);
            data.put("contactTACOnline", TAC_online);
            data.put("defaultDDOL", default_DDOL);
            data.put("defaultTDOL", default_TDOL);
            data.put("ThresholdValue", threshold);
            data.put("TargetPercentage", target_percentage);
            data.put("MaxTargetPercentage", max_target_percentage);

            data.put("contactlessCVMRequiredLimit", CVM_required_limit_Str);//
            data.put("contactlessFloorLimit", floorLimit_Str);//   //000000001200	//DF19
            data.put("contactlessTransactionLimit", "999999999999");//9F92810D				//DF20

            data.put("AcquirerIdentifier", "112233");  // 9f01
            data.put("AppSelIndicator", "00");//default 00 -part match 01 -full match
            data.put("TerminalAppPriority", "00");

            status = EmvNfcKernelApi.getInstance().updateAID(ContantPara.Operation.ADD, data);
            Log.e(TAG, TAG + " === updateAID: " + status);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return status;
    }

    public static boolean clearCAPK() {
        Log.e(TAG, TAG + "===clearCAPK");
        boolean status = false;
        try {
            status = EmvNfcKernelApi.getInstance().updateCAPK(ContantPara.Operation.CLEAR, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.e(TAG, TAG + "===status:" + status);
        return status;
    }

    public static boolean updateCAPK(CAPKBean capkBean) {
        Log.e(TAG, TAG + "===updateCAPK: " + capkBean.getRID());
        boolean status = false;
        Hashtable<String, String> capk = new Hashtable();
        try {
            capk.put("Rid", capkBean.getRID());
            capk.put("Index", Funs.FormatvalueByLeft(capkBean.getCA_PKIndex(), "00"));
            capk.put("Exponent", Funs.FormatvalueByLeft(capkBean.getCAPKExponent(), "00"));
            capk.put("Modulus", capkBean.getCAPKModulus());
            capk.put("Checksum", capkBean.getChecksumHash());
            status = EmvNfcKernelApi.getInstance().updateCAPK(ContantPara.Operation.ADD, capk);
            Log.e(TAG, TAG + " === updateCAPK: " + status);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return status;
    }

    public static String DecNumStrToHexNumStr(String DecAmount) {
        int HexAmountTmp = Integer.valueOf(DecAmount);
        String hexamoutstr = Integer.toHexString(HexAmountTmp);
        String szAmt = FormatvalueByLeft(hexamoutstr, "00000000");
        return szAmt.toUpperCase();
    }


    // 转成以FMT格式字符
    public static String FormatvalueByLeft(String str, String value) {
        try {
            int len = value.length();
            if (str.length() >= len) {
                return str;
            } else {
                String temp = "";

                temp = value.substring(0, value.length() - str.length());
                temp += str;
                return temp;
            }

        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
        return str;
    }

}
