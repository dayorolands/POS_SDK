package com.dspread.qpos.utils

import org.xmlpull.v1.XmlSerializer
import java.io.StringWriter

//  XML generation by code
inline fun XmlSerializer.document(
    docName: String = "UTF-8",
    xmlStringWriter: StringWriter = StringWriter(),
    standalone: Boolean = false,
    crossinline init: XmlSerializer.() -> Unit
): String {
//    startDocument(docName, standalone)
    xmlStringWriter.buffer.setLength(0) //  refreshing string writer due to reuse
    setOutput(xmlStringWriter)
    init()
    endDocument()
    return xmlStringWriter.toString()
}

//  element
inline fun XmlSerializer.element(name: String, crossinline init: XmlSerializer.() -> Unit) {
    startTag("", name)
    init()
    endTag("", name)
}

//  element with attribute & content
inline fun XmlSerializer.element(
    name: String,
    content: String,
    crossinline init: XmlSerializer.() -> Unit
) {
    startTag("", name)
    init()
    text(content)
    endTag("", name)
}

//  element with content
fun XmlSerializer.element(name: String, content: String) =
    element(name) {
        text(content)
    }

//  attribute
fun XmlSerializer.attribute(name: String, value: String): XmlSerializer =
    attribute("", name, value)