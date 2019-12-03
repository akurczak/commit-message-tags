package pl.kurczak.idea.committags.branch

import com.intellij.openapi.project.Project
import com.intellij.openapi.vcs.changes.Change
import pl.kurczak.idea.committags.common.TagsCreator

internal class BranchTagCreator(
    private val project: Project,
    private val branchTagPattern: BranchTagPattern
) : TagsCreator {

    override fun createTagsContent(changes: List<Change>): List<String> = changes.mapNotNull {
        BranchUtil.getCurrentBranch(project, it)?.branchName
    }.distinct().mapNotNull {
        branchTagPattern.findTag(it)
    }
}
