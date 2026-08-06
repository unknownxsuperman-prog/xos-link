package com.x0s.link.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.x0s.link.data.FeedItem
import com.x0s.link.ui.AppState
import com.x0s.link.ui.components.Avatar
import com.x0s.link.ui.components.BadgeDot
import com.x0s.link.ui.theme.*

@Composable
fun FeedScreen(state: AppState) {
    Column(modifier = Modifier.fillMaxSize().background(XosBackground)) {
        // ── top bar ──
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(XosBackground.copy(alpha = .55f))
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Avatar(
                url = state.activeUser?.avatar ?: "",
                size = 32.dp,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.align(Alignment.CenterStart).clickable { state.openDrawer() }
            )
            Text("x0s.link", style = MaterialTheme.typography.titleLarge, color = XosWhite, modifier = Modifier.align(Alignment.Center))
            Box {
                Icon(
                    Icons.Filled.MoreVert,
                    contentDescription = "menu",
                    tint = XosMuted,
                    modifier = Modifier.align(Alignment.CenterEnd).clickable { state.topDropdownOpen = !state.topDropdownOpen }
                )
            }
        }

        // ── tabs ──
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(XosBackground.copy(alpha = .55f))
        ) {
            listOf("For You", "Following").forEachIndexed { i, label ->
                Column(
                    modifier = Modifier.weight(1f).padding(vertical = 13.dp).clickable { state.selectedTab = i },
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        label,
                        color = if (state.selectedTab == i) XosWhite else XosMuted,
                        fontWeight = if (state.selectedTab == i) FontWeight.Bold else FontWeight.Normal,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(Modifier.height(6.dp))
                    if (state.selectedTab == i) {
                        Box(Modifier.height(2.dp).width(50.dp).background(XosAccent))
                    }
                }
            }
        }
        HorizontalDivider(color = XosBorder, thickness = 1.dp)

        // ── feed ──
        LazyColumn(modifier = Modifier.weight(1f)) {
            itemsIndexed(state.feed) { i, item -> PostCard(state, i, item) }
        }
    }
}

@Composable
private fun PostCard(state: AppState, index: Int, item: FeedItem) {
    val post = item.post
    val u = item.author
    val liked = state.liked[index] == true
    val savedFlag = state.saved[index] == true
    val mutedFlag = state.muted[index] ?: true

    Column(Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box {
                Avatar(u.avatar, 40.dp)
                Box(Modifier.align(Alignment.BottomEnd).offset(x = 2.dp, y = 2.dp)) {
                    BadgeDot(u.verified, 15.dp)
                }
            }
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(u.displayName, color = XosWhite, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                Text("${u.handle} \u00B7 ${post.time}", color = XosMuted, style = MaterialTheme.typography.bodyMedium)
            }
            Icon(Icons.Filled.MoreVert, contentDescription = null, tint = XosMuted)
        }

        Box(
            Modifier
                .fillMaxWidth()
                .aspectRatio(3f / 4f)
                .padding(horizontal = 12.dp)
                .clip(RoundedCornerShape(18.dp))
        ) {
            AsyncImage(
                model = post.files.firstOrNull(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize().background(XosCard)
            )
            if (post.type == "video") {
                Icon(
                    if (mutedFlag) Icons.Outlined.VolumeOff else Icons.Outlined.VolumeUp,
                    contentDescription = null,
                    tint = XosWhite,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(12.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = .55f))
                        .clickable { state.muted[index] = !mutedFlag }
                        .padding(8.dp)
                )
            }
        }

        if (post.caption.isNotBlank()) {
            Text(post.caption, color = XosWhite, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp))
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { state.liked[index] = !liked }
            ) {
                Icon(
                    if (liked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                    contentDescription = null,
                    tint = if (liked) Color(0xFFFF3B5C) else XosWhite
                )
                Spacer(Modifier.width(6.dp))
                Text("${post.likes + if (liked) 1 else 0}", color = if (liked) Color(0xFFFF3B5C) else XosWhite, style = MaterialTheme.typography.bodyMedium)
            }
            Spacer(Modifier.weight(1f))
            Icon(Icons.Outlined.IosShare, contentDescription = null, tint = XosWhite)
            Spacer(Modifier.width(20.dp))
            Icon(
                if (savedFlag) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                contentDescription = null,
                tint = if (savedFlag) XosAccent else XosWhite,
                modifier = Modifier.clickable { state.saved[index] = !savedFlag }
            )
        }
        HorizontalDivider(color = XosBorder, thickness = 1.dp)
    }
}
