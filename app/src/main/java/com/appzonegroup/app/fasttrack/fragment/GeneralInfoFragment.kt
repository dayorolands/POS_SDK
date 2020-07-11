package com.appzonegroup.app.fasttrack.fragment

import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.EditText
import androidx.fragment.app.activityViewModels
import com.appzonegroup.app.fasttrack.BuildConfig
import com.appzonegroup.app.fasttrack.R
import com.appzonegroup.app.fasttrack.databinding.FragmentCustomerRequestGeneralInfoBinding
import com.appzonegroup.app.fasttrack.ui.dataBinding
import com.creditclub.core.ui.CreditClubFragment
import com.creditclub.core.ui.widget.DateInputParams
import com.creditclub.core.util.*
import org.threeten.bp.LocalDate

class GeneralInfoFragment : CreditClubFragment(R.layout.fragment_customer_request_general_info) {
    private val binding by dataBinding<FragmentCustomerRequestGeneralInfoBinding>()
    private val viewModel by activityViewModels<OpenAccountViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.viewModel = viewModel
        binding.emailEt.hint = "Email"
        binding.placeOfBirthInputLayout.visibility = View.GONE

        if (BuildConfig.FLAVOR == "access") {
            binding.middleNameEt.hint = "Enter middle name"
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

        binding.basicInfoNextBtn.setOnClickListener { next() }
    }

    private inline fun AutoCompleteTextView.onItemClick(crossinline block: (position: Int) -> Unit) {
        setOnItemClickListener { _, _, position, _ ->
            block(position)
        }
    }

    private fun next() {
        val surname = viewModel.surname.value
        if (surname.isNullOrBlank()) {
            indicateError(
                "Please enter customer's surname",
                binding.surnameEt
            )
            return
        }

        if (!validate("Last name", surname)) return

        val middleName = viewModel.middleName.value

        if (BuildConfig.FLAVOR == "access") {

            if (middleName.isNullOrBlank()) {
                indicateError(
                    "Please enter customer's middle name",
                    binding.middleNameEt
                )
                return
            }
        }

        if (!validate("Middle name", middleName, required = false)) return

        val firstName = viewModel.firstName.value
        if (firstName.isNullOrBlank()) {
            indicateError(
                "Please enter customer's first name",
                binding.firstNameEt
            )
            return
        }

        if (!validate("First name", firstName)) return

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
        val email = viewModel.email.value
        if (email.isNullOrBlank()) {
            return dialogProvider.showError(
                resources.getString(
                    R.string.field_is_required,
                    "Email"
                )
            )
        }

        if (email.isNotEmpty() && !email.isValidEmail()) {
            return dialogProvider.showError(getString(R.string.email_is_invalid))
        }

//        if (!validate("Place of birth", placeOfBirth)) return

        if (viewModel.dob.value?.contains("Click") == true) {
            dialogProvider.showError("Please enter customer's date of birth")
            return
        }

        viewModel.afterGeneralInfo.value?.invoke()
    }

    private fun validate(name: String, value: String?, required: Boolean = true): Boolean {
        if (required && value.isNullOrBlank()) {
            dialogProvider.showError(resources.getString(R.string.field_is_required, name))
            return false
        }

        if (value == null) return true
        if (value.includesSpecialCharacters() || value.includesNumbers()) {
            dialogProvider.showError(
                resources.getString(
                    R.string.special_characters_not_permitted,
                    name
                )
            )
            return false
        }

        return true
    }

    private fun indicateError(message: String, view: EditText?) {
        view?.error = message
        view?.requestFocus()
    }
}