package com.dspread.qpos.injectKey;//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//


import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.ShortBufferException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

public class TDES {
    public TDES() {
    }

    public static byte[] desEncypt(byte[] keyBytes, byte[] input) throws NoSuchAlgorithmException, NoSuchProviderException, NoSuchPaddingException, InvalidKeyException, ShortBufferException, IllegalBlockSizeException, BadPaddingException {
        SecretKeySpec key = new SecretKeySpec(keyBytes, "DES");
        Cipher cipher = Cipher.getInstance("DES/ECB/NoPadding", new BouncyCastleProvider());
        cipher.init(1, key);
        byte[] cipherText = new byte[cipher.getOutputSize(input.length)];
        int ctLength = cipher.update(input, 0, input.length, cipherText, 0);
        ctLength += cipher.doFinal(cipherText, ctLength);
        return cipherText;
    }

    public static byte[] desDecrypt(byte[] keyBytes, byte[] input) throws NoSuchAlgorithmException, NoSuchProviderException, NoSuchPaddingException, InvalidKeyException, ShortBufferException, IllegalBlockSizeException, BadPaddingException {
        SecretKeySpec key = new SecretKeySpec(keyBytes, "DES");
        Cipher cipher = Cipher.getInstance("DES/ECB/NoPadding", new BouncyCastleProvider());
        cipher.init(2, key);
        byte[] plainText = new byte[cipher.getOutputSize(input.length)];
        int ptLength = cipher.update(input, 0, input.length, plainText, 0);
        ptLength += cipher.doFinal(plainText, ptLength);
        return plainText;
    }

    public static byte[] tdesEncypt(byte[] keyBytes, byte[] input) throws NoSuchAlgorithmException, NoSuchProviderException, NoSuchPaddingException, InvalidKeyException, ShortBufferException, IllegalBlockSizeException, BadPaddingException {
        SecretKeySpec key = new SecretKeySpec(keyBytes, "DES");
        Cipher cipher = Cipher.getInstance("DESede/ECB/NoPadding", new BouncyCastleProvider());
        cipher.init(1, key);
        byte[] cipherText = new byte[cipher.getOutputSize(input.length)];
        int ctLength = cipher.update(input, 0, input.length, cipherText, 0);
        ctLength += cipher.doFinal(cipherText, ctLength);
        return cipherText;
    }

    public static byte[] tdesDecrypt(byte[] keyBytes, byte[] input) throws NoSuchAlgorithmException, NoSuchProviderException, NoSuchPaddingException, InvalidKeyException, ShortBufferException, IllegalBlockSizeException, BadPaddingException {
        SecretKeySpec key = new SecretKeySpec(keyBytes, "DES");
        Cipher cipher = Cipher.getInstance("DESede/ECB/NoPadding", new BouncyCastleProvider());
        cipher.init(2, key);
        byte[] plainText = new byte[cipher.getOutputSize(input.length)];
        int ptLength = cipher.update(input, 0, input.length, plainText, 0);
        ptLength += cipher.doFinal(plainText, ptLength);
        return plainText;
    }

    public static byte[] tdesCBCEncypt(byte[] keyBytes, byte[] input) throws NoSuchAlgorithmException, NoSuchProviderException, NoSuchPaddingException, InvalidKeyException, ShortBufferException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException {
        SecretKeySpec key = new SecretKeySpec(keyBytes, "DES");
        Cipher cipher = Cipher.getInstance("DESede/CBC/NoPadding", new BouncyCastleProvider());
        byte[] iv = new byte[8];
        cipher.init(1, key, new IvParameterSpec(iv));
        byte[] cipherText = new byte[cipher.getOutputSize(input.length)];
        cipherText = cipher.doFinal(input);
        return cipherText;
    }

    public static byte[] tdesCBCDecrypt(byte[] keyBytes, byte[] input) throws NoSuchAlgorithmException, NoSuchProviderException, NoSuchPaddingException, InvalidKeyException, ShortBufferException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException {
        SecretKeySpec key = new SecretKeySpec(keyBytes, "DES");
        Cipher cipher = Cipher.getInstance("DESede/CBC/NoPadding", new BouncyCastleProvider());
        byte[] iv = new byte[8];
        cipher.init(2, key, new IvParameterSpec(iv));
        byte[] plainText = new byte[cipher.getOutputSize(input.length)];
        int ptLength = cipher.update(input, 0, input.length, plainText, 0);
        ptLength += cipher.doFinal(plainText, ptLength);
        return plainText;
    }

    public static byte[] tdesECBEncypt(byte[] keyBytes, byte[] input) throws NoSuchAlgorithmException, NoSuchProviderException, NoSuchPaddingException, InvalidKeyException, ShortBufferException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException {
        SecretKeySpec key = new SecretKeySpec(keyBytes, "DES");
        Cipher cipher = Cipher.getInstance("DESede/ECB/NoPadding", new BouncyCastleProvider());
        cipher.init(1, key);
        byte[] cipherText = new byte[cipher.getOutputSize(input.length)];
        int ctLength = cipher.update(input, 0, input.length, cipherText, 0);
        ctLength += cipher.doFinal(cipherText, ctLength);
        return cipherText;
    }

    public static byte[] tdesECBDecrypt(byte[] keyBytes, byte[] input) throws NoSuchAlgorithmException, NoSuchProviderException, NoSuchPaddingException, InvalidKeyException, ShortBufferException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException {
        SecretKeySpec key = new SecretKeySpec(keyBytes, "DES");
        Cipher cipher = Cipher.getInstance("DESede/ECB/NoPadding", new BouncyCastleProvider());
        cipher.init(2, key);
        byte[] plainText = new byte[cipher.getOutputSize(input.length)];
        int ptLength = cipher.update(input, 0, input.length, plainText, 0);
        ptLength += cipher.doFinal(plainText, ptLength);
        return plainText;
    }

    public static void main(String[] args) {

        String key = "11111111111111111111111111111111";
        String block = "22CF7120896C9F800A81357B39F16280998742F7BC2D7005";
        byte[] tdeskeyBytes = parseHexStr2Byte(key);
        byte[] input = parseHexStr2Byte(block);

        try {
            byte[] bytes = tdesDecrypt(tdeskeyBytes,input);
            System.out.println(parseByte2HexStr(bytes));

        } catch (Exception var8) {
            var8.printStackTrace();
        }

    }


    // 十六进制字符串转字节数组
    public static byte[] parseHexStr2Byte(String hexStr) {
        if (hexStr.length() < 1)
            return null;
        byte[] result = new byte[hexStr.length() / 2];
        for (int i = 0; i < hexStr.length() / 2; i++) {
            int high = Integer.parseInt(hexStr.substring(i * 2, i * 2 + 1), 16);
            int low = Integer.parseInt(hexStr.substring(i * 2 + 1, i * 2 + 2),
                    16);
            result[i] = (byte) (high * 16 + low);
        }
        return result;
    }

    // 字节数组转十六进制字符串
    public static String parseByte2HexStr(byte buf[]) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < buf.length; i++) {
            String hex = Integer.toHexString(buf[i] & 0xFF);
            if (hex.length() == 1) {
                hex = '0' + hex;
            }
            sb.append(hex.toUpperCase());
        }
        return sb.toString();
    }



}
