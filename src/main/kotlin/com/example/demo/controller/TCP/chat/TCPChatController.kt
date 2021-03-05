package com.example.demo.controller.TCP.chat

import com.example.demo.controller.Controller
import java.util.concurrent.ConcurrentLinkedQueue

interface TCPChatController {

    fun initChat()
    fun stop()
}