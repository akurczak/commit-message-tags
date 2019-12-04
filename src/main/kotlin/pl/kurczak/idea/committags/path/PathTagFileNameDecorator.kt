package pl.kurczak.idea.committags.path

import com.intellij.openapi.project.Project
import com.intellij.openapi.vcs.changes.ui.ChangesBrowserNodeRenderer
import com.intellij.openapi.vcs.changes.ui.WolfChangesFileNameDecorator
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.ui.JBColor
import com.intellij.ui.SimpleTextAttributes
import pl.kurczak.idea.committags.common.commitTagService
import java.awt.Color

internal class PathTagFileNameDecorator(private val project: Project) : WolfChangesFileNameDecorator(project) {

    override fun appendFileName(
        renderer: ChangesBrowserNodeRenderer,
        vFile: VirtualFile?,
        fileName: String?,
        color: Color?,
        highlightProblems: Boolean
    ) {
        super.appendFileName(renderer, vFile, fileName, color, highlightProblems)
        if (vFile == null) {
            return
        }
        val pathMappingTagService = project.commitTagService<PathMappingTagService>()?.takeIf { it.enabled } ?: return
        val settings = pathMappingTagService.settings.takeIf { it.pathTagsDisplay != PathTagsDisplay.NONE } ?: return
        val tag = settings.pathMappings.findTag(vFile.path).toTag()
        renderer.appendTag(tag)
    }
}

private val tagStyle = SimpleTextAttributes.GRAY_SMALL_ATTRIBUTES
private val errorStyle = tagStyle.derive(-1, JBColor.RED, null, null)

private sealed class Tag(val text: String, val attributes: SimpleTextAttributes)
private class ValidTag(text: String) : Tag(text, tagStyle)
private object UnknownTag : Tag("Unknown tag!", errorStyle)

private fun ChangesBrowserNodeRenderer.appendTag(tag: Tag) = append("  [${tag.text}]", tag.attributes, false)

private fun String?.toTag(): Tag = this?.let { ValidTag(it) } ?: UnknownTag
