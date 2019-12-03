package pl.kurczak.idea.committags.action

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.vcs.VcsDataKeys
import com.intellij.vcs.commit.AbstractCommitWorkflowHandler
import pl.kurczak.idea.committags.common.CommitMessageCheckinHandler

internal class AddTagsAction : AnAction(), DumbAware {

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val commitWorkflowHandler = e.getData(VcsDataKeys.COMMIT_WORKFLOW_HANDLER) ?: return
        val commitWorkflowUi = (commitWorkflowHandler as? AbstractCommitWorkflowHandler<*, *>)?.ui ?: return
        CommitMessageCheckinHandler(project, commitWorkflowUi).updateCommitMessage()
    }

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabled = shouldBeEnabled(e)
    }

    private fun shouldBeEnabled(e: AnActionEvent): Boolean {
        val commitWorkflowHandler = e.getData(VcsDataKeys.COMMIT_WORKFLOW_HANDLER) ?: return false
        val ui = (commitWorkflowHandler as? AbstractCommitWorkflowHandler<*, *>)?.ui ?: return false
        return !ui.getIncludedChanges().isNullOrEmpty()
    }
}
