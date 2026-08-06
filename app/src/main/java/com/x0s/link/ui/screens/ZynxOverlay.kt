package com.x0s.link.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Send
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.x0s.link.ui.AppState
import com.x0s.link.ui.Overlay
import com.x0s.link.ui.components.Avatar
import com.x0s.link.ui.components.BadgeDot
import com.x0s.link.ui.theme.*

@Composable
fun ZynxOverlay(state: AppState) {
    AnimatedVisibility(
        visible = state.overlay == Overlay.ZYNX,
        enter = slideInVertically(tween(320)) { it },
        exit = slideOutVertically(tween(280)) { it }
    ) {
        Box(Modifier.fillMaxSize().background(Color.Black)) {
            if (state.feed.isNotEmpty()) {
                val pagerState = rememberPagerState(pageCount = { state.feed.size })
                VerticalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
                    val item = state.feed[page]
                    val liked = state.liked[1000 + page] == true
                    val savedFlag = state.saved[1000 + page] == true
                    Box(Modifier.fillMaxSize()) {
                        AsyncImage(
                            model = item.post.files.firstOrNull(),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        // bottom scrim
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .height(220.dp)
                                .align(Alignment.BottomCenter)
                                .background(
                                    androidx.compose.ui.graphics.Brush.verticalGradient(
                                        listOf(Color.Transparent, Color.Black.copy(alpha = .75f))
                                    )
                                )
                        )
                        Column(
                            Modifier
                                .align(Alignment.BottomStart)
                                .padding(16.dp, 0.dp, 72.dp, 20.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Avatar(item.author.avatar, 34.dp, RoundedCornerShape(9.dp))
                                Spacer(Modifier.width(8.dp))
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(item.author.displayName, color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                                        Spacer(Modifier.width(4.dp))
                                        BadgeDot(item.author.verified, 13.dp)
                                    }
                                    Text(item.author.handle, color = Color.White.copy(alpha = .7f), style = MaterialTheme.typography.bodyMedium)
                                }
                            }
                            Spacer(Modifier.height(6.dp))
                            if (item.post.caption.isNotBlank()) {
                                Text(item.post.caption, color = Color.White.copy(alpha = .92f), style = MaterialTheme.typography.bodyMedium)
                            }
                        }

                        Column(
                            Modifier
                                .align(Alignment.BottomEnd)
                                .padding(end = 14.dp, bottom = 20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(18.dp)
                        ) {
                            ZAction(
                                icon = if (liked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                                tint = if (liked) Color(0xFFFF3B5C) else Color.White,
                                label = "${item.post.likes + if (liked) 1 else 0}"
                            ) { state.liked[1000 + page] = !liked }
                            ZAction(
                                icon = if (savedFlag) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                                tint = if (savedFlag) XosAccent else Color.White,
                                label = null
                            ) { state.saved[1000 + page] = !savedFlag }
                            ZAction(icon = Icons.Outlined.Send, tint = Color.White, label = null) {}
                        }
                    }
                }
            } else {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = XosAccent)
                }
            }

            Text(
                "Zynx",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.align(Alignment.TopStart).padding(18.dp, 16.dp)
            )
        }
    }
}

@Composable
private fun ZAction(icon: androidx.compose.ui.graphics.vector.ImageVector, tint: Color, label: String?, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { onClick() }) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(26.dp))
        if (label != null) {
            Spacer(Modifier.height(3.dp))
            Text(label, color = Color.White.copy(alpha = .8f), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold)
        }
    }
}
