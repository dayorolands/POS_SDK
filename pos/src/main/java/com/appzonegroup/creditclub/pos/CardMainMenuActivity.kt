package com.appzonegroup.creditclub.pos

import android.content.Intent
import android.os.Bundle
import android.view.View
import com.appzonegroup.creditclub.pos.data.PosPreferences
import com.appzonegroup.creditclub.pos.databinding.ActivityCardMainMenuBinding
import com.appzonegroup.creditclub.pos.service.ParameterService
import com.appzonegroup.creditclub.pos.util.MenuPage
import com.appzonegroup.creditclub.pos.util.MenuPages
import com.creditclub.core.util.format
import com.creditclub.core.util.localStorage
import com.creditclub.core.util.safeRunIO
import com.creditclub.core.util.showError
import com.creditclub.pos.PosParameter
import com.creditclub.pos.api.posApiService
import com.creditclub.ui.dataBinding
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import java.time.Instant


class CardMainMenuActivity : PosActivity(R.layout.activity_card_main_menu) {
    private val binding by dataBinding<ActivityCardMainMenuBinding>()

    //    override val functionId = FunctionIds.CARD_TRANSACTIONS
    private val posPreferences: PosPreferences by inject()
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
        binding.header.goBack.setOnClickListener {
            if (intent.getBooleanExtra("SHOW_BACK_BUTTON", false)) onBackPressed()
        }
        binding.hideBackButton = !intent.getBooleanExtra("SHOW_BACK_BUTTON", false)
        binding.purchaseButton.button.setOnClickListener {
            startActivity(CardWithdrawalActivity::class.java)
        }
        binding.adminButton.button.setOnClickListener {
            mainScope.launch {
                adminAction {
                    val intent = Intent(this@CardMainMenuActivity, MenuActivity::class.java)
                    intent.apply {
                        putExtra(MenuPage.TITLE, MenuPages[MenuPages.ADMIN]?.name)
                        putExtra(MenuPage.PAGE_NUMBER, MenuPages.ADMIN)
                    }
                    startActivity(intent)
                }
            }
        }
        binding.reprintButton.button.setOnClickListener {
            mainScope.launch {
                supervisorAction {
                    //                    Modules[Modules.REPRINT_LAST].click(this)
                    startActivity(ReprintMenuActivity::class.java)
                }
            }
        }
        binding.authButton.button.setOnClickListener {
            startActivity(PreAuthActivity::class.java)
        }
        binding.depositButton.button.setOnClickListener { showError("This function is disabled.") }
        binding.billPayButton.button.setOnClickListener { showError("This function is disabled.") }
        binding.cashAdvButton.button.setOnClickListener {
            startActivity(CashAdvanceActivity::class.java)
        }
        binding.reversalButton.button.setOnClickListener {
            supervisorAction {
                startActivity(ReversalActivity::class.java)
            }
        }
        binding.balanceButton.button.setOnClickListener {
            startActivity(BalanceInquiryActivity::class.java)
        }
        binding.cashBackButton.button.setOnClickListener {
            startActivity(CashBackActivity::class.java)
        }
        binding.refundButton.button.setOnClickListener {
            supervisorAction {
                startActivity(RefundActivity::class.java)
            }
        }
        binding.salesCompletionButton.button.setOnClickListener {
            startActivity(SalesCompleteActivity::class.java)
        }
        binding.eodButton.button.setOnClickListener {
            supervisorAction {
                startActivity(Intent(
                    this,
                    MenuActivity::class.java
                ).apply {
                    putExtra(MenuPage.TITLE, MenuPages[MenuPages.REPRINT_EODS]?.name)
                    putExtra(MenuPage.PAGE_NUMBER, MenuPages.REPRINT_EODS)
                })
            }
        }
        binding.unsettledButton.button.setOnClickListener {
            startActivity(UnsettledTransactionsActivity::class.java)
        }
        binding.keyDownloadButton.button.setOnClickListener {
            mainScope.launch { downloadKeys() }
        }
        binding.parameterDownloadButton.button.setOnClickListener {
            mainScope.launch { downloadParameters() }
        }
        binding.capkDownloadButton.button.setOnClickListener {
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
        binding.emvAidDownloadButton.button.setOnClickListener {
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
