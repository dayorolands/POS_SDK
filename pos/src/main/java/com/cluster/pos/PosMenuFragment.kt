package com.cluster.pos

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import com.cluster.ui.dataBinding
import com.cluster.pos.data.PosPreferences
import com.cluster.pos.databinding.PosMenuFragmentBinding
import com.cluster.pos.util.MenuPage
import com.cluster.pos.util.MenuPages
import com.cluster.core.data.api.retrofitService
import com.cluster.core.ui.CreditClubActivity
import com.cluster.core.ui.widget.TextFieldParams
import com.cluster.core.util.format
import com.cluster.core.util.safeRunIO
import com.cluster.pos.api.PosApiService
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import java.time.Instant


class PosMenuFragment : PosFragment(R.layout.pos_menu_fragment) {
    private val binding: PosMenuFragmentBinding by dataBinding()
    private val posPreferences: PosPreferences by inject()
    private val defaultParameterStore: PosParameter by inject()
    private val posApiService: PosApiService by retrofitService()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val creditClubActivity = requireActivity() as CreditClubActivity
        creditClubActivity.setSupportActionBar(binding.toolbar)
        creditClubActivity.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.cont.visibility = View.INVISIBLE
        if (localStorage.agent == null) {
            findNavController().popBackStack()
        }
        checkRequirements()
    }

    private fun checkRequirements() {
        if (config.terminalId.isEmpty()) {
            return dialogProvider.showError(getString(R.string.pos_terminal_id_required)) {
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
                    } else findNavController().popBackStack()
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
            posApiService.getBinRoutes(
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
        binding.purchaseButton.button.setOnClickListener {
            startActivity(CardWithdrawalActivity::class.java)
        }
        binding.adminButton.button.setOnClickListener {
            mainScope.launch {
                val params = TextFieldParams(hint = "Administrator password", type = "textPassword")
                val password = dialogProvider.getInput(params) ?: return@launch
                val passed = password == config.adminPin
                if (passed) {
                    val intent = Intent(requireActivity(), MenuActivity::class.java)
                    intent.apply {
                        putExtra(MenuPage.TITLE, MenuPages[MenuPages.ADMIN]?.name)
                        putExtra(MenuPage.PAGE_NUMBER, MenuPages.ADMIN)
                    }
                    startActivity(intent)
                } else {
                    dialogProvider.showError("Incorrect Password")
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
                    requireContext(),
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
        binding.chargebackButton.button.setOnClickListener {
            findNavController().navigate(R.id.action_to_chargeback)
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
                    defaultParameterStore.downloadCapk()
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
                    defaultParameterStore.downloadAid()
                }
                dialogProvider.hideProgressBar()
                if (error != null) return@launch dialogProvider.showError(error)
                dialogProvider.showSuccess("EMV AID Download successful")
            }
        }
    }

    private suspend fun checkKeysAndParameters() {
        if (posParameterList.isEmpty()) {
            dialogProvider.showError("No routes available")
            return
        }

        val localDate = Instant.now().format("MMdd")
        if (localDate == parameters.updatedAt) return

        dialogProvider.showProgressBar("Downloading Keys")
        for (parameterStore in posParameterList) {
            val (_, error) = safeRunIO {
                parameterStore.downloadKeys()
                parameterStore.downloadParameters()
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
        if (posParameterList.isEmpty()) {
            dialogProvider.showError("No routes available")
            return
        }

        dialogProvider.showProgressBar("Downloading Keys")
        for (parameterStore in posParameterList) {
            val (_, error) = safeRunIO {
                parameterStore.downloadKeys()
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
        if (posParameterList.isEmpty()) {
            dialogProvider.showError("No routes available")
            return
        }

        dialogProvider.showProgressBar("Downloading Parameters")
        for (parameterStore in posParameterList) {
            val (_, error) = safeRunIO {
                parameterStore.downloadParameters()
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

    private inline fun supervisorAction(crossinline next: () -> Unit) {
        dialogProvider.requestPIN(getString(R.string.pos_enter_supervisor_pin)) {
            onSubmit { pin ->
                confirmSupervisorPin(pin) { passed ->
                    if (passed) next()
                }
            }
        }
    }

    private inline fun confirmSupervisorPin(
        pin: String,
        closeOnFail: Boolean = false,
        crossinline next: (Boolean) -> Unit,
    ) {
        val status = pin == config.supervisorPin
        if (!status) {
            if (closeOnFail) return dialogProvider.showError("Authentication Failed") {
                onClose {
                    findNavController().popBackStack()
                }
            }

            dialogProvider.showError("Authentication Failed")
        }
        next(status)
    }

    private val posParameterList: List<PosParameter> by lazy {
        val context = requireContext()
        val connectionSequence = sequence {
            if (config.remoteConnectionInfo != InvalidRemoteConnectionInfo) {
                yield(config.remoteConnectionInfo)
            }
            posPreferences.binRoutes?.run {
                for (binRoutes in this) {
                    for (route in binRoutes.routes) {
                        yield(route.connectionInfo)
                    }
                }
            }
        }

        return@lazy connectionSequence
            .distinctBy { "${it.host}:${it.port}" }
            .map { it.getParameter(context) }
            .toList()
    }
}
