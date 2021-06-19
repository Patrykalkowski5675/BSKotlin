package com.example.demo.controller.TCP.transferfile

import com.example.demo.controller.Controller
import javafx.application.Platform
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
import javax.crypto.spec.IvParameterSpec
import kotlin.math.roundToInt


object TCPReceiverFileController {

    private var handleSocket: Socket? = null
    private var handleThread: Thread? = null

    fun receiveFile(mode: Controller.Companion.Modes,
                    progressBar: ProgressBar,
                    fileName: String,
                    fileSize: Long,
                    progressText: Text,
                    queue: ConcurrentLinkedQueue<String>,
                    queueReceive: ConcurrentLinkedQueue<String>,
                    sessionKey: Key,
                    secondUserIP: String
    ) {

        handleThread = Thread {
            var cipherOut: CipherOutputStream? = null
            var clientData: DataInputStream? = null
            try {
                handleSocket = Socket(secondUserIP, 13267)

                println("Connecting...")
                val bufferSize = handleSocket!!.receiveBufferSize

                clientData = DataInputStream(handleSocket!!.getInputStream())
                println("n" + fileName + "n")
                val os: OutputStream = FileOutputStream(fileName)

                /// CFB and OFB operate on 64-bit block by default
                /// cipher.blockSize
                //val cipher = Utility.initCipher(Cipher.DECRYPT_MODE, mode, sessionKey)

                val iv = ByteArray(16)
                clientData.read(iv)


                val cipher: Cipher = when (mode) {
                    Controller.Companion.Modes.EBC -> Cipher.getInstance("AES/ECB/PKCS5Padding")
                    Controller.Companion.Modes.CBC -> Cipher.getInstance("AES/CBC/PKCS5Padding")
                    Controller.Companion.Modes.CFB -> Cipher.getInstance("AES/CFB/PKCS5Padding")
                    Controller.Companion.Modes.OFB -> Cipher.getInstance("AES/OFB/PKCS5Padding")
                }


                if (mode == Controller.Companion.Modes.EBC)
                    cipher.init(Cipher.DECRYPT_MODE, sessionKey)
                else
                    cipher.init(Cipher.DECRYPT_MODE, sessionKey, IvParameterSpec(iv))





                cipherOut = CipherOutputStream(os, cipher)

                val buffer = ByteArray(bufferSize)

                var bufferCount = 16L
                var read: Int
                var tmp: Double

                while (clientData.read(buffer).also { read = it } != -1) {
                    cipherOut.write(buffer, 0, read)
                    bufferCount += 4096
                    Platform.runLater {
                        tmp = bufferCount.toDouble() / fileSize
                        progressBar.progress = tmp
                        progressText.text = (tmp * 100).roundToInt().toString() + "%"
                    }
                }
                cipherOut.flush()
                cipherOut.close()
                handleSocket?.close()

                Platform.runLater {
                    progressBar.progress = 1.0
                    progressText.text = "Completed"
                }
                queueReceive.add("*Transfer file successfully!")
                queueReceive.add("_Messag5eN")
            } catch (e: Exception) {
                e.printStackTrace()
                queueReceive.add("*The download was interrupted")
                queueReceive.add("_Messag5eN")
                cipherOut?.close()
                clientData?.close()
                handleSocket?.close()

            }
        }
        handleThread!!.start()
    }

    fun stopReceiving() {
        handleSocket?.close()
        handleThread?.interrupt()
    }

}