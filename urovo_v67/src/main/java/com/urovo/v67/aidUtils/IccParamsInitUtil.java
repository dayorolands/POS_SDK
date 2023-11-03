package com.urovo.v67.aidUtils;

import android.content.Context;

import com.urovo.i9000s.api.emv.EmvNfcKernelApi;

import java.util.ArrayList;
import java.util.List;

public class IccParamsInitUtil {

    public static final String TAG = "IccParamsInitUtil";

    public static void updateAID(Context context, List<AIDBean> list) throws Exception {
        if (list == null || list.size() <= 0) {
            return;
        }
        EmvNfcKernelApi mEmvNfcKernelApi = EmvNfcKernelApi.getInstance();
        mEmvNfcKernelApi.LogOutEnable(1);
        EmvUtils.clearAIDForContact();
        EmvUtils.clearAIDForContactless();
        for (AIDBean aidBean : list) {
            boolean bRet = false;
            try {
                bRet = EmvUtils.updateAIDForContact(aidBean);
                bRet = EmvUtils.updateAIDForContactless(aidBean);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void updateRID(Context context, List<CAPKBean> list) throws Exception {
        if (list == null || list.size() <= 0) {
            return;
        }
        EmvNfcKernelApi mEmvNfcKernelApi = EmvNfcKernelApi.getInstance();
        mEmvNfcKernelApi.LogOutEnable(1);
        EmvUtils.clearCAPK();
        for (CAPKBean capkBean : list) {
            boolean bRet = false;
            try {
                bRet = EmvUtils.updateCAPK(capkBean);
            } catch (Exception e) {
                e.printStackTrace();
                continue;
            }
        }
    }

    public static List<AIDBean> getInitAIDParams() {

        List<AIDBean> list = new ArrayList<AIDBean>();
        AIDBean aidBean = new AIDBean();
        aidBean.setAID("A0000000031010");
        aidBean.setAIDLable("VISA CREDIT");
        aidBean.setTerminalAIDVersionNumber("00960096008D");
        aidBean.setExactOnlySelection("0");
        aidBean.setSkipEMVProgressing("0");
        aidBean.setDefaultTDOL("9700");
        aidBean.setDefaultDDOL("9F3704");
        aidBean.setEMVAdditionalTags("");
        aidBean.setDenialActionCode("0010000000");
        aidBean.setOnlineActionCode("DC4004F800");
        aidBean.setDefaultActionCode("0010000000");
        aidBean.setThresholdValue("");
        aidBean.setTargetPercebtage("");
        aidBean.setMaximumTargetPercent("");
        list.add(aidBean);

        aidBean = new AIDBean();
        aidBean.setAID("A0000000032010");
        aidBean.setAIDLable("Visa Electron");
        aidBean.setTerminalAIDVersionNumber("0084008C");
        aidBean.setExactOnlySelection("0");
        aidBean.setSkipEMVProgressing("0");
        aidBean.setDefaultTDOL("9700");
        aidBean.setDefaultDDOL("9F3704");
        aidBean.setEMVAdditionalTags("");
        aidBean.setDenialActionCode("0010000000");
        aidBean.setOnlineActionCode("D84004F800");
        aidBean.setDefaultActionCode("0010000000");
        aidBean.setThresholdValue("");
        aidBean.setTargetPercebtage("");
        aidBean.setMaximumTargetPercent("");
        list.add(aidBean);

        aidBean = new AIDBean();
        aidBean.setAID("A0000000033010");
        aidBean.setAIDLable("Visa Interlink");
        aidBean.setTerminalAIDVersionNumber("0084008C");
        aidBean.setExactOnlySelection("0");
        aidBean.setSkipEMVProgressing("0");
        aidBean.setDefaultTDOL("9700");
        aidBean.setDefaultDDOL("9F3704");
        aidBean.setEMVAdditionalTags("");
        aidBean.setDenialActionCode("0010000000");
        aidBean.setOnlineActionCode("D84004F800");
        aidBean.setDefaultActionCode("0010000000");
        aidBean.setThresholdValue("");
        aidBean.setTargetPercebtage("");
        aidBean.setMaximumTargetPercent("");
        list.add(aidBean);

        aidBean = new AIDBean();
        aidBean.setAID("A0000000041010");
        aidBean.setAIDLable("MASTERCARD");
        aidBean.setTerminalAIDVersionNumber("0002");
        aidBean.setExactOnlySelection("0");
        aidBean.setSkipEMVProgressing("0");
        aidBean.setDefaultTDOL("970F9F02065F2A029A039C0195059F3704");
        aidBean.setDefaultDDOL("9F3704");
        aidBean.setEMVAdditionalTags("");
        aidBean.setDenialActionCode("0010000000");
        aidBean.setOnlineActionCode("F45084800C");
        aidBean.setDefaultActionCode("0010000000");
        aidBean.setThresholdValue("");
        aidBean.setTargetPercebtage("");
        aidBean.setMaximumTargetPercent("");
        list.add(aidBean);

        aidBean = new AIDBean();
        aidBean.setAID("A0000000043060");
        aidBean.setAIDLable("MAESTRO");
        aidBean.setTerminalAIDVersionNumber("0002");
        aidBean.setExactOnlySelection("0");
        aidBean.setSkipEMVProgressing("0");
        aidBean.setDefaultTDOL("970F9F02065F2A029A039C0195059F3704");
        aidBean.setDefaultDDOL("9F3704");
        aidBean.setEMVAdditionalTags("9F530152");
        aidBean.setDenialActionCode("0000800000");
        aidBean.setOnlineActionCode("F45004800C");
        aidBean.setDefaultActionCode("0000800000");
        aidBean.setThresholdValue("");
        aidBean.setTargetPercebtage("");
        aidBean.setMaximumTargetPercent("");
        list.add(aidBean);

        aidBean = new AIDBean();
        aidBean.setAID("A000000025");
        aidBean.setAIDLable("AMEX");
        aidBean.setTerminalAIDVersionNumber("0001");
        aidBean.setExactOnlySelection("0");
        aidBean.setSkipEMVProgressing("0");
        aidBean.setDefaultTDOL("970F9F02065F2A029A039C0195059F3704");
        aidBean.setDefaultDDOL("9F3704");
        aidBean.setEMVAdditionalTags("");
        aidBean.setDenialActionCode("0000000000");
        aidBean.setOnlineActionCode("0000000000");
        aidBean.setDefaultActionCode("0000000000");
        aidBean.setThresholdValue("0");
        aidBean.setTargetPercebtage("00");
        aidBean.setMaximumTargetPercent("00");
        list.add(aidBean);

        aidBean = new AIDBean();
        aidBean.setAID("A00000002501");
        aidBean.setAIDLable("AMEX");
        aidBean.setTerminalAIDVersionNumber("0001");
        aidBean.setExactOnlySelection("0");
        aidBean.setSkipEMVProgressing("0");
        aidBean.setDefaultTDOL("");
        aidBean.setDefaultDDOL("9F3704");
        aidBean.setEMVAdditionalTags("");
        aidBean.setDenialActionCode("0000000000");
        aidBean.setOnlineActionCode("C400000000");
        aidBean.setDefaultActionCode("0000000000");
        aidBean.setThresholdValue("");
        aidBean.setTargetPercebtage("");
        aidBean.setMaximumTargetPercent("");
        list.add(aidBean);

        aidBean = new AIDBean();
        aidBean.setAID("A000000025010402");
        aidBean.setAIDLable("AMEX");
        aidBean.setTerminalAIDVersionNumber("0001");
        aidBean.setExactOnlySelection("0");
        aidBean.setSkipEMVProgressing("0");
        aidBean.setDefaultTDOL("");
        aidBean.setDefaultDDOL("9F3704");
        aidBean.setEMVAdditionalTags("");
        aidBean.setDenialActionCode("0000000000");
        aidBean.setOnlineActionCode("CC00FC8000");
        aidBean.setDefaultActionCode("0000000000");
        aidBean.setThresholdValue("");
        aidBean.setTargetPercebtage("");
        aidBean.setMaximumTargetPercent("");
        list.add(aidBean);

        aidBean = new AIDBean();
        aidBean.setAID("A000000026");
        aidBean.setAIDLable("DISCOVER");
        aidBean.setTerminalAIDVersionNumber("001");
        aidBean.setExactOnlySelection("0");
        aidBean.setSkipEMVProgressing("0");
        aidBean.setDefaultTDOL("");
        aidBean.setDefaultDDOL("9F3704");
        aidBean.setEMVAdditionalTags("");
        aidBean.setDenialActionCode("0000000000");
        aidBean.setOnlineActionCode("0000000000");
        aidBean.setDefaultActionCode("0000000000");
        aidBean.setThresholdValue("0");
        aidBean.setTargetPercebtage("00");
        aidBean.setMaximumTargetPercent("00");
        list.add(aidBean);

        aidBean = new AIDBean();
        aidBean.setAID("A0000000651010");
        aidBean.setAIDLable("JCB");
        aidBean.setTerminalAIDVersionNumber("00030001");
        aidBean.setExactOnlySelection("0");
        aidBean.setSkipEMVProgressing("0");
        aidBean.setDefaultTDOL("");
        aidBean.setDefaultDDOL("9F3704");
        aidBean.setEMVAdditionalTags("");
        aidBean.setDenialActionCode("0000000000");
        aidBean.setOnlineActionCode("F45084800C");
        aidBean.setDefaultActionCode("0000000000");
        aidBean.setThresholdValue("0");
        aidBean.setTargetPercebtage("00");
        aidBean.setMaximumTargetPercent("00");
        list.add(aidBean);

        aidBean = new AIDBean();
        aidBean.setAID("A0000001523010");
        aidBean.setAIDLable("DISCOVER");
        aidBean.setTerminalAIDVersionNumber("00030001");
        aidBean.setExactOnlySelection("0");
        aidBean.setSkipEMVProgressing("0");
        aidBean.setDefaultTDOL("");
        aidBean.setDefaultDDOL("9F3704");
        aidBean.setEMVAdditionalTags("");
        aidBean.setDenialActionCode("0000000000");
        aidBean.setOnlineActionCode("F45084800C");
        aidBean.setDefaultActionCode("0000000000");
        aidBean.setThresholdValue("0");
        aidBean.setTargetPercebtage("00");
        aidBean.setMaximumTargetPercent("00");
        list.add(aidBean);

        aidBean = new AIDBean();
        aidBean.setAID("A0000002281010");
        aidBean.setAIDLable("SPAN MCHIP");
        aidBean.setTerminalAIDVersionNumber("0002");
        aidBean.setExactOnlySelection("0");
        aidBean.setSkipEMVProgressing("0");
        aidBean.setDefaultTDOL("");
        aidBean.setDefaultDDOL("9F3704");
        aidBean.setEMVAdditionalTags("");
        aidBean.setDenialActionCode("0010000000");
        aidBean.setOnlineActionCode("FC408CF800");
        aidBean.setDefaultActionCode("0010000000");
        aidBean.setThresholdValue("");
        aidBean.setTargetPercebtage("");
        aidBean.setMaximumTargetPercent("");
        list.add(aidBean);

        aidBean = new AIDBean();
        aidBean.setAID("A0000002282010");
        aidBean.setAIDLable("mada VSDC");
        aidBean.setTerminalAIDVersionNumber("0084008C");
        aidBean.setExactOnlySelection("0");
        aidBean.setSkipEMVProgressing("0");
        aidBean.setDefaultTDOL("");
        aidBean.setDefaultDDOL("9F3704");
        aidBean.setEMVAdditionalTags("");
        aidBean.setDenialActionCode("0010000000");
        aidBean.setOnlineActionCode("FC408CF800");
        aidBean.setDefaultActionCode("0010000000");
        aidBean.setThresholdValue("");
        aidBean.setTargetPercebtage("");
        aidBean.setMaximumTargetPercent("");
        list.add(aidBean);

        aidBean = new AIDBean();
        aidBean.setAID("A0000002288010");
        aidBean.setAIDLable("SPAN PURE Mchip");
        aidBean.setTerminalAIDVersionNumber("0002");
        aidBean.setExactOnlySelection("0");
        aidBean.setSkipEMVProgressing("0");
        aidBean.setDefaultTDOL("");
        aidBean.setDefaultDDOL("9F3704");
        aidBean.setEMVAdditionalTags("");
        aidBean.setDenialActionCode("0010000000");
        aidBean.setOnlineActionCode("FC408CF800");
        aidBean.setDefaultActionCode("0010000000");
        aidBean.setThresholdValue("");
        aidBean.setTargetPercebtage("");
        aidBean.setMaximumTargetPercent("");
        list.add(aidBean);

        aidBean = new AIDBean();
        aidBean.setAID("A0000002289010");
        aidBean.setAIDLable("SPAN PURE VSDC");
        aidBean.setTerminalAIDVersionNumber("0008C");
        aidBean.setExactOnlySelection("0");
        aidBean.setSkipEMVProgressing("0");
        aidBean.setDefaultTDOL("");
        aidBean.setDefaultDDOL("9F3704");
        aidBean.setEMVAdditionalTags("");
        aidBean.setDenialActionCode("0010000000");
        aidBean.setOnlineActionCode("FC408CF800");
        aidBean.setDefaultActionCode("0010000000");
        aidBean.setThresholdValue("00000000");
        aidBean.setTargetPercebtage("99");
        aidBean.setMaximumTargetPercent("99");
        list.add(aidBean);

        aidBean = new AIDBean();
        aidBean.setAID("A000000333010101");
        aidBean.setAIDLable("UPI");
        aidBean.setTerminalAIDVersionNumber("0030");
        aidBean.setExactOnlySelection("0");
        aidBean.setSkipEMVProgressing("0");
        aidBean.setDefaultTDOL("");
        aidBean.setDefaultDDOL("9F3704");
        aidBean.setEMVAdditionalTags("");
        aidBean.setDenialActionCode("0010000000");
        aidBean.setOnlineActionCode("DC4004F800");
        aidBean.setDefaultActionCode("0010000000");
        aidBean.setThresholdValue("00000000");
        aidBean.setTargetPercebtage("99");
        aidBean.setMaximumTargetPercent("99");
        list.add(aidBean);

        aidBean = new AIDBean();
        aidBean.setAID("A000000333010102");
        aidBean.setAIDLable("UPI");
        aidBean.setTerminalAIDVersionNumber("0030");
        aidBean.setExactOnlySelection("0");
        aidBean.setSkipEMVProgressing("0");
        aidBean.setDefaultTDOL("");
        aidBean.setDefaultDDOL("9F3704");
        aidBean.setEMVAdditionalTags("");
        aidBean.setDenialActionCode("0010000000");
        aidBean.setOnlineActionCode("DC4004F800");
        aidBean.setDefaultActionCode("0010000000");
        aidBean.setThresholdValue("00000000");
        aidBean.setTargetPercebtage("99");
        aidBean.setMaximumTargetPercent("99");
        list.add(aidBean);

        aidBean = new AIDBean();
        aidBean.setAID("A000000333010103");
        aidBean.setAIDLable("UPI");
        aidBean.setTerminalAIDVersionNumber("0030");
        aidBean.setExactOnlySelection("0");
        aidBean.setSkipEMVProgressing("0");
        aidBean.setDefaultTDOL("");
        aidBean.setDefaultDDOL("9F3704");
        aidBean.setEMVAdditionalTags("");
        aidBean.setDenialActionCode("0010000000");
        aidBean.setOnlineActionCode("DC4004F800");
        aidBean.setDefaultActionCode("0010000000");
        aidBean.setThresholdValue("00000000");
        aidBean.setTargetPercebtage("99");
        aidBean.setMaximumTargetPercent("99");
        list.add(aidBean);

        return list;
    }

    public static List<CAPKBean> getInitCAPKParams() {

        List<CAPKBean> list = new ArrayList<>();
        CAPKBean capkBean = new CAPKBean();

        capkBean.setRID("A000000003");
        capkBean.setCA_PKIndex("08");
        capkBean.setCA_HashAlgoIndicator("01");
        capkBean.setCA_PKAlgoIndicator("01");
        capkBean.setLengthOfCAPKModulus("9S");
        capkBean.setCAPKModulus("F0D825376D89E5C54CCADE15355237811EA1F0F8117E4C32E0BAE8432BDADC23BC887FF69C2F57BF66A745B54FDAF10BB8403165A3BB05CF5C57EE271546C7CA877251DB6328916D5E4407F1BCDD4B4AC2CA67E13691E644CC61FAA842111F04E6F6A1D104C042A524A2168312D908A236F9EB14F4A78F7E2B2105B7E1DE1DF2F5EE6CF7B19FC6E0C67F4BD8352471FB9805B080C8C130FFC2D116B9233B49DBF31ADF84A843BE86184288EB56DFBB19");
        capkBean.setLengthOfCAPKExponent("2");
        capkBean.setCAPKExponent("03");
        capkBean.setChecksumHash("1ACA1B3F2AEB23B42A81348267765D7F07656FE7");
        capkBean.setCAPKExpDate("251231");
        list.add(capkBean);

        capkBean = new CAPKBean();
        capkBean.setRID("A000000003");
        capkBean.setCA_PKIndex("09");
        capkBean.setCA_HashAlgoIndicator("01");
        capkBean.setCA_PKAlgoIndicator("01");
        capkBean.setLengthOfCAPKModulus("DS");
        capkBean.setCAPKModulus("9D912248DE0A4E39C1A7DDE3F6D2588992C1A4095AFBD1824D1BA74847F2BC4926D2EFD904B4B54954CD189A54C5D1179654F8F9B0D2AB5F0357EB642FEDA95D3912C6576945FAB897E7062CAA44A4AA06B8FE6E3DBA18AF6AE3738E30429EE9BE03427C9D64F695FA8CAB4BFE376853EA34AD1D76BFCAD15908C077FFE6DC5521ECEF5D278A96E26F57359FFAEDA19434B937F1AD999DC5C41EB11935B44C18100E857F431A4A5A6BB65114F174C2D7B59FDF237D6BB1DD0916E644D709DED56481477C75D95CDD68254615F7740EC07F330AC5D67BCD75BF23D28A140826C026DBDE971A37CD3EF9B8DF644AC385010501EFC6509D7A41");
        capkBean.setLengthOfCAPKExponent("2");
        capkBean.setCAPKExponent("03");
        capkBean.setChecksumHash("1FF80A40173F52D7D27E0F26A146A1C8CCB29046");
        capkBean.setCAPKExpDate("171231");
        list.add(capkBean);

        capkBean = new CAPKBean();
        capkBean.setRID("A000000003");
        capkBean.setCA_PKIndex("92");
        capkBean.setCA_HashAlgoIndicator("01");
        capkBean.setCA_PKAlgoIndicator("01");
        capkBean.setLengthOfCAPKModulus("9S");
        capkBean.setCAPKModulus("996AF56F569187D09293C14810450ED8EE3357397B18A2458EFAA92DA3B6DF6514EC060195318FD43BE9B8F0CC669E3F844057CBDDF8BDA191BB64473BC8DC9A730DB8F6B4EDE3924186FFD9B8C7735789C23A36BA0B8AF65372EB57EA5D89E7D14E9C7B6B557460F10885DA16AC923F15AF3758F0F03EBD3C5C2C949CBA306DB44E6A2C076C5F67E281D7EF56785DC4D75945E491F01918800A9E2DC66F60080566CE0DAF8D17EAD46AD8E30A247C9F");
        capkBean.setLengthOfCAPKExponent("2");
        capkBean.setCAPKExponent("03");
        capkBean.setChecksumHash("429C954A3859CEF91295F663C963E582ED6EB253");
        capkBean.setCAPKExpDate("221229");
        list.add(capkBean);

        capkBean = new CAPKBean();
        capkBean.setRID("A000000003");
        capkBean.setCA_PKIndex("94");
        capkBean.setCA_HashAlgoIndicator("01");
        capkBean.setCA_PKAlgoIndicator("01");
        capkBean.setLengthOfCAPKModulus("DS");
        capkBean.setCAPKModulus("ACD2B12302EE644F3F835ABD1FC7A6F62CCE48FFEC622AA8EF062BEF6FB8BA8BC68BBF6AB5870EED579BC3973E121303D34841A796D6DCBC41DBF9E52C4609795C0CCF7EE86FA1D5CB041071ED2C51D2202F63F1156C58A92D38BC60BDF424E1776E2BC9648078A03B36FB554375FC53D57C73F5160EA59F3AFC5398EC7B67758D65C9BFF7828B6B82D4BE124A416AB7301914311EA462C19F771F31B3B57336000DFF732D3B83DE07052D730354D297BEC72871DCCF0E193F171ABA27EE464C6A97690943D59BDABB2A27EB71CEEBDAFA1176046478FD62FEC452D5CA393296530AA3F41927ADFE434A2DF2AE3054F8840657A26E0FC617");
        capkBean.setLengthOfCAPKExponent("2");
        capkBean.setCAPKExponent("03");
        capkBean.setChecksumHash("C4A3C43CCF87327D136B804160E47D43B60E6E0F");
        capkBean.setCAPKExpDate("221229");
        list.add(capkBean);

        capkBean = new CAPKBean();
        capkBean.setRID("A000000004");
        capkBean.setCA_PKIndex("04");
        capkBean.setCA_HashAlgoIndicator("01");
        capkBean.setCA_PKAlgoIndicator("01");
        capkBean.setLengthOfCAPKModulus("80");
        capkBean.setCAPKModulus("A6DA428387A502D7DDFB7A74D3F412BE762627197B25435B7A81716A700157DDD06F7CC99D6CA28C2470527E2C03616B9C59217357C2674F583B3BA5C7DCF2838692D023E3562420B4615C439CA97C44DC9A249CFCE7B3BFB22F68228C3AF13329AA4A613CF8DD853502373D62E49AB256D2BC17120E54AEDCED6D96A4287ACC5C04677D4A5A320DB8BEE2F775E5FEC5");
        capkBean.setLengthOfCAPKExponent("2");
        capkBean.setCAPKExponent("03");
        capkBean.setChecksumHash("381A035DA58B482EE2AF75F4C3F2CA469BA4AA6C");
        capkBean.setCAPKExpDate("221231");
        list.add(capkBean);

        capkBean = new CAPKBean();
        capkBean.setRID("A000000004");
        capkBean.setCA_PKIndex("05");
        capkBean.setCA_HashAlgoIndicator("01");
        capkBean.setCA_PKAlgoIndicator("01");
        capkBean.setLengthOfCAPKModulus("9S");
        capkBean.setCAPKModulus("B8048ABC30C90D976336543E3FD7091C8FE4800DF820ED55E7E94813ED00555B573FECA3D84AF6131A651D66CFF4284FB13B635EDD0EE40176D8BF04B7FD1C7BACF9AC7327DFAA8AA72D10DB3B8E70B2DDD811CB4196525EA386ACC33C0D9D4575916469C4E4F53E8E1C912CC618CB22DDE7C3568E90022E6BBA770202E4522A2DD623D180E215BD1D1507FE3DC90CA310D27B3EFCCD8F83DE3052CAD1E48938C68D095AAC91B5F37E28BB49EC7ED597");
        capkBean.setLengthOfCAPKExponent("2");
        capkBean.setCAPKExponent("03");
        capkBean.setChecksumHash("EBFA0D5D06D8CE702DA3EAE890701D45E274C845");
        capkBean.setCAPKExpDate("221231");
        list.add(capkBean);

        capkBean = new CAPKBean();
        capkBean.setRID("A000000004");
        capkBean.setCA_PKIndex("06");
        capkBean.setCA_HashAlgoIndicator("01");
        capkBean.setCA_PKAlgoIndicator("01");
        capkBean.setLengthOfCAPKModulus("DS");
        capkBean.setCAPKModulus("CB26FC830B43785B2BCE37C81ED334622F9622F4C89AAE641046B2353433883F307FB7C974162DA72F7A4EC75D9D657336865B8D3023D3D645667625C9A07A6B7A137CF0C64198AE38FC238006FB2603F41F4F3BB9DA1347270F2F5D8C606E420958C5F7D50A71DE30142F70DE468889B5E3A08695B938A50FC980393A9CBCE44AD2D64F630BB33AD3F5F5FD495D31F37818C1D94071342E07F1BEC2194F6035BA5DED3936500EB82DFDA6E8AFB655B1EF3D0D7EBF86B66DD9F29F6B1D324FE8B26CE38AB2013DD13F611E7A594D675C4432350EA244CC34F3873CBA06592987A1D7E852ADC22EF5A2EE28132031E48F74037E3B34AB747F");
        capkBean.setLengthOfCAPKExponent("2");
        capkBean.setCAPKExponent("03");
        capkBean.setChecksumHash("F910A1504D5FFB793D94F3B500765E1ABCAD72D9");
        capkBean.setCAPKExpDate("221231");
        list.add(capkBean);

        capkBean = new CAPKBean();
        capkBean.setRID("A000000004");
        capkBean.setCA_PKIndex("EF");
        capkBean.setCA_HashAlgoIndicator("01");
        capkBean.setCA_PKAlgoIndicator("01");
        capkBean.setLengthOfCAPKModulus("DS");
        capkBean.setCAPKModulus("A191CB87473F29349B5D60A88B3EAEE0973AA6F1A082F358D849FDDFF9C091F899EDA9792CAF09EF28F5D22404B88A2293EEBBC1949C43BEA4D60CFD879A1539544E09E0F09F60F065B2BF2A13ECC705F3D468B9D33AE77AD9D3F19CA40F23DCF5EB7C04DC8F69EBA565B1EBCB4686CD274785530FF6F6E9EE43AA43FDB02CE00DAEC15C7B8FD6A9B394BABA419D3F6DC85E16569BE8E76989688EFEA2DF22FF7D35C043338DEAA982A02B866DE5328519EBBCD6F03CDD686673847F84DB651AB86C28CF1462562C577B853564A290C8556D818531268D25CC98A4CC6A0BDFFFDA2DCCA3A94C998559E307FDDF915006D9A987B07DDAEB3B");
        capkBean.setLengthOfCAPKExponent("1");
        capkBean.setCAPKExponent("03");
        capkBean.setChecksumHash("21766EBB0EE122AFB65D7845B73DB46BAB65427A");
        capkBean.setCAPKExpDate("221231");
        list.add(capkBean);

        capkBean = new CAPKBean();
        capkBean.setRID("A000000004");
        capkBean.setCA_PKIndex("F1");
        capkBean.setCA_HashAlgoIndicator("01");
        capkBean.setCA_PKAlgoIndicator("01");
        capkBean.setLengthOfCAPKModulus("9S");
        capkBean.setCAPKModulus("A0DCF4BDE19C3546B4B6F0414D174DDE294AABBB828C5A834D73AAE27C99B0B053A90278007239B6459FF0BBCD7B4B9C6C50AC02CE91368DA1BD21AAEADBC65347337D89B68F5C99A09D05BE02DD1F8C5BA20E2F13FB2A27C41D3F85CAD5CF6668E75851EC66EDBF98851FD4E42C44C1D59F5984703B27D5B9F21B8FA0D93279FBBF69E090642909C9EA27F898959541AA6757F5F624104F6E1D3A9532F2A6E51515AEAD1B43B3D7835088A2FAFA7BE7");
        capkBean.setLengthOfCAPKExponent("1");
        capkBean.setCAPKExponent("03");
        capkBean.setChecksumHash("D8E68DA167AB5A85D8C3D55ECB9B0517A1A5B4BB");
        capkBean.setCAPKExpDate("221231");
        list.add(capkBean);

        capkBean = new CAPKBean();
        capkBean.setRID("A000000004");
        capkBean.setCA_PKIndex("F1");
        capkBean.setCA_HashAlgoIndicator("01");
        capkBean.setCA_PKAlgoIndicator("01");
        capkBean.setLengthOfCAPKModulus("9S");
        capkBean.setCAPKModulus("A0DCF4BDE19C3546B4B6F0414D174DDE294AABBB828C5A834D73AAE27C99B0B053A90278007239B6459FF0BBCD7B4B9C6C50AC02CE91368DA1BD21AAEADBC65347337D89B68F5C99A09D05BE02DD1F8C5BA20E2F13FB2A27C41D3F85CAD5CF6668E75851EC66EDBF98851FD4E42C44C1D59F5984703B27D5B9F21B8FA0D93279FBBF69E090642909C9EA27F898959541AA6757F5F624104F6E1D3A9532F2A6E51515AEAD1B43B3D7835088A2FAFA7BE7");
        capkBean.setLengthOfCAPKExponent("1");
        capkBean.setCAPKExponent("03");
        capkBean.setChecksumHash("D8E68DA167AB5A85D8C3D55ECB9B0517A1A5B4BB");
        capkBean.setCAPKExpDate("221231");
        list.add(capkBean);

        capkBean = new CAPKBean();
        capkBean.setRID("A000000004");
        capkBean.setCA_PKIndex("F3");
        capkBean.setCA_HashAlgoIndicator("01");
        capkBean.setCA_PKAlgoIndicator("01");
        capkBean.setLengthOfCAPKModulus("80");
        capkBean.setCAPKModulus("98F0C770F23864C2E766DF02D1E833DFF4FFE92D696E1642F0A88C5694C6479D16DB1537BFE29E4FDC6E6E8AFD1B0EB7EA0124723C333179BF19E93F10658B2F776E829E87DAEDA9C94A8B3382199A350C077977C97AFF08FD11310AC950A72C3CA5002EF513FCCC286E646E3C5387535D509514B3B326E1234F9CB48C36DDD44B416D23654034A66F403BA511C5EFA3");
        capkBean.setLengthOfCAPKExponent("1");
        capkBean.setCAPKExponent("03");
        capkBean.setChecksumHash("A69AC7603DAF566E972DEDC2CB433E07E8B01A9A");
        capkBean.setCAPKExpDate("221231");
        list.add(capkBean);

        capkBean = new CAPKBean();
        capkBean.setRID("A000000004");
        capkBean.setCA_PKIndex("F8");
        capkBean.setCA_HashAlgoIndicator("01");
        capkBean.setCA_PKAlgoIndicator("01");
        capkBean.setLengthOfCAPKModulus("74");
        capkBean.setCAPKModulus("A1F5E1C9BD8650BD43AB6EE56B891EF7459C0A24FA84F9127D1A6C79D4930F6DB1852E2510F18B61CD354DB83A356BD190B88AB8DF04284D02A4204A7B6CB7C5551977A9B36379CA3DE1A08E69F301C95CC1C20506959275F41723DD5D2925290579E5A95B0DF6323FC8E9273D6F849198C4996209166D9BFC973C361CC826E1");
        capkBean.setLengthOfCAPKExponent("1");
        capkBean.setCAPKExponent("03");
        capkBean.setChecksumHash("F06ECC6D2AAEBF259B7E755A38D9A9B24E2FF3DD");
        capkBean.setCAPKExpDate("221231");
        list.add(capkBean);

        capkBean = new CAPKBean();
        capkBean.setRID("A000000004");
        capkBean.setCA_PKIndex("FA");
        capkBean.setCA_HashAlgoIndicator("01");
        capkBean.setCA_PKAlgoIndicator("01");
        capkBean.setLengthOfCAPKModulus("80");
        capkBean.setCAPKModulus("A90FCD55AA2D5D9963E35ED0F440177699832F49C6BAB15CDAE5794BE93F934D4462D5D12762E48C38BA83D8445DEAA74195A301A102B2F114EADA0D180EE5E7A5C73E0C4E11F67A43DDAB5D55683B1474CC0627F44B8D3088A492FFAADAD4F42422D0E7013536C3C49AD3D0FAE96459B0F6B1B6056538A3D6D44640F94467B108867DEC40FAAECD740C00E2B7A8852D");
        capkBean.setLengthOfCAPKExponent("1");
        capkBean.setCAPKExponent("03");
        capkBean.setChecksumHash("5BED4068D96EA16D2D77E03D6036FC7A160EA99C");
        capkBean.setCAPKExpDate("221231");
        list.add(capkBean);

        capkBean = new CAPKBean();
        capkBean.setRID("A000000004");
        capkBean.setCA_PKIndex("FE");
        capkBean.setCA_HashAlgoIndicator("01");
        capkBean.setCA_PKAlgoIndicator("01");
        capkBean.setLengthOfCAPKModulus("74");
        capkBean.setCAPKModulus("A653EAC1C0F786C8724F737F172997D63D1C3251C44402049B865BAE877D0F398CBFBE8A6035E24AFA086BEFDE9351E54B95708EE672F0968BCD50DCE40F783322B2ABA04EF137EF18ABF03C7DBC5813AEAEF3AA7797BA15DF7D5BA1CBAF7FD520B5A482D8D3FEE105077871113E23A49AF3926554A70FE10ED728CF793B62A1");
        capkBean.setLengthOfCAPKExponent("1");
        capkBean.setCAPKExponent("03");
        capkBean.setChecksumHash("9A295B05FB390EF7923F57618A9FDA2941FC34E0");
        capkBean.setCAPKExpDate("221231");
        list.add(capkBean);

        capkBean = new CAPKBean();
        capkBean.setRID("A000000025");
        capkBean.setCA_PKIndex("67");
        capkBean.setCA_HashAlgoIndicator("01");
        capkBean.setCA_PKAlgoIndicator("01");
        capkBean.setLengthOfCAPKModulus("9S");
        capkBean.setCAPKModulus("C687ADCCF3D57D3360B174E471EDA693AA555DFDC6C8CD394C74BA25CCDF8EABFD1F1CEADFBE2280C9E81F7A058998DC22B7F22576FE84713D0BDD3D34CFCD12FCD0D26901BA74103D075C664DABCCAF57BF789494051C5EC303A2E1D784306D3DB3EB665CD360A558F40B7C05C919B2F0282FE1ED9BF6261AA814648FBC263B14214491DE426D242D65CD1FFF0FBE4D4DAFF5CFACB2ADC7131C9B147EE791956551076270696B75FD97373F1FD7804F");
        capkBean.setLengthOfCAPKExponent("2");
        capkBean.setCAPKExponent("03");
        capkBean.setChecksumHash("52A2907300C8445BF54B970C894691FEADF2D28E");
        capkBean.setCAPKExpDate("220831");
        list.add(capkBean);

        capkBean = new CAPKBean();
        capkBean.setRID("A000000025");
        capkBean.setCA_PKIndex("68");
        capkBean.setCA_HashAlgoIndicator("01");
        capkBean.setCA_PKAlgoIndicator("01");
        capkBean.setLengthOfCAPKModulus("DS");
        capkBean.setCAPKModulus("F4D198F2F0CF140E4D2D81B765EB4E24CED4C0834822769854D0E97E8066CBE465029B3F410E350F6296381A253BE71A4BBABBD516625DAE67D073D00113AAB9EA4DCECA29F3BB7A5D46C0D8B983E2482C2AD759735A5AB9AAAEFB31D3E718B8CA66C019ECA0A8BE312E243EB47A62300620BD51CF169A9194C17A42E51B34D83775A98E80B2D66F4F98084A448FE0507EA27C905AEE72B62A8A29438B6A4480FFF72F93280432A55FDD648AD93D82B9ECF01275C0914BAD8EB3AAF46B129F8749FEA425A2DCDD7E813A08FC0CA7841EDD49985CD8BC6D5D56F17AB9C67CEC50BA422440563ECCE21699E435C8682B6266393672C693D8B7");
        capkBean.setLengthOfCAPKExponent("2");
        capkBean.setCAPKExponent("03");
        capkBean.setChecksumHash("415E5FE9EC966C835FBB3E6F766A9B1A4B8674C3");
        capkBean.setCAPKExpDate("220831");
        list.add(capkBean);

        capkBean = new CAPKBean();
        capkBean.setRID("A000000025");
        capkBean.setCA_PKIndex("C8");
        capkBean.setCA_HashAlgoIndicator("01");
        capkBean.setCA_PKAlgoIndicator("01");
        capkBean.setLengthOfCAPKModulus("80");
        capkBean.setCAPKModulus("BF0CFCED708FB6B048E3014336EA24AA007D7967B8AA4E613D26D015C4FE7805D9DB131CED0D2A8ED504C3B5CCD48C33199E5A5BF644DA043B54DBF60276F05B1750FAB39098C7511D04BABC649482DDCF7CC42C8C435BAB8DD0EB1A620C31111D1AAAF9AF6571EEBD4CF5A08496D57E7ABDBB5180E0A42DA869AB95FB620EFF2641C3702AF3BE0B0C138EAEF202E21D");
        capkBean.setLengthOfCAPKExponent("1");
        capkBean.setCAPKExponent("03");
        capkBean.setChecksumHash("33BD7A059FAB094939B90A8F35845C9DC779BD50");
        capkBean.setCAPKExpDate("220731");
        list.add(capkBean);

        capkBean = new CAPKBean();
        capkBean.setRID("A000000025");
        capkBean.setCA_PKIndex("C9");
        capkBean.setCA_HashAlgoIndicator("01");
        capkBean.setCA_PKAlgoIndicator("01");
        capkBean.setLengthOfCAPKModulus("9S");
        capkBean.setCAPKModulus("B362DB5733C15B8797B8ECEE55CB1A371F760E0BEDD3715BB270424FD4EA26062C38C3F4AAA3732A83D36EA8E9602F6683EECC6BAFF63DD2D49014BDE4D6D603CD744206B05B4BAD0C64C63AB3976B5C8CAAF8539549F5921C0B700D5B0F83C4E7E946068BAAAB5463544DB18C63801118F2182EFCC8A1E85E53C2A7AE839A5C6A3CABE73762B70D170AB64AFC6CA482944902611FB0061E09A67ACB77E493D998A0CCF93D81A4F6C0DC6B7DF22E62DB");
        capkBean.setLengthOfCAPKExponent("1");
        capkBean.setCAPKExponent("03");
        capkBean.setChecksumHash("8E8DFF443D78CD91DE88821D70C98F0638E51E49");
        capkBean.setCAPKExpDate("220731");
        list.add(capkBean);

        capkBean = new CAPKBean();
        capkBean.setRID("A000000025");
        capkBean.setCA_PKIndex("CA");
        capkBean.setCA_HashAlgoIndicator("01");
        capkBean.setCA_PKAlgoIndicator("01");
        capkBean.setLengthOfCAPKModulus("DS");
        capkBean.setCAPKModulus("C23ECBD7119F479C2EE546C123A585D697A7D10B55C2D28BEF0D299C01DC65420A03FE5227ECDECB8025FBC86EEBC1935298C1753AB849936749719591758C315FA150400789BB14FADD6EAE2AD617DA38163199D1BAD5D3F8F6A7A20AEF420ADFE2404D30B219359C6A4952565CCCA6F11EC5BE564B49B0EA5BF5B3DC8C5C6401208D0029C3957A8C5922CBDE39D3A564C6DEBB6BD2AEF91FC27BB3D3892BEB9646DCE2E1EF8581EFFA712158AAEC541C0BBB4B3E279D7DA54E45A0ACC3570E712C9F7CDF985CFAFD382AE13A3B214A9E8E1E71AB1EA707895112ABC3A97D0FCB0AE2EE5C85492B6CFD54885CDD6337E895CC70FB3255E3");
        capkBean.setLengthOfCAPKExponent("1");
        capkBean.setCAPKExponent("03");
        capkBean.setChecksumHash("6BDA32B1AA171444C7E8F88075A74FBFE845765F");
        capkBean.setCAPKExpDate("220731");
        list.add(capkBean);

        capkBean = new CAPKBean();
        capkBean.setRID("A000000065");
        capkBean.setCA_PKIndex("11");
        capkBean.setCA_HashAlgoIndicator("01");
        capkBean.setCA_PKAlgoIndicator("01");
        capkBean.setLengthOfCAPKModulus("9S");
        capkBean.setCAPKModulus("A2583AA40746E3A63C22478F576D1EFC5FB046135A6FC739E82B55035F71B09BEB566EDB9968DD649B94B6DEDC033899884E908C27BE1CD291E5436F762553297763DAA3B890D778C0F01E3344CECDFB3BA70D7E055B8C760D0179A403D6B55F2B3B083912B183ADB7927441BED3395A199EEFE0DEBD1F5FC3264033DA856F4A8B93916885BD42F9C1F456AAB8CFA83AC574833EB5E87BB9D4C006A4B5346BD9E17E139AB6552D9C58BC041195336485");
        capkBean.setLengthOfCAPKExponent("1");
        capkBean.setCAPKExponent("03");
        capkBean.setChecksumHash("D9FD62C9DD4E6DE7741E9A17FB1FF2C5DB948BCB");
        capkBean.setCAPKExpDate("241231");
        list.add(capkBean);

        capkBean = new CAPKBean();
        capkBean.setRID("A000000065");
        capkBean.setCA_PKIndex("13");
        capkBean.setCA_HashAlgoIndicator("01");
        capkBean.setCA_PKAlgoIndicator("01");
        capkBean.setLengthOfCAPKModulus("DS");
        capkBean.setCAPKModulus("A3270868367E6E29349FC2743EE545AC53BD3029782488997650108524FD051E3B6EACA6A9A6C1441D28889A5F46413C8F62F3645AAEB30A1521EEF41FD4F3445BFA1AB29F9AC1A74D9A16B93293296CB09162B149BAC22F88AD8F322D684D6B49A12413FC1B6AC70EDEDB18EC1585519A89B50B3D03E14063C2CA58B7C2BA7FB22799A33BCDE6AFCBEB4A7D64911D08D18C47F9BD14A9FAD8805A15DE5A38945A97919B7AB88EFA11A88C0CD92C6EE7DC352AB0746ABF13585913C8A4E04464B77909C6BD94341A8976C4769EA6C0D30A60F4EE8FA19E767B170DF4FA80312DBA61DB645D5D1560873E2674E1F620083F30180BD96CA589");
        capkBean.setLengthOfCAPKExponent("1");
        capkBean.setCAPKExponent("03");
        capkBean.setChecksumHash("54CFAE617150DFA09D3F901C9123524523EBEDF3");
        capkBean.setCAPKExpDate("301231");
        list.add(capkBean);

        capkBean = new CAPKBean();
        capkBean.setRID("A000000152");
        capkBean.setCA_PKIndex("29");
        capkBean.setCA_HashAlgoIndicator("01");
        capkBean.setCA_PKAlgoIndicator("01");
        capkBean.setLengthOfCAPKModulus("DS");
        capkBean.setCAPKModulus("B6D73BF68564C88A1AEE8BA70A5F60CE495CA722E097DADEEBB83B28040B1BAD16DBC9AC3CD181BA89193638E600AF397D220F0339A8E792AA08C1878482ACC463B3B3A257AE8667CDBC1D6613CB9CBB612830FDA7F7BA689A148EFFF34476F6E0A70C819C10B3B6150909B58BF9403F5BB2E9790EE82C50C8D6FB267C726DC255AE97FABF5A357B2A0FBD1387168D83B25ECD912027B3868F072E025240CF780CC8E5839823727E5547FD1366A203F4F70FA82660B8401D4D2D06FD9A4036D14C53F6289D6FDC724E7D06F31ED93AC1B54083D9B9FCF09B135FDE9F4F6C1F0BA0142C3715E49015958C45315859DB12D942D75497FB51ED");
        capkBean.setLengthOfCAPKExponent("1");
        capkBean.setCAPKExponent("03");
        capkBean.setChecksumHash("08374162F808F8DAD0CECB8FCD1AF5F64F213D6F");
        capkBean.setCAPKExpDate("241231");
        list.add(capkBean);

        capkBean = new CAPKBean();
        capkBean.setRID("A000000152");
        capkBean.setCA_PKIndex("5B");
        capkBean.setCA_HashAlgoIndicator("01");
        capkBean.setCA_PKAlgoIndicator("01");
        capkBean.setLengthOfCAPKModulus("80");
        capkBean.setCAPKModulus("D3F45D065D4D900F68B2129AFA38F549AB9AE4619E5545814E468F382049A0B9776620DA60D62537F0705A2C926DBEAD4CA7CB43F0F0DD809584E9F7EFBDA3778747BC9E25C5606526FAB5E491646D4DD28278691C25956C8FED5E452F2442E25EDC6B0C1AA4B2E9EC4AD9B25A1B836295B823EDDC5EB6E1E0A3F41B28DB8C3B7E3E9B5979CD7E079EF024095A1D19DD");
        capkBean.setLengthOfCAPKExponent("2");
        capkBean.setCAPKExponent("03");
        capkBean.setChecksumHash("4DC5C6CAB6AE96974D9DC8B2435E21F526BC7A60");
        capkBean.setCAPKExpDate("221231");
        list.add(capkBean);

        capkBean = new CAPKBean();
        capkBean.setRID("A000000152");
        capkBean.setCA_PKIndex("5C");
        capkBean.setCA_HashAlgoIndicator("01");
        capkBean.setCA_PKAlgoIndicator("01");
        capkBean.setLengthOfCAPKModulus("9S");
        capkBean.setCAPKModulus("833F275FCF5CA4CB6F1BF880E54DCFEB721A316692CAFEB28B698CAECAFA2B2D2AD8517B1EFB59DDEFC39F9C3B33DDEE40E7A63C03E90A4DD261BC0F28B42EA6E7A1F307178E2D63FA1649155C3A5F926B4C7D7C258BCA98EF90C7F4117C205E8E32C45D10E3D494059D2F2933891B979CE4A831B301B0550CDAE9B67064B31D8B481B85A5B046BE8FFA7BDB58DC0D7032525297F26FF619AF7F15BCEC0C92BCDCBC4FB207D115AA65CD04C1CF982191");
        capkBean.setLengthOfCAPKExponent("2");
        capkBean.setCAPKExponent("03");
        capkBean.setChecksumHash("60154098CBBA350F5F486CA31083D1FC474E31F8");
        capkBean.setCAPKExpDate("221231");
        list.add(capkBean);

        capkBean = new CAPKBean();
        capkBean.setRID("A000000152");
        capkBean.setCA_PKIndex("5D");
        capkBean.setCA_HashAlgoIndicator("01");
        capkBean.setCA_PKAlgoIndicator("01");
        capkBean.setLengthOfCAPKModulus("DR");
        capkBean.setCAPKModulus("AD938EA9888E5155F8CD272749172B3A8C504C17460EFA0BED7CBC5FD32C4A80FD810312281B5A35562800CDC325358A9639C501A537B7AE43DF263E6D232B811ACDB6DDE979D55D6C911173483993A423A0A5B1E1A70237885A241B8EEBB5571E2D32B41F9CC5514DF83F0D69270E109AF1422F985A52CCE04F3DF269B795155A68AD2D6B660DDCD759F0A5DA7B64104D22C2771ECE7A5FFD40C774E441379D1132FAF04CDF55B9504C6DCE9F61776D81C7C45F19B9EFB3749AC7D486A5AD2E781FA9D082FB2677665B99FA5F1553135A1FD2A2A9FBF625CA84A7D736521431178F13100A2516F9A43CE095B032B886C7A6AB126E203BE7");
        capkBean.setLengthOfCAPKExponent("2");
        capkBean.setCAPKExponent("03");
        capkBean.setChecksumHash("B51EC5F7DE9BB6D8BCE8FB5F69BA57A04221F39B");
        capkBean.setCAPKExpDate("221231");
        list.add(capkBean);

        capkBean = new CAPKBean();
        capkBean.setRID("A000000228");
        capkBean.setCA_PKIndex("18");
        capkBean.setCA_HashAlgoIndicator("01");
        capkBean.setCA_PKAlgoIndicator("01");
        capkBean.setLengthOfCAPKModulus("9S");
        capkBean.setCAPKModulus("A523924AFD826DAD39BC4532CB121C19A702D2B0D3F29CE79E2CBD0F847BC112A5FF61EF0E3913A6DF63A3E8017FC2B19F0E61304889A88E406DAC0FF82A423052E5387EF6C073D2B8C6004D2D4077C5179A78902CE4A8F361A85C6F46D56A75F374AF7AAD0F8409098AC1F388517184001AA316D05C842907BF0D62F8A05E083DBC8FED48FF84108D1C411C5540604408C42066E6B2ED465BC0DCBBB06383EE88C1CF0A7F694317C8B3A8EF1019059B");
        capkBean.setLengthOfCAPKExponent("1");
        capkBean.setCAPKExponent("03");
        capkBean.setChecksumHash("9FE167D85CB9A9EF79FCA0B2CAB09C764850B93C");
        capkBean.setCAPKExpDate("221231");
        list.add(capkBean);

        capkBean = new CAPKBean();
        capkBean.setRID("A000000228");
        capkBean.setCA_PKIndex("19");
        capkBean.setCA_HashAlgoIndicator("01");
        capkBean.setCA_PKAlgoIndicator("01");
        capkBean.setLengthOfCAPKModulus("DS");
        capkBean.setCAPKModulus("A856777B6B4AF4D1C5D8E955852D57539FAC9B51B7879E3AC99A9F9B32F9A9DE267D6FB3BFAB54A4C4955EF90A5C4561C714A5F1E57550D33B320FF9238CE703CD4834712E32A3AC09B11320DFECFD885CF58EAAEAE2462FA0F194CCA29F0FF3431653DDD30B51101D98ED4377E3AC6B525AAB8D2023804C8724B3A98A4E94D3AE358FA1FC05E4A8DCFEFCFB5E930834D1E94AA665F923F40CDB06C3ABF213165ADE547E67A2800FD15D32EE42FCC30A07F1F6709E4984AE55A7DB79D4EBE184392358F1CBFE1D7C772A62954759AB7BA563EF4E09B72A961D19C2B5870EEC69F6E28493FD3EF0CF8F97FC6584B75696675045015DB4AA95");
        capkBean.setLengthOfCAPKExponent("1");
        capkBean.setCAPKExponent("03");
        capkBean.setChecksumHash("498EF9E709EAF1A93F8AD35561504A109FB2DACB");
        capkBean.setCAPKExpDate("221231");
        list.add(capkBean);

        capkBean = new CAPKBean();
        capkBean.setRID("A000000228");
        capkBean.setCA_PKIndex("20");
        capkBean.setCA_HashAlgoIndicator("01");
        capkBean.setCA_PKAlgoIndicator("01");
        capkBean.setLengthOfCAPKModulus("AO");
        capkBean.setCAPKModulus("EBA2E3FBE75D51C519B7A498CFE53F51519B292C1BBE0C78C14FBE38E3717DE0C0ECC04605879EF617B97ED1E8E989FCD2C7DDF61EE09E96F7EADA7D9F553426D6D6A8BE4DCF943D6C8F3627265520F757BBA16FF68749F3D796A0AAACA0ED0929BE112BF7CAB87BED4D9B5DD9B7A0CAC7F9CA513A6BFC03B4C20EFDC03E2B58D76E2ABF466665CB9D64AA412FBBF85259C480DA2F0896AB28FBB26022EFAE74CCDB9C36749E8D29AE4069A1298A0B07A7F72DFF8E6F442A2393DFF7E4E1F06F");
        capkBean.setLengthOfCAPKExponent("1");
        capkBean.setCAPKExponent("03");
        capkBean.setChecksumHash("48AE1E709DBBBDC9CEED5B5FEEE233CD1248CA70");
        capkBean.setCAPKExpDate("241231");
        list.add(capkBean);

        capkBean = new CAPKBean();
        capkBean.setRID("A000000228");
        capkBean.setCA_PKIndex("21");
        capkBean.setCA_HashAlgoIndicator("01");
        capkBean.setCA_PKAlgoIndicator("01");
        capkBean.setLengthOfCAPKModulus("8W");
        capkBean.setCAPKModulus("863B43586D710D2ECCF922644ACDD7015057A26BE7D6999A65D023DE94CD81A171E93C5BAB92C753767A4720C2ACFBF358387790CCD437806F9C1F19CF66FCF20BF42570FFE21ED742608F56C9CB0B4F277CF8EF3394C8BC595B314044197B7AAEAADBF1E44D763CDFE3DF368CBA6B09788F8EAEF9B47DEC02BEA131D58551430621D71AA5EEDE29FC1AC8CBE44CE92177E01EEBC080E94BEAD5FA0CAEF4B487");
        capkBean.setLengthOfCAPKExponent("1");
        capkBean.setCAPKExponent("03");
        capkBean.setChecksumHash("64D1FDA19D5E0893A6A7A31987805BB9A3D19B8B");
        capkBean.setCAPKExpDate("241231");
        list.add(capkBean);

        capkBean = new CAPKBean();
        capkBean.setRID("A000000228");
        capkBean.setCA_PKIndex("22");
        capkBean.setCA_HashAlgoIndicator("01");
        capkBean.setCA_PKAlgoIndicator("01");
        capkBean.setLengthOfCAPKModulus("AO");
        capkBean.setCAPKModulus("9CC38384539AFFDB955ADF9FCF03A6A669B5F68D91DBE1562002D4617E75FF0BEF16731D8CAE5B6E690E00C0F106BD8EB127DAD03108D40576C95572B3C43E4A9641C11C4C5AED06643543CD02B6E3811FBC4F72956CA9D8641374CFB659263B8B22B5C5A3B624E99BF5CBFD34B99A069DA312B1F7C03CF8F4CFA91BB269DBD565073A9AFCB1DA7D839F13D43B57F924BB85E1BCCE28BE5A8EC03AA56FED231B13B920A3BA4227F53F927EC27F9DB20C32EB2AD8F9C0770EC97D930764E844C9");
        capkBean.setLengthOfCAPKExponent("1");
        capkBean.setCAPKExponent("03");
        capkBean.setChecksumHash("E924B09D66B00B7811678092DF8F95D460F4CC47");
        capkBean.setCAPKExpDate("241231");
        list.add(capkBean);

        capkBean = new CAPKBean();
        capkBean.setRID("A000000228");
        capkBean.setCA_PKIndex("23");
        capkBean.setCA_HashAlgoIndicator("01");
        capkBean.setCA_PKAlgoIndicator("01");
        capkBean.setLengthOfCAPKModulus("CG");
        capkBean.setCAPKModulus("EEEACE9FD905E43DD1EC43F471570F9255379E43813267F0C6C0363977C607E8E169B834A5072977ADA9BDF4D0F50748F3D2DED1F863A9A510C4D67BC923EB53E77711BB079B32F2837F1381F141B27B9361E67DDB5AEF107F05231042A9D003DA49338476FBA2E8FFD8D48621C830A6BAB87751570BEAB77AA501846E8F9EDE25EAD306C45AA21CEEB506E5256AADB01AAA0A5C5773DB7A75DBFB5D1EA30C89BFCB4937C0B1B6EDADFF12F9808F1E9129A39AC6996C7D9E551BD1AF924320D965BC0726AD9F9CE430415F1FDF9AC37C3DC0454452D73F0E0B1CBC8214522F5F");
        capkBean.setLengthOfCAPKExponent("1");
        capkBean.setCAPKExponent("03");
        capkBean.setChecksumHash("6DE12BDA2B42F9950578C2C50A94A216636D6045");
        capkBean.setCAPKExpDate("251231");
        list.add(capkBean);

        capkBean = new CAPKBean();
        capkBean.setRID("A000000228");
        capkBean.setCA_PKIndex("28");
        capkBean.setCA_HashAlgoIndicator("01");
        capkBean.setCA_PKAlgoIndicator("01");
        capkBean.setLengthOfCAPKModulus("9S");
        capkBean.setCAPKModulus("C05B993401063615EF211036DD8154066BE4BBEC7E93A82E83C65E2CFD76E498A6DBF6135C816F606B9564A30D259A9FD5463AA78261223FBB4718EAD4A17347E11BE475BF0DDD9BE9315DCF585D58863A7BCA0E67440586DA098E33047C0DF6F6A1D1BD081BF283321DDF248FDFFA9FB749D0FDA47ADE2E7C0AAA76B146A00A5EBDA270C52832E8132FBC631EAC1120F02215829EB1D852B1969F1C1504A659AB6057C92AF92D981C8171B68E3300E3");
        capkBean.setLengthOfCAPKExponent("2");
        capkBean.setCAPKExponent("03");
        capkBean.setChecksumHash("F1B99C16FF415746FF423241E66F17AF0235B2C2");
        capkBean.setCAPKExpDate("241231");
        list.add(capkBean);

        capkBean = new CAPKBean();
        capkBean.setRID("A000000228");
        capkBean.setCA_PKIndex("29");
        capkBean.setCA_HashAlgoIndicator("01");
        capkBean.setCA_PKAlgoIndicator("01");
        capkBean.setLengthOfCAPKModulus("DS");
        capkBean.setCAPKModulus("B6D73BF68564C88A1AEE8BA70A5F60CE495CA722E097DADEEBB83B28040B1BAD16DBC9AC3CD181BA89193638E600AF397D220F0339A8E792AA08C1878482ACC463B3B3A257AE8667CDBC1D6613CB9CBB612830FDA7F7BA689A148EFFF34476F6E0A70C819C10B3B6150909B58BF9403F5BB2E9790EE82C50C8D6FB267C726DC255AE97FABF5A357B2A0FBD1387168D83B25ECD912027B3868F072E025240CF780CC8E5839823727E5547FD1366A203F4F70FA82660B8401D4D2D06FD9A4036D14C53F6289D6FDC724E7D06F31ED93AC1B54083D9B9FCF09B135FDE9F4F6C1F0BA0142C3715E49015958C45315859DB12D942D75497FB51ED");
        capkBean.setLengthOfCAPKExponent("1");
        capkBean.setCAPKExponent("03");
        capkBean.setChecksumHash("08374162F808F8DAD0CECB8FCD1AF5F64F213D6F");
        capkBean.setCAPKExpDate("241231");
        list.add(capkBean);

        capkBean = new CAPKBean();
        capkBean.setRID("A000000228");
        capkBean.setCA_PKIndex("33");
        capkBean.setCA_HashAlgoIndicator("01");
        capkBean.setCA_PKAlgoIndicator("01");
        capkBean.setLengthOfCAPKModulus("DS");
        capkBean.setCAPKModulus("B40F44F511A2F54000A3326EDBF0E1CD2C4E8EF429A6C28C2014D5066488EE1D8EEF92F816690FEEDA4961DE12EDB366D6008D90CFBB72C1314F6FA37FD7BD8D96C9D61420AAC638862C1FF0A42C0678D3D6FB2DCBA4282D2BA78831899F6FE5F34A3086F959EAC6C734CCE18064F69B68E36901199E265631EB0129F17FFD175BB4EA59D84AFB916EC609492FB1AAD7431A6748F85B95686243E59A71D4E9332904D1FA16A134E52C85E59D6C26AE6A4EA32ED9FD1D0EFFACC6B3D2A7E102AA61BDD375594799BC7D41884C3F8F6C7D0125FC7C3AC13E4DF7488FC15352CC0953E6FDF073C2127AB7BA0E5FDB7C1B2852B5C3AEC89F89C5");
        capkBean.setLengthOfCAPKExponent("1");
        capkBean.setCAPKExponent("03");
        capkBean.setChecksumHash("ED535A86C7A766CDAA07EA8766C7A2B688B7F4F5");
        capkBean.setCAPKExpDate("301231");
        list.add(capkBean);

        capkBean = new CAPKBean();
        capkBean.setRID("A000000333");
        capkBean.setCA_PKIndex("08");
        capkBean.setCA_HashAlgoIndicator("01");
        capkBean.setCA_PKAlgoIndicator("01");
        capkBean.setLengthOfCAPKModulus("80");
        capkBean.setCAPKModulus("B61645EDFD5498FB246444037A0FA18C0F101EBD8EFA54573CE6E6A7FBF63ED21D66340852B0211CF5EEF6A1CD989F66AF21A8EB19DBD8DBC3706D135363A0D683D046304F5A836BC1BC632821AFE7A2F75DA3C50AC74C545A754562204137169663CFCC0B06E67E2109EBA41BC67FF20CC8AC80D7B6EE1A95465B3B2657533EA56D92D539E5064360EA4850FED2D1BF");
        capkBean.setLengthOfCAPKExponent("1");
        capkBean.setCAPKExponent("03");
        capkBean.setChecksumHash("EE23B616C95C02652AD18860E48787C079E8E85A");
        capkBean.setCAPKExpDate("241231");
        list.add(capkBean);

        capkBean = new CAPKBean();
        capkBean.setRID("A000000333");
        capkBean.setCA_PKIndex("0A");
        capkBean.setCA_HashAlgoIndicator("01");
        capkBean.setCA_PKAlgoIndicator("01");
        capkBean.setLengthOfCAPKModulus("74");
        capkBean.setCAPKModulus("B2AB1B6E9AC55A75ADFD5BBC34490E53C4C3381F34E60E7FAC21CC2B26DD34462B64A6FAE2495ED1DD383B8138BEA100FF9B7A111817E7B9869A9742B19E5C9DAC56F8B8827F11B05A08ECCF9E8D5E85B0F7CFA644EFF3E9B796688F38E006DEB21E101C01028903A06023AC5AAB8635F8E307A53AC742BDCE6A283F585F48EF");
        capkBean.setLengthOfCAPKExponent("1");
        capkBean.setCAPKExponent("03");
        capkBean.setChecksumHash("C88BE6B2417C4F941C9371EA35A377158767E4E3");
        capkBean.setCAPKExpDate("241231");
        list.add(capkBean);

        capkBean = new CAPKBean();
        capkBean.setRID("A000000333");
        capkBean.setCA_PKIndex("0B");
        capkBean.setCA_HashAlgoIndicator("01");
        capkBean.setCA_PKAlgoIndicator("01");
        capkBean.setLengthOfCAPKModulus("DS");
        capkBean.setCAPKModulus("CF9FDF46B356378E9AF311B0F981B21A1F22F250FB11F55C958709E3C7241918293483289EAE688A094C02C344E2999F315A72841F489E24B1BA0056CFAB3B479D0E826452375DCDBB67E97EC2AA66F4601D774FEAEF775ACCC621BFEB65FB0053FC5F392AA5E1D4C41A4DE9FFDFDF1327C4BB874F1F63A599EE3902FE95E729FD78D4234DC7E6CF1ABABAA3F6DB29B7F05D1D901D2E76A606A8CBFFFFECBD918FA2D278BDB43B0434F5D45134BE1C2781D157D501FF43E5F1C470967CD57CE53B64D82974C8275937C5D8502A1252A8A5D6088A259B694F98648D9AF2CB0EFD9D943C69F896D49FA39702162ACB5AF29B90BADE005BC157");
        capkBean.setLengthOfCAPKExponent("1");
        capkBean.setCAPKExponent("03");
        capkBean.setChecksumHash("BD331F9996A490B33C13441066A09AD3FEB5F66C");
        capkBean.setCAPKExpDate("241231");
        list.add(capkBean);

        return list;
    }
}
