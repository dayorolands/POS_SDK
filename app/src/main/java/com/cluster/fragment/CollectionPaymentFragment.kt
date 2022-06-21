package com.cluster.fragment

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import com.cluster.R
import com.cluster.core.data.api.CollectionsService
import com.cluster.core.data.api.retrofitService
import com.cluster.core.data.request.CollectionPaymentRequest
import com.cluster.core.ui.CreditClubFragment
import com.cluster.core.util.safeRunIO
import com.cluster.databinding.CollectionPaymentFragmentBinding
import com.cluster.pos.Platform
import com.cluster.pos.printer.PosPrinter
import com.cluster.receipt.collectionPaymentReceipt
import com.cluster.ui.dataBinding
import com.cluster.utility.FunctionIds
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import org.koin.android.ext.android.inject
import org.koin.core.parameter.parametersOf
import java.time.Instant
import java.util.*

class CollectionPaymentFragment : CreditClubFragment(R.layout.collection_payment_fragment) {
    private val posPrinter: PosPrinter by inject { parametersOf(requireContext(), dialogProvider) }
    private val binding by dataBinding<CollectionPaymentFragmentBinding>()
    private val viewModel: CollectionPaymentViewModel by navGraphViewModels(R.id.collectionGraph)
    override val functionId = FunctionIds.COLLECTION_PAYMENT
    private val request = CollectionPaymentRequest()
    private val uniqueReference = UUID.randomUUID().toString()
    private val collectionsService: CollectionsService by retrofitService()

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        (activity as AppCompatActivity).setSupportActionBar(binding.toolbar)
        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.viewModel = viewModel
        binding.toolbar.title = "Collections"
        mainScope.launch { loadBillers() }

        viewModel.run {
            billerList.bindDropDown(billers, binding.billerInput)
            paymentItemList.bindDropDown(paymentItem, binding.paymentItemInput)
        }
        viewModel.billerName.onChange {
            mainScope.launch {
//                binding.amountInput.value = "0.00"
                viewModel.paymentItemName.value = ""
                viewModel.paymentItemAmount?.value
                loadCollectionPaymentItems()
                Log.d("OkHttpClient", "Checking the amount here ${viewModel.paymentItemAmount.value}")
            }
        }

        Log.d("OkHttpClient", "Checking the amount here ${viewModel.paymentItemAmount.value}")

        if (viewModel.billerName.value != null) {
            mainScope.launch {
                loadCollectionPaymentItems()
            }
        }
    }

    private fun AutoCompleteTextView.clearSuggestions() {
        clearListSelection()
        val adapter = ArrayAdapter(requireContext(), R.layout.list_item, emptyList<String>())
        setAdapter(adapter)
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


    private suspend fun loadBillers() = viewModel.billerList.download("billers"){
        collectionsService.getCollectionBillers(
            localStorage.institutionCode,
            viewModel.collectionService.value
        )
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

    private suspend fun loadCollectionPaymentItems() = viewModel.paymentItemList.download("biller items"){
        collectionsService.getCollectionPaymentItems(
            localStorage.institutionCode,
            viewModel.billerId.value
        )
    }

    private suspend fun loadReference() {
        if (viewModel.region.value.isNullOrBlank())
            return dialogProvider.showErrorAndWait("Please select a region")

        if (viewModel.collectionType.value.isNullOrBlank())
            return dialogProvider.showErrorAndWait("Please select a collection type")

        viewModel.collectionReference.value = null
        dialogProvider.showProgressBar("Loading reference")
        val (response, error) = safeRunIO {
            collectionsService.getCollectionReferenceByReference(
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
        crossinline fetcher: suspend () -> List<String>?,
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
            R.id.action_collection_payment_to_reference_generation,
            bundleOf("offline" to true),
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

        val serializer = CollectionPaymentRequest.Additional.serializer()
        val agent = localStorage.agent
        val additional = CollectionPaymentRequest.Additional().apply {
            agentCode = agent?.agentCode
            terminalId = agent?.terminalID
        }
        request.apply {
            collectionReference = viewModel.collectionReference.value?.reference
            agentPin = pin
            region = viewModel.region.value
            categoryCode = viewModel.categoryCode.value
            collectionType = derivedCollectionType
            itemCode = viewModel.itemCode.value
            amount = amountDouble
            geoLocation = localStorage.lastKnownLocation
            currency = "NGN"
            institutionCode = localStorage.institutionCode
            agentPhoneNumber = localStorage.agentPhone
            collectionService = viewModel.collectionService.value
            requestReference = uniqueReference
            retrievalReferenceNumber = viewModel.retrievalReferenceNumber.value
            additionalInformation = Json.encodeToString(serializer, additional)
            deviceNumber = localStorage.deviceNumber
        }
        dialogProvider.showProgressBar("Processing request")
        val (response, error) = safeRunIO {
            collectionsService.collectionPayment(request)
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
                posPrinter.print(collectionPaymentReceipt(requireContext(), response))
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
