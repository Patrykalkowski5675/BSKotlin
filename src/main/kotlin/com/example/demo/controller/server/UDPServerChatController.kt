package com.example.demo.controller.server

import com.example.demo.controller.Controller
import javafx.scene.control.TextArea
import javafx.scene.control.TextField
import java.lang.StringBuilder
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue

object UDPServerChatController{

    private var dtf: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")
    val ds = DatagramSocket(1234)
    val ip = InetAddress.getLocalHost()
    

    fun initServerChat(queue: ConcurrentLinkedQueue<String>, textAreaChat: TextArea, iPText: TextField) {

        iPText.text = "Server (Unable change IP)"
        iPText.isEditable = false
        iPText.isMouseTransparent = true

        println("Running")
        println("Server is Up....")

        var threadSend = Thread {
            try {
                val sc = Scanner(System.`in`)
                while (true) {

                    var sd = ByteArray(1000)

                    while (queue.isEmpty());
                    sd = queue.poll().toByteArray()

                    val sp = DatagramPacket(sd, sd.size, ip, 5334)
                    ds.send(sp)
                }

            } catch (e: Exception) {
                println("Exception occurred")
            }  finally {
                ds.close()
            }
        }


        var threadReceive = Thread {

            try {
                while (true) {
                    val rd = ByteArray(1000)

                    val sp1 = DatagramPacket(rd, rd.size)
                    ds.receive(sp1)

                    val msg = String(rd).trim { it <= ' ' }

                    val sB = StringBuilder()

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
                println("Exception occured")
            }  finally {
                ds.close()
            }
        }

        threadSend.start()
        threadReceive.start()

    }
}