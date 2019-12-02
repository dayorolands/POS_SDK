package com.appzonegroup.app.fasttrack

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import com.appzonegroup.app.fasttrack.model.AppConstants
import com.appzonegroup.app.fasttrack.ui.SurveyDialog
import com.appzonegroup.app.fasttrack.utility.Dialogs
import com.appzonegroup.app.fasttrack.utility.LocalStorage
import com.appzonegroup.app.fasttrack.utility.Misc
import com.appzonegroup.creditclub.pos.Platform
import com.appzonegroup.creditclub.pos.util.posConfig
import com.appzonegroup.creditclub.pos.util.posParameters
import com.creditclub.core.data.model.SurveyQuestion
import com.creditclub.core.data.prefs.JsonStorage
import com.creditclub.core.data.request.SubmitSurveyRequest
import com.creditclub.core.ui.CreditClubActivity
import com.creditclub.core.util.*
import com.squareup.picasso.Picasso
import kotlinx.coroutines.launch

class LoginActivity : CreditClubActivity() {

    private val jsonStore by lazy { JsonStorage.getStore(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val environment = if (BuildConfig.DEBUG) ". Staging" else ""
        findViewById<TextView>(R.id.version_tv).text =
            "Version ${packageInfo?.versionName}$environment"

        if (BuildConfig.DEBUG) {
            findViewById<EditText>(R.id.login_phoneNumber).setText(localStorage.agentPhone)
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
            val (response, error) = safeRunIO {
                creditClubMiddleWareAPI.staticService.getAgentInfoByPhoneNumber(
                    localStorage.institutionCode,
                    localStorage.agentPhone
                )
            }

            if (error != null) return@launch
            response ?: return@launch

            safeRun {
                localStorage.agent = response

                if (Platform.isPOS) {
                    val configHasChanged =
                        posConfig.terminalId != response.terminalID // || posConfig.posModeStr != response.posMode

                    posConfig.terminalId = response.terminalID ?: ""
//                    posConfig.posModeStr = response.posMode

                    if (configHasChanged) {
                        posParameters.reset()
                    }
                }
            }
        }

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

    private fun checkLocalSurveyQuestions() {
        if (jsonStore.has("SURVEY_QUESTIONS")) {
            val questionsJson = jsonStore.get<List<SurveyQuestion>>("SURVEY_QUESTIONS")
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

            if (response.isSuccessful()) {
                bannerImageList.forEach { Picasso.get().load(it).fetch() }
                jsonStore.save("BANNER_IMAGES", bannerImageList)
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

            if (response.isSuccessful()) {
                jsonStore.save("SURVEY_QUESTIONS", surveyQuestions)
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

        val notification = Dialogs.getQuestionDialog(this@LoginActivity, "Do you want to exit? ")
        val logoutNoButton = notification.findViewById<Button>(R.id.cancel_btn)
        val logoutYesButton = notification.findViewById<Button>(R.id.ok_btn)

        logoutNoButton.setOnClickListener { notification.dismiss() }

        logoutYesButton.setOnClickListener {
            notification.dismiss()
            val intent = Intent(Intent.ACTION_MAIN)
            intent.addCategory(Intent.CATEGORY_HOME)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
        }
        notification.show()
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

            val lastLogin = "Last Login: " + Misc.dateToLongString(Misc.getCurrentDateTime())
            LocalStorage.SaveValue(AppConstants.LAST_LOGIN, lastLogin, baseContext)

            val activityToOpen = when {
                packageName.toLowerCase().contains("cashout") -> CashoutMainMenuActivity::class.java
                packageName.toLowerCase().contains(".creditclub") -> CreditClubMainMenuActivity::class.java
                else -> Menu3Activity::class.java
            }

            val intent = Intent(this@LoginActivity, activityToOpen)
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
        var locationMode = 0 // 0 == Settings.Secure.LOCATION_MODE_OFF
        var locationProviders: String? = null

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            try {
                locationMode = Settings.Secure.getInt(
                    applicationContext.contentResolver,
                    Settings.Secure.LOCATION_MODE
                )
            } catch (e: Settings.SettingNotFoundException) {

            }

        } else {
            locationProviders = Settings.Secure.getString(
                applicationContext.contentResolver,
                Settings.Secure.LOCATION_PROVIDERS_ALLOWED
            )
        }

        val locationEnabled = !TextUtils.isEmpty(locationProviders) || locationMode != 0
        if (!locationEnabled) {

            com.appzonegroup.app.fasttrack.ui.Dialogs.confirm(
                this,
                "An active GPS service is needed for this application",
                "Click 'OK' to activate it"
            ) {
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

