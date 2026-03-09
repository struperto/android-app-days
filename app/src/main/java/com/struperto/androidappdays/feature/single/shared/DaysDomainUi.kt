package com.struperto.androidappdays.feature.single.shared

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.DirectionsWalk
import androidx.compose.material.icons.outlined.CenterFocusStrong
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Hotel
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.ui.graphics.vector.ImageVector
import com.struperto.androidappdays.data.repository.lifeDomainLabel
import com.struperto.androidappdays.domain.LifeDomain

fun daysDomainIcon(domain: LifeDomain): ImageVector {
    return when (domain) {
        LifeDomain.SLEEP -> Icons.Outlined.Hotel
        LifeDomain.MOVEMENT -> Icons.AutoMirrored.Outlined.DirectionsWalk
        LifeDomain.HYDRATION -> Icons.Outlined.WaterDrop
        LifeDomain.NUTRITION -> Icons.Outlined.Restaurant
        LifeDomain.FOCUS -> Icons.Outlined.CenterFocusStrong
        LifeDomain.RECOVERY -> Icons.Outlined.FavoriteBorder
        LifeDomain.STRESS -> Icons.Outlined.NotificationsActive
        else -> Icons.Outlined.EditNote
    }
}

fun daysDomainShortLabel(domain: LifeDomain): String {
    return when (domain) {
        LifeDomain.SLEEP -> "Schlaf"
        LifeDomain.MOVEMENT -> "Bewegung"
        LifeDomain.HYDRATION -> "Wasser"
        LifeDomain.NUTRITION -> "Essen"
        LifeDomain.FOCUS -> "Fokus"
        LifeDomain.RECOVERY -> "Erholung"
        LifeDomain.STRESS -> "Druck"
        else -> lifeDomainLabel(domain)
    }
}
