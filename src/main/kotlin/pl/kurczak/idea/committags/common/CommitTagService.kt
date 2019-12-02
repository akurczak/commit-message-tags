package pl.kurczak.idea.committags.common

import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogPanel
import kotlin.streams.toList

internal val commitTagServiceExtensionPoint =
    ExtensionPointName.create<CommitTagService<*>>("pl.kurczak.idea.committags.commitTagService")

internal fun commitTagServices(project: Project) = commitTagServiceExtensionPoint.extensions(project).toList()

internal fun commitTagServicesById(project: Project) = commitTagServices(project).associateBy { it.id }

internal fun commitTagServices(project: Project, ids: List<CommitTagServiceId>) = commitTagServicesById(project).let { services ->
    ids.mapNotNull { services[it] }
}

interface CommitTagService<out Creator : TagsCreator> {

    val id: CommitTagServiceId

    val displayName: String

    fun createSettingsPanel(): DialogPanel

    fun createTagCreator(tagPrefix: String, tagSuffix: String): Creator
}

abstract class TagsCreator(private val tagPrefix: String, private val tagSuffix: String) {

    fun createTags() = createTagsContent().joinToString { "$tagPrefix$it$tagSuffix" }

    protected abstract fun createTagsContent(): List<String>
}

data class CommitTagServiceId(var id: String = "dummy")
