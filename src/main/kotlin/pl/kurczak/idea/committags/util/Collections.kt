package pl.kurczak.idea.committags.util

internal fun <K, V> Map<K, V?>.filterNotNullValues(): Map<K, V> {
    val result = LinkedHashMap<K, V>()
    for (entry in this) {
        val value = entry.value
        if (value != null) {
            result[entry.key] = value
        }
    }
    return result
}
