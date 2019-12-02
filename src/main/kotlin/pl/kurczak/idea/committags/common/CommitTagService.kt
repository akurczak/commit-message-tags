package pl.kurczak.idea.committags.common

import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.vcs.changes.Change
import kotlin.streams.toList

internal val commitTagServiceExtensionPoint =
    ExtensionPointName.create<CommitTagService<*>>("pl.kurczak.idea.committags.commitTagService")

internal fun commitTagServices(project: Project) = commitTagServiceExtensionPoint.extensions(project).toList()

internal fun commitTagServicesById(project: Project) = commitTagServices(project).associateBy { it.id }

internal fun commitTagServices(project: Project, ids: List<CommitTagServiceId>) =
    commitTagServicesById(project).let { services ->
        ids.mapNotNull { services[it] }
    }

interface CommitTagService<out Creator : TagsCreator> {

    val id: CommitTagServiceId

    val displayName: String

    fun createSettingsPanel(): DialogPanel?

    fun createTagCreator(): Creator
}

interface TagsCreator {

    fun createTagsContent(changes: List<Change>): List<String>
}

fun TagsCreator.createTags(tagPrefix: String, tagSuffix: String, changes: List<Change>) =
    createTagsContent(changes).joinToString(separator = "") {
        "$tagPrefix$it$tagSuffix"
    }

data class CommitTagServiceId(var id: String = "dummy")
