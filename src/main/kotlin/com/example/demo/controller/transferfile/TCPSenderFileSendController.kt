package com.example.demo.controller.transferfile

import com.example.demo.controller.Controller
import javafx.application.Platform
import javafx.scene.control.ChoiceBox
import javafx.scene.control.ProgressBar
import javafx.scene.text.Text
import java.io.BufferedInputStream
import java.io.File
import java.io.FileInputStream
import java.net.ServerSocket
import javax.crypto.Cipher
import javax.crypto.CipherOutputStream
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import kotlin.math.roundToInt


object TCPSenderFileSendController {

    fun sendFile(file: File, mode : Controller.Companion.Modes, progressBar: ProgressBar, progressText: Text, controller: Controller) {

        val encryptionKeyString = "thisisa128bitkey"
        val encryptionKeyBytes = encryptionKeyString.toByteArray()
        val initVector = "encryptionIntVec"
        val iv = IvParameterSpec(initVector.toByteArray(charset("UTF-8")))

        Thread {
            val servsock = ServerSocket(13267)
//           while (true) {
            println("Waiting...")
            val sock = servsock.accept()
            println("Accepted connection : $sock")

            val fis = FileInputStream(file)
            val bis = BufferedInputStream(fis)
            val os = sock.getOutputStream()
            println("Sending...")


            val cipher: Cipher = when (mode) {
                Controller.Companion.Modes.EBC -> Cipher.getInstance("AES/ECB/PKCS5Padding")
                Controller.Companion.Modes.CBC -> Cipher.getInstance("AES/CBC/PKCS5Padding")
                Controller.Companion.Modes.CFB -> Cipher.getInstance("AES/CFB/PKCS5Padding")
                Controller.Companion.Modes.OFB -> Cipher.getInstance("AES/OFB/PKCS5Padding")
                else -> Cipher.getInstance("AES/ECB/PKCS5Padding")
            }

            val secretKey: SecretKey = SecretKeySpec(encryptionKeyBytes, "AES")

            if (mode == Controller.Companion.Modes.EBC)
                cipher.init(Cipher.ENCRYPT_MODE, secretKey)
            else
                cipher.init(Cipher.ENCRYPT_MODE, secretKey, iv)

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
            sock.close()
            servsock.close()

            Platform.runLater {
                progressBar.progress = 1.0
                progressText.text = "Completed"
                controller.changesPostReciveFile()
            }
//            }
        }.start()

    }
}