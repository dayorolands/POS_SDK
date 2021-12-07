package com.cluster.fragment

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.fragment.app.activityViewModels
import com.cluster.CustomerRequestOpenAccountActivity
import com.cluster.R
import com.cluster.databinding.FragmentCustomerRequestAccountInfoBinding
import com.cluster.ui.dataBinding
import com.creditclub.core.data.model.AccountInfo
import com.creditclub.core.type.TokenType
import com.creditclub.core.ui.CreditClubFragment
import com.creditclub.core.util.isKotlinNPE
import com.cluster.requireAndValidateToken
import com.creditclub.core.data.api.StaticService
import com.creditclub.core.data.api.retrofitService
import com.creditclub.core.util.safeRunIO
import kotlinx.coroutines.launch

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

        viewModel.run {
            phoneNumber.value = result.phoneNumber
            firstName.value = result.firstName
            surname.value = result.lastName
            dob.value = result.dob
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