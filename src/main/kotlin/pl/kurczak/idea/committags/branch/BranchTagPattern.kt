package pl.kurczak.idea.committags.branch

import java.util.regex.PatternSyntaxException

private val NAMED_GROUP_PATTERN = Regex.escape("(?<") + "([^>]*)" + Regex.escape(">")
private val NAMED_GROUP_REGEX = NAMED_GROUP_PATTERN.toRegex()

internal class BranchTagPattern(branchNamePattern: String, private val branchTagTemplate: String) {

    private val branchTagRegex = try {
        branchNamePattern.toRegex()
    } catch (e: PatternSyntaxException) {
        null
    }

    val availableNamedGroups = NAMED_GROUP_REGEX.findAll(branchNamePattern).flatMap {
        it.groupValues.asSequence().drop(1)
    }.toList()

    val isValid = branchTagRegex != null

    fun findTag(branchName: String): String? {

        if (!isValid) return null
        val groups = branchTagRegex?.matchEntire(branchName)?.groups ?: return null
        var tag = branchTagTemplate
        for (groupName in availableNamedGroups) {
            val groupValue = groups[groupName]?.value
            if (groupValue != null) {
                tag = tag.replace("#{$groupName}", groupValue)
            }
        }
        return tag
    }
}
