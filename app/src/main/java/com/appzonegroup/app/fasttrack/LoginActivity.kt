package com.appzonegroup.app.fasttrack

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils
import android.view.View
import androidx.core.content.edit
import coil.Coil
import coil.request.ImageRequest
import com.appzonegroup.app.fasttrack.databinding.ActivityLoginBinding
import com.appzonegroup.app.fasttrack.ui.SurveyDialog
import com.appzonegroup.creditclub.pos.Platform
import com.appzonegroup.creditclub.pos.data.PosDatabase
import com.appzonegroup.creditclub.pos.data.PosPreferences
import com.appzonegroup.creditclub.pos.extension.posConfig
import com.appzonegroup.creditclub.pos.extension.posParameter
import com.creditclub.core.data.TRANSACTIONS_CLIENT
import com.creditclub.core.data.api.AppConfig
import com.creditclub.core.data.api.StaticService
import com.creditclub.core.data.api.VersionService
import com.creditclub.core.data.api.retrofitService
import com.creditclub.core.data.model.SurveyQuestion
import com.creditclub.core.data.request.SubmitSurveyRequest
import com.creditclub.core.data.response.isSuccessful
import com.creditclub.core.ui.CreditClubActivity
import com.creditclub.core.ui.getLatestVersion
import com.creditclub.core.util.*
import com.creditclub.core.util.delegates.jsonStore
import com.creditclub.pos.InvalidRemoteConnectionInfo
import com.creditclub.pos.PosConfig
import com.creditclub.pos.api.PosApiService
import com.creditclub.pos.model.PosTenant
import com.creditclub.ui.dataBinding
import kotlinx.coroutines.*
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import org.koin.android.ext.android.get
import org.koin.android.ext.android.inject
import java.time.Instant

