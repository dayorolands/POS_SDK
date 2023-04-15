package com.dspread.qpos

import android.content.Intent
import android.os.Bundle
import androidx.activity.result.ActivityResult
import androidx.fragment.app.Fragment

/**
 * A simple abstract [Fragment] subclass.
 *
 */
abstract class BaseActivityResultManager : Fragment() {

    private val rationalRequest = mutableMapOf<Int, Boolean>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        onActivityResult(ActivityResult(resultCode, data))
    }

    protected fun getActivityResult(requestId: Int, intent: Intent) {

        rationalRequest[requestId]?.let {
            startActivityForResult(intent, requestId)
            rationalRequest.remove(requestId)
            return
        }

        startActivityForResult(intent, requestId)
    }

    protected abstract fun onActivityResult(activityResult: ActivityResult)
}
