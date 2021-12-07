package com.cluster.fragment

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.activityViewModels
import com.cluster.R
import com.cluster.databinding.FragmentDocumentUploadBinding
import com.cluster.fragment.online.registerImagePicker
import com.cluster.ui.dataBinding
import com.creditclub.core.model.CreditClubImage
import com.creditclub.core.ui.CreditClubFragment
import com.creditclub.core.util.debugOnly
import com.creditclub.core.util.safeRunIO
import com.creditclub.core.util.showInternalError
import com.esafirm.imagepicker.features.ImagePickerConfig
import com.esafirm.imagepicker.features.ImagePickerMode
import com.esafirm.imagepicker.features.ReturnMode
import com.esafirm.imagepicker.features.cameraonly.CameraOnlyConfig

class DocumentUploadFragment : CreditClubFragment(R.layout.fragment_document_upload) {
    private val binding by dataBinding<FragmentDocumentUploadBinding>()
    private val viewModel by activityViewModels<OpenAccountViewModel>()
    private var imageType: ImageType = ImageType.Passport

    private enum class ImageType {
        //        IDCard,
        Passport,
        Signature
    }

    private val launcher = registerImagePicker {
        try {
            val tmpImage =
                it.firstOrNull() ?: return@registerImagePicker dialogProvider.showInternalError()
            val image = CreditClubImage(requireContext(), tmpImage)

            dialogProvider.showProgressBar("Processing image")
            val (bitmap) = safeRunIO { image.bitmap }
            val imageView = when (imageType) {
                ImageType.Passport -> binding.passportImageView
                ImageType.Signature -> binding.signatureImageView
            }
            imageView.setImageBitmap(bitmap)

            val bitmapString = safeRunIO { image.bitmapString }.data
            val bitmapLiveData = when (imageType) {
                ImageType.Passport -> viewModel.passportString
                ImageType.Signature -> viewModel.signatureString
            }
            bitmapLiveData.postValue(bitmapString)
            dialogProvider.hideProgressBar()
        } catch (ex: Exception) {
            debugOnly { Log.e("DocumentUpload", ex.message, ex) }
            dialogProvider.showInternalError()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.passportGalleryBtn.setOnClickListener {
            imageType = ImageType.Passport
            launcher(
                ImagePickerConfig(
                    mode = ImagePickerMode.SINGLE,
                    isShowCamera = false,
                    isFolderMode = true,
                    returnMode = ReturnMode.ALL,
                )
            )
        }

        binding.passportTakePhotoBtn.setOnClickListener {
            imageType = ImageType.Passport
            launcher(CameraOnlyConfig())
        }

        binding.signatureGalleryBtn.setOnClickListener {
            imageType = ImageType.Signature
            launcher(
                ImagePickerConfig(
                    mode = ImagePickerMode.SINGLE,
                    isShowCamera = false,
                    isFolderMode = true,
                    returnMode = ReturnMode.ALL,
                )
            )
        }

        binding.signatureTakePhotoBtn.setOnClickListener {
            imageType = ImageType.Signature
            launcher(CameraOnlyConfig())
        }

        binding.nextBtn.setOnClickListener { next() }
    }

    private fun next() {
        if (viewModel.passportString.value.isNullOrBlank()) {
            dialogProvider.showError(getString(R.string.please_upload_customer_passport_photo))
            return
        }

        /*if (idCardString == null)
        {
            dialogProvider.showError(getString(R.string.please_upload_customers_id_card));
            return;
        }*/

        if (viewModel.signatureString.value.isNullOrBlank()) {
            dialogProvider.showError(getString(R.string.please_upload_customers_signature))
            return
        }

        viewModel.afterDocumentUpload.value?.invoke()
    }
}