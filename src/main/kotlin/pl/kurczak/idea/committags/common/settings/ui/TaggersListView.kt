package pl.kurczak.idea.committags.common.settings.ui

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.actionSystem.impl.SimpleDataContext
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.ui.AnActionButton
import com.intellij.ui.CollectionListModel
import com.intellij.ui.ToolbarDecorator
import com.intellij.ui.components.JBList
import com.intellij.ui.components.JBList.StripedListCellRenderer
import com.intellij.ui.layout.CellBuilder
import com.intellij.ui.layout.PropertyBinding
import com.intellij.ui.layout.Row
import pl.kurczak.idea.committags.common.CommitTagService
import pl.kurczak.idea.committags.common.CommitTagServiceId
import pl.kurczak.idea.committags.common.commitTagServices
import java.awt.Component
import javax.swing.JList
import javax.swing.JPanel
import javax.swing.ListSelectionModel
import kotlin.reflect.KMutableProperty0

internal class TaggersListView(private val project: Project) {

    internal var model: CollectionListModel<CommitTagService<*>> = CollectionListModel()

    private val selectedTaggersListView = JBList(model).apply {
        emptyText.text = "No taggers configured"
        selectionMode = ListSelectionModel.SINGLE_SELECTION
        cellRenderer = TaggersListViewCellRenderer()
        visibleRowCount = 4
        fixedCellWidth = 360
    }

    internal val panelWithToolbar = ToolbarDecorator.createDecorator(selectedTaggersListView).setAddAction {
        addTagger(it)
    }.setAddActionUpdater {
        unusedTaggers.isNotEmpty()
    }.createPanel()

    private val unusedTaggers get() = project.commitTagServices() - model.items

    private fun addTagger(button: AnActionButton) {
        val actionGroup = DefaultActionGroup(null, false)
        for (tagger in unusedTaggers) {
            actionGroup.add(object : AnAction(tagger.displayName, null, null) {
                override fun actionPerformed(e: AnActionEvent) {
                    model.add(tagger)
                }
            })
        }
        val popup = JBPopupFactory.getInstance().createActionGroupPopup(
            "Add Tagger",
            actionGroup,
            SimpleDataContext.getProjectContext(project),
            false,
            null,
            -1
        )
        button.preferredPopupPoint?.let {
            popup.show(it)
        }
    }

    private val servicesSettings: MutableMap<CommitTagServiceId, DialogPanel> = mutableMapOf()

    fun applyTaggersConfigurations() = servicesSettings.values.forEach {
        it.apply()
    }

    fun resetTaggersConfigurations() = servicesSettings.values.forEach {
        it.reset()
    }

    fun areTaggersConfigurationsModified() = servicesSettings.values.any { it.isModified() }
}

private class TaggersListViewCellRenderer : StripedListCellRenderer() {

    override fun getListCellRendererComponent(
        list: JList<*>?,
        value: Any,
        index: Int,
        isSelected: Boolean,
        cellHasFocus: Boolean
    ): Component {
        super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus)
        if (value is CommitTagService<*>) {
            text = value.displayName
        }
        return this
    }
}

internal fun Row.taggersListView(
    project: Project,
    property: KMutableProperty0<List<CommitTagServiceId>>
): CellBuilder<JPanel> {
    val taggersListView = TaggersListView(project)
    val component = taggersListView.panelWithToolbar
    return component().withBinding(
        { taggersListView.model.items.map { it.id } },
        { _, it -> taggersListView.model.replaceAll(project.commitTagServices(it)) },
        PropertyBinding(property::get, property::set)
    ).onApply {
        taggersListView.applyTaggersConfigurations()
    }.onReset {
        taggersListView.resetTaggersConfigurations()
    }.onIsModified {
        taggersListView.areTaggersConfigurationsModified()
    }
}
