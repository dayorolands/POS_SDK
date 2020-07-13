package com.appzonegroup.app.fasttrack.fragment

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.navGraphViewModels
import com.appzonegroup.app.fasttrack.R
import com.appzonegroup.app.fasttrack.databinding.FragmentCollectionReferenceGenerationBinding
import com.appzonegroup.app.fasttrack.ui.dataBinding
import com.creditclub.core.data.model.CollectionCategory
import com.creditclub.core.data.model.CollectionPaymentItem
import com.creditclub.core.data.request.CollectionReferenceGenerationRequest
import com.creditclub.core.ui.CreditClubFragment
import com.creditclub.core.util.*
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import java.util.*

class CollectionReferenceGenerationFragment :
    CreditClubFragment(R.layout.fragment_collection_reference_generation) {

    private val args by navArgs<CollectionReferenceGenerationFragmentArgs>()
    private val request = CollectionReferenceGenerationRequest()

    private var paymentItems: List<CollectionPaymentItem>? = null
    private var categories: List<CollectionCategory>? = null
    private val binding by dataBinding<FragmentCollectionReferenceGenerationBinding>()
    private val viewModel: CollectionPaymentViewModel by navGraphViewModels(R.id.collectionGraph)
    private val uniqueReference = UUID.randomUUID().toString()

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        (activity as AppCompatActivity).setSupportActionBar(binding.toolbar)
        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.isOffline.value = args.offline

        binding.viewModel = viewModel

        mainScope.launch { loadCategories() }

        binding.categoryInput.onItemClick { position ->
            viewModel.category.value = categories?.get(position)
            if (!args.offline) {
                binding.paymentItemInput.clearSuggestions()

                mainScope.launch { loadPaymentItems() }
            }
        }

        binding.paymentItemInput.onItemClick { position ->
            viewModel.item.value = paymentItems?.get(position)
            viewModel.itemCode.value = paymentItems?.get(position)?.code
        }

        binding.generateReferenceButton.setOnClickListener {
            mainScope.launch { generateReference() }
        }

        binding.customerIdInputLayout.setEndIconOnClickListener {
            mainScope.launch { loadCustomer() }
        }

        viewModel.customerId.onChange {
            viewModel.customer.postValue(null)
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

    private inline fun AutoCompleteTextView.onItemClick(crossinline block: (position: Int) -> Unit) {
        setOnItemClickListener { _, _, position, _ ->
            block(position)
        }
    }

    private fun AutoCompleteTextView.clearSuggestions() {
        clearListSelection()
        val adapter = ArrayAdapter(requireContext(), R.layout.list_item, emptyList<String>())
        setAdapter(adapter)
    }

    private suspend fun loadCategories() = loadDependencies("categories", binding.categoryInput) {
        categories = creditClubMiddleWareAPI.collectionsService.getCollectionCategories(
            localStorage.institutionCode,
            viewModel.collectionType.value,
            viewModel.region.value,
            viewModel.collectionService.value
        )
        categories?.map { "${it.name} - ${it.code}" }
    }

    private suspend fun loadPaymentItems() =
        loadDependencies("payment items", binding.paymentItemInput) {
            paymentItems = creditClubMiddleWareAPI.collectionsService.getCollectionPaymentItems(
                localStorage.institutionCode,
                viewModel.categoryCode.value,
                viewModel.region.value,
                viewModel.collectionService.value
            )
            paymentItems?.map { "${it.name} - ${it.code}" }
        }

    private suspend fun loadCustomer() {
        if (viewModel.region.value.isNullOrBlank())
            return dialogProvider.showErrorAndWait("Please select a region")

        viewModel.customer.value = null
        dialogProvider.showProgressBar("Loading customer")
        val (response, error) = safeRunIO {
            creditClubMiddleWareAPI.collectionsService.getCollectionCustomer(
                localStorage.institutionCode,
                viewModel.customerId.value?.trim(),
                viewModel.region.value,
                viewModel.collectionService.value
            )
        }
        dialogProvider.hideProgressBar()

        if (error != null) return dialogProvider.showErrorAndWait(error)
        response?.name ?: return dialogProvider.showErrorAndWait("Please enter a valid customer id")
        viewModel.customer.value = response
    }

    private suspend inline fun loadDependencies(
        dependencyName: String,
        autoCompleteTextView: AutoCompleteTextView,
        crossinline block: suspend () -> List<String>?
    ) {
        dialogProvider.showProgressBar("Loading $dependencyName")
        val (items) = safeRunIO { block() }
        dialogProvider.hideProgressBar()

        if (items == null) {
            dialogProvider.showError<Nothing>("An error occurred while loading $dependencyName") {
                onClose {
//                    findNavController().popBackStack()
                }
            }
            return
        }

        val adapter = ArrayAdapter(requireContext(), R.layout.list_item, items)
        autoCompleteTextView.setAdapter(adapter)
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

        if (args.offline && viewModel.invoiceNumber.value.isNullOrBlank()) {
            return dialogProvider.showErrorAndWait("Please enter an invoice number")
        }

        if (viewModel.categoryCode.value.isNullOrBlank()) {
            return dialogProvider.showError("Please select a valid category")
        }

        if (!args.offline && viewModel.itemCode.value.isNullOrBlank()) {
            return dialogProvider.showError("Please select a valid payment item")
        }

        if (binding.amountInput.value.isBlank()) {
            return dialogProvider.showError("Please enter an amount")
        }

        val pin = dialogProvider.getPin("Agent PIN") ?: return

        val json = Json(JsonConfiguration.Stable)
        val serializer = CollectionReferenceGenerationRequest.Additional.serializer()
        val additional = CollectionReferenceGenerationRequest.Additional().apply {
            agentCode = localStorage.agent?.agentCode
            terminalId = localStorage.agent?.terminalID
        }

        request.apply {
            customerId = viewModel.customerId.value?.trim()
            if (args.offline) reference = viewModel.invoiceNumber.value?.trim()
            phoneNumber = viewModel.customerPhoneNumber.value?.trim()
            agentPin = pin
            region = viewModel.region.value
            categoryCode = viewModel.categoryCode.value
            collectionType = viewModel.collectionType.value

            itemCode = if (args.offline) "4000000"
            else viewModel.itemCode.value

            amount = binding.amountInput.value.toDoubleOrNull()
            geoLocation = gps.geolocationString
            currency = "NGN"
            referenceName = viewModel.customer.value?.name
            institutionCode = localStorage.institutionCode
            agentPhoneNumber = localStorage.agentPhone
            collectionService = viewModel.collectionService.value
            applyFee = true
            requestReference = uniqueReference
            additionalInformation = json.stringify(serializer, additional)
        }

        dialogProvider.showProgressBar("Processing request")
        val (response, error) = safeRunIO {
            creditClubMiddleWareAPI.collectionsService.generateCollectionReference(request)
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
            referenceName = request.referenceName
            customerId = request.customerId
        }
        viewModel.referenceString.value = response.reference
        viewModel.collectionReference.value = response
        viewModel.amountString.value = response.amount?.toString()
        findNavController().popBackStack()
    }
}