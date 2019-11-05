package com.appzonegroup.creditclub.pos.util;

import android.util.Log;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;

public class SocketJob {
    private static String TAG = SocketJob.class.getSimpleName();
    public static byte[] sslSocketConnectionJob(String host, int port, byte[] data) throws NoSuchAlgorithmException, KeyManagementException, IOException {
        byte[] messageByte = new byte[1000];
        boolean end = false;
        String dataString = "";

        TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() {
                        X509Certificate[] myTrustedAnchors = new X509Certificate[0];
                        return myTrustedAnchors;
                    }

                    public void checkClientTrusted(X509Certificate[] certs, String authType) {
                    }

                    public void checkServerTrusted(X509Certificate[] certs, String authType) {
                    }
                }
        };

        SSLContext sc = SSLContext.getInstance("TLS");
        sc.init(null, trustAllCerts, new SecureRandom());

        Log.d(TAG, "Connecting");
        SSLSocket sslsocket = (SSLSocket) sc.getSocketFactory().createSocket();
        sslsocket.connect(new InetSocketAddress(host, port), 30000);
        sslsocket.startHandshake();

        DataInputStream in = new DataInputStream(sslsocket.getInputStream());
        DataOutputStream out = new DataOutputStream(sslsocket.getOutputStream());



        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        //InputStream in = connectionSocket.getInputStream();

        //dialog.setMessage("Sending......");
        Log.d(TAG, "Sending......");
        Log.d(TAG, "Length to send " + data.length);
        byte [] outputInfo = TerminalUtils.appendLengthBytes(data);
        Log.d(TAG, "Bytes written to output " + new String(outputInfo));
        out.write(outputInfo);
        out.flush();
        //dialog.setMessage("Receiving....");
        Log.d(TAG, "Receiving....");
        int bytesRead;

        in.readFully(messageByte, 0, 2);
        System.out.println("Gotten length!!!!!");

        //ByteBuffer byteBuffer = ByteBuffer.wrap(messageByte, 0, 2);
        ByteBuffer byteBuffer = ByteBuffer.wrap(messageByte, 0, 2);

        int bytesToRead = byteBuffer.getShort();
        System.out.println("About to read " + bytesToRead + " octets");

        if(bytesToRead <= 1){
            return null;
        }

        //The following code shows in detail how to read from a TCP socket
        while(!end)
        {
            bytesRead = in.read(messageByte);
            baos.write(messageByte, 0, bytesRead);
            dataString += new String(messageByte, 0, bytesRead);
            if (dataString.length() == bytesToRead )
            {
                end = true;
            }
        }

        return baos.toByteArray();
    }


    public static byte[] socketConnectionJob(String host, int port, byte[] data) throws IOException {
        Socket connectionSocket;
        byte[] messageByte = new byte[1000];
        boolean end = false;
        String dataString = "";

        InetAddress serverAddr = InetAddress.getByName(host);
        Log.d(TAG, "Connecting");
        connectionSocket = new Socket(serverAddr, port);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        //InputStream in = connectionSocket.getInputStream();
        DataInputStream in = new DataInputStream(connectionSocket.getInputStream());
        OutputStream out = connectionSocket.getOutputStream();
        //dialog.setMessage("Sending......");
        Log.d(TAG, "Sending......");
        Log.d(TAG, "Length to send " + data.length);
        out.write(TerminalUtils.appendLengthBytes(data));
        //dialog.setMessage("Receiving....");
        Log.d(TAG, "Receiving....");
        int bytesRead;


        messageByte[0] = in.readByte();
        messageByte[1] = in.readByte();
        //ByteBuffer byteBuffer = ByteBuffer.wrap(messageByte, 0, 2);
        ByteBuffer byteBuffer = ByteBuffer.wrap(messageByte, 0, 2);

        int bytesToRead = byteBuffer.getShort();
        System.out.println("About to read " + bytesToRead + " octets");
        //The following code shows in detail how to read from a TCP socket
        while(!end)
        {
            bytesRead = in.read(messageByte);
            baos.write(messageByte, 0, bytesRead);
            dataString += new String(messageByte, 0, bytesRead);
            if (dataString.length() == bytesToRead )
            {
                end = true;
            }
        }

        return baos.toByteArray();
    }
}
