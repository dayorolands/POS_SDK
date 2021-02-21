package com.appzonegroup.app.fasttrack

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ListView
import android.widget.Toast
import com.appzonegroup.app.fasttrack.adapter.BillerAdapter
import com.appzonegroup.app.fasttrack.model.AppConstants
import com.appzonegroup.app.fasttrack.model.Biller
import com.appzonegroup.app.fasttrack.network.APICaller
import com.appzonegroup.app.fasttrack.scheduler.AndroidSchedulers
import com.appzonegroup.app.fasttrack.scheduler.HandlerScheduler
import com.appzonegroup.app.fasttrack.utility.BillsLocalStorage
import com.appzonegroup.app.fasttrack.utility.Dialogs
import com.appzonegroup.app.fasttrack.utility.Misc
import com.creditclub.core.util.safeRun
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import rx.Observable
import rx.Subscriber
import java.util.*

class BillerActivity : BaseActivity(), View.OnClickListener {

    internal val backgroundHandler: Handler by lazy { Misc.setupScheduler() }
    internal val listView: ListView by lazy { findViewById<ListView>(R.id.biller_listview) }
    private var extras: Bundle? = null
    internal var categoryIdField: String? = null
    private var categoryNameField: String? = null
    private var propertyChanged: String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_biller)

        extras = intent.extras

        if (extras != null) {
            categoryIdField = extras!!.getString("categoryId")
            categoryNameField = extras!!.getString("categoryName")
            propertyChanged = extras!!.getString("propertyChanged")
        } else {
            categoryIdField =
                BillsLocalStorage.GetValueFor(AppConstants.CATEGORYID, this@BillerActivity)
            categoryNameField =
                BillsLocalStorage.GetValueFor(AppConstants.CATEGORYNAME, this@BillerActivity)
            propertyChanged =
                BillsLocalStorage.GetValueFor(AppConstants.PROPERTYCHANGED, this@BillerActivity)
        }

        title = categoryNameField

        showProgressBar("Loading billers")
        runScheduler()
    }

    override fun onClick(view: View) {
        showProgressBar("Loading billers")
        runScheduler()
    }


    internal fun runScheduler() {
        myObservable()
            // Run on a background thread
            .subscribeOn(HandlerScheduler.from(backgroundHandler))
            // Be notified on the main thread
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Subscriber<String>() {

                override fun onCompleted() {

                }

                override fun onError(e: Throwable) {
                    /*if (loadingDialog.isShowing())
    {
    loadingDialog.dismiss();
    }*/
                    //Toast.makeText(getBaseContext(), e.toString(), Toast.LENGTH_LONG).show();
                    Log.e(this@BillerActivity.javaClass.simpleName, e.toString())
                    FirebaseCrashlytics.getInstance()
                        .recordException(Exception("Message: An error just occurred. Please try again later, Cause: " + e.message))

                }

                override fun onNext(result: String?) {
                    var result = result

                    hideProgressBar()

                    if (result == null) {
                        Dialogs.showErrorMessage(
                            this@BillerActivity,
                            "An error occurred. Please ensure that you have internet connection"
                        )
                        return
                    }

                    if (result.isEmpty()) {
                        Toast.makeText(
                            baseContext,
                            "You don't seem to have internet... Please ensure that you have internet connection",
                            Toast.LENGTH_LONG
                        ).show()
                        //return;
                    } else {

                        result = result.replace("\\", "").replace("\n", "").trim { it <= ' ' }
                        if (result.startsWith("\"") && result.endsWith("\"")) {
                            result = result.substring(1, result.length - 1)
                        }
                        result = result.replace("\"D1", "D1")
                        result = result.replace("”", "")

                        Log.e(this@BillerActivity.javaClass.simpleName, result)

                        val typeToken = object : TypeToken<List<Biller>>() {}

                        var billers = Gson().fromJson<List<Biller>>(result, typeToken.type)
                        billers = billers.filter { b ->
                            b.categoryIdField == categoryIdField || b.billerCategoryId == categoryIdField
                        }

                        val billerAdapter = BillerAdapter(this@BillerActivity, billers)
                        listView.adapter = billerAdapter

                        listView.onItemClickListener =
                            AdapterView.OnItemClickListener { parent, view, position, id ->
                                val biller = billers[position]

                                startActivityForResult(
                                    Intent(
                                        this@BillerActivity,
                                        BillerItemActivity::class.java
                                    ).apply {
                                        putExtra(
                                            "customer",
                                            intent.getSerializableExtra("customer")
                                        )
                                        putExtra("biller", biller)
                                        putExtra("categoryId", categoryIdField)
                                        putExtra("categoryName", categoryNameField)
                                        putExtra(
                                            "isAirtime",
                                            intent.getBooleanExtra("isAirtime", false)
                                        )
                                    }, 1
                                )
                            }

                        listView.invalidate()
                    }

                }
            })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 1 && resultCode == 1) {
            setResult(1)
            finish()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }
        }
        return false
    }

    private fun myObservable(): Observable<String> {
        return Observable.defer {
            val (result) = safeRun {
                val url =
                    "${BASE_URL}/api/PayBills/GetBillers?institutionCode=${localStorage.institutionCode}&billerCategoryID=${categoryIdField}"
                APICaller.makeGetRequest2(url)
            }
            Observable.just(result)
        }
    }

    companion object {
        private const val HOST = BuildConfig.API_HOST
        const val BASE_URL = "$HOST/CreditClubMiddlewareAPI"
    }
}
