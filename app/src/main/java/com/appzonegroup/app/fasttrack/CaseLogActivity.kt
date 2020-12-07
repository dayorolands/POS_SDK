package com.appzonegroup.app.fasttrack

import android.os.Bundle
import androidx.activity.viewModels
import com.appzonegroup.app.fasttrack.databinding.ActivityCaseLogBinding
import com.appzonegroup.app.fasttrack.fragment.CaseLogViewModel
import com.creditclub.core.data.request.LogCaseRequest
import com.creditclub.core.ui.CreditClubActivity
import com.creditclub.core.util.*
import com.creditclub.ui.dataBinding
import kotlinx.coroutines.launch
import kotlinx.serialization.SerializationException

class CaseLogActivity : CreditClubActivity(R.layout.activity_case_log) {
    private val binding: ActivityCaseLogBinding by dataBinding()
    private val caseLogService = creditClubMiddleWareAPI.caseLogService
    private val viewModel: CaseLogViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.viewModel = viewModel
        mainScope.launch { loadCaseDependencies() }

        binding.nextBtn.setOnClickListener {
            mainScope.launch { logCase() }
        }
    }

    private suspend fun loadCaseDependencies() {
        dialogProvider.showProgressBar("Loading products", "Please wait...")
        val (productList) = safeRunIO {
            caseLogService.getProducts()
        }
        dialogProvider.hideProgressBar()

        productList ?: return showNetworkError()
        viewModel.productList.value = productList


        dialogProvider.showProgressBar("Loading categories", "Please wait...")
        val (categoryList) = safeRunIO {
            caseLogService.getCaseCategories(localStorage.institutionCode)
        }
        dialogProvider.hideProgressBar()

        categoryList ?: return showNetworkError()
        viewModel.categoryList.value = categoryList
    }

    private suspend fun logCase() {
        if (viewModel.subject.value.isNullOrBlank()) return showError("Please enter a subject")

        if (viewModel.description.value.isNullOrBlank()) return showError("Please enter a description")

        val reporterEmail = viewModel.agentEmail.value
        if (reporterEmail.isNullOrBlank()) return showError("Please enter your email")
        if (!reporterEmail.isValidEmail()) return showError("Email is invalid")

        if (viewModel.category.value == null) return showError("Please select a category")
        if (viewModel.product.value == null) return showError("Please select a product")

        val pin = dialogProvider.getPin("Agent PIN") ?: return
        if (pin.isEmpty()) return showError("Please enter your PIN")
        if (pin.length != 4) return showError("PIN must be four digits")

        val request = LogCaseRequest().apply {
            institutionCode = localStorage.institutionCode
            agentPin = pin
            agentPhoneNumber = localStorage.agentPhone
            product = viewModel.product.value
            caseCategoryID = viewModel.category.value?.id
            subject = viewModel.subject.value
            caseReporterEmail = reporterEmail
            description = viewModel.description.value
        }

        dialogProvider.showProgressBar("Logging case", "Please wait...")
        val (response, error) = safeRunIO {
            caseLogService.logCase(request)
        }
        dialogProvider.hideProgressBar()

        if (error != null && (error is SerializationException || error.isKotlinNPE())) {
            return dialogProvider.showError("Unable to log case")
        }
        if (error != null) return dialogProvider.showError(error)
        response ?: return dialogProvider.showError("Unable to log case")

        if (response.isSuccessful) {
            dialogProvider.showSuccess<Nothing>("Cased logged successfully") {
                onClose {
                    finish()
                }
            }
        } else showError(response.message ?: "An error occurred")
    }
}
