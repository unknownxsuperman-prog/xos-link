package com.x0s.link.ui.nav

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.PlayCircle
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Send
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

/**
 * Mirrors the web app's .bottom-nav exactly: a fixed, full-width, blurred bar with a hairline
 * top border, holding 5 evenly-spaced icon-only nav-items (no labels, no floating pill).
 * Active tab is plain white/foreground; inactive is muted. Notifications carries a small red
 * unread dot, same as .nav-notif-dot.
 */
@Composable
fun XosBottomNav(
    currentRoute: String?,
    hasUnreadNotifications: Boolean,
    darkTheme: Boolean,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val barBg = if (darkTheme) Color.Black.copy(alpha = 0.8f) else Color.White.copy(alpha = 0.88f)
    val borderColor = if (darkTheme) Color.White.copy(alpha = 0.06f) else Color.Black.copy(alpha = 0.08f)
    val activeColor = if (darkTheme) Color.White else Color.Black
    val mutedColor = Color(0xFF666666)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(barBg)
            .drawTopBorder(borderColor)
            .navigationBarsPadding()
            .height(62.dp)
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        NavIcon(
            selected = currentRoute == Dest.FEED,
            filled = Icons.Filled.Home,
            outlined = Icons.Outlined.Home,
            activeColor = activeColor,
            mutedColor = mutedColor,
            onClick = { onNavigate(Dest.FEED) }
        )
        NavIcon(
            selected = currentRoute == Dest.SEARCH,
            filled = Icons.Filled.Search,
            outlined = Icons.Outlined.Search,
            activeColor = activeColor,
            mutedColor = mutedColor,
            onClick = { onNavigate(Dest.SEARCH) }
        )
        NavIcon(
            selected = currentRoute == Dest.ZYNX,
            filled = Icons.Filled.PlayCircle,
            outlined = Icons.Outlined.PlayCircle,
            activeColor = activeColor,
            mutedColor = mutedColor,
            onClick = { onNavigate(Dest.ZYNX) }
        )
        NavIcon(
            selected = currentRoute == Dest.MESSAGES,
            filled = Icons.Filled.Send,
            outlined = Icons.Outlined.Send,
            activeColor = activeColor,
            mutedColor = mutedColor,
            onClick = { onNavigate(Dest.MESSAGES) }
        )
        Box {
            NavIcon(
                selected = currentRoute == Dest.NOTIFICATIONS,
                filled = Icons.Filled.Notifications,
                outlined = Icons.Outlined.Notifications,
                activeColor = activeColor,
                mutedColor = mutedColor,
                onClick = { onNavigate(Dest.NOTIFICATIONS) }
            )
            if (hasUnreadNotifications) {
                Box(
                    Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 4.dp, end = 10.dp)
                        .size(7.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFF3B5C))
                )
            }
        }
    }
}

@Composable
private fun NavIcon(
    selected: Boolean,
    filled: ImageVector,
    outlined: ImageVector,
    activeColor: Color,
    mutedColor: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(52.dp)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = if (selected) filled else outlined,
            contentDescription = null,
            tint = if (selected) activeColor else mutedColor,
            modifier = Modifier.size(22.dp)
        )
    }
}

private fun Modifier.drawTopBorder(color: Color): Modifier = this.then(
    androidx.compose.ui.draw.drawWithContent {
        drawContent()
        drawLine(
            color = color,
            start = androidx.compose.ui.geometry.Offset(0f, 0f),
            end = androidx.compose.ui.geometry.Offset(size.width, 0f),
            strokeWidth = 1f
        )
    }
)
