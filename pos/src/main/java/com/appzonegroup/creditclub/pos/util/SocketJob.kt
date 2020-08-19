package com.appzonegroup.creditclub.pos.util

import android.util.Log
import com.creditclub.core.util.debugOnly
import com.creditclub.pos.RemoteConnectionInfo
import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.IOException
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Socket
import java.nio.ByteBuffer
import java.security.KeyManagementException
import java.security.NoSuchAlgorithmException
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocket
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

object SocketJob {
    private val TAG = SocketJob::class.java.simpleName

    @Throws(
        NoSuchAlgorithmException::class,
        KeyManagementException::class,
        IOException::class
    )
    fun execute(
        connectionInfo: RemoteConnectionInfo,
        data: ByteArray
    ): ByteArray? {
        val host = connectionInfo.ip
        val port = connectionInfo.port
        debugOnly { println("[remote environment]: $host:$port") }
        if (!connectionInfo.ssl) return socketConnectionJob(host, port, data)

        val messageByte = ByteArray(1000)
        var end = false
        var dataString = ""
        val trustManager = object : X509TrustManager {
            override fun getAcceptedIssuers(): Array<X509Certificate> {
                return emptyArray()
            }

            override fun checkClientTrusted(certs: Array<X509Certificate>, authType: String) {
            }

            override fun checkServerTrusted(certs: Array<X509Certificate>, authType: String) {
            }
        }
        val trustAllCerts = arrayOf<TrustManager>(trustManager)
        val sc = SSLContext.getInstance("TLS")
        sc.init(null, trustAllCerts, SecureRandom())
        Log.d(TAG, "Connecting")
        val sslsocket =
            sc.socketFactory.createSocket() as SSLSocket
        sslsocket.connect(InetSocketAddress(host, port), 30000)
        sslsocket.startHandshake()
        val `in` = DataInputStream(sslsocket.inputStream)
        val out = DataOutputStream(sslsocket.outputStream)
        val baos = ByteArrayOutputStream()
        //InputStream in = connectionSocket.getInputStream();

        //dialog.setMessage("Sending......");
        Log.d(TAG, "Sending......")
        Log.d(TAG, "Length to send " + data.size)
        val outputInfo = appendLengthBytes(data)
        Log.d(TAG, "Bytes written to output " + String(outputInfo))
        out.write(outputInfo)
        out.flush()
        //dialog.setMessage("Receiving....");
        Log.d(TAG, "Receiving....")
        var bytesRead: Int
        `in`.readFully(messageByte, 0, 2)
        println("Gotten length!!!!!")

        //ByteBuffer byteBuffer = ByteBuffer.wrap(messageByte, 0, 2);
        val byteBuffer = ByteBuffer.wrap(messageByte, 0, 2)
        val bytesToRead = byteBuffer.short.toInt()
        println("About to read $bytesToRead octets")
        if (bytesToRead <= 1) {
            return null
        }

        //The following code shows in detail how to read from a TCP socket
        while (!end) {
            bytesRead = `in`.read(messageByte)
            baos.write(messageByte, 0, bytesRead)
            dataString += String(messageByte, 0, bytesRead)
            if (dataString.length == bytesToRead) {
                end = true
            }
        }
        return baos.toByteArray()
    }

    @Throws(IOException::class)
    private fun socketConnectionJob(
        host: String?,
        port: Int,
        data: ByteArray
    ): ByteArray {
        val connectionSocket: Socket
        val messageByte = ByteArray(1000)
        var end = false
        var dataString = ""
        val serverAddr = InetAddress.getByName(host)
        Log.d(TAG, "Connecting")
        connectionSocket = Socket(serverAddr, port)
        val baos = ByteArrayOutputStream()
        //InputStream in = connectionSocket.getInputStream();
        val `in` =
            DataInputStream(connectionSocket.getInputStream())
        val out = connectionSocket.getOutputStream()
        //dialog.setMessage("Sending......");
        Log.d(TAG, "Sending......")
        Log.d(TAG, "Length to send " + data.size)
        out.write(appendLengthBytes(data))
        //dialog.setMessage("Receiving....");
        Log.d(TAG, "Receiving....")
        var bytesRead: Int
        messageByte[0] = `in`.readByte()
        messageByte[1] = `in`.readByte()
        //ByteBuffer byteBuffer = ByteBuffer.wrap(messageByte, 0, 2);
        val byteBuffer = ByteBuffer.wrap(messageByte, 0, 2)
        val bytesToRead = byteBuffer.short.toInt()
        println("About to read $bytesToRead octets")
        //The following code shows in detail how to read from a TCP socket
        while (!end) {
            bytesRead = `in`.read(messageByte)
            baos.write(messageByte, 0, bytesRead)
            dataString += String(messageByte, 0, bytesRead)
            if (dataString.length == bytesToRead) {
                end = true
            }
        }
        return baos.toByteArray()
    }

    private fun appendLengthBytes(data: ByteArray): ByteArray {
        val dataLength = data.size.toShort()
        val destination = ByteArray(2 + dataLength)
        destination[0] = (dataLength.toInt() shr 8).toByte()
        destination[1] = dataLength /*>> 0*/.toByte()
        System.arraycopy(data, 0, destination, 2, dataLength.toInt())
        return destination
    }
}