package com.appzonegroup.app.fasttrack.fragment

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import com.appzonegroup.app.fasttrack.R
import com.appzonegroup.app.fasttrack.databinding.CollectionPaymentFragmentBinding
import com.appzonegroup.app.fasttrack.receipt.CollectionPaymentReceipt
import com.appzonegroup.app.fasttrack.ui.dataBinding
import com.appzonegroup.app.fasttrack.utility.FunctionIds
import com.appzonegroup.creditclub.pos.Platform
import com.creditclub.core.data.request.CollectionPaymentRequest
import com.creditclub.core.ui.CreditClubFragment
import com.creditclub.core.util.getPin
import com.creditclub.core.util.safeRunIO
import com.creditclub.core.util.showErrorAndWait
import com.creditclub.core.util.showSuccessAndWait
import com.creditclub.pos.printer.PosPrinter
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import org.koin.android.ext.android.inject
import org.koin.core.parameter.parametersOf
import org.threeten.bp.Instant
import java.util.*

class CollectionPaymentFragment : CreditClubFragment(R.layout.collection_payment_fragment) {
    private var regions: List<String>? = null
    private var collectionTypes: List<String>? = null
    private val posPrinter: PosPrinter by inject { parametersOf(requireContext(), dialogProvider) }
    private val binding by dataBinding<CollectionPaymentFragmentBinding>()
    private val viewModel: CollectionPaymentViewModel by navGraphViewModels(R.id.collectionGraph)
    override val functionId = FunctionIds.COLLECTION_PAYMENT
    private val request = CollectionPaymentRequest()
    private val uniqueReference = UUID.randomUUID().toString()

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        (activity as AppCompatActivity).setSupportActionBar(binding.toolbar)
        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.viewModel = viewModel
        binding.toolbar.title = "IGR Collections"
        mainScope.launch { loadRegions() }
        binding.completePaymentButton.setOnClickListener {
            mainScope.launch { completePayment() }
        }

        binding.generateReferenceButton.setOnClickListener {
            mainScope.launch { onGenerateButtonClick(false) }
        }

        binding.generateOfflineBillButton.setOnClickListener {
            mainScope.launch { onGenerateButtonClick(true) }
        }

        binding.collectionReferenceInputLayout.setEndIconOnClickListener {
            mainScope.launch { loadReference() }
        }

        binding.collectionReferenceInput.doOnTextChanged { _, _, _, _ ->
            if (viewModel.collectionReference.value == null) {
                viewModel.collectionReference.postValue(null)
            }
        }

        viewModel.region.onChange {
            mainScope.launch {
                viewModel.customer.postValue(null)
                collectionTypes = null
                loadCollectionTypes()
            }
        }

        viewModel.collectionType.onChange {
            viewModel.collectionReference.postValue(null)
        }

