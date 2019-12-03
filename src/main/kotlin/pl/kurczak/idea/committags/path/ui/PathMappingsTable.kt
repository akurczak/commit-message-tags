package pl.kurczak.idea.committags.path.ui

import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.ToolbarDecorator
import com.intellij.ui.layout.CellBuilder
import com.intellij.ui.layout.PropertyBinding
import com.intellij.ui.layout.Row
import com.intellij.ui.layout.panel
import com.intellij.ui.table.JBTable
import pl.kurczak.idea.committags.path.PathMapping
import java.awt.Dimension
import java.awt.event.MouseEvent
import java.util.*
import javax.swing.JPanel
import javax.swing.ListSelectionModel
import javax.swing.table.AbstractTableModel
import kotlin.properties.Delegates
import kotlin.reflect.KMutableProperty0

internal class PathMappingsTable(items: MutableList<PathMapping>) : JBTable() {

    var items by Delegates.observable(items) { _, _, _ ->
        fireDataChanged()
    }

    init {
        model = TableModel()
        setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION)
        emptyText.text = "No path mappings defined"
    }

    val panelWithToolbar: JPanel = ToolbarDecorator.createDecorator(this)
        .setAddAction { addEntry() }
        .setRemoveActionUpdater { selectedRowCount > 0 }
        .setRemoveAction { removeSelectedEntries() }
        .setEditActionUpdater { selectedRowCount == 1 }
        .setEditAction { editEntry() }
        .setMoveUpActionUpdater { selectedRowCount == 1 && selectedRow != 0 }
        .setMoveUpAction { moveSelectedEntryUp() }
        .setMoveDownActionUpdater { selectedRowCount == 1 && selectedRow != items.size - 1 }
        .setMoveDownAction { moveSelectedEntryDown() }
        .setPreferredSize(Dimension(-1, 300))
        .createPanel()

    private fun moveSelectedEntryDown() = moveRow(selectedRow, selectedRow + 1)

    private fun moveSelectedEntryUp() = moveRow(selectedRow, selectedRow - 1)

    private fun moveRow(source: Int, target: Int) {
        if (target in 0 until items.size) {
            val sourceItem = items[source]
            items[source] = items[target]
            items[target] = sourceItem
            fireDataChanged()
            setRowSelectionInterval(target, target)
        }
    }

    private fun removeSelectedEntries() {
        val indicesToRemove = selectedRows.sortedArrayDescending()
        val originalRow = indicesToRemove[0]
        for (i in indicesToRemove) {
            items.removeAt(i)
        }
        fireDataChanged()
        if (originalRow < rowCount) {
            setRowSelectionInterval(originalRow, originalRow)
        } else if (rowCount > 0) {
            val index = rowCount - 1
            setRowSelectionInterval(index, index)
        }
    }

    private fun editEntry() {
        val row = selectedRow
        val item: PathMapping = items[row]
        val editor = EntryEditor("Edit path mapping", item.copy())
        if (editor.showAndGet()) {
            val editedItem = editor.item
            if (editedItem != item) {
                items[row] = editedItem
                fireDataChanged()
            }
        }
    }

    private fun addEntry() {
        val editor = EntryEditor("Add path mapping")
        if (editor.showAndGet()) {
            val item = editor.item
            val lastSelectedRow = selectionModel.maxSelectionIndex
            if (lastSelectedRow == -1) {
                items.add(item)
            } else {
                items.add(lastSelectedRow + 1, item)
            }
            fireDataChanged()
        }
    }

    override fun editCellAt(row: Int, column: Int, e: EventObject?): Boolean {
        if (e == null || e is MouseEvent && e.clickCount == 1) return false
        setRowSelectionInterval(row, row)
        editEntry()
        return false
    }

    private fun fireDataChanged() = (model as AbstractTableModel).fireTableDataChanged()

    private inner class TableModel : AbstractTableModel() {

        override fun getRowCount() = items.size

        override fun getColumnCount() = 2

        override fun getColumnClass(columnIndex: Int) = String::class.java

        override fun getValueAt(rowIndex: Int, columnIndex: Int): Any? = when (columnIndex) {
            0 -> items[rowIndex].pathPart
            1 -> items[rowIndex].tag
            else -> null
        }

        override fun getColumnName(columnIndex: Int): String? = when (columnIndex) {
            0 -> "Path part"
            1 -> "Tag"
            else -> null
        }
    }
}

private class EntryEditor(theTitle: String, val item: PathMapping = PathMapping()) : DialogWrapper(true) {

    init {
        title = theTitle
        init()
    }

    override fun createCenterPanel() = panel {
        row("Path part") {
            cell {
                textField(
                    { item.pathPart },
                    { item.pathPart = it },
                    40
                ).withErrorOnApplyIf("Empty path part") { it.text.isBlank() }.focused()
            }
        }
        row("Tag") {
            cell {
                textField(
                    { item.tag },
                    { item.tag = it },
                    40
                ).withErrorOnApplyIf("Empty tag name") { it.text.isBlank() }
            }
        }
    }
}

internal fun Row.pathMappingsTable(prop: KMutableProperty0<List<PathMapping>>): CellBuilder<JPanel> {
    val pathMappingsTable = PathMappingsTable(prop.get().toMutableList())
    val component = pathMappingsTable.panelWithToolbar
    return component(growX).withBinding(
        { pathMappingsTable.items },
        { _, it -> pathMappingsTable.items = it.toMutableList() },
        PropertyBinding(prop.getter, prop.setter)
    )
}
