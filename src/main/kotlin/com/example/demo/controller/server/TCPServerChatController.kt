package com.example.demo.controller.server

import com.example.demo.controller.Controller
import javafx.application.Platform
import javafx.scene.control.TextArea
import javafx.scene.control.TextField
import javafx.scene.text.Text
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintStream
import java.net.ServerSocket
import java.net.Socket
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.ConcurrentLinkedQueue


object TCPServerChatController {

    @Volatile
    var flagStart: Boolean = false

    val dtf: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")
    var ss = ServerSocket(5334)
    lateinit var ps: PrintStream
    lateinit var s: Socket
    lateinit var br: BufferedReader


    private fun initThreadSend(queue: ConcurrentLinkedQueue<String>){
        val threadSend = Thread {
            var sd: String
            try {
                while (true) {
                    while (queue.isEmpty());
                    if (flagStart) {
                        sd = queue.poll()
                        ps.println(sd)
                    }
                }

            } catch (e: Exception) {
                println("Exception occurred")
                initThreadSend(queue)
            }
        }

        threadSend.start()
    }

    private fun initThreadReceive(queueReceive: ConcurrentLinkedQueue<String>){
        val threadReceive = Thread {
            try {
                println("Running")
                println("Server is Up....")
//                ss = ServerSocket(5334)
                s = ss.accept()
                flagStart = true
                queueReceive.add("*User join to chat")
                ps = PrintStream(s.getOutputStream())
                br = BufferedReader(InputStreamReader(s.getInputStream()))
                while (true) {

                    val msg = br.readLine().trim { it <= ' ' }

                    queueReceive.add(msg)

                }
            } catch (e: Exception) {
                queueReceive.add("*User left chat")
                ps.close()
                br.close()
//                ss.close()
                s.close()
                flagStart = false
                initThreadReceive(queueReceive)

            }
        }
        threadReceive.start()
    }


    fun initServerChat(queue: ConcurrentLinkedQueue<String>, queueReceive: ConcurrentLinkedQueue<String>) {
        initThreadSend(queue)
        initThreadReceive(queueReceive)

    }
}