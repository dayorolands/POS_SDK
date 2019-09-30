package com.appzonegroup.app.fasttrack.utility;

import android.util.Base64;

import java.security.MessageDigest;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by Joseph on 1/5/2018.
 */

public class TT {

    //    public static String ALGO = "DESede/CBC/PKCS7Padding";
    /*public static String ALGO = "DESede/CBC/PKCS5Padding";

    public static String _encrypt(String message, String secretKey) throws Exception {

        Cipher cipher = Cipher.getInstance(ALGO);
        cipher.init(Cipher.ENCRYPT_MODE, getSecreteKey(secretKey));
        byte[] plainTextBytes = message.getBytes("UTF-8");
        byte[] buf = cipher.doFinal(plainTextBytes);
        byte[] base64Bytes = org.apache.commons.codec.binary.Base64.encodeBase64(buf);//, Base64.DEFAULT);
        String base64EncryptedString = new String(base64Bytes);
        return base64EncryptedString;
    }

    public static SecretKey getSecreteKey(String secretKey) throws Exception {
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] digestOfPassword = md.digest(secretKey.getBytes("utf-16LE"));
        //byte[] keyBytes = Arrays.copyOf(digestOfPassword, 16);
        SecretKey key = new SecretKeySpec(digestOfPassword, "DESede");
        return key;
    }

    public static String _decrypt(String encryptedText, String secretKey) throws Exception {

        byte[] message = Base64.decode(encryptedText.getBytes(), Base64.DEFAULT);

        Cipher decipher = Cipher.getInstance(ALGO);
        decipher.init(Cipher.DECRYPT_MODE, getSecreteKey(secretKey));

        byte[] plainText = decipher.doFinal(message);

        return new String(plainText, "UTF-8");
    }
*/
}
