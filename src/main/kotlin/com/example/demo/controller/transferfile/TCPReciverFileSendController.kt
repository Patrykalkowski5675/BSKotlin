package com.example.demo.controller.transferfile

import com.example.demo.controller.Controller
import javafx.scene.control.ChoiceBox
import javafx.scene.control.ProgressBar
import javafx.scene.control.TextArea
import javafx.scene.control.TextField
import java.io.DataInputStream
import java.io.FileOutputStream
import java.io.OutputStream
import java.net.Socket
import java.util.concurrent.ConcurrentLinkedQueue
import javax.crypto.Cipher
import javax.crypto.CipherOutputStream
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec


object TCPReciverFileSendController {

    fun initClientFileSend(queue: ConcurrentLinkedQueue<String>, cBMode: ChoiceBox<String>, progressBar: ProgressBar, fileName : String, sizeFile : Long) {


        // filesize temporary hardcoded
        val start = System.currentTimeMillis()

        val encryptionKeyString = "thisisa128bitkey"
        val encryptionKeyBytes = encryptionKeyString.toByteArray()
        val initVector = "encryptionIntVec"
        val iv = IvParameterSpec(initVector.toByteArray(charset("UTF-8")))

        val sock = Socket("localhost", 13267)
        println("Connecting...")
        val bufferSize = sock.receiveBufferSize

        val clientData = DataInputStream(sock.getInputStream())
        println("file.txt")
        val os: OutputStream = FileOutputStream("file.txt")

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
        var read: Int
        while (clientData.read(buffer).also { read = it } != -1) {
            cipherOut.write(buffer, 0, read)
        }
        cipherOut.flush()
        val end = System.currentTimeMillis()
        println(end - start)
        cipherOut.close()
        sock.close()
    }
}