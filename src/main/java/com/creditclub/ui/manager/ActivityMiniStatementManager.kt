package com.creditclub.ui.manager

import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.creditclub.core.data.request.MiniStatementRequest
import com.creditclub.core.ui.CreditClubActivity
import com.creditclub.core.ui.widget.DateInputParams
import com.creditclub.core.util.localStorage
import com.creditclub.core.util.safeRunIO
import com.creditclub.core.util.toString
import com.creditclub.ui.adapter.MiniStatementAdapter
import com.creditclub.ui.databinding.ActivityMiniStatementBinding
import kotlinx.coroutines.launch
import org.threeten.bp.LocalDate


/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 24/09/2019.
 * Appzone Ltd
 */
class ActivityMiniStatementManager(
    override val activity: CreditClubActivity,
    override val binding: ActivityMiniStatementBinding
) : DataBindingActivityManager<ActivityMiniStatementBinding>(activity) {

    private val adapter by lazy { MiniStatementAdapter(activity, emptyList()) }
    private var agentPIN = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestAgentPIN("Enter agent PIN") {
            onSubmit { pin ->
                activity.run {
                    agentPIN = pin

                    if (agentPIN.length != 4) {
                        return@onSubmit showError<Nothing>("Agent PIN must be 4 digits") {
                            onClose {
                                finish()
                            }
                        }
                    }

                    setSupportActionBar(binding.toolbar)
                    supportActionBar?.setDisplayHomeAsUpEnabled(true)

                    val today = LocalDate.now()

                    binding.content.endDateContentTv.text = today.toString("uuuu-MM-dd")
                    binding.content.startDateContentTv.text =
                        today.minusMonths(3).toString("uuuu-MM-dd")

                    binding.refreshButton.setOnClickListener {
                        fetchMiniStatement()
                    }

                    binding.content.startDateLayout.setOnClickListener {
                        showDateInput(DateInputParams("Select start date", maxDate = today)) {
                            onSubmit { date ->
                                binding.content.startDateContentTv.text =
                                    date.toString("uuuu-MM-dd")
                                fetchMiniStatement()
                            }
                        }
                    }

                    binding.content.endDateLayout.setOnClickListener {
                        showDateInput(DateInputParams("Select end date", maxDate = today)) {
                            onSubmit { date ->
                                binding.content.endDateContentTv.text = date.toString("uuuu-MM-dd")
                                fetchMiniStatement()
                            }
                        }
                    }

                    binding.content.container.layoutManager = LinearLayoutManager(this@run)
                    binding.content.container.adapter = adapter
                    fetchMiniStatement(closeOnFail = true)
                }
            }

            onClose {
                activity.finish()
            }
        }
    }

    private fun fetchMiniStatement(closeOnFail: Boolean = false) {
        activity.run {
            mainScope.launch {
                val request = MiniStatementRequest().apply {
                    agentPhoneNumber = localStorage.agentPhone
                    startDate = binding.content.startDateContentTv.text.toString()
                    endDate = binding.content.endDateContentTv.text.toString()
                    geoLocation = gps.geolocationString
                    transactionCount = 20
                    institutionCode = localStorage.institutionCode
                    agentPin = this@ActivityMiniStatementManager.agentPIN
                }

                showProgressBar("Getting transaction")
                val (response, error) = safeRunIO {
                    creditClubMiddleWareAPI.staticService.miniStatement(request)
                }
                hideProgressBar()

                if (error != null) return@launch showError(error)
                response ?: return@launch showInternalError()

                if (response.isSuccessful) {
                    adapter.setData(response.data)
                } else {
                    showError<Nothing>(response.responseMessage) {
                        onClose {
                            if (closeOnFail) finish()
                        }
                    }
                }
            }
        }
    }
}