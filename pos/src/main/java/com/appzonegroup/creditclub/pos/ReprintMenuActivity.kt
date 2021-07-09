package com.appzonegroup.creditclub.pos

import android.content.Intent
import android.os.Bundle
import com.appzonegroup.creditclub.pos.data.PosDatabase
import com.appzonegroup.creditclub.pos.databinding.ActivityReprintMenuBinding
import com.appzonegroup.creditclub.pos.util.MenuPage
import com.appzonegroup.creditclub.pos.util.MenuPages
import com.appzonegroup.creditclub.pos.widget.Dialogs
import com.creditclub.ui.dataBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ReprintMenuActivity : PosActivity(R.layout.activity_reprint_menu) {
    private val binding: ActivityReprintMenuBinding by dataBinding()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.reprintAnyButton.button.setOnClickListener {
            startActivity(
                Intent(
                    this@ReprintMenuActivity,
                    MenuActivity::class.java
                ).apply {
                    putExtra(MenuPage.TITLE, MenuPages[MenuPages.REPRINT_ANY]?.name)
                    putExtra(MenuPage.PAGE_NUMBER, MenuPages.REPRINT_ANY)
                })
        }

        binding.printEodsButton.button.setOnClickListener {
            startActivity(
                Intent(
                    this@ReprintMenuActivity,
                    MenuActivity::class.java
                ).apply {
                    putExtra(MenuPage.TITLE, MenuPages[MenuPages.REPRINT_EODS]?.name)
                    putExtra(MenuPage.PAGE_NUMBER, MenuPages.REPRINT_EODS)
                })
        }

        binding.delAllTransactionsButton.button.setOnClickListener {
            Dialogs.confirm(
                this@ReprintMenuActivity,
                "Delete",
                "Are you sure? This cannot be undone"
            ) {
                onSubmit {
                    PosDatabase.open(this@ReprintMenuActivity) { db ->
                        withContext(Dispatchers.Default) {
                            db.financialTransactionDao().deleteAll()
                        }

                        dismiss()
                        dialogProvider.showSuccess("All transactions deleted")
                    }
                }
            }.show()
        }

        binding.unsettledTransactionsButton.button.setOnClickListener {
            startActivity(Intent(this, UnsettledTransactionsActivity::class.java))
        }
    }
}
