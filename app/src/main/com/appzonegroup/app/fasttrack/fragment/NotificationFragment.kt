package com.appzonegroup.app.fasttrack.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import com.appzonegroup.app.fasttrack.R
import com.appzonegroup.app.fasttrack.databinding.NotificationDetailDialogBinding
import com.appzonegroup.app.fasttrack.databinding.NotificationFragmentBinding
import com.appzonegroup.app.fasttrack.ui.dataBinding
import com.creditclub.core.data.api.NotificationService
import com.creditclub.core.data.model.Notification
import com.creditclub.core.data.model.NotificationRequest
import com.creditclub.core.ui.CreditClubFragment
import com.creditclub.core.util.createDialog
import com.creditclub.core.util.delegates.service
import com.creditclub.core.util.safeRunIO
import com.creditclub.core.util.timeAgo
import com.creditclub.ui.theme.CreditClubTheme
import com.google.accompanist.insets.ProvideWindowInsets
import kotlinx.coroutines.launch
import java.time.Instant
import java.util.*

class NotificationFragment : CreditClubFragment(R.layout.notification_fragment) {
    private val binding by dataBinding<NotificationFragmentBinding>()
    private val viewModel by activityViewModels<NotificationViewModel>()
    private val notificationService by creditClubMiddleWareAPI.retrofit.service<NotificationService>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as AppCompatActivity).setSupportActionBar(binding.toolbar)
        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.viewModel = viewModel
        binding.composeView.setContent {
            CreditClubTheme {
                ProvideWindowInsets {
                    NotificationsContent()
                }
            }
        }
        mainScope.launch { getNotifications() }
        binding.swipeRefreshLayout.setOnRefreshListener {
            mainScope.launch { getNotifications() }
        }
    }

    @Composable
    private fun NotificationsContent() {
        val notifications by viewModel.notificationList.collectAsState()
        LazyColumn(
            modifier = Modifier.background(MaterialTheme.colors.surface),
        ) {
            items(notifications, key = { it.id }) {
                NotificationItem(notification = it, onClick = { showDetails(it) })
            }
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

        if (response != null) viewModel.notificationList.value = response.response ?: emptyList()
        if (error != null) dialogProvider.showError(error)
        binding.swipeRefreshLayout.isRefreshing = false
    }

    private fun showDetails(notification: Notification) {
        val dialog = requireContext().createDialog()
        val layoutInflater = LayoutInflater.from(requireContext())
        val binding = DataBindingUtil.inflate<NotificationDetailDialogBinding>(
            layoutInflater,
            R.layout.notification_detail_dialog,
            null,
            false
        )
        binding.titleTv.text = notification.header
        binding.messageTv.text = notification.message
        dialog.setContentView(binding.root)
        dialog.show()
        binding.btnDone.setOnClickListener { dialog.dismiss() }
    }
}

@Composable
private fun NotificationItem(notification: Notification, onClick: () -> Unit) {
    val formattedDate = remember(notification.dateLogged) {
        notification.dateLogged?.timeAgo() ?: ""
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                onClick = onClick,
                interactionSource = remember { MutableInteractionSource() },
                indication = rememberRipple(color = colorResource(R.color.ef_grey)),
            )
    ) {
        Row(
            modifier = Modifier.padding(start = 16.dp, top = 10.dp, end = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                notification.header?.toUpperCase(Locale.ROOT) ?: "",
                color = colorResource(id = R.color.colorPrimary),
                maxLines = 1,
                style = MaterialTheme.typography.subtitle1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = formattedDate,
                maxLines = 1,
                style = MaterialTheme.typography.caption,
                color = colorResource(R.color.ef_grey),
            )
        }
        Text(
            text = notification.message ?: "",
            maxLines = 1,
            style = MaterialTheme.typography.body1,
            color = colorResource(R.color.ef_grey),
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .padding(start = 16.dp, bottom = 10.dp, end = 16.dp)
                .fillMaxWidth(),
        )
        Divider(startIndent = 16.dp)
    }
}

@Preview
@Composable
private fun NotificationItemPreview() {
    val dateLogged = Instant.now()
    LazyColumn {
        items(5) {
            val notification = Notification(
                id = it,
                dateLogged = dateLogged,
                header = "New note $it",
                message = "Very new note",
                agentPhoneNumber = "07239488",
                isActive = true,
                isRead = true,
                reference = "jfjfjg",
                type = 1
            )
            NotificationItem(notification = notification, onClick = {})
        }
    }
}