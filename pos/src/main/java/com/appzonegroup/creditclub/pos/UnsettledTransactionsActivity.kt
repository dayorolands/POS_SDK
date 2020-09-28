package com.appzonegroup.creditclub.pos

import android.os.Bundle
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.appzonegroup.creditclub.pos.adapter.PosNotificationAdapter
import com.appzonegroup.creditclub.pos.models.PosNotification
import com.appzonegroup.creditclub.pos.models.view.NotificationViewModel
import com.creditclub.core.util.safeRunIO
import com.creditclub.core.util.showError
import com.creditclub.pos.api.PosApiService
import kotlinx.android.synthetic.main.activity_pending_confirmation.*
import kotlinx.coroutines.launch
import retrofit2.create

class UnsettledTransactionsActivity : PosActivity() {
    private val viewModel by viewModels<NotificationViewModel>()

    private val rAdapter by lazy {
        val posApiService: PosApiService = creditClubMiddleWareAPI.retrofit.create()
        PosNotificationAdapter(emptyList()) {
            onSettle { posNotification ->
                mainScope.launch { posApiService.settleTransaction(posNotification) }
            }

            onDelete { posNotification ->
                viewModel.deleteNotification(posNotification)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pending_confirmation)

        list.adapter = rAdapter
        list.layoutManager = LinearLayoutManager(this)

        viewModel.unsettledTransactions.observe(
            this,
            Observer<List<PosNotification>>(rAdapter::setData)
        )
    }

    private suspend fun PosApiService.settleTransaction(notification: PosNotification) {
        dialogProvider.showProgressBar("Settling")
        val (response, error) = safeRunIO {
            posCashOutNotification(
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

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item?.itemId != R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
