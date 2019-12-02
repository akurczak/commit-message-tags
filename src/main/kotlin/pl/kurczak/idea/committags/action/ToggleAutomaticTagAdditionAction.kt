package pl.kurczak.idea.committags.action

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.ToggleAction
import com.intellij.openapi.project.DumbAware
import pl.kurczak.idea.committags.common.settings.mainSettings

class ToggleAutomaticTagAdditionAction : ToggleAction(), DumbAware {

    override fun isSelected(e: AnActionEvent): Boolean = e.project?.mainSettings?.enableAutomaticMessageUpdate ?: false

    override fun setSelected(e: AnActionEvent, state: Boolean) {
        val project = e.project ?: return
        project.mainSettings.enableAutomaticMessageUpdate = state
    }
}
