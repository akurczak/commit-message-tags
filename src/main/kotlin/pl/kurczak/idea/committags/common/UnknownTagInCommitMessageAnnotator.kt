package pl.kurczak.idea.committags.common

import com.intellij.lang.annotation.Annotation
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.openapi.vcs.ui.CommitMessage
import com.intellij.psi.PsiElement
import pl.kurczak.idea.committags.common.settings.mainSettings
import kotlin.streams.toList

internal val unknownTagQuickFixRegistrarExtensionPoint =
    ExtensionPointName.create<UnknownTagQuickFixRegistrar>("pl.kurczak.idea.committags.unknownTagQuickFixRegistrar")

internal fun Project.unknownTagQuickFixRegistrars() = unknownTagQuickFixRegistrarExtensionPoint.extensions(this).toList()

internal class UnknownTagInCommitMessageAnnotator : Annotator {

    private fun getTagsPositions(
        tagPrefix: String,
        tagSuffix: String,
        tags: List<String>,
        messageTextRange: TextRange
    ): Map<String, TextRange> {
        val additionalSize = tagPrefix.length + tagSuffix.length
        var startOffset = messageTextRange.startOffset
        val result = mutableMapOf<String, TextRange>()
        for (tag in tags) {
            val endOffset = startOffset + tag.length + additionalSize
            result[tag] = TextRange(startOffset, endOffset)
            startOffset = endOffset
        }
        return result
    }

    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        if (!CommitMessage.isCommitMessage(element)) {
            return
        }
        val project = element.project
        val workflowUi = project.getUserData(commitWorkflowUiKey) ?: return
        val actualTags = parseTaggedMessage(project, element.text).tags
        val expectedTags = project.configuredCommitTagServices().map {
            it.createTagCreator()
        }.flatMap {
            it.createTagsContent(workflowUi.getIncludedChanges())
        }
        val unexpectedTags = actualTags - expectedTags
        if (unexpectedTags.isNotEmpty()) {
            val settings = project.mainSettings
            val quickFixFactories = project.unknownTagQuickFixRegistrars()
            val tagsPositions = getTagsPositions(settings.tagPrefix, settings.tagSuffix, actualTags, element.textRange)
            for (tag in unexpectedTags) {
                val position = tagsPositions[tag] ?: error("Cannot retrieve tag position in message")
                val annotation = holder.createErrorAnnotation(position, "Unknown tag")
                annotation.registerFix(RemoveTagIntentionAction(position))
                for (it in quickFixFactories) {
                    it.registerQuickFix(tag, position, annotation)
                }
            }
        }
    }
}

interface UnknownTagQuickFixRegistrar {

    fun registerQuickFix(tag: String, textRange: TextRange, annotation: Annotation)
}
