package com.example.demo.app

import com.example.demo.view.MainView
import javafx.application.Application
import javafx.stage.Stage
import tornadofx.App


fun main(args: Array<String>) {
    Application.launch(MyApp::class.java, *args)
}

class MyApp : App(MainView::class) {

//    override fun start(stage: Stage) {
//        super.start(stage)
//    }

}


