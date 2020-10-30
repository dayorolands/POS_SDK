package com.dspread.qpos.injectKey;//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//


import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Envelope {
    public static String digitalEnvelopStr;


    public Envelope() {

    }

    public byte[] readMessageFile(String fileName) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        FileInputStream stream = null;
        byte[] results = new byte[0];

        try {
            stream = new FileInputStream(fileName);
            boolean var5 = false;

            int c;
            while ((c = stream.read()) != -1) {
                baos.write(c);
            }

            results = baos.toByteArray();
        } catch (Exception var18) {
            var18.printStackTrace();
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException var17) {
                    ;
                }
            }

            try {
                baos.close();
            } catch (IOException var16) {
                ;
            }

        }

        return results;
    }

    public static byte[] packageMessage(byte[] message) {
        byte[] results = new byte[message.length + 8];

        for (int i = 0; i < results.length; ++i) {
            results[i] = 0;
        }

        byte[] lenBytes = Utils.int2Byte(message.length);
        System.arraycopy(lenBytes, 0, results, 0, lenBytes.length);
        System.arraycopy(message, 0, results, 8, message.length);
        return results;
    }

    public static byte[] getTdesKey() {
        return new byte[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15};
    }

    public static String bytes2hex(byte[] src) {
        StringBuilder stringBuilder = new StringBuilder("");
        if (src != null && src.length > 0) {
            for (int i = 0; i < src.length; ++i) {
                int v = src[i] & 255;
                String hv = Integer.toHexString(v);
                if (hv.length() < 2) {
                    stringBuilder.append(0);
                }

                stringBuilder.append(hv);
            }

            return stringBuilder.toString().toUpperCase();
        } else {
            return null;
        }
    }

    public static byte[] byteEvelope(byte[] message, RSA senderRsa, RSA receiverRsa) throws Exception {

        return byteEvelope(message, senderRsa, receiverRsa, 1024);
    }

    public static byte[] byteEvelope(byte[] message, RSA senderRsa, RSA receiverRsa, int RSA_len) throws Exception {
        byte[] encrypedTdesKey = receiverRsa.encrypt(getTdesKey());
        byte[] encrypedMessage = encrypt(message, senderRsa);
        byte[] toSha1Message = new byte[encrypedTdesKey.length + encrypedMessage.length];
        System.arraycopy(encrypedTdesKey, 0, toSha1Message, 0, encrypedTdesKey.length);
        System.arraycopy(encrypedMessage, 0, toSha1Message, encrypedTdesKey.length, encrypedMessage.length);
        byte[] signedMessage = senderRsa.sign(toSha1Message);
        byte[] results = new byte[4 + encrypedTdesKey.length + encrypedMessage.length + signedMessage.length];
        int len = encrypedTdesKey.length + encrypedMessage.length + signedMessage.length;
        byte[] lenBytes = Utils.int2Byte(len);
        if (RSA_len == 2048) {
            lenBytes[3] = (byte) 0x80;
        }
        System.out.println("encrypedTdesKey:" + encrypedTdesKey.length + "\n" + "encrypedMessage:" + encrypedMessage.length + "\n" + "signedMessage:" + signedMessage.length);
        System.arraycopy(lenBytes, 0, results, 0, lenBytes.length);
        System.arraycopy(encrypedTdesKey, 0, results, lenBytes.length, encrypedTdesKey.length);
        System.arraycopy(encrypedMessage, 0, results, lenBytes.length + encrypedTdesKey.length, encrypedMessage.length);
        System.arraycopy(signedMessage, 0, results, lenBytes.length + encrypedTdesKey.length + encrypedMessage.length, signedMessage.length);
        return results;
    }


    public static byte[] encrypt(byte[] message, RSA senderRsa) throws Exception {
        byte[] packagedMessage = packageMessage(message);
        int blockSize = packagedMessage.length / 8;
        if (packagedMessage.length % 8 != 0) {
            ++blockSize;
        }

        byte[] padedPackagedMessage = new byte[blockSize * 8];

        int i;
        for (i = 0; i < padedPackagedMessage.length; ++i) {
            padedPackagedMessage[i] = -1;
        }

        System.arraycopy(packagedMessage, 0, padedPackagedMessage, 0, packagedMessage.length);
        byte[] encryptedMess = new byte[blockSize * 8];

        for (i = 0; i < blockSize; ++i) {
            byte[] temp = new byte[8];
            byte[] temp2 = new byte[8];
            System.arraycopy(padedPackagedMessage, i * 8, temp, 0, 8);
            temp2 = TDES.tdesCBCEncypt(getTdesKey(), temp);
            System.arraycopy(temp2, 0, encryptedMess, i * 8, 8);
        }

        return encryptedMess;
    }

    public byte[] sha1(byte[] message) {
        byte[] results = new byte[0];

        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            md.update(message);
            results = md.digest();
            System.out.println("PlainText message:" + bytesToHexString(message));
            System.out.println("sha-1:" + bytesToHexString(results));
        } catch (NoSuchAlgorithmException var4) {
            ;
        }

        return results;
    }

    public byte[] sha1(String message) {
        return this.sha1(message.getBytes());
    }

    public static String bytesToHexString(byte[] src) {
        StringBuilder stringBuilder = new StringBuilder("");
        if (src != null && src.length > 0) {
            for (int i = 0; i < src.length; ++i) {
                int v = src[i] & 255;
                String hv = Integer.toHexString(v);
                if (hv.length() < 2) {
                    stringBuilder.append(0);
                }

                stringBuilder.append(hv);
            }

            return stringBuilder.toString();
        } else {
            return null;
        }
    }

    public static byte[] hexStringToBytes(String hexString) {
        if (hexString != null && !hexString.equals("")) {
            hexString = hexString.toUpperCase();
            int length = hexString.length() / 2;
            char[] hexChars = hexString.toCharArray();
            byte[] d = new byte[length];

            for (int i = 0; i < length; ++i) {
                int pos = i * 2;
                d[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));
            }

            return d;
        } else {
            return null;
        }
    }

    private static byte charToByte(char c) {
        return (byte) "0123456789ABCDEF".indexOf(c);
    }

    public static void main(String[] args) {
//        String aString = getDigitalEnvelopStr();
//        System.out.println("-------->" + aString);
    }


    /*
    * in privateKey inputstream
    *
    * */


    public static String getDigitalEnvelopStrByKey(InputStream in, Poskeys posKeys, Poskeys.RSA_KEY_LEN rsa_key_len , int keyIndex) {
        String ipekKeyStr = null;
        try {
            if (posKeys instanceof DukptKeys) {
                DukptKeys dukptKeys = (DukptKeys) posKeys;
                String trackipekString = dukptKeys.getTrackipek();
                String emvipekString = dukptKeys.getEmvipek();
                String pinipekString = dukptKeys.getPinipek();
                byte[] trackipek = hexStringToBytes(trackipekString);
                byte[] emvipek = hexStringToBytes(emvipekString);
                byte[] pinipek = hexStringToBytes(pinipekString);
                String trackksn = dukptKeys.getTrackksn();
                String emvksn = dukptKeys.getEmvksn();
                String pinksn = dukptKeys.getPinksn();
                String tmkString = dukptKeys.getTmk();
                byte[] tmk = hexStringToBytes(tmkString);
                String trackipek1 = bytes2hex(TDES.tdesECBDecrypt(tmk, trackipek));
                String emvipek1 = bytes2hex(TDES.tdesECBDecrypt(tmk, emvipek));
                String pinipek1 = bytes2hex(TDES.tdesECBDecrypt(tmk, pinipek));
                ipekKeyStr = trackksn + trackipek1 + emvksn + emvipek1 + pinksn + pinipek1;

            } else if (posKeys instanceof TMKKey) {
                TMKKey tmkKey = (TMKKey) posKeys;
                ipekKeyStr = tmkKey.getTmkKey();
           }else if (posKeys instanceof DukptKeys_spe) {
                DukptKeys_spe dukptKeys = (DukptKeys_spe) posKeys;
                String trackipekString = dukptKeys.getTrackipek();
                String emvipekString = dukptKeys.getEmvipek();
                String pinipekString = dukptKeys.getPinipek();
                String pinkey_covertString = dukptKeys.getPinkey_covert();
                byte[] trackipek = hexStringToBytes(trackipekString);
                byte[] emvipek = hexStringToBytes(emvipekString);
                byte[] pinipek = hexStringToBytes(pinipekString);
                byte[] pinkey_covert = hexStringToBytes(pinkey_covertString);
                String trackksn = dukptKeys.getTrackksn();
                String emvksn = dukptKeys.getEmvksn();
                String pinksn = dukptKeys.getPinksn();
                String pinksn_covert = dukptKeys.getPinksn_covert();
                String tmkString = dukptKeys.getTmk();
                byte[] tmk = hexStringToBytes(tmkString);
                String trackipek1 = bytes2hex(TDES.tdesECBDecrypt(tmk, trackipek));
                String emvipek1 = bytes2hex(TDES.tdesECBDecrypt(tmk, emvipek));
                String pinipek1 = bytes2hex(TDES.tdesECBDecrypt(tmk, pinipek));
                String pinkey_covert1 = bytes2hex(TDES.tdesECBDecrypt(tmk, pinkey_covert));
                ipekKeyStr = trackksn + trackipek1 + emvksn + emvipek1 + pinksn + pinipek1+pinksn_covert+pinkey_covert1;
            }

            byte[] setIpekKeyStrData = new byte[5 + ipekKeyStr.length() / 2+1];
            if (posKeys instanceof DukptKeys)
                setIpekKeyStrData[0] = 5;
            else if (posKeys instanceof TMKKey)
                setIpekKeyStrData[0] = 4;
            else if (posKeys instanceof DukptKeys_spe)
                setIpekKeyStrData[0] = 6;

            byte[] lenBytes = Utils.int2Byte(ipekKeyStr.length() / 2+1);
            System.arraycopy(lenBytes, 0, setIpekKeyStrData, 1, lenBytes.length);
            byte[] ipekBytes = hexStringToBytes(ipekKeyStr);
            byte bytKeyIndex = (byte) keyIndex;
            if (posKeys instanceof DukptKeys || posKeys instanceof DukptKeys_spe){
                System.arraycopy(ipekBytes, 0, setIpekKeyStrData, 1 + lenBytes.length, ipekBytes.length);
                setIpekKeyStrData[setIpekKeyStrData.length -1] = bytKeyIndex;
            } else if (posKeys instanceof TMKKey){
                setIpekKeyStrData[5] = bytKeyIndex;
                System.arraycopy(ipekBytes, 0, setIpekKeyStrData, 1 + lenBytes.length+1, ipekBytes.length);
            }


            byte[] command = new byte[]{1, 2, 0, 0};
            byte[] message2 = new byte[8 + setIpekKeyStrData.length];
            System.arraycopy(command, 0, message2, 0, command.length);
            lenBytes = Utils.int2Byte(setIpekKeyStrData.length);
            System.arraycopy(lenBytes, 0, message2, 0 + command.length, lenBytes.length);
            System.arraycopy(setIpekKeyStrData, 0, message2, 0 + command.length + lenBytes.length, setIpekKeyStrData.length);


            RSA senderRsa = new RSA();
            RSA receiverRsa = new RSA();
            senderRsa.loadPrivateKey(in); //私钥
            String n = posKeys.getRSA_public_key();
            String e = "010001";
            receiverRsa.loadPublicKey(n, e);//公钥

            return packageEnvelopFun(message2,senderRsa,receiverRsa,rsa_key_len);
        } catch (Exception e) {
            return digitalEnvelopStr;

        }
    }

    private static String packageEnvelopFun(byte[] message2,RSA senderRsa,RSA receiverRsa,Poskeys.RSA_KEY_LEN rsa_key_len) {
        try{

        byte[] de = null;
        if (rsa_key_len == Poskeys.RSA_KEY_LEN.RSA_KEY_1024)
            de = byteEvelope(message2, senderRsa, receiverRsa);
        else if (rsa_key_len == Poskeys.RSA_KEY_LEN.RSA_KEY_2048)
            de = byteEvelope(message2, senderRsa, receiverRsa, 2048);
        else {
            throw new Exception("Bad key length");
        }
        int blockSize = de.length / 256;
        if (de.length % 256 != 0) {
            ++blockSize;
        }

        byte[] pde = new byte[blockSize * 256];

        int i;
        for (i = 0; i < pde.length; ++i) {
            pde[i] = -1;
        }

        for (i = 0; i < 10000; ++i) {
            ++i;
        }

        System.arraycopy(de, 0, pde, 0, de.length);
        System.out.println("de:" + de.length + "\n" + "pde:" + pde.length);
        System.out.println(bytes2hex(pde));
        digitalEnvelopStr = bytes2hex(pde);
        System.out.println("length:" + bytes2hex(pde).length());
        System.out.println("digitalEnvelopStr:" + digitalEnvelopStr);
        return digitalEnvelopStr;
    } catch (Exception var30) {
        var30.printStackTrace();
        return digitalEnvelopStr;
    }
    }

    public static String getDigitalEnvelopStr(InputStream in, DukptKeys dukptKeys, Poskeys.RSA_KEY_LEN rsa_key_len) {
       return getDigitalEnvelopStrByKey(in,dukptKeys,rsa_key_len,0);
    }

    public static String getDigitalEnvelopStr(InputStream in, DukptKeys dukptKeys) {
        return getDigitalEnvelopStr(in, dukptKeys, DukptKeys.RSA_KEY_LEN.RSA_KEY_1024);
    }

}
