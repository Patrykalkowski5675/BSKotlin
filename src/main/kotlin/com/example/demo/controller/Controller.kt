package com.example.demo.controller

import com.example.demo.controller.TCP.chat.TCPChatController
import com.example.demo.controller.TCP.chat.TCPClientChatController
import com.example.demo.controller.TCP.chat.TCPServerChatController
import com.example.demo.controller.TCP.transferfile.TCPReciverFileSendController
import com.example.demo.controller.TCP.transferfile.TCPSenderFileSendController
import com.example.demo.controller.UDP.chat.UDPChatController
import com.example.demo.tools.GenerateRSAKeys
import javafx.application.Platform
import javafx.event.EventHandler
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.geometry.Pos
import javafx.scene.Group
import javafx.scene.Scene
import javafx.scene.control.*
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.layout.AnchorPane
import javafx.scene.layout.StackPane
import javafx.scene.text.Text
import javafx.stage.FileChooser
import javafx.stage.Stage
import tornadofx.FX.Companion.primaryStage
import java.io.File
import java.net.URL
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.security.PrivateKey
import java.security.PublicKey
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.collections.ArrayList
import kotlin.system.exitProcess


class Controller : Initializable {

    @FXML
    lateinit var iPText: TextField
    lateinit var tIPHint: Text
    lateinit var textAreaChat: TextArea
    lateinit var bTChoose: Button
    lateinit var bTSend: Button
    lateinit var bTAccept: Button
    lateinit var bTDecline: Button
    lateinit var bTTCP: Button
    lateinit var bTUDP: Button
    lateinit var bTEBC: Button
    lateinit var bTCBC: Button
    lateinit var bTCFB: Button
    lateinit var bTOFB: Button
    lateinit var tFFile: TextField
    lateinit var tFEnterText: TextField
    lateinit var tSize: Text
    lateinit var mHelp: MenuItem
    lateinit var mQuit: MenuItem
    lateinit var progressBar: ProgressBar
    lateinit var progressText: Text
    lateinit var tSelectedFile: Text
    lateinit var rightPane: AnchorPane
    lateinit var leftPane: AnchorPane
    lateinit var textPassword: PasswordField
    lateinit var groupPassword: Group
    lateinit var buttonPassword: Button
    lateinit var image: ImageView


    private var fileSize: Long = 0L
    private var fileName = "hmmm.txt"
    private val queue = ConcurrentLinkedQueue<String>()
    private val queueReceive = ConcurrentLinkedQueue<String>()
    private var dtf: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")
    var listModeButtons = ArrayList<Button>()
    var file: File? = null
    lateinit var handleUDPChat: UDPChatController
    lateinit var handleTCPChat: TCPChatController

    lateinit var keys : Pair<PrivateKey, PublicKey>

    companion object {
        var nameUserFlag = true
        var namePartnerFlag = true

        enum class Modes {
            EBC,
            CBC,
            CFB,
            OFB,
        }

        enum class Protocols {
            TCP,
            UDP
        }

        var mode: Modes = Modes.EBC
        var protocol: Protocols = Protocols.TCP
        var ip: String = ""
        var whoiam: String = ""

    }

    @FXML
    override fun initialize(location: URL?, resources: ResourceBundle?) {
        primaryStage.maxHeight = 650.0

        initButtonChooseFile()
        initMenu()
        initChat()
        initTextFieldEnterText()
        initFnOnClose()
        initSendButton()
        initProtocolButtons()
        initTransferButtons()
        initModeButtons()
        initIPTextField()
        initWindowsForPassword()


    }

    private fun initWindowsForPassword() {
        fun changeVisibility(bool : Boolean){
            leftPane.isDisable = bool
            tFEnterText.isDisable = bool
            textAreaChat.isDisable = bool
            rightPane.isDisable = bool
            groupPassword.isVisible = bool
        }

        if (whoiam == "Client") return

        changeVisibility(true)
        image.image = Image(javaClass.classLoader.getResource("lock.png")?.toURI()?.toString())

        buttonPassword.setOnAction {
            changeVisibility(false)
            val str = textPassword.text
            val digest = MessageDigest.getInstance("SHA-256")
            val hash = digest.digest(str.toByteArray(StandardCharsets.UTF_8))


            keys = GenerateRSAKeys(hash).getKeys()
        }
    }


    //////////////////////// FILECHOOSE

    private fun initSendButton() {
        bTSend.setOnAction {
            queue.add("_Messag5eT|${fileSize}|${fileName}")
            rightPane.isDisable = true
            progressText.text = "Waiting for response"
        }
    }

