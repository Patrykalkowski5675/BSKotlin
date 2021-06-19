package com.example.demo.controller.TCP.chat

import com.example.demo.controller.Controller

interface TCPChatController {
    fun stop()
    fun changeCipherMode(mode : Controller.Companion.Modes)
}