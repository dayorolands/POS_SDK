package com.dspread.qpos.xmlparse

import com.dspread.xpos.EmvAppTag
import com.dspread.xpos.EmvCapkTag
import java.util.*

/**
 * Created by dsppc11 on 2018/8/1.
 */
open class BaseTag {
    private val fields: MutableList<String?>? = ArrayList()

    fun addData(value: String?) {
        fields!!.add(value)
    }

    fun getData(i: Int): String? {
        return fields!![i]
    }

    val datasLength: Int
        get() = fields?.size ?: 0

    companion object {
        const val EMV_APP = "emvApp"
        const val EMV_CAPK = "emvCapk"
        val TAG_APP = arrayOf(
            EmvAppTag.Application_Identifier_AID_terminal,
            EmvAppTag.Transaction_Currency_Code,
            EmvAppTag.Transaction_Currency_Exponent,
            EmvAppTag.Acquirer_Identifier,
            EmvAppTag.Application_Version_Number,
            EmvAppTag.Merchant_Category_Code,
            EmvAppTag.Merchant_Identifier,
            EmvAppTag.Terminal_Country_Code,
            EmvAppTag.Terminal_Floor_Limit,
            EmvAppTag.Terminal_Identification,
            EmvAppTag.Interface_Device_IFD_Serial_Number,  //            EmvAppTag.Terminal_Cterminal_contactless_transaction_limitapabilities,
            EmvAppTag.Terminal_type,
            EmvAppTag.Point_of_Service_POS_EntryMode,
            EmvAppTag.Transaction_Reference_Currency_Code,
            EmvAppTag.Transaction_Reference_Currency_Exponent,
            EmvAppTag.Additional_Terminal_Capabilities,
            EmvAppTag.Merchant_Name_and_Location,
            EmvAppTag.Terminal_Default_Transaction_Qualifiers,
            EmvAppTag.Currency_conversion_factor,
            EmvAppTag.Electronic_cash_Terminal_Transaction_Limit,
            EmvAppTag.Application_Selection_Indicator,
            EmvAppTag.TAC_Default,
            EmvAppTag.TAC_Online,
            EmvAppTag.TAC_Denial,
            EmvAppTag.Default_DDOL,
            EmvAppTag.Threshold_Value_BiasedRandom_Selection,
            EmvAppTag.Maximum_Target_Percentage_to_be_used_for_Biased_Random_Selection,
            EmvAppTag.Target_Percentage_to_be_Used_for_Random_Selection,
            EmvAppTag.terminal_contactless_offline_floor_limit,
            EmvAppTag.terminal_contactless_transaction_limit,
            EmvAppTag.terminal_execute_cvm_limit,
            EmvAppTag.Contactless_CVM_Required_limit,
            EmvAppTag.Currency_Exchange_Transaction_Reference,
            EmvAppTag.Script_length_Limit,
            EmvAppTag.ICS,
            EmvAppTag.status,
            EmvAppTag.Identity_of_each_limit_exist,
            EmvAppTag.terminal_status_check,
            EmvAppTag.Default_Tdol,
            EmvAppTag.ContactlessTerminal_Capabilities,
            EmvAppTag.ContactlessAdditionalTerminal_Capabilities
        )
        val TAG_CAPK = arrayOf(
            EmvCapkTag.RID,
            EmvCapkTag.Public_Key_Index,
            EmvCapkTag.Public_Key_Module,
            EmvCapkTag.Public_Key_CheckValue,
            EmvCapkTag.Pk_exponent,
            EmvCapkTag.Expired_date,
            EmvCapkTag.Hash_algorithm_identification,
            EmvCapkTag.Pk_algorithm_identification
        )
    }
}