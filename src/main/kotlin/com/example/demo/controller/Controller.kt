package com.example.demo.controller

import com.example.demo.controller.TCP.chat.TCPChatController
import com.example.demo.controller.TCP.chat.TCPClientChatController
import com.example.demo.controller.TCP.chat.TCPServerChatController
import com.example.demo.controller.TCP.tranferSessionKey.TCPReceiverKey
import com.example.demo.controller.TCP.tranferSessionKey.TCPSenderKey
import com.example.demo.controller.TCP.transferfile.TCPReceiverFileSendController
import com.example.demo.controller.TCP.transferfile.TCPSenderFileSendController
import com.example.demo.tools.EstablishConnection
import com.example.demo.tools.ToolsRSAKeys
import com.example.demo.tools.ToolsSessionKey
import com.example.demo.tools.Utility
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
import javafx.scene.input.KeyCode
import javafx.scene.layout.AnchorPane
import javafx.scene.layout.StackPane
import javafx.scene.text.Text
import javafx.stage.FileChooser
import javafx.stage.Stage
import tornadofx.FX.Companion.primaryStage
import java.io.File
import java.net.InetAddress
import java.net.URL
import java.nio.charset.StandardCharsets
import java.security.Key
import java.security.MessageDigest
import java.security.PrivateKey
import java.security.PublicKey
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue
import javax.xml.bind.DatatypeConverter
import kotlin.collections.ArrayList
import kotlin.system.exitProcess


class Controller : Initializable {

    @FXML
    lateinit var iPText: TextField
    lateinit var tFFile: TextField
    lateinit var tFEnterText: TextField
    lateinit var textAreaChat: TextArea
    lateinit var tIPHint: Text
    lateinit var tSize: Text
    lateinit var progressText: Text
    lateinit var tSelectedFile: Text
    lateinit var tNoFileWithKeys: Text
    lateinit var tPassTooShort: Text
    lateinit var bTChoose: Button
    lateinit var bTSend: Button
    lateinit var bTAccept: Button
    lateinit var bTDecline: Button
    lateinit var bTStop: Button
    lateinit var bTEBC: Button
    lateinit var bTCBC: Button
    lateinit var bTCFB: Button
    lateinit var bTOFB: Button
    lateinit var bTRenewKey: Button
    lateinit var bTShowKey: Button
    lateinit var buttonPassword: Button
    lateinit var progressBar: ProgressBar
    lateinit var textPassword: PasswordField
    lateinit var groupPassword: Group
    lateinit var image: ImageView
    lateinit var status: Label
    lateinit var mHelp: MenuItem
    lateinit var mQuit: MenuItem
    lateinit var rightPane: AnchorPane
    lateinit var groupRightPane: Group
    lateinit var leftPane: AnchorPane


    private var fileSize: Long = 0L
    private var fileName = "hmmm.txt"
    private var queue = ConcurrentLinkedQueue<String>()
    private var queueReceive = ConcurrentLinkedQueue<String>()
    private var dtf: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")
    private var listModeButtons = ArrayList<Button>()
    private var file: File? = null
    private var handleChat : TCPChatController? = null

    lateinit var keys: Pair<PrivateKey, PublicKey>
    lateinit var sessionKey: Key

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
        var secondUserIP: String = "localhost"
        var whoiam: String = ""
        var invoked = false
    }

    @FXML
    override fun initialize(location: URL?, resources: ResourceBundle?) {
        primaryStage.maxHeight = 650.0

        ////menu
        initMenu()
        initFnOnClose()

        ////left side
        initButtonChooseFile()
        initSendButton()

        ///right side
        initTransferButtons()
        initModeButtons()
        initIPTextField()

        //center side
        initChatWindows(status)

        ///password and chat
        exchangeSessionKey()
        initSessionKeyButtons()
    }


