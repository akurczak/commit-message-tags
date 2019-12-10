package pl.kurczak.idea.committags.common

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement

internal class DuplicatedTagsAnnotator : CommitMessageTagsAnnotator() {

    override fun doAnnotate(
        project: Project,
        parsedMessage: TaggedMessage,
        element: PsiElement,
        holder: AnnotationHolder
    ) {
        val actualTags = parsedMessage.tags
        val duplicatedTags = actualTags.groupingBy { it }.eachCount().filterValues { it > 1 }
        if (duplicatedTags.isNotEmpty()) {
            val tagsPositions = getTagsPositions(project, actualTags, element.text, element.textOffset)
            for ((tag, count) in duplicatedTags) {
                val positions = tagsPositions[tag] ?: error("Cannot retrieve tag position in message")
                for (position in positions) {
                    holder.createAnnotation(HighlightSeverity.ERROR, position, "Duplicated tag", "Duplicated tag: $tag ($count times)")
                        .registerFix(RemoveTagIntentionAction(position, "duplicated"))
                }
            }
        }
    }

}
