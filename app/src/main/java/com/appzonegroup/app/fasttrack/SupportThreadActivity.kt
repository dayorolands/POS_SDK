package com.appzonegroup.app.fasttrack

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.LinearLayoutManager
import com.appzonegroup.app.fasttrack.databinding.ActivitySupportThreadBinding
import com.appzonegroup.app.fasttrack.databinding.ItemCaseMessageBinding
import com.appzonegroup.app.fasttrack.databinding.ItemCaseMessageReceivedBinding
import com.appzonegroup.app.fasttrack.model.AgentInfo
import com.creditclub.core.data.model.Feedback
import com.creditclub.core.data.request.CaseMessageThreadRequest
import com.creditclub.core.data.response.CaseResponse
import com.creditclub.core.ui.SimpleAdapter
import com.creditclub.core.ui.widget.DialogListener
import com.creditclub.core.ui.widget.DialogListenerBlock
import com.creditclub.core.util.*
import com.creditclub.core.util.delegates.contentView
import com.google.gson.Gson
import kotlinx.coroutines.launch
import java.time.Instant

class SupportThreadActivity : BaseActivity() {

    private val binding by contentView<SupportThreadActivity, ActivitySupportThreadBinding>(R.layout.activity_support_thread)
    private var messageList: List<Feedback> = emptyList()
    private val adapter = Adapter(messageList)
    private val reference by lazy { intent.getStringExtra("REFERENCE") ?: "" }
    private val agentName: String? by lazy {
        Gson().fromJson(
            localStorage.agentInfo,
            AgentInfo::class.java
        )?.agentName
    }

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

                sendMessage(message)
            }
            closeBtn.setOnClickListener {
                closeCase()
            }
        }

        loadData {
            onSubmit { thread ->
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

            onClose {
                finish()
            }
        }
    }

    private fun loadData(block: DialogListenerBlock<CaseResponse<List<Feedback>>>) {

        mainScope.launch {
            showProgressBar("Loading Cases")

            val request = CaseMessageThreadRequest().apply {
                caseReference = reference
            }

            val (response, error) = safeRunIO {
                creditClubMiddleWareAPI.caseLogService.getCaseMessageThread(request)
            }

            hideProgressBar()

            if (error != null) return@launch showError(error, block)
            response ?: return@launch showNetworkError(block)

            DialogListener.create(block).submit(Dialog(this@SupportThreadActivity), response)
        }
    }

    private fun sendMessage(newMessage: String) {
        mainScope.launch {
            showProgressBar("Sending feedback")

            val request = Feedback().apply {
                caseReference = reference
                message = newMessage
                isAgent = true
                name = agentName
            }

            val (response, error) = safeRunIO {
                creditClubMiddleWareAPI.caseLogService.saveFeedback(request)
            }

            hideProgressBar()

            if (error != null) return@launch showError(error)
            response ?: return@launch showNetworkError()

            request.dateLogged = Instant.now().toString()

            binding.edittextChatbox.setText("")
            val newMessageList = ArrayList<Feedback>()
            newMessageList.addAll(messageList)
            newMessageList.add(request)
            adapter.setData(newMessageList)
            binding.container.smoothScrollToPosition(newMessageList.size - 1)

            showSuccess<Nothing>("Feedback sent successfully") {
                onClose {
                    finish()
                }
            }
        }
    }

    private fun closeCase() {
        mainScope.launch {
            showProgressBar("Closing case")
            val (response, error) = safeRunIO {
                creditClubMiddleWareAPI.caseLogService.closeCase(reference)
            }
            hideProgressBar()

            if (error != null) return@launch showError(error)
            response ?: return@launch showNetworkError()

            showSuccess<Unit>("Case closed") {
                onClose {
                    finish()
                }
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

    private inner class Adapter(override var values: List<Feedback>) :
        SimpleAdapter<Adapter.ViewHolder, Feedback>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

            val binding: ViewDataBinding = if (viewType == 0) {
                DataBindingUtil.inflate(
                    LayoutInflater.from(parent.context),
                    R.layout.item_case_message,
                    parent,
                    false
                )
            } else {
                DataBindingUtil.inflate(
                    LayoutInflater.from(parent.context),
                    R.layout.item_case_message_received,
                    parent,
                    false
                )
            }

            return ViewHolder(binding)
        }

        override fun getItemViewType(position: Int): Int {
            return if (values[position].isAgent) 0 else return 1
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

                    is ItemCaseMessageReceivedBinding -> {
                        binding.nameTv.text = feedback.name
                        binding.messageTv.text = feedback.message
                        binding.timeTv.text = duration
                    }
                }
            }
        }

        private inner class ViewHolder(val binding: ViewDataBinding) :
            SimpleAdapter.ViewHolder(binding.root)
    }
}
