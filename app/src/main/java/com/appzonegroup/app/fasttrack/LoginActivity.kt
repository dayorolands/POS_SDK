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
import com.appzonegroup.app.fasttrack.utility.extensions.syncAgentInfo
import com.appzonegroup.creditclub.pos.data.posPreferences
import com.creditclub.core.CreditClubApplication
import com.creditclub.core.data.model.SurveyQuestion
import com.creditclub.core.data.prefs.JsonStorage
import com.creditclub.core.data.request.SubmitSurveyRequest
import com.creditclub.core.data.response.isSuccessful
import com.creditclub.core.ui.CreditClubActivity
import com.creditclub.core.util.*
import com.creditclub.pos.api.posApiService
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.coroutines.launch
import kotlinx.serialization.builtins.list
import kotlinx.serialization.builtins.serializer

class LoginActivity : CreditClubActivity() {

    private val jsonStore by lazy { JsonStorage.getStore(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.navigationBarColor = getColor(R.color.colorPrimary)
        }

        debugOnly {
            val debugInfo = "Version ${packageInfo?.versionName}. ${BuildConfig.BUILD_TYPE}"
            version_tv.text = debugInfo
            login_phoneNumber.setText(localStorage.agent?.phoneNumber)
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

        findViewById<View>(R.id.email_sign_in_button).setOnClickListener { attemptLogin() }

        mainScope.launch {
            syncAgentInfo()
            firebaseAnalytics.setUserId(localStorage.agent?.agentCode)
        }
        mainScope.launch { (application as CreditClubApplication).getLatestVersion() }
        mainScope.launch { updateBinRoutes() }

        if (intent.getBooleanExtra("SESSION_TIMEOUT", false)) {
            dialogProvider.showError("Timeout due to inactivity")
        } else {
            val loggedOut = intent.getBooleanExtra("LOGGED_OUT", false)

            if (!loggedOut && jsonStore.has("BANNER_IMAGES")) {
                startActivity(Intent(this, BannerActivity::class.java))
            }

            checkLocalSurveyQuestions()
        }

        downloadBannerImages()
        downloadSurveyQuestions()
    }

    private suspend fun updateBinRoutes() {
        val (response) = safeRunIO {
            creditClubMiddleWareAPI.posApiService.getBinRoutes(
                localStorage.institutionCode,
                localStorage.agentPhone
            )
        }
        if (response?.isSuccessful() == true) {
            posPreferences.binRoutes = response.data
        }
    }

    private fun checkLocalSurveyQuestions() {
        if (jsonStore.has("SURVEY_QUESTIONS")) {
            val questionsJson = jsonStore.get("SURVEY_QUESTIONS", SurveyQuestion.serializer().list)
            val questions = questionsJson.data ?: return
            if (questions.isEmpty()) return

            SurveyDialog.create(this, questions) {
                onSubmit { data ->
                    ioScope.launch {
                        safeRunIO {
                            val surveyData = SubmitSurveyRequest().apply {
                                answers = data
                                institutionCode = localStorage.institutionCode
                                agentPhoneNumber = localStorage.agent?.phoneNumber
                                geoLocation = gps.geolocationString
                            }

                            creditClubMiddleWareAPI.staticService.submitSurvey(surveyData)
                        }
                        jsonStore.delete("SURVEY_QUESTIONS")
                    }
                }
            }.show()
        }
    }

    private fun downloadBannerImages() {
        ioScope.launch {
            val (response) = safeRunIO {
                creditClubMiddleWareAPI.staticService.getBannerImages(
                    localStorage.institutionCode,
                    localStorage.agent?.phoneNumber,
                    packageInfo?.versionName
                )
            }

            response ?: return@launch
            val bannerImageList = response.data ?: return@launch

            if (response.isSuccessful) {
                bannerImageList.forEach { Picasso.get().load(it).fetch() }
                jsonStore.save("BANNER_IMAGES", bannerImageList, String.serializer().list)
            }
        }
    }

    private fun downloadSurveyQuestions() {
        ioScope.launch {
            val (response) = safeRunIO {
                creditClubMiddleWareAPI.staticService.getSurveyQuestions(
                    localStorage.institutionCode,
                    localStorage.agent?.phoneNumber,
                    packageInfo?.versionName
                )
            }

            response ?: return@launch
            val surveyQuestions = response.data ?: return@launch

            if (response.isSuccessful) {
                jsonStore.save(
                    "SURVEY_QUESTIONS",
                    surveyQuestions,
                    SurveyQuestion.serializer().list
                )
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

    private fun attemptLogin() {
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

        mainScope.launch {
            dialogProvider.showProgressBar("Logging you in")
            val (response, error) = safeRunIO {
                creditClubMiddleWareAPI.staticService.confirmAgentInformation(
                    localStorage.institutionCode,
                    phoneNumber,
                    pin
                )
            }
            dialogProvider.hideProgressBar()

            if (error != null) return@launch dialogProvider.showError(error)
            if (response == null) return@launch dialogProvider.showError("PIN is invalid")
            if (!response.isSuccessful) return@launch dialogProvider.showError(response.responseMessage)

            firebaseAnalytics.logEvent("login", Bundle().apply {
                putString("agent_code", localStorage.agent?.agentCode)
                putString("institution_code", localStorage.institutionCode)
                putString("phone_number", phoneNumber)
            })

            val lastLogin = "Last Login: " + Misc.dateToLongString(Misc.getCurrentDateTime())
            LocalStorage.SaveValue(AppConstants.LAST_LOGIN, lastLogin, baseContext)

            val intent = Intent(this@LoginActivity, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
            startActivity(intent)
            finish()
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