class LoginActivity : CreditClubActivity(R.layout.activity_login) {
    private val posTenant: PosTenant by inject()
    private val posPreferences: PosPreferences by inject()
    private val jsonPrefs by lazy { getSharedPreferences("JSON_STORAGE", 0) }
    private val posDatabase: PosDatabase by inject()
    private val ioScope = CoroutineScope(Dispatchers.IO)
    private val binding: ActivityLoginBinding by dataBinding()
    private val posApiService: PosApiService by retrofitService()
    private val staticService: StaticService by retrofitService()
    private val versionService: VersionService by retrofitService()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.statusBarColor = getColor(R.color.colorLoginBg)
            window.navigationBarColor = getColor(R.color.colorLoginBg)
        }
        binding.versionTv.text = "Version ${packageInfo?.versionName}"
        debugOnly {
            binding.loginPhoneNumber.setText(localStorage.agentPhone)
        }
        val agentName = localStorage.agent?.agentName
        if (agentName != null) {
            binding.welcomeMessageTv.text = "Welcome back, $agentName"
        }

        binding.emailSignInButton.setOnClickListener {
            mainScope.launch { attemptLogin() }
        }

        mainScope.launch {
            getLatestVersion(
                versionService = versionService,
                appDataStorage = get(),
                appConfig = appConfig,
            )
        }
        if (Platform.isPOS) {
            mainScope.launch {
                updateBinRoutes()
                checkRequirements()
            }
        }

        if (intent.getBooleanExtra("SESSION_TIMEOUT", false)) {
            dialogProvider.showError("Timeout due to inactivity")
        } else {
            val loggedOut = intent.getBooleanExtra("LOGGED_OUT", false)

            if (!loggedOut && jsonPrefs.contains("DATA_BANNER_IMAGES")) {
                startActivity(Intent(this, BannerActivity::class.java))
            }

            checkLocalSurveyQuestions()
        }

        downloadBannerImages()
        downloadSurveyQuestions()
        if (Platform.isPOS) {
            mainScope.launch { settle() }
        }

        binding.forgetDeviceBtn.setOnClickListener {
            mainScope.launch { attemptForgetDevice() }
        }

        binding.forgotPasswordBtn.setOnClickListener {
            val intent = Intent(this, AgentActivationActivity::class.java).apply {
                putExtra("phone_number", localStorage.agentPhone)
            }
            startActivity(intent)
        }

        debugOnly {
            if (BuildConfig.BUILD_TYPE == "debug") {
                binding.skipAuthBtn.run {
                    visibility = View.VISIBLE
                    setOnClickListener {
                        mainScope.launch {
                            if (!syncAgentInfo()) return@launch
                            val intent = Intent(this@LoginActivity, MainActivity::class.java)
                            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                            startActivity(intent)
                            finish()
                        }
                    }
                }
            }
        }
    }

    private suspend fun attemptForgetDevice() {
        val confirmed = dialogProvider.getConfirmation(
            title = "Forget Device",
            subtitle = "Are you sure you want to remove your ${getString(R.string.app_name)} profile from this device"
        )
        if (!confirmed) return
        if (Platform.isPOS) {
            dialogProvider.showProgressBar("Processing")
            settle()
            dialogProvider.hideProgressBar()

            val notificationCount = withContext(Dispatchers.IO) {
                posDatabase.posNotificationDao().count()
            }
            if (notificationCount > 0) {
                dialogProvider.showErrorAndWait(
                    "Hmm. \n" +
                            "There are pending cashout settlement requests \n" +
                            "Kindly contact your administrator"
                )
                return
            }
        }

        localStorage.edit { remove("ACTIVATED") }

        val intent = Intent(this, AgentActivationActivity::class.java)
        startActivity(intent)
        finish()
    }

    private suspend fun updateBinRoutes() {
        posPreferences.clearBinRoutes()
        val (response) = safeRunIO {
            posApiService.getBinRoutes(
                localStorage.institutionCode,
                localStorage.agentPhone
            )
        }
        if (response?.isSuccessful() == true) {
            posPreferences.binRoutes = response.data
        }
    }

    private fun checkLocalSurveyQuestions() {
        var surveyQuestions by jsonPrefs.jsonStore(
            "DATA_SURVEY_QUESTIONS",
            ListSerializer(SurveyQuestion.serializer())
        )
        val questions = surveyQuestions ?: return
        if (questions.isEmpty()) return

        SurveyDialog.create(this, questions) {
            onSubmit { data ->
                ioScope.launch {
                    safeRunIO {
                        val surveyData = SubmitSurveyRequest(
                            answers = data,
                            institutionCode = localStorage.institutionCode,
                            agentPhoneNumber = localStorage.agentPhone,
                            geoLocation = localStorage.lastKnownLocation,
                        )

                        staticService.submitSurvey(surveyData)
                    }
                    surveyQuestions = null
                }
            }
        }.show()
    }

    private fun downloadBannerImages() {
        var bannerImages by jsonPrefs.jsonStore(
            "DATA_BANNER_IMAGES",
            ListSerializer(String.serializer())
        )
        ioScope.launch {
            val (response) = safeRunIO {
                staticService.getBannerImages(
                    localStorage.institutionCode,
                    localStorage.agentPhone,
                    packageInfo?.versionName
                )
            }

            response ?: return@launch
            val data = response.data ?: return@launch

            if (response.isSuccessful) {
                data.forEach {
                    Coil.enqueue(
                        ImageRequest.Builder(this@LoginActivity)
                            .data(it)
                            .build()
                    )
                }
                bannerImages = data
            }
        }
    }

    private fun downloadSurveyQuestions() {
        var surveyQuestions by jsonPrefs.jsonStore<List<SurveyQuestion>>(
            "DATA_SURVEY_QUESTIONS",
            ListSerializer(SurveyQuestion.serializer())
        )
        ioScope.launch {
            val (response) = safeRunIO {
                staticService.getSurveyQuestions(
                    localStorage.institutionCode,
                    localStorage.agentPhone,
                    packageInfo?.versionName
                )
            }

            response ?: return@launch
            val data = response.data ?: return@launch

            if (response.isSuccessful) {
                surveyQuestions = data
            }
        }
    }

    override fun onResume() {
        super.onResume()
        ensureLocationEnabled()
    }

    override fun onBackPressed() {
        dialogProvider.confirm("Close Application", null) {
            onSubmit {
                val intent = Intent(Intent.ACTION_MAIN)
                intent.addCategory(Intent.CATEGORY_HOME)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
                finish()
            }
        }
    }

    private suspend fun attemptLogin() {
        val phoneNumber = binding.loginPhoneNumber.text.toString()
        val pin = binding.loginPin.text.toString()

        if (TextUtils.isEmpty(phoneNumber)) {
            showError("Phone number is required", R.id.login_phoneNumber)
            return
        }

        if (phoneNumber.length != 11) {
            showError("Phone number must be 11 digits", R.id.login_phoneNumber)
            return
        }

        if (localStorage.agentPhone != phoneNumber) {
            showError("Phone number is incorrect", R.id.login_phoneNumber)
            return
        }

        if (TextUtils.isEmpty(pin)) {
            showError("PIN is required", R.id.login_pin)
            return
        }

        if (pin.length != 4) {
            dialogProvider.showError("PIN must be 4 digits")
            return
        }

        if (!syncAgentInfo()) return
        dialogProvider.showProgressBar("Logging you in")
        val (response, error) = safeRunIO {
            staticService.confirmAgentInformation(
                localStorage.institutionCode,
                phoneNumber,
                pin,
                appVersionName,
                Platform.deviceType
            )
        }
        dialogProvider.hideProgressBar()

        if (error != null) return dialogProvider.showError(error)
        if (response == null) return dialogProvider.showError("PIN is invalid")
        if (!response.isSuccessful) return dialogProvider.showError(response.responseMessage)

        val agent = localStorage.agent
        firebaseAnalytics.logEvent("login", Bundle().apply {
            putString("agent_code", agent?.agentCode)
            putString("institution_code", localStorage.institutionCode)
            putString("phone_number", phoneNumber)
            putString("terminal_id", agent?.terminalID)
        })

        localStorage.edit {
            putString(
                "LAST_LOGIN",
                Instant.now().format(CREDIT_CLUB_REQUEST_DATE_PATTERN)
            )
        }

        val intent = Intent(this@LoginActivity, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
        startActivity(intent)
        finish()
    }

    private fun checkRequirements() {
        if (!posPreferences.hasBinRoutes) {
            return dialogProvider.confirm(
                "We couldn't download some settings",
                "Do you want to retry?"
            ) {
                onSubmit {
                    if (it) {
                        mainScope.launch {
                            updateBinRoutes()
                            checkRequirements()
                        }
                    }
                }
            }
        }
    }

    private fun showError(message: String, viewId: Int) {
        dialogProvider.showError(message)
        findViewById<View>(viewId).requestFocus()
    }

    private suspend fun settle() = withContext(Dispatchers.IO) {
        val appConfig: AppConfig by inject()
        val configService: PosConfig by inject()
        val posNotificationDao = posDatabase.posNotificationDao()
        val posApiService: PosApiService by retrofitService(TRANSACTIONS_CLIENT)

        val jobs = posNotificationDao.all().map { notification ->
            async {
                val (response) = safeRunSuspend {
                    posApiService.posCashOutNotification(
                        notification,
                        "iRestrict ${appConfig.posNotificationToken}",
                        notification.terminalId ?: configService.terminalId
                    )
                }

                if (!response?.billerReference.isNullOrBlank()) {
                    posNotificationDao.delete(notification)
                }
            }
        }

        jobs.awaitAll()
    }

    private suspend fun syncAgentInfo(): Boolean {
        dialogProvider.showProgressBar("Checking agent details")
        val (agent, error) = safeRunIO {
            staticService.getAgentInfoByPhoneNumber(
                localStorage.institutionCode,
                localStorage.agentPhone
            )
        }
        dialogProvider.hideProgressBar()

        if (error != null) {
            dialogProvider.showErrorAndWait(error)
            return false
        }
        if (agent == null) {
            dialogProvider.showErrorAndWait("Invalid agent details")
            return false
        }

        localStorage.agent = agent
        firebaseCrashlytics.setUserId(agent.agentCode ?: "0")
        firebaseCrashlytics.setCustomKey("agent_name", agent.agentName ?: "")
        firebaseCrashlytics.setCustomKey("agent_phone", agent.phoneNumber ?: "")
        firebaseCrashlytics.setCustomKey("terminal_id", agent.terminalID ?: "")

        if (Platform.isPOS) {
            val configHasChanged = posConfig.terminalId != agent.terminalID
            val defaultConnectionInfo = posTenant.infoList.find { it.id == agent.posMode }
                ?: InvalidRemoteConnectionInfo
            posConfig.remoteConnectionInfo = defaultConnectionInfo
            debug("default connection info is $defaultConnectionInfo")
            firebaseCrashlytics.setCustomKey("default_pos_mode", agent.posMode ?: "")
            firebaseCrashlytics.setCustomKey("pos_host", defaultConnectionInfo.host)
            firebaseCrashlytics.setCustomKey("pos_port", "${defaultConnectionInfo.port}")

            if (configHasChanged) {
                val notificationCount = withContext(Dispatchers.IO) {
                    posDatabase.posNotificationDao().count()
                }

                if (notificationCount > 0) {
                    dialogProvider.showErrorAndWait(
                        "Access restricted. \n" +
                                "There are pending cashout settlement requests against your previous terminal id. \n" +
                                "Kindly contact your administrator"
                    )
                    return false
                }

                posConfig.terminalId = agent.terminalID ?: ""
                posParameter.reset()
            }
        }

        return true
    }

    private fun ensureLocationEnabled() {
        var locationMode = 0

        try {
            locationMode = Settings.Secure.getInt(
                applicationContext.contentResolver,
                Settings.Secure.LOCATION_MODE
            )
        } catch (e: Settings.SettingNotFoundException) {

        }

        if (locationMode == 0) {
            val title = "An active GPS service is needed for this application"
            val subtitle = "Click 'OK' to activate it"

            dialogProvider.confirm(title, subtitle) {
                onSubmit {
                    if (it) {
                        val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                        startActivity(intent)
                    } else finish()
                }

                onClose {
                    finish()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        ioScope.cancel()
    }
}

