package com.appzonegroup.app.fasttrack;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.PagerAdapter;
import com.google.android.material.tabs.TabLayout;
import androidx.fragment.app.Fragment;
import androidx.loader.content.CursorLoader;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;


import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;
import android.os.Bundle;
import android.util.Base64;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.appzonegroup.app.fasttrack.fragment.PassportPhotoFragment;
import com.appzonegroup.app.fasttrack.fragment.IdentityPhotoFragment;
import com.appzonegroup.app.fasttrack.fragment.SignatureFragment;
import com.appzonegroup.app.fasttrack.dataaccess.AccountDAO;
import com.appzonegroup.app.fasttrack.dataaccess.ProductDAO;
import com.appzonegroup.app.fasttrack.model.Account;
import com.appzonegroup.app.fasttrack.model.AppConstants;
import com.appzonegroup.app.fasttrack.model.ImageSource;
import com.creditclub.core.data.model.Product;
import com.appzonegroup.app.fasttrack.model.ResponseBody;
import com.appzonegroup.app.fasttrack.utility.AppStatus;
import com.appzonegroup.app.fasttrack.network.APICaller;
import com.appzonegroup.app.fasttrack.utility.CalendarDialog;
import com.appzonegroup.app.fasttrack.utility.ImageManipulations;
import com.appzonegroup.app.fasttrack.utility.LocalStorage;
import com.appzonegroup.app.fasttrack.utility.TrackGPS;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.gson.Gson;

