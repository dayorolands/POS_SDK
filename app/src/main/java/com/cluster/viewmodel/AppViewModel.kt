package com.cluster.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.cluster.core.data.api.SubscriptionService
import com.cluster.core.data.model.*
import com.cluster.core.data.prefs.LocalStorage
import com.cluster.core.util.safeRunIO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import java.time.Duration
import java.time.Instant

class AppViewModel : ViewModel() {
    val notificationList = MutableStateFlow<List<Notification>>(emptyList())
    val notificationCount = notificationList.map { it.size }
    val caseDetails = mutableStateOf<List<CaseDetail>>(emptyList())
    val fcmToken = mutableStateOf("")
    val sessionTimedOut = MutableLiveData(false)

    // Subscriptions
    val activeSubscription = MutableStateFlow<Subscription?>(null)
    val subscriptionMilestones = MutableStateFlow<List<SubscriptionMilestone>>(emptyList())
    val subscriptionHistory = MutableStateFlow<List<Subscription>>(emptyList())
    val subscriptionPlans = MutableStateFlow<List<SubscriptionPlan>>(emptyList())
    val planDaysToExpire: Flow<Long?> = activeSubscription.map {
        if (it == null) {
            return@map null
        }
        Duration.between(Instant.now(), it.expiryDate).toDays()
    }

    private suspend fun loadActiveSubscription(
        subscriptionService: SubscriptionService,
        localStorage: LocalStorage,
    ) {
        val (subscription) = safeRunIO {
            subscriptionService.getActiveSubscription(
                institutionCode = localStorage.institutionCode,
                agentPhoneNumber = localStorage.agentPhone,
            )
        }
        if (subscription?.data != null) {
            activeSubscription.value = subscription.data!!
        }
    }

    private suspend fun loadMilestones(
        subscriptionService: SubscriptionService,
        subscriptionId: Long,
    ) {
        val result = safeRunIO {
            subscriptionService.getMilestonesBySubscriptionId(
                subscriptionId = subscriptionId,
            )
        }
        if (result.data?.data != null) {
            subscriptionMilestones.value = result.data!!.data!!
        }
    }

    suspend fun loadSubscriptionData(
        subscriptionService: SubscriptionService,
        localStorage: LocalStorage,
    ) {
        loadActiveSubscription(
            subscriptionService = subscriptionService,
            localStorage = localStorage,
        )
        val subscriptionId = activeSubscription.value?.id
        if (subscriptionId != null) {
            loadMilestones(
                subscriptionService = subscriptionService,
                subscriptionId = subscriptionId.toLong(),
            )
        }
    }


    // Agent loan
    val agentLoan = MutableStateFlow<AgentLoanEligibility?>(null)
    val agentLoanHistory = MutableStateFlow<List<AgentLoanRecord>>(emptyList())
}