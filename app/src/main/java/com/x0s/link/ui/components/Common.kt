package com.x0s.link.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.x0s.link.data.Badge
import com.x0s.link.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun Avatar(url: String, size: Dp, shape: androidx.compose.ui.graphics.Shape = RoundedCornerShape(10.dp), modifier: Modifier = Modifier) {
    AsyncImage(
        model = url,
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = modifier.size(size).clip(shape).background(XosCard)
    )
}

@Composable
fun BadgeDot(badge: Badge, size: Dp = 14.dp) {
    if (badge == Badge.NONE) return
    val color = when (badge) {
        Badge.GOLD -> XosGold
        Badge.BLUE -> XosAccent
        Badge.GREEN -> Color(0xFF25D366)
        Badge.NONE -> Color.Transparent
    }
    Box(
        modifier = Modifier.size(size).clip(CircleShape).background(color),
        contentAlignment = Alignment.Center
    ) {
        Icon(Icons.Filled.Check, contentDescription = null, tint = Color.Black, modifier = Modifier.size(size * 0.62f))
    }
}

@Composable
fun PillButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, filled: Boolean = true, following: Boolean = false) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(30.dp))
            .background(
                when {
                    following -> XosGlass
                    filled -> XosWhite
                    else -> Color.Transparent
                }
            )
            .then(if (following) Modifier else Modifier)
            .padding(horizontal = 20.dp, vertical = 9.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (following) XosWhite.copy(alpha = .5f) else if (filled) Color.Black else XosWhite,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

/** Bottom nav mirroring index.html: single-tap search opens Search overlay,
 *  a fast second tap toggles into Disha "essential" search instead. */
@Composable
fun XosBottomBar(
    active: String,
    onHome: () -> Unit,
    onOpenSearch: () -> Unit,
    onOpenEssential: () -> Unit,
    onZynx: () -> Unit,
    onMessages: () -> Unit,
    onNotif: () -> Unit
) {
    var tapCount by remember { mutableStateOf(0) }
    val scope = rememberCoroutineScope()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(XosBackground.copy(alpha = .8f))
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        NavIcon(Icons.Filled.Home, active == "home") { onHome() }
        Box {
            NavIcon(Icons.Filled.Search, active == "search") {
                tapCount++
                if (tapCount == 1) {
                    scope.launch {
                        delay(300)
                        if (tapCount == 1) onOpenSearch()
                        tapCount = 0
                    }
                } else {
                    tapCount = 0
                    onOpenEssential()
                }
            }
        }
        NavIcon(Icons.Filled.PlayCircle, active == "zynx", size = 30.dp) { onZynx() }
        NavIcon(Icons.Filled.Send, active == "messages") { onMessages() }
        Box {
            NavIcon(Icons.Outlined.Notifications, active == "notif") { onNotif() }
            Box(
                Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = (-2).dp, y = 4.dp)
                    .size(7.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFFF3B5C))
            )
        }
    }
}

@Composable
private fun NavIcon(icon: androidx.compose.ui.graphics.vector.ImageVector, active: Boolean, size: Dp = 24.dp, onClick: () -> Unit) {
    IconButton(onClick = onClick) {
        Icon(icon, contentDescription = null, tint = if (active) XosWhite else XosMuted, modifier = Modifier.size(size))
    }
}

/** Small bottom toast, mirrors #toast in index.html */
@Composable
fun XosToast(message: String?) {
    AnimatedVisibility(
        visible = message != null,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = Modifier.fillMaxWidth().padding(bottom = 84.dp),
    ) {
        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.BottomCenter) {
            Box(
                Modifier
                    .clip(RoundedCornerShape(99.dp))
                    .background(Color(0xFF1E1E1E).copy(alpha = .92f))
                    .padding(horizontal = 20.dp, vertical = 10.dp)
            ) {
                Text(message ?: "", color = XosWhite, style = MaterialTheme.typography.bodyMedium, fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold)
            }
        }
    }
}
