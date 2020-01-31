package com.appzonegroup.app.fasttrack.fragment.online;


import android.app.Activity;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import androidx.loader.content.CursorLoader;
import androidx.core.content.FileProvider;
import androidx.appcompat.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.appzonegroup.app.fasttrack.BankOneApplication;
import com.appzonegroup.app.fasttrack.OnlineActivity;
import com.appzonegroup.app.fasttrack.R;
import com.appzonegroup.app.fasttrack.model.TransactionCountType;
import com.appzonegroup.app.fasttrack.model.online.AuthResponse;
import com.appzonegroup.app.fasttrack.model.online.Response;
import com.appzonegroup.app.fasttrack.network.online.APIHelper;
import com.appzonegroup.app.fasttrack.utility.GPSTracker;
import com.appzonegroup.app.fasttrack.utility.LocalStorage;
import com.appzonegroup.app.fasttrack.utility.Misc;
import com.creditclub.core.data.Encryption;
import com.appzonegroup.app.fasttrack.utility.online.ErrorMessages;
import com.appzonegroup.app.fasttrack.utility.online.ImageUtils;
import com.appzonegroup.app.fasttrack.utility.online.XmlToJson;
import com.crashlytics.android.Crashlytics;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeoutException;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * A simple {@link Fragment} subclass.
 */
public class CustomerImageFragment extends Fragment implements View.OnClickListener {


    @Bind(R.id.btnActivate)
    Button next;

    @Bind(R.id.gallery)
    Button fromGallery;

    @Bind(R.id.takePhoto)
    Button takePhoto;

    @Bind(R.id.upperHint)
    TextView upperHint;

    @Bind(R.id.image)
    ImageView imageView;

    BankOneApplication bankOneApplication;


    private static final int TAKE_PHOTO = 412341;
    private static final int PICK_PHOTO = 412342;

    private Uri mPhotoUri;

    AlertDialog.Builder dialog;

    private static EnterDetailFragment.OptionsText optionsText;

    public static CustomerImageFragment instantiate(JSONObject data) {
        setOptionsText(new EnterDetailFragment.OptionsText(data));
        return new CustomerImageFragment();
    }

    public CustomerImageFragment() {
        // Required empty public constructor
    }

    public static EnterDetailFragment.OptionsText getOptionsText() {
        return optionsText;
    }

