package com.example.demo.controller

import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.geometry.Pos
import javafx.scene.Scene
import javafx.scene.control.*
import javafx.scene.layout.StackPane
import javafx.scene.text.Text
import javafx.stage.FileChooser
import javafx.stage.Stage
import tornadofx.FX.Companion.primaryStage
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintStream
import java.net.ServerSocket
import java.net.URL
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.math.roundToInt
import kotlin.system.exitProcess


class Controller : Initializable {

    @FXML
    lateinit var iPText: TextField
    lateinit var textAreaChat: TextArea
    lateinit var bTChoose: Button
    lateinit var bTSend: Button
    lateinit var bTApply: Button
    lateinit var tFFile: TextField
    lateinit var tFEnterText: TextField
    lateinit var tSize: Text
    lateinit var mHelp: MenuItem
    lateinit var mQuit: MenuItem
    lateinit var cBMode: ChoiceBox<String>
    lateinit var progressBar : ProgressBar


    private val queue = ConcurrentLinkedQueue<String>()

    var dtf = DateTimeFormatter.ofPattern("HH:mm:ss")
    var timePoint = LocalDateTime.now()

    enum class Modes {
        EBC,
        CBC,
        OTB,
        ABC,
    }

    @FXML
    override fun initialize(location: URL?, resources: ResourceBundle?) {
        initButtonChooseFile()
        initMenu()
        initRadioBox()
        initButtonApplay()
        initChat()
        initTextFieldEnterText()
    }


    private fun initChat() {

        Thread {
            val ss = ServerSocket(888)
            val s = ss.accept()

            textAreaChat.appendText("User join to chat" + '\n')

            val ps = PrintStream(s.getOutputStream())
            val br = BufferedReader(InputStreamReader(s.getInputStream()))

            try {
                while (true) {
                    var str: String?

                    while (br.readLine().also { str = it } != null) {
                        textAreaChat.appendText(str + '\n')

                        println(str)

                        if (!queue.isEmpty()) {
                            ps.println(queue.poll())
                        } else {
                            println("pusto")
                        }

                    }
                }
            } catch (e: Exception) {
                initChat()
            } finally {
                ps.close()
                br.close()
                ss.close()
                s.close()

                textAreaChat.appendText("User left chat" + '\n')
                println("koniec")
            }
        }.start()

    }

    private fun initTextFieldEnterText() {
        tFEnterText.setOnAction {
            if(!tFEnterText.text.isNullOrBlank()) {
                queue.add(tFEnterText.text)
                textAreaChat.appendText(dtf.format(LocalDateTime.now()) + " -> " + tFEnterText.text + '\n')
                tFEnterText.clear()
            }
        }
    }


    private fun initButtonApplay() {
        bTApply.setOnAction { println(iPText.text) }
    }

    private fun initRadioBox() {
        cBMode.items.addAll(Modes.EBC.name, Modes.CBC.name, Modes.OTB.name, Modes.ABC.name)
        cBMode.value = Modes.EBC.name
        cBMode.setOnAction { println(cBMode.selectionModel.selectedItem) }
    }

    private fun initMenu() {
        mHelp.setOnAction {
            val secondLabel = Label("Program wykonany w ramach projektu\nna przedmiot BSK przez: \nPatryk Kalkowski 175669")
            secondLabel.alignment = Pos.CENTER
            val secondaryLayout = StackPane()
            secondaryLayout.children.add(secondLabel)

            val secondScene = Scene(secondaryLayout, 300.0, 100.0)

            val stage = Stage()
            stage.scene = secondScene
            stage.title = "ABC"
            stage.show()
        }

        mQuit.setOnAction { exitProcess(1) }

    }


    private fun initButtonChooseFile() {
        bTChoose.setOnAction {
            val fileChooser = FileChooser()
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
                progressBar.progress = -1.0
            }
        }
    }
}