package com.appzonegroup.app.fasttrack.utility;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.provider.MediaStore;
import androidx.annotation.Nullable;
import android.util.Base64;

import com.appzonegroup.app.fasttrack.model.ImageSource;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by Joseph on 3/14/2016.
 */
public class ImageManipulations {

    public static void selectImage(final Activity activity) {
        final int REQUEST_CAMERA = 3, SELECT_FILE = 4;
        final CharSequence[] items = { "Take Photo", "Choose from Library", "Cancel" };
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("Photo!");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (items[item].equals("Take Photo")) {
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    activity.startActivityForResult(intent, REQUEST_CAMERA);
                } else if (items[item].equals("Choose from Library")) {
                    Intent intent = new Intent(
                            Intent.ACTION_PICK,
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    intent.setType("image/*");
                    activity.startActivityForResult(
                            Intent.createChooser(intent, "Select File"), SELECT_FILE);
                } else if (items[item].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    public static File bitmapToFile(Bitmap bitmap, Context context) {
        try {
            File f = new File(context.getCacheDir(), "Send file.png");
            f.createNewFile();

            //Convert bitmap to byte array
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 0 /*ignored for PNG*/, bos);
            byte[] bitmapdata = bos.toByteArray();

            //write the bytes in file
            FileOutputStream fos = new FileOutputStream(f);
            fos.write(bitmapdata);
            fos.flush();
            fos.close();
            return f;
        } catch (IOException ioException) {
            return null;
        }
    }

    public static void requestImage(Activity activity, ImageSource imageSource){

        switch (imageSource)
        {
            case CAMERA:{
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                activity.startActivityForResult(intent, imageSource.ordinal());
                break;
            }
            case GALLERY:{
                Intent intent = new Intent(
                        Intent.ACTION_PICK,
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                intent.setType("image/*");
                activity.startActivityForResult(
                        Intent.createChooser(intent, "Select File"), imageSource.ordinal());
                break;
            }
            case IDENTITYCAMERA:{

                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    activity.startActivityForResult(intent, imageSource.ordinal());
                    break;
            }
            case IDENTITYGALLERY:{

                Intent intent = new Intent(
                        Intent.ACTION_PICK,
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                intent.setType("image/*");
                activity.startActivityForResult(
                        Intent.createChooser(intent, "Select File"), imageSource.ordinal());
                break;
            }
            case PASSPORTCAMERA:{

                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                activity.startActivityForResult(intent, imageSource.ordinal());
                break;
            }
            case PASSPORTGALLERY:{

                Intent intent = new Intent(
                        Intent.ACTION_PICK,
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                intent.setType("image/*");
                activity.startActivityForResult(
                        Intent.createChooser(intent, "Select File"), imageSource.ordinal());
                break;

            }
            case SIGNATURECAMERA:{

                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                activity.startActivityForResult(intent, imageSource.ordinal());
                break;

            }
            case SIGNATUREGALLERY:{

                Intent intent = new Intent(
                        Intent.ACTION_PICK,
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                intent.setType("image/*");
                activity.startActivityForResult(
                        Intent.createChooser(intent, "Select File"), imageSource.ordinal());
                break;
            }
            case BENEFICIARYCAMERA:{

                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                activity.startActivityForResult(intent, imageSource.ordinal());
                break;

            }

            case BENEFICIARYGALERY:{

                Intent intent = new Intent(
                        Intent.ACTION_PICK,
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                intent.setType("image/*");
                activity.startActivityForResult(
                        Intent.createChooser(intent, "Select File"), imageSource.ordinal());
                break;
            }
            default:{


            }
        }

    }

    /**
     * @param encodedString
     * @return bitmap (from given string)
     */
    @Nullable
    public static Bitmap StringToBitmap(String encodedString){
        try {
            byte [] encodeByte = Base64.decode(encodedString,Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);
            return bitmap;
        } catch(Exception e) {
            e.getMessage();
            return null;
        }
    }
}
