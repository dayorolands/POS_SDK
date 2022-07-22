package com.cluster

import android.content.DialogInterface
import android.os.Bundle
import android.text.Html
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContentProviderCompat.requireContext
import com.cluster.fragment.online.EnterDetailFragment
import com.cluster.fragment.online.ListOptionsFragment
import com.cluster.model.TransactionCountType
import com.cluster.model.online.Response
import com.cluster.network.online.APIHelper
import com.cluster.utility.Misc
import com.cluster.utility.online.ErrorMessages
import com.cluster.utility.online.convertXmlToJson
import com.cluster.core.data.Encryption.decrypt
import com.cluster.core.data.Encryption.generateSessionId
import com.cluster.core.ui.CreditClubActivity
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import org.koin.android.ext.android.get
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeoutException

class OnlineActivity : CreditClubActivity(R.layout.bottom_sheet) {
    private val authResponse get() = (application as ClusterApplication).authResponse
    private val apiHelper by lazy {
        APIHelper(
            ctx = baseContext,
            scope = mainScope,
            localStorage = localStorage,
            client = get()
        )
    }
    private val currentTime: LocalDateTime = LocalDateTime.now()
    private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isHome = false
        firebaseAnalytics.logEvent("OnEntryHLATagging", Bundle().apply {
            firebaseAnalytics.setUserId(localStorage.agent!!.agentCode)
            putString("activity_type", "HLA Tagging(Entry)")
            putString("hla_entry_time", currentTime.format(formatter))
        })