    public static void setOptionsText(EnterDetailFragment.OptionsText optionsText) {
        CustomerImageFragment.optionsText = optionsText;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_customer_image, container, false);
        OnlineActivity.isHome = false;
        bankOneApplication = (BankOneApplication)getActivity().getApplication();
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        dialog = new AlertDialog.Builder(getActivity()).setPositiveButton("OK", null);
        upperHint.setText(getOptionsText() == null ? "" : getOptionsText().getHintText());
        fromGallery.setOnClickListener(this);
        takePhoto.setOnClickListener(this);
        next.setOnClickListener(this);
        view = getActivity().getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    public File getAlbumStorageDir(String albumName) {
        // Get the directory for the user's public pictures directory.
        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), albumName);
        if (!file.mkdirs()) {
            Log.e("TakePicture", "Directory not created");
        }
        return file;
    }

    String finalLocation;
    String mCurrentPhotoPath;

    private File createImageFile() {
        // Create an image file name
        try {
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String imageFileName = "JPEG_" + timeStamp + "_";
            File storageDir = getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            File image = File.createTempFile(
                    imageFileName,  /* prefix */
                    ".jpg",         /* suffix */
                    storageDir      /* directory */
            );

            // Save a file: path for use with ACTION_VIEW intents
            mCurrentPhotoPath = image.getAbsolutePath();
            return image;
        }catch (Exception ex)
        {
            return null;
        }
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (Exception ex) {
                // Error occurred while creating the File
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                //Uri photoURI
                mPhotoUri = FileProvider.getUriForFile(getActivity(),
                        "com.android.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, mPhotoUri);
                startActivityForResult(takePictureIntent, TAKE_PHOTO);
            }
        }
    }

    @Override
    public void onClick(View view) {

        try {

            if (view == fromGallery) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, PICK_PHOTO);
            } else if (view == takePhoto) {

                //dispatchTakePictureIntent();

                AuthResponse ar = LocalStorage.getCachedAuthResponse(getActivity());
                File f = new File(getAlbumStorageDir("Download"), "Picture_" + ar.getPhoneNumber() + ".jpg");
                mPhotoUri = Uri.fromFile(f);
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, mPhotoUri);
                startActivityForResult(intent, TAKE_PHOTO);

                /*Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
                    startActivityForResult(takePictureIntent, TAKE_PHOTO);
                }*/
            } else {
                final ProgressDialog pdialog = new ProgressDialog(getActivity());
                pdialog.setCancelable(false);
                pdialog.setMessage("Loading...");
                pdialog.show();
                if (getImage() != null) {
                    final APIHelper ah = new APIHelper(getActivity());
                    //final CacheHelper ch = new CacheHelper(getActivity());
                    finalLocation = "0.00;0.00";
                    final GPSTracker gpsTracker = new GPSTracker(getActivity());
                    if (gpsTracker.getLocation() != null) {
                        Log.e("CangetLocation", "NULL");
                        String longitude = String.valueOf(gpsTracker.getLocation().getLongitude());
                        String latitude = String.valueOf(gpsTracker.getLocation().getLatitude());
                        finalLocation = latitude + ";" + longitude;
                        Log.e("Location", finalLocation);
                    }

                    final AsyncTask<Void, Void, Void> asyncTask = new AsyncTask<Void, Void, Void>() {
                        File finalFile;

                        @Override
                        protected Void doInBackground(Void... voids) {
                            finalFile = convertBitmapToFile(getImage());
                            return null;
                        }

                        @Override
                        protected void onPostExecute(Void aVoid) {
                            super.onPostExecute(aVoid);
                            //final AuthResponse authResponse = LocalStorage.getCachedAuthResponse(getActivity());
                            final AuthResponse authResponse = ((BankOneApplication)getActivity().getApplication()).getAuthResponse();
                            ah.getNextOperationImage(authResponse.getPhoneNumber(), authResponse.getSessionId(), finalFile, finalLocation, getOptionsText().isShouldCompress(),
                                    (e, result) -> {
                                        pdialog.dismiss();
                                        if (e == null && result != null) {
                                            try {
                                                String answer = Response.fixResponse(result);
                                                Log.e("Answer", answer);
                                                Log.e("AnswerLength", answer.length() + "");
                                                if (TextUtils.isEmpty(answer.trim())) {
                                                    Toast.makeText(getActivity(), "Upload successful!", Toast.LENGTH_SHORT).show();
                                                    Misc.increaseTransactionMonitorCounter(getActivity(), TransactionCountType.SUCCESS_COUNT, authResponse.getSessionId());
                                                    moveToNext();
                                                }
                                            } catch (Exception c) {
                                                c.printStackTrace();
                                                showDialogWithGoHomeAction("Something went wrong! Please try again.");
                                                Misc.increaseTransactionMonitorCounter(getActivity(), TransactionCountType.NO_RESPONSE_COUNT, authResponse.getSessionId());
                                            }
                                        } else {
                                            if (e != null) {
                                                Log.e("ErrorResponse", e.toString());
                                                e.printStackTrace();
                                                if (e instanceof TimeoutException) {
                                                    showDialogWithGoHomeAction("Something went wrong! Please try again.");
                                                    Misc.increaseTransactionMonitorCounter(getActivity(), TransactionCountType.NO_RESPONSE_COUNT, authResponse.getSessionId());
                                                } else {
                                                    Misc.increaseTransactionMonitorCounter(getActivity(), TransactionCountType.NO_INTERNET_COUNT, authResponse.getSessionId());
                                                    dialog.setMessage("Connection lost").setPositiveButton("OK", null).show();
                                                }
                                            } else {
                                                dialog.setMessage("Connection lost").setPositiveButton("OK", null).show();
                                                Misc.increaseTransactionMonitorCounter(getActivity(), TransactionCountType.NO_INTERNET_COUNT, authResponse.getSessionId());
                                            }
                                        }

                                    });
                        }
                    };
                    asyncTask.execute();
                } else {
                    pdialog.dismiss();
                    Toast.makeText(getActivity(), "Please select or take a picture!", Toast.LENGTH_SHORT).show();
                }
            }
        }catch (Exception ex)
        {
            Toast.makeText(getActivity(), "An error just occurred. Please try again later.", Toast.LENGTH_LONG).show();
        }
    }

    void showDialogWithGoHomeAction(String message)
    {
        dialog.setMessage(message)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        ((OnlineActivity) getActivity()).goHome();
                    }
                }).setCancelable(false)
                .show();
    }

    public void setmPhotoUri(Uri mPhotoUri) {
        this.mPhotoUri = mPhotoUri;
    }

    public Uri getmPhotoUri() {
        return mPhotoUri;
    }

    static boolean state = false;

    private void moveToNext() {
        final ProgressDialog pdialog = new ProgressDialog(getActivity());
        pdialog.setCancelable(false);
        pdialog.setMessage("Loading...");
        pdialog.show();
        //final AuthResponse authResponse = bankOneApplication.getAuthResponse();// LocalStorage.getCachedAuthResponse(getActivity());
        final AuthResponse authResponse = ((BankOneApplication)getActivity().getApplication()).getAuthResponse();
        APIHelper ah = new APIHelper(getActivity());

        String finalLocation = "0.00;0.00";
        final GPSTracker gpsTracker = new GPSTracker(getActivity());
        if (gpsTracker.getLocation() != null) {
            String longitude = String.valueOf(gpsTracker.getLocation().getLongitude());
            String latitude = String.valueOf(gpsTracker.getLocation().getLatitude());
            finalLocation = latitude + ";" + longitude;
        }
        ah.getNextOperation(authResponse.getPhoneNumber(), authResponse.getSessionId(), getImageFile().getAbsolutePath(), finalLocation,
                new APIHelper.VolleyCallback<String>() {
                    @Override
                    public void onCompleted(Exception e, String result,boolean status) {
                        pdialog.dismiss();
                        if (status) {
                            try {
                                String answer = Response.fixResponse(result);
                                Log.e("DECRYPTED", Encryption.decrypt(answer));
                                String decryptedAnswer = Encryption.decrypt(answer);
                                JSONObject response = XmlToJson.convertXmlToJson(decryptedAnswer);
                                if (response == null) {
                                    dialog.setMessage("Connection lost").setPositiveButton("OK", null).show();
                                    Misc.increaseTransactionMonitorCounter(getActivity(), TransactionCountType.NO_INTERNET_COUNT, authResponse.getSessionId());
                                } else {
                                    String resp = response.toString();
                                    Log.e("ResponseJsonText", resp);
                                    JSONObject response_base = response.getJSONObject("Response");
                                    if (response_base != null) {
                                        int shouldClose = response_base.optInt("ShouldClose", 1);
                                        if (shouldClose == 0) {
                                            if (resp.contains("IN-CORRECT ACTIVATION CODE") && state == true) {
                                                Log.e("Case", "Incorrect activation code||Deleted cache auth");
                                                LocalStorage.deleteCacheAuth(getActivity());
                                            } else if (state) {
                                                Log.e("Case", "correct activation code||" + getImageFile().getAbsolutePath());
                                                JSONObject auth = new JSONObject();
                                                auth.put("phone_number", authResponse.getPhoneNumber());
                                                auth.put("session_id", authResponse.getSessionId());
                                                auth.put("activation_code", getImageFile().getAbsolutePath());
                                                LocalStorage.saveCacheAuth(auth.toString(), getActivity());
                                            }

                                            Misc.increaseTransactionMonitorCounter(getActivity(), TransactionCountType.SUCCESS_COUNT, authResponse.getSessionId());
                                            if (resp.contains("MenuItem")) {
                                                JSONObject menuWrapper = response_base.getJSONObject("Menu").getJSONObject("Response").getJSONObject("Display");
                                                getFragmentManager().beginTransaction().replace(R.id.container, ListOptionsFragment.instantiate(menuWrapper, false)).commit();
                                            } else {
                                                Object menuWrapper = response_base.getJSONObject("Menu").getJSONObject("Response").get("Display");
                                                if (menuWrapper instanceof String && resp.contains("ShouldMask") && !resp.contains("Invalid Response")) {
                                                    JSONObject data = response_base.getJSONObject("Menu").getJSONObject("Response");
                                                    if (resp.contains("\"IsImage\":true")) {
                                                        getFragmentManager().beginTransaction().replace(R.id.container, CustomerImageFragment.instantiate(data)).commit();
                                                    } else {
                                                        getFragmentManager().beginTransaction().replace(R.id.container, EnterDetailFragment.instantiate(data)).commit();
                                                    }
                                                } else {
                                                    String message = response_base.getJSONObject("Menu").getJSONObject("Response").getString("Display");
                                                    dialog.setMessage(message).show();
                                                }
                                            }
                                        } else {
                                            Misc.increaseTransactionMonitorCounter(getActivity(), TransactionCountType.ERROR_RESPONSE_COUNT, authResponse.getSessionId());
                                            if (response_base.toString().contains("Display")) {
                                                showDialogWithGoHomeAction(response_base.getJSONObject("Menu")
                                                        .getJSONObject("Response")
                                                        .optString("Display", ErrorMessages.OPERATION_NOT_COMPLETED));
                                            } else {
                                                dialog.setMessage(response_base.optString("Menu", ErrorMessages.PHONE_NOT_REGISTERED)).show();
                                            }
                                        }
                                    }

                                }
                            } catch (Exception c) {
                                showDialogWithGoHomeAction("Something went wrong! Please try again.");
                                Misc.increaseTransactionMonitorCounter(getActivity(), TransactionCountType.ERROR_RESPONSE_COUNT, authResponse.getSessionId());
                            }
                        } else {
                            if (e != null) {
                                if (e instanceof TimeoutException) {
                                    showDialogWithGoHomeAction("Something went wrong! Please try again.");
                                    Misc.increaseTransactionMonitorCounter(getActivity(), TransactionCountType.NO_INTERNET_COUNT, authResponse.getSessionId());
                                } else {
                                    dialog.setMessage("Connection lost").setPositiveButton("OK", null).show();
                                    Misc.increaseTransactionMonitorCounter(getActivity(), TransactionCountType.ERROR_RESPONSE_COUNT, authResponse.getSessionId());
                                }
                            } else {
                                dialog.setMessage("Connection lost").setPositiveButton("OK", null).show();
                                Misc.increaseTransactionMonitorCounter(getActivity(), TransactionCountType.ERROR_RESPONSE_COUNT, authResponse.getSessionId());
                            }
                        }

                    }
                });
    }

    void updateImage(Uri imageUri)
    {
        try {
            Bitmap bm = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), imageUri);
            setImage(bm);
            imageView.setImageBitmap(bm);
        }catch (Exception ex)
        {

        }
        String[] filePathColumn = {MediaStore.Images.Media.DATA};
        Cursor cursor = getActivity().getContentResolver().query(imageUri, filePathColumn, null, null, null);
        cursor.moveToFirst();

        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
        String picturePath = cursor.getString(columnIndex);
        setImageFile(new File(picturePath));
        cursor.close();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == TAKE_PHOTO) {
            if (resultCode == Activity.RESULT_OK) {

                /*Bundle extras = data.getExtras();
                Bitmap imageBitmap = (Bitmap) extras.get("data");
                setImage(imageBitmap);
                imageView.setImageBitmap(imageBitmap);*/

                File f = new File(URI.create(mPhotoUri.toString()));
                setImageFile(f);
                if (f.exists()) {
                    //updateImage(Uri.fromFile(f));
                    Uri uri = Uri.fromFile(f);
                    updateImage(uri, TAKE_PHOTO);
                }
            }
        } else if (requestCode == PICK_PHOTO) {
            if (resultCode == Activity.RESULT_OK) {
                Uri selectedImage = data.getData();

                updateImage(selectedImage);
                    /*Bitmap bm = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), selectedImage);
                    setImage(bm);
                    imageView.setImageBitmap(bm);*/

                /*setImage(uriToBitmap(getActivity(), selectedImage));
                imageView.setImageBitmap(getImage());
                String[] filePathColumn = {MediaStore.Images.Media.DATA};

                Cursor cursor = getActivity().getContentResolver().query(selectedImage,
                        filePathColumn, null, null, null);
                cursor.moveToFirst();

                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                String picturePath = cursor.getString(columnIndex);
                setImageFile(new File(picturePath));
                cursor.close();
                updateImage(Uri.parse(picturePath), PICK_PHOTO);*/
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void updateImage(Uri uri, int flag) {
        if (uri != null) {
            Bitmap bitmap = null;
            if (flag == TAKE_PHOTO) {
                File f = new File(URI.create(uri.toString()));
                try {
                    bitmap = BitmapFactory.decodeFile(f.getAbsolutePath());// ImageUtils.getProcessedBitmap(f.getAbsolutePath(), getOptionsText().isShouldCompress());
                }catch (OutOfMemoryError ex)
                {
                    Log.e("MemoryError", ex.toString());
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inSampleSize = 2;
                    bitmap = BitmapFactory.decodeFile(f.getAbsolutePath(), options);
                }
                catch (Exception ex)
                {
                    Log.e("Error", "Some other error: " + ex.toString());
                }
            } else {
                bitmap = uriToBitmap(getActivity(), uri);
                //ImageUtils.getProcessedBitmap(uri.getPath());// BitmapFactory.decodeFile(uri.toString());
            }
            setImage(bitmap);
            imageView.setImageBitmap(bitmap);
        } else {
            imageView.setImageResource(R.drawable.ic_default);
        }
    }

    Bitmap uriToBitmap(Context context, Uri imageUri)
    {
        String[] projection = {MediaStore.MediaColumns.DATA};
        CursorLoader cursorLoader = new CursorLoader(context, imageUri, projection, null, null, null);
        Cursor cursor = cursorLoader.loadInBackground();
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
        cursor.moveToFirst();

        String selectedImagePath = cursor.getString(column_index);

        Bitmap bm;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(selectedImagePath, options);
        final int REQUIRED_SIZE = 200;
        int scale = 1;
        if (getOptionsText().isShouldCompress()) {
            while (options.outWidth / scale / 2 >= REQUIRED_SIZE && options.outHeight / scale / 2 >= REQUIRED_SIZE)
                scale *= 2;
            options.inSampleSize = scale;
            options.inJustDecodeBounds = false;
        }
        bm = BitmapFactory.decodeFile(selectedImagePath, options);

        //Convert bitmap to string
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        //byte[] byteArray = bytes.toByteArray();
        return bm;
    }

    private Bitmap image;
    private File imageFile;

    private File convertBitmapToFile(Bitmap bitmap) {
        try {
            File file = new File(imageFile.getAbsolutePath());//getAlbumStorageDir("Download"), imageFile.getName());//"Picture_" + UUID.randomUUID().toString() + ".jpg");
            Log.e("Compress:::", String.valueOf(getOptionsText().isShouldCompress()));

            if (getOptionsText().isShouldCompress()) {
                FileOutputStream fOut = new FileOutputStream(file);
                //Change image size and compress quality  target : 15kb
                Bitmap newBitmap = ImageUtils.getResizedBitmap(bitmap, 400);
                newBitmap.compress(Bitmap.CompressFormat.PNG, 100, fOut);
                fOut.flush();
                fOut.close();
            }
            //Log.e("FileSize", String.valueOf(Float.parseFloat(file.length()+"")/1024f) + "");
            return file;
        } catch (Exception e) {
            Crashlytics.logException(e);
            e.printStackTrace();
            Log.e("Image", "Save file error!" + e.toString());
            return null;
        }
    }

    public Bitmap getImage() {
        return image;
    }

    public void setImage(Bitmap image) {
        this.image = image;
    }

    public File getImageFile() {
        return imageFile;
    }

    public void setImageFile(File imageFile) {
        this.imageFile = imageFile;
    }
}
