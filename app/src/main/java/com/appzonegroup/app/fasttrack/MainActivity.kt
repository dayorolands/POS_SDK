package com.appzonegroup.app.fasttrack

import android.view.MenuItem
import com.creditclub.core.ui.CreditClubActivity

class MainActivity : CreditClubActivity(R.layout.activity_main) {
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}