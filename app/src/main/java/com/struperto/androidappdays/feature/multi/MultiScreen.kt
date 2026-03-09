package com.struperto.androidappdays.feature.multi

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Coffee
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Forum
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.NearMe
import androidx.compose.material.icons.outlined.OfflineBolt
import androidx.compose.material.icons.outlined.RocketLaunch
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.struperto.androidappdays.feature.single.shared.DaysTile
import com.struperto.androidappdays.feature.single.shared.TileRole
import com.struperto.androidappdays.navigation.AppDestination
import com.struperto.androidappdays.navigation.DaysModeTopBar
import com.struperto.androidappdays.ui.theme.AppTheme

private val OrbitShellTop = Color(0xFFF4EEE5)
private val OrbitShellBottom = Color(0xFFECE2D5)
private val OrbitLine = Color(0xFFD7CCBF)
private val OrbitLineSoft = Color(0xFFE9E0D5)

@Composable
fun MultiScreen(
    onOpenStart: () -> Unit,
    onOpenSingle: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    var nodes by remember { mutableStateOf<List<OrbitNode>>(emptyList()) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppTheme.colors.surface),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            DaysModeTopBar(
                activeDestination = AppDestination.Multi,
                onOpenStart = onOpenStart,
                onOpenSingle = onOpenSingle,
                onOpenMulti = {},
                onOpenSettings = onOpenSettings,
                settingsTestTag = "multi-open-settings",
            )

            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 16.dp),
            ) {
                val isExpanded = maxWidth >= 840.dp
                val colors = AppTheme.colors

                if (nodes.isEmpty()) {
                    MultiEmptyState(
                        onLoadDemo = { nodes = getSampleNodes(colors) },
                        modifier = Modifier.align(Alignment.Center)
                    )
                } else {
                    if (isExpanded) {
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            horizontalArrangement = Arrangement.spacedBy(20.dp),
                        ) {
                            OrbitTile(
                                nodes = nodes,
                                modifier = Modifier
                                    .weight(1.5f)
                                    .fillMaxHeight()
                                    .testTag("multi-orbit-hero"),
                            )
                            MultiDetailPane(
                                nodes = nodes,
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight(),
                            )
                        }
                    } else {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                        ) {
                            OrbitTile(
                                nodes = nodes,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(0.85f)
                                    .testTag("multi-orbit-compact"),
                            )
                            MultiDetailPane(
                                nodes = nodes,
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MultiEmptyState(
    onLoadDemo: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .widthIn(max = 480.dp)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(AppTheme.colors.accentSoft.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.Groups,
                contentDescription = null,
                tint = AppTheme.colors.accent,
                modifier = Modifier.size(56.dp)
            )
        }

        DaysTile(
            role = TileRole.Hero,
            headline = "Allein ist gut. Gemeinsam ist besser.",
            support = "Der Multi-Modus hilft dir, deinen Tag mit anderen zu koordinieren, ohne ständig Pläne zu vergleichen.",
            containerColor = AppTheme.colors.surfaceStrong
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "• Sehe den 'Drift' deines Teams auf einen Blick.\n" +
                           "• Finde Zeitfenster für Fokus oder Austausch.\n" +
                           "• Schütze deine Privatsphäre: Nur Status, keine Details.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = { /* Add member action */ },
                shape = RoundedCornerShape(22.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AppTheme.colors.accent),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Outlined.Add, contentDescription = null)
                Text("Mitglied hinzufügen", modifier = Modifier.padding(start = 8.dp))
            }
            
            TextButton(
                onClick = onLoadDemo,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Outlined.RocketLaunch, contentDescription = null, modifier = Modifier.size(18.dp))
                Text("Demo-Cluster laden", modifier = Modifier.padding(start = 8.dp))
            }
        }
    }
}

