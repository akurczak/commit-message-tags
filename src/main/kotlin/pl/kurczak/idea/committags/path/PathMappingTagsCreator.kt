package pl.kurczak.idea.committags.path

import com.intellij.openapi.vcs.changes.Change
import pl.kurczak.idea.committags.common.TagsCreator

internal class PathMappingTagsCreator(private val pathMappings: List<PathMapping>) : TagsCreator {

    override fun createTagsContent(changes: List<Change>): List<String> = changes.asSequence()
        .flatMap { sequenceOf(it.beforeRevision, it.afterRevision) }
        .distinct()
        .filterNotNull()
        .mapNotNull {
            pathMappings.findTag(it.file.path)
        }.distinct().toList()
}
