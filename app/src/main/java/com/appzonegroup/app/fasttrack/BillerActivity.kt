package com.appzonegroup.app.fasttrack

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView
import com.appzonegroup.app.fasttrack.model.AppConstants
import com.appzonegroup.app.fasttrack.utility.BillsLocalStorage
import com.creditclub.core.data.api.BillsPaymentService
import com.creditclub.core.data.api.retrofitService
import com.creditclub.core.data.model.Biller
import com.creditclub.core.ui.CreditClubActivity
import com.creditclub.core.util.safeRunIO
import kotlinx.coroutines.launch
import java.util.*

class BillerActivity : CreditClubActivity(R.layout.activity_biller), View.OnClickListener {

    private var extras: Bundle? = null
    internal var categoryIdField: String? = null
    private var categoryNameField: String? = null
    private var propertyChanged: String? = null
    private val billsPaymentService: BillsPaymentService by retrofitService()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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

        mainScope.launch { runScheduler() }
    }

    override fun onClick(view: View) {
        mainScope.launch { runScheduler() }
    }


    private suspend fun runScheduler() {
        val listView = findViewById<ListView>(R.id.biller_listview)

        dialogProvider.showProgressBar("Loading billers")
        val (result, error) = safeRunIO {
            billsPaymentService.getBillers(localStorage.institutionCode, categoryIdField)
        }
        dialogProvider.hideProgressBar()

        if (error != null) dialogProvider.showError(error)
        if (result == null) return

        val billers = result.filter { b ->
            b.categoryId == categoryIdField || b.billerCategoryId?.toString() == categoryIdField
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

    private class BillerAdapter(private val activity: Activity, private val billers: List<Biller>) :
        ArrayAdapter<Biller?>(activity, 0, billers) {
        override fun getView(position: Int, view: View?, parent: ViewGroup): View {
            val itemView =
                view ?: activity.layoutInflater.inflate(R.layout.biller_report_item, parent, false)
            val billerName = billers[position].name
            itemView.findViewById<TextView>(R.id.biller_name_tv).text = billerName
            return itemView
        }
    }
}
