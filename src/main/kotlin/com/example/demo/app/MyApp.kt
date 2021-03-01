package com.example.demo.app

import com.example.demo.view.MainView
import javafx.application.Application
import javafx.stage.Stage
import tornadofx.App



fun main(args: Array<String>) {
    Application.launch(MyApp::class.java, *args)
}

class MyApp : App(MainView::class) {
    companion object{
        lateinit var param : String
        lateinit var ip : String
    }
    override fun start(stage: Stage) {
        param = parameters.unnamed[0]
        ip = parameters.unnamed[1]
        println(param)
        println(ip)
        super.start(stage)
    }

}


