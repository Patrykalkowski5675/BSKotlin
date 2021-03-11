package com.example.demo.controller.TCP.chat

import com.example.demo.controller.Controller
import java.io.DataInputStream
import java.io.DataOutputStream
import java.net.ServerSocket
import java.net.Socket
import java.security.Key
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue
import javax.crypto.Cipher
import javax.crypto.CipherInputStream
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec


object TCPServerChatController : TCPChatController {

    @Volatile
    private var flagStart: Boolean = false
    private var running: Boolean = false

    private var ss = ServerSocket(5334)
    private var s: Socket? = null
    private lateinit var outTo: DataOutputStream
    private lateinit var br: DataInputStream

    private lateinit var queue: ConcurrentLinkedQueue<String>
    private lateinit var queueReceive: ConcurrentLinkedQueue<String>
    private lateinit var mode: Controller.Companion.Modes
    private lateinit var sessionKey: Key

    private const val initVector = "encryptionIntVec"
    private val iv = IvParameterSpec(initVector.toByteArray(charset("UTF-8")))


    fun initChat(queue: ConcurrentLinkedQueue<String>,
                          queueReceive: ConcurrentLinkedQueue<String>,
                          mode: Controller.Companion.Modes,
                          sessionKey: Key) {
        this.queue = queue
        this.queueReceive = queueReceive
        this.mode = mode
        this.sessionKey = sessionKey
        running = true
        initThreadSend()
        initThreadReceive()
    }

    private fun initThreadSend() {

        val cipher = initCipher(Cipher.ENCRYPT_MODE)

        val threadSend = Thread {
            var sd: String
            try {
                while (running) {
                    while (queue.isNotEmpty()) {
                        sd = queue.poll()

                        val encryptedMessageBytes = cipher.doFinal(sd.toByteArray())

                        outTo.writeInt(encryptedMessageBytes.size)
                        outTo.write(encryptedMessageBytes)
                        outTo.flush()
                    }
                }

            } catch (e: Exception) {
                println("Exception occurred")
//                initThreadSend()
                e.printStackTrace()
            }
        }

        threadSend.start()
    }

    private fun initThreadReceive() {

        val cipher = initCipher(Cipher.DECRYPT_MODE)

        Thread {
            try {
                s = ss.accept()

                outTo = DataOutputStream(s!!.getOutputStream())
                br = DataInputStream(s!!.getInputStream())

                flagStart = true
                queueReceive.add("*User join to chat TCP")
                queueReceive.add("*Sync settings TCP")

                //syncSettingAndSendPublicKeyOnStartChat()

                while (running) {
                    val length: Int = br.readInt()
                    val message = ByteArray(length)
                    for (i in message.indices)
                        message[i] = br.readByte()

                    val encryptedMessageBytes = cipher.doFinal(message)

                    queueReceive.add(String(encryptedMessageBytes))
                }
            } catch (e: Exception) {
                running = false
                queueReceive.add("*User left chat")
                println("ServerRecive")
                outTo.close()
                br.close()
                s?.close()
//                ss.close()
                flagStart = false
                running = false
                queueReceive.add("_Messag8e1")
//                e.printStackTrace()
//                initThreadReceive()
            }
        }.start()
    }

    private fun syncSettingAndSendPublicKeyOnStartChat() {
        when (Controller.mode) {
            Controller.Companion.Modes.EBC -> queue.add("_Messag6e1")
            Controller.Companion.Modes.CBC -> queue.add("_Messag6e2")
            Controller.Companion.Modes.CFB -> queue.add("_Messag6e3")
            Controller.Companion.Modes.OFB -> queue.add("_Messag6e4")
        }
    }


    private fun initCipher(cipherMode: Int): Cipher {
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
        running = false
        Thread.sleep(500)
        br.close()
//                ss.close()
        s?.close()
    }
}