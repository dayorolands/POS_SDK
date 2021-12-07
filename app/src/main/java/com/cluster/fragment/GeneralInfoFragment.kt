package com.cluster.fragment

import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.EditText
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.cluster.BuildConfig
import com.cluster.R
import com.cluster.databinding.FragmentCustomerRequestGeneralInfoBinding
import com.cluster.ui.dataBinding
import com.creditclub.core.data.api.StaticService
import com.creditclub.core.data.api.retrofitService
import com.creditclub.core.data.response.ApiResponse
import com.creditclub.core.data.response.isSuccessful
import com.creditclub.core.ui.CreditClubFragment
import com.creditclub.core.ui.widget.DateInputParams
import com.creditclub.core.util.*
import kotlinx.coroutines.launch
import java.time.LocalDate

class GeneralInfoFragment : CreditClubFragment(R.layout.fragment_customer_request_general_info) {
    private val binding by dataBinding<FragmentCustomerRequestGeneralInfoBinding>()
    private val viewModel by activityViewModels<OpenAccountViewModel>()
    private val staticService: StaticService by retrofitService()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.viewModel = viewModel
        viewModel.requiresEmail.observe(viewLifecycleOwner, Observer { requiresEmail ->
            binding.emailInputLayout.hint =
                if (requiresEmail == true) "Email" else "Email (optional)"
        })
        binding.placeOfBirthInputLayout.visibility = View.GONE

        if (BuildConfig.FLAVOR == "access") {
            binding.middleNameInputLayout.hint = "Middle name"
        }

        binding.dobInput.setOnClickListener {
            dialogProvider.showDateInput(
                DateInputParams(
                    "Date of Birth",
                    LocalDate.now().minusYears(13),
                    LocalDate.now().minusYears(200)
                )
            ) {
                onSubmit { date ->
                    val dateString = date.format("uuuu-MM-dd")
                    viewModel.dob.value = dateString
                    binding.dobInput.gravity = Gravity.START
                }
            }
        }

        val genderList = listOf("Female", "Male")
        val adapter = ArrayAdapter(requireContext(), R.layout.list_item, genderList)
        binding.genderInput.setAdapter(adapter)
        binding.genderInput.onItemClick { position ->
            viewModel.gender.value = genderList[position]
        }

        viewModel.requiresState.observe(viewLifecycleOwner, Observer { requiresState ->
            if (requiresState == true) {
                val stateArray = resources.getStringArray(R.array.States)
                val options = stateArray.map {
                    val stateInfo = it.split(",")
                    stateInfo[1]
                }
                val stateAdapter = ArrayAdapter(requireContext(), R.layout.list_item, options)
                binding.stateInput.setAdapter(stateAdapter)
                binding.stateInput.onItemClick { position ->
                    viewModel.stateCode.value = stateArray[position].split(",")[0]
                }
            }
        })

        binding.basicInfoNextBtn.setOnClickListener { next() }
        viewModel.run {
            addressState.onChange {
                binding.addressLgaInput.setText("")
                mainScope.launch { loadLgas() }
            }
        }
        viewModel.stateList.value ?: mainScope.launch { loadStates() }
    }

    private inline fun <T> MutableLiveData<T>.onChange(crossinline block: () -> Unit) {
        var oldValue = value
        observe(viewLifecycleOwner, Observer {
            if (value != oldValue) {
                oldValue = value
                block()
            }
        })
    }

    private inline fun AutoCompleteTextView.onItemClick(crossinline block: (position: Int) -> Unit) {
        setOnItemClickListener { _, _, position, _ ->
            block(position)
        }
    }

    private fun next() {
        if (!validate("Customer's surname", binding.surnameEt)) return
        if (!validate("Customer's first name", binding.firstNameEt)) return

        val requiresMiddleName = BuildConfig.FLAVOR == "access"
        if (!validate("Middle name", binding.middleNameEt, required = requiresMiddleName)) return

        if (viewModel.gender.value.isNullOrBlank()) {
            dialogProvider.showError("Please select a gender")
            return
        }

        val phoneNumber = viewModel.phoneNumber.value
        if (phoneNumber.isNullOrBlank()) {
            indicateError(
                "Please enter customer's phone number",
                binding.phoneEt
            )
            return
        }

        if (phoneNumber.length != 11) {
            indicateError(
                "Customer's phone number must be 11 digits",
                binding.phoneEt
            )
            return
        }

        if (viewModel.addressState.value == null) {
            return dialogProvider.showError("Please select a valid state")
        }

        if (viewModel.addressLga.value == null) {
            return dialogProvider.showError("Please select a valid lga")
        }

        val address = viewModel.address.value
        if (address.isNullOrBlank()) {
            indicateError(
                "Please enter customer's address",
                binding.addressEt
            )
            return
        }

        if (TextPatterns.invalidAddress.matcher(address).find()) {
            indicateError(
                getString(R.string.please_enter_a_valid_customer_address),
                binding.addressEt
            )
            return
        }

//        if (placeOfBirth.isEmpty()) {
//            indicateError(
//                "Please enter customer's place of birth",
//                Form.CONTACT_DETAILS.ordinal,
//                place_of_birth_et
//            )
//            return
//        }
//        if (!validate("Place of birth", placeOfBirth)) return

        if (viewModel.isWalletAccount.value == true && viewModel.stateCode.value.isNullOrBlank()) {
            dialogProvider.showError("Please select a state")
            return
        }

        val email = viewModel.email.value
        val requiresEmail = viewModel.requiresEmail.value == true
        if (requiresEmail && email.isNullOrBlank()) {
            return indicateError(
                resources.getString(
                    R.string.field_is_required,
                    "Email"
                ),
                binding.emailInput
            )
        }

        if (!email.isNullOrBlank() && !email.isValidEmail()) {
            return indicateError(getString(R.string.email_is_invalid), binding.emailInput)
        }


        if (viewModel.dob.value?.contains("Click") == true) {
            dialogProvider.showError("Please enter customer's date of birth")
            return
        }

        viewModel.afterGeneralInfo.value?.invoke()
    }

    private fun validate(name: String, editText: EditText, required: Boolean = true): Boolean {
        val value = editText.value
        if (required && value.isBlank()) {
            indicateError(resources.getString(R.string.field_is_required, name), editText)
            return false
        }

        if (value.includesSpecialCharacters() || value.includesNumbers()) {
            indicateError(
                resources.getString(
                    R.string.special_characters_not_permitted,
                    name
                ),
                editText
            )
            return false
        }

        return true
    }

    private fun indicateError(message: String, view: EditText?) {
        view?.error = message
        view?.requestFocus()
    }

    private suspend fun loadStates() = viewModel.stateList.download("states") {
        staticService.getStates(localStorage.institutionCode)
    }

    private suspend fun loadLgas() =
        viewModel.lgaList.download("lgas") {
            staticService.getLgas(
                localStorage.institutionCode,
                viewModel.addressState.value?.id
            )
        }

    private suspend inline fun <T> MutableLiveData<T>.download(
        dependencyName: String,
        crossinline fetcher: suspend () -> ApiResponse<T?>?
    ) {
        value = null
        dialogProvider.showProgressBar("Loading $dependencyName")
        val (response) = safeRunIO { fetcher() }
        dialogProvider.hideProgressBar()

        if (response == null) {
            dialogProvider.showErrorAndWait("An error occurred while loading $dependencyName")
            return
        }

        if (response.isSuccessful) {
            postValue(response.data)
            return
        }

        dialogProvider.showErrorAndWait(
            response.message ?: getString(R.string.an_error_occurred_please_try_again_later)
        )
    }
}