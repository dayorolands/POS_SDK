package com.appzonegroup.creditclub.pos

import android.app.PendingIntent
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.databinding.DataBindingUtil
import com.appzonegroup.creditclub.pos.databinding.ActivityCardMainMenuBinding
import com.appzonegroup.creditclub.pos.service.ParameterService
import com.appzonegroup.creditclub.pos.util.MenuPage
import com.appzonegroup.creditclub.pos.util.MenuPages
import com.appzonegroup.creditclub.pos.widget.Dialogs


class CardMainMenuActivity : MenuActivity(), View.OnClickListener {
    override val pageNumber = MenuPages.MAIN_MENU
    override val title = MenuPages[MenuPages.MAIN_MENU]?.name ?: "Welcome"
//    override val functionId = FunctionIds.CARD_TRANSACTIONS

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.empty)

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

                    parameters.downloadAsync(dialogProvider)
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
                R.id.admin_button -> {
                    adminAction {
                        startActivity(Intent(this, MenuActivity::class.java).apply {
                            putExtra(MenuPage.TITLE, MenuPages[MenuPages.ADMIN]?.name)
                            putExtra(MenuPage.PAGE_NUMBER, MenuPages.ADMIN)
                        })
                    }
                }
                R.id.reprint_button -> supervisorAction {
                    //                    Modules[Modules.REPRINT_LAST].click(this)
                    startActivity(ReprintMenuActivity::class.java)
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
//                    Modules[Modules.PRINT_EOD].click(this)
                }
                R.id.key_download_button -> {
                    try {
                        ParameterService.getInstance(this).downloadKeysAsync(dialogProvider, true)
                    } catch (ex: Exception) {
                        ex.printStackTrace()
                    }
                }
                R.id.parameter_download_button -> {
                    ParameterService.getInstance(this).downloadParametersAsync(dialogProvider)
                }

                else -> showError("This function is disabled.")
            }
        }
    }

    private fun notifyForUpdates() {
        val intent = Intent(this, UpdateActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent: PendingIntent = PendingIntent.getActivity(this, 0, intent, 0)

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.notification_icon)
            .setContentTitle("Update Available for POS")
            .setContentText("A new version is available")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            // Set the intent that will fire when the user taps the notification
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(this)) {
            notify(1, builder.build())
        }
    }

    private fun updateProgressNotification() {
        val builder = NotificationCompat.Builder(this, CHANNEL_ID).apply {
            setContentTitle("Picture Download")
            setContentText("Download in progress")
            setSmallIcon(R.drawable.notification_icon)
            priority = NotificationCompat.PRIORITY_LOW
        }
        val PROGRESS_MAX = 100
        val PROGRESS_CURRENT = 0
        NotificationManagerCompat.from(this).apply {
            // Issue the initial notification with zero progress
            builder.setProgress(PROGRESS_MAX, PROGRESS_CURRENT, false)
            notify(1, builder.build())

            // Do the job here that tracks the progress.
            // Usually, this should be in a
            // worker thread
            // To show progress, update PROGRESS_CURRENT and update the notification with:
            // builder.setProgress(PROGRESS_MAX, PROGRESS_CURRENT, false);
            // notificationManager.notify(notificationId, builder.build());

            // When done, update the notification one more time to remove the progress bar
            builder.setContentText("Download complete")
                .setProgress(0, 0, false)
            notify(1, builder.build())
        }
    }

    override fun goBack(v: View) {
        if (intent.getBooleanExtra("SHOW_BACK_BUTTON", false)) onBackPressed()
    }

    companion object {
        const val CHANNEL_ID = "update"
    }
}
