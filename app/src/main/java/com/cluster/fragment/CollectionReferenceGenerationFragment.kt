package com.cluster.fragment

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import com.cluster.R
import com.cluster.databinding.FragmentCollectionReferenceGenerationBinding
import com.cluster.ui.dataBinding
import com.cluster.core.data.api.CollectionsService
import com.cluster.core.data.api.retrofitService
import com.cluster.core.data.request.CollectionCustomerValidationRequest
import com.cluster.core.data.request.CollectionReferenceGenerationRequest
import com.cluster.core.ui.CreditClubFragment
import com.cluster.core.util.safeRunIO
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import java.util.*

class CollectionReferenceGenerationFragment :
    CreditClubFragment(R.layout.fragment_collection_reference_generation) {

    private var argIsOffline = false
    private val request = CollectionReferenceGenerationRequest()

    private val binding by dataBinding<FragmentCollectionReferenceGenerationBinding>()
    private val viewModel: CollectionPaymentViewModel by navGraphViewModels(R.id.collectionGraph)
    private val uniqueReference = UUID.randomUUID().toString()
    private val collectionsService: CollectionsService by retrofitService()

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        (activity as AppCompatActivity).setSupportActionBar(binding.toolbar)
        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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

    private fun AutoCompleteTextView.clearSuggestions() {
        clearListSelection()
        val adapter = ArrayAdapter(requireContext(), R.layout.list_item, emptyList<String>())
        setAdapter(adapter)
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

    private suspend fun loadCategories() = viewModel.categoryList.download("categories") {
        collectionsService.getCollectionCategories(
            localStorage.institutionCode,
            derivedCollectionType,
            viewModel.region.value,
            viewModel.collectionService.value
        )
    }

    private suspend fun loadPaymentItems() =
        viewModel.itemList.download("payment items") {
            collectionsService.getCollectionPaymentItems(
                localStorage.institutionCode,
                viewModel.categoryCode.value,
                viewModel.region.value,
                viewModel.collectionService.value
            )
        }

    private suspend fun loadCustomer() {
        if (viewModel.customerType.value == null) {
            dialogProvider.showErrorAndWait(
                "Please select a customer ID type " +
                        "\n(N- for individuals and C- for companies)"
            )
            return
        }
        viewModel.customer.value = null
        dialogProvider.showProgressBar("Loading customer")
        val (response, error) = safeRunIO {
            collectionsService.getCollectionCustomer(
                localStorage.institutionCode,
                "${viewModel.customerType.value}${viewModel.customerId.value?.trim()}",
                viewModel.region.value,
                viewModel.collectionService.value
            )
        }
        dialogProvider.hideProgressBar()

        if (error != null) return dialogProvider.showErrorAndWait(error)
        response?.name ?: return dialogProvider.showErrorAndWait("Please enter a valid customer id")
        viewModel.customer.value = response
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

    private suspend fun generateReference() {
        if (viewModel.customerId.value.isNullOrBlank()) {
            return dialogProvider.showErrorAndWait("Please enter a customer id")
        } else if (viewModel.customer.value == null) {
            loadCustomer()
            viewModel.customer.value ?: return
        }

        if (viewModel.customerPhoneNumber.value.isNullOrBlank()) {
            return dialogProvider.showErrorAndWait("Please enter a phone number")
        }

        if (argIsOffline && viewModel.invoiceNumber.value.isNullOrBlank()) {
            return dialogProvider.showErrorAndWait("Please enter an invoice number")
        }

        if (viewModel.categoryCode.value.isNullOrBlank()) {
            return dialogProvider.showError("Please select a valid category")
        }

        if (!argIsOffline && viewModel.itemCode.value.isNullOrBlank()) {
            return dialogProvider.showError("Please select a valid payment item")
        }

        if (binding.amountInput.value.isBlank()) {
            return dialogProvider.showError("Please enter an amount")
        }

        val pin = dialogProvider.getPin("Agent PIN") ?: return
        if (pin.length != 4) return dialogProvider.showError("Agent PIN must be 4 digits long")

        val serializer = CollectionReferenceGenerationRequest.Additional.serializer()
        val agent = localStorage.agent
        val additional = CollectionReferenceGenerationRequest.Additional().apply {
            agentCode = agent?.agentCode
            terminalId = agent?.terminalID
        }

        request.apply {
            customerId =
                "${viewModel.customerType.value}${viewModel.customerId.value?.trim()}"
            if (argIsOffline) reference = viewModel.invoiceNumber.value?.trim()
            phoneNumber = viewModel.customerPhoneNumber.value?.trim()
            agentPin = pin
            region = viewModel.region.value
            categoryCode = viewModel.categoryCode.value
            collectionType = derivedCollectionType
            itemCode = if (argIsOffline) "4000000"
            else viewModel.itemCode.value

            amount = binding.amountInput.value.toDoubleOrNull()
            geoLocation = localStorage.lastKnownLocation
            currency = "NGN"
            referenceName = viewModel.paymentReferenceName.value
            institutionCode = localStorage.institutionCode
            agentPhoneNumber = localStorage.agentPhone
            collectionService = viewModel.collectionService.value
            applyFee = true
            requestReference = uniqueReference
            additionalInformation = Json.encodeToString(serializer, additional)
        }

        dialogProvider.showProgressBar("Processing request")
        val (response, error) = safeRunIO {
            collectionsService.generateCollectionReference(request)
        }
        dialogProvider.hideProgressBar()

        if (error != null) return dialogProvider.showError(error)
        response ?: return dialogProvider.showError("An error occurred while generating reference")

        if (response.isSuccessful == true) {
            dialogProvider.showSuccessAndWait(response.responseMessage ?: "Success")
        } else {
            dialogProvider.showError(response.responseMessage ?: "Error")
            return
        }

        response.apply {
            amount = request.amount
            referenceName = viewModel.customerName.value
            customerId = request.customerId
        }
        viewModel.referenceString.value = response.reference
        viewModel.collectionReference.value = response
        viewModel.amountString.value = response.amount?.toString()
        findNavController().popBackStack()
    }

    private inline val derivedCollectionType
        get() = viewModel.run {
            if (collectionTypeIsCbs.value == true && argIsOffline) "WEBGUID"
            else collectionType.value
        }
}