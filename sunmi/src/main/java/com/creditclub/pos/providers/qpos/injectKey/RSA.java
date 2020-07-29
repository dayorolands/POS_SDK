package com.appzonegroup.creditclub.pos.provider.qpos.injectKey;//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import Decoder.BASE64Decoder;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.security.spec.X509EncodedKeySpec;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;


public class RSA {
    public static final String SIGN_ALGORITHMS = "SHA1WithRSA";
    private static final String DEFAULT_PUBLIC_KEY = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQChDzcjw/rWgFwnxunbKp7/4e8w\r/UmXx2jk6qEEn69t6N2R1i/LmcyDT1xr/T2AHGOiXNQ5V8W4iCaaeNawi7aJaRht\rVx1uOH/2U378fscEESEG8XDqll0GCfB1/TjKI2aitVSzXOtRs8kYgGU78f7VmDNg\rXIlk3gdhnzh+uoEQywIDAQAB\r";
    private static final String DEFAULT_PRIVATE_KEY = "MIICdQIBADANBgkqhkiG9w0BAQEFAASCAl8wggJbAgEAAoGBAKEPNyPD+taAXCfG\r6dsqnv/h7zD9SZfHaOTqoQSfr23o3ZHWL8uZzINPXGv9PYAcY6Jc1DlXxbiIJpp4\r1rCLtolpGG1XHW44f/ZTfvx+xwQRIQbxcOqWXQYJ8HX9OMojZqK1VLNc61GzyRiA\rZTvx/tWYM2BciWTeB2GfOH66gRDLAgMBAAECgYBp4qTvoJKynuT3SbDJY/XwaEtm\ru768SF9P0GlXrtwYuDWjAVue0VhBI9WxMWZTaVafkcP8hxX4QZqPh84td0zjcq3j\rDLOegAFJkIorGzq5FyK7ydBoU1TLjFV459c8dTZMTu+LgsOTD11/V/Jr4NJxIudo\rMBQ3c4cHmOoYv4uzkQJBANR+7Fc3e6oZgqTOesqPSPqljbsdF9E4x4eDFuOecCkJ\rDvVLOOoAzvtHfAiUp+H3fk4hXRpALiNBEHiIdhIuX2UCQQDCCHiPHFd4gC58yyCM\r6Leqkmoa+6YpfRb3oxykLBXcWx7DtbX+ayKy5OQmnkEG+MW8XB8wAdiUl0/tb6cQ\rFaRvAkBhvP94Hk0DMDinFVHlWYJ3xy4pongSA8vCyMj+aSGtvjzjFnZXK4gIjBjA\r2Z9ekDfIOBBawqp2DLdGuX2VXz8BAkByMuIh+KBSv76cnEDwLhfLQJlKgEnvqTvX\rTB0TUw8avlaBAXW34/5sI+NUB1hmbgyTK/T/IFcEPXpBWLGO+e3pAkAGWLpnH0Zh\rFae7oAqkMAd3xCNY6ec180tAe57hZ6kS+SYLKwb4gGzYaCxc22vMtYksXHtUeamo\r1NMLzI2ZfUoX\r";
    private RSAPrivateKey privateKey;
    private RSAPublicKey publicKey;
    private static final char[] HEX_CHAR = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    public RSA() {
    }

    public RSAPrivateKey getPrivateKey() {
        return this.privateKey;
    }

    public RSAPublicKey getPublicKey() {
        return this.publicKey;
    }

    public void genKeyPair() {
        KeyPairGenerator keyPairGen = null;

        try {
            keyPairGen = KeyPairGenerator.getInstance("RSA");
        } catch (NoSuchAlgorithmException var3) {
            var3.printStackTrace();
        }

        keyPairGen.initialize(1024, new SecureRandom());
        KeyPair keyPair = keyPairGen.generateKeyPair();
        this.privateKey = (RSAPrivateKey)keyPair.getPrivate();
        this.publicKey = (RSAPublicKey)keyPair.getPublic();
    }

