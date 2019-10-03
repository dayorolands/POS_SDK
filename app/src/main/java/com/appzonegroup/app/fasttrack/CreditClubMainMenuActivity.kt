package com.appzonegroup.app.fasttrack

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Button
import androidx.core.view.GravityCompat
import androidx.databinding.DataBindingUtil
import androidx.drawerlayout.widget.DrawerLayout
import com.appzonegroup.app.fasttrack.app.AppFunctions
import com.appzonegroup.app.fasttrack.databinding.ActivityCreditClubMainMenuBinding
import com.appzonegroup.app.fasttrack.model.AgentInfo
import com.appzonegroup.app.fasttrack.model.AppConstants
import com.appzonegroup.app.fasttrack.ui.TextView
import com.appzonegroup.app.fasttrack.utility.ActivityMisc
import com.appzonegroup.app.fasttrack.utility.Dialogs
import com.appzonegroup.app.fasttrack.utility.LocalStorage
import com.appzonegroup.app.fasttrack.utility.Misc
import com.creditclub.core.util.localStorage
import com.creditclub.core.util.packageInfo
import com.google.android.material.navigation.NavigationView
import com.google.gson.Gson

class CreditClubMainMenuActivity : BaseActivity(), NavigationView.OnNavigationItemSelectedListener {
    private var updateChecked: Boolean = false
    private var shouldForceUpdate: Boolean = false
    private lateinit var mDrawerLayout: DrawerLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding: ActivityCreditClubMainMenuBinding =
            DataBindingUtil.setContentView(this, R.layout.activity_credit_club_main_menu)

        binding.agentCategoryButton.button.setOnClickListener(categoryClickListener(AppFunctions.Categories.AGENT_CATEGORY))
        binding.customerCategoryButton.button.setOnClickListener(categoryClickListener(AppFunctions.Categories.CUSTOMER_CATEGORY))
        binding.loanCategoryButton.button.setOnClickListener(categoryClickListener(AppFunctions.Categories.LOAN_CATEGORY))
        binding.transactionsCategoryButton.button.setOnClickListener(
            categoryClickListener(
                AppFunctions.Categories.TRANSACTIONS_CATEGORY
            )
        )

