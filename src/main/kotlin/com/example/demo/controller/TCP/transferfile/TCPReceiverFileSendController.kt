package com.example.demo.controller.TCP.transferfile

import com.example.demo.controller.Controller
import javafx.application.Platform
import javafx.scene.control.ChoiceBox
import javafx.scene.control.ProgressBar
import javafx.scene.text.Text
import java.io.DataInputStream
import java.io.FileOutputStream
import java.io.OutputStream
import java.net.Socket
import java.security.Key
import java.util.concurrent.ConcurrentLinkedQueue
import javax.crypto.Cipher
import javax.crypto.CipherOutputStream
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import kotlin.math.roundToInt


object TCPReceiverFileSendController {

    fun reciveFile(mode: Controller.Companion.Modes,
                   progressBar: ProgressBar,
                   fileName: String,
                   fileSize: Long,
                   progressText: Text,
                   queue: ConcurrentLinkedQueue<String>,
                   queueReceive: ConcurrentLinkedQueue<String>,
//                   sessionKey: Key
    ) {

        Thread {

            val initVector = "encryptionIntVec"
            val iv = IvParameterSpec(initVector.toByteArray(charset("UTF-8")))

            val sock = Socket("localhost", 13267)
            println("Connecting...")
            val bufferSize = sock.receiveBufferSize

            val clientData = DataInputStream(sock.getInputStream())
            println("n" + fileName + "n")
            val os: OutputStream = FileOutputStream(fileName)

            /// CFB and OFB operate on 64-bit block by default
            /// cipher.blockSize
            val cipher: Cipher = when (mode) {
                Controller.Companion.Modes.EBC -> Cipher.getInstance("AES/ECB/PKCS5Padding")
                Controller.Companion.Modes.CBC -> Cipher.getInstance("AES/CBC/PKCS5Padding")
                Controller.Companion.Modes.CFB -> Cipher.getInstance("AES/CFB/PKCS5Padding")
                Controller.Companion.Modes.OFB -> Cipher.getInstance("AES/OFB/PKCS5Padding")
            }
//            val cipher: Cipher = when (mode) {
//                Controller.Companion.Modes.EBC -> Cipher.getInstance("AES/CBC/PKCS5Padding")
//                Controller.Companion.Modes.CBC -> Cipher.getInstance("AES/CBC/PKCS5Padding")
//                Controller.Companion.Modes.CFB -> Cipher.getInstance("AES/CFB/PKCS5Padding")
//                Controller.Companion.Modes.OFB -> Cipher.getInstance("AES/OFB/PKCS5Padding")
//            }


//            if (mode == Controller.Companion.Modes.EBC)
//                cipher.init(Cipher.DECRYPT_MODE, sessionKey)
//            else
//                cipher.init(Cipher.DECRYPT_MODE, sessionKey, iv)


            val cipherOut = CipherOutputStream(os, cipher)

            val buffer = ByteArray(bufferSize)

            var bufferCount = 0L
            var read: Int
            var tmp: Double

            while (clientData.read(buffer).also { read = it } != -1) {
                cipherOut.write(buffer, 0, read)
                bufferCount += 4096
                Platform.runLater {
                    tmp = bufferCount.toDouble() / fileSize
                    progressBar.progress = tmp
                    progressText.text = (tmp * 100).roundToInt().toString()
                }
            }
            cipherOut.flush()
            cipherOut.close()
            sock.close()

            Platform.runLater {
                progressBar.progress = 1.0
                progressText.text = "Completed"
                queue.add("*Transfer file $fileName successfully")
                queueReceive.add("_Messag5eN")
            }
        }.start()
    }
}