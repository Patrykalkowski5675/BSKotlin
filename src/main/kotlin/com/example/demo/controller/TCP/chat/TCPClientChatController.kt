package com.example.demo.controller.TCP.chat

import com.example.demo.controller.Controller
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.Socket
import java.time.format.DateTimeFormatter
import java.util.concurrent.ConcurrentLinkedQueue


class TCPClientChatController(val queue: ConcurrentLinkedQueue<String>,val queueReceive: ConcurrentLinkedQueue<String>,val controller: Controller) : TCPChatController {

    @Volatile
    var flagStart: Boolean = false
    @Volatile
    var running : Boolean = false

    private var dtf: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")

    var s: Socket? = null
    var pwrite: PrintWriter? = null
    var br: BufferedReader? = null

    override fun initChat() {
        println("Running")
        println("Client is Up....")

        running = true
        initThreadSend()
        initThreadReceive()

    }


    private fun initThreadSend(){

        var threadSend = Thread {
            var sd: String
            try {
                while (running) {
                    while (queue.isEmpty());
                    if(flagStart){
                        sd = queue.poll()
//                    dos.writeBytes(sd);
                        pwrite?.println(sd);       // sending to server
                        pwrite?.flush();
                        print(sd)
                    }

                }
            } catch (e: Exception) {
                println("Exception occured");
                initThreadSend()
            }
        }.start()
    }

    private fun initThreadReceive(){
        if (!running) return
        var threadRecive = Thread {
            try {
                s = Socket("localhost", 5334)
                flagStart = true
                if(s != null) {
                    pwrite = PrintWriter(s!!.getOutputStream(), true)
                    br = BufferedReader(InputStreamReader(s!!.getInputStream()))
                    queueReceive.add("*Successfully connected with server TCP")
                    while (running) {

                        val msg = br?.readLine()?.trim { it <= ' ' }
                        println("Server: $msg")

                        queueReceive.add(msg)

                    }
                }
            } catch (e: Exception) {
                queueReceive.add("*Server TCP is not available, please wait, reconnecting in 5 second...")
                pwrite?.close()
                br?.close()
                s?.close()
                flagStart = false
                Thread.sleep(5000)
                initThreadReceive()
            }
        }.start()
    }


    override fun stop(){
       running = false

        pwrite?.close()
        br?.close()
        s?.close()
    }

}