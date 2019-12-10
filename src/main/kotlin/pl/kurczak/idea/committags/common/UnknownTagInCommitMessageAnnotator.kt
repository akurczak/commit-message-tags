package pl.kurczak.idea.committags.common

import com.intellij.lang.annotation.Annotation
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
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

internal class UnknownTagInCommitMessageAnnotator : CommitMessageTagsAnnotator() {

    override fun doAnnotate(
        project: Project,
        parsedMessage: TaggedMessage,
        element: PsiElement,
        holder: AnnotationHolder
    ) {
        val workflowUi = project.getUserData(commitWorkflowUiKey) ?: return
        val expectedTags = project.configuredCommitTagServices().map {
            it.createTagCreator()
        }.flatMap {
            it.createTagsContent(workflowUi.getIncludedChanges())
        }
        val actualTags = parsedMessage.tags
        val unexpectedTags = actualTags - expectedTags
        if (unexpectedTags.isNotEmpty()) {
            val quickFixFactories = project.unknownTagQuickFixRegistrars()
            val tagsPositions = getTagsPositions(project, actualTags, element.text, element.textOffset)
            for (tag in unexpectedTags) {
                val positions = tagsPositions[tag] ?: error("Cannot retrieve tag position in message")
                for (position in positions) {
                    val annotation = holder.createAnnotation(HighlightSeverity.ERROR, position, "Unexpected tag", "Unexpected tag: $tag")
                    annotation.registerFix(RemoveTagIntentionAction(position, "unexpected"))
                    for (it in quickFixFactories) {
                        it.registerQuickFix(tag, position, annotation)
                    }
                }
            }
        }
    }

}

interface UnknownTagQuickFixRegistrar {

    fun registerQuickFix(tag: String, textRange: TextRange, annotation: Annotation)
}
