package com.example.demo.controller.client

import com.example.demo.app.MyApp
import com.example.demo.controller.Controller
import javafx.scene.control.TextArea
import javafx.scene.control.TextField
import java.lang.StringBuilder
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.ConcurrentLinkedQueue

object UDPClientChatController {

    private var dtf: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")
    val ds = DatagramSocket(5334)
    val ip = InetAddress.getLocalHost()


    fun initClientChat(queue: ConcurrentLinkedQueue<String>, textAreaChat: TextArea, iPText: TextField) {

//        iPText.text = MyApp.ip



        println("Running")
        println("Client is Up....")

        var threadSend = Thread {
            try {
                while (true) {
                    var sd = ByteArray(1000)

                    while (queue.isEmpty());
                    sd = queue.poll().toByteArray()

                    val sp = DatagramPacket(sd, sd.size, ip, 1234)
                    ds.send(sp)

                }
            } catch (e: Exception) {
                println("Exception occured");
            }
            finally {
                ds.close()
            }
        }

        var threadRecive = Thread {
            try {
                while (true) {
                    val rd = ByteArray(1000)

                    val sp1 = DatagramPacket(rd, rd.size)
                    ds.receive(sp1)

                    val msg = String(rd).trim { it <= ' ' }
                    println("Server: $msg")

                    var sB = StringBuilder()
                    if (Controller.namePartnerFlag) {
                        sB.append("\tClient:\n")
                        Controller.namePartnerFlag = false
                        Controller.nameUserFlag = true
                    }
                    sB.append(dtf.format(LocalDateTime.now()))
                            .append(" -> ")
                            .append(msg)
                            .append('\n')

                    textAreaChat.appendText(sB.toString())

                }
            } catch (e: Exception) {
                System.out.println("Exception occured")
            }
            finally {
                ds.close()
            }
        }

        threadSend.start()
        threadRecive.start()
    }
}