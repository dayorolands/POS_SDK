package com.cluster.pos

import android.content.Intent
import android.os.Bundle
import com.cluster.pos.data.PosDatabase
import com.cluster.pos.databinding.ActivityReprintMenuBinding
import com.cluster.pos.util.MenuPage
import com.cluster.pos.util.MenuPages
import com.cluster.pos.widget.Dialogs
import com.cluster.ui.dataBinding
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

//        binding.unsettledTransactionsButton.button.setOnClickListener {
//            startActivity(Intent(this, UnsettledTransactionsActivity::class.java))
//        }
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.unbind()
    }
}
