package com.appzonegroup.creditclub.pos.provider.qpos.xmlparse

import android.text.TextUtils
import com.dspread.xpos.EmvAppTag
import com.dspread.xpos.EmvCapkTag
import org.xml.sax.Attributes
import org.xml.sax.SAXException
import org.xml.sax.helpers.DefaultHandler
import java.util.*

/**
 * Created by dsppc11 on 2018/7/31.
 */
class SAXParserHandler : DefaultHandler() {
    var value = StringBuffer()
    private val appList: MutableList<TagApp?> = ArrayList()
    private val capkList: MutableList<TagCapk?> = ArrayList()
    private var tagApp: TagApp? = null
    private var tagCapk: TagCapk? = null
    fun getAppList(): List<TagApp?> {
        return appList
    }

    fun getCapkList(): List<TagCapk?> {
        return capkList
    }

    private var appIndex = 0
    private var capkIndex = 0
    private var paresTagApp = false
    private var paresTagCapk = false
    /**
     * 用来标识解析开始
     */
    @Throws(SAXException::class)
    override fun startDocument() { // TODO Auto-generated method stub
        super.startDocument()
        println("SAX解析开始")
    }

    /**
     * 用来标识解析结束
     */
    @Throws(SAXException::class)
    override fun endDocument() { // TODO Auto-generated method stub
        super.endDocument()
        println("SAX解析结束")
    }

    /**
     * 解析xml元素
     */
    @Throws(SAXException::class)
    override fun startElement(
        uri: String, localName: String, qName: String,
        attributes: Attributes
    ) { //调用DefaultHandler类的startElement方法
        super.startElement(uri, localName, qName, attributes)
        value.delete(0, value.length)
        if (qName == "app") {
            appIndex++
            tagApp = TagApp()
            paresTagApp = true
        } else if (qName == "capk") {
            capkIndex++
            tagCapk = TagCapk()
            paresTagCapk = true
        }
    }

    @Throws(SAXException::class)
    override fun endElement(
        uri: String,
        localName: String,
        qName: String
    ) { //调用DefaultHandler类的endElement方法
        super.endElement(uri, localName, qName)
        if (qName == "app") {
            appList.add(tagApp)
            tagApp = null
            paresTagApp = false
        } else if (qName == "capk") {
            capkList.add(tagCapk)
            tagCapk = null
            paresTagCapk = false
        } else if (qName.startsWith("_")) {
            matchDataAndKey(qName.substring(1, qName.length))
        }
    }

