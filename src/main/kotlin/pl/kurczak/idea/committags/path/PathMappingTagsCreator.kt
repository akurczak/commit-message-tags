package pl.kurczak.idea.committags.path

import com.intellij.openapi.vcs.changes.Change
import pl.kurczak.idea.committags.common.TagsCreator

internal class PathMappingTagsCreator(private val pathMappings: List<PathMapping>) : TagsCreator {

    override fun createTagsContent(changes: List<Change>): List<String> = changes.asSequence()
        .flatMap { sequenceOf(it.beforeRevision, it.afterRevision) }
        .distinct()
        .filterNotNull()
        .mapNotNull { revision ->
            pathMappings.find { it.pathPattern in revision.file.path }?.tag
        }.distinct().toList()
}
