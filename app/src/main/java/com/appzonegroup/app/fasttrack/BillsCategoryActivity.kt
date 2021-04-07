package com.appzonegroup.app.fasttrack

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.appzonegroup.app.fasttrack.model.AppConstants
import com.appzonegroup.app.fasttrack.utility.BillsLocalStorage
import com.appzonegroup.app.fasttrack.utility.FunctionIds
import com.creditclub.core.data.api.BillsPaymentService
import com.creditclub.core.data.api.retrofitService
import com.creditclub.core.data.model.BillCategory
import com.creditclub.core.ui.CreditClubActivity
import com.creditclub.core.util.safeRunIO
import kotlinx.coroutines.launch
import java.util.*

class BillsCategoryActivity : CreditClubActivity(R.layout.activity_category), View.OnClickListener {
    internal val listView: ListView by lazy { findViewById<View>(R.id.category_listview) as ListView }
    private var categoryAdapter: CategoryAdapter? = null
    override val functionId = FunctionIds.PAY_BILL
    private val billsPaymentService: BillsPaymentService by retrofitService()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mainScope.launch { runScheduler() }
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
        mainScope.launch { runScheduler() }
    }

    private suspend fun runScheduler() {
        dialogProvider.showProgressBar("Loading categories")
        val (result, error) = safeRunIO {
            billsPaymentService.getBillerCategories(localStorage.institutionCode)
        }
        dialogProvider.hideProgressBar()
        if (error != null) dialogProvider.showError(error)
        if (result == null) return
        categoryAdapter = CategoryAdapter(this@BillsCategoryActivity, result)

        listView.adapter = categoryAdapter
        listView.onItemClickListener =
            AdapterView.OnItemClickListener { _, _, position, _ ->
                val billCategory = result[position]

                val i = Intent(
                    this@BillsCategoryActivity,
                    BillerActivity::class.java
                ).apply {
                    putExtra("categoryId", billCategory.id ?: billCategory.id)
                    putExtra("categoryName", billCategory.name)
                    putExtra("customer", intent.getSerializableExtra("customer"))
                    putExtra("isAirtime", billCategory.isAirtime)
                }

                startActivityForResult(i, 1)
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

    private class CategoryAdapter(var activity: Activity, var billCategories: List<BillCategory>) :
        ArrayAdapter<BillCategory?>(activity, 0, billCategories) {
        private var originalData: ArrayList<BillCategory> = ArrayList(billCategories)
        override fun getView(position: Int, view: View?, parent: ViewGroup): View {
            val itemView = view ?: activity.layoutInflater.inflate(
                R.layout.category_report_item,
                parent,
                false
            )
            val billCategory = billCategories[position]
            itemView.findViewById<TextView>(R.id.category_name_tv).text = billCategory.name
            itemView.findViewById<TextView>(R.id.category_desc_tv).text = billCategory.description
            return itemView
        }

        override fun getFilter(): Filter = object : Filter() {
            override fun performFiltering(constraint: CharSequence): FilterResults {
                val constraintString =
                    constraint.toString().trim { it <= ' ' }.toLowerCase(Locale.getDefault())
                val result = FilterResults()
                if (constraintString.isEmpty()) {
                    result.values = originalData
                    result.count = originalData.size
                    return result
                }

                val founded = ArrayList<BillCategory>()
                for (item in billCategories) {
                    if (
                        item.name.toString().toLowerCase(Locale.ROOT).contains(constraintString)
                    ) {
                        founded.add(item)
                    }
                }
                result.values = founded
                result.count = founded.size
                return result
            }

            override fun publishResults(constraint: CharSequence, results: FilterResults) {
                clear()
                for (item in results.values as ArrayList<BillCategory?>) {
                    add(item)
                }
                notifyDataSetChanged()
            }
        }
    }
}
