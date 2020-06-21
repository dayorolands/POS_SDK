package com.appzonegroup.app.fasttrack.fragment

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.appzonegroup.app.fasttrack.R
import com.appzonegroup.app.fasttrack.databinding.CollectionPaymentFragmentBinding
import com.appzonegroup.app.fasttrack.receipt.CollectionPaymentReceipt
import com.appzonegroup.app.fasttrack.ui.dataBinding
import com.appzonegroup.app.fasttrack.utility.FunctionIds
import com.appzonegroup.creditclub.pos.Platform
import com.appzonegroup.creditclub.pos.printer.PosPrinter
import com.creditclub.core.data.request.CollectionPaymentRequest
import com.creditclub.core.ui.CreditClubFragment
import com.creditclub.core.util.*
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import java.util.*

class CollectionPaymentFragment : CreditClubFragment(R.layout.collection_payment_fragment) {
    private val posPrinter: PosPrinter by lazy { PosPrinter(requireContext(), dialogProvider) }
    private val binding by dataBinding<CollectionPaymentFragmentBinding>()
    private val viewModel: CollectionPaymentViewModel by activityViewModels()
    override val functionId = FunctionIds.COLLECTION_PAYMENT
    private val request = CollectionPaymentRequest()
    private val uniqueReference = UUID.randomUUID().toString()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainScope.launch { loadRegions() }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        (activity as AppCompatActivity).setSupportActionBar(binding.toolbar)
        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.viewModel = viewModel
        binding.toolbar.title = "Collection Payment"
        binding.completePaymentButton.setOnClickListener {
            mainScope.launch { completePayment() }
        }

        binding.generateReferenceButton.setOnClickListener {
            mainScope.launch { onGenerateButtonClick() }
        }

        binding.customerIdInputLayout.setEndIconOnClickListener {
            mainScope.launch { loadCustomer() }
        }

        binding.collectionReferenceInputLayout.setEndIconOnClickListener {
            mainScope.launch { loadReference() }
        }

        binding.regionInput.onItemClick {
            viewModel.region.postValue(binding.regionInput.value)
            viewModel.customer.postValue(null)
        }

        binding.customerIdInput.onChange {
            viewModel.customer.postValue(null)
            viewModel.collectionReference.postValue(null)
        }
    }

    private inline fun AutoCompleteTextView.onItemClick(crossinline block: (position: Int) -> Unit) {
        var oldValue = value
        setOnItemClickListener { _, _, position, _ ->
            if (value != oldValue) {
                oldValue = value
                block(position)
            }
        }
    }

    private inline fun TextInputEditText.onChange(crossinline block: () -> Unit) {
        var oldValue = value.trim()
        addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (value.trim() != oldValue) {
                    oldValue = value
                    block()
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private suspend fun loadRegions() = loadDependencies("regions", binding.regionInput) {
        creditClubMiddleWareAPI.collectionsService.getCollectionRegions(
            localStorage.institutionCode,
            viewModel.collectionService.value
        )
    }

    private suspend fun loadCustomer() {
        if (viewModel.region.value.isNullOrBlank())
            return dialogProvider.showError("Please select a region")

        viewModel.customer.value = null
        dialogProvider.showProgressBar("Loading customer")
        val (response, error) = safeRunIO {
            creditClubMiddleWareAPI.collectionsService.getCollectionCustomer(
                localStorage.institutionCode,
                viewModel.customerId.value,
                viewModel.region.value,
                viewModel.collectionService.value
            )
        }
        dialogProvider.hideProgressBar()

        if (error != null) return dialogProvider.showError(error)
        response?.name ?: return dialogProvider.showError("Please enter a valid customer id")
        viewModel.customer.value = response
    }

    private suspend fun loadReference() {
        if (viewModel.region.value.isNullOrBlank())
            return dialogProvider.showError("Please select a region")

        viewModel.collectionReference.value = null
        dialogProvider.showProgressBar("Loading reference")
        val (response, error) = safeRunIO {
            creditClubMiddleWareAPI.collectionsService.getCollectionReferenceByReference(
                localStorage.institutionCode,
                viewModel.reference.value,
                viewModel.region.value,
                viewModel.collectionService.value
            )
        }
        dialogProvider.hideProgressBar()

        if (error != null) return dialogProvider.showError(error)
        response?.reference ?: return dialogProvider.showError("Please enter a valid reference")
        if (response.isSuccessful != true) {
            return dialogProvider.showError(response.responseMessage)
        }
        viewModel.collectionReference.value = response
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

    private suspend fun onGenerateButtonClick() {
        if (viewModel.region.value.isNullOrBlank()) {
            return dialogProvider.showError("Please select a region")
        }

        if (viewModel.customerId.value.isNullOrBlank()) {
            return dialogProvider.showError("Please enter a customer id")
        } else if (viewModel.customer.value == null) {
            loadCustomer()
            viewModel.customer.value ?: return
        }

        if (viewModel.customerPhoneNumber.value.isNullOrBlank()) {
            return dialogProvider.showError("Please enter a phone number")
        }

        findNavController().navigate(R.id.action_collection_payment_to_reference_generation)
    }

    private suspend fun completePayment() {
        if (viewModel.customerId.value.isNullOrBlank()) {
            return dialogProvider.showError("Please enter a customer id")
        } else if (viewModel.customer.value == null) {
            loadCustomer()
            viewModel.customer.value ?: return
        }

        if (viewModel.customerPhoneNumber.value.isNullOrBlank()) {
            return dialogProvider.showError("Please enter a phone number")
        }

        if (viewModel.reference.value.isNullOrBlank()) {
            return dialogProvider.showError("Please enter a reference")
        } else if (viewModel.collectionReference.value == null) {
            loadReference()
            viewModel.collectionReference.value ?: return
        }

        val pin = dialogProvider.getPin("Agent PIN") ?: return

        val json = Json(JsonConfiguration.Stable)
        val serializer = CollectionPaymentRequest.Additional.serializer()
        val additional = CollectionPaymentRequest.Additional().apply {
            agentCode = localStorage.agent?.agentCode
            terminalId = localStorage.agent?.terminalID
        }
        request.apply {
            collectionReference = viewModel.collectionReference.value?.reference
            agentPin = pin
            region = viewModel.region.value
            categoryCode = viewModel.categoryCode.value
            itemCode = viewModel.itemCode.value
            amount = viewModel.collectionReference.value?.amount
            geoLocation = gps.geolocationString
            currency = "NGN"
            institutionCode = localStorage.institutionCode
            agentPhoneNumber = localStorage.agentPhone
            collectionService = viewModel.collectionService.value
            requestReference = uniqueReference
            additionalInformation = json.stringify(serializer, additional)
        }
        dialogProvider.showProgressBar("Processing request")
        val (response, error) = safeRunIO {
            creditClubMiddleWareAPI.collectionsService.collectionPayment(request)
        }
        dialogProvider.hideProgressBar()
        if (error != null) return dialogProvider.showError(error)
        response ?: return dialogProvider.showError("An error occurred. Please try again later")

        if (response.isSuccessful == true) {
            dialogProvider.showSuccessAndWait(response.responseMessage ?: "Success")
        } else {
            dialogProvider.showErrorAndWait(response.responseMessage ?: "Error")
        }

        if (Platform.hasPrinter) {
            posPrinter.print(CollectionPaymentReceipt(requireContext(), response))
        }

        activity?.onBackPressed()
    }
}