    private fun matchDataAndKey(qName: String) {
        val realValue = value.toString().trim { it <= ' ' }
        var concat: String? = null
        when (qName) {
            "9F06" -> {
                if (paresTagApp) {
                    concat = EmvAppTag.Application_Identifier_AID_terminal + realValue
                    tagApp!!.application_Identifier_AID_terminal = concat
                }
                if (paresTagCapk) {
                    concat = EmvCapkTag.RID + realValue
                    tagCapk?.rID = concat
                }
            }
            "9F22" -> {
                concat = EmvCapkTag.Public_Key_Index + realValue
                tagCapk?.public_Key_Index = (concat)
            }
            "DF02" -> {
                concat = EmvCapkTag.Public_Key_Module + realValue
                tagCapk?.public_Key_Module = (concat)
            }
            "DF03" -> {
                concat = EmvCapkTag.Public_Key_CheckValue + realValue
                tagCapk?.public_Key_CheckValue = (concat)
            }
            "DF04" -> {
                concat = EmvCapkTag.Pk_exponent + realValue
                tagCapk?.pk_exponent = (concat)
            }
            "DF05" -> {
                concat = EmvCapkTag.Expired_date + realValue
                tagCapk?.expired_date = (concat)
            }
            "DF06" -> {
                concat = EmvCapkTag.Hash_algorithm_identification + realValue
                tagCapk?.hash_algorithm_identification = (concat)
            }
            "DF07" -> {
                concat = EmvCapkTag.Pk_algorithm_identification + realValue
                tagCapk?.pk_algorithm_identification = (concat)
            }
            "9F15" -> {
                concat = EmvAppTag.Merchant_Category_Code + realValue
                tagApp!!.merchant_Category_Code = concat
            }
            "9F09" -> {
                concat = EmvAppTag.Application_Version_Number + realValue
                tagApp!!.application_Version_Number = concat
            }
            "9F01" -> {
                concat = EmvAppTag.Acquirer_Identifier + realValue
                tagApp!!.acquirer_Identifier = concat
            }
            "5F36" -> {
                concat = EmvAppTag.Transaction_Currency_Exponent + realValue
                tagApp!!.transaction_Currency_Exponent = concat
            }
            "9F1E" -> {
                concat = EmvAppTag.Interface_Device_IFD_Serial_Number + realValue
                tagApp!!.interface_Device_IFD_Serial_Number = concat
            }
            "9F1C" -> {
                concat = EmvAppTag.Terminal_Identification + realValue
                tagApp!!.terminal_Identification = concat
            }
            "9F1B" -> {
                concat = EmvAppTag.Terminal_Floor_Limit + realValue
                tagApp!!.terminal_Floor_Limit = concat
            }
            "9F1A" -> {
                concat = EmvAppTag.Terminal_Country_Code + realValue
                tagApp!!.terminal_Country_Code = concat
            }
            "9F16" -> {
                concat = EmvAppTag.Merchant_Identifier + realValue
                tagApp!!.merchant_Identifier = concat
            }
            "9F33" -> {
                concat =
                    EmvAppTag.Terminal_Cterminal_contactless_transaction_limitapabilities + realValue
                tagApp!!.terminal_Cterminal_contactless_transaction_limitapabilities = concat
            }
            "9F3D" -> {
                concat = EmvAppTag.Transaction_Reference_Currency_Exponent + realValue
                tagApp!!.transaction_Reference_Currency_Exponent = concat
            }
            "9F3C" -> {
                concat = EmvAppTag.Transaction_Reference_Currency_Code + realValue
                tagApp!!.transaction_Reference_Currency_Code = concat
            }
            "9F39" -> {
                concat = EmvAppTag.Point_of_Service_POS_EntryMode + realValue
                tagApp!!.point_of_Service_POS_EntryMode = concat
            }
            "9F35" -> {
                concat = EmvAppTag.Terminal_type + realValue
                tagApp!!.terminal_type = concat
            }
            "9F40" -> {
                concat = EmvAppTag.Additional_Terminal_Capabilities + realValue
                tagApp!!.additional_Terminal_Capabilities = concat
            }
            "9F4E" -> {
                concat = EmvAppTag.Merchant_Name_and_Location + realValue
                tagApp!!.merchant_Name_and_Location = concat
            }
            "9F66" -> {
                concat = EmvAppTag.Terminal_Default_Transaction_Qualifiers + realValue
                tagApp!!.terminal_Default_Transaction_Qualifiers = concat
            }
            "DF13" -> {
                concat = EmvAppTag.TAC_Denial + realValue
                tagApp!!.tAC_Denial = concat
            }
            "DF12" -> {
                concat = EmvAppTag.TAC_Online + realValue
                tagApp!!.tAC_Online = concat
            }
            "DF11" -> {
                concat = EmvAppTag.TAC_Default + realValue
                tagApp!!.tAC_Default = concat
            }
            "DF01" -> {
                concat = EmvAppTag.Application_Selection_Indicator + realValue
                tagApp!!.application_Selection_Indicator = concat
            }
            "9F7B" -> {
                concat = EmvAppTag.Electronic_cash_Terminal_Transaction_Limit + realValue
                tagApp!!.electronic_cash_Terminal_Transaction_Limit = concat
            }
            "9F73" -> {
                concat = EmvAppTag.Currency_conversion_factor + realValue
                tagApp!!.currency_conversion_factor = concat
            }
            "DF15" -> {
                concat = EmvAppTag.Threshold_Value_BiasedRandom_Selection + realValue
                tagApp!!.threshold_Value_BiasedRandom_Selection = concat
            }
            "DF14" -> {
                concat = EmvAppTag.Default_DDOL + realValue
                tagApp!!.default_DDOL = concat
            }
            "DF16" -> {
                concat =
                    EmvAppTag.Maximum_Target_Percentage_to_be_used_for_Biased_Random_Selection + realValue
                tagApp!!.maximum_Target_Percentage_to_be_used_for_Biased_Random_Selection = concat
            }
            "DF17" -> {
                concat = EmvAppTag.Target_Percentage_to_be_Used_for_Random_Selection + realValue
                tagApp!!.target_Percentage_to_be_Used_for_Random_Selection = concat
            }
            "DF19" -> {
                concat = EmvAppTag.terminal_contactless_offline_floor_limit + realValue
                tagApp!!.terminal_contactless_offline_floor_limit = concat
            }
            "DF20" -> {
                concat = EmvAppTag.terminal_contactless_transaction_limit + realValue
                tagApp!!.terminal_contactless_transaction_limit = concat
            }
            "DF21" -> {
                concat = EmvAppTag.terminal_execute_cvm_limit + realValue
                tagApp!!.terminal_execute_cvm_limit = concat
            }
            "DF78" -> {
                concat = EmvAppTag.Contactless_CVM_Required_limit + realValue
                tagApp!!.contactless_CVM_Required_limit = concat
            }
            "DF70" -> {
                concat = EmvAppTag.Currency_Exchange_Transaction_Reference + realValue
                tagApp!!.currency_Exchange_Transaction_Reference = concat
            }
            "DF71" -> {
                concat = EmvAppTag.Script_length_Limit + realValue
                tagApp!!.script_length_Limit = concat
            }
            "DF72" -> {
                concat = EmvAppTag.ICS + realValue
                tagApp!!.iCS = concat
            }
            "DF73" -> {
                concat = EmvAppTag.status + realValue
                tagApp!!.status = concat
            }
            "DF74" -> {
                concat = EmvAppTag.Identity_of_each_limit_exist + realValue
                tagApp!!.identity_of_each_limit_exist = concat
            }
            "DF75" -> {
                concat = EmvAppTag.terminal_status_check + realValue
                tagApp!!.terminal_status_check = concat
            }
            "DF79" -> {
                concat = EmvAppTag.ContactlessTerminal_Capabilities + realValue
                tagApp!!.contactlessTerminal_Capabilities = concat
            }
            "5F2A" -> {
                concat = EmvAppTag.Transaction_Currency_Code + realValue
                tagApp!!.transaction_Currency_Code = concat
            }
            "DF7A" -> {
                concat = EmvAppTag.ContactlessAdditionalTerminal_Capabilities + realValue
                tagApp!!.contactlessAdditionalTerminal_Capabilities = concat
            }
            "DF76" -> {
                concat = EmvAppTag.Default_Tdol + realValue
                tagApp!!.default_Tdol = concat
            }
        }
        if (TextUtils.isEmpty(concat)) concat = qName
        if (paresTagApp) tagApp?.addData(concat)
        if (paresTagCapk) tagCapk?.addData(concat)
    }

    @Throws(SAXException::class)
    override fun characters(
        ch: CharArray,
        start: Int,
        length: Int
    ) { // TODO Auto-generated method stub
        super.characters(ch, start, length)
        value.append(ch, start, length)
    }
}