package net.robocode2.gui.extensions

import java.awt.Window
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent

object WindowExt {

    fun Window.onClosing(handler: ((WindowEvent) -> Unit)) {
        addWindowListener(object : WindowAdapter() {
            override fun windowClosing(e: WindowEvent) {
                handler.invoke(e)
            }
        })
    }

    fun Window.onActivated(handler: ((WindowEvent) -> Unit)) {
        addWindowListener(object : WindowAdapter() {
            override fun windowActivated(e: WindowEvent) {
                handler.invoke(e)
            }
        })
    }

    fun Window.onDeactivated(handler: ((WindowEvent) -> Unit)) {
        addWindowListener(object : WindowAdapter() {
            override fun windowDeactivated(e: WindowEvent) {
                handler.invoke(e)
            }
        })
    }
}