package com.cluster.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.cluster.core.data.api.SubscriptionService
import com.cluster.core.data.model.CaseDetail
import com.cluster.core.data.model.Notification
import com.cluster.core.data.model.Subscription
import com.cluster.core.data.model.SubscriptionPlan
import com.cluster.core.data.prefs.LocalStorage
import com.cluster.core.util.safeRunIO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import java.time.Duration
import java.time.Instant

class AppViewModel : ViewModel() {
    val notificationList = MutableStateFlow<List<Notification>>(emptyList())
    val caseDetails = mutableStateOf<List<CaseDetail>>(emptyList())
    val fcmToken = mutableStateOf("")
    val sessionTimedOut = MutableLiveData(false)
    val activeSubscription = MutableStateFlow<Subscription?>(null)
    val subscriptionHistory = MutableStateFlow<List<Subscription>>(emptyList())
    val subscriptionPlans = MutableStateFlow<List<SubscriptionPlan>>(emptyList())
    val planDaysToExpire: Flow<Long?> = activeSubscription.map {
        if (it == null) {
            return@map null
        }
        Duration.between(it.expiryDate, Instant.now()).toDays()
    }

    suspend fun loadActiveSubscription(
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
}