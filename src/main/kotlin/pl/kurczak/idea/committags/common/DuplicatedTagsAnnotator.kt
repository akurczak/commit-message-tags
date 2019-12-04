package pl.kurczak.idea.committags.common

import com.intellij.lang.annotation.AnnotationHolder
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
        val duplicatedTags = actualTags.groupingBy { it }.eachCount().filterValues { it > 1 }.keys
        if (duplicatedTags.isNotEmpty()) {
            val tagsPositions = getTagsPositions(project, actualTags, element.textRange)
            for (tag in duplicatedTags) {
                val positions = tagsPositions[tag] ?: error("Cannot retrieve tag position in message")
                for (position in positions) {
                    holder.createErrorAnnotation(position, "Duplicated tag")
                        .registerFix(RemoveTagIntentionAction(position, "duplicated"))
                }
            }
        }
    }

}
