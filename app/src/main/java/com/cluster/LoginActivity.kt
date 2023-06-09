package com.cluster

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils
import android.util.Log
import android.view.View
import androidx.core.content.edit
import androidx.databinding.library.BuildConfig
import coil.Coil
import coil.request.ImageRequest
import com.cluster.core.data.TRANSACTIONS_CLIENT
import com.cluster.core.data.api.*
import com.cluster.core.data.model.LoginRequest
import com.cluster.core.data.model.SurveyQuestion
import com.cluster.core.data.request.SubmitSurveyRequest
import com.cluster.core.data.response.isSuccessful
import com.cluster.core.ui.CreditClubActivity
import com.cluster.core.ui.getLatestVersion
import com.cluster.core.util.*
import com.cluster.core.util.delegates.*
import com.cluster.databinding.ActivityLoginBinding
import com.cluster.pos.InvalidRemoteConnectionInfo
import com.cluster.pos.Platform
import com.cluster.pos.PosConfig
import com.cluster.pos.api.PosApiService
import com.cluster.pos.data.PosDatabase
import com.cluster.pos.data.PosPreferences
import com.cluster.pos.extension.posConfig
import com.cluster.pos.extension.posParameter
import com.cluster.pos.model.PosTenant
import com.cluster.ui.SurveyDialog
import com.cluster.ui.dataBinding
import kotlinx.coroutines.*
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import org.koin.android.ext.android.get
import org.koin.android.ext.android.inject
import java.time.Instant
import java.util.*

private const val PASSWORD_LENGTH = 6

class LoginActivity : CreditClubActivity(R.layout.activity_login) {
    private val posTenant: PosTenant by inject()
    private val posPreferences: PosPreferences by inject()
    private val jsonPrefs by lazy { getSharedPreferences("JSON_STORAGE", 0) }
    private val posDatabase: PosDatabase by inject()
    private val ioScope = CoroutineScope(Dispatchers.IO)
    private val binding: ActivityLoginBinding by dataBinding()
    private val posApiService: PosApiService by retrofitService()
    private val staticService: StaticService by retrofitService()
    private val authService: AuthService by retrofitService()
    private val versionService: VersionService by retrofitService()
    private lateinit var featureCodes: String

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
                localStorage = localStorage,
                deviceType = Platform.deviceType,
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
            val intent = Intent(this, ForgotLoginPinActivity::class.java).apply {
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
                            if (!getInstitutionFeatures()) return@launch
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
            showError("Login PIN is required", R.id.login_pin)
            return
        }

        if (pin.length != PASSWORD_LENGTH && pin.length != 4) {
            dialogProvider.showError("Login PIN must be $PASSWORD_LENGTH digits or 4 digits for old PIN")
            return
        }

        if (!syncAgentInfo()) return
        if (!getInstitutionFeatures()) return
        dialogProvider.showProgressBar("Logging you in")
        val deviceID = localStorage.deviceNumber
        val terminalID = localStorage.agent?.terminalID
        val flowID = "LGN"
        val uniqueReference = UUID.randomUUID().toString()
        val locationTracking = localStorage.lastKnownLocation
        val currentTimeStamp = Instant.now().toString("dd-MM-yyyy hh:mm")
        val agentCategory = localStorage.agent!!.agentCategory
        val userType = "UserType"
        val instituteCode = localStorage.institutionCode
        val (response, error) = safeRunIO {
            val request = LoginRequest(
                agentPhoneNumber = phoneNumber,
                appVersion = appVersionName,
                deviceType = Platform.deviceType,
                institutionCode = localStorage.institutionCode!!,
                password = pin,
            )
            authService.login(
                deviceReference = "$deviceID-$terminalID",
                flowReference = "$flowID-$uniqueReference-$locationTracking",
                operationReference = "$uniqueReference-$currentTimeStamp",
                userReference = "$instituteCode-$agentCategory-$userType",
                request
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

        localStorage.agentLoanEligibility = response.data?.loanEligibility
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

    private suspend fun getInstitutionFeatures(): Boolean {
        dialogProvider.showProgressBar("Getting Agent Features")
        val(features, error) = safeRunIO {
            staticService.getInstitutionFeatures(
                localStorage.institutionCode!!,
                localStorage.agent!!.agentCategory
            )
        }
        dialogProvider.hideProgressBar()

        if(error != null){
            dialogProvider.showErrorAndWait(error)
            return false
        }

        if(features == null){
            dialogProvider.showErrorAndWait("Error getting agent features")
            return false
        }

        if(features.isSuccessful){
            for (i in features.data) {
                Log.d("OkHttpClient", "This is a returned feature: ${i?.code}")
                jsonPrefs.addItemToList("institution_features", i?.code)
            }
        }
        return true
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

