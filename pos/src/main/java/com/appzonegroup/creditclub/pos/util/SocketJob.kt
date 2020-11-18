package com.appzonegroup.creditclub.pos.util

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
    @Throws(
        NoSuchAlgorithmException::class,
        KeyManagementException::class,
        IOException::class
    )
    fun execute(
        connectionInfo: RemoteConnectionInfo,
        data: ByteArray,
        isRetry: Boolean = false
    ): ByteArray? {
        if (!connectionInfo.ssl) return socketConnectionJob(connectionInfo, data, isRetry)

        val host = connectionInfo.ip
        val port = connectionInfo.port

        val timeout =
            if (isRetry) connectionInfo.requeryConfig?.timeout ?: connectionInfo.timeout
            else connectionInfo.timeout

        debugOnly { println("[remote environment]: $host:$port") }

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
        val sslsocket =
            sc.socketFactory.createSocket() as SSLSocket
        sslsocket.soTimeout = timeout * 1000
        sslsocket.connect(InetSocketAddress(host, port), timeout * 1000)
        sslsocket.startHandshake()
        val `in` = DataInputStream(sslsocket.inputStream)
        val out = DataOutputStream(sslsocket.outputStream)
        val baos = ByteArrayOutputStream()
        val outputInfo = appendLengthBytes(data)
        out.write(outputInfo)
        out.flush()
        var bytesRead: Int
        `in`.readFully(messageByte, 0, 2)

        //ByteBuffer byteBuffer = ByteBuffer.wrap(messageByte, 0, 2);
        val byteBuffer = ByteBuffer.wrap(messageByte, 0, 2)
        val bytesToRead = byteBuffer.short.toInt()
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
        connectionInfo: RemoteConnectionInfo,
        data: ByteArray,
        isRetry: Boolean = false
    ): ByteArray {
        val host = connectionInfo.ip
        val port = connectionInfo.port

        val timeout =
            if (isRetry) connectionInfo.requeryConfig?.timeout ?: connectionInfo.timeout
            else connectionInfo.timeout

        debugOnly { println("[remote environment]: $host:$port") }

        val connectionSocket: Socket
        val messageByte = ByteArray(1000)
        var end = false
        var dataString = ""
        val serverAddr = InetAddress.getByName(host)
        connectionSocket = Socket(serverAddr, port)
        connectionSocket.soTimeout = timeout * 1000
        val baos = ByteArrayOutputStream()
        //InputStream in = connectionSocket.getInputStream();
        val `in` =
            DataInputStream(connectionSocket.getInputStream())
        val out = connectionSocket.getOutputStream()
        out.write(appendLengthBytes(data))
        var bytesRead: Int
        messageByte[0] = `in`.readByte()
        messageByte[1] = `in`.readByte()
        //ByteBuffer byteBuffer = ByteBuffer.wrap(messageByte, 0, 2);
        val byteBuffer = ByteBuffer.wrap(messageByte, 0, 2)
        val bytesToRead = byteBuffer.short.toInt()
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