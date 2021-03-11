package com.example.demo.controller.TCP.transferfile

import com.example.demo.controller.Controller
import javafx.application.Platform
import javafx.scene.control.ProgressBar
import javafx.scene.text.Text
import java.io.BufferedInputStream
import java.io.File
import java.io.FileInputStream
import java.net.ServerSocket
import java.security.Key
import java.util.concurrent.ConcurrentLinkedQueue
import javax.crypto.Cipher
import javax.crypto.CipherOutputStream
import javax.crypto.spec.IvParameterSpec
import kotlin.math.roundToInt


object TCPSenderFileSendController {

    fun sendFile(file: File, mode: Controller.Companion.Modes,
                 progressBar: ProgressBar, progressText: Text,
                 queue: ConcurrentLinkedQueue<String>,
                 queueReceive: ConcurrentLinkedQueue<String>,
//                 sessionKey: Key
    ) {

        val initVector = "encryptionIntVec"
        val iv = IvParameterSpec(initVector.toByteArray(charset("UTF-8")))

        Thread {
            val serverSocket = ServerSocket(13267)
//            println("Waiting...")
            val socket = serverSocket.accept()
//            println("Accepted connection : $sock")
            queueReceive.add("_Messag1eS|Start transfer file")

            val fis = FileInputStream(file)
            val bis = BufferedInputStream(fis)
            val os = socket.getOutputStream()
//            println("Sending...")


            val cipher: Cipher = when (mode) {
                Controller.Companion.Modes.EBC -> Cipher.getInstance("AES/ECB/PKCS5Padding")
                Controller.Companion.Modes.CBC -> Cipher.getInstance("AES/CBC/PKCS5Padding")
                Controller.Companion.Modes.CFB -> Cipher.getInstance("AES/CFB/PKCS5Padding")
                Controller.Companion.Modes.OFB -> Cipher.getInstance("AES/OFB/PKCS5Padding")
            }
            
//            if (mode == Controller.Companion.Modes.EBC)
//                cipher.init(Cipher.ENCRYPT_MODE, sessionKey)
//            else
//                cipher.init(Cipher.ENCRYPT_MODE, sessionKey, iv)

            val cipherOut = CipherOutputStream(os, cipher)

            // val size = cipher.getOutputSize(myFile.length().toInt()).toLong()
            // dla duzych plikow ta funkcja zachowywala się dziwnie, np dla plikow ponad 10GB zwracala ponad 500%
            // zwykly size dzialal lepiej, a niescislosci wynikajace z paddigu sa nie do dostrzerzenia w progress bar

            val size = file.length()
            var bufferCount: Long = 0

            val data = ByteArray(4096) // 4k buffer, could be much larger
            var count: Int
            var tmp: Double

            while (bis.read(data).also { count = it } != -1) {
                cipherOut.write(data, 0, count)
                bufferCount += 4096
                Platform.runLater {
                    tmp = bufferCount.toDouble() / size
                    progressBar.progress = tmp
                    progressText.text = (tmp * 100).roundToInt().toString()
                }
            }
            cipherOut.flush()
            cipherOut.close()
            socket.close()
            serverSocket.close()

            Platform.runLater {
                progressBar.progress = 1.0
                progressText.text = "Completed"
                queue.add("*Transfer file ${file.name} successfully")
                queueReceive.add("_Messag5eN")
                queueReceive.add("_Messag1eS|Transfer file successfull")
            }
        }.start()

    }
}