    internal fun changesPostReciveFile() {
        queue.add("*Transfer file ${fileName} successfully")
        queueReceive.add("*Transfer file ${fileName} successfully")
        changeGUIforFileChoice()
    }

    internal fun changeGUIforFileTransfer(array: List<String>) {
        bTChoose.isVisible = false
        bTDecline.isVisible = true
        bTSend.isVisible = false
        bTAccept.isVisible = true

//        progressBar.progress = -1.0

        tSize.text = Utility.calculateSizeFile(array[1].toLong())
        tFFile.text = array[2]
        tSelectedFile.text = "File to transfer:"

        fileName = array[2]
        fileSize = array[1].toLong()

        rightPane.isDisable = true
    }

    internal fun changeGUIforFileChoice() {
        bTChoose.isVisible = true
        bTDecline.isVisible = false
        bTSend.isVisible = true
        bTSend.isDisable = true
        bTAccept.isVisible = false

        tFFile.text = ""
        tSize.text = "Size of file:"
        tSelectedFile.text = "Selected file:"

        Platform.runLater {
            progressText.text = "Sending is no init"
            progressBar.progress = 0.0
        }

        rightPane.isDisable = false
    }

    private fun initButtonChooseFile() {
        bTChoose.setOnAction {
            val fileChooser = FileChooser()
            file = fileChooser.showOpenDialog(primaryStage)

            if (file != null) {
                tFFile.text = file!!.name
                fileName = file!!.name
                fileSize = file!!.length()


                tSize.text = Utility.calculateSizeFile(fileSize)

                bTSend.isDisable = false
                Platform.runLater {
                    progressBar.progress = -1.0
                    progressText.text = "Waiting for send"
                }

            }
        }
    }

    private fun initTransferButtons() {
        bTAccept.setOnAction {
            queue.add("_Messag5eY")
            queue.add("*User accepted the file")
            TCPReciverFileSendController.reciveFile(mode, progressBar, fileName, fileSize, progressText, queue, queueReceive)
            rightPane.isDisable = true
        }
        bTDecline.setOnAction {
            queue.add("_Messag5eN")
            queue.add("*User rejected the file")
            changeGUIforFileChoice()
        }
    }


    //////////////////////// CHAT

    internal fun analizeHiddenMessage(msg: String): Boolean {

        if (msg.length < 10) return false

        val tmpSubString = msg.subSequence(0, 10)
        print(tmpSubString)

        when (tmpSubString) {
            "_Messag5eT" -> changeGUIforFileTransfer(msg.split('|'))
            "_Messag5eY" -> TCPSenderFileSendController.sendFile(file!!, mode, progressBar, progressText, queue, queueReceive)
            "_Messag5eN" -> changeGUIforFileChoice()
            "_Messag6e1" -> Platform.runLater {
                changeUIButtonsMode(0)
            }
            "_Messag6e2" -> Platform.runLater {
                changeUIButtonsMode(1)
            }
            "_Messag6e3" -> Platform.runLater {
                changeUIButtonsMode(2)
            }
            "_Messag6e4" -> Platform.runLater {
                changeUIButtonsMode(3)
            }
            "_Messag7e1" -> clickOnTCPButton()
            "_Messag7e2" -> clickOnUDPButton()
            else -> return false

        }
        return true
    }

    private fun initChat() {
        when (whoiam) {
            "Server" -> {
                handleTCPChat = TCPServerChatController(queue, queueReceive, this)
                handleTCPChat.initChat()
                handleUDPChat = UDPChatController(11111, 22222, queue, queueReceive, this)
//                handleUDPChat.initChat()

                iPText.text = "Server (Unable change IP)"
                iPText.isEditable = false
                iPText.isMouseTransparent = true
            }
            "Client" -> {
                handleTCPChat = TCPClientChatController(queue, queueReceive, this)
                handleTCPChat.initChat()
                handleUDPChat = UDPChatController(22222, 11111, queue, queueReceive, this)
//                handleUDPChat.initChat()
                iPText.text = ip
            }
            else -> println("Unknown parameter")
        }

        Thread {
            var msg = ""
            while (true) {
                if (!queueReceive.isEmpty()) {
                    msg = queueReceive.poll()

                    // function returns false if in msg in no hidden message
                    if (!analizeHiddenMessage(msg)) {

                        val sB = StringBuilder()
//
                        if (msg.isNotEmpty() && msg[0] != '*') {
                            if (namePartnerFlag) {
                                sB.append(protocol.name)
                                when (whoiam) {
                                    "Server" -> sB.append("\tClient:\n")
                                    "Client" -> sB.append("\tServer:\n")
                                }
                                namePartnerFlag = false
                                nameUserFlag = true
                            }
                            sB.append(dtf.format(LocalDateTime.now()))
                                    .append(" -> ")
                        }

                        sB.append(msg)
                                .append('\n')

                        Platform.runLater { textAreaChat.appendText(sB.toString()) }
                    }
                }
            }
        }.start()
    }

