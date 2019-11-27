package com.appzonegroup.app.fasttrack.utility

import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.appzonegroup.app.fasttrack.BuildConfig
import com.appzonegroup.app.fasttrack.R
import com.appzonegroup.app.fasttrack.contract.Logger
import com.appzonegroup.app.fasttrack.dataaccess.AccountDAO
import com.appzonegroup.app.fasttrack.dataaccess.BeneficiaryDAO
import com.appzonegroup.app.fasttrack.dataaccess.DeviceTransactionInformationDAO
import com.appzonegroup.app.fasttrack.model.*
import com.appzonegroup.app.fasttrack.network.APICaller
import com.appzonegroup.app.fasttrack.network.ApiServiceObject
import com.appzonegroup.creditclub.pos.data.PosDatabase
import com.appzonegroup.creditclub.pos.models.NotificationResponse
import com.appzonegroup.creditclub.pos.service.ConfigService
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.*
import okhttp3.Headers
import java.util.*

/**
 * Created by Oto-obong on 03/08/2017.
 */

class SyncService : Service(), Logger {
    override val tag: String = "SyncService"

    internal var notificationMgr: NotificationManager? = null
    private val am: AlarmManager by lazy { this.getSystemService(Context.ALARM_SERVICE) as AlarmManager }
    private val pi: PendingIntent by lazy {
        val intent = Intent(this, MyReceiver::class.java)
        PendingIntent.getBroadcast(this, 1, intent, 0)
    }
    private val interval = 600000 //10  mins

    internal val thread: Thread by lazy { Thread(SyncClass(), "sync_thread") }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()

        am.setRepeating(AlarmManager.RTC_WAKEUP, Calendar.getInstance().timeInMillis + interval, interval.toLong(), pi)

//        startMiddlewareNotifications()

        val handler = Handler()
//        val updateChecker = UpdateChecker(this, "Single")
//        updateChecker.run()
        /*Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                new Thread(
                        new Runnable() {
                            @Override
                            public void run() {
                                new UpdateChecker(SyncService.this, "In timer");//.start();
                            }
                        }
                );

            }
        }, 60 * 60 * 1000, 60000);*/

