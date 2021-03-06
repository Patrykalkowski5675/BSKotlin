package com.example.demo.tools

import com.example.demo.controller.Controller
import com.sun.deploy.panel.TextFieldProperty
import javafx.scene.control.Button
import tornadofx.field
import java.security.Key
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import kotlin.math.roundToInt

object Utility {

    fun calculateSizeFile(size: Long): String {
        var fileSizeString = ""
        val localFileSize: Double = size.toDouble()

        if ((localFileSize / (1024 * 1024 * 1024)).roundToInt() > 0) {
            fileSizeString = ((localFileSize / (1024 * 1024 * 1024) * 100).roundToInt() / 100.0).toString() + " GB"
        } else if ((localFileSize / (1024 * 1024)).roundToInt() > 0) {
            fileSizeString = ((localFileSize / (1024 * 1024) * 100).roundToInt() / 100.0).toString() + " MB"
        } else if ((localFileSize / (1024)).roundToInt() > 0) {
            fileSizeString = ((localFileSize / (1024) * 100).roundToInt() / 100.0).toString() + " KB"
        } else {
            fileSizeString = ((localFileSize * 100).roundToInt() / 100.0).toString() + " B"
        }

        return "Size of file: $fileSizeString"
    }

    fun initCipher(cipherMode: Int, mode: Controller.Companion.Modes, sessionKey: Key, iv : ByteArray? = null): Cipher {
//        val randomSecureRandom = SecureRandom()
//        val iv = ByteArray(16)
//        randomSecureRandom.nextBytes(iv)
//        val ivParams = IvParameterSpec(iv)
       // val initVector = "encryptionIntVec"
       //  val iv2 = IvParameterSpec(initVector.toByteArray(charset("UTF-8")))

        val cipher: Cipher = when (mode) {
            Controller.Companion.Modes.EBC -> Cipher.getInstance("AES/ECB/PKCS5Padding")
            Controller.Companion.Modes.CBC -> Cipher.getInstance("AES/CBC/PKCS5Padding")
            Controller.Companion.Modes.CFB -> Cipher.getInstance("AES/CFB/PKCS5Padding")
            Controller.Companion.Modes.OFB -> Cipher.getInstance("AES/OFB/PKCS5Padding")
        }

        when (cipherMode) {
            Cipher.ENCRYPT_MODE -> {
                if (mode == Controller.Companion.Modes.EBC)
                    cipher.init(Cipher.ENCRYPT_MODE, sessionKey)
                else
                    cipher.init(Cipher.ENCRYPT_MODE, sessionKey,  IvParameterSpec(iv))
            }
            Cipher.DECRYPT_MODE -> {
                if (mode == Controller.Companion.Modes.EBC)
                    cipher.init(Cipher.DECRYPT_MODE, sessionKey)
                else
                    //cipher.init(Cipher.DECRYPT_MODE, sessionKey, IvParameterSpec(iv!!))
                    cipher.init(Cipher.DECRYPT_MODE, sessionKey, IvParameterSpec(iv))
            }
        }
        return cipher
    }

    fun initIV() : ByteArray
    {
        val randomSecureRandom = SecureRandom()
        val iv = ByteArray(16)
        randomSecureRandom.nextBytes(iv)
        return  iv
    }
}