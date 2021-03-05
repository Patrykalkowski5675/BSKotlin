package com.example.demo.controller.UDP.transferfile

import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.time.format.DateTimeFormatter
import java.util.concurrent.ConcurrentLinkedQueue

object UDPTransferfileController {

    @Volatile
    var flagStart: Boolean = false

    private var dtf: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")

    val ip = InetAddress.getLocalHost()
    val portReceive : Int = 5335
    val portSend : Int = 4445
    var socket: DatagramSocket? =  DatagramSocket(5334)
    private fun initThreadSend(queue: ConcurrentLinkedQueue<String>) {
        var threadSend = Thread {
            var sd: ByteArray
            var packet: DatagramPacket
            try {
                while (true) {
                    while (queue.isEmpty());
//                    if(flagStart) {
                        sd = queue.poll().toByteArray()
                        packet = DatagramPacket(sd, sd.size, ip, 1234)
                        socket?.send(packet)
//                    }
                }
            } catch (e: Exception) {
                println("UDPException occurred")
                initThreadSend(queue)
            }
        }.start()
    }

    private fun initThreadReceive(queueReceive: ConcurrentLinkedQueue<String>) {
        var threadRecive = Thread {
            try {
//                ds = DatagramSocket(portSend)
                println("Running")
                println("Server is Up....")
                while (true) {
                    val rd = ByteArray(4096)

                    val packet = DatagramPacket(rd, rd.size)
                    socket?.receive(packet)

                    val msg = String(rd).trim { it <= ' ' }
                    println("Server: $msg")

                    queueReceive.add(msg)

                }
            } catch (e: Exception) {
                println("Exception occured")
//                ds?.close()
            }

        }.start()
    }

    fun initClientChat(queue: ConcurrentLinkedQueue<String>, queueReceive: ConcurrentLinkedQueue<String>) {

        initThreadSend(queue)
        initThreadReceive(queueReceive)
    }
}