package com.appzonegroup.app.fasttrack.receipt

import android.content.Context
import com.appzonegroup.creditclub.pos.printer.LogoNode
import com.creditclub.pos.printer.PrintNode
import com.appzonegroup.creditclub.pos.printer.footerNodes
import com.appzonegroup.creditclub.pos.receipt.TransactionReceipt
import com.creditclub.core.data.response.CollectionPaymentResponse

class CollectionPaymentReceipt(context: Context, val response: CollectionPaymentResponse) :
    TransactionReceipt(context) {

    override val nodes: List<PrintNode>
        get() {
            return mutableListOf<PrintNode>(
                LogoNode()

//                TextNode(
//                    """
//Agent Code: ${context.localStorage.agent?.agentCode}
//Agent Phone: ${context.localStorage.agentPhone}
//--------------------------
//Amount NGN${response.amount}
//
//Payment Item: ${response.paymentItemName}
//Collection Type ${response.billerName}
//
//Customer ID: ${response.customerId}
//Reference Name: ${response.referenceName}
//
//Reference: ${response.collectionReference}
//Transaction Date: ${response.date.toString("dd-MM-YYYY hh:mm")}
//"""
//                )
            ).apply { addTransactionStatus(); addAll(footerNodes) }.toList()
        }
}