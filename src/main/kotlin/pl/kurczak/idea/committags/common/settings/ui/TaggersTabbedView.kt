package pl.kurczak.idea.committags.common.settings.ui

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogPanel
import com.intellij.ui.components.JBTabbedPane
import com.intellij.ui.layout.CCFlags
import com.intellij.ui.layout.CellBuilder
import com.intellij.ui.layout.Row
import pl.kurczak.idea.committags.common.CommitTagService
import pl.kurczak.idea.committags.common.CommitTagServiceId
import pl.kurczak.idea.committags.common.commitTagServicesById
import pl.kurczak.idea.committags.util.filterNotNullValues
import javax.swing.SwingConstants
import javax.swing.border.EmptyBorder

internal class TaggersTabbedView(project: Project) {

    private val services: Map<CommitTagServiceId, CommitTagService<*>> = commitTagServicesById(project)

    private val settingsPanels: Map<CommitTagServiceId, DialogPanel> = services.mapValues {
        it.value.createSettingsPanel()?.withBorder(EmptyBorder(10, 0, 10, 0))
    }.filterNotNullValues()

    internal val tabs = JBTabbedPane(SwingConstants.TOP).apply {
        for ((id, service) in services) {
            val dialogPanel = settingsPanels[id]
            addTab(service.displayName, dialogPanel)
        }
    }

    internal fun reset() = settingsPanels.values.forEach { it.reset() }

    internal fun apply() = settingsPanels.values.forEach { it.apply() }

    internal fun isModified() = settingsPanels.values.any { it.isModified() }
}

internal fun Row.taggersTabbedView(project: Project): CellBuilder<JBTabbedPane> {
    val tabsView = TaggersTabbedView(project)
    return tabsView.tabs(CCFlags.grow)
        .onReset { tabsView.reset() }
        .onApply { tabsView.apply() }
        .onIsModified { tabsView.isModified() }
}
