package com.appzonegroup.app.fasttrack

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ListView
import android.widget.Toast

import com.appzonegroup.app.fasttrack.adapter.BillerItemAdapter
import com.appzonegroup.app.fasttrack.model.Biller
import com.appzonegroup.app.fasttrack.model.BillerItem
import com.appzonegroup.app.fasttrack.network.APICaller
import com.appzonegroup.app.fasttrack.scheduler.AndroidSchedulers
import com.appzonegroup.app.fasttrack.scheduler.HandlerScheduler
import com.appzonegroup.app.fasttrack.ui.Dialogs
import com.appzonegroup.app.fasttrack.utility.LocalStorage
import com.appzonegroup.app.fasttrack.utility.Misc
import com.crashlytics.android.Crashlytics
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

import java.util.ArrayList

import rx.Observable
import rx.Subscriber
import rx.functions.Func0

/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 4/12/2019.
 * Appzone Ltd
 */

class BillerItemActivity : BaseActivity(), View.OnClickListener {

    internal val backgroundHandler: Handler by lazy { Misc.setupScheduler() }
    internal val loadingDialog: Dialog by lazy { Dialogs.getProgress(this, "Loading items") }
    internal val listView: ListView by lazy { findViewById<ListView>(R.id.billerItem_listview) }
    internal val categoryIdField by lazy { intent.extras?.getString("categoryId") }
    internal val categoryNameField by lazy { intent.extras?.getString("categoryName") }
    internal val biller: Biller by lazy { intent.getSerializableExtra("biller") as Biller }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_biller_item)

        title = biller.billerNameField
        loadingDialog.show()
        runScheduler()
    }

    override fun onClick(view: View) {
        loadingDialog.show()
        runScheduler()
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
                        if (loadingDialog.isShowing) {
                            loadingDialog.dismiss()
                        }
                        Toast.makeText(baseContext, e.toString(), Toast.LENGTH_LONG).show()
                        Crashlytics.logException(Exception("An error occurred. Please try again later"))
                    }

                    override fun onNext(result: String?) {
                        var result = result

                        if (loadingDialog.isShowing)
                            loadingDialog.dismiss()

                        if (result == null || result.isEmpty()) {
                            Toast.makeText(baseContext, "You don't seem to have internet... Please ensure that you have internet connection", Toast.LENGTH_LONG).show()
                            //return;
                        } else {

                            result = result.replace("\\", "").replace("\n", "").trim { it <= ' ' }
                            if (result.startsWith("\"") && result.endsWith("\"")) {
                                result = result.substring(1, result.length - 1)
                            }


                            val typeToken = object : TypeToken<ArrayList<BillerItem>>() {

                            }
                            val reports = Gson().fromJson<ArrayList<BillerItem>>(result, typeToken.type).filter { b -> b.merchantBillerIdField == biller.billerID }
                            val billerAdapter = BillerItemAdapter(this@BillerItemActivity, reports)
                            listView.adapter = billerAdapter

                            listView.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
                                //                                getCustomer {
//                                    onSubmit { customer ->
                                val billerItem = reports[position]
                                val intent = Intent(this@BillerItemActivity, BillPaymentActivity::class.java).apply {
                                    putExtra("customer", intent.getSerializableExtra("customer"))

                                    billerItem.merchantBillerIdField = biller.billerID
                                    putExtra("billeritem", billerItem)
                                    putExtra("biller", biller)
                                    putExtra("categoryId", categoryIdField)
                                    putExtra("categoryName", categoryNameField)
                                    putExtra("isAirtime", intent.getBooleanExtra("isAirtime", false))
                                }

                                startActivityForResult(intent, 1)
//                                    }
//
//                                    onClose {
//
//                                    }
//                            }
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

    internal fun myObservable(): Observable<String> {
        return Observable.defer(Func0<Observable<String>> {
            var result: String? = ""
            try {

                val url = Misc.getBillerItemURL(biller.billerID.toString()) + "?institutionCode=" + LocalStorage.getInstitutionCode(this)
                result = APICaller.makeGetRequest2(url)

            } catch (e: Exception) {
                Log.e("Register", e.message)
                Crashlytics.logException(Exception(e.message))
            }

            Observable.just(result)
        })
    }
}
