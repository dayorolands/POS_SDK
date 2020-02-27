package com.appzonegroup.app.fasttrack

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.core.view.GravityCompat
import com.appzonegroup.app.fasttrack.databinding.ActivityCreditClubMainMenuBinding
import com.appzonegroup.app.fasttrack.ui.Dialogs
import com.appzonegroup.app.fasttrack.utility.logout
import com.appzonegroup.app.fasttrack.utility.openPageById
import com.appzonegroup.creditclub.pos.Platform
import com.creditclub.core.AppFunctions
import com.creditclub.core.util.appDataStorage
import com.creditclub.core.util.delegates.contentView
import com.creditclub.core.util.localStorage
import com.creditclub.core.util.packageInfo
import com.creditclub.core.util.safeRunIO
import com.creditclub.ui.UpdateActivity
import com.google.android.material.navigation.NavigationView
import kotlinx.coroutines.launch

class CreditClubMainMenuActivity : BaseActivity(), NavigationView.OnNavigationItemSelectedListener {

    private val binding by contentView<CreditClubMainMenuActivity, ActivityCreditClubMainMenuBinding>(
        R.layout.activity_credit_club_main_menu
    )

    private val frequentBindings by lazy {
        listOf(binding.frequent.fn1, binding.frequent.fn2, binding.frequent.fn3)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.agentCategoryButton.button.setOnClickListener(categoryClickListener(AppFunctions.Categories.AGENT_CATEGORY))
        binding.customerCategoryButton.button.setOnClickListener(categoryClickListener(AppFunctions.Categories.CUSTOMER_CATEGORY))
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

        val environment = if (BuildConfig.DEBUG) ". Staging" else ""

        binding.versionTv.value = "v${packageInfo?.versionName}$environment. Powered by CreditClub"
        binding.logoutButton.setOnClickListener { logout() }

        localStorage.agent?.let { info ->
            binding.navView.getHeaderView(0).run {
                findViewById<TextView>(R.id.username_tv).text = info.agentName
                findViewById<TextView>(R.id.phone_no_tv).text = info.phoneNumber
            }
        }

        getFavorites()

        binding.navView.menu.getItem(0).run {
            isVisible = institutionConfig.hasOnlineFunctions
        }

        val hasPosUpdateManager = Platform.isPOS

        binding.navView.menu.findItem(R.id.fn_update)?.isVisible = hasPosUpdateManager

        binding.navView.menu.findItem(R.id.fn_hla_tagging)?.run {
            isVisible = institutionConfig.hasHlaTagging
        }

        if (hasPosUpdateManager) {
            appDataStorage.latestVersion?.run {
                val currentVersion = packageInfo?.versionName
                if (currentVersion != null) {
                    if (updateIsAvailable(currentVersion)) {
                        val updateIsRequired = updateIsRequired(currentVersion)
                        val mustUpdate = updateIsRequired && daysOfGraceLeft() < 1
                        val message = "A new version (v$version) is available."
                        val subtitle =
                            if (updateIsRequired && mustUpdate) "You need to update now"
                            else if (updateIsRequired) "Please update with ${daysOfGraceLeft()} days"
                            else "Please update"

                        val dialog =
                            Dialogs.confirm(this@CreditClubMainMenuActivity, message, subtitle) {
                                onSubmit {
                                    if (it) startActivity(UpdateActivity::class.java)
                                    else if (mustUpdate) finish()
                                }

                                onClose {
                                    if (mustUpdate) finish()
                                }
                            }
                        dialog.setCancelable(!mustUpdate)
                        dialog.setCanceledOnTouchOutside(false)
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        getFavorites()
    }

    private fun getFavorites() {
        binding.frequent.root.visibility = View.GONE
        frequentBindings.forEach { it.root.visibility = View.GONE }

        mainScope.launch {
            val (list) = safeRunIO {
                coreDatabase.appFunctionUsageDao().getMostUsed()
            }

            if (list == null || list.isEmpty()) {
                return@launch
            }

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
    }

    override fun onBackPressed() {
        try {
            if (binding.creditClubMainMenuCoordinator.isDrawerOpen(GravityCompat.START)) {
                binding.creditClubMainMenuCoordinator.closeDrawer(GravityCompat.START)
            } else {
                super.onBackPressed()
            }
        } catch (ex: Exception) {
            finish()
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.logout -> logout()
            R.id.online_functions -> startActivity(
                Intent(
                    this@CreditClubMainMenuActivity,
                    OnlineActivity::class.java
                )
            )
            R.id.reports -> startActivity(
                Intent(
                    this@CreditClubMainMenuActivity,
                    ReportActivity::class.java
                )
            )
            R.id.commissions -> startActivity(
                Intent(
                    this@CreditClubMainMenuActivity,
                    CommissionsActivity::class.java
                )
            )
            R.id.support -> startActivity(
                Intent(
                    this@CreditClubMainMenuActivity,
                    SupportActivity::class.java
                )
            )
            R.id.fn_update -> startActivity(UpdateActivity::class.java)
            R.id.fn_hla_tagging -> startActivity(HlaTaggingActivity::class.java)
            R.id.fn_faq -> startActivity(FaqActivity::class.java)
        }

        return true
    }

    fun openDrawer(view: View) {
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
            val intent =
                Intent(this@CreditClubMainMenuActivity, CreditClubSubMenuActivity::class.java)
            intent.putExtra(CreditClubSubMenuActivity.CATEGORY_TYPE, category)
            startActivity(intent)
        }
    }
}
