package pl.kurczak.idea.committags.common

import com.intellij.openapi.project.Project
import pl.kurczak.idea.committags.common.settings.MainSettingsState
import pl.kurczak.idea.committags.common.settings.mainSettings

data class TaggedMessage(
    private val tagPrefix: String,
    private val tagSuffix: String,
    val bareMessage: String,
    val tags: List<String>
) {

    val tagsString = tags.joinToString(separator = "") { "$tagPrefix$it$tagSuffix" }

    val fullMessage = "$tagsString $bareMessage"

    val fullMessageWithDot = if (fullMessage.last() == '.') {
        fullMessage
    } else {
        "$fullMessage."
    }
}

private val messageRegexCache = mutableMapOf<Pair<String, String>, Regex>()
private val splitRegexCache = mutableMapOf<Pair<String, String>, Regex>()

fun parseTaggedMessage(project: Project, message: String): TaggedMessage {

    val settings = project.mainSettings
    val messageRegex = messageRegexCache.getOrPut(settings.tags) {
        val escapedPrefix = Regex.escape(settings.tagPrefix)
        val escapedSuffix = Regex.escape(settings.tagSuffix)
        """^(?<tags>(${escapedPrefix}[^${escapedSuffix}]*${escapedSuffix}[\n\r\s]*)*)(?<message>.*)$""".toRegex(RegexOption.DOT_MATCHES_ALL)
    }
    val groups = messageRegex.matchEntire(message)?.groups
    val tags = groups?.get("tags")?.value ?: ""
    val bareMessage = groups?.get("message")?.value?.trim() ?: ""
    return TaggedMessage(settings.tagPrefix, settings.tagSuffix, bareMessage, splitTags(settings, tags))
}

private fun splitTags(settings: MainSettingsState, tagsString: String): List<String> {
    val splitRegex = splitRegexCache.getOrPut(settings.tags) {
        val escapedPrefix = Regex.escape(settings.tagPrefix)
        val escapedSuffix = Regex.escape(settings.tagSuffix)
        """$escapedSuffix[\n\r\s]*$escapedPrefix""".toRegex()
    }
    val tags = tagsString.split(splitRegex).map { it.trim() }.toMutableList()
    if (tags.size > 0) {
        tags[0] = tags[0].drop(1)
        tags[tags.lastIndex] = tags[tags.lastIndex].dropLast(1)
    }
    return tags.filter { it.isNotEmpty() }
}

private val MainSettingsState.tags get() = tagPrefix to tagSuffix
