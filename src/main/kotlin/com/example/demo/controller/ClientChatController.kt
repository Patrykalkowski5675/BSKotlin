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


object ClientChatController {

    private var dtf: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")
//    val ds = DatagramSocket(5334)
//    val ip = InetAddress.getLocalHost()

    var s = Socket("localhost", 5334)
//    var dos: DataOutputStream = DataOutputStream(s.getOutputStream())
var pwrite: PrintWriter = PrintWriter(s.getOutputStream(), true)
    var br = BufferedReader(InputStreamReader(s.getInputStream()))

    fun initClientChat(queue: ConcurrentLinkedQueue<String>, textAreaChat: TextArea, iPText: TextField) {

        iPText.text = MyApp.ip

        println("Running")
        println("Client is Up....")

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
                println("Exception occured");
            }
            finally {
                s.close()
            }
        }

        var threadRecive = Thread {
            try {
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
                System.out.println("Exception occured")
            }
            finally {
                s.close()
            }
        }

        threadSend.start()
        threadRecive.start()
    }
}