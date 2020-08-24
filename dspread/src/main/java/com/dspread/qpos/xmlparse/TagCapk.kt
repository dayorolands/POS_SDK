package com.dspread.qpos.xmlparse

/**
 * Created by dsppc11 on 2018/7/31.
 */
class TagCapk : BaseTag() {

    var rID: String? = null
    var public_Key_Index: String? = null
    var public_Key_Module: String? = null
    var public_Key_CheckValue: String? = null
    var pk_exponent: String? = null
    var expired_date: String? = null
    var hash_algorithm_identification: String? = null
    var pk_algorithm_identification: String? = null

    override fun toString(): String {
        return "TagCapk{" +
                "RID='" + rID + '\'' +
                ", Public_Key_Index='" + public_Key_Index + '\'' +
                ", Public_Key_Module='" + public_Key_Module + '\'' +
                ", Public_Key_CheckValue='" + public_Key_CheckValue + '\'' +
                ", Pk_exponent='" + pk_exponent + '\'' +
                ", Expired_date='" + expired_date + '\'' +
                ", Hash_algorithm_identification='" + hash_algorithm_identification + '\'' +
                ", Pk_algorithm_identification='" + pk_algorithm_identification + '\'' +
                '}'
    }
}