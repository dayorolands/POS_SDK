package com.appzonegroup.app.fasttrack.fragment

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.appzonegroup.app.fasttrack.R
import com.appzonegroup.app.fasttrack.databinding.NotificationFragmentBinding
import com.appzonegroup.app.fasttrack.ui.dataBinding
import com.creditclub.core.data.api.NotificationService
import com.creditclub.core.data.model.NotificationRequest
import com.creditclub.core.ui.CreditClubFragment
import com.creditclub.core.util.delegates.service
import com.creditclub.core.util.safeRunIO
import com.creditclub.core.util.showError
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

        if (response != null) viewModel.notificationList.value = response
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
}