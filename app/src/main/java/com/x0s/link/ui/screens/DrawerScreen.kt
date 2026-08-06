package com.x0s.link.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.x0s.link.ui.AppState
import com.x0s.link.ui.components.Avatar
import com.x0s.link.ui.components.BadgeDot
import com.x0s.link.ui.theme.*

@Composable
fun DrawerOverlay(state: AppState) {
    AnimatedVisibility(
        visible = state.drawerOpen,
        enter = fadeIn(tween(200)),
        exit = fadeOut(tween(200))
    ) {
        Box(
            Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = .6f))
                .clickable(indication = null, interactionSource = remember_null()) { state.closeDrawer() }
        )
    }

    AnimatedVisibility(
        visible = state.drawerOpen,
        enter = slideInHorizontally(tween(280)) { -it },
        exit = slideOutHorizontally(tween(240)) { -it }
    ) {
        val user = state.activeUser
        Column(
            Modifier
                .fillMaxHeight()
                .fillMaxWidth(0.72f)
                .background(XosBackground)
                .clickable(indication = null, interactionSource = remember_null()) { }
        ) {
            Column(Modifier.padding(20.dp, 20.dp, 16.dp, 14.dp)) {
                Box {
                    Avatar(user?.avatar ?: "", 54.dp, RoundedCornerShape(14.dp), modifier = Modifier.clickable {})
                    Box(Modifier.align(Alignment.BottomEnd)) { BadgeDot(user?.verified ?: com.x0s.link.data.Badge.NONE, 20.dp) }
                }
                Spacer(Modifier.height(10.dp))
                Text(user?.displayName ?: "", color = XosWhite, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Text(user?.handle ?: "", color = XosMuted, style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(10.dp))
                Row {
                    Text(
                        buildAnnotatedFollow((user?.following?.size ?: 0), "Following"),
                        color = XosMuted, style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(Modifier.width(14.dp))
                    Text(
                        buildAnnotatedFollow((user?.followers?.size ?: 0), "Followers"),
                        color = XosMuted, style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            HorizontalDivider(color = XosBorder)

            DrawerItem(Icons.Filled.Person, "Profile") {}
            DrawerItem(Icons.Filled.Bookmark, "Bookmarks") {}
            DrawerItem(Icons.Filled.ListAlt, "Lists") {}
            DrawerItem(Icons.Filled.WorkspacePremium, "Get Badge") {}
            DrawerItem(Icons.Filled.HelpOutline, "Help & Support") {}

            Spacer(Modifier.weight(1f))

            Row(
                Modifier.fillMaxWidth().padding(16.dp, 14.dp, 16.dp, 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Dark Mode", color = XosWhite, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
                Switch(
                    checked = state.darkMode,
                    onCheckedChange = { state.darkMode = it },
                    colors = SwitchDefaults.colors(checkedTrackColor = XosAccent)
                )
            }
            Text(
                "X0S INDIA",
                color = XosMuted,
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(16.dp, 6.dp, 16.dp, 20.dp)
            )
        }
    }
}

private fun buildAnnotatedFollow(count: Int, label: String) = "$count $label"

@Composable
private fun DrawerItem(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp, 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = XosWhite, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(14.dp))
        Text(label, color = XosWhite, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
@Composable
private fun remember_null(): androidx.compose.foundation.interaction.MutableInteractionSource =
    androidx.compose.runtime.remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
