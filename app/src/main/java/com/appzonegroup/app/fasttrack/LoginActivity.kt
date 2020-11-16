package com.appzonegroup.app.fasttrack

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import com.appzonegroup.app.fasttrack.model.AppConstants
import com.appzonegroup.app.fasttrack.ui.SurveyDialog
import com.appzonegroup.app.fasttrack.utility.LocalStorage
import com.appzonegroup.app.fasttrack.utility.Misc
import com.appzonegroup.creditclub.pos.Platform
import com.appzonegroup.creditclub.pos.data.PosDatabase
import com.appzonegroup.creditclub.pos.data.posPreferences
import com.appzonegroup.creditclub.pos.extension.posConfig
import com.appzonegroup.creditclub.pos.extension.posParameter
import com.appzonegroup.creditclub.pos.service.ConfigService
import com.creditclub.core.CreditClubApplication
import com.creditclub.core.data.CreditClubMiddleWareAPI
import com.creditclub.core.data.api.BackendConfig
import com.creditclub.core.data.model.SurveyQuestion
import com.creditclub.core.data.request.SubmitSurveyRequest
import com.creditclub.core.data.response.isSuccessful
import com.creditclub.core.ui.CreditClubActivity
import com.creditclub.core.util.*
import com.creditclub.core.util.delegates.jsonStore
import com.creditclub.pos.api.PosApiService
import com.creditclub.pos.api.posApiService
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.coroutines.*
import org.koin.android.ext.android.inject
import retrofit2.create

class LoginActivity : CreditClubActivity() {

    private val jsonPrefs by lazy { getSharedPreferences("JSON_STORAGE", 0) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.navigationBarColor = getColor(R.color.colorPrimary)
        }
        debugOnly {
            val debugInfo = "Version ${packageInfo?.versionName}. ${BuildConfig.BUILD_TYPE}"
            version_tv.text = debugInfo
            login_phoneNumber.setText(localStorage.agentPhone)
        }

