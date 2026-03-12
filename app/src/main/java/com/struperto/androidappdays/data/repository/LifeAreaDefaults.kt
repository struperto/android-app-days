package com.struperto.androidappdays.data.repository

import com.struperto.androidappdays.domain.area.startAreaKernelSeeds
import com.struperto.androidappdays.feature.start.startSeedAreas

data class LifeAreaPreset(
    val id: String,
    val label: String,
    val definition: String,
    val targetScore: Int,
    val templateId: String,
    val iconKey: String,
)

val allLifeAreaPresets = startAreaKernelSeeds.map { seed ->
    LifeAreaPreset(
        id = seed.definition.id,
        label = seed.definition.title,
        definition = seed.blueprint.summary,
        targetScore = seed.definition.defaultConfig.targetScore,
        templateId = seed.blueprint.defaultTemplateId,
        iconKey = seed.definition.iconKey,
    )
}

fun defaultLifeAreas(): List<LifeArea> = startSeedAreas()
