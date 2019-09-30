package com.appzonegroup.app.fasttrack.utility;

import android.util.Log;

import com.appzonegroup.app.fasttrack.model.AppConstants;

import java.security.MessageDigest;
import java.security.spec.KeySpec;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESedeKeySpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import Decoder.BASE64Decoder;
import Decoder.BASE64Encoder;
import cz.msebera.android.httpclient.extras.Base64;

import static android.R.attr.key;

/**
 * Created by Oto-obong on 07/08/2017.
 */

public class TripleDES {
    public static String ALGO = "DESede/CBC/ISO10126";

    public static String encrypt(String clearText)// throws Exception
    {
        try {
            byte[] clear = clearText.getBytes("ASCII");
            byte[] key = new BASE64Decoder().decodeBuffer(AppConstants.getEncryptionKey());// AppConstants.getEncryptionKey().toUpperCase().getBytes("UTF-8");
            SecretKeySpec skeySpec = new SecretKeySpec(key, "AES");
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
            byte[] encrypted = cipher.doFinal(clear);

            byte[] base64Bytes = Base64.encode(encrypted, Base64.DEFAULT);
            String base64EncryptedString = new String(base64Bytes).replace("\n", "");
            return base64EncryptedString;
        } catch (Exception ex)
        {
            return null;
        }
    }

    /*public static String HMACSHA256Encrypt(String data){
        try {

            Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
            SecretKeySpec secret_key = new SecretKeySpec(AppConstants.getEncryptionKey().toUpperCase().getBytes("UTF-8"), "HmacSHA256");
            sha256_HMAC.init(secret_key);

            return new BASE64Encoder().encode(sha256_HMAC.doFinal(data.getBytes("UTF-8")));
        }
        catch (Exception ex)
        {
            return "Error";
        }
    }
*/
    /*public static String encrypt2(String message, String secretKey)
    {
        try
        {
            // Create an array to hold the key
            byte[] encryptKey = secretKey == null ? AppConstants.getEncryptionKey().getBytes() : secretKey.getBytes();

            // Create a DESede key spec from the key
            DESedeKeySpec spec = new DESedeKeySpec(encryptKey);

            // Get the secret key factor for generating DESede keys
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DESede");

            // Generate a DESede SecretKey object
            SecretKey theKey = keyFactory.generateSecret(spec);

            // Create a DESede Cipher
            Cipher cipher = Cipher.getInstance(ALGO);

            // Create an initialization vector (necessary for CBC mode)

            IvParameterSpec IvParameters = new IvParameterSpec(new byte[8] );//{ 12, 34, 56, 78, 90, 87, 65, 43 });

            // Initialize the cipher and put it into encrypt mode
            cipher.init(Cipher.ENCRYPT_MODE, theKey);//, IvParameters);

            byte[] plaintext = message.getBytes();

            // Encrypt the data
            byte[] encrypted = cipher.doFinal(plaintext);

            byte[] base64Bytes = Base64.encode(encrypted, Base64.DEFAULT);
            return new String(base64Bytes);
            //return base64EncryptedString;
        }
        catch (Exception exc)
        {
            exc.printStackTrace();
            return null;
        }
    }*/

    public static String encrypt(String message, String secretKey)// throws Exception
    {
        try
        {
            if (secretKey == null)
                secretKey = AppConstants.getEncryptionKey();

            Cipher cipher = Cipher.getInstance(ALGO);
            cipher.init(Cipher.ENCRYPT_MODE, getSecreteKey(secretKey));

            byte[] plainTextBytes = message.getBytes("UTF-8");
            byte[] buf = cipher.doFinal(plainTextBytes);
            byte[] base64Bytes = Base64.encode(buf, Base64.DEFAULT);
            String base64EncryptedString = new String(base64Bytes);
            return base64EncryptedString;
        }catch (Exception ex)
        {
            Log.e("Error", ex.getMessage());
            return null;
        }
    }

    public static SecretKey getSecreteKey(String secretKey) throws Exception {
        MessageDigest md = MessageDigest.getInstance("DESede");
        byte[] digestOfPassword = md.digest(secretKey.getBytes("utf-8"));
        byte[] keyBytes = Arrays.copyOf(digestOfPassword, 8);
        SecretKey key = new SecretKeySpec(keyBytes, "DESede");
        return key;
    }

    public static String decrypt(String message) throws Exception {

        byte[] bytemessage = Base64.decode(message.getBytes(), Base64.DEFAULT);

        final MessageDigest md = MessageDigest.getInstance("md5");
        final byte[] digestOfPassword = md.digest("CIC9XRNBWPDAYQFEVKEWAZMVHXHBZCIU".getBytes("utf-8"));
        final byte[] keyBytes = Arrays.copyOf(digestOfPassword, 24);
        for (int j = 0, k = 16; j < 8;) {
            keyBytes[k++] = keyBytes[j++];
        }

        final SecretKey key = new SecretKeySpec(keyBytes, "DESede");
        final IvParameterSpec iv = new IvParameterSpec(new byte[8]);
        final Cipher decipher = Cipher.getInstance("DESede/CBC/PKCS5Padding");
        decipher.init(Cipher.DECRYPT_MODE, key, iv);

        // final byte[] encData = new
        // sun.misc.BASE64Decoder().decodeBuffer(message);
        final byte[] plainText = decipher.doFinal(bytemessage);

        return new String(plainText, "UTF-8");
    }
}