//////////////////////// PASSWORD

    private fun changeDisability(bool: Boolean) {
        leftPane.isDisable = bool
        tFEnterText.isDisable = bool
//        textAreaChat.isDisable = bool
        groupRightPane.isDisable = bool
    }

    private fun exchangeSessionKey() {
        fun continueInit() {
            initTextFieldEnterText()
            initChat()
        }


        if (invoked) return
        queueReceive.add("*Preparing for exchange keys")
        invoked = true

        changeDisability(true)


        when (whoiam) {
            "Client" -> {

                groupPassword.isVisible = false
                queueReceive.add("_Messag1eS|Waiting for the Server")


                Thread {
                    /// SECOND operation - Establish a connection
                    EstablishConnection.changeSecondUserIP(secondUserIP)
                    EstablishConnection.responseForConnection()

                    iPText.isDisable = true
                    iPText.text = secondUserIP + " (Accepted)"
                    /// FOURTH operation- recieve a public key and generate a session key
                    var pair: Pair<Key, ByteArray>

                    val byteArray = TCPReceiverKey.initReceiveKey(secondUserIP)
                    pair = ToolsSessionKey.generateSessionKeyAndEncodeIt(byteArray)
                    queueReceive.add("*Generated session Key")
                    sessionKey = pair.first


                    /// FIFTH operation - send a session key
                    TCPSenderKey.initTransferEncodedKey(pair.second)
                    continueInit()
                    changeDisability(false)
                    invoked = false

                    queueReceive.add("_Messag1eS|Successfully connected with Server")
                    queueReceive.add("*Successfully connected with Server")


                }.start()

            }
            "Server" -> {
                val lengthOfPassword = 4
                if (textPassword.text.isNullOrBlank()) {
                    groupPassword.isVisible = true
                    queueReceive.add("_Messag1eS|Waiting for the password")
                }
                /// adding image
                image.image = Image(javaClass.classLoader.getResource("lock.png")?.toURI()?.toString())

                if (!ToolsRSAKeys.areKeysPresent())
                    tNoFileWithKeys.isVisible = true

                /// adding approving pass with enter
                textPassword.onKeyPressed = EventHandler { event ->
                    if (event.code == KeyCode.ENTER) {
                        if (textPassword.text.length >= lengthOfPassword) buttonPassword.fire()
                        else tPassTooShort.isVisible = true
                    }
                    if (textPassword.text.length >= lengthOfPassword) tPassTooShort.isVisible = false
                }

                buttonPassword.setOnAction {

                    /// create hash from password
                    val str = textPassword.text
                    if (str.length >= lengthOfPassword) {
                        val digest = MessageDigest.getInstance("SHA-256")
                        val hash = digest.digest(str.toByteArray(StandardCharsets.UTF_8))

                        groupPassword.isVisible = false
                        queueReceive.add("*Waiting for other user")


                        Thread {
                            /// FIRST operation- establish a connection
                            EstablishConnection.waitForConnection()

                            /// THIRD operation- send a public key
                            keys = ToolsRSAKeys(hash).getKeys()
                            TCPSenderKey.initTransferKey(keys.second)
                            queueReceive.add("*Sent public key")

                            /// SIXTH operation - receive a session
                            queueReceive.add("*Waiting for session key")
                            val byteArray = TCPReceiverKey.initReceiveKey(secondUserIP)
                            sessionKey = ToolsSessionKey.decodeSessionKey(byteArray, keys.first)
                            changeDisability(false)
                            continueInit()
                            invoked = false
                        }.start()

                        queueReceive.add("_Messag1eS|The password has been entered successfully")
                    } else tPassTooShort.isVisible = true
                }

                if (!textPassword.text.isNullOrBlank()) {
                    buttonPassword.fire()
                    queueReceive.add("_Messag1eS|The password has been read from the cache")
                }
            }
        }

    }

    private fun initSessionKeyButtons() {
        if (whoiam == "Server") bTRenewKey.isDisable = false

        bTRenewKey.setOnAction {
            queue.add("_Messag8e1")
            exchangeSessionKey()
        }

        bTShowKey.setOnAction { queueReceive.add("Session Key HEX: " + DatatypeConverter.printHexBinary(sessionKey.encoded)) }

    }


    //////////////////////// FILECHOOSE

    private fun initSendButton() {
        bTSend.setOnAction {
            queue.add("_Messag5eT|${fileSize}|${fileName}")
            rightPane.isDisable = true
            progressText.text = "Waiting for response"
            queueReceive.add("_Messag1eS|Waiting for accept or decline file")
        }
    }

    private fun changeGUIForPreFileTransfer(array: List<String>) {
        bTChoose.isVisible = false
        bTDecline.isVisible = true
        bTSend.isVisible = false
        bTAccept.isVisible = true
        bTStop.isVisible = false
//        progressBar.progress = -1.0

        tSize.text = Utility.calculateSizeFile(array[1].toLong())
        tFFile.text = array[2]
        tSelectedFile.text = "File to transfer:"

        fileName = array[2]
        fileSize = array[1].toLong()

        rightPane.isDisable = true
    }

    private fun changeGUIForFileTransfer() {
        bTStop.isVisible = true
        bTAccept.isVisible = false
        bTSend.isVisible = false
    }


    private fun changeGUIForFileChoice() {
        bTChoose.isVisible = true
        bTDecline.isVisible = false
        bTSend.isVisible = true
        bTSend.isDisable = true
        bTAccept.isVisible = false
        bTStop.isVisible = false

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
            queueReceive.add("_Messag1eS|Open windows File Chooser")
            val fileChooser = FileChooser()
            file = fileChooser.showOpenDialog(primaryStage)

            if (file != null) {
                queueReceive.add("_Messag1eS|The file was selected correctly")

                tFFile.text = file!!.name
                fileName = file!!.name
                fileSize = file!!.length()


                tSize.text = Utility.calculateSizeFile(fileSize)

                bTSend.isDisable = false
                Platform.runLater {
                    progressBar.progress = -1.0
                    progressText.text = "Waiting for send"
                }

            } else {
                queueReceive.add("_Messag1eS|No file has been selected")
                changeGUIForFileChoice()
            }
        }
    }

    private fun initTransferButtons() {
        bTAccept.setOnAction {
            queue.add("_Messag5eY")
            queue.add("*User accepted the file")
            queueReceive.add("_Messag1eS|File accepted")
            TCPReceiverFileSendController.receiveFile(mode, progressBar, fileName, fileSize, progressText, queue, queueReceive, sessionKey, secondUserIP)
            rightPane.isDisable = true
            changeGUIForFileTransfer()
        }
        bTDecline.setOnAction {
            queue.add("_Messag5eN")
            queue.add("*User rejected the file")
            queueReceive.add("_Messag1eS|File rejected")
            changeGUIForFileChoice()
        }

        bTStop.setOnAction {
            TCPReceiverFileSendController.stopReceiving()
            TCPSenderFileSendController.stopReceiving()
        }
    }


    //////////////////////// CHAT

    private fun analiseHiddenMessage(msg: String, status: Label): Boolean {

        fun changeStatus(msg: String) {
            Platform.runLater { status.text = msg }
        }

        if (msg.length < 10) return false

        val tmpSubString = msg.subSequence(0, 10)
        print(tmpSubString)

        when (tmpSubString) {
            "_Messag1eS" -> changeStatus(msg.split('|')[1])
            "_Messag5eT" -> {
                Platform.runLater { changeGUIForPreFileTransfer(msg.split('|')) }
                changeStatus("User wants to send a file")
            }
            "_Messag5eY" -> Platform.runLater {
                TCPSenderFileSendController.sendFile(file!!, mode, progressBar, progressText, queue, queueReceive, sessionKey)
                changeGUIForFileTransfer()
            }
            "_Messag5eN" -> Platform.runLater { changeGUIForFileChoice() }
            "_Messag6e1" -> Platform.runLater {
                changeUIButtonsMode(0)
                changeStatus("User changed mode to ECB")
            }
            "_Messag6e2" -> Platform.runLater {
                changeUIButtonsMode(1)
                changeStatus("User changed mode to CBC")
            }
            "_Messag6e3" -> Platform.runLater {
                changeUIButtonsMode(2)
                changeStatus("User changed mode to CFB")
            }
            "_Messag6e4" -> Platform.runLater {
                Platform.runLater { changeUIButtonsMode(3) }
                changeStatus("User changed mode to OFB")
            }
            "_Messag8e1" -> Platform.runLater { exchangeSessionKey() }
            else -> {

                return false
            }

        }
        return true
    }

    private fun initChat() {
        queue = ConcurrentLinkedQueue<String>()
        queueReceive = ConcurrentLinkedQueue<String>()
        when (whoiam) {
            "Server" -> {

                handleChat?.stop()
                handleChat = TCPServerChatController(queue, queueReceive, mode, sessionKey)

            }
            "Client" -> {

                    handleChat?.stop()
                handleChat = TCPClientChatController(queue, queueReceive, mode, sessionKey, secondUserIP)
            }
            else -> println("Unknown parameter")
        }
    }

    private fun initChatWindows(status: Label) {

        Thread {
            var msg = ""
            while (true) {
                if (!queueReceive.isEmpty()) {
                    msg = queueReceive.poll()

                    // function returns false if in msg in no hidden message
                    if (!analiseHiddenMessage(msg, status)) {

                        val sB = StringBuilder()

                        if (msg.isNotEmpty() && msg[0] != '*') {
                            if (namePartnerFlag) {
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

    private fun initTextFieldEnterText() {
        tFEnterText.setOnAction {
            if (!tFEnterText.text.isNullOrBlank()) {
                queue.add(tFEnterText.text)
                if (nameUserFlag) {
                    textAreaChat.appendText("\t" + whoiam + ":\n" + dtf.format(LocalDateTime.now()) + " -> " + tFEnterText.text + '\n')
                    nameUserFlag = false
                    namePartnerFlag = true
                } else
                    textAreaChat.appendText(dtf.format(LocalDateTime.now()) + " -> " + tFEnterText.text + '\n')
                tFEnterText.clear()
            }
        }

        tFEnterText.textProperty().addListener { _, _, newValue ->
            if (newValue.length > 256) {
                val s = newValue.substring(0..256)
                tFEnterText.text = s
            }
        }


    }

    //////////////////////// SETTING

    private fun initIPTextField() {
        when (whoiam) {
            "Server" -> {
                iPText.text = InetAddress.getLocalHost().hostAddress.toString() + " (Unable change IP)"
                iPText.isDisable = true
//                iPText.isEditable = false
//                iPText.isMouseTransparent = true
            }
            "Client" -> {
                iPText.text = secondUserIP
            }
            else -> println("Unknown parameter")
        }

        iPText.setOnAction {
            secondUserIP = iPText.text
            tIPHint.isVisible = false
            queueReceive.add("_Messag1eS|An IP address has been entered")
            println(secondUserIP)
            EstablishConnection.changeSecondUserIP(secondUserIP)
            //TCPReceiverKey.stopReceiveKey()
            // exchangeSessionKey()
        }
        iPText.onKeyPressed = EventHandler {
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
            queueReceive.add("_Messag1eS|Change to EBC")
            changeUIButtonsMode(0)
        }
        bTCBC.setOnAction {
            queue.add("_Messag6e2")
            queueReceive.add("_Messag1eS|Change to CBC")
            changeUIButtonsMode(1)
        }
        bTCFB.setOnAction {
            queue.add("_Messag6e3")
            queueReceive.add("_Messag1eS|Change to CFB")
            changeUIButtonsMode(2)
        }
        bTOFB.setOnAction {
            queue.add("_Messag6e4")
            queueReceive.add("_Messag1eS|Change to OFB")
            changeUIButtonsMode(3)
        }

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
            stage.title = "Help"
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