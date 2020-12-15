package com.appzonegroup.app.fasttrack

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.LinearLayoutManager
import com.appzonegroup.app.fasttrack.databinding.*
import com.creditclub.core.data.api.RequestFailureException
import com.creditclub.core.data.model.Feedback
import com.creditclub.core.data.request.CaseMessageThreadRequest
import com.creditclub.core.data.response.CaseResponse
import com.creditclub.core.model.CreditClubImage
import com.creditclub.core.ui.CreditClubActivity
import com.creditclub.core.ui.SimpleAdapter
import com.creditclub.core.util.*
import com.creditclub.ui.dataBinding
import com.esafirm.imagepicker.features.ImagePicker
import com.esafirm.imagepicker.features.ReturnMode
import com.esafirm.imagepicker.model.Image
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import com.squareup.picasso.Picasso
import kotlinx.coroutines.launch
import java.time.Instant

class SupportThreadActivity : CreditClubActivity(R.layout.activity_support_thread) {

    private var fcmToken: String? = null
    private val binding by dataBinding<ActivitySupportThreadBinding>()
    private var messageList: List<Feedback> = emptyList()
    private val adapter = Adapter(messageList)
    private val reference by lazy { intent.getStringExtra("REFERENCE") ?: "" }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = intent.getStringExtra("TITLE") ?: "Support"

        binding.run {
            container.layoutManager = LinearLayoutManager(this@SupportThreadActivity)
            container.adapter = adapter
            buttonChatboxSend.setOnClickListener {
                val message = edittextChatbox.text.toString()

                if (message.isEmpty()) return@setOnClickListener Toast.makeText(
                    this@SupportThreadActivity,
                    "You cannot send an empty message",
                    Toast.LENGTH_LONG
                ).show()

                mainScope.launch { sendMessage(message) }
            }
            closeBtn.setOnClickListener {
                mainScope.launch { closeCase() }
            }
            cameraBtn.setOnClickListener {
                ImagePicker.create(this@SupportThreadActivity)
                    .returnMode(ReturnMode.ALL)
                    .folderMode(true)
                    .single().showCamera(true).start()
//                ImagePicker.cameraOnly().start(this@SupportThreadActivity)
            }
        }

        loadFcmToken()

