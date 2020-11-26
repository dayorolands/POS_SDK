package com.appzonegroup.creditclub.pos

import android.content.Intent
import android.os.Bundle
import android.view.View
import com.appzonegroup.creditclub.pos.data.posPreferences
import com.appzonegroup.creditclub.pos.databinding.ActivityCardMainMenuBinding
import com.appzonegroup.creditclub.pos.service.ParameterService
import com.appzonegroup.creditclub.pos.util.MenuPage
import com.appzonegroup.creditclub.pos.util.MenuPages
import com.creditclub.core.util.format
import com.creditclub.core.util.localStorage
import com.creditclub.core.util.safeRunIO
import com.creditclub.core.util.showError
import com.creditclub.pos.PosManager
import com.creditclub.pos.PosParameter
import com.creditclub.pos.RemoteConnectionInfo
import com.creditclub.pos.api.posApiService
import com.creditclub.ui.dataBinding
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.core.parameter.parametersOf
import java.time.Instant


class CardMainMenuActivity : PosActivity(R.layout.activity_card_main_menu), View.OnClickListener {
    private val binding by dataBinding<ActivityCardMainMenuBinding>()

    //    override val functionId = FunctionIds.CARD_TRANSACTIONS
    private val defaultParameterStore: PosParameter by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.cont.visibility = View.INVISIBLE
        localStorage.agent ?: return finish()
        checkRequirements()
    }

    private fun checkRequirements() {
        if (config.terminalId.isEmpty()) {
            return showError(getString(R.string.pos_terminal_id_required)) {
                onClose {
                    super.onBackPressed()
                }
            }
        }

        if (!posPreferences.hasBinRoutes) {
            return dialogProvider.confirm(
                "We couldn't download some settings",
                "Do you want to retry?"
            ) {
                onSubmit {
                    if (it) {
                        mainScope.launch {
                            updateBinRoutes()
                            checkRequirements()
                        }
                    } else finish()
                }
            }
        }

        mainScope.launch {
            binding.cont.visibility = View.VISIBLE
            checkKeysAndParameters()
            bindView()
        }
    }

    private suspend fun updateBinRoutes() {
        dialogProvider.showProgressBar("Downloading pos settings")
        val (response) = safeRunIO {
            creditClubMiddleWareAPI.posApiService.getBinRoutes(
                localStorage.institutionCode,
                localStorage.agentPhone
            )
        }
        dialogProvider.hideProgressBar()
        if (response?.isSuccessful() == true) {
            posPreferences.binRoutes = response.data
        }
    }

    private fun bindView() {
        binding.hideBackButton = !intent.getBooleanExtra("SHOW_BACK_BUTTON", false)
        binding.purchaseButton.button.setOnClickListener(this)
        binding.adminButton.button.setOnClickListener(this)
        binding.reprintButton.button.setOnClickListener(this)
        binding.authButton.button.setOnClickListener(this)
        binding.depositButton.button.setOnClickListener(this)
        binding.billPayButton.button.setOnClickListener(this)
        binding.cashAdvButton.button.setOnClickListener(this)
        binding.reversalButton.button.setOnClickListener(this)
        binding.balanceButton.button.setOnClickListener(this)
        binding.cashBackButton.button.setOnClickListener(this)
        binding.refundButton.button.setOnClickListener(this)
        binding.salesCompletionButton.button.setOnClickListener(this)
        binding.eodButton.button.setOnClickListener(this)
        binding.unsettledButton.button.setOnClickListener(this)
        binding.keyDownloadButton.button.setOnClickListener(this)
        binding.parameterDownloadButton.button.setOnClickListener(this)
        binding.capkDownloadButton.button.setOnClickListener(this)
        binding.emvAidDownloadButton.button.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.purchase_button -> {
                startActivity(CardWithdrawalActivity::class.java)
            }
            R.id.admin_button -> mainScope.launch {
                adminAction {
                    val intent = Intent(this@CardMainMenuActivity, MenuActivity::class.java)
                    intent.apply {
                        putExtra(MenuPage.TITLE, MenuPages[MenuPages.ADMIN]?.name)
                        putExtra(MenuPage.PAGE_NUMBER, MenuPages.ADMIN)
                    }
                    startActivity(intent)
                }
            }
            R.id.reprint_button -> mainScope.launch {
                supervisorAction {
                    //                    Modules[Modules.REPRINT_LAST].click(this)
                    startActivity(ReprintMenuActivity::class.java)
                }
            }
//                R.id.balance_button -> {
//                    startActivity(BalanceInquiryActivity::class.java)
//                }
            R.id.cash_back_button -> {
                startActivity(CashBackActivity::class.java)
            }
            R.id.cash_adv_button -> startActivity(CashAdvanceActivity::class.java)
//                R.id.refund_button -> printerDependentAction(false) {
//                    supervisorAction {
//                        startActivity(RefundActivity::class.java)
//                    }
//                }
//                R.id.reversal_button -> printerDependentAction(false) {
//                    supervisorAction {
//                        startActivity(ReversalActivity::class.java)
//                    }
//                }
            R.id.auth_button -> {
                startActivity(PreAuthActivity::class.java)
            }
            R.id.sales_completion_button -> {
                startActivity(SalesCompleteActivity::class.java)
            }
            R.id.unsettled_button -> {
                startActivity(UnsettledTransactionsActivity::class.java)
            }
            R.id.eod_button -> supervisorAction {
                startActivity(Intent(
                    this,
                    MenuActivity::class.java
                ).apply {
                    putExtra(MenuPage.TITLE, MenuPages[MenuPages.REPRINT_EODS]?.name)
                    putExtra(MenuPage.PAGE_NUMBER, MenuPages.REPRINT_EODS)
                })
            }
            R.id.key_download_button -> {
                mainScope.launch { downloadKeys() }
            }
            R.id.parameter_download_button -> {
                mainScope.launch { downloadParameters() }
            }
            R.id.capk_download_button -> {
                mainScope.launch {
                    dialogProvider.showProgressBar("Downloading CAPK")
                    val (_, error) = safeRunIO {
                        defaultParameterStore.downloadCapk(this@CardMainMenuActivity)
                    }
                    dialogProvider.hideProgressBar()
                    if (error != null) return@launch dialogProvider.showError(error)
                    dialogProvider.showSuccess("CAPK Download successful")
                }
            }
            R.id.emv_aid_download_button -> {
                mainScope.launch {
                    dialogProvider.showProgressBar("Downloading EMV AID")
                    val (_, error) = safeRunIO {
                        defaultParameterStore.downloadAid(this@CardMainMenuActivity)
                    }
                    dialogProvider.hideProgressBar()
                    if (error != null) return@launch dialogProvider.showError(error)
                    dialogProvider.showSuccess("EMV AID Download successful")
                }
            }

            else -> showError("This function is disabled.")
        }
    }

    private suspend fun checkKeysAndParameters() {
        val localDate = Instant.now().format("MMdd")
        if (localDate == parameters.updatedAt) return

        dialogProvider.showProgressBar("Downloading Keys and Parameters")
        for (parameterStore in parameterStores) {
            val (_, error) = safeRunIO {
                parameters.downloadKeys(this@CardMainMenuActivity)
                parameters.downloadParameters(this@CardMainMenuActivity)
            }
            if (error != null) {
                dialogProvider.hideProgressBar()
                dialogProvider.showError("Download Failed. ${error.message}")
                parameters.updatedAt = ""
                return
            }
        }
        dialogProvider.hideProgressBar()

        parameters.updatedAt = localDate
        dialogProvider.showSuccess("Download successful")
    }

    private suspend fun downloadKeys() {
        dialogProvider.showProgressBar("Downloading Keys")
        for (parameterStore in parameterStores) {
            val (_, error) = safeRunIO {
                parameterStore.downloadKeys(this@CardMainMenuActivity)
            }
            if (error != null) {
                dialogProvider.hideProgressBar()
                dialogProvider.showError("Download Failed. ${error.message}")
                parameters.updatedAt = ""
                return
            }
        }
        dialogProvider.hideProgressBar()

        parameters.updatedAt = Instant.now().format("MMdd")
        dialogProvider.showSuccess("Download successful")
    }

    private suspend fun downloadParameters() {
        dialogProvider.showProgressBar("Downloading Parameters")
        for (parameterStore in parameterStores) {
            val (_, error) = safeRunIO {
                parameterStore.downloadParameters(this@CardMainMenuActivity)
            }
            if (error != null) {
                dialogProvider.hideProgressBar()
                dialogProvider.showError("Download Failed. ${error.message}")
                return
            }
        }
        dialogProvider.hideProgressBar()

        parameters.updatedAt = Instant.now().format("MMdd")
        dialogProvider.showSuccess("Download successful")
    }

    fun goBack(v: View) {
        if (intent.getBooleanExtra("SHOW_BACK_BUTTON", false)) onBackPressed()
    }

    private inline val parameterStores: Sequence<PosParameter>
        get() {
            val connectionSequence = sequence {
                yield(config.remoteConnectionInfo)
                posPreferences.binRoutes?.run {
                    for (binRoutes in this) {
                        for (route in binRoutes.routes) {
                            yield(route.connectionInfo)
                        }
                    }
                }
            }

            return connectionSequence
                .distinctBy { "${it.ip}:${it.port}" }
                .map { ParameterService(this, it) }
        }
}
