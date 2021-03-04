package com.example.demo.controller.client

import com.example.demo.app.MyApp
import com.example.demo.controller.Controller
import com.example.demo.controller.server.TCPServerChatController
import com.example.demo.controller.transferfile.TCPSenderFileSendController
import javafx.application.Platform
import javafx.scene.control.TextArea
import javafx.scene.control.TextField
import javafx.scene.text.Text
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.Socket
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.ConcurrentLinkedQueue


object TCPClientChatController {

    @Volatile
    var flagStart: Boolean = false

    private var dtf: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")

    lateinit var s: Socket
    lateinit var pwrite: PrintWriter
    lateinit var br: BufferedReader

    private fun initThreadSend(queue: ConcurrentLinkedQueue<String>){

        var threadSend = Thread {
            var sd: String
            try {
                while (true) {
                    while (queue.isEmpty());
                    if(flagStart){
                        sd = queue.poll()
//                    dos.writeBytes(sd);
                        pwrite.println(sd);       // sending to server
                        pwrite.flush();
                        print(sd)
                    }

                }
            } catch (e: Exception) {
                println("Exception occured");
                initThreadSend(queue)
            }
        }.start()
    }

    private fun initThreadReceive(queueReceive: ConcurrentLinkedQueue<String>){
        var threadRecive = Thread {
            try {
                s = Socket("localhost", 5334)
                flagStart = true
                pwrite = PrintWriter(s.getOutputStream(), true)
                br = BufferedReader(InputStreamReader(s.getInputStream()))
                queueReceive.add("*Successfully connected with server")
                while (true) {

                    val msg = br.readLine().trim { it <= ' ' }
                    println("Server: $msg")

                    queueReceive.add(msg)

                }
            } catch (e: Exception) {
                queueReceive.add("*Server is not available, please wait, reconnecting in 5 second...")
                pwrite.close()
                br.close()
                s.close()
                flagStart = false
                Thread.sleep(5000)
                initThreadReceive( queueReceive)
            }
        }.start()
    }

    fun initClientChat(queue: ConcurrentLinkedQueue<String>, queueReceive: ConcurrentLinkedQueue<String>) {
        println("Running")
        println("Client is Up....")

        initThreadSend(queue)
        initThreadReceive(queueReceive)

    }


}