package com.cluster

import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.cluster.utility.FunctionIds
import com.creditclub.core.data.request.MiniStatementRequest
import com.creditclub.core.ui.CreditClubActivity
import com.creditclub.core.ui.widget.DateInputParams
import com.creditclub.core.util.safeRunIO
import com.creditclub.core.util.toString
import com.creditclub.ui.adapter.MiniStatementAdapter
import com.creditclub.ui.dataBinding
import com.cluster.databinding.ActivityMiniStatementBinding
import com.creditclub.core.data.api.StaticService
import com.creditclub.core.data.api.retrofitService
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.Period

class MiniStatementActivity : CreditClubActivity(R.layout.activity_mini_statement) {
    private val binding: ActivityMiniStatementBinding by dataBinding()
    override val functionId = FunctionIds.AGENT_MINI_STATEMENT

    private val adapter by lazy { MiniStatementAdapter(this, emptyList()) }
    private var agentPIN = ""

    private var endDate = LocalDate.now()
    private var startDate = endDate.minusDays(6)
    private val staticService: StaticService by retrofitService()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        dialogProvider.requestPIN("Enter agent PIN") {
            onSubmit { pin ->
                agentPIN = pin

                if (agentPIN.length != 4) {
                    return@onSubmit dialogProvider.showError("Agent PIN must be 4 digits") {
                        onClose {
                            finish()
                        }
                    }
                }

                setSupportActionBar(binding.toolbar)
                supportActionBar?.setDisplayHomeAsUpEnabled(true)

                val today = LocalDate.now()
                val threeMonthsAgo = today.minusMonths(2)

                binding.content.endDateContentTv.text = endDate.toString("uuuu-MM-dd")
                binding.content.startDateContentTv.text = startDate.toString("uuuu-MM-dd")

                binding.refreshButton.setOnClickListener {
                    mainScope.launch { fetchMiniStatement() }
                }

                binding.content.startDateLayout.setOnClickListener {
                    val params = DateInputParams(
                        "Select start date",
                        maxDate = today,
                        minDate = threeMonthsAgo
                    )
                    dialogProvider.showDateInput(params) {
                        onSubmit { date ->
                            startDate = date
                            binding.content.startDateContentTv.text =
                                date.toString("uuuu-MM-dd")
                            mainScope.launch { fetchMiniStatement() }
                        }
                    }
                }

                binding.content.endDateLayout.setOnClickListener {
                    dialogProvider.showDateInput(
                        DateInputParams(
                            "Select end date",
                            maxDate = today,
                            minDate = threeMonthsAgo
                        )
                    ) {
                        onSubmit { date ->
                            endDate = date
                            binding.content.endDateContentTv.text = date.toString("uuuu-MM-dd")
                            mainScope.launch { fetchMiniStatement() }
                        }
                    }
                }

                binding.content.container.layoutManager =
                    LinearLayoutManager(this@MiniStatementActivity)
                binding.content.container.adapter = adapter
                mainScope.launch { fetchMiniStatement(closeOnFail = true) }
            }

            onClose {
                finish()
            }
        }
    }

    private suspend fun fetchMiniStatement(closeOnFail: Boolean = false) {
        val period = Period.between(startDate, endDate)
        if (period.days < 0) {
            return dialogProvider.showError("Start date must not be greater than end date")
        }

        if (period.days > 6) {
            return dialogProvider.showError("Date range must not be more than seven (7) days")
        }

        val request = MiniStatementRequest(
            agentPhoneNumber = localStorage.agentPhone,
            startDate = binding.content.startDateContentTv.text.toString(),
            endDate = binding.content.endDateContentTv.text.toString(),
            geoLocation = localStorage.lastKnownLocation,
            transactionCount = 20,
            institutionCode = localStorage.institutionCode,
            agentPin = agentPIN,
        )

        dialogProvider.showProgressBar("Getting transaction")
        val (response, error) = safeRunIO {
            staticService.miniStatement(request)
        }
        dialogProvider.hideProgressBar()

        if (error != null) return dialogProvider.showError(error)
        if (response == null) return showInternalError()

        if (!response.isSuccessful) {
            dialogProvider.showErrorAndWait(
                response.responseMessage ?: getString(R.string.an_error_occurred)
            )
            if (closeOnFail) finish()
            return
        }

        if (response.data?.isNullOrEmpty() == true) {
            dialogProvider.showError("You don't have any transaction for this period")
            return
        }

        adapter.setData(response.data)
    }
}
