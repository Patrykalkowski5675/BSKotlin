package com.example.demo.controller

import com.example.demo.app.MyApp
import javafx.scene.control.TextArea
import javafx.scene.control.TextField
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.Socket
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.ConcurrentLinkedQueue


object TCPClientChatController {

    private var dtf: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")




    fun initClientChat(queue: ConcurrentLinkedQueue<String>, textAreaChat: TextArea, iPText: TextField) {

        iPText.text = MyApp.ip

        println("Running")
        println("Client is Up....")

        lateinit var s : Socket
        lateinit var pwrite: PrintWriter
        lateinit var br : BufferedReader

        var threadSend = Thread {
            var sd: String
            try {
                while (true) {
                    while (queue.isEmpty());
                    sd = queue.poll()
//                    dos.writeBytes(sd);
                    pwrite.println(sd);       // sending to server
                    pwrite.flush();
                    print(sd)
                }
            } catch (e: Exception) {
                pwrite.close()
                println("Exception occured");
            }
        }

        var threadRecive = Thread {
            try {
                s = Socket("localhost", 5334)
                pwrite = PrintWriter(s.getOutputStream(), true)
                br = BufferedReader(InputStreamReader(s.getInputStream()))
                threadSend.start()
                textAreaChat.appendText("Successfully connected with server")
                while (true) {
                    val rd = ByteArray(1000)

//                    val sp1 = DatagramPacket(rd, rd.size)
//                    ds.receive(sp1)

                    val msg =  br.readLine().trim { it <= ' ' }
                    println("Server: $msg")

                    var sB = StringBuilder()
                    if (Controller.namePartnerFlag) {
                        sB.append("\tServer:\n")
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
                textAreaChat.appendText("*Server is not available, please wait, reconnecting in 5 second...\n")
                Thread.sleep(5000)
                initClientChat(queue, textAreaChat, iPText)
            }
        }


        threadRecive.start()

    }
}