package com.appzonegroup.app.fasttrack.receipt

import android.content.Context
import com.appzonegroup.creditclub.pos.printer.LogoNode
import com.appzonegroup.creditclub.pos.receipt.TransactionReceipt
import com.creditclub.core.data.response.CollectionPaymentResponse
import com.creditclub.core.util.format
import com.creditclub.core.util.localStorage
import com.creditclub.pos.printer.PrintNode
import com.creditclub.pos.printer.TextNode

class CollectionPaymentReceipt(context: Context, val response: CollectionPaymentResponse) :
    TransactionReceipt(context) {

    override val nodes: List<PrintNode>
        get() {
            return mutableListOf(
                LogoNode(),

                TextNode(
                    """
Agent Code: ${context.localStorage.agent?.agentCode}
Agent Phone: ${context.localStorage.agentPhone}
--------------------------
Amount NGN${response.amount}

Category ${response.collectionCategoryName}
Payment Item: ${response.collectionPaymentItemName}

Reference: ${response.collectionReference}
Transaction Date: ${response.date?.format("dd-MM-YYYY hh:mm")}
"""
                )
            ).apply { addTransactionStatus(); addAll(footerNodes(context)) }.toList()
        }
}