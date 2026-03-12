package com.struperto.androidappdays.domain.area

import java.time.Instant

sealed class SkillTodayOutput {
    data class Screenshots(
        val count: Int,
        val latestAt: Instant?,
    ) : SkillTodayOutput()

    data class Photos(
        val count: Int,
        val latestAt: Instant?,
    ) : SkillTodayOutput()

    data class AppUsage(
        val totalMinutes: Int,
        val topApp: String?,
    ) : SkillTodayOutput()

    data class Website(
        val title: String?,
        val excerpt: String?,
        val fetchedAt: Instant?,
    ) : SkillTodayOutput()

    data class Podcast(
        val newEpisodeCount: Int,
        val latestTitle: String?,
    ) : SkillTodayOutput()

    data class Location(
        val isNear: Boolean,
        val distanceMeters: Int?,
    ) : SkillTodayOutput()

    data class Contacts(
        val recentName: String?,
        val daysSince: Int?,
    ) : SkillTodayOutput()

    data class Checklist(
        val total: Int,
        val completed: Int,
    ) : SkillTodayOutput()
}
