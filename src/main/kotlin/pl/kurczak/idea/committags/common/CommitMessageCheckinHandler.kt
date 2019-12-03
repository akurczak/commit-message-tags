package pl.kurczak.idea.committags.common

import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.intellij.openapi.vcs.CheckinProjectPanel
import com.intellij.openapi.vcs.changes.CommitContext
import com.intellij.openapi.vcs.checkin.CheckinHandler
import com.intellij.openapi.vcs.checkin.CheckinHandlerFactory
import com.intellij.openapi.vcs.ui.RefreshableOnComponent
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

internal val commitWorkflowUiKey = Key.create<CommitWorkflowUi>("pl.kurczak.idea.committags.commitWorkflowUi")

internal class CommitMessageCheckinHandler(
    private val project: Project,
    private val commitWorkflowUi: CommitWorkflowUi
) : CheckinHandler() {

    private val settings get() = project.mainSettings

    override fun getBeforeCheckinConfigurationPanel(): RefreshableOnComponent? =
        super.getBeforeCheckinConfigurationPanel().also {
            project.putUserData(commitWorkflowUiKey, commitWorkflowUi)
            includedChangesChanged()
        }

    override fun includedChangesChanged() {

        if (settings.enableAutomaticMessageUpdate) {
            updateCommitMessage()
        }
    }

    fun updateCommitMessage() {
        val commitMessageUi = commitWorkflowUi.commitMessageUi
        val currentMessage = commitMessageUi.text
        val newTags = project.configuredCommitTagServices().map {
            it.createTagCreator()
        }.flatMap {
            it.createTagsContent(commitWorkflowUi.getIncludedChanges())
        }
        updateCommitMessage(parseTaggedMessage(project, currentMessage).copy(tags = newTags))
    }

    fun updateCommitMessage(newMessage: TaggedMessage) {
        val commitMessageUi = commitWorkflowUi.commitMessageUi
        if (commitMessageUi.text != newMessage.fullMessageWithDot) {
            commitMessageUi.text = newMessage.fullMessageWithDot
        }
    }
}