        handler.postDelayed({
//            updateChecker.run()
            //new UpdateChecker(SyncService.this, "Single");
        }, (60 * 60 * 1000).toLong())//Check every hour
    }

    private fun startMiddlewareNotifications() {
        val config = ConfigService.getInstance(this)

        GlobalScope.launch(Dispatchers.IO) {
            log("Running middleware notifications....")

            val url = "${ApiServiceObject.BASE_URL}/${ApiServiceObject.STATIC}/POSCashOutNotification"
            val serializer = Gson()
            val dao = PosDatabase.getInstance(this@SyncService).posNotificationDao()
            var notifications = dao.all()

            while (true) {
                if (notifications.isNotEmpty()) {
                    val dataToSend = serializer.toJson(notifications.first())
                    log("PosNotification request: $dataToSend")

                    val headers = Headers.Builder()
                    headers.add("Authorization", "iRestrict ${BuildConfig.NOTIFICATION_TOKEN}")
                    headers.add("TerminalID", config.terminalId)
                    val (responseString, error) = withContext(Dispatchers.IO) {
                        ApiServiceObject.post(url, dataToSend, headers.build())
                    }

                    error?.printStackTrace()

                    responseString?.also {
                        log("PosNotification response: $responseString")
                        try {
                            val response = serializer.fromJson(responseString, NotificationResponse::class.java)
                            if (response != null) {
                                if (response.isSuccessFul) {
                                    dao.delete(notifications.first().id)
                                }
                            }
                        } catch (ex: Exception) {
                            ex.printStackTrace()
                        }
                    }
                }

                delay(10000)

                notifications = dao.all()
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        try {
            thread.start()
        } catch (ex: Exception) {
            //displayNotificationMessage(ex.getMessage());
        }

        return super.onStartCommand(intent, flags, startId)
    }

    internal inner class SyncClass : Runnable {

        var accountDAO: AccountDAO
        var beneficiaryDAO: BeneficiaryDAO
        //Context context;
        var gson: Gson
        var agentPhone = ""
        var agentPIN = ""
        var institutionCode = ""

        init {
            //this.context = context;

            doNotification("Sync Service is running!")

            accountDAO = AccountDAO(baseContext)
            beneficiaryDAO = BeneficiaryDAO(baseContext)
            gson = Gson()
            agentPhone = LocalStorage.GetValueFor(AppConstants.AGENT_PHONE, baseContext)
            agentPIN = LocalStorage.GetValueFor(AppConstants.AGENT_PIN, baseContext)
            institutionCode = LocalStorage.GetValueFor(AppConstants.INSTITUTION_CODE, baseContext)
        }

        override fun run() {

            getToken()
            syncBeneficiaries()
            logTransactionsInfoToServer()

        }

        private fun sleep() {
            try {
                Thread.sleep(10000)
            } catch (ex: InterruptedException) {
                Log.e("SyncClassSleep", ex.toString())
            }

        }


        fun doNotification(message: String) {
            val mBuilder = NotificationCompat.Builder(baseContext)
                .setContentTitle(resources.getString(R.string.app_name))
                .setContentText(message) as NotificationCompat.Builder

            val resultPendingIntent = PendingIntent.getActivity(
                applicationContext,
                0, Intent(), PendingIntent.FLAG_CANCEL_CURRENT
            )
            mBuilder.setContentIntent(resultPendingIntent)
            //            NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            //            // mId allows you to update the notification later on.
            //            mNotificationManager.notify(1234567, mBuilder.build());
        }

        private fun logTransactionsInfoToServer() {
            val url =
                AppConstants.getBaseUrl() + "/CreditClubMiddleWareAPI/Api/MobileTracking/SaveAgentMobileTrackingDetails"

            val transactionInformationDAO = DeviceTransactionInformationDAO(baseContext)
            var transactionInformations = transactionInformationDAO.GetAll()
            val finishedTransacionInformations = ArrayList<DeviceTransactionInformation>()
            val ar = LocalStorage.getCachedAuthResponse(applicationContext)
            var sessionId: String? = "nothing"
            try {
                sessionId = ar!!.sessionId
            } catch (e: Exception) {
                e.printStackTrace()
            }

            for (information in transactionInformations) {
                if (information.sessionID != sessionId) {
                    val sum =
                        information.successCount + information.errorResponse + information.noInternet + information.noInternet
                    if (information.requestCount > sum) {
                        information.noInternet = information.noInternet + 1
                    }
                    finishedTransacionInformations.add(information)
                }
            }
            val gson = Gson()

            while (transactionInformations.size > 0) {
                val dataToSend = gson.toJson(finishedTransacionInformations)
                if (APICaller.postRequest(baseContext, url, dataToSend) != null) {
                    if (!transactionInformationDAO.DeleteSentRecords(
                            transactionInformations[0].id,
                            transactionInformations[transactionInformations.size - 1].id
                        )
                    )
                        Log.e("Failed Delete", "Deleting device ic_help failed")
                }
                transactionInformations = transactionInformationDAO.GetAll()
                sleep()
            }

        }

        private fun getToken() {

            val tokenUrl = AppConstants.getApiTokenUrl() + "GetToken?appId=edef4ef"

            val response = APICaller.makeGetRequest2(tokenUrl)

            try {

                val tokenClass = Gson().fromJson(response!!.trim { it <= ' ' }, Token::class.java)

                val Token = tokenClass.token

                val decryptedToken = TripleDES.decrypt(Token)

                LocalStorage.SaveValue(AppConstants.API_TOKEN, decryptedToken, baseContext)

                sleep()
            } catch (ex: Exception) {

            }

        }

        private fun syncBeneficiaries() {
            val beneficiaries = beneficiaryDAO.GetSync("Pending") as ArrayList<Beneficiary>

            val institutionCode = LocalStorage.GetValueFor(AppConstants.INSTITUTION_CODE, baseContext)
            val agentPhone = LocalStorage.GetValueFor(AppConstants.AGENT_PHONE, baseContext)

            for (beneficiary in beneficiaries) {
                val url = Misc.syncBeneficiaryUrl(institutionCode, agentPhone, beneficiary.trackingReference)

                val Photo = gson.toJson(beneficiary.photo)

                val response = APICaller.firstPostRequest(url, Photo)

                val serverResponse: SyncBeneficiaryResponse
                try {

                    serverResponse = gson.fromJson(response, SyncBeneficiaryResponse::class.java)

                    if (serverResponse.status == true) {

                        beneficiary.sync = "Yes"

                        beneficiaryDAO.UpdateBeneficiary(beneficiary)
                    } else {

                    }
                } catch (ex: Exception) {
                    Log.e("syncBeneficiaries: ", ex.message)
                }

            }
        }
    }
}


