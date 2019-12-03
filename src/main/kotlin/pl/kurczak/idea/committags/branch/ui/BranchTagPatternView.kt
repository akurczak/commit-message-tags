package pl.kurczak.idea.committags.branch.ui

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogPanel
import com.intellij.ui.components.Label
import com.intellij.ui.layout.CellBuilder
import com.intellij.ui.layout.Row
import com.intellij.ui.layout.panel
import pl.kurczak.idea.committags.branch.BranchSettingsState
import pl.kurczak.idea.committags.branch.BranchTagPattern
import pl.kurczak.idea.committags.branch.BranchUtil
import pl.kurczak.idea.committags.common.settings.ui.onChange
import pl.kurczak.idea.committags.common.settings.ui.withBinding
import pl.kurczak.idea.committags.common.settings.ui.withPanelBindings
import javax.swing.JTextField

internal class BranchTagPatternView(
    project: Project,
    private val tagPrefix: String,
    private val tagSuffix: String,
    private val settings: BranchSettingsState
) {

    private val branchName = BranchUtil.getCurrentBranch(project)?.branchName

    private val currentBranchLabel = if (branchName == null) {
        Label("Unknown current branch")
    } else {
        Label("Current branch name: $branchName")
    }

    private val currentTagLabel = Label("")

    private val namedGroupsLabel = Label("")

    private val branchNamePatternField = JTextField(settings.branchNamePattern, 40)

    private val branchTagTemplateField = JTextField(settings.branchTagTemplate, 40)

    internal val component = panel {
        commentRow(
            """
            <p>Branch tag definition consists of two parts.</p><br/>
            <p>First, a regex matching the branch name, containing named capturing groups, e.g:</p>
            <pre>^prefix-.*-(?&lt;issue&gt;[0-9]+).*$</pre>
            <p>Second, a template containing variables in form of #{name}, e.g:</p>
            <pre>ISSUE #{issueId}</pre>
        """.trimIndent()
        )
        row("Branch name regex:") {
            branchNamePatternField().withBinding(settings::branchNamePattern).onChange {
                update()
            }
        }
        row("Branch tag template:") {
            branchTagTemplateField().withBinding(settings::branchTagTemplate).onChange {
                update()
            }
        }
        row { currentBranchLabel() }
        row { namedGroupsLabel() }
        row { currentTagLabel() }

    }

    init {
        update()
    }

    private fun update() {
        val branchNamePattern = branchNamePatternField.text
        val branchTagTemplate = branchTagTemplateField.text
        if (branchName != null && branchNamePattern != null && branchTagTemplate != null) {
            val pattern = BranchTagPattern(branchNamePattern, branchTagTemplate)
            namedGroupsLabel.text = "Available named captured groups: ${pattern.availableNamedGroups.joinToString()}"
            currentTagLabel.text = if (pattern.isValid) {
                val tag = pattern.findTag(branchName)
                if (tag == null) {
                    "Could not create tag!"
                } else {
                    "Tag preview: ${tagPrefix}$tag${tagSuffix}"
                }
            } else {
                "Branch name regex is invalid."
            }
        }
    }
}

internal fun Row.branchTagPatternView(
    project: Project,
    tagPrefix: String,
    tagSuffix: String,
    settings: BranchSettingsState
): CellBuilder<DialogPanel> {
    val component = BranchTagPatternView(project, tagPrefix, tagSuffix, settings).component
    return component().withPanelBindings()
}
