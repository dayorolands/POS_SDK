package com.appzonegroup.creditclub.pos.util

import com.creditclub.core.util.debugOnly
import com.creditclub.pos.RemoteConnectionInfo
import java.io.*
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

private val trustManager = object : X509TrustManager {
    override fun getAcceptedIssuers(): Array<X509Certificate> {
        return emptyArray()
    }

    override fun checkClientTrusted(certs: Array<X509Certificate>, authType: String) {
    }

    override fun checkServerTrusted(certs: Array<X509Certificate>, authType: String) {
    }
}

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
        val sslEnabled = connectionInfo.ssl
        if (!sslEnabled) return socketConnectionJob(connectionInfo, data, isRetry)

        val host = connectionInfo.ip
        val port = connectionInfo.port

        val timeout =
            if (isRetry) connectionInfo.requeryConfig?.timeout ?: connectionInfo.timeout
            else connectionInfo.timeout

        debugOnly { println("[remote environment]: $host:$port") }

        val trustAllCerts = arrayOf<TrustManager>(trustManager)
        val sc = SSLContext.getInstance("TLS")
        sc.init(null, trustAllCerts, SecureRandom())
        val sslSocket = sc.socketFactory.createSocket() as SSLSocket
        sslSocket.soTimeout = timeout * 1000
        sslSocket.connect(InetSocketAddress(host, port), timeout * 1000)
        sslSocket.startHandshake()
        val inputStream = DataInputStream(sslSocket.inputStream)
        val outputStream = DataOutputStream(sslSocket.outputStream)

        return send(inputStream, outputStream, data)
    }

    private fun send(
        inputStream: DataInputStream,
        outputStream: OutputStream,
        data: ByteArray
    ): ByteArray? {
        val messageByte = ByteArray(1000)
        var end = false
        var dataString = ""

        val outputInfo = appendLengthBytes(data)
        outputStream.write(outputInfo)
        outputStream.flush()
        var bytesRead: Int
        inputStream.readFully(messageByte, 0, 2)

        //ByteBuffer byteBuffer = ByteBuffer.wrap(messageByte, 0, 2);
        val byteBuffer = ByteBuffer.wrap(messageByte, 0, 2)
        val bytesToRead = byteBuffer.short.toInt()
        if (bytesToRead <= 1) {
            return null
        }

        //The following code shows in detail how to read from a TCP socket
        val baos = ByteArrayOutputStream()
        while (!end) {
            bytesRead = inputStream.read(messageByte)
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
    ): ByteArray? {
        val host = connectionInfo.ip
        val port = connectionInfo.port

        val timeout =
            if (isRetry) connectionInfo.requeryConfig?.timeout ?: connectionInfo.timeout
            else connectionInfo.timeout

        debugOnly { println("[remote environment]: $host:$port") }

        val serverAddr = InetAddress.getByName(host)
        val connectionSocket = Socket(serverAddr, port).apply {
            soTimeout = timeout * 1000
        }
        //InputStream in = connectionSocket.getInputStream();
        val inputStream = DataInputStream(connectionSocket.getInputStream())
        val outputStream = connectionSocket.getOutputStream()

        return send(inputStream, outputStream, data)
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