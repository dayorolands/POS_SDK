package com.appzonegroup.creditclub.pos.util

import com.appzonegroup.creditclub.pos.PosActivity
import com.appzonegroup.creditclub.pos.R
import com.appzonegroup.creditclub.pos.TerminalOptionsActivity
import com.appzonegroup.creditclub.pos.command.printEOD
import com.appzonegroup.creditclub.pos.extension.apnInfo
import com.appzonegroup.creditclub.pos.extension.posParameter
import com.appzonegroup.creditclub.pos.models.FinancialTransaction
import com.appzonegroup.creditclub.pos.printer.Receipt
import com.cluster.core.ui.widget.DateInputParams
import com.cluster.core.ui.widget.TextFieldParams
import com.cluster.pos.printer.PrinterStatus
import com.cluster.pos.printer.printJob
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import kotlin.collections.set


/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 3/1/2019.
 * Appzone Ltd
 */

object MenuPages {
    const val REPRINT_EODS = 100
    const val REPRINT_ANY = 101
    const val ADMIN = 102
    const val REPRINT_LAST = 1

    private const val PRINT_PARAMETER = 12
    private const val NETWORK_PARAMETERS = 15
    private const val PRINT_CONFIG = 17

    private val PAGE_MAP = hashMapOf<Int, MenuPage>()

    operator fun get(id: Int) = PAGE_MAP[id]

    init {
        PAGE_MAP[REPRINT_ANY] = menuPage(
            id = REPRINT_ANY,
            icon = R.drawable.ic_purchase,
            name = "Reprint Any",
            options = lazy {
                listOf(
                    actionButton(
                        id = R.id.module_reprint_last,
                        name = "Reprint Last",
                        onClick = { activity ->
                            val trn = withContext(Dispatchers.IO) {
                                activity.posDatabase.financialTransactionDao().last()
                            } ?: return@actionButton activity.showError("No transactions")
                            printReceipt(trn, activity)
                        },
                    ),

                    actionButton(
                        id = R.id.module_reprint_by_stan,
                        name = "Reprint By STAN",
                        onClick = { activity ->
                            val params = TextFieldParams("STAN")
                            val stan =
                                activity.dialogProvider.getInput(params) ?: return@actionButton
                            val trn = withContext(Dispatchers.IO) {
                                activity.posDatabase.financialTransactionDao().findByStan(stan)
                            } ?: return@actionButton activity.showError("Transaction not found")
                            printReceipt(trn, activity)
                        },
                    ),
                )
            },
        )

        PAGE_MAP[REPRINT_EODS] = menuPage(
            id = REPRINT_EODS,
            name = "End of Day",
            options = lazy {
                listOf(
                    actionButton(
                        name = "Print EOD",
                        onClick = { activity ->
                            activity.mainScope.launch {
                                printEOD(
                                    context = activity,
                                    dialogProvider = activity.dialogProvider,
                                    localDate = LocalDate.now(),
                                    posPrinter = activity.printer
                                )
                            }
                        }
                    ),

                    actionButton(
                        name = "EOD by date",
                        onClick = { activity ->
                            val params = DateInputParams("EOD by date")
                            val localDate =
                                activity.dialogProvider.getDate(params) ?: return@actionButton
                            printEOD(
                                context = activity,
                                dialogProvider = activity.dialogProvider,
                                localDate = localDate,
                                posPrinter = activity.printer,
                            )
                        },
                    )
                )
            },
        )

        PAGE_MAP[ADMIN] = menuPage(
            isSecure = true,
            name = "Admin",
            options = lazy {
                listOf(
                    actionButton(
                        id = NETWORK_PARAMETERS,
                        name = "Settings",
                        icon = R.drawable.ic_settings_fancy,
                        activityClass = TerminalOptionsActivity::class.java,
                    ),

                    actionButton(
                        id = PRINT_CONFIG,
                        name = "PRINT CONFIGURATION",
                        onClick = ::printConfig,
                    ),

                    actionButton(
                        id = PRINT_PARAMETER,
                        name = "PRINT PARAMETER",
                        onClick = {
                            val parameters = it.posParameter.managementData
                            val printJob = printJob {
                                text(
                                    """
                                    |POS Parameters
                                    |--------------
                                    |
                                    |Card Acceptor ID: ${parameters.cardAcceptorId}
                                    |Card Acceptor Location: ${parameters.cardAcceptorLocation}
                                    |Country Code: ${parameters.countryCode}
                                    |""".trimMargin()
                                )
                                walkPaper(20)
                            }
                            val printerStatus = it.printer.print(printJob)
                            if (printerStatus != PrinterStatus.READY) {
                                it.showError(printerStatus.message)
                            }
                        },
                    )
                )
            },
        )
    }
}

private suspend fun printConfig(activity: PosActivity) {
    val config = activity.config
    val printJob = printJob {
        text(
            """
            |POS Configuration
            |-----------------
            |
            |POS Mode: ${config.remoteConnectionInfo.label}
            |APN: ${activity.apnInfo}
            |Host Name: ${config.host}
            |Terminal ID: ${config.terminalId}
            |IP: ${config.remoteConnectionInfo.host}
            |Port: ${config.remoteConnectionInfo.port}
            |Keep Alive (Call Home) in seconds: ${config.callHome}
            |""".trimMargin()
        )
        walkPaper(20)
    }
    val printerStatus = activity.printer.print(printJob = printJob)
    if (printerStatus != PrinterStatus.READY) {
        activity.dialogProvider.showError(printerStatus.message)
    }
}

private suspend fun printReceipt(trn: FinancialTransaction, activity: PosActivity) {
    val receipt = Receipt(context = activity, transaction = trn)
    receipt.isReprint = true

    if (trn.type == "BALANCE INQUIRY") {
        receipt.isCustomerCopy = true

        val printerStatus = activity.printer.print(receipt)
        if (printerStatus != PrinterStatus.READY) {
            activity.showError(printerStatus.message)
        }

        return
    }

    receipt.isCustomerCopy = false

    run {
        val printerStatus = activity.printer.print(receipt)
        if (printerStatus != PrinterStatus.READY) {
            activity.showError(printerStatus.message)
            return
        }
    }

    val printCustomerCopy = activity.dialogProvider.getConfirmation(
        title = "Print Customer Copy?",
        subtitle = "",
    )
    if (printCustomerCopy) {
        receipt.isCustomerCopy = true
        val printerStatus = activity.printer.print(receipt)
        if (printerStatus != PrinterStatus.READY) {
            activity.showError(printerStatus.message)
        }
    }
}