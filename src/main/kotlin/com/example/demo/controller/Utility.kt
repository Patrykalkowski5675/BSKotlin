package com.example.demo.controller

import com.sun.deploy.panel.TextFieldProperty
import javafx.scene.control.Button
import tornadofx.field
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

        return "Size of file: " + fileSizeString
    }


}