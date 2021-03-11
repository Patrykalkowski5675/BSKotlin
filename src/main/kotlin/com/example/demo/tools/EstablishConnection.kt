package com.example.demo.tools

import com.example.demo.controller.Controller
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.Socket

object EstablishConnection {

    @Volatile
    lateinit var secondUserIP : String

    fun waitForConnection() {
        val ss = ServerSocket(13267)
        try {
            val socket = ss.accept()
            Controller.secondUserIP = (socket.remoteSocketAddress as InetSocketAddress).address.toString().substring(1)
            socket.close()
            ss.close()
            println(Controller.secondUserIP)
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    fun responseForConnection() {
        if(secondUserIP.isNullOrBlank()) throw ExceptionInInitializerError()

        var flag = true
        while (flag) {
            try {
                val socket = Socket(secondUserIP, 13267)
                socket.close()
                flag = false
            } catch (e: Exception) {
                Thread.sleep(800)
            }
        }
    }

    fun changeSecondUserIP(secondUserIP: String) {
        this.secondUserIP = secondUserIP
    }

}