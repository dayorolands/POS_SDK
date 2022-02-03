package com.dspread.qpos

import com.cluster.pos.card.CardData
import com.dspread.qpos.utils.TLV

class QposCardData : CardData() {
    private var tlvList: List<TLV>? = null

    fun setIccTlv(tlv: List<TLV>) {
        tlvList = tlv
    }

    override val iccString: String?
        get() = tlvList?.run {
            val tags = listOf(
                "82",
                "84",
                "95",
                "9F26",
                "9F27",
                "9F10",
                "9F37",
                "9F36",
                "9A",
                "9C",
                "9F02",
                "9F03",
                "5F2A",
                "9F1A",
                "9F33",
                "9F34",
                "9F35",
                "9F09",
                "9F41"
            )
            val iccData = StringBuilder()
            for (tag in tags) {
                iccData.append(calculateTlv(tag))
            }
            return iccData.toString()
        }

    private fun List<TLV>.calculateTlv(tag: String): String {
        val value = getValue(tag)
        return String.format("%s%02x%s", tag, value.length / 2, value)
    }
}