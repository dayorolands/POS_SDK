package com.appzonegroup.creditclub.pos

import android.os.Bundle
import android.view.View
import androidx.databinding.DataBindingUtil
import com.appzonegroup.creditclub.pos.databinding.ActivityReprintAnyBinding

class ReprintAnyActivity : PosActivity(), View.OnClickListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding: ActivityReprintAnyBinding = DataBindingUtil.setContentView(this, R.layout.activity_reprint_any)

        binding.reprintLastButton.button.setOnClickListener(this)
        binding.reprintByStanButton.button.setOnClickListener(this)
    }

    fun goBack(view: View) {
        onBackPressed()
    }

    override fun onClick(v: View?) {
        v?.apply {
            when (id) {
                R.id.unsettled_transactions_button -> adminAction {
                    startActivity(UnsettledTransactionsActivity::class.java)
                }
            }
        }
    }
}
