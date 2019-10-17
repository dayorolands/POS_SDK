package com.appzonegroup.app.fasttrack

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.core.view.GravityCompat
import com.appzonegroup.app.fasttrack.app.AppFunctions
import com.appzonegroup.app.fasttrack.databinding.ActivityCreditClubMainMenuBinding
import com.appzonegroup.app.fasttrack.model.AgentInfo
import com.appzonegroup.app.fasttrack.ui.TextView
import com.appzonegroup.app.fasttrack.utility.ActivityMisc
import com.appzonegroup.app.fasttrack.utility.Dialogs
import com.creditclub.core.util.delegates.contentView
import com.creditclub.core.util.localStorage
import com.creditclub.core.util.packageInfo
import com.google.android.material.navigation.NavigationView
import com.google.gson.Gson

class CreditClubMainMenuActivity : BaseActivity(), NavigationView.OnNavigationItemSelectedListener {

    private val binding by contentView<CreditClubMainMenuActivity, ActivityCreditClubMainMenuBinding>(
        R.layout.activity_credit_club_main_menu
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.agentCategoryButton.button.setOnClickListener(categoryClickListener(AppFunctions.Categories.AGENT_CATEGORY))
        binding.customerCategoryButton.button.setOnClickListener(categoryClickListener(AppFunctions.Categories.CUSTOMER_CATEGORY))
        binding.loanCategoryButton.button.setOnClickListener(categoryClickListener(AppFunctions.Categories.LOAN_CATEGORY))
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
        binding.logoutButton.setOnClickListener { logout() }

        localStorage.agentInfo?.run {
            val info = Gson().fromJson(this, AgentInfo::class.java)

            binding.navView.getHeaderView(0).run {
                findViewById<TextView>(R.id.username_tv).text = info.agentName
                findViewById<TextView>(R.id.phone_no_tv).text = info.phoneNumber
            }
        }
    }

    private fun logout() {
        startActivity(Intent(this@CreditClubMainMenuActivity, LoginActivity::class.java))
        finish()
    }

//    private fun openPlayStore() {
//        val appPackageName = packageInfo?.packageName
//
//        try {
//            startActivity(
//                Intent(
//                    Intent.ACTION_VIEW,
//                    Uri.parse("market://details?id=$appPackageName")
//                )
//            )
//        } catch (anfe: android.content.ActivityNotFoundException) {
//            startActivity(
//                Intent(
//                    Intent.ACTION_VIEW,
//                    Uri.parse("https://play.google.com/store/apps/details?id=$appPackageName")
//                )
//            )
//        }
//
//    }

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
        }

        return true
    }

    fun onRegisterClicked(v: View) {
        ActivityMisc.startActivity(
            this@CreditClubMainMenuActivity,
            CustomerRequestOpenAccountActivity::class.java
        )
    }

    fun onDepositClicked(v: View) {
        ActivityMisc.startActivity(this@CreditClubMainMenuActivity, DepositActivity::class.java)
    }

    fun onWithdrawalClicked(v: View) {
        startActivity(WithdrawActivity::class.java)
    }

    fun onAgentBalanceClicked(v: View) {
        val optionsDialog =
            Dialogs.getDialog(R.layout.dialog_balance_options, this@CreditClubMainMenuActivity)
        optionsDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        optionsDialog.setCanceledOnTouchOutside(true)
        optionsDialog.findViewById<View>(R.id.agent_balance).setOnClickListener {
            startActivity(BalanceEnquiryActivity::class.java)
            optionsDialog.dismiss()
        }

        optionsDialog.findViewById<View>(R.id.customer_balance).setOnClickListener {
            startActivity(AccountDetailsActivity::class.java)
            optionsDialog.dismiss()
        }

        optionsDialog.show()
    }

    fun onChangePinClicked(v: View) {
        val optionsDialog =
            Dialogs.getDialog(R.layout.dialog_balance_options, this@CreditClubMainMenuActivity)
        optionsDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        optionsDialog.setCanceledOnTouchOutside(true)
        optionsDialog.findViewById<View>(R.id.agent_balance)
            .setOnClickListener { startActivity(ChangePinActivity::class.java) }
        optionsDialog.findViewById<View>(R.id.customer_balance)
            .setOnClickListener { startActivity(ChangeCustomerPinActivity::class.java) }

        optionsDialog.show()
    }

    fun onLoanRequestClicked(v: View) {
        startActivity(CreditClubLoanRequestActivity::class.java)
    }

    fun onBVNUpdateClicked(v: View) {
        ActivityMisc.startActivity(this@CreditClubMainMenuActivity, BVNUpdateActivity::class.java)
    }

    fun onPayBillClicked(view: View) {
        showNotification(
            "This function is not available at the moment. Please look out for it in our next update.",
            false
        )
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
