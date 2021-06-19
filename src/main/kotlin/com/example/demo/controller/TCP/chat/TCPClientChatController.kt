package com.example.demo.controller.TCP.chat

import com.example.demo.controller.Controller
import com.example.demo.tools.Utility
import java.io.DataInputStream
import java.io.DataOutputStream
import java.net.Socket
import java.security.Key
import java.util.concurrent.ConcurrentLinkedQueue
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec


class TCPClientChatController(private val queue: ConcurrentLinkedQueue<String>,
                              private val queueReceive: ConcurrentLinkedQueue<String>,
                              private var mode: Controller.Companion.Modes,
                              private val sessionKey: Key,
                              private var secondUserIP: String) : TCPChatController {

    @Volatile
    private var flagStart: Boolean = false
    private var running: Boolean = true
    private var invoked: Boolean = false

    private var s: Socket? = null
    private var inStream: DataInputStream? = null
    private var outStream: DataOutputStream? = null

    private var handleThreadSend: Thread? = null
    private var handleThreadReceive: Thread? = null

    private var handleCipherSend: Cipher? = null
    private var handleCipherReceive: Cipher? = null

    init {
        initThreadReceive()
        initThreadSend()
        running = true
    }


    private fun initThreadSend() {

        handleCipherSend = Utility.initCipher(Cipher.ENCRYPT_MODE, mode, sessionKey)

        handleThreadSend = Thread {
            try {
                var sd: String
                while (running) {
                    while (queue.isEmpty());
                    if (flagStart) {
                        sd = queue.poll()
                        val encryptedMessageBytes = handleCipherSend!!.doFinal(sd.toByteArray())
                        outStream?.writeInt(encryptedMessageBytes.size)
                        outStream?.write(encryptedMessageBytes)

                        outStream?.flush()
                    }
                }
            } catch (e: Exception) {
                println("Exception occured");
                initThreadSend()
            }
        }
        handleThreadSend!!.start()
    }

    private fun initThreadReceive() {

        handleCipherReceive = Utility.initCipher(Cipher.DECRYPT_MODE, mode, sessionKey)

//        if (!running) return
        handleThreadReceive = Thread {
            try {
//                println(secondUserIP)
                s = Socket(secondUserIP, 5334)
                inStream = DataInputStream(s!!.getInputStream())
                outStream = DataOutputStream(s!!.getOutputStream())

                flagStart = true
                invoked = true

                while (true) {

                    val length: Int? = inStream?.readInt()
                    val message = length?.let { ByteArray(it) }
                    if (message != null) {
                        for (i in message.indices)
                            message[i] = inStream?.readByte()!!
                    }

                    val encryptedMessageBytes = handleCipherReceive!!.doFinal(message)

                    queueReceive.add(String(encryptedMessageBytes))
                }
            } catch (e: Exception) {
                queueReceive.add("*Server TCP is not available, please wait, reconnecting in 5 second...")
//                inStream.close()
                s?.close()
                flagStart = false
                println("errorCLREV")
                Thread.sleep(5000)
                if (invoked) {
                    queueReceive.add("_Messag8e1")
                    invoked = false
                }
                initThreadReceive()
            }
        }
        handleThreadReceive!!.start()
    }

    override fun changeCipherMode(mode: Controller.Companion.Modes) {
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
        try {
//            handleThreadSend?.interrupt()
//            handleThreadReceive?.interrupt()
        } catch (e: Exception) {

        }
    }

}