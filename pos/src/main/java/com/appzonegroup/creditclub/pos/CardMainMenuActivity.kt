package com.appzonegroup.creditclub.pos

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.databinding.DataBindingUtil
import com.appzonegroup.creditclub.pos.databinding.ActivityCardMainMenuBinding
import com.appzonegroup.creditclub.pos.util.MenuPage
import com.appzonegroup.creditclub.pos.util.MenuPages
import com.appzonegroup.creditclub.pos.widget.Dialogs
import com.creditclub.core.util.format
import com.creditclub.core.util.localStorage
import com.creditclub.core.util.safeRunIO
import com.creditclub.core.util.showError
import com.creditclub.pos.PosManager
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.core.parameter.parametersOf
import org.threeten.bp.Instant


class CardMainMenuActivity : MenuActivity(), View.OnClickListener {
    override val pageNumber = MenuPages.MAIN_MENU
    override val title = MenuPages[MenuPages.MAIN_MENU]?.name ?: "Welcome"

    //    override val functionId = FunctionIds.CARD_TRANSACTIONS
    private val posManager: PosManager by inject { parametersOf(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.empty)

        localStorage.agent ?: return finish()

        if (config.terminalId.isEmpty()) {
            return showError(getString(R.string.pos_terminal_id_required)) {
                onClose {
                    super.onBackPressed()
                }
            }
        }

        Dialogs.requestPin(this, getString(R.string.pos_enter_supervisor_pin), timeout = 0) { pin ->
            if (pin == null) return@requestPin super.onBackPressed()
            confirmSupervisorPin(pin, closeOnFail = true) { passed ->
                if (passed) {
                    val binding: ActivityCardMainMenuBinding =
                        DataBindingUtil.setContentView(this, R.layout.activity_card_main_menu)
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

                    mainScope.launch { checkKeysAndParameters() }
                }
            }
        }
    }

    override fun onClick(v: View?) {
        v?.also {
            when (v.id) {
                R.id.purchase_button -> {
                    startActivity(CardWithdrawalActivity::class.java)
                }
                R.id.admin_button -> mainScope.launch {
                    if (!posManager.openSettings()) {
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
                R.id.reprint_button -> mainScope.launch {
                    if (!posManager.openReprint()) {
                        supervisorAction {
                            //                    Modules[Modules.REPRINT_LAST].click(this)
                            startActivity(ReprintMenuActivity::class.java)
                        }
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
                            parameters.downloadCapk(this@CardMainMenuActivity)
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
                            parameters.downloadAid(this@CardMainMenuActivity)
                        }
                        dialogProvider.hideProgressBar()
                        if (error != null) return@launch dialogProvider.showError(error)
                        dialogProvider.showSuccess("EMV AID Download successful")
                    }
                }

                else -> showError("This function is disabled.")
            }
        }
    }

    private suspend fun checkKeysAndParameters() {
        val localDate = Instant.now().format("MMdd")
        if (localDate == parameters.updatedAt) return

        dialogProvider.showProgressBar("Downloading Keys and Parameters")
        val (_, error) = safeRunIO {
            parameters.downloadKeys(this@CardMainMenuActivity)
            parameters.downloadParameters(this@CardMainMenuActivity)
        }
        dialogProvider.hideProgressBar()
        if (error != null) {
            dialogProvider.showError("Download Failed. ${error.message}")
            return
        }

        parameters.updatedAt = localDate
        dialogProvider.showSuccess("Download successful")
    }

    private suspend fun downloadKeys() {
        dialogProvider.showProgressBar("Downloading Keys")
        val (_, error) = safeRunIO {
            parameters.downloadKeys(this@CardMainMenuActivity)
        }
        dialogProvider.hideProgressBar()
        if (error != null) {
            dialogProvider.showError("Download Failed. ${error.message}")
            return
        }

        parameters.updatedAt = Instant.now().format("MMdd")
        dialogProvider.showSuccess("Download successful")
    }

    private suspend fun downloadParameters() {
        dialogProvider.showProgressBar("Downloading Parameters")
        val (_, error) = safeRunIO {
            parameters.downloadParameters(this@CardMainMenuActivity)
        }
        dialogProvider.hideProgressBar()
        if (error != null) {
            dialogProvider.showError("Download Failed. ${error.message}")
            return
        }

        parameters.updatedAt = Instant.now().format("MMdd")
        dialogProvider.showSuccess("Download successful")
    }

    override fun goBack(v: View) {
        if (intent.getBooleanExtra("SHOW_BACK_BUTTON", false)) onBackPressed()
    }
}
