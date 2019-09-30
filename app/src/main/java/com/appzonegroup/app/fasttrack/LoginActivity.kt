package com.appzonegroup.app.fasttrack

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import com.appzonegroup.app.fasttrack.model.AppConstants
import com.appzonegroup.app.fasttrack.utility.Dialogs
import com.appzonegroup.app.fasttrack.utility.LocalStorage
import com.appzonegroup.app.fasttrack.utility.Misc
import com.creditclub.core.util.localStorage
import com.creditclub.core.util.safeRunIO
import kotlinx.coroutines.launch
import java.util.*

class LoginActivity : BaseActivity() {

    private val REQUEST_READ_CONTACTS = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        (findViewById<View>(R.id.version_tv) as TextView).text =
            String.format(Locale.getDefault(), "Version %s", Misc.getVersionName(this))

        if (BuildConfig.DEBUG) {
            findViewById<EditText>(R.id.login_phoneNumber).setText(localStorage.agentPhone)
        }

        addValidPhoneNumberListener(findViewById(R.id.login_phoneNumber))
        findViewById<View>(R.id.email_sign_in_button).setOnClickListener { attemptLogin() }

        mainScope.launch {
            val (response, error) = safeRunIO {
                creditClubMiddleWareAPI.staticService.getAgentInfoByPhoneNumber(
                    localStorage.institutionCode,
                    localStorage.agentPhone
                )
            }

            if (error != null) return@launch
            response ?: return@launch

            localStorage.agentInfo = response.string()
        }
    }

    override fun onResume() {
        super.onResume()
        ensureLocationEnabled()

    }

    /**
     * Callback received when a permissions request has been completed.
     */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode == REQUEST_READ_CONTACTS) {
            if (grantResults.size == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //populateAutoComplete();
            }
        }
    }

    override fun onBackPressed() {

        val notification = Dialogs.getQuestionDialog(this@LoginActivity, "Do you want to exit? ")
        val logout_no_button = notification.findViewById<Button>(R.id.cancel_btn)
        val logout_yes_button = notification.findViewById<Button>(R.id.ok_btn)

        logout_no_button.setOnClickListener { notification.dismiss() }

        logout_yes_button.setOnClickListener {
            notification.dismiss()
            val intent = Intent(Intent.ACTION_MAIN)
            intent.addCategory(Intent.CATEGORY_HOME)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
        }
        notification.show()

    }

    /**
     * Attempts to sign in or register the customer specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private fun attemptLogin() {

        // Store values at the time of the login attempt.
        val phoneNumber = (findViewById<View>(R.id.login_phoneNumber) as EditText).text.toString()
        val pin = (findViewById<View>(R.id.login_pin) as EditText).text.toString()

        if (TextUtils.isEmpty(phoneNumber)) {
            showError("Phone number is required", R.id.login_phoneNumber)
            return
        }

        if (TextUtils.isEmpty(pin)) {
            showError("PIN is required", R.id.login_pin)
            return
        }


        /*if (!Misc.isValidNumber(phoneNumber))
        {
            showError("Phone number is invalid. Please enter a valid phone number");
            return;
        }*/


        // Check for a valid phone number.
        if (!isPhoneValid(phoneNumber)) {
            showError("Phone number is incorrect", R.id.login_phoneNumber)
            return
        }

        // Check for a valid password, if the user entered one.
        if (!isPINValid(pin)) {
            showError("PIN is invalid", R.id.login_pin)
            return
        }

        val Login = "Last Login: " + Misc.dateToLongString(Misc.getCurrentDateTime())
        LocalStorage.SaveValue(AppConstants.LAST_LOGIN, Login, baseContext)

        val activityToOpen: Class<*>

        if (packageName.toLowerCase().contains("cashout")) {
            activityToOpen = CashoutMainMenuActivity::class.java
        } else if (packageName.toLowerCase().contains(".creditclub")) {
            activityToOpen = CreditClubMainMenuActivity::class.java
        } else {
            activityToOpen = Menu3Activity::class.java
        }

        val intent = Intent(this@LoginActivity, activityToOpen)
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
        startActivity(intent)
        finish()
    }

    internal fun showError(message: String, viewId: Int) {
        Dialogs.getErrorDialog(this, message).show()
        findViewById<View>(viewId).requestFocus()
    }

    private fun isPhoneValid(phoneNumber: String): Boolean {
        //TODO: Replace this with your own logic

        val storedPhone = LocalStorage.GetValueFor(AppConstants.AGENT_PHONE, baseContext)
        return phoneNumber == storedPhone
    }

    private fun isPINValid(pin: String): Boolean {
        //TODO: Replace this with your own logic
        val storedPIN = LocalStorage.GetValueFor(AppConstants.AGENT_PIN, baseContext)
        return pin == storedPIN
    }

    /**
     * Shows the progress UI and hides the login form.
     *//*
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }*/

    /*@Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(this,
                // Retrieve data rows for the device user's 'profile' contact.
                Uri.withAppendedPath(ContactsContract.Profile.CONTENT_URI,
                        ContactsContract.Contacts.Data.CONTENT_DIRECTORY), ProfileQuery.PROJECTION,

                // Select only email addresses.
                ContactsContract.Contacts.Data.MIMETYPE +
                        " = ?", new String[]{ContactsContract.CommonDataKinds.Email
                .CONTENT_ITEM_TYPE},

                // Show primary email addresses first. Note that there won't be
                // a primary email address if the user hasn't specified one.
                ContactsContract.Contacts.Data.IS_PRIMARY + " DESC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        List<String> emails = new ArrayList<>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            emails.add(cursor.getString(ProfileQuery.ADDRESS));
            cursor.moveToNext();
        }

        addEmailsToAutoComplete(emails);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {

    }*/

    /*private void addEmailsToAutoComplete(List<String> emailAddressCollection) {
        //Create adapter to tell the AutoCompleteTextView what to show in its dropdown list.
        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(LoginActivity.this,
                        android.R.layout.simple_dropdown_item_1line, emailAddressCollection);

        phoneEt.setAdapter(adapter);
    }


    private interface ProfileQuery {
        String[] PROJECTION = {
                ContactsContract.CommonDataKinds.Email.ADDRESS,
                ContactsContract.CommonDataKinds.Email.IS_PRIMARY,
        };

        int ADDRESS = 0;
        int IS_PRIMARY = 1;
    }*/

    /*
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    /*public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        private final String mEmail;
        private final String mPassword;

        UserLoginTask(String email, String password) {
            mEmail = email;
            mPassword = password;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            // TODO: attempt authentication against a network service.

            try {
                // Simulate network access.
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                return false;
            }

            for (String credential : DUMMY_CREDENTIALS) {
                String[] pieces = credential.split(":");
                if (pieces[0].equals(mEmail)) {
                    // Account exists, return true if the password matches.
                    return pieces[1].equals(mPassword);
                }
            }

            // TODO: ic_register the new customerRequest here.
            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            showProgress(false);

            if (success) {
                finish();
            } else {
                pinEt.setError(getString(R.string.error_incorrect_password));
                pinEt.requestFocus();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }*/

    protected fun ensureLocationEnabled() {
        var locationMode = 0 // 0 == Settings.Secure.LOCATION_MODE_OFF
        var locationProviders: String? = null

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            try {
                locationMode = Settings.Secure.getInt(
                    applicationContext.contentResolver,
                    Settings.Secure.LOCATION_MODE
                )
            } catch (e: Settings.SettingNotFoundException) {

            }

        } else {
            locationProviders = Settings.Secure.getString(
                applicationContext.contentResolver,
                Settings.Secure.LOCATION_PROVIDERS_ALLOWED
            )
        }

        val locationEnabled = !TextUtils.isEmpty(locationProviders) || locationMode != 0
        if (!locationEnabled) {
//            android.app.AlertDialog.Builder(this)
//                .setCancelable(false)
//                .setMessage("An active GPS service is needed for this application. Click 'OK' to activate it.")
//                .setPositiveButton(android.R.string.ok) { dialog, which ->
//                    //this will navigate user to the device location settings screen
//                    val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
//                    startActivity(intent)
//                }
//                .show()

            com.appzonegroup.app.fasttrack.ui.Dialogs.confirm(
                this,
                "An active GPS service is needed for this application",
                "Click 'OK' to activate it"
            ) {
                onSubmit {
                    if (it) {
                        val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                        startActivity(intent)
                    } else finish()
                }

                onClose {
                    finish()
                }
            }
            /*Intent s = new Intent(getApplicationContext(), LocationCheckDialog.class);
            s.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(s);*/
        }
    }

    override fun processFinished(output: String?) {

//        if (output != null) {
//            val (_, error) = safeRun {
//                val info = Gson().fromJson(output, AgentInfo::class.java)
//                if (info.isStatus) {
//                    LocalStorage.setAgentInfo(this, output)
//                    //LocalStorage.SaveValue(AppConstants.AGENT_NAME, info.getAgentName(), this);
//                }
//            }
//
//            if (error != null) Crashlytics.logException(error)
//        } else {
//            if (LocalStorage.getAgentInfo(this) == null) {
//                showError("You do not have internet on your device. Please make interrnet available and try again")
//            }
//        }
    }

}