    internal fun syncSettingBetweenUsersOnStartChat() {
        when (mode) {
            Modes.EBC -> queue.add("_Messag6e1")
            Modes.CBC -> queue.add("_Messag6e2")
            Modes.CFB -> queue.add("_Messag6e3")
            Modes.OFB -> queue.add("_Messag6e4")
        }

//        when (protocol) {
//            Protocols.TCP -> queue.add("_Messag7e1")
//            Protocols.UDP -> queue.add("_Messag7e2")
//        }
    }

    private fun initTextFieldEnterText() {
        tFEnterText.setOnAction {
            if (!tFEnterText.text.isNullOrBlank()) {
                queue.add(tFEnterText.text)
                if (nameUserFlag) {
                    textAreaChat.appendText(protocol.name + "\t" + whoiam + ":\n" + dtf.format(LocalDateTime.now()) + " -> " + tFEnterText.text + '\n')
                    nameUserFlag = false
                    namePartnerFlag = true
                } else
                    textAreaChat.appendText(dtf.format(LocalDateTime.now()) + " -> " + tFEnterText.text + '\n')
                tFEnterText.clear()
            }
        }

        tFEnterText.textProperty().addListener { _, _, newValue ->
            if (newValue.length > 512) {
                val s = newValue.substring(0..512)
                tFEnterText.text = s
            }
        }


    }

    //////////////////////// SETTING

    private fun initIPTextField() {
        iPText.setOnAction {
            print(iPText.text)
            ip = iPText.text
            tIPHint.isVisible = false
        }
        iPText.onKeyPressed = EventHandler {
//            tIPHint.isVisible = iPText.text.length <= 16
            if (iPText.text.length > 15) tIPHint.text = "Invalid IP"
            else tIPHint.text = "Enter to save"
        }
        iPText.onMousePressed = EventHandler { tIPHint.isVisible = true }
    }

    private fun changeUIButtonsMode(index: Int) {

        when (index) {
            0 -> mode = Modes.EBC
            1 -> mode = Modes.CBC
            2 -> mode = Modes.CFB
            3 -> mode = Modes.OFB
        }

        for ((i, button) in listModeButtons.withIndex()) {
            if (index != i) {
                button.style = "-fx-background-radius : 0;"
            } else {
                button.style = "-fx-background-color:silver; -fx-background-radius : 0;"
            }
        }
    }

    private fun initModeButtons() {
        listModeButtons.add(bTEBC)
        listModeButtons.add(bTCBC)
        listModeButtons.add(bTCFB)
        listModeButtons.add(bTOFB)


        bTEBC.setOnAction {
            queue.add("_Messag6e1")
            changeUIButtonsMode(0)
        }
        bTCBC.setOnAction {
            queue.add("_Messag6e2")
            changeUIButtonsMode(1)
        }
        bTCFB.setOnAction {
            queue.add("_Messag6e3")
            changeUIButtonsMode(2)
        }
        bTOFB.setOnAction {
            queue.add("_Messag6e4")
            changeUIButtonsMode(3)
        }

    }


    private fun initProtocolButtons() {
        bTTCP.setOnAction {
            if (protocol != Protocols.TCP) {
                queue.add("_Messag7e1")
                clickOnTCPButton()
            }
        }

        bTUDP.setOnAction {
            if (protocol != Protocols.UDP) {
                queue.add("_Messag7e2")
                clickOnUDPButton()
            }
        }
    }

    private fun clickOnTCPButton() {
        bTTCP.style = "-fx-background-color:silver; -fx-background-radius : 0;"
        bTUDP.style = "-fx-background-radius : 0;"
        protocol = Protocols.TCP
//        Thread.sleep(500)

        handleUDPChat.stop()
        handleTCPChat.initChat()
    }

    private fun clickOnUDPButton() {
        bTTCP.style = "-fx-background-radius : 0;"
        bTUDP.style = "-fx-background-color:silver; -fx-background-radius : 0;"
        protocol = Protocols.UDP
//        Thread.sleep(500)
        handleTCPChat.stop()
        handleUDPChat.initChat()

    }


    internal fun setSettingToTCP() {
        queueReceive.add("*Set protocol to TCP")
        clickOnTCPButton()
    }

    //////////////////////// MENU

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

    private fun initFnOnClose() {
        primaryStage.onCloseRequest = EventHandler {
            if (protocol == Protocols.UDP) queue.add("*User has left the chat")
            Platform.exit()
            exitProcess(0)
        }
    }


}