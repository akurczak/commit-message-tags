package pl.kurczak.idea.committags.branch

import com.intellij.openapi.project.Project
import com.intellij.openapi.vcs.FilePath
import com.intellij.openapi.vcs.changes.Change
import com.intellij.openapi.vcs.changes.ChangesUtil
import com.intellij.vcs.branch.BranchData
import com.intellij.vcs.branch.BranchStateProvider
import com.intellij.vcsUtil.VcsUtil

internal object BranchUtil {

    fun getCurrentBranch(project: Project?, change: Change) = getCurrentBranch(project, ChangesUtil.getFilePath(change))

    fun getCurrentBranch(project: Project): BranchData? = project.basePath?.let {
        getCurrentBranch(project, VcsUtil.getFilePath(it))
    }

    private fun getCurrentBranch(project: Project?, filePath: FilePath): BranchData? =
        BranchStateProvider.EP_NAME.getExtensionList(project)
            .asSequence()
            .mapNotNull {
                it.getCurrentBranch(filePath)
            }.firstOrNull()
}