        mDrawerLayout = findViewById(R.id.credit_club_main_menu_coordinator)
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START)
        }

        val navigationView = findViewById<NavigationView>(R.id.nav_view)
        navigationView.setNavigationItemSelectedListener(this)
        navigationView.findViewById<TextView>(R.id.version_tv).text =
            "v${packageInfo?.versionName}. Powered by CreditClub"

        localStorage.agentInfo?.run {
            val info = Gson().fromJson(this, AgentInfo::class.java)

            navigationView.getHeaderView(0).findViewById<TextView>(R.id.username_tv).text =
                info.agentName

            navigationView.getHeaderView(0).findViewById<TextView>(R.id.phone_no_tv).text =
                info.phoneNumber
        }
    }

    override fun onResume() {
        super.onResume()
        Log.e("UpdateChecked", updateChecked.toString())

        if (updateChecked) return

        val updateCheckDateTime = try {
            LocalStorage.GetValueFor(AppConstants.UPDATE_CHECK_DATE, this).toLong()
        } catch (ex: Exception) {
            Misc.getCurrentDateTime().time
        }

        val currentTime = Misc.getCurrentDateTime().time

        val timeDifference = currentTime - updateCheckDateTime
        LocalStorage.SaveValue(
            AppConstants.UPDATE_CHECK_TIME_DIFFERENCE,
            timeDifference.toString(),
            this
        )
        Log.e("Diff", timeDifference.toString() + "")

        // The update hasn't been checked
        if (timeDifference == 0L) return

        shouldForceUpdate =
            timeDifference > 1814400000//(300000 * 3);//86400000;//1814400000;3600000*21*24

        /*if (!updateChecked)
        {
            updateChecked = true;
            showUpdateDialog();
            return;
        }*/
//        showUpdateDialog()
        updateChecked = true
        /*if (shouldForceUpdate)
        {
            showUpdateDialog();
        }else
        {
            showUpdateDialog();
        }*/
    }

    private fun openPlayStore() {
        val appPackageName = packageName
        try {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("market://details?id=$appPackageName")
                )
            )
        } catch (anfe: android.content.ActivityNotFoundException) {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/apps/details?id=$appPackageName")
                )
            )
        }

    }

    /*private class GetLatestVersion extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            String latestVersion = null;
            try {
                //It retrieves the latest version by scraping the content of current version from play store at runtime
                Document doc = Jsoup.connect("https://play.google.com/store/apps/details?" +
                        "id=com.appzone.android.bankonemobile.creditclub").get();
                latestVersion = doc.getElementsByClass("htlgb").get(6).text();

            }catch (Exception e){
                e.printStackTrace();
            }

            return latestVersion;
        }

        @Override
        protected void onPostExecute(String latestVersion) {
            super.onPostExecute(latestVersion);
            if(latestVersion != null) {
                try {
                    long currentTime = Misc.getCurrentDateTime().getTime();
                    if (!getCurrentVersion().equals(latestVersion)) {
                        if (!isFinishing())
                        { //This would help to prevent Error : BinderProxy@45d459c0 is not valid; is your activity running? error

                            long timeDifference = 0, updateCheckDateTime = 0;
                            try{
                                timeDifference = Long.parseLong(LocalStorage.GetValueFor(AppConstants.UPDATE_CHECK_TIME_DIFFERENCE,CreditClubMainMenuActivity.this));
                            }catch (Exception ex)
                            {
                                ex.printStackTrace();
                            }

                            try{
                                updateCheckDateTime = Long.parseLong(LocalStorage.GetValueFor(AppConstants.UPDATE_CHECK_DATE,CreditClubMainMenuActivity.this));
                            }catch (Exception ex)
                            {
                                LocalStorage.SaveValue(AppConstants.UPDATE_CHECK_DATE, String.valueOf(currentTime), CreditClubMainMenuActivity.this);
                                ex.printStackTrace();
                            }

                            // If the difference between the current dateTime and the first time the user
                            // got notified
                            // OR
                            // If the user takes the date on the device back such that the date of the
                            // last
                            // force the update
                            // 1814400000 is the number of milliseconds in 21 days
                            *//*if (currentTime - updateCheckDateTime > 1814400000 || currentTime - updateCheckDateTime < timeDifference)
                            {

                            }*//*
                            shouldForceUpdate = currentTime - updateCheckDateTime > 1814400000 ||
                                    currentTime - updateCheckDateTime < timeDifference;

                            showUpdateDialog();

                            LocalStorage.SaveValue(AppConstants.UPDATE_CHECK_TIME_DIFFERENCE, String.valueOf(currentTime - updateCheckDateTime), CreditClubMainMenuActivity.this);
                            updateChecked = true;
                        }
                    }
                    else{
                        LocalStorage.remove(AppConstants.UPDATE_CHECK_DATE, CreditClubMainMenuActivity.this);
                        LocalStorage.SaveValue(AppConstants.UPDATE_CHECK_TIME_DIFFERENCE, String.valueOf(0), CreditClubMainMenuActivity.this);
                    }
                }catch (Exception ex)
                {
                    ex.printStackTrace();
                }
            }
            //else


        }

    }
*/
    private fun showUpdateDialog() {

        val messageToDisplay = "CreditClub has an update. " + if (shouldForceUpdate)
            "You have to download it now"
        else
            "Do you want to download it now?"

        val dialog = if (shouldForceUpdate)
            Dialogs.getErrorDialog(this, messageToDisplay)
        else
            Dialogs.getQuestionDialog(this, messageToDisplay)

        if (shouldForceUpdate) {
            (dialog.findViewById<View>(R.id.close_btn) as Button).text =
                getString(R.string.continue_)
            dialog.findViewById<View>(R.id.close_btn).setOnClickListener { openPlayStore() }
        } else {
            (dialog.findViewById<View>(R.id.cancel_btn) as Button).text = getString(R.string.no)
            (dialog.findViewById<View>(R.id.ok_btn) as Button).text = getString(R.string.yes)
            dialog.findViewById<View>(R.id.ok_btn).setOnClickListener {
                openPlayStore()
                dialog.dismiss()
            }

            dialog.findViewById<View>(R.id.cancel_btn).setOnClickListener { dialog.dismiss() }
        }

        dialog.show()


        /*final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("CreditClub has an update. " +
                (shouldForceUpdate ? "You have to download it now"
                        : "Do you want to download it now?"));

        builder.setPositiveButton((shouldForceUpdate ? "Continue" : "Yes"), new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which) {
                openPlayStore();
                if (!shouldForceUpdate)
                    dialog.dismiss();
            }
        });

        if (!shouldForceUpdate) {
            builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
        }

        builder.setCancelable(false);
        builder.show();*/
    }

    /*boolean shouldForceUpdate()
    {
        long currentTime = Misc.getCurrentDateTime().getTime();

        if (!isFinishing()) { //This would help to prevent Error : BinderProxy@45d459c0 is not valid; is your activity running? error

            long timeDifference = 0, updateCheckDateTime = 0;
            try {
                timeDifference = Long.parseLong(LocalStorage.GetValueFor(AppConstants.UPDATE_CHECK_TIME_DIFFERENCE, CreditClubMainMenuActivity.this));
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            try {
                updateCheckDateTime = Long.parseLong(LocalStorage.GetValueFor(AppConstants.UPDATE_CHECK_DATE, CreditClubMainMenuActivity.this));
            } catch (Exception ex) {
                LocalStorage.SaveValue(AppConstants.UPDATE_CHECK_DATE, String.valueOf(currentTime), CreditClubMainMenuActivity.this);
                ex.printStackTrace();
            }

            // If the difference between the current dateTime and the first time the user
            // got notified
            // OR
            // If the user takes the date on the device back such that the date of the
            // last
            // force the update
            // 1814400000 is the number of milliseconds in 21 days
            *//*if (currentTime - updateCheckDateTime > 1814400000 || currentTime - updateCheckDateTime < timeDifference)
            {

            }*//*

            shouldForceUpdate = currentTime - updateCheckDateTime > 1814400000 ||
                    currentTime - updateCheckDateTime < timeDifference;
            if (shouldForceUpdate) {
                showUpdateDialog();
            }
            *//*else
            {
                if (!updateChecked)
                    showUpdateDialog();
            }*//*

            //LocalStorage.SaveValue(AppConstants.UPDATE_CHECK_TIME_DIFFERENCE, String.valueOf(currentTime - updateCheckDateTime), CreditClubMainMenuActivity.this);
            updateChecked = true;
        }
        return shouldForceUpdate;
    }*/

    override fun onBackPressed() {
        try {
            if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
                mDrawerLayout.closeDrawer(GravityCompat.START)
            } else {
                super.onBackPressed()
            }
        } catch (ex: Exception) {
            finish()
        }

    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.logout -> {
                startActivity(Intent(this@CreditClubMainMenuActivity, LoginActivity::class.java))
                finish()
            }
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
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START)
        } else {
            mDrawerLayout.openDrawer(GravityCompat.START)
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
