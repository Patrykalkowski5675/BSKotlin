package com.example.demo.controller.TCP.chat

import com.example.demo.controller.Controller
import com.example.demo.controller.UDP.chat.UDPChatController
import javafx.application.Platform
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintStream
import java.net.ServerSocket
import java.net.Socket
import java.time.format.DateTimeFormatter
import java.util.concurrent.ConcurrentLinkedQueue


class TCPServerChatController(val queue: ConcurrentLinkedQueue<String>,val queueReceive: ConcurrentLinkedQueue<String>,val controller: Controller) : TCPChatController {

    @Volatile
    var flagStart: Boolean = false

    @Volatile
    var running : Boolean = false

    val dtf: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")
    var ss = ServerSocket(5334)
    var ps: PrintStream? = null
    var s: Socket? = null
    var br: BufferedReader? = null

    override fun initChat() {
        running = true
        initThreadSend()
        initThreadReceive()

    }

     private fun initThreadSend() {
        val threadSend = Thread {
            var sd: String
            try {
                while (running) {
                    while (queue.isEmpty());
                    if (flagStart) {
                        sd = queue.poll()
                        ps?.println(sd)
                    }
                }

            } catch (e: Exception) {
                println("Exception occurred")
                initThreadSend()
            }
        }

        threadSend.start()
    }

    private fun initThreadReceive() {
        val threadReceive = Thread {
            try {
                println("Running")
                println("Server is Up....")
//                ss = ServerSocket(5334)
                s = ss.accept()
                Platform.runLater { controller.syncSettingBetweenUsersOnStartChat() }
                flagStart = true
                queueReceive.add("*User join to chat TCP")
                queueReceive.add("*Sync settings TCP")
                ps = PrintStream(s!!.getOutputStream())
                br = BufferedReader(InputStreamReader(s!!.getInputStream()))
                while (running) {

                    val msg = br!!.readLine().trim { it <= ' ' }

                    queueReceive.add(msg)

                }
            } catch (e: Exception) {
                queueReceive.add("*User left chat")
                ps?.close()
                br?.close()
//                ss.close()
                s?.close()
                flagStart = false
                initThreadReceive()

            }
        }
        threadReceive.start()
    }

    override fun stop(){
        running = false
        Thread.sleep(500)
        ps?.close()
        br?.close()
//                ss.close()
        s?.close()
    }
}