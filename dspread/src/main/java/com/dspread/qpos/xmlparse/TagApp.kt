package com.dspread.qpos.xmlparse

/**
 * Created by dsppc11 on 2018/7/31.
 */
class TagApp : BaseTag() {
    var application_Identifier_AID_terminal: String? = null
    var transaction_Currency_Code: String? = null
    var transaction_Currency_Exponent: String? = null
    var acquirer_Identifier: String? = null
    var application_Version_Number: String? = null
    var merchant_Category_Code: String? = null
    var merchant_Identifier: String? = null
    var terminal_Country_Code: String? = null
    var terminal_Floor_Limit: String? = null
    var terminal_Identification: String? = null
    var interface_Device_IFD_Serial_Number: String? = null
    var terminal_Cterminal_contactless_transaction_limitapabilities: String? = null
    var terminal_type: String? = null
    var point_of_Service_POS_EntryMode: String? = null
    var transaction_Reference_Currency_Code: String? = null
    var transaction_Reference_Currency_Exponent: String? = null
    var additional_Terminal_Capabilities: String? = null
    var merchant_Name_and_Location: String? = null
    var terminal_Default_Transaction_Qualifiers: String? = null
    var currency_conversion_factor: String? = null
    var electronic_cash_Terminal_Transaction_Limit: String? = null
    var application_Selection_Indicator: String? = null
    var tAC_Default: String? = null
    var tAC_Online: String? = null
    var tAC_Denial: String? = null
    var default_DDOL: String? = null
    var threshold_Value_BiasedRandom_Selection: String? = null
    var maximum_Target_Percentage_to_be_used_for_Biased_Random_Selection: String? =
        null
    var target_Percentage_to_be_Used_for_Random_Selection: String? = null
    var terminal_contactless_offline_floor_limit: String? = null
    var terminal_contactless_transaction_limit: String? = null
    var terminal_execute_cvm_limit: String? = null
    var contactless_CVM_Required_limit: String? = null
    var currency_Exchange_Transaction_Reference: String? = null
    var script_length_Limit: String? = null
    var iCS: String? = null
    var status: String? = null
    var identity_of_each_limit_exist: String? = null
    var terminal_status_check: String? = null
    var default_Tdol: String? = null
    var contactlessTerminal_Capabilities: String? = null
    var contactlessAdditionalTerminal_Capabilities: String? = null

    override fun toString(): String {
        return "TagApp{" +
                "Application_Identifier_AID_terminal='" + application_Identifier_AID_terminal + '\'' +
                ", Transaction_Currency_Code='" + transaction_Currency_Code + '\'' +
                ", Transaction_Currency_Exponent='" + transaction_Currency_Exponent + '\'' +
                ", Acquirer_Identifier='" + acquirer_Identifier + '\'' +
                ", Application_Version_Number='" + application_Version_Number + '\'' +
                ", Merchant_Category_Code='" + merchant_Category_Code + '\'' +
                ", Merchant_Identifier='" + merchant_Identifier + '\'' +
                ", Terminal_Country_Code='" + terminal_Country_Code + '\'' +
                ", Terminal_Floor_Limit='" + terminal_Floor_Limit + '\'' +
                ", Terminal_Identification='" + terminal_Identification + '\'' +
                ", Interface_Device_IFD_Serial_Number='" + interface_Device_IFD_Serial_Number + '\'' +
                ", Terminal_Cterminal_contactless_transaction_limitapabilities='" + terminal_Cterminal_contactless_transaction_limitapabilities + '\'' +
                ", Terminal_type='" + terminal_type + '\'' +
                ", Point_of_Service_POS_EntryMode='" + point_of_Service_POS_EntryMode + '\'' +
                ", Transaction_Reference_Currency_Code='" + transaction_Reference_Currency_Code + '\'' +
                ", Transaction_Reference_Currency_Exponent='" + transaction_Reference_Currency_Exponent + '\'' +
                ", Additional_Terminal_Capabilities='" + additional_Terminal_Capabilities + '\'' +
                ", Merchant_Name_and_Location='" + merchant_Name_and_Location + '\'' +
                ", Terminal_Default_Transaction_Qualifiers='" + terminal_Default_Transaction_Qualifiers + '\'' +
                ", Currency_conversion_factor='" + currency_conversion_factor + '\'' +
                ", Electronic_cash_Terminal_Transaction_Limit='" + electronic_cash_Terminal_Transaction_Limit + '\'' +
                ", Application_Selection_Indicator='" + application_Selection_Indicator + '\'' +
                ", TAC_Default='" + tAC_Default + '\'' +
                ", TAC_Online='" + tAC_Online + '\'' +
                ", TAC_Denial='" + tAC_Denial + '\'' +
                ", Default_DDOL='" + default_DDOL + '\'' +
                ", Threshold_Value_BiasedRandom_Selection='" + threshold_Value_BiasedRandom_Selection + '\'' +
                ", Maximum_Target_Percentage_to_be_used_for_Biased_Random_Selection='" + maximum_Target_Percentage_to_be_used_for_Biased_Random_Selection + '\'' +
                ", Target_Percentage_to_be_Used_for_Random_Selection='" + target_Percentage_to_be_Used_for_Random_Selection + '\'' +
                ", terminal_contactless_offline_floor_limit='" + terminal_contactless_offline_floor_limit + '\'' +
                ", terminal_contactless_transaction_limit='" + terminal_contactless_transaction_limit + '\'' +
                ", terminal_execute_cvm_limit='" + terminal_execute_cvm_limit + '\'' +
                ", Contactless_CVM_Required_limit='" + contactless_CVM_Required_limit + '\'' +
                ", Currency_Exchange_Transaction_Reference='" + currency_Exchange_Transaction_Reference + '\'' +
                ", Script_length_Limit='" + script_length_Limit + '\'' +
                ", ICS='" + iCS + '\'' +
                ", status='" + status + '\'' +
                ", Identity_of_each_limit_exist='" + identity_of_each_limit_exist + '\'' +
                ", terminal_status_check='" + terminal_status_check + '\'' +
                ", Default_Tdol='" + default_Tdol + '\'' +
                ", ContactlessTerminal_Capabilities='" + contactlessTerminal_Capabilities + '\'' +
                ", ContactlessAdditionalTerminal_Capabilities='" + contactlessAdditionalTerminal_Capabilities + '\'' +
                '}'
    }
}