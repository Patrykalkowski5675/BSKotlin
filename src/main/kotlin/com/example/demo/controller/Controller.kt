package com.example.demo.controller

import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.Scene
import javafx.scene.control.*
import javafx.scene.layout.StackPane
import javafx.scene.text.Text
import javafx.stage.FileChooser
import javafx.stage.Modality
import javafx.stage.Stage
import javafx.stage.StageStyle
import tornadofx.FX.Companion.primaryStage
import java.net.URL
import java.util.*
import kotlin.math.roundToInt


class Controller : Initializable {


    @FXML
    lateinit var iPText: TextField
    lateinit var textAreaChat: TextArea
    lateinit var bTChoose: Button
    lateinit var bTSend: Button
    lateinit var tFFile: TextField
    lateinit var tSize: Text
    lateinit var mHelp: MenuItem


    @FXML
    override fun initialize(location: URL?, resources: ResourceBundle?) {
        initButtonSend()
        initMenu()
    }


    fun initMenu(){
        mHelp.setOnAction {

            val secondLabel = Label("Program wykonany w ramach projektu\nna przedmiot BSK przez: \n Patryk Kalkowski 175669")

            val secondaryLayout = StackPane()
            secondaryLayout.children.add(secondLabel)
            val secondScene = Scene(secondaryLayout, 300.0, 100.0)

            val stage = Stage()
            stage.scene = secondScene

            stage.title = "ABC"
            stage.show() }
    }

    fun initButtonSend() {
        bTChoose.setOnAction {
            val fileChooser = FileChooser()
//              val extFilter = FileChooser.ExtensionFilter("TXT files (*.txt)", "*.txt")
//              fileChooser.extensionFilters.add(extFilter)
            val file = fileChooser.showOpenDialog(primaryStage)

            if (file != null) {
                tFFile.text = file.name

                if (((file.length()).toDouble() / (1024 * 1024 * 1024)).roundToInt() > 0)
                    tSize.text = "Size of file: " + (((file.length()).toDouble() / (1024 * 1024 * 1024) * 100).roundToInt() / 100.0) + " GB"
                else if (((file.length()).toDouble() / (1024 * 1024)).roundToInt() > 0)
                    tSize.text = "Size of file: " + (((file.length()).toDouble() / (1024 * 1024) * 100).roundToInt() / 100.0) + " MB"
                else if (((file.length()).toDouble() / (1024)).roundToInt() > 0)
                    tSize.text = "Size of file: " + (((file.length()).toDouble() / (1024) * 100).roundToInt() / 100.0) + " KB"
                else tSize.text = "Size of file: " + (((file.length()).toDouble() / (1024) * 100).roundToInt() / 100.0) + " B"

                bTSend.isDisable = false
            }
        }
    }


}