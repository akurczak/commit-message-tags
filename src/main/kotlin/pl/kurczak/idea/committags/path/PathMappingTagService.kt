package pl.kurczak.idea.committags.path

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.ui.layout.panel
import pl.kurczak.idea.committags.common.CommitTagService
import pl.kurczak.idea.committags.common.CommitTagServiceId
import pl.kurczak.idea.committags.common.settings.COMMIT_TAGS_SETTINGS_FILE
import pl.kurczak.idea.committags.path.ui.pathMappingsTable
import javax.swing.DefaultComboBoxModel

internal class PathMappingTagService(private val project: Project) : CommitTagService<PathMappingTagsCreator> {

    override val id = CommitTagServiceId("PathMappingTagService")

    override val displayName = "Path mappings"

    private val settings get() = project.service<PathMappingSettings>().state

    override fun createSettingsPanel() = panel {
        row("Path mappings:") {
            pathMappingsTable(settings::paths)
        }
    }

    override fun createTagCreator() = PathMappingTagsCreator(settings.paths)
}

@State(name = "PathMappingSettings", storages = [Storage(file = COMMIT_TAGS_SETTINGS_FILE)])
internal class PathMappingSettings : PersistentStateComponent<PathMappingSettingsState> {

    private var settingsState = PathMappingSettingsState()

    override fun getState(): PathMappingSettingsState = settingsState

    override fun loadState(state: PathMappingSettingsState) {
        settingsState = state
    }
}

internal data class PathMappingSettingsState(
    var paths: List<PathMapping> = emptyList()
)

internal data class PathMapping(var pathPattern: String = "", var tag: String = "")
