package com.example.demo.controller.TCP.chat

import com.example.demo.controller.Controller
import java.io.DataInputStream
import java.io.DataOutputStream
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.Socket
import java.security.Key
import java.util.concurrent.ConcurrentLinkedQueue
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec


class TCPServerChatController(val queue: ConcurrentLinkedQueue<String>,
                              val queueReceive: ConcurrentLinkedQueue<String>,
                              val mode: Controller.Companion.Modes,
                              val sessionKey: Key
                              ) : TCPChatController {

    @Volatile
    private var flagStart: Boolean = false
    private var running: Boolean = false

    private var ss = ServerSocket(5334)
    private var s: Socket? = null
    private var inStream: DataInputStream? = null
    private var outStream: DataOutputStream? = null


    private var handleThreadSend: Thread? = null
    private var handleThreadReceive: Thread? = null


   init{
        initThreadReceive()
        initThreadSend()
        running = true
    }

    private fun initThreadSend() {
        val cipher = initCipher(Cipher.ENCRYPT_MODE)

        handleThreadSend = Thread {
            var sd: String
            try {
                while (running) {
                    while (queue.isEmpty());
                    sd = queue.poll()

                    val encryptedMessageBytes = cipher.doFinal(sd.toByteArray())

                    outStream?.writeInt(encryptedMessageBytes.size)
                    outStream?.write(encryptedMessageBytes)
                    outStream?.flush()

                }

            } catch (e: Exception) {
                println("Exception occurred")
//                initThreadSend()
                e.printStackTrace()
            }
        }

        handleThreadSend!!.start()
    }

    private fun initThreadReceive() {

        val cipher = initCipher(Cipher.DECRYPT_MODE)

        handleThreadReceive = Thread {
            try {
                s = ss.accept()
                println((s!!.remoteSocketAddress as InetSocketAddress).address)
                outStream = DataOutputStream(s!!.getOutputStream())
                inStream = DataInputStream(s!!.getInputStream())

                flagStart = true
                queueReceive.add("*User join to chat TCP")
                queueReceive.add("*Sync settings TCP")

                //syncSettingAndSendPublicKeyOnStartChat()

                while (running) {
                    val length: Int? = inStream?.readInt()
                    val message = length?.let { ByteArray(it) }
                    if (message != null) {
                        for (i in message.indices)
                            message[i] = inStream!!.readByte()
                    }
                    val encryptedMessageBytes = cipher.doFinal(message)

                    queueReceive.add(String(encryptedMessageBytes))
                }
            } catch (e: Exception) {
                running = false
                queueReceive.add("*User left chat")
                e.printStackTrace()
                outStream?.close()
                inStream?.close()
                s?.close()
//                ss.close()
                flagStart = false
                running = false
                queueReceive.add("_Messag8e1")
//                e.printStackTrace()
//                initThreadReceive()
            }
        }
        handleThreadReceive!!.start()
    }


    override fun stop() {
        running = false
        outStream?.close()
        inStream?.close()
        s?.close()
        ss.close()
        try {
//            handleThreadSend?.interrupt()
//            handleThreadReceive?.interrupt()
        }catch (e : Exception){

        }

//        running = false
//        Thread.sleep(500)
//        inStream.close()
////                ss.close()
//        s.close()
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
        val initVector = "encryptionIntVec"
        val iv = IvParameterSpec(initVector.toByteArray(charset("UTF-8")))

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


}