        findViewById<EditText>(R.id.login_phoneNumber).also {
            it.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence,
                    start: Int,
                    count: Int,
                    after: Int
                ) {

                }

                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                    phoneNumberEditTextFilter(it, this)
                }

                override fun afterTextChanged(s: Editable) {

                }
            })
        }

        findViewById<View>(R.id.email_sign_in_button).setOnClickListener {
            mainScope.launch { attemptLogin() }
        }

        mainScope.launch { (application as CreditClubApplication).getLatestVersion() }
        mainScope.launch {
            updateBinRoutes()
            checkRequirements()
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
    }

    private suspend fun updateBinRoutes(showDialog: Boolean = false) {
        posPreferences.clearBinRoutes()
        if (showDialog) dialogProvider.showProgressBar("Downloading pos settings")
        val (response) = safeRunIO {
            creditClubMiddleWareAPI.posApiService.getBinRoutes(
                localStorage.institutionCode,
                localStorage.agentPhone
            )
        }
        if (showDialog) dialogProvider.hideProgressBar()
        if (response?.isSuccessful() == true) {
            posPreferences.binRoutes = response.data
        }
    }

    private fun checkLocalSurveyQuestions() {
        var surveyQuestions by jsonPrefs.jsonStore<List<SurveyQuestion>>("DATA_SURVEY_QUESTIONS")
        val questions = surveyQuestions ?: return
        if (questions.isEmpty()) return

        SurveyDialog.create(this, questions) {
            onSubmit { data ->
                ioScope.launch {
                    safeRunIO {
                        val surveyData = SubmitSurveyRequest().apply {
                            answers = data
                            institutionCode = localStorage.institutionCode
                            agentPhoneNumber = localStorage.agentPhone
                            geoLocation = gps.geolocationString
                        }

                        creditClubMiddleWareAPI.staticService.submitSurvey(surveyData)
                    }
                    surveyQuestions = null
                }
            }
        }.show()
    }

    private fun downloadBannerImages() {
        var bannerImages by jsonPrefs.jsonStore<List<String>>("DATA_BANNER_IMAGES")
        ioScope.launch {
            val (response) = safeRunIO {
                creditClubMiddleWareAPI.staticService.getBannerImages(
                    localStorage.institutionCode,
                    localStorage.agentPhone,
                    packageInfo?.versionName
                )
            }

            response ?: return@launch
            val data = response.data ?: return@launch

            if (response.isSuccessful) {
                data.forEach { Picasso.get().load(it).fetch() }
                bannerImages = data
            }
        }
    }

    private fun downloadSurveyQuestions() {
        var surveyQuestions by jsonPrefs.jsonStore<List<SurveyQuestion>>("DATA_SURVEY_QUESTIONS")
        ioScope.launch {
            val (response) = safeRunIO {
                creditClubMiddleWareAPI.staticService.getSurveyQuestions(
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

    fun phoneNumberEditTextFilter(editText: EditText, textWatcher: TextWatcher) {
        val numbers = "0123456789"

        val text = editText.text.toString().trim { it <= ' ' }

        val textToCharArray = text.toCharArray()

        val accumulator = StringBuilder()

        for (c in textToCharArray) {
            if (numbers.contains(c + "")) {
                accumulator.append(c)
            }
        }
        editText.removeTextChangedListener(textWatcher)

        //This line without the line before and after will cause endless loop
        //of call to the text changed listener
        editText.setText(accumulator)
        editText.addTextChangedListener(textWatcher)
        editText.setSelection(accumulator.length)
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
        val phoneNumber = (findViewById<View>(R.id.login_phoneNumber) as EditText).text.toString()
        val pin = (findViewById<View>(R.id.login_pin) as EditText).text.toString()

        if (TextUtils.isEmpty(phoneNumber)) {
            showError("Phone number is required", R.id.login_phoneNumber)
            return
        }

        if (TextUtils.isEmpty(pin)) {
            showError("PIN is required", R.id.login_pin)
            return
        }

        if (!isPhoneValid(phoneNumber)) {
            showError("Phone number is incorrect", R.id.login_phoneNumber)
            return
        }

        if (pin.length != 4) {
            dialogProvider.showError("PIN must be 4 digits")
            return
        }

        if (!syncAgentInfo()) return
        dialogProvider.showProgressBar("Logging you in")
        val (response, error) = safeRunIO {
            creditClubMiddleWareAPI.staticService.confirmAgentInformation(
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

        val lastLogin = "Last Login: " + Misc.dateToLongString(Misc.getCurrentDateTime())
        LocalStorage.SaveValue(AppConstants.LAST_LOGIN, lastLogin, baseContext)

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

    private fun isPhoneValid(phoneNumber: String): Boolean {
        val storedPhone = LocalStorage.GetValueFor(AppConstants.AGENT_PHONE, baseContext)
        return phoneNumber == storedPhone
    }

    private suspend fun settle() = withContext(Dispatchers.IO) {
        val creditClubMiddleWareAPI: CreditClubMiddleWareAPI by inject()
        val posDatabase: PosDatabase = PosDatabase.getInstance(this@LoginActivity)
        val backendConfig: BackendConfig by inject()
        val configService: ConfigService by inject()
        val posNotificationDao = posDatabase.posNotificationDao()
        val posApiService: PosApiService = creditClubMiddleWareAPI.retrofit.create()

        val jobs = posNotificationDao.all().map { notification ->
            async {
                val (response) = safeRunSuspend {
                    posApiService.posCashOutNotification(
                        notification,
                        "iRestrict ${backendConfig.posNotificationToken}",
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
            creditClubMiddleWareAPI.staticService.getAgentInfoByPhoneNumber(
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
            val configHasChanged =
                posConfig.terminalId != agent.terminalID // || posConfig.posModeStr != agent.posMode

            if (configHasChanged) {
                val notificationCount = withContext(Dispatchers.IO) {
                    val posDatabase: PosDatabase = PosDatabase.getInstance(this@LoginActivity)
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

//                    posConfig.posModeStr = agent.posMode
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
}

