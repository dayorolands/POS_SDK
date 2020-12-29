package com.appzonegroup.app.fasttrack.fragment

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.core.view.GravityCompat
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.appzonegroup.app.fasttrack.*
import com.appzonegroup.app.fasttrack.databinding.HomeFragmentBinding
import com.appzonegroup.app.fasttrack.ui.Dialogs
import com.appzonegroup.app.fasttrack.ui.dataBinding
import com.appzonegroup.app.fasttrack.utility.logout
import com.appzonegroup.app.fasttrack.utility.openPageById
import com.appzonegroup.creditclub.pos.Platform
import com.creditclub.core.AppFunctions
import com.creditclub.core.data.api.NotificationService
import com.creditclub.core.data.model.NotificationRequest
import com.creditclub.core.ui.CreditClubFragment
import com.creditclub.core.ui.widget.DialogConfirmParams
import com.creditclub.core.util.debugOnly
import com.creditclub.core.util.delegates.service
import com.creditclub.core.util.packageInfo
import com.creditclub.core.util.safeRunIO
import com.creditclub.ui.UpdateActivity
import com.google.android.material.navigation.NavigationView
import kotlinx.coroutines.launch


class HomeFragment : CreditClubFragment(R.layout.home_fragment),
    NavigationView.OnNavigationItemSelectedListener {

    private val packageInfo get() = requireContext().packageInfo
    private val binding by dataBinding<HomeFragmentBinding>()
    private val notificationViewModel by activityViewModels<NotificationViewModel>()
    private val notificationService by creditClubMiddleWareAPI.retrofit.service<NotificationService>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.agentCategoryButton.button.setOnClickListener(categoryClickListener(AppFunctions.Categories.AGENT_CATEGORY))
        binding.customerCategoryButton.run {
            if (institutionConfig.categories.customers) {
                root.visibility = View.VISIBLE
                button.setOnClickListener(categoryClickListener(AppFunctions.Categories.CUSTOMER_CATEGORY))
            } else {
                root.visibility = View.GONE
            }
        }
        binding.loanCategoryButton.run {
            if (institutionConfig.categories.loans) {
                root.visibility = View.VISIBLE
                button.setOnClickListener(categoryClickListener(AppFunctions.Categories.LOAN_CATEGORY))
            } else {
                root.visibility = View.GONE
            }
        }
        binding.transactionsCategoryButton.button.setOnClickListener(
            categoryClickListener(
                AppFunctions.Categories.TRANSACTIONS_CATEGORY
            )
        )

        if (binding.creditClubMainMenuCoordinator.isDrawerOpen(GravityCompat.START)) {
            binding.creditClubMainMenuCoordinator.closeDrawer(GravityCompat.START)
        }

        binding.navView.setNavigationItemSelectedListener(this)

        binding.versionTv.value = "v${packageInfo?.versionName}. Powered by CreditClub"
        debugOnly {
            binding.versionTv.value = "v${packageInfo?.versionName}. Staging. Powered by CreditClub"
        }

        binding.logoutButton.setOnClickListener { requireActivity().logout() }

        localStorage.agent?.let { info ->
            binding.navView.getHeaderView(0).run {
                findViewById<TextView>(R.id.username_tv).text = info.agentName
                findViewById<TextView>(R.id.phone_no_tv).text = info.phoneNumber
            }
        }

        mainScope.launch { getFavorites() }
        mainScope.launch { getNotifications() }

        binding.navView.menu.getItem(0).run {
            isVisible = institutionConfig.hasOnlineFunctions
        }

        val hasPosUpdateManager = Platform.isPOS

        binding.navView.menu.findItem(R.id.fn_update)?.isVisible = hasPosUpdateManager

        binding.navView.menu.findItem(R.id.fn_hla_tagging)?.run {
            isVisible = institutionConfig.hasHlaTagging
        }

        if (hasPosUpdateManager) checkForUpdate()

        binding.drawerToggle.setOnClickListener { openDrawer() }
        binding.notificationBtn.setOnClickListener {
            findNavController().navigate(HomeFragmentDirections.homeToNotifications())
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
//        notificationViewModel.notificationList.watch {
//            if (it != null && it.isNotEmpty()) {
//                val notification=it.first()
//                val snackbar = Snackbar.make(
//                    binding.frequent.root,
//                    notification.message ?: "",
//                    Snackbar.LENGTH_INDEFINITE
//                )
//                snackbar.setAction("Close") {
//                    snackbar.dismiss()
//                    mainScope.launch {
//                        safeRunIO {
//                            notificationService.markAsRead(
//                                localStorage.agentPhone,
//                                localStorage.institutionCode,
//                                notification.reference
//                            )
//                        }
//                        getNotifications()
//                    }
//                }
//                snackbar.show()
//            }
//        }
    }

    private suspend fun getNotifications() {
        val (response, error) = safeRunIO {
            notificationService.getNotifications(
                NotificationRequest(
                    localStorage.agentPhone,
                    localStorage.institutionCode,
                    20,
                    0
                )
            )
        }

        if (response != null) notificationViewModel.notificationList.value = response.response
    }

    private fun checkForUpdate() = appDataStorage.latestVersion?.run {
        val currentVersion = packageInfo?.versionName
        if (currentVersion != null && updateIsAvailable(currentVersion)) {
            val updateIsRequired = updateIsRequired(currentVersion)
            val mustUpdate = updateIsRequired && daysOfGraceLeft() < 1
            val message = "A new version (v$version) is available."
            val subtitle =
                if (updateIsRequired && mustUpdate) "You need to update now"
                else if (updateIsRequired) "Please update with ${daysOfGraceLeft()} days"
                else "Please update"

            dialogProvider.confirm(DialogConfirmParams(message, subtitle)) {
                onSubmit {
                    if (it) {
                        startActivity(
                            Intent(
                                requireContext(),
                                UpdateActivity::class.java
                            )
                        )
                        requireActivity().finish()
                    } else if (mustUpdate) requireActivity().finish()
                }

                onClose {
                    if (mustUpdate) requireActivity().finish()
                }
            }
        }
    }

    private suspend fun getFavorites() {
        val frequentBindings =
            listOf(binding.frequent.fn1, binding.frequent.fn2, binding.frequent.fn3)

        binding.frequent.root.visibility = View.GONE
        frequentBindings.forEach { it.root.visibility = View.GONE }

        val (list) = safeRunIO {
            coreDatabase.appFunctionUsageDao().getMostUsed()
        }

        if (list == null || list.isEmpty()) return

        binding.frequent.root.visibility = View.VISIBLE

        for (appFunctionUsage in list) {

            AppFunctions[appFunctionUsage.fid]?.run {
                val index = list.indexOf(appFunctionUsage)

                frequentBindings[index].root.visibility = View.VISIBLE
                if (icon != null) frequentBindings[index].iconIv.setImageResource(icon!!)
                frequentBindings[index].nameTv.setText(label)
                frequentBindings[index].root.setOnClickListener {
                    openPageById(id)
                }
            }
        }
    }

    override fun onBackPressed(): Boolean {
        try {
            return if (binding.creditClubMainMenuCoordinator.isDrawerOpen(GravityCompat.START)) {
                binding.creditClubMainMenuCoordinator.closeDrawer(GravityCompat.START)
                true
            } else {
                super.onBackPressed()
            }
        } catch (ex: Exception) {
//            requireActivity().finish()
        }

        return super.onBackPressed()
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.logout -> requireActivity().logout()
            R.id.online_functions -> startActivity(
                Intent(
                    requireContext(),
                    OnlineActivity::class.java
                )
            )
            R.id.reports -> startActivity(
                Intent(
                    requireContext(),
                    ReportActivity::class.java
                )
            )
            R.id.commissions -> startActivity(
                Intent(
                    requireContext(),
                    CommissionsActivity::class.java
                )
            )
            R.id.support -> startActivity(
                Intent(
                    requireContext(),
                    SupportActivity::class.java
                )
            )
            R.id.fn_update -> startActivity(UpdateActivity::class.java)
            R.id.fn_hla_tagging -> startActivity(HlaTaggingActivity::class.java)
            R.id.fn_faq -> startActivity(FaqActivity::class.java)
        }

        return true
    }

    private inline fun <T> MutableLiveData<T>.watch(crossinline block: (T?) -> Unit) {
        block(value)
        var oldValue = value
        observe(viewLifecycleOwner, Observer {
            if (value != oldValue) {
                oldValue = value
                block(it)
            }
        })
    }

    private fun openDrawer() {
        binding.creditClubMainMenuCoordinator.run {
            if (isDrawerOpen(GravityCompat.START)) {
                closeDrawer(GravityCompat.START)
            } else {
                openDrawer(GravityCompat.START)
            }
        }
    }

    private fun categoryClickListener(category: Int): View.OnClickListener? {
        return View.OnClickListener {
            findNavController().navigate(HomeFragmentDirections.actionHomeToSubMenu(category))
        }
    }
}
