package com.appzonegroup.creditclub.pos.util

import com.creditclub.pos.RemoteConnectionInfo
import okio.*
import java.io.Closeable
import java.io.IOException
import java.net.InetSocketAddress
import java.net.Socket
import java.nio.ByteBuffer
import java.security.KeyManagementException
import java.security.NoSuchAlgorithmException
import java.security.SecureRandom
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocket
import javax.net.ssl.TrustManager

private var trustManagers: Array<TrustManager?>? = null

/**
 * @param remoteConnectionInfo
 * @param timeout in seconds
 */
class TcpClient(
    private val remoteConnectionInfo: RemoteConnectionInfo,
    private val timeout: Int = remoteConnectionInfo.timeout,
) : Closeable {
    private val isConnected: Boolean
        get() = socket?.isConnected == true

    private var socket: Socket? = null
    private var source: BufferedSource? = null
    private var sink: BufferedSink? = null

    @Throws(
        NoSuchAlgorithmException::class,
        KeyManagementException::class,
        IOException::class
    )
    fun sendAndReceive(
        data: ByteArray,
    ): ByteArray? {
        if (!isConnected) {
            connect()
        }

        val outputInfo = withPrependedLength(data)
        sink!!.write(outputInfo).flush()

        val sourceLength = source!!.readByteArray(2)
        val byteBuffer = ByteBuffer.wrap(sourceLength, 0, 2)
        val bytesToRead = byteBuffer.short.toLong()
        if (bytesToRead <= 1) {
            return null
        }

        val response = source!!.readByteArray(bytesToRead)
        recycle()
        return response
    }

    private fun recycle() {
        socket?.apply {
            close()
            source = null
            sink = null
        }
        socket = null
    }

    private fun connect() {
        if (isConnected) return

        socket = getSocket(
            remoteConnectionInfo = remoteConnectionInfo,
            timeout = timeout,
        )
        source = socket!!.source().buffer()
        sink = socket!!.sink().buffer()
    }

    override fun close() {
        socket?.apply {
            close()
            source = null
            sink = null
        }
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
        timeout: Int = connectionInfo.timeout,
    ): ByteArray? = connectionInfo.tcp(timeout = timeout).use {
        it.sendAndReceive(data = data)
    }

    fun setTrustManagers(tManagers: Array<TrustManager?>) {
        trustManagers = tManagers
    }
}

fun RemoteConnectionInfo.tcp(
    timeout: Int = this.timeout,
) = TcpClient(
    remoteConnectionInfo = this,
    timeout = timeout,
)

private fun withPrependedLength(data: ByteArray): ByteArray {
    val dataLength = data.size.toShort()
    val destination = ByteArray(2 + dataLength)
    destination[0] = (dataLength.toInt() shr 8).toByte()
    destination[1] = dataLength.toByte()
    System.arraycopy(data, 0, destination, 2, dataLength.toInt())
    return destination
}

private fun getSocket(
    remoteConnectionInfo: RemoteConnectionInfo,
    timeout: Int,
): Socket {
    val timeoutInMilliSeconds = timeout * 1000
    if (!remoteConnectionInfo.sslEnabled) {
        return Socket(remoteConnectionInfo.host, remoteConnectionInfo.port).apply {
            soTimeout = timeoutInMilliSeconds
        }
    }

    val sc = SSLContext.getInstance("TLSv1.2")
    sc.init(null, trustManagers, SecureRandom())
    val sslSocket = sc.socketFactory.createSocket() as SSLSocket
    sslSocket.soTimeout = timeoutInMilliSeconds
    val address = InetSocketAddress(remoteConnectionInfo.host, remoteConnectionInfo.port)
    sslSocket.connect(address, timeoutInMilliSeconds)
    sslSocket.startHandshake()
    return sslSocket
}
