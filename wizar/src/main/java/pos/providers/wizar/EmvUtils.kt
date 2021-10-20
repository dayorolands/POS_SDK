package pos.providers.wizar

import com.cloudpos.jniinterface.EMVJNIInterface
import com.creditclub.pos.extensions.hexBytes
import com.wizarpos.util.ByteUtil

fun getTagValue(tag: Int, hex: Boolean = false, fPadded: Boolean = false): String {
    val tagData = ByteArray(50)
    val tagDataLength = EMVJNIInterface.emv_get_tag_data(tag, tagData, tagData.size)
    val rawValue = ByteUtil.arrayToHexStr(tagData, tagDataLength)

    val value = when {
        hex -> String(rawValue.hexBytes)
        else -> rawValue
    }

    if (fPadded) {
        val stringBuffer = StringBuffer(value)
        if (stringBuffer[stringBuffer.toString().length - 1] == 'F') {
            stringBuffer.deleteCharAt(stringBuffer.toString().length - 1)
        }
        return stringBuffer.toString()
    }

    return value
}