        goHome()
    }

    fun goHome() {
        if (isHome) {
            firebaseAnalytics.logEvent("OnExitHLATagging", Bundle().apply {
                firebaseAnalytics.setUserId(localStorage.agent!!.agentCode)
                putString("activity_type", "HLA Tagging(Exit)")
                putString("hla_exit_time", currentTime.format(formatter))
            })
            showCloseDialog()
            return
        }
        isHome = true
        //(isHome)
        run {
            val dialog = AlertDialog.Builder(this)
                .setPositiveButton("OK", null)
            val phoneNumber = authResponse.phoneNumber
            val verificationCode = authResponse.activationCode
            val sessionId = generateSessionId(phoneNumber)
            dialogProvider.showProgressBar("Loading")
            Misc.resetTransactionMonitorCounter(baseContext)
            apiHelper.attemptValidation(
                phoneNumber,
                sessionId,
                verificationCode,
                localStorage.lastKnownLocation,
                false
            ) { e, result, _ ->
                dialogProvider.hideProgressBar()
                if (e == null && result != null) {
                    try {
                        val answer = Response.fixResponse(result)
                        val decryptedAnswer = decrypt(answer)
                        val response = convertXmlToJson(decryptedAnswer)
                        if (response == null) {
                            Misc.increaseTransactionMonitorCounter(
                                baseContext,
                                TransactionCountType.NO_INTERNET_COUNT,
                                sessionId
                            )
                            dialogProvider.showError("Connection lost")
                        } else {
                            Misc.increaseTransactionMonitorCounter(
                                baseContext, TransactionCountType.SUCCESS_COUNT, sessionId
                            )
                            val resp = response.toString()
                            val responseBase = response.getJSONObject("Response")
                            if (responseBase != null) {
                                val shouldClose = responseBase.optInt("ShouldClose", 1)
                                if (shouldClose == 0) {
                                    /*JSONObject auth = new JSONObject();
                                                auth.put("phone_number", phoneNumber);
                                                auth.put("session_id", sessionId);
                                                new CacheHelper(getApplicationContext()).saveCacheAuth(auth.toString());*/
                                    authResponse.sessionId = sessionId
                                    if (resp.contains("MenuItem")) {
                                        val menuWrapper =
                                            responseBase.getJSONObject("Menu")
                                                .getJSONObject("Response")
                                                .getJSONObject("Display")
                                        supportFragmentManager.beginTransaction()
                                            .replace(
                                                R.id.container,
                                                ListOptionsFragment.instantiate(
                                                    menuWrapper,
                                                    true
                                                )
                                            )
                                            .commitAllowingStateLoss()
                                    } else {
                                        val menuWrapper =
                                            responseBase.getJSONObject("Menu")
                                                .getJSONObject("Response")["Display"]
                                        if (menuWrapper is String && resp.contains("ShouldMask") && !resp.contains(
                                                "Invalid Response"
                                            )
                                        ) {
                                            val data = responseBase.getJSONObject("Menu")
                                                .getJSONObject("Response")
                                            supportFragmentManager.beginTransaction()
                                                .replace(
                                                    R.id.container,
                                                    EnterDetailFragment.instantiate(
                                                        data,
                                                        resp.contains("ACTIVATION CODE")
                                                    )
                                                )
                                                .commit()
                                        } else {
                                            val message =
                                                responseBase.getJSONObject("Menu")
                                                    .getJSONObject("Response")
                                                    .getString("Display")
                                            dialog.setMessage(Html.fromHtml(message)).show()
                                        }
                                    }
                                } else {
                                    if (responseBase.toString().contains("Display")) {
                                        dialog.setMessage(
                                            Html.fromHtml(
                                                responseBase.getJSONObject("Menu")
                                                    .getJSONObject("Response")
                                                    .optString(
                                                        "Display",
                                                        ErrorMessages.OPERATION_NOT_COMPLETED
                                                    )
                                            )
                                        ).setPositiveButton(
                                            "OK",
                                            DialogInterface.OnClickListener { dialogInterface, i ->
                                                Misc.increaseTransactionMonitorCounter(
                                                    baseContext,
                                                    TransactionCountType.ERROR_RESPONSE_COUNT,
                                                    sessionId
                                                )
                                                goHome()
                                            })
                                            .show()
                                    } else {
                                        dialog.setMessage(
                                            Html.fromHtml(
                                                responseBase.optString(
                                                    "Menu",
                                                    ErrorMessages.PHONE_NOT_REGISTERED
                                                )
                                            )
                                        ).show()
                                    }
                                }
                            }
                        }
                    } catch (c: Exception) {
                        FirebaseCrashlytics.getInstance().recordException(c)
                        c.printStackTrace()
                    }
                } else {
                    if (e != null) {
                        e.printStackTrace()
                        if (e is TimeoutException) {
                            Misc.increaseTransactionMonitorCounter(
                                baseContext,
                                TransactionCountType.NO_INTERNET_COUNT,
                                sessionId
                            )
                            dialog.setMessage("Something went wrong! Please try again.")
                                .setPositiveButton(
                                    "OK",
                                    object : DialogInterface.OnClickListener {
                                        override fun onClick(
                                            dialogInterface: DialogInterface,
                                            i: Int,
                                        ) {
                                            goHome()
                                        }
                                    }).setCancelable(false)
                                .show()
                        } else {
                            Misc.increaseTransactionMonitorCounter(
                                baseContext,
                                TransactionCountType.NO_INTERNET_COUNT,
                                sessionId
                            )
                            dialogProvider.showError("Connection lost")
                        }
                    } else {
                        Misc.increaseTransactionMonitorCounter(
                            baseContext, TransactionCountType.NO_INTERNET_COUNT, sessionId
                        )
                        dialogProvider.showError("Connection lost")
                    }
                }
            }
        }
    }

    override fun onBackPressed() {
        firebaseAnalytics.logEvent("OnExitHLATagging", Bundle().apply {
            firebaseAnalytics.setUserId(localStorage.agent!!.agentCode)
            putString("activity_type", "HLA Tagging(Exit)")
            putString("hla_exit_time", currentTime.format(formatter))
        })
        goHome()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_go_offline, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.menu_go_offline) {
            firebaseAnalytics.logEvent("OnExitHLATagging", Bundle().apply {
                firebaseAnalytics.setUserId(localStorage.agent!!.agentCode)
                putString("activity_type", "HLA Tagging(Exit)")
                putString("hla_exit_time", currentTime.format(formatter))
            })
            showCloseDialog()
        }
        if (item.itemId == android.R.id.home) {
            finish()
        }
        return true
    }

    private fun showCloseDialog() {
        dialogProvider.confirm("Go Offline?") {
            onSubmit {
                if (it) {
                    finish()
                }
            }
        }
    }

    companion object {
        @JvmField
        var isHome = false
    }
}