        mainScope.launch {
            val thread = loadData() ?: return@launch finish()
            binding.noticeLayout.visibility =
                if (thread.isResolved == true) View.VISIBLE else View.GONE

            thread.response?.run {
                if (isNotEmpty()) {
                    messageList = this
                    adapter.setData(this)
                    binding.container.smoothScrollToPosition(size - 1)
                }
            }
        }
    }

    private fun loadFcmToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.e("LogCase", "Fetching FCM registration token failed", task.exception)
                loadFcmToken()
                return@OnCompleteListener
            }

            // Get new FCM registration token
            val token = task.result
            fcmToken = token
        })
    }

    private suspend fun loadData(): CaseResponse<List<Feedback>>? {
        showProgressBar("Loading Cases")

        val request = CaseMessageThreadRequest().apply {
            caseReference = reference
        }

        val (response, error) = safeRunIO {
            creditClubMiddleWareAPI.caseLogService.getCaseMessageThread(request)
        }

        hideProgressBar()

        if (error != null) {
            dialogProvider.showErrorAndWait(error)
            return null
        }
        if (response == null) {
            dialogProvider.showErrorAndWait(RequestFailureException("No response from server"))
            return null
        }

        return response
    }

    private suspend fun sendMessage(newMessage: String?, image: CreditClubImage? = null) {
        showProgressBar("Sending feedback")

        val request = Feedback().apply {
            caseReference = reference
            message = newMessage
            isAgent = true
            name = localStorage.agent?.agentName
        }
        request.fcmToken = fcmToken

        if (image != null) {
            request.isAttachment = true
            request.blobs = listOf(image)
        }

        val (response, error) = safeRunIO {
            creditClubMiddleWareAPI.caseLogService.saveFeedback(request)
        }

        hideProgressBar()

        if (error != null) return showError(error)
        response ?: return showNetworkError()

        request.dateLogged = Instant.now().toString()

        binding.edittextChatbox.setText("")
        val newMessageList = ArrayList<Feedback>()
        newMessageList.addAll(messageList)
        newMessageList.add(request)
        adapter.setData(newMessageList)
        binding.container.smoothScrollToPosition(newMessageList.size - 1)

        dialogProvider.showSuccess<Nothing>("Feedback sent successfully") {
            onClose {
                finish()
            }
        }
    }

    private suspend fun closeCase() {
        showProgressBar("Closing case")
        val (response, error) = safeRunIO {
            creditClubMiddleWareAPI.caseLogService.closeCase(reference)
        }
        hideProgressBar()

        if (error != null) return showError(error)
        response ?: return showNetworkError()

        dialogProvider.showSuccess<Nothing>("Case closed") {
            onClose {
                finish()
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (ImagePicker.shouldHandle(requestCode, resultCode, data)) {
            val image: Image? = ImagePicker.getFirstImageOrNull(data)
            image ?: return showInternalError()
            val creditClubImage = CreditClubImage(this, image)
            mainScope.launch { sendMessage(null, creditClubImage) }
        } else super.onActivityResult(requestCode, resultCode, data)
    }

    private fun loadImage(feedback: Feedback, imageBinding: ItemAddImageBinding) {
        if (feedback.message != null) {
            Picasso.get().load(feedback.message).into(imageBinding.imageView)
        } else {
            feedback.blobs?.run {
                if (isNotEmpty()) {
                    imageBinding.imageView.setImageBitmap(first().bitmap ?: return@run)
                }
            }
        }
    }

    private inner class Adapter(override var values: List<Feedback>) :
        SimpleAdapter<Adapter.ViewHolder, Feedback>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

            val binding: ViewDataBinding = when (viewType) {
                0 -> {
                    DataBindingUtil.inflate(
                        LayoutInflater.from(parent.context),
                        R.layout.item_case_message,
                        parent,
                        false
                    )
                }
                1 -> {
                    DataBindingUtil.inflate(
                        LayoutInflater.from(parent.context),
                        R.layout.item_case_image,
                        parent,
                        false
                    )
                }
                2 -> {
                    DataBindingUtil.inflate(
                        LayoutInflater.from(parent.context),
                        R.layout.item_case_message_received,
                        parent,
                        false
                    )
                }
                else -> {
                    DataBindingUtil.inflate(
                        LayoutInflater.from(parent.context),
                        R.layout.item_case_image_received,
                        parent,
                        false
                    )
                }
            }

            return ViewHolder(binding)
        }

        override fun getItemViewType(position: Int): Int {
            val feedback = values[position]
            return when {
                feedback.isAgent -> if (feedback.isAttachment) 1 else 0
                else -> return if (feedback.isAttachment) 3 else 2
            }
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val feedback = values[position]
            val instant = feedback.dateLogged?.toInstant(CREDIT_CLUB_DATE_PATTERN)
            val duration = instant?.timeAgo()

            holder.run {
                when (binding) {
                    is ItemCaseMessageBinding -> {
                        binding.messageTv.text = feedback.message
                        binding.timeTv.text = duration
                    }

                    is ItemCaseImageBinding -> {
                        loadImage(feedback, binding.image)
                        binding.timeTv.text = duration
                    }

                    is ItemCaseMessageReceivedBinding -> {
                        binding.nameTv.text = feedback.name
                        binding.messageTv.text = feedback.message
                        binding.timeTv.text = duration
                    }

                    is ItemCaseImageReceivedBinding -> {
                        binding.nameTv.text = feedback.name
                        loadImage(feedback, binding.image)
                        binding.timeTv.text = duration
                    }
                }
            }
        }

        private inner class ViewHolder(val binding: ViewDataBinding) :
            SimpleAdapter.ViewHolder(binding.root)
    }
}
