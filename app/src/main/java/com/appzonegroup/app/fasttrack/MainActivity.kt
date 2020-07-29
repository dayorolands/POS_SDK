package com.appzonegroup.app.fasttrack

import android.view.MenuItem
import android.view.View
import com.creditclub.core.ui.CreditClubActivity
import com.creditclub.core.ui.CreditClubFragment


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

    fun onBackPressed(v: View?) {
        onBackPressed()
    }

    fun goBack(v: View?) {
        onBackPressed()
    }

    override fun onBackPressed() {
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment)
        val fragment = navHostFragment?.childFragmentManager?.fragments?.get(0)

        if (fragment == null || fragment !is CreditClubFragment || !fragment.onBackPressed()) super.onBackPressed()
    }
}