package com.appzonegroup.app.fasttrack.fragment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.activityViewModels
import com.appzonegroup.app.fasttrack.R
import com.appzonegroup.app.fasttrack.databinding.FragmentDocumentUploadBinding
import com.appzonegroup.app.fasttrack.ui.dataBinding
import com.creditclub.core.model.CreditClubImage
import com.creditclub.core.ui.CreditClubFragment
import com.creditclub.core.util.debugOnly
import com.creditclub.core.util.safeRunIO
import com.creditclub.core.util.showInternalError
import com.esafirm.imagepicker.features.ImagePicker
import com.esafirm.imagepicker.features.ReturnMode
import com.esafirm.imagepicker.model.Image
import kotlinx.coroutines.launch

class DocumentUploadFragment : CreditClubFragment(R.layout.fragment_document_upload) {
    private val binding by dataBinding<FragmentDocumentUploadBinding>()
    private val viewModel by activityViewModels<OpenAccountViewModel>()
    private var imageType: ImageType = ImageType.Passport

    private enum class ImageType {
        //        IDCard,
        Passport,
        Signature
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.passportGalleryBtn.setOnClickListener {
            imageType = ImageType.Passport
            ImagePicker.create(this)
                .returnMode(ReturnMode.ALL)
                .folderMode(true)
                .single().single().showCamera(false).start()
        }

        binding.passportTakePhotoBtn.setOnClickListener {
            imageType = ImageType.Passport
            ImagePicker.cameraOnly().start(this)
        }

        binding.signatureGalleryBtn.setOnClickListener {
            imageType = ImageType.Signature
            ImagePicker.create(this)
                .returnMode(ReturnMode.ALL)
                .folderMode(true)
                .single().single().showCamera(false).start()
        }

        binding.signatureTakePhotoBtn.setOnClickListener {
            imageType = ImageType.Signature
            ImagePicker.cameraOnly().start(this)
        }

        binding.nextBtn.setOnClickListener { next() }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (ImagePicker.shouldHandle(requestCode, resultCode, data)) {
            try {
                val tmpImage: Image? = ImagePicker.getFirstImageOrNull(data)
                tmpImage ?: return dialogProvider.showInternalError()
                val image = CreditClubImage(requireContext(), tmpImage)

                mainScope.launch {
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
                }
            } catch (ex: Exception) {
                debugOnly { Log.e("DocumentUpload", ex.message, ex) }
                dialogProvider.showInternalError()
            }
        } else super.onActivityResult(requestCode, resultCode, data)
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