@Composable
private fun MultiDetailPane(
    nodes: List<OrbitNode>,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        DaysTile(
            role = TileRole.Hero,
            eyebrow = "Koordinations-Overview",
            headline = "Team Pulse",
            support = "${nodes.size} Personen im Cluster aktiv",
        )

        nodes.take(4).forEach { node ->
            DaysTile(
                role = TileRole.Summary,
                headline = node.label,
                containerColor = node.tint.copy(alpha = 0.08f),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Icon(
                        imageVector = node.icon,
                        contentDescription = null,
                        tint = node.tint,
                        modifier = Modifier.size(24.dp),
                    )
                    Text(
                        text = if (node.isPrimary) "Aktiv am Planen" else "Im Fokus",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun OrbitTile(
    nodes: List<OrbitNode>,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(
            containerColor = AppTheme.colors.surfaceStrong,
        ),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            OrbitShellTop,
                            OrbitShellBottom,
                        ),
                    ),
                )
                .padding(horizontal = 8.dp, vertical = 10.dp),
        ) {
            OrbitDashboard(
                nodes = nodes,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

@Composable
private fun OrbitDashboard(
    nodes: List<OrbitNode>,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(modifier = modifier) {
        val accent = AppTheme.colors.accent
        val info = AppTheme.colors.info
        val warning = AppTheme.colors.warning

        val orbitWidth = (maxWidth - 8.dp).coerceAtLeast(260.dp)
        val orbitHeight = (maxHeight - 18.dp).coerceAtLeast(420.dp)
        val coreSize = (orbitWidth * 0.27f).coerceIn(126.dp, 172.dp)

        Box(modifier = Modifier.fillMaxSize()) {
            Canvas(
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxSize(),
            ) {
                val widthPx = orbitWidth.toPx()
                val heightPx = orbitHeight.toPx()
                val center = Offset(size.width / 2f, size.height / 2f)
                val halfWidth = widthPx / 2f
                val halfHeight = heightPx / 2f
                val stroke = widthPx * 0.0105f

                drawOval(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            accent.copy(alpha = 0.08f),
                            Color.Transparent,
                        ),
                        center = center,
                        radius = halfWidth,
                    ),
                    topLeft = Offset(center.x - halfWidth * 0.74f, center.y - halfHeight * 0.66f),
                    size = Size(halfWidth * 1.48f, halfHeight * 1.32f),
                )

                listOf(0.22f, 0.40f, 0.58f, 0.78f, 0.92f).forEachIndexed { index, factor ->
                    drawOval(
                        color = if (index == 0) accent.copy(alpha = 0.16f) else OrbitLineSoft.copy(alpha = 0.92f),
                        topLeft = Offset(
                            center.x - halfWidth * factor,
                            center.y - halfHeight * factor,
                        ),
                        size = Size(
                            halfWidth * factor * 2f,
                            halfHeight * factor * 2f,
                        ),
                        style = Stroke(width = stroke * 1.35f),
                    )
                }

                drawArc(
                    color = info.copy(alpha = 0.42f),
                    startAngle = -28f,
                    sweepAngle = 54f,
                    useCenter = false,
                    topLeft = Offset(center.x - halfWidth * 0.92f, center.y - halfHeight * 0.92f),
                    size = Size(halfWidth * 1.84f, halfHeight * 1.84f),
                    style = Stroke(width = stroke * 2.6f, cap = StrokeCap.Round),
                )
                
                nodes.forEach { node ->
                    drawCircle(
                        color = node.tint.copy(alpha = if (node.isPrimary) 0.22f else 0.12f),
                        radius = stroke * if (node.isPrimary) 2.4f else 1.9f,
                        center = Offset(
                            center.x + halfWidth * node.offsetX,
                            center.y + halfHeight * node.offsetY,
                        ),
                    )
                }
            }

            OrbitCore(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(coreSize),
                tint = accent,
            )

            nodes.forEach { node ->
                OrbitNodeItem(
                    node = node,
                    orbitWidth = orbitWidth,
                    orbitHeight = orbitHeight,
                    modifier = Modifier.align(Alignment.Center),
                )
            }
        }
    }
}

@Composable
private fun OrbitCore(
    modifier: Modifier = Modifier,
    tint: Color,
) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        AppTheme.colors.surfaceStrong,
                        AppTheme.colors.surface,
                    ),
                ),
            )
            .border(1.dp, tint.copy(alpha = 0.26f), CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(58.dp)
                .clip(CircleShape)
                .background(tint.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Outlined.OfflineBolt,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(28.dp),
            )
        }
    }
}

@Composable
private fun OrbitNodeItem(
    node: OrbitNode,
    orbitWidth: Dp,
    orbitHeight: Dp,
    modifier: Modifier = Modifier,
) {
    val bubbleSize = if (node.isPrimary) 104.dp else 88.dp
    val iconSize = if (node.isPrimary) 28.dp else 24.dp

    Column(
        modifier = modifier.offset(
            x = orbitWidth * node.offsetX,
            y = orbitHeight * node.offsetY,
        ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Box(
            modifier = Modifier
                .size(bubbleSize)
                .clip(CircleShape)
                .background(node.tint.copy(alpha = if (node.isPrimary) 0.14f else 0.11f))
                .border(1.dp, node.tint.copy(alpha = 0.36f), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = node.icon,
                contentDescription = null,
                tint = node.tint,
                modifier = Modifier.size(iconSize),
            )
        }
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(999.dp))
                .background(AppTheme.colors.surfaceStrong.copy(alpha = 0.72f))
                .border(1.dp, node.tint.copy(alpha = 0.16f), RoundedCornerShape(999.dp))
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(node.tint),
            )
            Text(
                text = node.label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

private fun getSampleNodes(colors: com.struperto.androidappdays.ui.theme.AppColors): List<OrbitNode> {
    return listOf(
        OrbitNode("Mara", 0.28f, -0.41f, colors.accent, Icons.Outlined.Coffee, true),
        OrbitNode("Noah", -0.38f, -0.14f, colors.info, Icons.Outlined.NearMe, false),
        OrbitNode("Ela", 0.40f, -0.01f, colors.warning, Icons.Outlined.AutoAwesome, false),
        OrbitNode("Mina", -0.14f, 0.28f, colors.success, Icons.Outlined.FavoriteBorder, false),
        OrbitNode("Cluster", 0.18f, 0.37f, colors.danger, Icons.Outlined.Groups, false),
        OrbitNode("Jonas", -0.41f, 0.12f, colors.muted, Icons.Outlined.Forum, false),
    )
}

private data class OrbitNode(
    val label: String,
    val offsetX: Float,
    val offsetY: Float,
    val tint: Color,
    val icon: ImageVector,
    val isPrimary: Boolean,
)
