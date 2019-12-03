package pl.kurczak.idea.committags.common.settings

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.util.xmlb.annotations.XCollection
import pl.kurczak.idea.committags.common.CommitTagServiceId

const val COMMIT_TAGS_SETTINGS_FILE = "commit-tags.xml"

@State(name = "MainSettings", storages = [Storage(file = COMMIT_TAGS_SETTINGS_FILE)])
internal class MainSettings : PersistentStateComponent<MainSettingsState> {

    private var settingsState = MainSettingsState()

    override fun getState(): MainSettingsState = settingsState

    override fun loadState(state: MainSettingsState) {
        settingsState = state
    }
}

internal val Project.mainSettings get() = service<MainSettings>().state

internal data class MainSettingsState(
    var tagPrefix: String = "[",
    var tagSuffix: String = "]",
    var enableAutomaticMessageUpdate: Boolean = true,
    @XCollection(style = XCollection.Style.v2) var orderedCommitTagServices: List<CommitTagServiceId> = emptyList()
)
