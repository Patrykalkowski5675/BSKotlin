package com.example.demo.controller.TCP.tranferSessionKey

import java.net.ServerSocket
import java.nio.ByteBuffer
import java.security.Key

object TCPSenderKey {
    private const val port = 13267

    fun initTransferKey(key: Key) {
            val serverSocket = ServerSocket(port)
            try {
                val socket = serverSocket.accept()

                val bb = ByteBuffer.allocate(4)
                bb.putInt(key.encoded.size)
                val output = socket.getOutputStream()
                output.write(bb.array())
                output.write(key.encoded)
                output.flush()

                socket.close()
                serverSocket.close()

            } catch (e: Exception) {
                serverSocket.close()
                Thread.sleep(100)
                initTransferKey(key)
            }
    }

    fun initTransferEncodedKey(byteArray: ByteArray) {
        val serverSocket = ServerSocket(port)
        try {
            val socket = serverSocket.accept()

            val bb = ByteBuffer.allocate(4)
            bb.putInt(byteArray.size)
            val output = socket.getOutputStream()
            output.write(bb.array())
            println(byteArray.size)
            output.write(byteArray)
            output.flush()

            socket.close()
            serverSocket.close()

        } catch (e: Exception) {
            serverSocket.close()
            Thread.sleep(100)
            initTransferEncodedKey(byteArray)
        }
    }
}