    public void loadPublicKey(InputStream in) throws Exception {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String readLine = null;
            StringBuilder sb = new StringBuilder();

            while((readLine = br.readLine()) != null) {
                if(readLine.contains("BEGIN")) {
                    sb.delete(0, sb.length());
                } else {
                    if(readLine.contains("END")) {
                        break;
                    }

                    sb.append(readLine);
                    sb.append('\r');
                }
            }

            this.loadPublicKey(sb.toString());
        } catch (IOException var5) {
            throw new Exception("公钥数据流读取错�?");
        } catch (NullPointerException var6) {
            throw new Exception("公钥输入流为�?");
        }
    }

    public void loadPublicKey(String publicKeyStr) throws Exception {
        try {
            BASE64Decoder base64Decoder = new BASE64Decoder();
            byte[] buffer = base64Decoder.decodeBuffer(publicKeyStr);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(buffer);
            this.publicKey = (RSAPublicKey)keyFactory.generatePublic(keySpec);
        } catch (NoSuchAlgorithmException var6) {
            throw new Exception("无此算法");
        } catch (InvalidKeySpecException var7) {
            throw new Exception("公钥非法");
        } catch (IOException var8) {
            throw new Exception("公钥数据内容读取错误");
        } catch (NullPointerException var9) {
            throw new Exception("公钥数据为空");
        }
    }

    public void loadPrivateKey(InputStream in) throws Exception {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String readLine = null;
            StringBuilder sb = new StringBuilder();

            while((readLine = br.readLine()) != null) {
                if(readLine.charAt(0) != 45) {
                    sb.append(readLine);
                    sb.append('\r');
                }
            }

            this.loadPrivateKey(sb.toString());
        } catch (IOException var5) {
            throw new Exception("私钥数据读取错误");
        } catch (NullPointerException var6) {
            throw new Exception("私钥输入流为�?");
        }
    }

    public void loadPrivateKey(String privateKeyStr) throws Exception {
        try {
            BASE64Decoder base64Decoder = new BASE64Decoder();
            byte[] buffer = base64Decoder.decodeBuffer(privateKeyStr);
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(buffer);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            this.privateKey = (RSAPrivateKey)keyFactory.generatePrivate(keySpec);
        } catch (NoSuchAlgorithmException var6) {
            throw new Exception("无此算法");
        } catch (InvalidKeySpecException var7) {
            throw new Exception("私钥非法");
        } catch (IOException var8) {
            throw new Exception("私钥数据内容读取错误");
        } catch (NullPointerException var9) {
            throw new Exception("私钥数据为空");
        }
    }

    public byte[] encrypt(byte[] plainTextData) throws Exception {
        RSAPublicKey publicKey = this.getPublicKey();
        if(publicKey == null) {
            throw new Exception("加密公钥为空, 请设�?");
        } else {
            Cipher cipher = null;

            try {
                cipher = Cipher.getInstance("RSA/None/PKCS1Padding", new BouncyCastleProvider());
                cipher.init(1, publicKey);
                byte[] output = cipher.doFinal(plainTextData);
                return output;
            } catch (NoSuchAlgorithmException var5) {
                throw new Exception("无此加密算法");
            } catch (NoSuchPaddingException var6) {
                var6.printStackTrace();
                return null;
            } catch (InvalidKeyException var7) {
                throw new Exception("加密公钥非法,请检�?");
            } catch (IllegalBlockSizeException var8) {
                throw new Exception("明文长度非法");
            } catch (BadPaddingException var9) {
                throw new Exception("明文数据已损�?");
            }
        }
    }

    public byte[] decrypt(byte[] cipherData) throws Exception {
        RSAPrivateKey privateKey = this.getPrivateKey();
        if(privateKey == null) {
            throw new Exception("解密私钥为空, 请设�?");
        } else {
            Cipher cipher = null;

            try {
                cipher = Cipher.getInstance("RSA/None/PKCS1Padding", new BouncyCastleProvider());
                cipher.init(2, privateKey);
                byte[] output = cipher.doFinal(cipherData);
                return output;
            } catch (NoSuchAlgorithmException var5) {
                throw new Exception("无此解密算法");
            } catch (NoSuchPaddingException var6) {
                var6.printStackTrace();
                return null;
            } catch (InvalidKeyException var7) {
                throw new Exception("解密私钥非法,请检�?");
            } catch (IllegalBlockSizeException var8) {
                throw new Exception("密文长度非法");
            } catch (BadPaddingException var9) {
                throw new Exception("密文数据已损�?");
            }
        }
    }

    public static String byteArrayToString(byte[] data) {
        StringBuilder stringBuilder = new StringBuilder();

        for(int i = 0; i < data.length; ++i) {
            stringBuilder.append(HEX_CHAR[(data[i] & 240) >>> 4]);
            stringBuilder.append(HEX_CHAR[data[i] & 15]);
            if(i < data.length - 1) {
                stringBuilder.append(' ');
            }
        }

        return stringBuilder.toString();
    }

    public static byte[] hexStringToByte(String hex) {
        int len = hex.length() / 2;
        System.out.println(len);
        byte[] result = new byte[len];
        char[] achar = hex.toCharArray();

        for(int i = 0; i < len; ++i) {
            int pos = i * 2;
            result[i] = (byte)(toByte(achar[pos]) << 4 | toByte(achar[pos + 1]));
        }

        return result;
    }

    private static byte toByte(char c) {
        byte b = (byte)"0123456789ABCDEF".indexOf(c);
        return b;
    }

    public static byte[] hexStringToBytes(String hexString) {
        if(hexString != null && !hexString.equals("")) {
            hexString = hexString.toUpperCase();
            int length = hexString.length() / 2;
            char[] hexChars = hexString.toCharArray();
            byte[] d = new byte[length];

            for(int i = 0; i < length; ++i) {
                int pos = i * 2;
                d[i] = (byte)(charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));
            }

            return d;
        } else {
            return null;
        }
    }

    private static byte charToByte(char c) {
        return (byte)"0123456789ABCDEF".indexOf(c);
    }

    public byte[] sign(byte[] content) {
        try {
            Signature signature = Signature.getInstance("SHA1WithRSA");
            signature.initSign(this.privateKey);
            signature.update(content);
            return signature.sign();
        } catch (Exception var3) {
            var3.printStackTrace();
            return null;
        }
    }

    boolean check(byte[] content) {
        try {
            Signature signature = Signature.getInstance("SHA1WithRSA");
            signature.initVerify(this.publicKey);
            signature.update(content);
            return signature.verify(content);
        } catch (Exception var3) {
            System.out.println("验签失败");
            return false;
        }
    }

    public static void main(String[] args) {
        RSA rsaEncrypt = new RSA();

        String encryptStr;
        try {
            encryptStr = "150479491925261579720208703452547977089765773133163483855836185828048489607003741592913530042316680238856089069019174749776137982649856843271794093582822170915724974369562653137375918136521773899771851799179130917321516038499217993296118718532162256761744707253251577731818247634523594321069305828794576805901";
            String e = "65537";
            rsaEncrypt.loadPublicKey(new BigInteger(encryptStr), new BigInteger(e));
            rsaEncrypt.loadPrivateKey((InputStream)(new FileInputStream(new File("./keys/pkcs8_rsa_private_key.pem"))));
        } catch (FileNotFoundException var9) {
            var9.printStackTrace();
        } catch (Exception var10) {
            var10.printStackTrace();
        }

        encryptStr = "你好, BOB!";

        try {
            byte[] cipher = rsaEncrypt.encrypt(encryptStr.getBytes());
            System.out.println("密文长度:" + cipher.length);
            System.out.println(byteArrayToString(cipher));
            String str = "b213e8074be982e332b70588668759ce58522386917d065ddb7c6272bd7e2646b89fb298632809f727514dd4de8b76ea53f063dbd9b35a3d43529c2464f7300c44a27eafe9f0ea5c5bd37d5f39e451190e136496ad86858dc71c2b3809fd845a9f7b20d320e22fdf70980a4b53fdae07e2eba6ce06c9f989b8ffc77b4bed7fa3";
            byte[] plainText = rsaEncrypt.decrypt(cipher);
            System.out.println("明文长度:" + plainText.length);
            System.out.println(new String(plainText, "utf-8"));
            byte[] sign = rsaEncrypt.sign("你好, BOB!".getBytes("utf-8"));
            System.out.println("签名长度:" + sign.length);
            System.out.println(byteArrayToString(sign));
            str = "6b479adc770b3e86cedda99af7bb111f46241e433f6e72c719295cf09001e4a803b7d1d44b4696f0e9fcc1529e1aecc423472001d84d991e769414db6ee5070e0dacef77e6b5651f1deea250dc1a140cbbd4ae31ad9cc986d6ce8bf2a63013f451f8a7dd2f675c2d0e9354e36308cc7a369320e359cc8b354cdfbfaa637cb059";
            boolean r = rsaEncrypt.check(sign);
            if(r) {
                System.out.println("验签成功");
            } else {
                System.out.println("验签失败");
            }
        } catch (Exception var8) {
            System.err.println(var8.getMessage());
        }

    }

    public void loadPublicKey(String moduls, String publicExponent) {
        try {
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            this.publicKey = (RSAPublicKey)keyFactory.generatePublic(new RSAPublicKeySpec(new BigInteger(moduls, 16), new BigInteger(publicExponent, 16)));
        } catch (NoSuchAlgorithmException var5) {
            var5.printStackTrace();
        } catch (InvalidKeySpecException var6) {
            var6.printStackTrace();
        }

    }

    public void loadPublicKey(BigInteger pn, BigInteger pe) {
        try {
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            this.publicKey = (RSAPublicKey)keyFactory.generatePublic(new RSAPublicKeySpec(pn, pe));
        } catch (NoSuchAlgorithmException var5) {
            var5.printStackTrace();
        } catch (InvalidKeySpecException var6) {
            var6.printStackTrace();
        }

    }
}
