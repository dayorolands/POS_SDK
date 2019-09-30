package com.appzonegroup.app.fasttrack;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import androidx.loader.content.CursorLoader;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.appzonegroup.app.fasttrack.dataaccess.BeneficiaryDAO;
import com.appzonegroup.app.fasttrack.model.Beneficiary;
import com.appzonegroup.app.fasttrack.model.ImageSource;
import com.appzonegroup.app.fasttrack.utility.ImageManipulations;
import com.crashlytics.android.Crashlytics;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by Oto-obong on 22/08/2017.
 */

public class BeneficiaryDetailActivity extends AppCompatActivity {

    static long id;
    static BeneficiaryDAO beneficiaryDAO;
    public static ImageView benficiaryImageView;
    static String beneficiaryImageString;
    static Bitmap sendBitmap;
    static Beneficiary beneficiary;
    TextView firstName, lastName, middleName, accountNumber, address, dob, eligibleAmount, gender, phoneNumber, trackingReference;
    private String Gender;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);



        beneficiaryDAO = new BeneficiaryDAO(getBaseContext());
        beneficiaryImageString = "";
        setContentView(R.layout.activity_beneficiary_detail);
        Intent i = getIntent();
        beneficiary = (Beneficiary) i.getSerializableExtra("beneficiary");

        firstName = (TextView)findViewById(R.id.beneficiary_firstName_tv);
        firstName.setText(beneficiary.getFirstName());

        lastName = (TextView)findViewById(R.id.beneficiary_lastName_tv);
        lastName.setText(beneficiary.getLastName());

        middleName = (TextView)findViewById(R.id.beneficiary_middleName_tv);
        middleName.setText(beneficiary.getMiddleName());

        accountNumber = (TextView)findViewById(R.id.benficiary_accountNumber_tv);
        accountNumber.setText(beneficiary.getAccountNumber());

        address = (TextView)findViewById(R.id.beneficiary_address_tv);
        address.setText(beneficiary.getAddress());

        dob = (TextView)findViewById(R.id.beneficiary_dob_tv);
        dob.setText(beneficiary.getDateOfBirth());

        eligibleAmount = (TextView)findViewById(R.id.beneficiary_eligibleAmount_tv);
        eligibleAmount.setText(String.valueOf(beneficiary.getEligibleAmount()));

        gender = (TextView)findViewById(R.id.beneficiary_gender_tv);
        if((String.valueOf(beneficiary.getGender()).equalsIgnoreCase("0"))){

          gender.setText("Male");

        }else{

            gender.setText("Female");
        }


        phoneNumber = (TextView)findViewById(R.id.beneficiary_phone_number_tv);
        phoneNumber.setText(beneficiary.getPhoneNumber());

        //trackingReference = (TextView)findViewById(R.id.beneficiary_trackingReference_tv);
        //trackingReference.setText(beneficiary.getTrackingReference());

        benficiaryImageView = (ImageView)findViewById(R.id.beneficiaryPhoto_imageView);

       // benficiaryImageView.setImageBitmap(ImageManipulations.StringToBitmap(beneficiary.getPhoto()));
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == ImageSource.BENEFICIARYCAMERA.ordinal()) {
                Bitmap thumbnail = (Bitmap) data.getExtras().get("data");
                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                thumbnail.compress(Bitmap.CompressFormat.JPEG, 90, bytes);

                sendBitmap = thumbnail;

                byte[] byteArray = bytes.toByteArray();
                beneficiary.setPhoto(Base64.encodeToString(byteArray, Base64.DEFAULT));
                beneficiaryImageString = (Base64.encodeToString(byteArray, Base64.DEFAULT));

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
                    Crashlytics.logException(new Exception(e.getMessage()));

                } catch (IOException e) {
                    e.printStackTrace();
                    Crashlytics.logException(new Exception(e.getMessage()));
                }

                benficiaryImageView.setImageBitmap(thumbnail);


            } else if(requestCode == ImageSource.BENEFICIARYGALERY.ordinal()){

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

                beneficiary.setPhoto(Base64.encodeToString(byteArray, Base64.DEFAULT));
                beneficiaryImageString = Base64.encodeToString(byteArray, Base64.DEFAULT);

                benficiaryImageView.setImageBitmap(bm);

            }
        }
    }

    public void BeneficiaryCamera_Click(View view){
        ImageManipulations.requestImage(this, ImageSource.BENEFICIARYCAMERA);
    }

    public void BeneficiaryGallery_Click(View view){
        ImageManipulations.requestImage(this, ImageSource.BENEFICIARYGALERY);
    }

    public void PayBeneficiary(View view){

        if(beneficiaryImageString.length()== 0){

            Toast.makeText(this, "Please provide a valid photo", Toast.LENGTH_SHORT).show();

            return;
        }



        final AlertDialog.Builder builder = new AlertDialog.Builder(BeneficiaryDetailActivity.this);
        View logout_dialog = getLayoutInflater().inflate(R.layout.beneficiary_pay_dialog, null);
        TextView dialog_header = (TextView) logout_dialog.findViewById(R.id.beneficiary_dialog_header);
        final TextView dialog_message = (TextView) logout_dialog.findViewById(R.id.beneficiary_dialog_message);
        final Button dialog_yes_button = (Button) logout_dialog.findViewById(R.id.beneficiary_dialog_yes_btn);
        final Button dialog_no_button = (Button) logout_dialog.findViewById(R.id.beneficiary_dialog_no_btn);
        final Button dialog_ok_button = (Button) logout_dialog.findViewById(R.id.beneficiary_dialog_ok_btn);

        dialog_message.setText("Make payment of N"+beneficiary.getEligibleAmount()+" to \n"+ beneficiary.getFirstName()+" "+beneficiary.getLastName());


        builder.setView(logout_dialog);
        final AlertDialog notification = builder.create();
        notification.show();
        notification.setCanceledOnTouchOutside(false);
        notification.setCancelable(false);

        dialog_no_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                notification.dismiss();
            }
        });

        dialog_yes_button.setOnClickListener(new View.OnClickListener(){


            @Override
            public void onClick(View v) {

                beneficiary.setSync("Pending");
                beneficiary.setPaid(true);
                try {
                      id = beneficiaryDAO.UpdateBeneficiary(beneficiary);

                    if(id > 0) {

                        dialog_message.setText("SUCCESSFUL TRANSACTION");
                        dialog_ok_button.setVisibility(View.VISIBLE);
                        dialog_yes_button.setVisibility(View.GONE);
                        dialog_no_button.setVisibility(View.GONE);


                    }else{

                        dialog_message.setText("TRANSACTION FAILED");
                        dialog_ok_button.setVisibility(View.VISIBLE);
                        dialog_yes_button.setVisibility(View.GONE);
                        dialog_no_button.setVisibility(View.GONE);

                    }


                }catch (Exception ex){

                    Log.e("Beneficiary Get all ", ex.getMessage());
                    Crashlytics.logException(new Exception(ex.getMessage()));
                }

            }
        });


             dialog_ok_button.setOnClickListener(new View.OnClickListener(){


            @Override
            public void onClick(View v) {

                    Intent i = new Intent(BeneficiaryDetailActivity.this, Menu3Activity.class);
                    startActivity(i);


            }
        });



    }
}
