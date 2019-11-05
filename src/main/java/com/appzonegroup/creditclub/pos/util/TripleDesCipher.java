package com.appzonegroup.creditclub.pos.util;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESedeKeySpec;

/**
 * Created by mac on 1/29/19.
 */

public class TripleDesCipher {
    SecretKey key;

    public TripleDesCipher(byte [] rawkey) throws Exception
    {
        key = readKey(rawkey);
    }

    public  SecretKey readKey(byte[] rawkey) throws Exception
    {
        byte[] keyDes = new byte[24];
        System.arraycopy(rawkey, 0, keyDes, 0, 16);
        System.arraycopy(rawkey, 0, keyDes, 16, 8);
        DESedeKeySpec keyspec = new DESedeKeySpec(keyDes);
        SecretKeyFactory keyfactory = SecretKeyFactory.getInstance("DESede");
        return keyfactory.generateSecret(keyspec);
    }

    public byte[] encrypt(byte[] plain ) throws Exception
    {
        Cipher cipher = Cipher.getInstance("DESede/ECB/NoPadding");
        //final IvParameterSpec iv = new IvParameterSpec(new byte[8]);
        cipher.init(Cipher.ENCRYPT_MODE, key);
        byte[] encrypted = cipher.doFinal(plain);
        return encrypted;
    }
    public byte[] decrypt(byte[] cipher ) throws Exception
    {
        Cipher dcipher = Cipher.getInstance("DESede/ECB/NoPadding");
        //final IvParameterSpec iv = new IvParameterSpec(new byte[8]);
        dcipher.init(Cipher.DECRYPT_MODE, key);
        byte[] decrypted = dcipher.doFinal(cipher);
        return decrypted;
    }
}
