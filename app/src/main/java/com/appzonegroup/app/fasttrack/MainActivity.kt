package com.appzonegroup.app.fasttrack

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.activity.viewModels
import com.appzonegroup.app.fasttrack.utility.AppTimer
import com.creditclub.core.ui.CreditClubActivity
import com.creditclub.core.ui.CreditClubFragment
import com.creditclub.viewmodel.AppViewModel


class MainActivity : CreditClubActivity(R.layout.activity_main) {
    private val appViewModel: AppViewModel by viewModels()
    private val logoutTimer = AppTimer {
        appViewModel.sessionTimedOut.postValue(true)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        logoutTimer.restart()
    }

    override fun onUserInteraction() {
        super.onUserInteraction()
        if (appViewModel.sessionTimedOut.value != true) {
            logoutTimer.restart()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onDestroy() {
        logoutTimer.stop()
        super.onDestroy()
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