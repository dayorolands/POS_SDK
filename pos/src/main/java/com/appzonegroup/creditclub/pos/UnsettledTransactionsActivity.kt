package com.appzonegroup.creditclub.pos

import android.os.Bundle
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.appzonegroup.creditclub.pos.adapter.PosNotificationAdapter
import com.appzonegroup.creditclub.pos.models.PosNotification
import com.appzonegroup.creditclub.pos.models.view.NotificationViewModel
import com.creditclub.core.data.api.retrofitService
import com.creditclub.core.ui.CreditClubActivity
import com.creditclub.core.util.safeRunIO
import com.creditclub.core.util.showError
import com.creditclub.pos.PosConfig
import com.creditclub.pos.api.PosApiService
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class UnsettledTransactionsActivity : CreditClubActivity(R.layout.activity_pending_confirmation) {
    private val viewModel: NotificationViewModel by viewModels()
    private val config: PosConfig by inject()
    private val posApiService: PosApiService by retrofitService()

    private val rAdapter by lazy {
        PosNotificationAdapter(emptyList()) {
            onSettle { posNotification ->
                mainScope.launch { settleTransaction(posNotification) }
            }

            onDelete { posNotification ->
                viewModel.deleteNotification(posNotification)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pending_confirmation)

        val list = findViewById<RecyclerView>(R.id.list)
        list.adapter = rAdapter
        list.layoutManager = LinearLayoutManager(this)

        viewModel.unsettledTransactions.observe(this, rAdapter::setData)
    }

    private suspend fun settleTransaction(notification: PosNotification) {
        dialogProvider.showProgressBar("Settling")
        val (response, error) = safeRunIO {
            posApiService.posCashOutNotification(
                notification,
                "iRestrict ${backendConfig.posNotificationToken}",
                notification.terminalId ?: config.terminalId
            )
        }

        if (error != null) return dialogProvider.showError(error)

        if (!response?.billerReference.isNullOrBlank()) safeRunIO {
            viewModel.deleteNotification(notification)
        } else {
            return dialogProvider.showError(
                response?.message ?: "A network error occurred. Please try again later"
            )
        }
        dialogProvider.hideProgressBar()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId != R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
