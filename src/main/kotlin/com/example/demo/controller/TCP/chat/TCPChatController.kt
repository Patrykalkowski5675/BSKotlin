package com.example.demo.controller.TCP.chat

import com.example.demo.controller.Controller
import java.io.DataInputStream
import java.io.DataOutputStream
import java.net.Socket
import java.security.Key
import java.util.concurrent.ConcurrentLinkedQueue
import javax.crypto.Cipher

interface TCPChatController {

    fun stop()

}