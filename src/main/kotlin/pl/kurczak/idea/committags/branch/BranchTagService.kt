package pl.kurczak.idea.committags.branch

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.ui.layout.panel
import pl.kurczak.idea.committags.branch.ui.branchTagPatternView
import pl.kurczak.idea.committags.common.CommitTagService
import pl.kurczak.idea.committags.common.CommitTagServiceId
import pl.kurczak.idea.committags.common.settings.COMMIT_TAGS_SETTINGS_FILE
import pl.kurczak.idea.committags.common.settings.mainSettings

internal class BranchTagService(project: Project) : CommitTagService<BranchTagCreator>(project) {

    override val id = CommitTagServiceId("BranchTagService")

    override val displayName = "Branch name"

    private val branchSettings get() = project.branchSettings

    private val mainSettings get() = project.mainSettings

    override fun createSettingsPanel() = panel {
        row {
            branchTagPatternView(project, mainSettings.tagPrefix, mainSettings.tagSuffix, branchSettings)
        }
    }

    override fun createTagCreator() = BranchTagCreator(
        project,
        BranchTagPattern(branchSettings.branchNamePattern, branchSettings.branchTagTemplate)
    )
}

internal val Project.branchSettings get() = service<BranchSettings>().state

@State(name = "BranchSettings", storages = [Storage(file = COMMIT_TAGS_SETTINGS_FILE)])
internal class BranchSettings : PersistentStateComponent<BranchSettingsState> {

    private var settingsState = BranchSettingsState()

    override fun getState(): BranchSettingsState = settingsState

    override fun loadState(state: BranchSettingsState) {
        settingsState = state
    }
}

internal data class BranchSettingsState(var branchNamePattern: String = "", var branchTagTemplate: String = "")
