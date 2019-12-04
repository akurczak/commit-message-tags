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

internal class PathMappingTagService(project: Project) : CommitTagService<PathMappingTagsCreator>(project) {

    override val id = CommitTagServiceId("PathMappingTagService")

    override val displayName = "Path mappings"

    val settings get() = project.pathMappingSettings

    override fun createSettingsPanel() = panel {
        row("Display tags in local changes list:") {
            comboBox(DefaultComboBoxModel(PathTagsDisplay.values()), settings::pathTagsDisplay)
        }
        row("Path mappings:") {
            pathMappingsTable(settings::pathMappings)
        }
    }

    override fun createTagCreator() = PathMappingTagsCreator(settings.pathMappings)
}

internal val Project.pathMappingSettings get() = service<PathMappingSettings>().state

@State(name = "PathMappingSettings", storages = [Storage(file = COMMIT_TAGS_SETTINGS_FILE)])
internal class PathMappingSettings : PersistentStateComponent<PathMappingSettingsState> {

    private var settingsState = PathMappingSettingsState()

    override fun getState(): PathMappingSettingsState = settingsState

    override fun loadState(state: PathMappingSettingsState) {
        settingsState = state
    }
}

internal data class PathMappingSettingsState(
    var pathMappings: List<PathMapping> = emptyList(),
    var pathTagsDisplay: PathTagsDisplay = PathTagsDisplay.ALL
)

internal fun List<PathMapping>.findTag(path: String) = firstOrNull { it.pathPart in path }?.tag

internal enum class PathTagsDisplay(private val displayName: String) {

    ALL("Always"),
    UNKNOWN_ONLY("For unknown tags only"),
    NONE("Never");

    override fun toString() = displayName
}

internal data class PathMapping(var pathPart: String = "", var tag: String = "")
