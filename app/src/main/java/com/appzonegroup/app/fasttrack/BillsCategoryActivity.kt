package com.appzonegroup.app.fasttrack

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.Menu
import android.view.View
import android.widget.AdapterView
import android.widget.ListView
import android.widget.SearchView
import android.widget.Toast
import com.appzonegroup.app.fasttrack.adapter.CategoryAdapter
import com.appzonegroup.app.fasttrack.model.AppConstants
import com.appzonegroup.app.fasttrack.model.BillCategory
import com.appzonegroup.app.fasttrack.network.APICaller
import com.appzonegroup.app.fasttrack.scheduler.AndroidSchedulers
import com.appzonegroup.app.fasttrack.scheduler.HandlerScheduler
import com.appzonegroup.app.fasttrack.utility.LocalStorage
import com.appzonegroup.app.fasttrack.utility.Misc
import com.crashlytics.android.Crashlytics
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import rx.Observable
import rx.Subscriber
import rx.functions.Func0
import java.util.*

/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 4/12/2019.
 * Appzone Ltd
 */

class BillsCategoryActivity : BaseActivity(), View.OnClickListener {
    internal val backgroundHandler: Handler by lazy { Misc.setupScheduler() }
    internal val listView: ListView by lazy { findViewById<View>(R.id.category_listview) as ListView }
    internal var categoryAdapter: CategoryAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_category)

        showProgressBar("Loading categories")
        runScheduler()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {

        val inflater = menuInflater
        inflater.inflate(R.menu.menu_search, menu)
        val item = menu.findItem(R.id.search)
        val searchView = item.actionView as SearchView

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                categoryAdapter?.filter?.filter(newText)

                return false
            }
        })


        return super.onCreateOptionsMenu(menu)
    }

    override fun onClick(view: View) {
        showProgressBar("Loading categories")
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
                    hideProgressBar()
                    Toast.makeText(
                        baseContext,
                        "An error just occurred. Please try again later",
                        Toast.LENGTH_LONG
                    ).show()

                    Crashlytics.logException(Exception(e.message))
                }

                override fun onNext(result: String) {
                    var result = result

                    hideProgressBar()

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


                        val typeToken = object : TypeToken<ArrayList<BillCategory>>() {

                        }
                        val reports =
                            Gson().fromJson<ArrayList<BillCategory>>(result, typeToken.type)
                        categoryAdapter = CategoryAdapter(this@BillsCategoryActivity, reports)

                        listView.adapter = categoryAdapter
                        listView.onItemClickListener =
                            AdapterView.OnItemClickListener { parent, view, position, id ->
                                val billCategory = reports[position]

                                val i = Intent(
                                    this@BillsCategoryActivity,
                                    BillerActivity::class.java
                                ).apply {
                                    putExtra("categoryId", billCategory.id)
                                    LocalStorage.SaveValue(
                                        AppConstants.CATEGORYID,
                                        billCategory.id,
                                        baseContext
                                    )

                                    putExtra("categoryName", billCategory.name)
                                    LocalStorage.SaveValue(
                                        AppConstants.CATEGORYNAME,
                                        billCategory.name,
                                        baseContext
                                    )

                                    putExtra("propertyChanged", billCategory.propertyChanged)
                                    LocalStorage.SaveValue(
                                        AppConstants.PROPERTYCHANGED,
                                        billCategory.propertyChanged,
                                        baseContext
                                    )

                                    putExtra("customer", intent.getSerializableExtra("customer"))
                                    putExtra("isAirtime", billCategory.isAirtime)
                                }

                                startActivityForResult(i, 1)
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
                val url =
                    Misc.getCategoryURL() + "?institutionCode=" + LocalStorage.getInstitutionCode(
                        this
                    )
                result = APICaller.makeGetRequest2(url)

            } catch (e: Exception) {
                Log.e("Register", e.message)

                Crashlytics.logException(Exception(e.message))
            }

            Observable.just(result)
        })
    }

}
