package com.appzonegroup.app.fasttrack.fragment

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.fragment.app.activityViewModels
import com.appzonegroup.app.fasttrack.CustomerRequestOpenAccountActivity
import com.appzonegroup.app.fasttrack.R
import com.appzonegroup.app.fasttrack.databinding.FragmentCustomerRequestAccountInfoBinding
import com.appzonegroup.app.fasttrack.ui.dataBinding
import com.creditclub.core.data.model.AccountInfo
import com.creditclub.core.type.TokenType
import com.creditclub.core.ui.CreditClubFragment
import com.creditclub.core.util.isKotlinNPE
import com.creditclub.core.util.requireAndValidateToken
import com.creditclub.core.util.safeRunIO
import com.creditclub.core.util.showError
import com.google.firebase.crashlytics.FirebaseCrashlytics
import kotlinx.coroutines.launch

class AccountInfoFragment : CreditClubFragment(R.layout.fragment_customer_request_account_info) {
    private val activity get() = getActivity() as CustomerRequestOpenAccountActivity
    private val binding by dataBinding<FragmentCustomerRequestAccountInfoBinding>()
    private val viewModel by activityViewModels<OpenAccountViewModel>()
    private val requiresProducts = institutionConfig.flows.accountOpening.products

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.viewModel = viewModel
        binding.accountInfoNextBtn.setOnClickListener { next() }

        if (requiresProducts) {
            binding.productInputLayout.visibility = View.VISIBLE
            mainScope.launch { loadProducts() }
        } else {
            binding.productInputLayout.visibility = View.GONE
        }
    }

    private suspend fun loadProducts() {
//        val productDAO = ProductDAO(requireContext())
//        val products= productDAO.GetAll()
//        productDAO.close()
//        var productNames = products.map { it.name }

        dialogProvider.showProgressBar("Getting Products...")
        val (response, error) = safeRunIO {
            creditClubMiddleWareAPI.staticService.getAllProducts(
                localStorage.institutionCode,
                localStorage.agentPhone
            )
        }
        dialogProvider.hideProgressBar()
        if (error != null) return dialogProvider.showError(error)
        response ?: return dialogProvider.showError("No products available")

//        productDAO.Insert(response)

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
            dialogProvider.showError("Please enter the BVN")
            return
        }

        if (requiresProducts && viewModel.productName.value.isNullOrBlank()) {
            dialogProvider.showError("Please select a product")
            return
        }

        mainScope.launch { getCustomerBVN() }
    }

    private suspend fun getCustomerBVN() {
        dialogProvider.showProgressBar("Getting the BVN information...")
        val (result, error) = safeRunIO {
            creditClubMiddleWareAPI.staticService.getCustomerDetailsByBVN(
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

        try {
            viewModel.run {
                phoneNumber.value = result.phoneNumber
                firstName.value = result.firstName
                surname.value = result.lastName
                dob.value = result.dob
                middleName.value = result.otherNames
            }

            val accountInfo = AccountInfo()
            accountInfo.phoneNumber = result.phoneNumber

            activity.requireAndValidateToken(
                accountInfo,
                operationType = TokenType.AccountOpening
            ) {
                onSubmit {
                    viewModel.afterAccountInfo.value?.invoke()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            FirebaseCrashlytics.getInstance().recordException(Exception(e.message))

            dialogProvider.showError("An error occurred. Please try again")
        }
    }
}