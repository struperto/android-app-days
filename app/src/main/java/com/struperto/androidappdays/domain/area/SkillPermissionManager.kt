package com.struperto.androidappdays.domain.area

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.core.content.ContextCompat

class SkillPermissionManager(
    private val context: Context,
) {
    fun isGranted(skillKind: AreaSkillKind): Boolean {
        val permissions = permissionsForSkill(skillKind)
        if (permissions.isEmpty()) return true
        return permissions.all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    fun requiredPermissions(skills: Set<AreaSkillKind>): Set<String> {
        return skills.flatMap { permissionsForSkill(it) }.toSet()
    }

    fun intentForSpecialPermission(skillKind: AreaSkillKind): Intent? {
        return when (skillKind) {
            AreaSkillKind.APP_USAGE -> Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
            AreaSkillKind.NOTIFICATION_FILTER -> Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
            else -> null
        }
    }

    private fun permissionsForSkill(skillKind: AreaSkillKind): List<String> {
        return when (skillKind) {
            AreaSkillKind.HEALTH_TRACKING -> emptyList()
            AreaSkillKind.CALENDAR_WATCH -> listOf(Manifest.permission.READ_CALENDAR)
            AreaSkillKind.NOTIFICATION_FILTER -> emptyList()
            AreaSkillKind.MANUAL_LOG -> emptyList()
            AreaSkillKind.SCREENSHOT_READER,
            AreaSkillKind.PHOTO_STREAM -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                listOf(Manifest.permission.READ_MEDIA_IMAGES)
            } else {
                listOf(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
            AreaSkillKind.CONTACT_WATCH -> listOf(Manifest.permission.READ_CONTACTS)
            AreaSkillKind.APP_USAGE -> emptyList()
            AreaSkillKind.WEBSITE_READER -> emptyList()
            AreaSkillKind.PODCAST_FOLLOW -> emptyList()
            AreaSkillKind.LOCATION_CONTEXT -> listOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
            )
            AreaSkillKind.CHECKLIST -> emptyList()
        }
    }
}
