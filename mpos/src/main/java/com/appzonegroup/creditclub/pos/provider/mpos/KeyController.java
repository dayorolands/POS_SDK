package com.appzonegroup.creditclub.pos.provider.mpos;

import android.annotation.SuppressLint;
import android.util.Log;

import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class KeyController {
    private static final String ALGORITHM_AES = "AES";
    private static final String ALGORITHM_AES_CBC_NOPADDING = "AES/CBC/NoPadding";
    private static final String ALGORITHM_RSA = "RSA";
    private static final String ALGORITHM_RSA_ECB_PKCS1PADDING = "RSA/ECB/PKCS1Padding";
    private static final int KEY_SIZE = 2048;
    private static KeyController keyController;
    private String privateKeyEncoded = null;

    public static KeyController getInstance() {
        if (keyController == null) {
            keyController = new KeyController();
        }
        return keyController;
    }

    private KeyController() {
    }

    private Key generateKey(byte[] passwordValueByteArray) throws Exception {
        return new SecretKeySpec(passwordValueByteArray, ALGORITHM_AES);
    }

    public String getPrivateKeyEncoded() {
        return this.privateKeyEncoded;
    }

    @SuppressLint({"NewApi"})
    public String decryptWithPrivateKey2(byte[] encryptedValue, String encodedPrivateKey) {
        try {
            PrivateKey privateKey = KeyFactory.getInstance(ALGORITHM_RSA).generatePrivate(new PKCS8EncodedKeySpec(Base64.decode(encodedPrivateKey, 0)));
            Cipher cipher = Cipher.getInstance(ALGORITHM_RSA_ECB_PKCS1PADDING);
            cipher.init(2, privateKey);
            return new String(cipher.doFinal(encryptedValue));
        } catch (Exception e) {
            Log.e("Exception {}", e.getStackTrace().toString());
            return null;
        }
    }

    public String generatePrivateAndPublicKeys() {
        String publicKeyEncoded = null;
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(ALGORITHM_RSA);
            keyPairGenerator.initialize(2048);
            KeyPair keyPair = keyPairGenerator.genKeyPair();
            android.util.Base64.encodeToString(keyPair.getPublic().getEncoded(), 0);
            this.privateKeyEncoded = new String(keyPair.getPrivate().getEncoded());
            byte[] modulus = ((RSAPublicKey) keyPair.getPublic()).getModulus().toByteArray();
            byte[] publicKey = new byte[(modulus.length - 1)];
            System.arraycopy(modulus, 1, publicKey, 0, publicKey.length);
            return byteToHex(publicKey);
        } catch (Exception e) {
            return publicKeyEncoded;
        }
    }

    private String byteToHex(byte[] bytes) {
        String str;
        String str2 = "";
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            String strHex = Integer.toHexString(b & 255);
            if (strHex.length() == 1) {
                str = "0" + strHex;
            } else {
                str = strHex;
            }
            sb.append(str);
        }
        return sb.toString().trim();
    }

    private String ttkey() {
        try {
            KeyPairGenerator gen = KeyPairGenerator.getInstance(ALGORITHM_RSA);
            gen.initialize(2048);
            byte[] modulus = ((RSAPublicKey) gen.genKeyPair().getPublic()).getModulus().toByteArray();
            byte[] publicKey = new byte[(modulus.length - 1)];
            System.arraycopy(modulus, 1, publicKey, 0, publicKey.length);
            return byteToHex(publicKey);
        } catch (Exception e) {
            return "";
        }
    }
}
