package io.github.spair.strongdmm.gui.menubar

import io.github.spair.strongdmm.diDirect
import io.github.spair.strongdmm.diInstance
import io.github.spair.strongdmm.gui.ViewController
import io.github.spair.strongdmm.gui.chooseFileDialog
import io.github.spair.strongdmm.gui.mapcanvas.MapCanvasController
import io.github.spair.strongdmm.gui.runWithProgressBar
import io.github.spair.strongdmm.gui.showAvailableMapsDialog
import io.github.spair.strongdmm.logic.Environment
import io.github.spair.strongdmm.logic.history.redoAction
import io.github.spair.strongdmm.logic.history.undoAction
import io.github.spair.strongdmm.logic.map.saveMap
import java.awt.event.ActionListener
import javax.swing.SwingUtilities

class MenuBarController : ViewController<MenuBarView>(diDirect()) {

    private val env by diInstance<Environment>()
    private val mapCanvasController by diInstance<MapCanvasController>()

    override fun init() {
        with(view) {
            openEnvItem.addActionListener(openEnvironmentAction())
            saveItem.addActionListener(saveSelectedMapAction())
            exitMenuItem.addActionListener { System.exit(0) }

            undoActionItem.addActionListener { undoAction() }
            redoActionItem.addActionListener { redoAction() }
        }
    }

    fun switchUndo(enabled: Boolean) {
        view.undoActionItem.isEnabled = enabled
    }

    fun switchRedo(enabled: Boolean) {
        view.redoActionItem.isEnabled = enabled
    }

    // While map canvas is in focus, swing won't catch key events, so we fire them programmatically
    fun fireShortcutEvent(shortcut: Shortcut) {
        SwingUtilities.invokeLater {
            with(view) {
                when (shortcut) {
                    Shortcut.CTRL_O -> openMapItem.doClick()
                    Shortcut.CTRL_S -> saveItem.doClick()
                    Shortcut.CTRL_Q -> exitMenuItem.doClick()
                    Shortcut.CTRL_Z -> undoActionItem.doClick()
                    Shortcut.CTRL_SHIFT_O -> availableMapsItem.doClick()
                    Shortcut.CTRL_SHIFT_Z -> redoActionItem.doClick()
                }
            }
        }
    }

    private fun openEnvironmentAction() = ActionListener {
        chooseFileDialog("BYOND Environments (*.dme)", "dme")?.let { dmeFile ->
            runWithProgressBar("Parsing environment...") {
                env.parseAndPrepareEnv(dmeFile)
                view.saveItem.isEnabled = true

                view.openMapItem.apply {
                    isEnabled = true
                    addActionListener {
                        chooseFileDialog("BYOND Maps (*.dmm)", "dmm", env.absoluteRootPath)?.let { dmmFile ->
                            env.openMap(dmmFile)
                        }
                    }
                }

                view.availableMapsItem.apply {
                    isEnabled = true
                    addActionListener {
                        showAvailableMapsDialog(env.availableMaps)?.let {
                            env.openMap(it)
                        }
                    }
                }
            }
        }
    }

    private fun saveSelectedMapAction() = ActionListener {
        mapCanvasController.selectedMap?.let { saveMap(it) }
    }
}
