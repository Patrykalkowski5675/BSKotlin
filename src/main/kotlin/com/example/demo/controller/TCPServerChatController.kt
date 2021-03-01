package com.example.demo.controller

import javafx.scene.control.TextArea
import javafx.scene.control.TextField
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintStream
import java.net.DatagramPacket
import java.net.InetAddress
import java.net.ServerSocket
import java.net.Socket
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.ConcurrentLinkedQueue


object TCPServerChatController{



    fun initServerChat(queue: ConcurrentLinkedQueue<String>, textAreaChat: TextArea, iPText: TextField) {

        iPText.text = "Server (Unable change IP)"
        iPText.isEditable = false
        iPText.isMouseTransparent = true

        println("Running")
        println("Server is Up....")

        val dtf: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")
        val ss = ServerSocket(5334)
//        val ip = InetAddress.getLocalHost()
        lateinit var ps: PrintStream
        lateinit var s : Socket
        lateinit var br: BufferedReader

        val threadSend = Thread {
            var sd : String
            try {
                while (true) {
                    while (queue.isEmpty());
                    sd = queue.poll()
                    ps.println(sd)
                }

            } catch (e: Exception) {
                println("Exception occurred")
            }
        }


        val threadReceive = Thread {
            try {
                s = ss.accept()
                textAreaChat.appendText("*User join to chat\n")
                ps = PrintStream(s.getOutputStream())
                br = BufferedReader(InputStreamReader(s.getInputStream()))
                while (true) {
//                    val rd = ByteArray(1000)

//                    val sp1 = DatagramPacket(rd, rd.size)
//                    ds.receive(sp1)

                    val msg = br.readLine().trim { it <= ' ' }

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
                textAreaChat.appendText("*User left chat\n")
                ps.close()
                br.close()
                ss.close()
                s.close()
                initServerChat(queue, textAreaChat, iPText)
            }
        }

        threadSend.start()
        threadReceive.start()

    }
}