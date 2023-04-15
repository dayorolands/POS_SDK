package com.dspread.qpos

import android.content.Intent
import androidx.activity.result.ActivityResult
import androidx.fragment.app.Fragment
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Permission manager which handles checking permission is granted or not and if not then will request permission.
 * A headless fragment which wraps the boilerplate code for checking and requesting permission
 * and suspends the coroutines until result is available.
 * A simple [Fragment] subclass.
 */
class ActivityResultManager : BaseActivityResultManager() {

    companion object {

        private const val TAG = "ActivityResultManager"

        /**
         * A static method to request permission from activity.
         *
         * @param activity an instance of [AppCompatActivity]
         * @param intent Intent
         * @param requestId Request ID for permission request
         *
         * @return [ActivityResult]
         *
         * Suspends the coroutines until result is available.
         */
        suspend fun getActivityResult(
            activity: AppCompatActivity,
            intent: Intent,
            requestId: Int,
        ): ActivityResult {
            return withContext(Dispatchers.Main) {
                return@withContext _getActivityResult(
                    activity,
                    intent,
                    requestId,
                )
            }
        }

        /**
         * A static method to request permission from fragment.
         *
         * @param fragment an instance of [Fragment]
         * @param intent Intent
         * @param requestId Request ID for permission request
         *
         * @return [ActivityResult]
         *
         * Suspends the coroutines until result is available.
         */
        suspend fun getActivityResult(
            fragment: Fragment,
            intent: Intent,
            requestId: Int,
        ): ActivityResult {
            return withContext(Dispatchers.Main) {
                return@withContext _getActivityResult(
                    fragment,
                    intent,
                    requestId,
                )
            }
        }

        private suspend fun _getActivityResult(
            activityOrFragment: Any,
            intent: Intent,
            requestId: Int,
        ): ActivityResult {
            val fragmentManager = if (activityOrFragment is AppCompatActivity) {
                activityOrFragment.supportFragmentManager
            } else {
                (activityOrFragment as Fragment).childFragmentManager
            }
            return if (fragmentManager.findFragmentByTag(TAG) != null) {
                val intentResultManager =
                    fragmentManager.findFragmentByTag(TAG) as ActivityResultManager
                intentResultManager.completableDeferred = CompletableDeferred()
                intentResultManager.getActivityResult(
                    requestId,
                    intent
                )
                intentResultManager.completableDeferred.await()
            } else {
                val intentResultManager = ActivityResultManager().apply {
                    completableDeferred = CompletableDeferred()
                }
                fragmentManager.beginTransaction().add(
                    intentResultManager,
                    TAG
                ).commitNow()
                intentResultManager.startActivityForResult(intent, requestId)
                intentResultManager.completableDeferred.await()
            }
        }
    }

    private lateinit var completableDeferred: CompletableDeferred<ActivityResult>

    override fun onActivityResult(activityResult: ActivityResult) {
        // When fragment gets recreated due to memory constraints by OS completableDeferred would be
        // uninitialized and hence check
        if (::completableDeferred.isInitialized) {
            completableDeferred.complete(activityResult)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::completableDeferred.isInitialized && completableDeferred.isActive) {
            completableDeferred.cancel()
        }
    }
}
