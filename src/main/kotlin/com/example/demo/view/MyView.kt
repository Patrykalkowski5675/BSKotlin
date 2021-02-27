package com.example.demo.view

import javafx.fxml.FXMLLoader
import javafx.scene.Parent
import tornadofx.View


class MainView : View() {
    override val root: Parent = FXMLLoader.load(javaClass.classLoader.getResource("sample.fxml"))
}