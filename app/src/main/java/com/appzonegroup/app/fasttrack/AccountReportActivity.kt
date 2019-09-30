package com.appzonegroup.app.fasttrack

import android.os.Bundle
import android.util.Log
import android.view.View
import com.appzonegroup.app.fasttrack.adapter.ReportsAdapter
import com.appzonegroup.app.fasttrack.contract.ReportType
import com.appzonegroup.app.fasttrack.databinding.ActivityAccountReportBinding
import com.appzonegroup.app.fasttrack.databinding.ItemPosCashoutBinding
import com.appzonegroup.app.fasttrack.model.Report
import com.crashlytics.android.Crashlytics
import com.creditclub.core.data.model.PosTransactionReport
import com.creditclub.core.data.request.POSTransactionReportRequest
import com.creditclub.core.type.TransactionStatus
import com.creditclub.core.ui.SimpleBindingAdapter
import com.creditclub.core.util.*
import com.creditclub.core.util.delegates.contentView
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

class AccountReportActivity : BaseActivity() {
    private val binding by contentView<AccountReportActivity, ActivityAccountReportBinding>(R.layout.activity_account_report)

    private var startIndex = 0
    private val maxSize = 20

    private var report: Report? = null

    private val transactionTypeId by lazy { intent.extras?.getString("TRANSACTION_TYPE_ID") ?: "0" }
    private val reportType by lazy { getReportType(transactionTypeId.toInt()) }
    private val transactionType by lazy { intent.extras?.getString("TRANSACTION_TYPE") }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        title = "$transactionType Report"
        loadReport()
    }

    fun refresh(view: View) {
        loadReport()
    }

    private fun getReportType(reportID: Int): Int {
        return when (reportID) {
            2, 20 -> 1
            1 -> 6
            10, 21, 22 -> 2
            /* 4, */5, 6 -> 4
            9 -> 3
            7, 8 -> 5
            else -> 0
        }
    }

    private fun updateButtons() {
        binding.nextButton.isEnabled = startIndex + maxSize < report?.totalCount ?: 0
        binding.previousButton.isEnabled = startIndex >= maxSize
    }

    override fun processFinished(response: String?) {
        var response = response
        super.processFinished(response)
        if (response != null) {
            try {
                //Standard .NET additions to serialized objects
                response = response.replace("\\", "").replace("\n", "").trim { it <= ' ' }
                Log.e("Report Data", response)
                report = Gson().fromJson(response, Report::class.java)
                val items = report!!.reports

                if (report!!.reports.size == 0) {
                    showNotification("No records exist for the given criteria")
                    return
                }
                /*reports = new ArrayList<>();
                reports.addAll(items);*/
                //reports = report.getReports();
                if (ReportType[transactionTypeId] === ReportType.POSCashOut) {
                    val report = Json.parse(PosTransactionReport.serializer(), response)

                    val myReportAdapter = PosCashoutAdapter(report.reports ?: emptyList())
                    binding.accountsListView.visibility = View.GONE
                    binding.container.visibility = View.VISIBLE
                    binding.container.adapter = myReportAdapter
                } else {
                    setupReportView()
                }

                //hideProgressBar();
            } catch (e: Exception) {
                e.printStackTrace()
                Crashlytics.logException(Exception(e.message))
                showError(e.message ?: "")
            }

        } else {
            Log.e(
                "ResponseFailed",
                "There was a problem reaching the server. Please ensure that you have internet and try again"
            )
            showError("There was a problem reaching the server. Please ensure that you have internet and try again")
        }
    }

    private fun loadReport() {
        scope.launch(Dispatchers.Main) {
            showProgressBar("Loading Report")

            if (ReportType[transactionTypeId] === ReportType.POSCashOut) {

                val request = POSTransactionReportRequest().apply {
                    agentPhoneNumber = localStorage.agentPhone
                    institutionCode = localStorage.institutionCode
                    from = intent.extras?.getString("START_DATE")
                    to = intent.extras?.getString("END_DATE")
                    status = "${TransactionStatus.Successful.code}"
                }
                request.startIndex = "$startIndex"
                request.maxSize = "$maxSize"

                val (response, error) = safeRunIO {
                    creditClubMiddleWareAPI.reportService.getPOSTransactions(request)
                }
            } else {
                val startDate = intent.extras?.getString("START_DATE")
                val endDate = intent.extras?.getString("END_DATE")

                val (response, error) = safeRunIO {
                    creditClubMiddleWareAPI.reportService.getTransactions(
                        localStorage.agentPhone,
                        localStorage.institutionCode,
                        transactionTypeId.toInt(),
                        startDate,
                        endDate,
                        TransactionStatus.Successful.code,
                        startIndex,
                        maxSize
                    )
                }
            }

            hideProgressBar()
//            processFinished(response?.string())
            updateButtons()
        }
    }

    private fun setupReportView() {
        val myReportAdapter =
            ReportsAdapter(this@AccountReportActivity, report?.reports ?: arrayListOf(), reportType)
        binding.accountsListView.adapter = myReportAdapter
    }

    fun onNext(view: View) {
        if (startIndex + maxSize >= report?.totalCount ?: 0) {
            showError("There is no more data to display")
            return
        }

        startIndex += maxSize
        //refreshControls();
        loadReport()
    }

    fun onPrevious(view: View) {
        if (startIndex >= maxSize) {
            startIndex -= maxSize
        }
        loadReport()
        //refreshControls();
    }


    private inner class PosCashoutAdapter(override var values: List<PosTransactionReport.Report>) :
        SimpleBindingAdapter<PosTransactionReport.Report, ItemPosCashoutBinding>(R.layout.item_pos_cashout) {

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val transaction = values[position]

            holder.binding.amountTv.text = transaction.transactionAmount.toString()
            holder.binding.customerNameTv.text = transaction.customerAccountNumber
            holder.binding.phoneNoTv.text = transaction.customerPhoneNumber
            holder.binding.timeOccurredTv.text =
                transaction.dateLogged.toInstant(CREDIT_CLUB_DATE_PATTERN).timeAgo()
            holder.binding.toTv.text = null
        }
    }
}
