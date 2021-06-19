package com.example.demo.controller.TCP.chat

import com.example.demo.controller.Controller
import com.example.demo.tools.Utility
import java.io.DataInputStream
import java.io.DataOutputStream
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.Socket
import java.security.Key
import java.util.concurrent.ConcurrentLinkedQueue
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec


class TCPServerChatController(private val queue: ConcurrentLinkedQueue<String>,
                              private val queueReceive: ConcurrentLinkedQueue<String>,
                              private var mode: Controller.Companion.Modes,
                              private val sessionKey: Key) : TCPChatController {

    @Volatile
    private var flagStart: Boolean = false
    private var running: Boolean = false

    private var ss = ServerSocket(5334)
    private var s: Socket? = null
    private var inStream: DataInputStream? = null
    private var outStream: DataOutputStream? = null

    private var handleThreadSend: Thread? = null
    private var handleThreadReceive: Thread? = null

    private var handleCipherSend : Cipher? = null
    private var handleCipherReceive : Cipher? = null

    init {
        initThreadReceive()
        initThreadSend()
        running = true
    }

    private fun initThreadSend() {

        handleCipherSend = Utility.initCipher(Cipher.ENCRYPT_MODE, mode, sessionKey)

        handleThreadSend = Thread {
            var sd: String
            try {
                while (running) {
                    while (queue.isEmpty());
                    sd = queue.poll()

                    val encryptedMessageBytes =  handleCipherSend!!.doFinal(sd.toByteArray())

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

        handleCipherReceive = Utility.initCipher(Cipher.DECRYPT_MODE, mode, sessionKey)

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
                    val encryptedMessageBytes = handleCipherReceive!!.doFinal(message)

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

    override fun changeCipherMode(mode : Controller.Companion.Modes) {
       this.mode = mode
        while (queue.isNotEmpty() && queueReceive.isNotEmpty());
        handleCipherSend = Utility.initCipher(Cipher.ENCRYPT_MODE, mode, sessionKey)
        handleCipherReceive = Utility.initCipher(Cipher.DECRYPT_MODE, mode, sessionKey)
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
        } catch (e: Exception) {

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


}