import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class OpenAccountActivity extends AppCompatActivity {

    /**
     * The {@link PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link FragmentStatePagerAdapter}.
     */

    static EditText surnameEt, firstNameEt, middleNameEt, emailEt, phoneNumberEt,
            addressEt, placeOfBirthEt,customersBVNEt, agentsPINEt, cardSerialNoEt, confirmCardSerialNoEt, bvn_edittext;
    static TextView dobTv, message;
    static String identityImageString = "", passportImageString = "", signatureImageString = "";
    public static ImageView photoImageView, identityPhoto_ImageView, passportPhoto_ImageView, signaturePhoto_ImageView;

    TrackGPS gps;

    Button retry, saveoffline, cancel;
    ProgressBar progressbar;
    TextView dialogheader;

    String longitude;
    String latitude;


    static Spinner genderSpinner, productSpinner, stateSpinner, religionSpinner;
    //static CheckBox cardlessTrxnCheckBox;
    static ArrayList<String> productNames;
    static ArrayList<Product> products;
    static long id = 0;
    AppStatus appStatus = new AppStatus();
    public static Account account;
    AccountDAO accountDAO;

    Gson gson;


    String accountName, phonenumber, firstname, lastname, bvn , dob, agentPhone = "", agentPIN = "", institutionCode = "";

    String surname, firstName,religion, middleName, phoneNumber, gender, email, address, placeOfBirth;


    static Boolean bvnisfound = false;


    Button bvn_verifybvn, bvn_cancel;
    TextView bvn_message;
    ProgressBar bvn_progressbar;
    Account tempaccount;

    static Bitmap sendBitmap;

    enum Form {
        GENERAL_INFO,
        CONTACT_DETAILS,
        PHOTO_CAPTURE,
        OTHER_DETAILS
    }

    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */

    public static ViewPager mViewPager2;
    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_open_account);

        /*Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);*/
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.

        gps = new TrackGPS(OpenAccountActivity.this);
        if (gps.canGetLocation()) {


        } else {

            gps.showSettingsAlert();
        }

        account = new Account();
        accountDAO = new AccountDAO(getBaseContext());

        try {
            id = getIntent().getLongExtra("ID", 0);
            account = accountDAO.Get(id);

            if (account == null) {
                account = new Account();
            }
        } catch (Exception ex) {
            account = new Account();
            //customerRequest.setId(0);
        }

        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        mViewPager.setOffscreenPageLimit(mSectionsPagerAdapter.getCount());

        ProductDAO productDAO = new ProductDAO(getBaseContext());
        products = (ArrayList<Product>) productDAO.GetAll();
        productDAO.close();

        productNames = new ArrayList<>();
        productNames.add("Select product...");
        for (Product product : products) {
            productNames.add(product.getName());
        }
    }

    public void usebvn_btn_click(View v) {

        final AlertDialog.Builder builder = new AlertDialog.Builder(OpenAccountActivity.this);
        View getbvnview = getLayoutInflater().inflate(R.layout.activity_dialog, null);
        bvn_verifybvn = (Button) getbvnview.findViewById(R.id.btn_yes);
        bvn_cancel = (Button) getbvnview.findViewById(R.id.btn_no);
        bvn_edittext = (EditText) getbvnview.findViewById(R.id.enter_bvnNumber_et);

        bvn_message = (TextView) getbvnview.findViewById(R.id.notification);
        bvn_progressbar = (ProgressBar) getbvnview.findViewById(R.id.bvnprogressBar);


        builder.setView(getbvnview);
        final AlertDialog getBVN_dialog = builder.create();
        getBVN_dialog.show();
        getBVN_dialog.setCanceledOnTouchOutside(false);


        bvn_verifybvn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (appStatus.getInstance(OpenAccountActivity.this).isOnline()) {


                    String bvnNumber = bvn_edittext.getText().toString();

                    if (bvnNumber.length() != 11) {

                        bvn_message.setVisibility(View.VISIBLE);
                        bvn_message.setText("Invalid BVN");
                        bvn_message.setTextColor(Color.RED);

                    } else {

                        bvn_progressbar.setVisibility(View.VISIBLE);
                        GetBVN getbvn = new GetBVN(OpenAccountActivity.this, bvnNumber, getBVN_dialog);
                        getbvn.execute();
                    }


                } else {
                    bvn_message.setVisibility(View.VISIBLE);
                    bvn_message.setText("No internet connection");
                    bvn_message.setTextColor(Color.RED);
                }


            }
        });

        bvn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getBVN_dialog.dismiss();
            }
        });
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == ImageSource.IDENTITYCAMERA.ordinal()) {
                Bitmap thumbnail = (Bitmap) data.getExtras().get("data");
                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                thumbnail.compress(Bitmap.CompressFormat.JPEG, 90, bytes);

                sendBitmap = thumbnail;

                byte[] byteArray = bytes.toByteArray();
                identityImageString = Base64.encodeToString(byteArray, Base64.DEFAULT);

                File destination = new File(Environment.getExternalStorageDirectory(),
                        System.currentTimeMillis() + ".jpg");

                FileOutputStream fo;
                try {
                    destination.createNewFile();
                    fo = new FileOutputStream(destination);
                    fo.write(bytes.toByteArray());
                    fo.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    FirebaseCrashlytics.getInstance().recordException(new Exception(e.getMessage()));
                } catch (IOException e) {
                    e.printStackTrace();
                    FirebaseCrashlytics.getInstance().recordException(new Exception(e.getMessage()));
                }

                identityPhoto_ImageView.setImageBitmap(thumbnail);
            } else if (requestCode == ImageSource.PASSPORTCAMERA.ordinal()) {

                Bitmap thumbnail = (Bitmap) data.getExtras().get("data");
                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                thumbnail.compress(Bitmap.CompressFormat.JPEG, 90, bytes);

                sendBitmap = thumbnail;

                byte[] byteArray = bytes.toByteArray();
                passportImageString = Base64.encodeToString(byteArray, Base64.DEFAULT);

                File destination = new File(Environment.getExternalStorageDirectory(),
                        System.currentTimeMillis() + ".jpg");

                FileOutputStream fo;
                try {
                    destination.createNewFile();
                    fo = new FileOutputStream(destination);
                    fo.write(bytes.toByteArray());
                    fo.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    FirebaseCrashlytics.getInstance().recordException(new Exception(e.getMessage()));
                } catch (IOException e) {
                    e.printStackTrace();
                    FirebaseCrashlytics.getInstance().recordException(new Exception(e.getMessage()));
                }

                passportPhoto_ImageView.setImageBitmap(thumbnail);

            } else if (requestCode == ImageSource.SIGNATURECAMERA.ordinal()) {

                Bitmap thumbnail = (Bitmap) data.getExtras().get("data");
                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                thumbnail.compress(Bitmap.CompressFormat.JPEG, 90, bytes);

                sendBitmap = thumbnail;

                byte[] byteArray = bytes.toByteArray();
                signatureImageString = Base64.encodeToString(byteArray, Base64.DEFAULT);

                File destination = new File(Environment.getExternalStorageDirectory(),
                        System.currentTimeMillis() + ".jpg");

                FileOutputStream fo;
                try {
                    destination.createNewFile();
                    fo = new FileOutputStream(destination);
                    fo.write(bytes.toByteArray());
                    fo.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    FirebaseCrashlytics.getInstance().recordException(new Exception(e.getMessage()));
                } catch (IOException e) {
                    e.printStackTrace();
                    FirebaseCrashlytics.getInstance().recordException(new Exception(e.getMessage()));
                }

                signaturePhoto_ImageView.setImageBitmap(thumbnail);

            } else if (requestCode == ImageSource.IDENTITYGALLERY.ordinal()) {

                Uri selectedImageUri = data.getData();
                String[] projection = {MediaStore.MediaColumns.DATA};
                CursorLoader cursorLoader = new CursorLoader(this, selectedImageUri, projection, null, null, null);
                Cursor cursor = cursorLoader.loadInBackground();
                int column_index = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
                cursor.moveToFirst();

                String selectedImagePath = cursor.getString(column_index);

                Bitmap bm;
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeFile(selectedImagePath, options);
                final int REQUIRED_SIZE = 100;
                int scale = 1;
                while (options.outWidth / scale / 2 >= REQUIRED_SIZE && options.outHeight / scale / 2 >= REQUIRED_SIZE)
                    scale *= 2;
                options.inSampleSize = scale;
                options.inJustDecodeBounds = false;
                bm = BitmapFactory.decodeFile(selectedImagePath, options);

                //Convert bitmap to string
                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                bm.compress(Bitmap.CompressFormat.JPEG, 90, bytes);
                byte[] byteArray = bytes.toByteArray();

                sendBitmap = bm;

                identityImageString = Base64.encodeToString(byteArray, Base64.DEFAULT);

                identityPhoto_ImageView.setImageBitmap(bm);

            }else if(requestCode == ImageSource.PASSPORTGALLERY.ordinal()){

                Uri selectedImageUri = data.getData();
                String[] projection = {MediaStore.MediaColumns.DATA};
                CursorLoader cursorLoader = new CursorLoader(this, selectedImageUri, projection, null, null, null);
                Cursor cursor = cursorLoader.loadInBackground();
                int column_index = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
                cursor.moveToFirst();

                String selectedImagePath = cursor.getString(column_index);

                Bitmap bm;
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeFile(selectedImagePath, options);
                final int REQUIRED_SIZE = 100;
                int scale = 1;
                while (options.outWidth / scale / 2 >= REQUIRED_SIZE && options.outHeight / scale / 2 >= REQUIRED_SIZE)
                    scale *= 2;
                options.inSampleSize = scale;
                options.inJustDecodeBounds = false;
                bm = BitmapFactory.decodeFile(selectedImagePath, options);

                //Convert bitmap to string
                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                bm.compress(Bitmap.CompressFormat.JPEG, 90, bytes);
                byte[] byteArray = bytes.toByteArray();

                sendBitmap = bm;

                passportImageString = Base64.encodeToString(byteArray, Base64.DEFAULT);

                passportPhoto_ImageView.setImageBitmap(bm);

            }else if(requestCode == ImageSource.SIGNATUREGALLERY.ordinal()){

                Uri selectedImageUri = data.getData();
                String[] projection = {MediaStore.MediaColumns.DATA};
                CursorLoader cursorLoader = new CursorLoader(this, selectedImageUri, projection, null, null, null);
                Cursor cursor = cursorLoader.loadInBackground();
                int column_index = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
                cursor.moveToFirst();

                String selectedImagePath = cursor.getString(column_index);

                Bitmap bm;
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeFile(selectedImagePath, options);
                final int REQUIRED_SIZE = 100;
                int scale = 1;
                while (options.outWidth / scale / 2 >= REQUIRED_SIZE && options.outHeight / scale / 2 >= REQUIRED_SIZE)
                    scale *= 2;
                options.inSampleSize = scale;
                options.inJustDecodeBounds = false;
                bm = BitmapFactory.decodeFile(selectedImagePath, options);

                //Convert bitmap to string
                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                bm.compress(Bitmap.CompressFormat.JPEG, 90, bytes);
                byte[] byteArray = bytes.toByteArray();

                sendBitmap = bm;

                signatureImageString = Base64.encodeToString(byteArray, Base64.DEFAULT);

                signaturePhoto_ImageView.setImageBitmap(bm);

            }
    }
}

    public void galleryButton_click(View view){

        ImageManipulations.requestImage(this, ImageSource.GALLERY);

    }

    public void PassportCamera_Click(View view){
        ImageManipulations.requestImage(this, ImageSource.PASSPORTCAMERA);
    }

    public void PassportGallery_Click(View view){
        ImageManipulations.requestImage(this, ImageSource.PASSPORTGALLERY);
    }

    public void IdentityCamera_Click(View view){
        ImageManipulations.requestImage(this, ImageSource.IDENTITYCAMERA);
    }

    public void IdentityGallery_Click(View view){
        ImageManipulations.requestImage(this, ImageSource.IDENTITYGALLERY);
    }

    public void SignatureCamera_Click(View view){
        ImageManipulations.requestImage(this, ImageSource.SIGNATURECAMERA);
    }

    public void SignatureGallery_Click(View view){
        ImageManipulations.requestImage(this, ImageSource.SIGNATUREGALLERY);
    }

    void showNotification(String message){
        Toast.makeText(getBaseContext(), message, Toast.LENGTH_LONG).show();
    }

    void indicateError(String message, int position, View view){
        mViewPager.setCurrentItem(position);
        showNotification(message);
        if (view != null){
            view.requestFocus();
        }
    }

    public void createAccount_click(View view){



        /*if (agentPIN == ""){
            indicateError("Please enter your PIN", Form.OTHER_DETAILS.ordinal(), agentsPINEt);
            return;
        }

        String pin = LocalStorage.GetValueFor(AppConstants.AGENT_PIN, getBaseContext());

        if (!agentPIN.equals(pin)){
            indicateError("Incorrect PIN", Form.OTHER_DETAILS.ordinal(), agentsPINEt);
            return;
        }
*/
        String Coordinate = gps.getGeolocationString();


        tempaccount = new Account();
        tempaccount.setLastName(surname);
        tempaccount.setFirstName(firstName);
        tempaccount.setOtherNames(middleNameEt.getText().toString().trim());
        tempaccount.setAccountofficercode("");
        tempaccount.setBvn(customersBVNEt.getText().toString().trim().length() > 0 ? customersBVNEt.getText().toString().trim(): "");
        tempaccount.setCustomerPassportInBytes(passportImageString);
        tempaccount.setCustomerSignatureInBytes(signatureImageString);
        tempaccount.setEmail(emailEt.getText().toString());
        tempaccount.setNationalIdentityNo("");
        tempaccount.setReferralPhoneNo("");
        tempaccount.setReferralName("");
        tempaccount.setReligion(religionSpinner.getSelectedItem().toString());
        tempaccount.setSecondaryIdentityInBytes(identityImageString);
        tempaccount.setPhoneNo(phoneNumber);
        tempaccount.setDateOfBirth(dob);
        tempaccount.setGender(gender);
        tempaccount.setState(stateSpinner.getSelectedItem().toString());
        tempaccount.setAddress(address);
        tempaccount.setPlaceOfBirth(placeOfBirth);
        tempaccount.setSecondaryIdentityInBytes(identityImageString);
        tempaccount.setProductCode("");


        final AlertDialog.Builder builder1 = new AlertDialog.Builder(OpenAccountActivity.this);
        View processonlineview = getLayoutInflater().inflate(R.layout.activity_dialog2, null);
        retry = (Button) processonlineview.findViewById(R.id.btn_retry);
        saveoffline = (Button) processonlineview.findViewById(R.id.btn_save_offline);
        cancel = (Button) processonlineview.findViewById(R.id.btn_cancel);

        message = (TextView) processonlineview.findViewById(R.id.notification_textview);
        progressbar = (ProgressBar) processonlineview.findViewById(R.id.notification_progressbar);
        dialogheader = (TextView) processonlineview.findViewById(R.id.txt_dialogheader);
        builder1.setView(processonlineview);
        final AlertDialog processonline_dialog = builder1
                .create();
        processonline_dialog.show();
        processonline_dialog.setCanceledOnTouchOutside(false);





        if(appStatus.getInstance(this).isOnline()){



            dialogheader.setText("Opening Account Info");
            message.setText("what would you like to do?");
                          retry.setText("Continue");





            retry.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    retry.setVisibility(View.GONE);
                    saveoffline.setVisibility(View.GONE);
                    cancel.setVisibility(View.GONE);
                    progressbar.setVisibility(View.VISIBLE);
                    message.setText("Opening customerRequest in progress");

                    if (tempaccount.getID() == 0){
                        syncAccount syncaccount = new syncAccount(OpenAccountActivity.this,tempaccount, gson);
                        syncaccount.execute();

                    }else{


                        syncAccount syncaccount = new syncAccount(OpenAccountActivity.this,tempaccount, gson);
                        syncaccount.execute();
                    }

                }
            });

            saveoffline.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {


                        Toast.makeText(getBaseContext(), "Feature not currently available", Toast.LENGTH_LONG).show();

                }
            });

            cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    processonline_dialog.dismiss();
                }
            });




        }else{


            dialogheader.setText("Opening Account Info");
            message.setText("No internet connection?");
            retry.setText("Retry");

            retry.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(appStatus.getInstance(OpenAccountActivity.this).isOnline()){
                        retry.setVisibility(View.GONE);
                        saveoffline.setVisibility(View.GONE);
                        progressbar.setVisibility(View.VISIBLE);
                        message.setText("Opening customerRequest in progress");

                        if (tempaccount.getID() == 0){

                            syncAccount syncaccount = new syncAccount(OpenAccountActivity.this,tempaccount, gson);
                            syncaccount.execute();

                        }else{


                            syncAccount syncaccount = new syncAccount(OpenAccountActivity.this,tempaccount, gson);
                            syncaccount.execute();
                        }

                    }else{
                        processonline_dialog.show();
                        message.setText("No internet connection");
                        retry.setText("Retry");
                    }
                }
            });

            cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    processonline_dialog.dismiss();
                }
            });

            saveoffline.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {


                }
            });

        }

    }


    /*public long saveOffline(Account customerRequest){

        AccountDAO accountDAO = new AccountDAO(getBaseContext());

        long id = 0;

        if (customerRequest.getId() == 0){

            id = accountDAO.Insert(customerRequest);
            Toast.makeText(getBaseContext(), "Account saved successfully", Toast.LENGTH_LONG).show();

        }else{

            accountDAO.UpdateAccount(customerRequest);
            Toast.makeText(getBaseContext(), "Account updated successfully", Toast.LENGTH_LONG).show();
        }


        accountDAO.close();


        return id;
    }*/

    public void next_button_click1(View view){

        surname = surnameEt.getText().toString().trim();

        if (surname.length() == 0){
            indicateError("Please enter customer's surname", Form.GENERAL_INFO.ordinal(), surnameEt);

            FirebaseCrashlytics.getInstance().recordException(new Exception("incorrect user name"));
            FirebaseCrashlytics.getInstance().log("this is a crash");
            return;
        }

        firstName = firstNameEt.getText().toString().trim();
        if (firstName.length() == 0){
            indicateError("Please enter customer's first name", Form.GENERAL_INFO.ordinal(), firstNameEt);
            return;
        }

        middleName = middleNameEt.getText().toString().trim();
        if (middleName.length() == 0){
            indicateError("Please enter customer's middle name", Form.GENERAL_INFO.ordinal(), firstNameEt);
            return;
        }

        phoneNumber = phoneNumberEt.getText().toString().trim();
        //String phoneNumberConfirm = generalinfo_confirmPhone_et.getText().toString().trim();
        if (phoneNumber.length() == 0){
            indicateError("Please enter customer's phone number", Form.GENERAL_INFO.ordinal(), phoneNumberEt);
            return;
        }

        if (phoneNumber.length() != 11){
            indicateError("Customer's phone number must be 11 digits", Form.GENERAL_INFO.ordinal(), phoneNumberEt);
            return;
        }



        dob = dobTv.getText().toString().trim();
        if (dob.contains("Click")){
            indicateError("Please enter customer's date of birth", Form.GENERAL_INFO.ordinal(), dobTv);
            return;
        }

        if (genderSpinner.getSelectedItemPosition() == 0){
            indicateError("Please select a gender", Form.GENERAL_INFO.ordinal(), genderSpinner);
            return;
        }

        gender = genderSpinner.getSelectedItem().toString();

        if(religionSpinner.getSelectedItemPosition()==0){
            indicateError("Please select a religion", Form.GENERAL_INFO.ordinal(), religionSpinner);
            return;
        }

        mViewPager.setCurrentItem(mViewPager.getCurrentItem() + 1, true);
    }

    public void next_button_click2(View view){

        if (emailEt.getText().toString().trim().length() == 0){
            indicateError("Please enter an e-mail address", Form.CONTACT_DETAILS.ordinal(), addressEt);
            return;
        }


        address = addressEt.getText().toString().trim();

        if (address.length() == 0){
            indicateError("Please enter customer's address", Form.CONTACT_DETAILS.ordinal(), addressEt);
            return;
        }

        placeOfBirth = placeOfBirthEt.getText().toString().trim();
        if (placeOfBirth.length() == 0){
            indicateError("Please enter customer's place of birth", Form.CONTACT_DETAILS.ordinal(), placeOfBirthEt);
            return;
        }

        if(stateSpinner.getSelectedItemPosition()==0){
            indicateError("Please select a state", Form.CONTACT_DETAILS.ordinal(), placeOfBirthEt);
            return;
        }


        mViewPager.setCurrentItem(mViewPager.getCurrentItem() + 1, true);
    }

    public void capture1(View view){

        if (passportImageString.length() == 0){
            indicateError("Please provide a valid passport", Form.PHOTO_CAPTURE.ordinal(), passportPhoto_ImageView);
            return;
        }
        mViewPager2.setCurrentItem(mViewPager2.getCurrentItem() + 1, true);
    }

    public void capture2(View view){

        if (signatureImageString.length() == 0){
            indicateError("Please provide a signature photo", Form.PHOTO_CAPTURE.ordinal(), signaturePhoto_ImageView);
            return;
        }

        mViewPager2.setCurrentItem(mViewPager2.getCurrentItem() + 1, true);
    }


    public void capture3(View view){


        if (identityImageString.length() == 0){
            indicateError("Please provide an identity photo", Form.PHOTO_CAPTURE.ordinal(), identityPhoto_ImageView);
            return;
        }

        mViewPager.setCurrentItem(mViewPager.getCurrentItem() + 1, true);
    }

    public void show_calendar(View view) {

        final Dialog dialog = CalendarDialog.showCalendarDialog(OpenAccountActivity.this);
        final DatePicker datePicker = (DatePicker) dialog.findViewById(R.id.datePicker);

        dialog.findViewById(R.id.calendarViewButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int dayOfMonth = datePicker.getDayOfMonth();
                int month = datePicker.getMonth() + 1;

                String DD = dayOfMonth > 9 ? (dayOfMonth + "") : ("0" + dayOfMonth);
                String MM = month > 9 ?(month + "") : ("0" + month);

                dobTv.setText(datePicker.getYear()+"-"+ MM +"-"+ DD);
                dobTv.setGravity(Gravity.LEFT);
                dialog.dismiss();

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_open_account, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public static class GeneralInfoFragment extends Fragment{

        public GeneralInfoFragment(){}

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_general_info, container, false);

            religionSpinner = (Spinner)rootView.findViewById(R.id.generalinfo_religion_spinner);
            surnameEt = (EditText)rootView.findViewById(R.id.basicinfo_surname_et);
            firstNameEt = (EditText)rootView.findViewById(R.id.basicinfo_firstname_et);
            middleNameEt = (EditText)rootView.findViewById(R.id.basicinfo_middlename_et);
            phoneNumberEt = (EditText)rootView.findViewById(R.id.generalinfo_phone_et);
            dobTv = (TextView)rootView.findViewById(R.id.generalinfo_dob_tv);
            genderSpinner = (Spinner)rootView.findViewById(R.id.generalinfo_gender_spinner);

            if (account.getID() != 0){
                surnameEt.setText(account.getLastName());
                firstNameEt.setText(account.getFirstName());
                phoneNumberEt.setText(account.getPhoneNo());
                dobTv.setText(account.getDateOfBirth());

                if (account.getGender().startsWith("M")){
                    genderSpinner.setSelection(1);
                }else{
                    genderSpinner.setSelection(2);
                }
            }

            return rootView;
        }
    }

    public static class ContactInfoFragment extends Fragment{

        public ContactInfoFragment(){}

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_contact_info, container, false);
            stateSpinner = (Spinner) rootView.findViewById(R.id.generalinfo_state_spinner);
            emailEt = (EditText)rootView.findViewById(R.id.contactInfo_email_et);
            //contactInfo_confirmNokPhone_et = (EditText)rootView.findViewById(R.id.contactInfo_confirmNokPhone_et);
            addressEt = (EditText)rootView.findViewById(R.id.contactInfo_address_et);
            placeOfBirthEt = (EditText)rootView.findViewById(R.id.contactInfo_placeOfBirth_et);

            if (account.getID() != 0){

                addressEt.setText(account.getAddress());
                placeOfBirthEt.setText(account.getPlaceOfBirth());
            }

            return rootView;
        }
    }


    /*void doNotification(String message){
        NotificationCompat.Builder mBuilder =
                (NotificationCompat.Builder) new NotificationCompat.Builder(getBaseContext())
                        .setSmallIcon(R.drawable.ic_launcher_transparent)
                        .setContentTitle("CreditClub Offline")
                        .setContentText(message);

        PendingIntent resultPendingIntent = PendingIntent.getActivity(getApplicationContext(),
                0, new Intent(), PendingIntent.FLAG_CANCEL_CURRENT);
        mBuilder.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        // mId allows you to update the notification later on.
        mNotificationManager.notify(1234567, mBuilder.build());
    }*/

    public class CapturePhotoFragment extends Fragment{

        CapturePhoto_SectionsPagerAdapter mAppSectionsPagerAdapter;

        public CapturePhotoFragment(){

        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {

            View rootView = inflater.inflate(R.layout.fragment_capture_photo, container, false);
            mAppSectionsPagerAdapter =  new CapturePhoto_SectionsPagerAdapter(getSupportFragmentManager());

//            final ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();

            mViewPager2 = (ViewPager) rootView.findViewById(R.id.capture_container);
            mViewPager2.setAdapter(mAppSectionsPagerAdapter);


            TabLayout tabLayout = (TabLayout) rootView.findViewById(R.id.capture_photo_tabs);
            tabLayout.setupWithViewPager(mViewPager2);

            mViewPager2.setOffscreenPageLimit(mAppSectionsPagerAdapter.getCount());

            return rootView;
        }



        public class CapturePhoto_SectionsPagerAdapter extends FragmentPagerAdapter {

            public CapturePhoto_SectionsPagerAdapter(FragmentManager fm) {
                super(fm);
            }

            @Override
            public Fragment getItem(int position) {
                // getItem is called to instantiate the fragment for the given page.
                // Return a PlaceholderFragment (defined as a static inner class below).
                switch (position)
                {
                    case 0:{
                        return new PassportPhotoFragment();
                    }
                    case 1:{
                        return new SignatureFragment();
                    }
                    case 2:{
                        return new IdentityPhotoFragment();
                    }

                    default:{
                        return new PassportPhotoFragment();
                    }
                }
            }

            @Override
            public int getCount() {
                // Show 3 total pages.
                return 3;
            }

            @Override
            public CharSequence getPageTitle(int position) {
                switch (position) {
                    case 0:
                        return "Passport Photo";
                    case 1:
                        return "Signature";

                    case 2:
                        return "Identity";
                }
                return null;
            }
        }

    }

    public static class OtherDetailsFragment extends Fragment {

        public OtherDetailsFragment(){}

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_other_details, container, false);
            final View v = rootView;

            customersBVNEt = (EditText) rootView.findViewById(R.id.otherInfo_customerBVN_et);

            return rootView;
        }

    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            switch (position)
            {
                case 0:{
                    return new GeneralInfoFragment();
                }
                case 1:{
                    return new ContactInfoFragment();
                }
                case 2:{
                    return new CapturePhotoFragment();
                }
                case 3:{
                    return new OtherDetailsFragment();
                }
                default:{
                    return new GeneralInfoFragment();
                }
            }
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 4;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "General Details";
                case 1:
                    return "Contact Details";
                case 2:
                    return "Photo Capture";
                case 3:
                    return "Other Details";
            }
            return null;
        }
    }

    private class GetBVN extends AsyncTask<String, String, Boolean>{

        Context context;
        String bvnNumber;
        AlertDialog getBVN_dialog;

        public GetBVN(Context context, String bvnNumber, AlertDialog getBVN_dialog){

            this.context = context;
            this.bvnNumber = bvnNumber;
            this.getBVN_dialog = getBVN_dialog;
        }


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }



        @Override
        protected Boolean doInBackground(String... params) {
            try{

                URL url = new URL(AppConstants.getAccessUrl()+"Get/GetCustomerInfo?BVN="
                        + bvnNumber);

                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                try {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                    StringBuffer json = new StringBuffer(1024);
                    String tmp="";
                    while((tmp = reader.readLine())!=null)
                        json.append(tmp).append("\n");
                    reader.close();


                    JSONObject object = (JSONObject) new JSONTokener(json.toString()).nextValue();
                    phonenumber = object.getString("PhoneNumber");
                    firstname = object.getString("FirstName");
                    lastname = object.getString("LastName");
                    dob = object.getString("DOB");
                    bvn = object.getString("BVN");

                    return  true;
                }catch(Exception e) {

                    FirebaseCrashlytics.getInstance().recordException(new Exception(e.getMessage()));
                    return false;
                }
            }catch(Exception e){

                FirebaseCrashlytics.getInstance().recordException(new Exception(e.getMessage()));
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean response) {
            if(response == true){

                getBVN_dialog.dismiss();

                surnameEt.setText(lastname);
                surnameEt.setFocusable(false);

                firstNameEt.setText(firstname);
                firstNameEt.setFocusable(false);

                phoneNumberEt.setText(phonenumber);
                phoneNumberEt.setFocusable(false);

                dobTv.setText(dob);
                dobTv.setFocusable(false);

                bvnisfound = true;

            }else{

                bvn_progressbar.setVisibility(View.GONE);

                bvn_message.setVisibility(View.VISIBLE);
                bvn_message.setText("No BVN found");

            }
        }
    }

    private class syncAccount extends AsyncTask<String,String, String>{

        Context context;
        Account account;
        Gson gson;
        String tempnumber;

        public syncAccount(Context context, Account account,Gson gson){

            this.context = context;
            this.account = account;
            this.gson = gson;


        }

        @Override
        protected void onPreExecute() {

            gson = new Gson();

        }



        @Override
        protected String doInBackground(String... params) {

            String Token = LocalStorage.GetValueFor(AppConstants.API_TOKEN, getBaseContext());
            agentPhone = LocalStorage.GetValueFor(AppConstants.AGENT_PHONE, getBaseContext());
            agentPIN = LocalStorage.GetValueFor(AppConstants.AGENT_PIN, getBaseContext());
            institutionCode = LocalStorage.GetValueFor(AppConstants.INSTITUTION_CODE, getBaseContext());


            String url = "";//AppConstants.getAccessUrl() + "OpenAccountFlexCube?institutionCode="+institutionCode+"&agentPhoneNumber="+agentPhone+"&agentPin="+agentPIN;

            if (getPackageName().contains("creditclub"))
            {
                url = AppConstants.getAccessUrl() + "OpenAccountFlexCube?institutionCode="+institutionCode+"&agentPhoneNumber="+agentPhone+"&agentPin="+agentPIN;
            }
            else{
                url = AppConstants.getBaseUrl() + "/CreditClubMiddleWareAPI/api/Register?IsRetrial=false"; //"OpenAccountFlexCube?institutionCode="+institutionCode+"&agentPhoneNumber="+agentPhone+"&agentPin="+agentPIN;
            }

            String accountJson = gson.toJson(account);

            String response = APICaller.postRequest(OpenAccountActivity.this, url, accountJson, Token);


            ResponseBody serverResponse;
            try {

                serverResponse = gson.fromJson(response, ResponseBody.class);

                if(serverResponse.getStatus().equalsIgnoreCase("00")){

                    tempnumber = serverResponse.getAccountNumber();
                    return "Open customerRequest successful. \n Your customerRequest number is: " + serverResponse.getAccountNumber();

                }else if (serverResponse.getStatus() != null) {

                    FirebaseCrashlytics.getInstance().recordException(new Exception(serverResponse.getModelResponse().getResponseMessage()));
                    return serverResponse.getModelResponse().getResponseMessage();

                }else {

                    FirebaseCrashlytics.getInstance().recordException(new Exception(serverResponse.getModelResponse().getResponseMessage()));

                    return serverResponse.getModelResponse().getResponseMessage();
                }

            }catch (Exception ex){

                FirebaseCrashlytics.getInstance().recordException(new Exception(ex.getMessage()));
                return null;
            }
        }

        @Override
        protected void onPostExecute(String response) {

            // if the requests hits an exception
            if(response == null){
                progressbar.setVisibility(View.GONE);
                retry.setText("Retry");
                retry.setVisibility(View.VISIBLE);
                saveoffline.setVisibility(View.VISIBLE);
                cancel.setVisibility(View.VISIBLE);
                message.setText("An error has occurred");


                //if the customerRequest has been synced, it save to the database to check customerRequest status
            }else if(response.equalsIgnoreCase("Open customerRequest successful. \n Your customerRequest number is: " + tempnumber)){

                progressbar.setVisibility(View.GONE);
                message.setText(response);
                cancel.setText("Ok");
                cancel.setVisibility(View.VISIBLE);


                cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent i = new Intent(OpenAccountActivity.this,Menu3Activity.class);
                        startActivity(i);
                    }
                });


                // if the server throws any other response which is not successful
            }else{

                message.setText(response);
                progressbar.setVisibility(View.GONE);
                retry.setText("Retry");
                retry.setVisibility(View.VISIBLE);
                saveoffline.setVisibility(View.VISIBLE);
                cancel.setVisibility(View.VISIBLE);


            }
        }
    }
}

