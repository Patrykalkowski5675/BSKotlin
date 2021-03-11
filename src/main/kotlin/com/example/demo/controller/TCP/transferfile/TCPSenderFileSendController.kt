package com.example.demo.controller.TCP.transferfile

import com.example.demo.controller.Controller
import com.example.demo.tools.Utility
import javafx.application.Platform
import javafx.scene.control.ProgressBar
import javafx.scene.text.Text
import java.io.BufferedInputStream
import java.io.File
import java.io.FileInputStream
import java.net.ServerSocket
import java.net.Socket
import java.security.Key
import java.util.concurrent.ConcurrentLinkedQueue
import javax.crypto.Cipher
import javax.crypto.CipherOutputStream
import javax.crypto.spec.IvParameterSpec
import kotlin.math.roundToInt


object TCPSenderFileSendController {

    var handleSocket: Socket? = null

    fun sendFile(file: File, mode: Controller.Companion.Modes,
                 progressBar: ProgressBar, progressText: Text,
                 queue: ConcurrentLinkedQueue<String>,
                 queueReceive: ConcurrentLinkedQueue<String>,
                 sessionKey: Key
    ) {

        Thread {
            var serverSocket: ServerSocket? = null
            var cipherOut: CipherOutputStream? = null
            try {
                serverSocket = ServerSocket(13267)
//            println("Waiting...")
                handleSocket = serverSocket.accept()
//            println("Accepted connection : $sock")
                queueReceive.add("_Messag1eS|Start transfer file")

                val fis = FileInputStream(file)
                val bis = BufferedInputStream(fis)
                val os = handleSocket!!.getOutputStream()
//            println("Sending...")


                val cipher = Utility.initCipher(Cipher.ENCRYPT_MODE,mode,sessionKey)

                cipherOut = CipherOutputStream(os, cipher)

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
                        progressText.text = (tmp * 100).roundToInt().toString() + '%'
                    }
                }
                cipherOut.flush()
                cipherOut.close()
                handleSocket?.close()
                serverSocket.close()

                Platform.runLater {
                    progressBar.progress = 1.0
                    progressText.text = "Completed"
                }

                queueReceive.add("_Messag5eN")
                queueReceive.add("_Messag1eS|Transfer file successfull")
            } catch (e: Exception) {
                queueReceive.add("*The download was interrupted")
                queueReceive.add("_Messag5eN")
                cipherOut?.close()
                handleSocket?.close()
                serverSocket?.close()
            }
        }.start()
    }

    fun stopReceiving() {
        handleSocket?.close()
    }

}