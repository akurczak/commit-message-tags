package pl.kurczak.idea.committags.common

import com.intellij.codeInsight.intention.IntentionAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiFile

internal class RemoveTagIntentionAction(private val textRange: TextRange, private val tagType: String) : IntentionAction {
    override fun startInWriteAction(): Boolean = true

    override fun getFamilyName(): String = "Remove Tag"

    override fun isAvailable(project: Project, editor: Editor?, file: PsiFile?): Boolean = editor?.document != null

    override fun getText(): String = "Remove $tagType tag"

    override fun invoke(project: Project, editor: Editor?, file: PsiFile?) {
        val document = editor?.document ?: return
        document.deleteString(textRange.startOffset, textRange.endOffset)
        editor.caretModel.moveToOffset(textRange.startOffset)
    }
}
