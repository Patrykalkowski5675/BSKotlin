package com.example.demo.controller.TCP.chat

import com.example.demo.controller.Controller
import java.io.DataInputStream
import java.io.DataOutputStream
import java.net.Socket
import java.security.Key
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue
import javax.crypto.Cipher
import javax.crypto.CipherOutputStream
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec


object TCPClientChatController : TCPChatController {

    @Volatile
    private var flagStart: Boolean = false
    private var running: Boolean = true
    private var invoked: Boolean = false

    private var s: Socket? = null
    private var br: DataInputStream? = null
    private lateinit var outTo: DataOutputStream

    private lateinit var queue: ConcurrentLinkedQueue<String>
    private lateinit var queueReceive: ConcurrentLinkedQueue<String>
    private lateinit var mode: Controller.Companion.Modes
    private lateinit var sessionKey: Key

    private const val initVector = "encryptionIntVec"
    private val iv = IvParameterSpec(initVector.toByteArray(charset("UTF-8")))


    fun initChat(queue: ConcurrentLinkedQueue<String>,
                 queueReceive: ConcurrentLinkedQueue<String>,
                 mode: Controller.Companion.Modes,
                 sessionKey: Key,
                 ip: String) {
        this.queue = queue
        this.queueReceive = queueReceive
        this.mode = mode
        this.sessionKey = sessionKey

        running = true
        initThreadSend()
        initThreadReceive(ip)
    }


    private fun initThreadSend() {

        val cipher = initCipher(Cipher.ENCRYPT_MODE)

        Thread {
            try {
                var sd: String
                while (running) {
                    while (queue.isEmpty());
                    if (flagStart) {
                        sd = queue.poll()
                        val encryptedMessageBytes = cipher.doFinal(sd.toByteArray())
                        outTo.writeInt(encryptedMessageBytes.size)
                        outTo.write(encryptedMessageBytes)

                        outTo.flush()
                    }
                }
            } catch (e: Exception) {
                println("Exception occured");
                initThreadSend()
            }
        }.start()
    }

    private fun initThreadReceive(ip : String) {

        val cipher = initCipher(Cipher.DECRYPT_MODE)

//        if (!running) return
        var threadRecive = Thread {
            try {
                s = Socket(ip, 5334)
                br = DataInputStream(s!!.getInputStream())
                outTo = DataOutputStream(s!!.getOutputStream())

                flagStart = true
                invoked = true

                while (true) {

                    val length: Int = br!!.readInt()
                    val message = ByteArray(length)
                    for (i in message.indices)
                        message[i] = br!!.readByte()

                    val encryptedMessageBytes = cipher.doFinal(message)

                    queueReceive.add(String(encryptedMessageBytes))
                }
            } catch (e: Exception) {
                queueReceive.add("*Server TCP is not available, please wait, reconnecting in 5 second...")
                br?.close()
                s?.close()
                flagStart = false
                println("errorCLREV")
                Thread.sleep(5000)
                if (invoked) {
                    queueReceive.add("_Messag8e1")
                    invoked = false
                }
                initThreadReceive(ip)
            }
        }.start()
    }

    fun initCipher(cipherMode: Int): Cipher {
        val cipher: Cipher = when (mode) {
            Controller.Companion.Modes.EBC -> Cipher.getInstance("AES/ECB/PKCS5Padding")
            Controller.Companion.Modes.CBC -> Cipher.getInstance("AES/CBC/PKCS5Padding")
            Controller.Companion.Modes.CFB -> Cipher.getInstance("AES/CFB/PKCS5Padding")
            Controller.Companion.Modes.OFB -> Cipher.getInstance("AES/OFB/PKCS5Padding")
        }

        when (cipherMode) {
            Cipher.ENCRYPT_MODE -> {
                if (mode == Controller.Companion.Modes.EBC)
                    cipher.init(Cipher.ENCRYPT_MODE, sessionKey)
                else
                    cipher.init(Cipher.ENCRYPT_MODE, sessionKey, iv)
            }
            Cipher.DECRYPT_MODE -> {
                if (mode == Controller.Companion.Modes.EBC)
                    cipher.init(Cipher.DECRYPT_MODE, sessionKey)
                else
                    cipher.init(Cipher.DECRYPT_MODE, sessionKey, iv)
            }
        }
        return cipher
    }

    override fun stop() {
//        running = false

        br?.close()
        s?.close()
    }

}