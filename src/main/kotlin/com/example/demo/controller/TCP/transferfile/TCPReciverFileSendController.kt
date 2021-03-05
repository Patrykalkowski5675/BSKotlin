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
import javax.crypto.Cipher
import javax.crypto.CipherOutputStream
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import kotlin.math.roundToInt


object TCPReciverFileSendController {

    fun reciveFile(cBMode: ChoiceBox<String>, progressBar: ProgressBar, fileName: String, fileSize: Long, progressText: Text, controller: Controller) {

        Thread {
            val encryptionKeyString = "thisisa128bitkey"
            val encryptionKeyBytes = encryptionKeyString.toByteArray()
            val initVector = "encryptionIntVec"
            val iv = IvParameterSpec(initVector.toByteArray(charset("UTF-8")))

            val sock = Socket("localhost", 13267)
            println("Connecting...")
            val bufferSize = sock.receiveBufferSize

            val clientData = DataInputStream(sock.getInputStream())
            println(fileName)
            val os: OutputStream = FileOutputStream(fileName)

            val cipher: Cipher = when (cBMode.value) {
                Controller.Companion.Modes.EBC.name -> Cipher.getInstance("AES/ECB/PKCS5Padding")
                Controller.Companion.Modes.CBC.name -> Cipher.getInstance("AES/CBC/PKCS5Padding")
                Controller.Companion.Modes.CFB.name -> Cipher.getInstance("AES/CFB/PKCS5Padding")
                Controller.Companion.Modes.OFB.name -> Cipher.getInstance("AES/OFB/PKCS5Padding")
                else -> Cipher.getInstance("AES/ECB/PKCS5Padding")
            }

            val secretKey: SecretKey = SecretKeySpec(encryptionKeyBytes, "AES")

            if (cBMode.value == Controller.Companion.Modes.EBC.name)
                cipher.init(Cipher.DECRYPT_MODE, secretKey)
            else
                cipher.init(Cipher.DECRYPT_MODE, secretKey, iv)


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
                controller.changesPostReciveFile()
            }
        }.start()
    }
}