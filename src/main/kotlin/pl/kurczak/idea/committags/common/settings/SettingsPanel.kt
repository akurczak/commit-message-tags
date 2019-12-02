package pl.kurczak.idea.committags.common.settings

import com.intellij.openapi.components.service
import com.intellij.openapi.options.BoundConfigurable
import com.intellij.openapi.options.SearchableConfigurable
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogPanel
import com.intellij.ui.layout.panel
import pl.kurczak.idea.committags.common.settings.ui.taggersListView
import pl.kurczak.idea.committags.common.settings.ui.taggersTabbedView

internal class SettingsPanel(private val project: Project) :
    BoundConfigurable("Commit Tags", "pl.kurczak.idea.committags"),
    SearchableConfigurable {

    override fun createPanel(): DialogPanel {

        val settings = project.service<MainSettings>().state
        return panel {
            titledRow("General") {
                row("Module tag prefix:") {
                    textField(settings::tagPrefix, 10)
                }
                row("Module tag suffix:") {
                    textField(settings::tagSuffix, 10)
                }
                row("Enabled taggers:") {
                    taggersListView(project, settings::orderedCommitTagServices)
                }
            }
            row {
                taggersTabbedView(project)
            }
        }
    }

    override fun getId(): String {
        return "pl.kurczak.idea.committags"
    }
}
