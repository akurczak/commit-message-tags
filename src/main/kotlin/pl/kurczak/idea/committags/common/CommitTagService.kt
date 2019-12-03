package pl.kurczak.idea.committags.common

import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.vcs.changes.Change
import pl.kurczak.idea.committags.common.settings.mainSettings
import kotlin.streams.asSequence
import kotlin.streams.toList

internal val commitTagServiceExtensionPoint =
    ExtensionPointName.create<CommitTagService<*>>("pl.kurczak.idea.committags.commitTagService")

internal fun Project.commitTagServices() = commitTagServiceExtensionPoint.extensions(this).toList()

internal fun Project.commitTagServicesById() = commitTagServices().associateBy { it.id }

internal inline fun <reified T> Project.commitTagService(): T? =
    commitTagServiceExtensionPoint.extensions(this).asSequence().first { it is T } as? T

internal fun Project.commitTagServices(ids: List<CommitTagServiceId>) =
    commitTagServicesById().let { services ->
        ids.mapNotNull { services[it] }
    }

internal fun Project.configuredCommitTagServices() = commitTagServices(mainSettings.orderedCommitTagServices)

abstract class CommitTagService<out Creator : TagsCreator>(protected val project: Project) {

    abstract val id: CommitTagServiceId

    abstract val displayName: String

    abstract fun createSettingsPanel(): DialogPanel?

    abstract fun createTagCreator(): Creator

    val enabled
        get() = id in project.mainSettings.orderedCommitTagServices
}

interface TagsCreator {

    fun createTagsContent(changes: List<Change>): List<String>
}

data class CommitTagServiceId(var id: String = "dummy")
