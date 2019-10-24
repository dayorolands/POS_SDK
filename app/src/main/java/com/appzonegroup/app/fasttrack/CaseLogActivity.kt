package com.appzonegroup.app.fasttrack

import android.os.Bundle
import android.widget.AdapterView
import android.widget.ArrayAdapter
import com.appzonegroup.app.fasttrack.databinding.ActivityCaseLogBinding
import com.appzonegroup.app.fasttrack.model.PostRequestBody
import com.appzonegroup.app.fasttrack.network.ApiServiceObject
import com.appzonegroup.app.fasttrack.utility.FunctionIds
import com.creditclub.core.data.model.CaseCategory
import com.creditclub.core.util.delegates.contentView
import com.creditclub.core.util.isValidEmail
import com.creditclub.core.util.localStorage
import com.creditclub.core.util.safeRunIO
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.layout_spinner.view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CaseLogActivity : BaseActivity() {
    val binding by contentView<CaseLogActivity, ActivityCaseLogBinding>(R.layout.activity_case_log)
//    override val functionId = FunctionIds.SUPPORT

    private var categoryList: List<CaseCategory>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mainScope.launch {
            showProgressBar("Loading products", "Please wait...")

            val (productList) = safeRunIO {
                creditClubMiddleWareAPI.caseLogService.getProducts()
            }

            productList ?: return@launch showNetworkError()

            run {
                val list = productList.toMutableList()
                list.add(0, "Select Product")

                val arrayAdapter = ArrayAdapter(this@CaseLogActivity, android.R.layout.simple_spinner_item, list)
                arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

                binding.products.spinner.adapter = arrayAdapter
            }

            showProgressBar("Loading categories", "Please wait...")

            val (data) = safeRunIO {
                creditClubMiddleWareAPI.caseLogService.getCaseCategories(localStorage.institutionCode)
            }

            categoryList = data

            categoryList ?: return@launch showNetworkError()

            run {
                val list = mutableListOf("Select Category")

                categoryList?.also {
                    for (category in it) {
                        list.add(category.name)
                    }
                }

                val arrayAdapter = ArrayAdapter(this@CaseLogActivity, android.R.layout.simple_spinner_item, list)
                arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

                binding.categories.adapter = arrayAdapter
            }

            hideProgressBar()
        }

        binding.nextBtn.setOnClickListener {
            val caseSubject = binding.subject.input.text.toString()
            if (caseSubject.isEmpty()) return@setOnClickListener showError("Please enter a subject")

            val caseDescription = binding.description.input.text.toString()
            if (caseDescription.isEmpty()) return@setOnClickListener showError("Please enter a description")

            val reporterEmail = binding.email.input.text.toString()
            if (reporterEmail.isEmpty()) return@setOnClickListener showError("Please enter your email")

            if (!reporterEmail.isValidEmail()) return@setOnClickListener showError("Email is invalid")

            if (binding.categories.selectedItemPosition == AdapterView.INVALID_POSITION || binding.categories.selectedItemPosition == 0)
                return@setOnClickListener showError("Please select a category")
            val selectedCategory = categoryList?.get(binding.categories.selectedItemPosition - 1)

            if (binding.products.selectedItemPosition == AdapterView.INVALID_POSITION || binding.products.selectedItemPosition == 0)
                return@setOnClickListener showError("Please select a product")

            val selectedProduct = binding.products.selectedItem as String

            val pin = binding.agentPin.input.text.toString()
            if (pin.isEmpty()) return@setOnClickListener showError("Please enter your PIN")
            if (pin.length!=4) return@setOnClickListener showError("PIN must be four digits")

            mainScope.launch {
                showProgressBar("Logging case", "Please wait...")

                val request = LogCaseRequest().apply {
                    institutionCode = localStorage.institutionCode
                    agentPin = pin
                    agentPhoneNumber = localStorage.agentPhone
                    product = selectedProduct
                    caseCategoryID = selectedCategory?.id
                    subject = caseSubject
                    caseReporterEmail = reporterEmail
                    description = caseDescription
                }

                val responseString = withContext(Dispatchers.Default) {
                    ApiServiceObject.run {
                        post("$BASE_URL/$CASE_LOG/LogCase", "$request")
                    }
                }

                hideProgressBar()

                val response = Gson().fromJson<LogCaseResponse?>(
                    responseString.value,
                    object : TypeToken<LogCaseResponse?>() {}.type
                )

                response ?: return@launch run {
                    showError("A network error occurred")
                }

                if (response.isSuccessful) {
                    showSuccess<Unit>("Cased logged successfully") {
                        onClose {
                            finish()
                        }
                    }
                } else showError(response.message ?: "An error occurred")
            }
        }
    }
//
//    class CaseCategory {
//        @SerializedName("ID")
//        var id = ""
//
//        @SerializedName("Name")
//        var name = ""
//    }

    class LogCaseRequest : PostRequestBody() {
        @SerializedName("InstitutionCode")
        var institutionCode: String? = ""

        @SerializedName("AgentPhoneNumber")
        var agentPhoneNumber: String? = ""

        @SerializedName("AgentPin")
        var agentPin = ""

        @SerializedName("Product")
        var product = ""

        @SerializedName("CaseCategoryID")
        var caseCategoryID: String? = ""

        @SerializedName("Subject")
        var subject = ""

        @SerializedName("Description")
        var description = ""

        @SerializedName("CaseReporterEmail")
        var caseReporterEmail = ""
    }

    class LogCaseResponse {
        @SerializedName("Status")
        var status: Boolean? = false

        @SerializedName("Code")
        var code: String? = ""

        val isSuccessful get() = code == "SUCCESS"

        @SerializedName("Message")
        var message: String? = null
    }
}