        if (viewModel.region.value != null) {
            mainScope.launch { loadCollectionTypes() }
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

    private suspend fun loadRegions() = loadDependencies("regions", regions, binding.regionInput) {
        regions = creditClubMiddleWareAPI.collectionsService.getCollectionRegions(
            localStorage.institutionCode,
            viewModel.collectionService.value
        )
        regions
    }

    private suspend fun loadCollectionTypes() =
        loadDependencies("collection types", collectionTypes, binding.collectionTypeInput) {
            collectionTypes = creditClubMiddleWareAPI.collectionsService.getCollectionTypes(
                localStorage.institutionCode,
                viewModel.region.value,
                viewModel.collectionService.value
            )
            collectionTypes
        }

    private suspend fun loadReference() {
        if (viewModel.region.value.isNullOrBlank())
            return dialogProvider.showErrorAndWait("Please select a region")

        if (viewModel.collectionType.value.isNullOrBlank())
            return dialogProvider.showErrorAndWait("Please select a collection type")

        viewModel.collectionReference.value = null
        dialogProvider.showProgressBar("Loading reference")
        val (response, error) = safeRunIO {
            creditClubMiddleWareAPI.collectionsService.getCollectionReferenceByReference(
                localStorage.institutionCode,
                viewModel.referenceString.value?.trim(),
                viewModel.region.value,
                viewModel.collectionService.value,
                derivedCollectionType
            )
        }
        dialogProvider.hideProgressBar()

        if (error != null) return dialogProvider.showErrorAndWait(error)
        response?.reference
            ?: return dialogProvider.showErrorAndWait("Please enter a valid reference")
        if (response.isSuccessful != true) {
            if (response.responseMessage?.contains("invoice not found", true) == true) {
                viewModel.referenceString.value = ""
            }
            return dialogProvider.showError(response.responseMessage)
        }
        viewModel.collectionReference.value = response
        viewModel.amountString.value = response.amount?.toString()
    }

    private suspend inline fun loadDependencies(
        dependencyName: String,
        currentValue: List<String>?,
        autoCompleteTextView: AutoCompleteTextView,
        crossinline fetcher: suspend () -> List<String>?
    ) {
        val items = if (currentValue == null) {
            dialogProvider.showProgressBar("Loading $dependencyName")
            val (items) = safeRunIO { fetcher() }
            dialogProvider.hideProgressBar()
            items
        } else {
            currentValue
        }

        if (items == null) {
            dialogProvider.showErrorAndWait("An error occurred while loading $dependencyName")
//            findNavController().popBackStack()
            return
        }

        val adapter = ArrayAdapter(requireContext(), R.layout.list_item, items)
        autoCompleteTextView.setAdapter(adapter)
    }

    private suspend fun onGenerateButtonClick(offline: Boolean) {
        viewModel.run {
            clearData(
                customerId,
                customer,
                customerPhoneNumber,
                item,
                itemCode,
                itemName,
                category,
                categoryName,
                referenceString,
                collectionReference
            )
        }

        if (viewModel.region.value.isNullOrBlank()) {
            return dialogProvider.showErrorAndWait("Please select a region")
        }

        if (viewModel.collectionType.value.isNullOrBlank()) {
            return dialogProvider.showErrorAndWait("Please enter a collection type")
        }

        findNavController().navigate(
            CollectionPaymentFragmentDirections.actionCollectionPaymentToReferenceGeneration(offline)
        )
    }

    private fun clearData(vararg liveData: MutableLiveData<*>) {
        for (liveDatum in liveData) {
            liveDatum.value = null
        }
    }

    private suspend fun completePayment() {
        if (viewModel.referenceString.value.isNullOrBlank()) {
            return dialogProvider.showErrorAndWait("Please enter a reference")
        } else if (viewModel.collectionReference.value == null) {
            loadReference()
            viewModel.collectionReference.value ?: return
        }

        val amountDouble = viewModel.amountString.value?.toDouble()
        amountDouble ?: return dialogProvider.showErrorAndWait("Please enter a reference")
        if (amountDouble == 0.0) return dialogProvider.showErrorAndWait("Amount cannot be zero")

        if (viewModel.collectionTypeIsCbs.value == true || viewModel.collectionTypeIsWebGuid.value == true) {
            if (amountDouble > viewModel.collectionReference.value?.amount ?: 0.0) {
                return dialogProvider.showErrorAndWait("You cannot pay above the amount of bill generated")
            }
        }

        val pin = dialogProvider.getPin("Agent PIN") ?: return
        if (pin.length != 4) return dialogProvider.showError("Agent PIN must be 4 digits long")

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
            collectionType = derivedCollectionType
            itemCode = viewModel.itemCode.value
            amount = amountDouble
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
        if (error != null) return dialogProvider.showErrorAndWait(error)
        if (response == null) {
            dialogProvider.showErrorAndWait("An error occurred. Please try again later")
            activity?.onBackPressed()
            return
        }

        response.date = Instant.now()
        response.collectionPaymentItemName =
            response.collectionPaymentItemName
                ?: "${viewModel.itemName.value} (${viewModel.itemCode.value})"
        response.collectionCategoryName =
            response.collectionCategoryName
                ?: "${viewModel.categoryName.value} (${viewModel.categoryCode.value})"

        if (response.isSuccessful == true) {
            dialogProvider.showSuccessAndWait(response.responseMessage ?: "Success")
            if (Platform.hasPrinter) {
                posPrinter.print(CollectionPaymentReceipt(requireContext(), response))
            }
            activity?.onBackPressed()
        } else {
            dialogProvider.showErrorAndWait(response.responseMessage ?: "Error")
        }
    }

    private inline val derivedCollectionType
        get() = viewModel.run {
            if (collectionTypeIsCbs.value == true && isOffline.value == true) "WEBGUID"
            else collectionType.value
        }
}
