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
import com.creditclub.core.data.api.BillsPaymentService
import com.creditclub.core.data.api.retrofitService
import com.creditclub.core.data.model.BillPaymentItem
import com.creditclub.core.data.model.Biller
import com.creditclub.core.util.safeRunIO
import kotlinx.coroutines.launch
import java.util.*

class BillerItemActivity : BaseActivity(), View.OnClickListener {

    internal val listView: ListView by lazy { findViewById<ListView>(R.id.billerItem_listview) }
    internal val categoryIdField by lazy { intent.extras?.getString("categoryId") }
    internal val categoryNameField by lazy { intent.extras?.getString("categoryName") }
    internal val biller: Biller by lazy { intent.getParcelableExtra("biller")!!}
    private val billsPaymentService: BillsPaymentService by retrofitService()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_biller_item)

        title = biller.name
        mainScope.launch { runScheduler() }
    }

    override fun onClick(view: View) {
        mainScope.launch { runScheduler() }
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

    private suspend fun runScheduler() {
        dialogProvider.showProgressBar("Loading items")
        val (result, error) = safeRunIO {
            billsPaymentService.getPaymentItems(localStorage.institutionCode, biller.id)
        }
        dialogProvider.hideProgressBar()
        if (error != null) dialogProvider.showError(error)
        if (result == null) return
        val reports = result.filter { b -> "${b.billerId}" == biller.id }
        val billerAdapter = BillerItemAdapter(this@BillerItemActivity, reports)
        listView.adapter = billerAdapter

        listView.onItemClickListener =
            AdapterView.OnItemClickListener { _, _, position, _ ->
                val billerItem = reports[position]
                val intent =
                    Intent(this@BillerItemActivity, BillPaymentActivity::class.java).apply {
                        putExtra("customer", intent.getSerializableExtra("customer"))

                        billerItem.billerId = biller.id?.toIntOrNull()
                        putExtra("billeritem", billerItem)
                        putExtra("biller", biller)
                        putExtra("categoryId", categoryIdField)
                        putExtra("categoryName", categoryNameField)
                        putExtra("isAirtime", intent.getBooleanExtra("isAirtime", false))
                    }

                startActivityForResult(intent, 1)
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

    private class BillerItemAdapter(
        private val activity: Activity,
        private val billerItems: List<BillPaymentItem>
    ) :
        ArrayAdapter<BillPaymentItem?>(activity, 0, billerItems) {
        override fun getView(position: Int, view: View?, parent: ViewGroup): View {
            val itemView =
                view ?: activity.layoutInflater.inflate(R.layout.billeritem_report_item, parent, false)
            val billerItem = billerItems[position]
            itemView.findViewById<TextView>(R.id.billeritem_name_tv).text = billerItem.name
            return itemView
        }
    }
}
