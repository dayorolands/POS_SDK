package com.cluster.fragment

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.MutableState
import androidx.compose.ui.platform.ComposeView
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelStoreOwner
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.fragment.findNavController
import com.cluster.R
import com.cluster.Routes
import com.cluster.activity.UpdateActivity
import com.cluster.clusterNavigation
import com.cluster.conversation.LocalBackPressedDispatcher
import com.cluster.core.data.api.AppConfig
import com.cluster.core.data.api.NotificationService
import com.cluster.core.data.api.SubscriptionService
import com.cluster.core.data.api.retrofitService
import com.cluster.core.data.model.NotificationRequest
import com.cluster.core.data.prefs.AppDataStorage
import com.cluster.core.data.prefs.LocalStorage
import com.cluster.core.ui.CreditClubFragment
import com.cluster.core.ui.widget.DialogConfirmParams
import com.cluster.core.ui.widget.DialogProvider
import com.cluster.core.util.packageInfo
import com.cluster.core.util.safeRunIO
import com.cluster.pos.Platform
import com.cluster.screen.home.HomeScreen
import com.cluster.ui.theme.CreditClubTheme
import com.cluster.viewmodel.AppViewModel
import com.cluster.viewmodel.ProvideViewModelStoreOwner
import com.google.accompanist.insets.ExperimentalAnimatedInsets
import com.google.accompanist.insets.ProvideWindowInsets
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import java.io.File

class HomeFragment : CreditClubFragment() {
    private val notificationViewModel: NotificationViewModel by activityViewModels()
    private val appViewModel: AppViewModel by activityViewModels()
    private val notificationService: NotificationService by retrofitService()
    private val subscriptionService: SubscriptionService by retrofitService()
    private val appDataStorage: AppDataStorage by inject()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mainScope.launch {
            coroutineScope {
                getNotifications(
                    notificationService = notificationService,
                    notificationViewModel = notificationViewModel,
                    appViewModel = appViewModel,
                    localStorage = localStorage,
                )
            }
            coroutineScope {
                appViewModel.loadActiveSubscription(
                    subscriptionService = subscriptionService,
                    localStorage = localStorage,
                )
            }
        }

        val hasPosUpdateManager = Platform.isPOS && Platform.deviceType != 2

        if (hasPosUpdateManager) {
            checkForUpdate(
                activity = requireActivity(),
                context = requireContext(),
                dialogProvider = dialogProvider,
                appDataStorage = appDataStorage,
                appConfig = appConfig,
            )
        }

        loadFcmToken(appViewModel.fcmToken)
    }

    @OptIn(ExperimentalAnimatedInsets::class)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val fragmentNavController = findNavController()
        return ComposeView(inflater.context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
            )
            val viewModelStoreOwner: ViewModelStoreOwner = requireActivity()

            setContent {
                val composeNavController = rememberNavController()
                CreditClubTheme {
                    ProvideWindowInsets {
                        CompositionLocalProvider(
                            LocalBackPressedDispatcher provides requireActivity().onBackPressedDispatcher,
                        ) {
                            NavHost(
                                navController = composeNavController,
                                startDestination = Routes.Home,
                            ) {
                                composable(Routes.Home) {
                                    ProvideViewModelStoreOwner(
                                        viewModelStoreOwner = viewModelStoreOwner,
                                    ) {
                                        HomeScreen(
                                            mainNavController = fragmentNavController,
                                            composeNavController = composeNavController,
                                            fragment = this@HomeFragment,
                                        )
                                    }
                                }
                                clusterNavigation(
                                    navController = composeNavController,
                                    dialogProvider = dialogProvider,
                                    appViewModel = appViewModel,
                                    viewModelStoreOwner = viewModelStoreOwner,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private suspend fun getNotifications(
    notificationService: NotificationService,
    notificationViewModel: NotificationViewModel,
    appViewModel: AppViewModel,
    localStorage: LocalStorage
) {
    val (response) = safeRunIO {
        notificationService.getNotifications(
            NotificationRequest(
                localStorage.agentPhone,
                localStorage.institutionCode,
                20,
                0
            )
        )
    }

    if (response?.response != null) {
        notificationViewModel.notificationList.value = response.response!!
        appViewModel.notificationList.value = response.response!!
    }
}

private fun Fragment.checkForUpdate(
    activity: Activity,
    dialogProvider: DialogProvider,
    appDataStorage: AppDataStorage,
    context: Context,
    appConfig: AppConfig,
) {
    val latestVersion = appDataStorage.latestVersion ?: return
    val currentVersion = context.packageInfo!!.versionName
    if (!latestVersion.isNewerThan(currentVersion)) {
        return
    }
    val canUpdate = latestVersion.updateIsRequired(currentVersion)
    val mustUpdate = canUpdate && latestVersion.daysOfGraceLeft() < 1
    val message = "A new version (v${latestVersion.version}) is available."
    val subtitle = when {
        canUpdate && mustUpdate -> "You need to update now"
        canUpdate -> "Kindly update within ${latestVersion.daysOfGraceLeft()} days"
        else -> "Kindly update"
    }

    val latestApkFile = appDataStorage.latestApkFile
    if (latestApkFile?.exists() == true) {
        dialogProvider.confirm(DialogConfirmParams(message, subtitle)) {
            onSubmit {
                if (it) {
                    openApk(context, latestApkFile, appConfig)
                    if (mustUpdate) {
                        activity.finish()
                    }
                } else if (mustUpdate) {
                    activity.finish()
                }
            }

            onClose {
                if (mustUpdate) {
                    activity.finish()
                }
            }
        }
        return
    }

    dialogProvider.confirm(DialogConfirmParams(message, subtitle)) {
        onSubmit {
            if (it) {
                val intent = Intent(context, UpdateActivity::class.java)
                startActivity(intent)
                if (mustUpdate) {
                    activity.finish()
                }
            } else if (mustUpdate) {
                activity.finish()
            }
        }

        onClose {
            if (mustUpdate) {
                activity.finish()
            }
        }
    }
}

@SuppressLint("QueryPermissionsNeeded")
private fun openApk(context: Context, apkFile: File, appConfig: AppConfig) {
    val intent = Intent(Intent.ACTION_VIEW).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        val uri = FileProvider.getUriForFile(
            context.applicationContext,
            appConfig.fileProviderAuthority,
            apkFile,
        )
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        intent.setDataAndType(uri, "application/vnd.android.package-archive")
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

        //GRANTING THE PERMISSIONS EXPLICITLY HERE! to all possible choosers (3rd party apps):
        val resolvedInfoActivities: List<ResolveInfo> = context.packageManager
            .queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)

        for (ri in resolvedInfoActivities) {
            context.grantUriPermission(
                ri.activityInfo.packageName,
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION,
            )
        }
    } else {
        val uri = Uri.fromFile(apkFile)
        intent.setDataAndType(uri, "application/vnd.android.package-archive")
    }
    context.startActivity(
        Intent.createChooser(
            intent,
            context.resources.getText(R.string.share_receipt_to)
        ).addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION),
    )
}

private fun loadFcmToken(fcmToken: MutableState<String>, retryOnFail: Boolean = true) {
    FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
        if (!task.isSuccessful) {
            Log.e("Support", "Fetching FCM registration token failed", task.exception)
            if (retryOnFail) {
                loadFcmToken(fcmToken = fcmToken, retryOnFail = false)
            }
            return@addOnCompleteListener
        }

        // Get new FCM registration token
        fcmToken.value = task.result
    }
}

