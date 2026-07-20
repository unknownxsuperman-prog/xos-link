package com.x0s.link.ui.nav

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.PlayCircle
import androidx.compose.material.icons.outlined.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Floating, semi-transparent pill nav bar (matches the reference screenshot): a rounded
 * capsule holding Home / Search / Zynx / Messages - with the active tab expanding to show
 * its icon + label - plus a separate circular button off to the side for Notifications.
 * Floats over content rather than docking a full-width opaque bar, and stays clear of the
 * system gesture area via navigationBarsPadding().
 */
@Composable
fun FloatingNavBar(
    currentRoute: String?,
    hasUnreadNotifications: Boolean,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 18.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Main pill: Home, Search, Zynx, Messages
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(99.dp))
                .background(Color.Black.copy(alpha = 0.6f))
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            PillNavItem(
                selected = currentRoute == Dest.FEED,
                label = "Home",
                filledIcon = Icons.Filled.Home,
                outlinedIcon = Icons.Outlined.Home,
                onClick = { onNavigate(Dest.FEED) }
            )
            PillNavItem(
                selected = currentRoute == Dest.SEARCH,
                label = "Disha",
                filledIcon = Icons.Filled.AutoAwesome,
                outlinedIcon = Icons.Outlined.AutoAwesome,
                onClick = { onNavigate(Dest.SEARCH) }
            )
            PillNavItem(
                selected = currentRoute == Dest.ZYNX,
                label = "Zynx",
                filledIcon = Icons.Filled.PlayCircle,
                outlinedIcon = Icons.Outlined.PlayCircle,
                onClick = { onNavigate(Dest.ZYNX) }
            )
            PillNavItem(
                selected = currentRoute == Dest.MESSAGES,
                label = "Messages",
                filledIcon = Icons.Filled.Send,
                outlinedIcon = Icons.Outlined.Send,
                onClick = { onNavigate(Dest.MESSAGES) }
            )
        }

        // Separate circular button: Notifications
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.6f))
                .clickable { onNavigate(Dest.NOTIFICATIONS) },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (currentRoute == Dest.NOTIFICATIONS) Icons.Filled.Notifications else Icons.Outlined.Notifications,
                contentDescription = "Notifications",
                tint = Color.White,
                modifier = Modifier.size(22.dp)
            )
            if (hasUnreadNotifications) {
                Box(
                    Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 10.dp, end = 10.dp)
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                )
            }
        }
    }
}

@Composable
private fun PillNavItem(
    selected: Boolean,
    label: String,
    filledIcon: ImageVector,
    outlinedIcon: ImageVector,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(99.dp))
            .background(if (selected) Color.White.copy(alpha = 0.16f) else Color.Transparent)
            .clickable { onClick() }
            .padding(horizontal = if (selected) 14.dp else 11.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (selected) filledIcon else outlinedIcon,
            contentDescription = label,
            tint = Color.White,
            modifier = Modifier.size(20.dp)
        )
        AnimatedVisibility(visible = selected, enter = fadeIn(), exit = fadeOut()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Spacer(Modifier.width(7.dp))
                Text(label, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}
