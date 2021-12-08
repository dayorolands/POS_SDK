package com.cluster

import android.os.Bundle
import com.cluster.databinding.ActivityHlaTaggingBinding
import com.cluster.databinding.ItemAddImageBinding
import com.cluster.utility.FunctionIds
import com.cluster.core.contract.FormDataHolder
import com.cluster.core.data.api.OfflineHlaTaggingService
import com.cluster.core.data.api.retrofitService
import com.cluster.core.data.request.GeoTagCoordinate
import com.cluster.core.data.model.State
import com.cluster.core.data.model.StatesAndLgas
import com.cluster.core.data.request.OfflineHLATaggingRequest
import com.cluster.core.model.CreditClubImage
import com.cluster.core.ui.CreditClubActivity
import com.cluster.core.ui.widget.DialogListenerBlock
import com.cluster.core.ui.widget.DialogOptionItem
import com.cluster.core.util.includesSpecialCharacters
import com.cluster.core.util.safeRunIO
import com.cluster.ui.dataBinding
import com.esafirm.imagepicker.features.cameraonly.CameraOnlyConfig
import com.esafirm.imagepicker.features.registerImagePicker
import kotlinx.coroutines.launch
import java.io.IOException
import java.time.Instant

typealias ImageListenerBlock = (CreditClubImage) -> Unit

class HlaTaggingActivity : CreditClubActivity(R.layout.activity_hla_tagging),
    FormDataHolder<OfflineHLATaggingRequest> {

    private val binding: ActivityHlaTaggingBinding by dataBinding()
    override val functionId = FunctionIds.HLA_TAGGING
    private var imageListener: ImageListenerBlock? = null
    private val offlineHlaTaggingService: OfflineHlaTaggingService by retrofitService()

    override val formData: OfflineHLATaggingRequest = OfflineHLATaggingRequest().apply {
        pictures = mutableListOf(null, null, null, null)
    }

    private val launcher = registerImagePicker {
        try {
            val image = it.firstOrNull() ?: return@registerImagePicker showInternalError()
            imageListener?.invoke(CreditClubImage(this, image))
            imageListener = null
        } catch (ex: Exception) {
            ex.printStackTrace()
            showInternalError()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        mainScope.launch {
            dialogProvider.showProgressBar("Loading")
            val (response, error) = safeRunIO {
                offlineHlaTaggingService.getStatesAndLGA()
            }
            dialogProvider.hideProgressBar()

            val finishOnFail: DialogListenerBlock<*> = {
                onClose {
                    finish()
                }
            }

            if (error != null) return@launch dialogProvider.showError(error, finishOnFail)

            val infoError = "Couldn't get State/LGA information"

            response ?: return@launch dialogProvider.showError(infoError, finishOnFail)

            if (!response.status) return@launch dialogProvider.showError(
                response.message,
                finishOnFail
            )

            response.data ?: return@launch dialogProvider.showError(
                "Couldn't get state information",
                finishOnFail
            )

            populateStateInformation(response)
        }

        listOf(
            binding.image1, binding.image2,
            binding.image3, binding.image4
        ).forEachIndexed { i, imageBinding ->

            createImageListener(imageBinding, onSubmit = { image ->
                mainScope.launch {
                    imageBinding.processing = true
                    val (bitmap) = safeRunIO { image.bitmap }
                    imageBinding.imageView.setImageBitmap(bitmap)

                    val (bitmapString) = safeRunIO { image.bitmapString }
                    formData.pictures?.set(i, bitmapString)
                    imageBinding.processing = false
                }
            })
        }

        binding.btnUpload.setOnClickListener { onSubmit() }
    }

    private fun populateStateInformation(info: StatesAndLgas) {

        binding.stateSelector.run {

            val stateArray = info.data!!
            val stateOptions = stateArray.map { DialogOptionItem(it.name, extraInfo = it.id) }

            root.setOnClickListener {
                dialogProvider.showOptions(getString(R.string.state_hint), stateOptions) {
                    onSubmit { position ->
                        if (this@run.selected != stateOptions[position]) {
                            binding.lgaSelector.selected = null
                        }

                        selected = stateOptions[position]

                        populateLgaInformation(stateArray[position])
                    }
                }
            }
        }

        binding.lgaSelector.run {

            root.setOnClickListener {
                dialogProvider.showError("Select a state first")
                return@setOnClickListener
            }
        }
    }

    private fun populateLgaInformation(state: State) {
        binding.lgaSelector.run {

            root.setOnClickListener {

                val lgaArray = state.lgas ?: emptyList()
                val lgaOptions = lgaArray.map { DialogOptionItem(it.name, extraInfo = it.id) }

                dialogProvider.showOptions(getString(R.string.lga_hint), lgaOptions) {
                    onSubmit { position ->
                        selected = lgaOptions[position]
                    }
                }
            }
        }
    }

    private fun onSubmit() {
        formData.name = binding.nameLayout.input.value
        formData.description = binding.descriptionLayout.input.value
        formData.state = binding.stateSelector.selected?.extraInfo
        formData.lga = binding.lgaSelector.selected?.extraInfo

        if (!validate("Location name", formData.name)) return
        if (!validate("Location description", formData.description)) return
        if (!validate("State", formData.state)) return
        if (!validate("LGA", formData.lga)) return

        if (formData.pictures!!.any { it == null }) {
            dialogProvider.showError("Four (4) images must be selected")
            return
        }

        formData.dateTagged = Instant.now().toString()
        val (latitude, longitude) = localStorage.lastKnownLocation.split(";")
        formData.location = GeoTagCoordinate(latitude = latitude, longitude = longitude)
        formData.agentPhoneNumber = localStorage.agent?.phoneNumber
        formData.institutionCode = localStorage.institutionCode

        uploadData()
    }

    private fun validate(name: String, value: String?, required: Boolean = true): Boolean {
        if (required && value.isNullOrEmpty()) {
            dialogProvider.showError(resources.getString(R.string.field_is_required, name))
            return false
        }

        if (value?.includesSpecialCharacters() == true) {
            dialogProvider.showError(
                resources.getString(
                    R.string.special_characters_not_permitted,
                    name
                )
            )
            return false
        }

        return true
    }

    private fun createImageListener(
        imageBinding: ItemAddImageBinding,
        onSubmit: ImageListenerBlock,
    ) {
        imageBinding.run {
            root.setOnClickListener {
                imageListener = onSubmit

                launcher.launch(CameraOnlyConfig())
            }
        }
    }

    private fun uploadData() {
        mainScope.launch {
            dialogProvider.showProgressBar("Uploading")
            val (response, error) = safeRunIO {
                offlineHlaTaggingService.postOfflineTaggingData(formData)
            }
            dialogProvider.hideProgressBar()

            if (error != null) return@launch showError(error)
            response ?: return@launch showError(IOException())

            if (response.status) {
                dialogProvider.showSuccess("Location tagged successfully") {
                    onClose {
                        finish()
                    }
                }
            } else dialogProvider.showError(response.message)
        }
    }
}
