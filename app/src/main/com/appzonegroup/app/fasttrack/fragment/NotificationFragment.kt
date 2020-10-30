package com.appzonegroup.app.fasttrack.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.appzonegroup.app.fasttrack.R
import com.appzonegroup.app.fasttrack.databinding.NotificationDetailDialogBinding
import com.appzonegroup.app.fasttrack.databinding.NotificationFragmentBinding
import com.appzonegroup.app.fasttrack.databinding.NotificationItemBinding
import com.appzonegroup.app.fasttrack.ui.dataBinding
import com.creditclub.core.data.api.NotificationService
import com.creditclub.core.data.model.Notification
import com.creditclub.core.data.model.NotificationRequest
import com.creditclub.core.ui.CreditClubFragment
import com.creditclub.core.ui.SimpleBindingAdapter
import com.creditclub.core.util.*
import com.creditclub.core.util.delegates.service
import kotlinx.coroutines.launch

class NotificationFragment : CreditClubFragment(R.layout.notification_fragment) {
    private val binding by dataBinding<NotificationFragmentBinding>()
    private val viewModel by activityViewModels<NotificationViewModel>()
    private val notificationService by creditClubMiddleWareAPI.retrofit.service<NotificationService>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.list.layoutManager = LinearLayoutManager(context)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        (activity as AppCompatActivity).setSupportActionBar(binding.toolbar)
        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.viewModel = viewModel
        viewModel.notificationList.watch {
            binding.list.adapter = NotificationAdapter(it ?: emptyList())
        }
        mainScope.launch { getNotifications() }
        binding.swipeRefreshLayout.setOnRefreshListener {
            mainScope.launch { getNotifications() }
        }
    }

    private suspend fun getNotifications() {
        binding.swipeRefreshLayout.isRefreshing = true
        val (response, error) = safeRunIO {
            notificationService.getNotifications(
                NotificationRequest(
                    localStorage.agentPhone,
                    localStorage.institutionCode,
                    20,
                    0
                )
            )
        }

        if (response != null) viewModel.notificationList.value = response.response
        if (error != null) dialogProvider.showError(error)
        binding.swipeRefreshLayout.isRefreshing = false
    }

    private inline fun <T> MutableLiveData<T>.watch(crossinline block: (T?) -> Unit) {
        block(value)
        var oldValue = value
        observe(viewLifecycleOwner, Observer {
            if (value != oldValue) {
                oldValue = value
                block(it)
            }
        })
    }

    private fun showDetails(notification: Notification) {
        val dialog = requireContext().createDialog()
        val binding = LayoutInflater.from(requireContext()).viewBinding(
            NotificationDetailDialogBinding::bind,
            R.layout.notification_detail_dialog
        )
        binding.titleTv.text = notification.header
        binding.messageTv.text = notification.message
        dialog.setContentView(binding.root)
        dialog.show()
        binding.btnDone.setOnClickListener { dialog.dismiss() }
    }

    private inner class NotificationAdapter(
        override var values: List<Notification>
    ) : SimpleBindingAdapter<Notification, NotificationItemBinding>(R.layout.notification_item) {

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val notification = values[position]
            holder.binding.name.text = notification.header
            holder.binding.description.text = notification.message
            holder.binding.timeTv.text = notification.dateLogged?.timeAgo()
            holder.binding.referenceTv.text = notification.id.toString()

            holder.binding.root.setOnClickListener { showDetails(notification) }
        }
    }
}