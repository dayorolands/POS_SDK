package com.appzonegroup.app.fasttrack.fragment

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
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
        viewModel.collectionReference.observe(viewLifecycleOwner, Observer {
            binding.completePaymentButton.isEnabled = it != null
        })

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
            binding.customerIdInput.text?.clear()
            viewModel.customer.postValue(null)
            viewModel.customerId.postValue(null)
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

    private suspend fun loadRegions() = loadDependencies("regions", binding.regionInput) {
        creditClubMiddleWareAPI.collectionsService.getCollectionRegions(
            localStorage.institutionCode,
            viewModel.collectionService.value
        )
    }

    private suspend fun loadCustomer() {
        if (viewModel.region.value.isNullOrBlank())
            return dialogProvider.showError("Please select a region")

        dialogProvider.showProgressBar("Loading customer")
        val (response) = safeRunIO {
            creditClubMiddleWareAPI.collectionsService.getCollectionCustomer(
                localStorage.institutionCode,
                viewModel.customerId.value,
                viewModel.region.value,
                viewModel.collectionService.value
            )
        }
        dialogProvider.hideProgressBar()

        viewModel.customer.value = response
    }

    private suspend fun loadReference() {
        if (viewModel.region.value.isNullOrBlank())
            return dialogProvider.showError("Please select a region")

        dialogProvider.showProgressBar("Loading reference")
        val (response) = safeRunIO {
            creditClubMiddleWareAPI.collectionsService.getCollectionReferenceByReference(
                localStorage.institutionCode,
                viewModel.reference.value,
                viewModel.region.value,
                viewModel.collectionService.value
            )
        }
        dialogProvider.hideProgressBar()

        viewModel.collectionReference.postValue(response)
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
        if (!viewModel.customerId.value.isNullOrBlank() && viewModel.customer.value == null) {
            loadCustomer()
        }

        if (viewModel.region.value.isNullOrBlank())
            return dialogProvider.showError("Please select a region")
        if (viewModel.customer.value == null)
            return dialogProvider.showError("Please enter a valid customer id")

        findNavController().navigate(R.id.action_collection_payment_to_reference_generation)
    }

    private suspend fun completePayment() {
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