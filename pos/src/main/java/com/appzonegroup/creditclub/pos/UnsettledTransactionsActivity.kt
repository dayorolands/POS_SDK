package com.appzonegroup.creditclub.pos

import android.os.Bundle
import android.view.MenuItem
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.appzonegroup.creditclub.pos.adapter.PosNotificationAdapter
import com.appzonegroup.creditclub.pos.contract.Logger
import com.appzonegroup.creditclub.pos.models.NotificationResponse
import com.appzonegroup.creditclub.pos.models.PosNotification
import com.appzonegroup.creditclub.pos.models.view.NotificationViewModel
import com.appzonegroup.creditclub.pos.service.ApiService
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_pending_confirmation.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Headers

class UnsettledTransactionsActivity : PosActivity(), Logger {

    override val tag: String
        get() = "Unsettled Transactions"

    private val viewModel by lazy {
        ViewModelProviders.of(this).get(NotificationViewModel::class.java)
    }

    private val rAdapter by lazy {
        PosNotificationAdapter(emptyList()) {
            onSettle { posNotification ->
                GlobalScope.launch(Dispatchers.Main) {
                    dialogProvider.showProgressBar("Settling")

                    log("Running middleware notifications....")

                    val url = "${ApiService.BASE_URL}/POSCashOutNotification"
                    val serializer = Gson()

                    val dataToSend = serializer.toJson(posNotification)
                    log("PosNotification request: $dataToSend")

                    val headers = Headers.Builder()
                    headers.add("Authorization", "iRestrict ${BuildConfig.NOTIFICATION_TOKEN}")
                    headers.add("TerminalID", config.terminalId)

                    val (responseString, error) = withContext(Dispatchers.IO) {
                        ApiService.post(url, dataToSend, headers.build())
                    }
                    dialogProvider.hideProgressBar()

                    responseString ?: return@launch showError("A network error occurred. Please try again later")

                    error?.printStackTrace()

                    log("PosNotification response: $responseString")

                    try {
                        val notificationResponse =
                            Gson().fromJson(responseString, NotificationResponse::class.java)
                        if (notificationResponse != null) {
                            if (notificationResponse.billerReference != null && notificationResponse.billerReference!!.isNotEmpty()) {
                                viewModel.deleteNotification(posNotification)
                            }
                        }
                    } catch (ex: Exception) {
                        ex.printStackTrace()
                        return@launch showError("A network error occurred. Please try again later")
                    }
                }
            }

            onDelete { posNotification ->
                viewModel.deleteNotification(posNotification)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pending_confirmation)

        list.apply {
            adapter = rAdapter
            layoutManager = LinearLayoutManager(this@UnsettledTransactionsActivity)
        }

        viewModel.unsettledTransactions.observe(this, Observer<List<PosNotification>>(rAdapter::setData))
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item?.itemId != R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
