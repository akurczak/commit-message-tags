package pl.kurczak.idea.committags.common

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.openapi.vcs.ui.CommitMessage
import com.intellij.psi.PsiElement
import pl.kurczak.idea.committags.common.settings.mainSettings

abstract class CommitMessageTagsAnnotator : Annotator {

    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        if (!CommitMessage.isCommitMessage(element)) {
            return
        }
        val project = element.project
        val parsedMessage = parseTaggedMessage(project, element.text)
        doAnnotate(project, parsedMessage, element, holder)
    }

    protected abstract fun doAnnotate(
        project: Project,
        parsedMessage: TaggedMessage,
        element: PsiElement,
        holder: AnnotationHolder
    )

    protected fun getTagsPositions(
        project: Project,
        tags: List<String>,
        messageTextRange: TextRange
    ): Map<String, List<TextRange>> {
        val settings = project.mainSettings
        val additionalSize = settings.tagPrefix.length + settings.tagSuffix.length
        var startOffset = messageTextRange.startOffset
        val result = mutableMapOf<String, MutableList<TextRange>>()
        for (tag in tags) {
            val endOffset = startOffset + tag.length + additionalSize
            result.getOrPut(tag) { mutableListOf() }.add(TextRange(startOffset, endOffset))
            startOffset = endOffset
        }
        return result
    }

}
