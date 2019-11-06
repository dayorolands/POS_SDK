package com.appzonegroup.app.fasttrack

import android.content.Intent
import android.os.Bundle
import com.appzonegroup.app.fasttrack.databinding.ActivityHlaTaggingBinding
import com.appzonegroup.app.fasttrack.databinding.ItemAddImageBinding
import com.creditclub.core.contract.FormDataHolder
import com.creditclub.core.data.request.OfflineHLATaggingRequest
import com.creditclub.core.model.CreditClubImage
import com.creditclub.core.ui.CreditClubActivity
import com.creditclub.core.ui.widget.DialogOptionItem
import com.creditclub.core.util.delegates.contentView
import com.creditclub.core.util.includesSpecialCharacters
import com.creditclub.core.util.localStorage
import com.creditclub.core.util.safeRunIO
import com.esafirm.imagepicker.features.ImagePicker
import com.esafirm.imagepicker.model.Image
import kotlinx.coroutines.launch
import org.threeten.bp.Instant
import java.io.IOException

typealias ImageListenerBlock = (CreditClubImage) -> Unit

class HlaTaggingActivity : BaseActivity(), FormDataHolder<OfflineHLATaggingRequest> {

    private val binding by contentView<HlaTaggingActivity, ActivityHlaTaggingBinding>(R.layout.activity_hla_tagging)
    override val formData: OfflineHLATaggingRequest = OfflineHLATaggingRequest()
    private val images = Array<String?>(4) { null }
    private var imageListener: ImageListenerBlock? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.stateSelector.run {

            val stateArray = resources.getStringArray(R.array.States)
            val stateOptions = stateArray.map {
                val stateInfo = it.split(",")
                DialogOptionItem(stateInfo[1], extraInfo = stateInfo[0])
            }

            root.setOnClickListener {
                dialogProvider.showOptions(getString(R.string.state_hint), stateOptions) {
                    onSubmit { selectedState ->
                        if (this@run.selected != stateOptions[selectedState]) {
                            binding.lgaSelector.selected = null
                        }

                        this@run.selected = stateOptions[selectedState]
                    }
                }
            }
        }

        binding.lgaSelector.run {

            root.setOnClickListener {
                val state = binding.stateSelector.selected
                if (state == null) {
                    dialogProvider.showError("Select a state first")
                    return@setOnClickListener
                }

                val lgaArray = resources.getStringArray(R.array.lgas)
                val lgaOptions = lgaArray.filter { it.substring(0, 2) == state.extraInfo }.map {
                    val stateInfo = it.split(",")
                    DialogOptionItem(stateInfo[2], extraInfo = stateInfo[1])
                }

                dialogProvider.showOptions(getString(R.string.lga_hint), lgaOptions) {
                    onSubmit { selectedState ->
                        if (this@run.selected != lgaOptions[selectedState]) {
                            binding.lgaSelector.selected = null
                        }

                        this@run.selected = lgaOptions[selectedState]
                    }
                }
            }
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
                    images[i] = bitmapString
                    imageBinding.processing = false
                }
            })
        }

        binding.btnUpload.setOnClickListener { onSubmit() }
    }

    private fun onSubmit() {
        formData.name = binding.nameLayout.input.value
        formData.description = binding.descriptionLayout.input.value
        formData.state = binding.stateSelector.selected?.extraInfo
        formData.lga = binding.lgaSelector.selected?.extraInfo
        formData.pictures = images

        if (!validate("Location name", formData.name)) return
        if (!validate("Location description", formData.description)) return
        if (!validate("State", formData.state)) return
        if (!validate("LGA", formData.lga)) return

        if (formData.pictures!!.any { it == null }) {
            dialogProvider.showError("Four (4) images must be selected")
            return
        }

        formData.dateTagged = Instant.now().toString()
        formData.location = gps.geolocationString
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
        onSubmit: ImageListenerBlock
    ) {
        imageBinding.run {
            root.setOnClickListener {
                imageListener = onSubmit

                ImagePicker.cameraOnly().start(this@HlaTaggingActivity)
            }
        }
    }

    private fun uploadData() {
        mainScope.launch {
            dialogProvider.showProgressBar("Uploading")
            val (response, error) = safeRunIO {
                creditClubMiddleWareAPI.offlineHlaTaggingService.postOfflineTaggingData(formData)
            }
            dialogProvider.hideProgressBar()

            if (error != null) return@launch showError(error)
            response ?: return@launch showError(IOException())

            if (response.isSuccessful) {
                dialogProvider.showSuccess<Nothing>("Location tagged successfully") {
                    onClose {
                        finish()
                    }
                }
            } else dialogProvider.showError(response.responseMessage)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (ImagePicker.shouldHandle(requestCode, resultCode, data)) {
            try {
                val image: Image? = ImagePicker.getFirstImageOrNull(data)
                image ?: return showInternalError()
                imageListener?.invoke(CreditClubImage(image))
                imageListener = null
            } catch (ex: Exception) {
                ex.printStackTrace()
                showInternalError()
            }
        } else super.onActivityResult(requestCode, resultCode, data)
    }
}
