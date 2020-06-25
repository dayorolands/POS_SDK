package com.appzonegroup.creditclub.pos.util

import com.appzonegroup.creditclub.pos.R
import com.appzonegroup.creditclub.pos.TerminalOptionsActivity
import com.creditclub.pos.printer.PrinterStatus
import com.creditclub.pos.printer.TextNode
import com.creditclub.pos.printer.WalkPaper
import com.appzonegroup.creditclub.pos.service.ParameterService


/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 3/1/2019.
 * Appzone Ltd
 */

object MenuPages {
    const val MAIN_MENU = 0
    const val REPRINT_EODS = 100
    const val REPRINT_ANY = 101
    const val ADMIN = 102

    private val PAGE_MAP = hashMapOf<Int, MenuPage>()

    operator fun get(id: Int) = PAGE_MAP[id]

    init {
        PAGE_MAP[MAIN_MENU] = menuPage {
            id = MAIN_MENU
            icon = R.mipmap.ic_launcher
            name = "Welcome"

            options = lazy {
                linkedMapOf(
                    Modules.PURCHASE to Modules[Modules.PURCHASE]
                )
            }
        }

        PAGE_MAP[REPRINT_ANY] = menuPage {
            id = REPRINT_ANY
            icon = R.drawable.ic_purchase
            name = "Reprint Any"

            options = lazy {
                linkedMapOf(
                    Modules.REPRINT_LAST to Modules[Modules.REPRINT_LAST],
                    Modules.REPRINT_BY_STAN to Modules[Modules.REPRINT_BY_STAN]
                )
            }
        }

        PAGE_MAP[REPRINT_EODS] = menuPage {
            id = REPRINT_EODS
            name = "End of Day"

            options = lazy {
                linkedMapOf(
                    Modules.PRINT_EOD to Modules[Modules.PRINT_EOD],
                    Modules.EOD_BY_DATE to Modules[Modules.EOD_BY_DATE]
//                    Modules.PRINT_TOTAL to Modules[Modules.PRINT_TOTAL],
//                    Modules.PRINT_SUMMARY to Modules[Modules.PRINT_SUMMARY]
                )
            }
        }

        PAGE_MAP[ADMIN] = menuPage {
            name = "Admin"
            isSecure = true
            options = lazy {
                linkedMapOf(
                    Modules.NETWORK_PARAMETERS to actionButton {
                        name = "Settings"
                        icon = R.drawable.ic_settings_fancy
                        activityClass = TerminalOptionsActivity::class.java
//                        onClick { flow ->
//                            flow.openAdminPage(SettingsActivity::class.java)
//                        }
                    },

                    Modules.PRINT_CONFIG to actionButton {
                        name = "PRINT CONFIGURATION"

                        onClick {
                            it.config.run {
                                it.printer.printAsync(
                                    TextNode(
                                        """
POS Configuration
-----------------

POS Mode: ${posMode.label}
APN: ${getApnInfo(it)}
Host Name: $host
Terminal ID: $terminalId
IP: ${posMode.ip}
Port: ${posMode.port}
Keep Alive (Call Home) in seconds: $callHome
                            """.trimIndent()
                                    ),
                                    WalkPaper(20)
                                ) { printerStatus ->
                                    if (printerStatus != PrinterStatus.READY) return@printAsync it.showError(
                                        printerStatus.message
                                    )
                                }
                            }
                        }
                    },

                    Modules.PRINT_PARAMETER to actionButton {
                        name = "PRINT PARAMETER"
                        onClick {
                            try {
                                val parameters = ParameterService.getInstance(it).parameters

                                it.printer.printAsync(
                                    TextNode(
                                        """
POS Parameters
--------------

Card Acceptor ID: ${parameters.cardAcceptorId}
Card Acceptor Location: ${parameters.cardAcceptorLocation}
Country Code: ${parameters.countryCode}
                            """.trimIndent()
                                    ),
                                    WalkPaper(20)
                                ) { printerStatus ->
                                    if (printerStatus != PrinterStatus.READY) return@printAsync it.showError(
                                        printerStatus.message
                                    )
                                }
                            } catch (ex: java.lang.Exception) {
                                ex.printStackTrace()
                                it.showError("An error occurred")
                            }
                        }
                    },
//
                    Modules.DOWN_CAPK to actionButton {
                        name = "DOWN CAPK"
                    }
                )
            }
        }
    }
}