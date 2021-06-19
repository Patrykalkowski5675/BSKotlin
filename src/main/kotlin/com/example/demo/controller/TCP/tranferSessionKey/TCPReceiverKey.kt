package com.example.demo.controller.TCP.tranferSessionKey

import java.net.Socket
import java.nio.ByteBuffer

object TCPReceiverKey {

    private const val port = 13267

    @Volatile
    var flag = true

    fun initReceiveKey(secondUserIP : String): ByteArray {

        lateinit var byteArray: ByteArray
        lateinit var socket: Socket
        println('\n' + secondUserIP)
        flag = true
        while (flag)
            try {
                socket = Socket(secondUserIP, port)
                flag = false
            } catch (e: Exception) {
                Thread.sleep(500)
                e.printStackTrace()
            }

        val byteArrayToByteBuffer = ByteArray(4)
        socket.getInputStream().read(byteArrayToByteBuffer, 0, 4)

        val byteBuffer = ByteBuffer.wrap(byteArrayToByteBuffer)
        val length = byteBuffer.int
        val readByteArray: ByteArray = ByteArray(length)

        socket.getInputStream().read(readByteArray)
        socket.close()

        byteArray = readByteArray

        return byteArray
    }



}