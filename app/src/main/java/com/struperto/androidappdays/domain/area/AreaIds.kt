package com.struperto.androidappdays.domain.area

val legacyStartAreaIdAliases: Map<String, String> = linkedMapOf(
    "curiosity" to "discovery",
    "purpose" to "meaning",
)

fun canonicalStartAreaId(areaId: String): String {
    return legacyStartAreaIdAliases[areaId] ?: areaId
}
