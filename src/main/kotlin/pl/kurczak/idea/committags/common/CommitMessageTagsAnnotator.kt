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
        message: String,
        startOffset: Int
    ): Map<String, List<TextRange>> {
        val settings = project.mainSettings
        val prefixLength = settings.tagPrefix.length
        val suffixLength = settings.tagSuffix.length
        var searchStartIndex = 0
        val result = mutableMapOf<String, MutableList<TextRange>>()
        for (tag in tags) {
            val tagValueStartIndex = message.indexOf(tag, searchStartIndex)
            val tagStartIndex = tagValueStartIndex - prefixLength
            val tagEndIndex = tagValueStartIndex + tag.length + suffixLength
            result.getOrPut(tag) { mutableListOf() }.add(TextRange(tagStartIndex, tagEndIndex).shiftRight(startOffset))
            searchStartIndex = tagEndIndex
        }
        return result
    }

}
