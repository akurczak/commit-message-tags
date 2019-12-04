package pl.kurczak.idea.committags.path

import com.intellij.codeInsight.intention.IntentionAction
import com.intellij.lang.annotation.Annotation
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.command.CommandProcessor
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiFile
import pl.kurczak.idea.committags.common.UnknownTagQuickFixRegistrar
import pl.kurczak.idea.committags.common.settings.mainSettings
import pl.kurczak.idea.committags.path.ui.EntryEditor
import javax.swing.SwingUtilities

private const val zeroWidthSpace = "\u200B"

internal class DefinePathMappingQuickFixRegistrar : UnknownTagQuickFixRegistrar {

    override fun registerQuickFix(tag: String, textRange: TextRange, annotation: Annotation) {
        annotation.registerFix(DefinePathMappingIntentionAction(tag, textRange))
    }
}

internal class DefinePathMappingIntentionAction(
    private val tag: String,
    private val textRange: TextRange
) : IntentionAction {

    override fun startInWriteAction(): Boolean = true

    override fun getFamilyName(): String = "Define path mapping"

    override fun isAvailable(project: Project, editor: Editor?, file: PsiFile?): Boolean =
        project.pathMappingSettings.pathMappings.none { it.tag == tag }

    override fun getText(): String = "Define path mapping"

    override fun invoke(project: Project, editor: Editor?, file: PsiFile?) {
        val entryEditor = EntryEditor("Add path mapping", PathMapping(tag = tag))
        SwingUtilities.invokeLater {
            if (entryEditor.showAndGet()) {
                with(project.pathMappingSettings) {
                    pathMappings += entryEditor.item
                }
                val mainSettings = project.mainSettings
                editor?.document?.let {
                    ApplicationManager.getApplication().runWriteAction {
                        CommandProcessor.getInstance().executeCommand(project, {
                            val newTag = mainSettings.tagPrefix + entryEditor.item.tag + mainSettings.tagSuffix
                            it.replaceString(textRange.startOffset, textRange.endOffset, newTag)
                        }, null, null, it)
                    }
                }
            }
        }
    }
}
