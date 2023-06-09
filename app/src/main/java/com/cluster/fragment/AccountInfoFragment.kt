package com.cluster.fragment

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.fragment.app.activityViewModels
import com.cluster.CustomerRequestOpenAccountActivity
import com.cluster.R
import com.cluster.databinding.FragmentCustomerRequestAccountInfoBinding
import com.cluster.ui.dataBinding
import com.cluster.core.data.model.AccountInfo
import com.cluster.core.type.TokenType
import com.cluster.core.ui.CreditClubFragment
import com.cluster.core.util.isKotlinNPE
import com.cluster.requireAndValidateToken
import com.cluster.core.data.api.StaticService
import com.cluster.core.data.api.retrofitService
import com.cluster.core.util.format
import com.cluster.core.util.safeRunIO
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class AccountInfoFragment : CreditClubFragment(R.layout.fragment_customer_request_account_info) {
    private val activity get() = getActivity() as CustomerRequestOpenAccountActivity
    private val binding by dataBinding<FragmentCustomerRequestAccountInfoBinding>()
    private val viewModel by activityViewModels<OpenAccountViewModel>()
    private val staticService: StaticService by retrofitService()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.viewModel = viewModel
        binding.accountInfoNextBtn.setOnClickListener { next() }
        val requiresProducts = institutionConfig.flows.accountOpening?.products == true
        viewModel.requiresProduct.value = requiresProducts
        if (requiresProducts) {
            mainScope.launch { loadProducts() }
        }
    }

    private suspend fun loadProducts() {
        dialogProvider.showProgressBar("Getting Products...")
        val (response, error) = safeRunIO {
            staticService.getAllProducts(
                localStorage.institutionCode,
                localStorage.agentPhone
            )
        }
        dialogProvider.hideProgressBar()
        if (error != null) return dialogProvider.showError(error)
        response ?: return dialogProvider.showError("No products available")

        val productNames = response.map { it.name }
        val adapter = ArrayAdapter(requireContext(), R.layout.list_item, productNames)
        binding.productInput.setAdapter(adapter)
        binding.productInput.onItemClick { position ->
            val product = response[position]
            viewModel.productName.value = product.name
            viewModel.productCode.value = product.code
        }
    }

    private inline fun AutoCompleteTextView.onItemClick(crossinline block: (position: Int) -> Unit) {
        setOnItemClickListener { _, _, position, _ ->
            block(position)
        }
    }

    private fun next() {
        if (viewModel.bvn.value?.length != 11) {
            dialogProvider.showError("Please enter a valid BVN")
            return
        }

        if (viewModel.requiresProduct.value == true && viewModel.productName.value.isNullOrBlank()) {
            dialogProvider.showError("Please select a product")
            return
        }

        mainScope.launch { getCustomerBVN() }
    }

    private suspend fun getCustomerBVN() {
        dialogProvider.showProgressBar("Getting the BVN information...")
        val (result, error) = safeRunIO {
            staticService.getCustomerDetailsByBVN(
                localStorage.institutionCode,
                viewModel.bvn.value!!
            )
        }
        dialogProvider.hideProgressBar()

        if (error != null && error.isKotlinNPE()) return dialogProvider.showError("BVN is invalid")
        if (error != null) return dialogProvider.showError(error)
        if (result == null) {
            dialogProvider.showError("BVN is invalid")
            return
        }
        val newDateOfBirth = result.dob
        val formatter = DateTimeFormatter.ofPattern("dd-MMM-yyyy")
        val formattedDate = LocalDate.parse(newDateOfBirth, formatter)

        viewModel.run {
            phoneNumber.value = result.phoneNumber
            firstName.value = result.firstName
            surname.value = result.lastName
            dob.value = formattedDate.toString()
            middleName.value = result.otherNames
        }

        val accountInfo = AccountInfo(phoneNumber = result.phoneNumber)

        activity.requireAndValidateToken(accountInfo, operationType = TokenType.AccountOpening) {
            onSubmit {
                viewModel.afterAccountInfo.value?.invoke()
            }
        }
    }
}