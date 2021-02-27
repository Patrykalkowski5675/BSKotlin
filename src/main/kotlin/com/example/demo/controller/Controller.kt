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
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintStream
import java.net.ServerSocket
import java.net.URL
import java.nio.charset.StandardCharsets
import java.util.*
import java.util.concurrent.BlockingQueue
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.math.roundToInt


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
    lateinit var cBMode: ChoiceBox<String>


    private val queue = ConcurrentLinkedQueue<String>()

    enum class modes {
        EBC,
        CBC,
        OTB,
        ABC,

    }

    @FXML
    override fun initialize(location: URL?, resources: ResourceBundle?) {
        initButtonSend()
        initMenu()
        initRadioBox()
        initButtonApplay()
        initChat()
        initTextFieldEnterText()
    }


    fun initChat() {

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
            }
            catch (e : Exception) {
                initChat()
            }
            finally {

                ps.close()
                br.close()
                ss.close()
                s.close()

                textAreaChat.appendText("User left chat" + '\n')
                println("koniec")
            } // end of while
        }.start()

    }

    private fun initTextFieldEnterText() {
        tFEnterText.setOnAction { queue.add(tFEnterText.text)
            println(tFEnterText.text)
            tFEnterText.clear()
        }

    }


    fun initButtonApplay() {
        bTApply.setOnAction { println(iPText.text) }
    }

    fun initRadioBox() {
        cBMode.items.addAll(modes.EBC.name, modes.CBC.name, modes.OTB.name, modes.ABC.name)
        cBMode.setOnAction { println(cBMode.selectionModel.selectedItem) }
    }

    fun initMenu() {
        mHelp.setOnAction {

            val secondLabel = Label("Program wykonany w ramach projektu\nna przedmiot BSK przez: \n Patryk Kalkowski 175669")

            val secondaryLayout = StackPane()
            secondaryLayout.children.add(secondLabel)
            val secondScene = Scene(secondaryLayout, 300.0, 100.0)

            val stage = Stage()
            stage.scene = secondScene

            stage.title = "ABC"
            stage.show()
        }
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