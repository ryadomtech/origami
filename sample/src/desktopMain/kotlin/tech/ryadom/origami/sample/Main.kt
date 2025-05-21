package tech.ryadom.origami.sample

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import tech.ryadom.origami.SampleApp

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "OrigamiSample",
    ) {
        SampleApp()
    }
}
