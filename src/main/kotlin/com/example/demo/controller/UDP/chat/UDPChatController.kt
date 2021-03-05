package com.example.demo.controller.UDP.chat

import com.example.demo.controller.Controller
import com.example.demo.controller.TCP.chat.TCPClientChatController
import javafx.application.Platform
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.util.concurrent.ConcurrentLinkedQueue

class UDPChatController(val portReceive: Int, val portSend: Int,
                        val queue: ConcurrentLinkedQueue<String>,
                        val queueReceive: ConcurrentLinkedQueue<String>,
                        val controller: Controller) {

    @Volatile
    var running: Boolean = false

    val ip = InetAddress.getLocalHost()

    @Volatile
    var socket: DatagramSocket = DatagramSocket(portReceive)

//    var socket: DatagramSocket   = DatagramSocket(portReceive)


    fun initChat() {

        println("UDPRunning")
        println("Server is Up....")

//        socket = DatagramSocket(portReceive)

        running = true
        initThreadSend(queue)
        initThreadReceive(queueReceive, controller)
    }

    private fun initThreadSend(queue: ConcurrentLinkedQueue<String>) {
        var packet: DatagramPacket

        fun sendPacket(msg : String){
            packet = DatagramPacket(msg.toByteArray(), msg.toByteArray().size, ip, portSend)
            socket.send(packet)
        }

        Thread {
            try {
                if (running) {
                    sendPacket("*User has join to the chat UDP")

                    while (running) {
                        while (queue.isEmpty());
                        sendPacket(queue.poll())
                    }
                }
            } catch (e: Exception) {
                println("UDPException occurred")
                initThreadSend(queue)
            }
        }.start()
    }

    private fun initThreadReceive(queueReceive: ConcurrentLinkedQueue<String>, controller: Controller) {

        Thread {
            var buffor = ByteArray(2048)
            var msg: String
            try {
                while (running) {
                    val packet = DatagramPacket(buffor, buffor.size)
                    socket.receive(packet)
                    msg = String(buffor).trim { it <= ' ' }
//                    if (msg == "*User has join to the chat" && Controller.whoiam == "Server")
//                        Platform.runLater { controller.syncSettingBetweenUsersOnStartChat() }
                    if ( msg == "*User has left the chat"){
                        controller.setSettingToTCP()
                    }
                    queueReceive.add(msg)
                    buffor = ByteArray(2048)
                }
            } catch (e: Exception) {
                println("UDP2Exception occured")
//                socket?.close()
                initThreadReceive(queueReceive, controller)
            }
        }.start()
    }

    fun stop() {
        running = false
//        socket.close()

    }

}