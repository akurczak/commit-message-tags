package pl.kurczak.idea.committags.common

import com.intellij.openapi.project.Project
import com.intellij.openapi.vcs.CheckinProjectPanel
import com.intellij.openapi.vcs.changes.CommitContext
import com.intellij.openapi.vcs.checkin.CheckinHandler
import com.intellij.openapi.vcs.checkin.CheckinHandlerFactory
import com.intellij.vcs.commit.AbstractCommitWorkflowHandler
import com.intellij.vcs.commit.CommitWorkflowUi
import pl.kurczak.idea.committags.common.settings.mainSettings

class CommitMessageCheckinHandlerFactory : CheckinHandlerFactory() {

    override fun createHandler(panel: CheckinProjectPanel, commitContext: CommitContext): CheckinHandler {
        val commitWorkflowUi =
            (panel.commitWorkflowHandler as? AbstractCommitWorkflowHandler<*, *>)?.ui ?: return CheckinHandler.DUMMY
        return CommitMessageCheckinHandler(panel.project, commitWorkflowUi)
    }
}

internal class CommitMessageCheckinHandler(
    private val project: Project,
    private val commitWorkflowUi: CommitWorkflowUi
) : CheckinHandler() {

    private val settings get() = project.mainSettings

    override fun includedChangesChanged() {
        if (settings.enableAutomaticMessageUpdate) {
            updateCommitMessage()
        }
    }

    fun updateCommitMessage() {
        val commitMessageUi = commitWorkflowUi.commitMessageUi
        val currentMessage = commitMessageUi.text
        val bareMessage = removeExistingTags(currentMessage).trim()
        val newTags = commitTagServices(project, settings.orderedCommitTagServices).map {
            it.createTagCreator()
        }.joinToString(separator = "") {
            it.createTags(settings.tagPrefix, settings.tagSuffix, commitWorkflowUi.getIncludedChanges())
        }
        val newMessage = if (bareMessage.endsWith(".")) {
            "$newTags $bareMessage"
        } else {
            "$newTags $bareMessage."
        }
        if (newMessage != currentMessage) {
            commitMessageUi.text = newMessage
        }
    }

    private fun removeExistingTags(currentMessage: String): String {
        return settings.tagRegex.replace(currentMessage, "")
    }
}
