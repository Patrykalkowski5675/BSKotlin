package com.example.demo.app

import com.example.demo.controller.Controller
import com.example.demo.view.MainView
import javafx.application.Application
import javafx.stage.Stage
import tornadofx.App



fun main(args: Array<String>) {
    Application.launch(MyApp::class.java, *args)
}

class MyApp : App(MainView::class) {
    override fun start(stage: Stage) {

        Controller.whoiam = parameters.unnamed[0]

        if (parameters.unnamed.size >= 2)
        Controller.ip = parameters.unnamed[1] ?:"Not given"

        super.start(stage)
    }
}


