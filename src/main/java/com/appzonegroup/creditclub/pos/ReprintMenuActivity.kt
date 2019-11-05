package com.appzonegroup.creditclub.pos

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.databinding.DataBindingUtil
import com.appzonegroup.creditclub.pos.data.PosDatabase
import com.appzonegroup.creditclub.pos.databinding.ActivityReprintMenuBinding
import com.appzonegroup.creditclub.pos.util.MenuPage
import com.appzonegroup.creditclub.pos.util.MenuPages
import com.appzonegroup.creditclub.pos.widget.Dialogs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ReprintMenuActivity : PosActivity(), View.OnClickListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding: ActivityReprintMenuBinding = DataBindingUtil.setContentView(this, R.layout.activity_reprint_menu)

        binding.reprintAnyButton.button.setOnClickListener(this)
        binding.printEodsButton.button.setOnClickListener(this)
        binding.delAllTransactionsButton.button.setOnClickListener(this)
        binding.unsettledTransactionsButton.button.setOnClickListener(this)
    }

    fun goBack(view: View) {
        onBackPressed()
    }

    override fun onClick(v: View?) {
        v?.apply {
            when (id) {
                R.id.unsettled_transactions_button -> openPage(UnsettledTransactionsActivity::class.java)

                R.id.reprint_any_button -> startActivity(
                    Intent(
                        this@ReprintMenuActivity,
                        MenuActivity::class.java
                    ).apply {
                        putExtra(MenuPage.TITLE, MenuPages[MenuPages.REPRINT_ANY]?.name)
                        putExtra(MenuPage.PAGE_NUMBER, MenuPages.REPRINT_ANY)
                    })

                R.id.print_eods_button -> startActivity(
                    Intent(
                        this@ReprintMenuActivity,
                        MenuActivity::class.java
                    ).apply {
                        putExtra(MenuPage.TITLE, MenuPages[MenuPages.REPRINT_EODS]?.name)
                        putExtra(MenuPage.PAGE_NUMBER, MenuPages.REPRINT_EODS)
                    })

                R.id.del_all_transactions_button -> Dialogs.confirm(
                    this@ReprintMenuActivity,
                    "Delete",
                    "Are you sure? This cannot be undone"
                ) {
                    onSubmit {
                        PosDatabase.open(this@ReprintMenuActivity) { db->
                            withContext(Dispatchers.Default) {
                                db.financialTransactionDao().deleteAll()
                            }

                            dismiss()
                            showSuccess("All transactions deleted")
                        }
                    }
                }.show()
            }
        }
    }
}
