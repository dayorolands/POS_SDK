package com.appzonegroup.app.fasttrack

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.recyclerview.widget.LinearLayoutManager
import com.appzonegroup.app.fasttrack.databinding.ActivitySupportBinding
import com.appzonegroup.app.fasttrack.databinding.ItemCaseBinding
import com.appzonegroup.app.fasttrack.utility.FunctionIds
import com.creditclub.core.data.model.CaseDetail
import com.creditclub.core.data.request.CaseDetailsRequest
import com.creditclub.core.ui.SimpleBindingAdapter
import com.creditclub.core.util.*
import com.creditclub.core.util.delegates.contentView
import kotlinx.coroutines.launch

class SupportActivity : BaseActivity() {

    private val binding by contentView<SupportActivity, ActivitySupportBinding>(R.layout.activity_support)
    private val adapter = Adapter(emptyList())
    override val functionId = FunctionIds.SUPPORT

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.container.layoutManager = LinearLayoutManager(this)
        binding.container.adapter = adapter

        binding.fab.setOnClickListener { startActivity(CaseLogActivity::class.java) }

        loadData()
    }

    override fun onResume() {
        super.onResume()
        loadData()
    }

    private fun loadData() {

        mainScope.launch {
            showProgressBar("Loading Cases")

            val request = CaseDetailsRequest(
                agentPhoneNumber = localStorage.agentPhone,
                institutionCode = localStorage.institutionCode
            )

            val (response, error) = safeRunIO {
                creditClubMiddleWareAPI.caseLogService.caseDetails(request)
            }

            hideProgressBar()

            if (error != null) return@launch showError(error)

            adapter.setData(response?.response)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    private inner class Adapter(override var values: List<CaseDetail>) :
        SimpleBindingAdapter<CaseDetail, ItemCaseBinding>(R.layout.item_case) {

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.binding.name.text = values[position].subject
            holder.binding.description.text = values[position].description
            holder.binding.timeTv.text = values[position].dateLogged?.toInstant(CREDIT_CLUB_DATE_PATTERN)?.timeAgo()
            holder.binding.referenceTv.text = values[position].caseReference

            holder.binding.root.setOnClickListener {
                startActivity(Intent(this@SupportActivity, SupportThreadActivity::class.java).apply {
                    putExtra("REFERENCE", values[position].caseReference)
                    putExtra("TITLE", values[position].subject)
                })
            }
        }
    }
}
