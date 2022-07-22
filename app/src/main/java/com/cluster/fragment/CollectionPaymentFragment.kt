package com.cluster.fragment

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import com.cluster.R
import com.cluster.core.data.api.CollectionsService
import com.cluster.core.data.api.retrofitService
import com.cluster.core.data.model.CollectionPaymentItem
import com.cluster.core.data.request.CollectionCustomerValidationRequest
import com.cluster.core.ui.CreditClubFragment
import com.cluster.core.util.safeRunIO
import com.cluster.databinding.CollectionPaymentFragmentBinding
import com.cluster.pos.Platform
import com.cluster.pos.printer.PosPrinter
import com.cluster.receipt.collectionPaymentReceipt
import com.cluster.ui.dataBinding
import com.cluster.utility.FunctionIds
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import org.koin.android.ext.android.inject
import org.koin.core.parameter.parametersOf
import java.time.Instant
import java.util.*

class CollectionPaymentFragment : CreditClubFragment(R.layout.collection_payment_fragment) {
    private val binding by dataBinding<CollectionPaymentFragmentBinding>()
    private val viewModel: CollectionPaymentViewModel by navGraphViewModels(R.id.collectionGraph)
    override val functionId = FunctionIds.COLLECTION_PAYMENT
    private val collectionsService: CollectionsService by retrofitService()
    private val customerValidationRequest = CollectionCustomerValidationRequest()
    private var isFixedAmount = false

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        (activity as AppCompatActivity).setSupportActionBar(binding.toolbar)
        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.viewModel = viewModel
        binding.toolbar.title = "Collections"
        mainScope.launch {
            loadBillers()
        }

        viewModel.run {
            billerList.bindDropDown(billers, binding.billerInput)
            paymentItemList.bindDropDown(paymentItem, binding.paymentItemInput)

            viewModel.billerName.onChange {
                mainScope.launch {
                    binding.amountInput.value = ""
                    paymentItemName.value = ""
                    binding.customerValueInput.value = ""
                    loadCollectionPaymentItems()
                }
            }

            viewModel.paymentItem.onChange {
                binding.amountInput.visibility = View.INVISIBLE
                isFixedAmount = viewModel.paymentItem.value?.isFixedAmount!!

                viewModel.isFixedAmountCheck.value = viewModel.paymentItem.value?.isFixedAmount
                if(isFixedAmount) {
                    binding.amountInput.isEnabled = false
                    binding.amountInput.visibility = View.VISIBLE
                    binding.amountInput.value = viewModel.paymentItem.value?.amount!!
                }else{
                    binding.amountInput.visibility = View.INVISIBLE
                    binding.amountInput.value = "0"
                }

                binding.customerValueInput.visibility = View.VISIBLE
                val customFields = viewModel.paymentItem.value?.customFields
                if(!customFields.isNullOrEmpty()) {
                    binding.customerValueInput.setHint(
                        customFields[0].displayText ?: ""
                    )
                }
            }
        }



        binding.validateCustomerBtn.setOnClickListener{
            mainScope.launch {
                validateCustomer()
            }
        }

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

    @Suppress("UNCHECKED_CAST")
    private fun <T> MutableLiveData<List<T>>.bindDropDown(
        selectedItemLiveData: MutableLiveData<T>,
        autoCompleteTextView: AutoCompleteTextView
    ) {
        observe(viewLifecycleOwner, Observer { list ->
            val items = list ?: emptyList()
            val adapter = ArrayAdapter(requireContext(), R.layout.list_item, items)
            autoCompleteTextView.setAdapter(adapter)
            if (list != null) {
                autoCompleteTextView.setOnItemClickListener { parent, _, position, _ ->
                    selectedItemLiveData.postValue(parent.getItemAtPosition(position) as T)
                }
            }
        })
    }

    private suspend fun loadBillers() = viewModel.billerList.download("billers"){
        collectionsService.getCollectionBillers(
            localStorage.institutionCode,
            viewModel.collectionService.value
        )
    }

    private suspend fun validateCustomer(){
        if (binding.customerValueInput.value.isBlank()) {
            dialogProvider.showError("Please enter a valid customer value")
        } else{
            viewModel.customerValue.value = binding.customerValueInput.value
        }

        viewModel.paymentItemAmount.value = binding.amountInput.value

        Log.d("OkHttpClient", "Checking the amount Input for the transaction ${binding.amountInput.value}")
        Log.d("OkHttpClient", "Checking the amount Input for the transaction ${viewModel.paymentItemAmount.value}")


        var customFieldRequest = arrayListOf<CollectionCustomerValidationRequest.CustomFields>()
        val customerFieldCheck = viewModel.paymentItem.value?.customFields
        if(!customerFieldCheck.isNullOrEmpty()) {
            val items = CollectionCustomerValidationRequest.CustomFields().apply {
                id = customerFieldCheck[0].customId
                name = customerFieldCheck[0].customName
                value = viewModel.customerValue.value
            }
            customFieldRequest.add(items)
        }
        customerValidationRequest.apply{
            channel = "mobile"
            itemCode = viewModel.paymentItem.value?.code
            amount = viewModel.paymentItemAmount.value!!.toDoubleOrNull()
            customFields = customFieldRequest
            customerPhoneNumber = localStorage.agentPhone
            customerName = ""
            customerEmail = ""
            institutionCode = localStorage.institutionCode
            agentPhoneNumber = localStorage.agentPhone
        }

        dialogProvider.showProgressBar("Validating customer")
        val (response, error) = safeRunIO {
            collectionsService.validateCustomer(customerValidationRequest)
        }
        dialogProvider.hideProgressBar()

        if (error != null) return dialogProvider.showError(error)
        response ?: return dialogProvider.showError("An error occurred while generating reference")

        if (response.isSuccessful == true) {
            viewModel.acceptPartPayment.value = response.result!!.acceptPartPayment
            viewModel.paymentReferece.value = response.result!!.paymentReference
            //viewModel.feeAmount.value = response.result!!.surcharge?.toInt()
            viewModel.customerName.value = response.result!!.customerName
            viewModel.amountDue.value = response.result!!.amountDue?.toInt()

            findNavController().navigate(R.id.action_collection_payment_to_reference_generation)
        }
        else {
            dialogProvider.showError(response.message ?: "Error")
            return
        }
    }

    private suspend fun loadCollectionPaymentItems() = viewModel.paymentItemList.download("biller items"){
        collectionsService.getCollectionPaymentItems(
            localStorage.institutionCode,
            viewModel.billerId.value
        )
    }

    private suspend inline fun <T> MutableLiveData<T>.download(
        dependencyName: String,
        crossinline fetcher: suspend () -> T?
    ) {
        dialogProvider.showProgressBar("Loading $dependencyName")
        val (data) = safeRunIO { fetcher() }
        dialogProvider.hideProgressBar()

        if (data == null) {
            dialogProvider.showErrorAndWait("An error occurred while loading $dependencyName")
            return
        }
        postValue(data)
    }
}
