package com.appzonegroup.creditclub.pos.util

import com.appzonegroup.creditclub.pos.PosActivity
import com.appzonegroup.creditclub.pos.CardWithdrawalActivity
import com.appzonegroup.creditclub.pos.R
import com.appzonegroup.creditclub.pos.data.PosDatabase
import com.appzonegroup.creditclub.pos.command.PrintEOD
import com.appzonegroup.creditclub.pos.models.FinancialTransaction
import com.appzonegroup.creditclub.pos.printer.PrinterStatus
import com.appzonegroup.creditclub.pos.printer.Receipt
import com.appzonegroup.creditclub.pos.widget.Dialogs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 1/3/2019.
 * Appzone Ltd
 */

object Modules {
    const val PURCHASE = 0
    const val REPRINT_LAST = 1
    const val REPRINT_BY_STAN = 2

    const val PRINT_EOD = 3
    const val EOD_BY_DATE = 4
    const val PRINT_SUMMARY = 5
    const val PRINT_TOTAL = 6

//    const val LOGON = 7
    const val DOWNLOAD_PARAMETER = 8
//    const val DOWNLOAD_BILL = 9
//    const val PRINT_LIBS = 10
//    const val BALANCE = 11
    const val PRINT_PARAMETER = 12
//    const val DOWNLOAD_AID = 13
    const val DOWN_CAPK = 14
    const val NETWORK_PARAMETERS = 15
    const val KEY_DOWNLOAD = 16
    const val PRINT_CONFIG = 17

    private val MODULE_MAP = hashMapOf<Int, ActionButton>()

    operator fun get(id: Int) = MODULE_MAP[id]!!

    init {
        MODULE_MAP[PURCHASE] = actionButton {
            id = R.id.purchase_button
            icon = R.drawable.ic_purchase
            name = "Purchase"
            activityClass = CardWithdrawalActivity::class.java
        }

        MODULE_MAP[REPRINT_LAST] = actionButton {
            id = R.id.reprint_last_button
            name = "Reprint Last"

            onClick { activity ->
                PosDatabase.open(activity, Dispatchers.Main) { db ->
                    val trn = withContext(Dispatchers.Default) {
                        db.financialTransactionDao().lastTransaction()
                    } ?: return@open activity.showError("No transactions")

                    printReceipt(trn, activity)
                }
            }

        }

        MODULE_MAP[REPRINT_BY_STAN] = actionButton {
            id = R.id.reprint_by_stan_button
            name = "Reprint By Stan"

            onClick { activity ->
                Dialogs.input(activity, "STAN") {
                    onSubmit { stan ->
                        dismiss()
                        PosDatabase.open(activity, Dispatchers.Main) { db ->
                            val trn = withContext(Dispatchers.Default) {
                                db.financialTransactionDao().byStan(stan)
                            } ?: return@open activity.showError("Transaction not found")
                            printReceipt(trn, activity)
                        }
                    }
                }.show()
            }
        }

        MODULE_MAP[PRINT_EOD] = actionButton {
            name = "Print EOD"

            onClick { activity ->
                printEod(activity)
            }
        }

        MODULE_MAP[PRINT_TOTAL] = actionButton {
            name = "Print Total"
            onClick {

            }
        }

        MODULE_MAP[EOD_BY_DATE] = actionButton {
            name = "EOD by date"
            onClick { activity ->
                Dialogs.date(activity) {
                    onSubmit { dateValue ->
                        printEod(activity, "${dateValue[1]}${dateValue[0]}")
                    }
                }.show()
            }
        }

        MODULE_MAP[PRINT_SUMMARY] = actionButton {
            name = "Print Summary"
        }
    }

    private fun printReceipt(trn: FinancialTransaction, activity: PosActivity) {
        val receipt = Receipt(activity, trn)
        receipt.isReprint = true

        if (trn.type == "BALANCE INQUIRY") {
            receipt.isCustomerCopy = true

            activity.printer.printAsync(receipt) { printerStatus ->
                if (printerStatus != PrinterStatus.READY) return@printAsync activity.showError(printerStatus.message)
            }

            return
        }

        receipt.isCustomerCopy = false
        activity.run {
            printer.printAsync(receipt) { printerStatus ->
                if (printerStatus != PrinterStatus.READY) return@printAsync activity.showError(printerStatus.message)

                Dialogs.confirm(activity, "Print Customer Copy?", "") {
                    onSubmit {
                        dismiss()
                        receipt.isCustomerCopy = true
                        printer.printAsync(receipt) { printerStatus ->
                            if (printerStatus != PrinterStatus.READY) activity.showError(printerStatus.message)
                        }
                    }
                }.show()
            }
        }
    }

    private fun printEod(activity: PosActivity, localDate: String = TransmissionDateParams().localDate) {
        PrintEOD(activity, activity.isoSocketHelper, activity, localDate).run()
    }
}