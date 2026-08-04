package com.x0s.link.ui.zynx

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import com.x0s.link.data.model.XosPost
import com.x0s.link.data.model.XosProfile
import com.x0s.link.data.repository.ProfileRepository
import com.x0s.link.ui.components.NameWithBadge
import com.x0s.link.ui.components.SquareAvatar
import com.x0s.link.ui.theme.LikeRed

@Composable
fun ZynxScreen(repo: ProfileRepository, onOpenProfile: (String) -> Unit) {
    var posts by remember { mutableStateOf(listOf<Pair<XosProfile, XosPost>>()) }
    LaunchedEffect(Unit) {
        repo.ensureLoaded()
        posts = repo.feedPosts()
    }
    if (posts.isEmpty()) return

    val pagerState = rememberPagerState(pageCount = { posts.size })

    VerticalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
        val (profile, post) = posts[page]
        ZynxItem(
            profile = profile,
            post = post,
            isActive = pagerState.currentPage == page,
            onOpenProfile = { onOpenProfile(profile.userid) }
        )
    }
}

@Composable
private fun ZynxItem(profile: XosProfile, post: XosPost, isActive: Boolean, onOpenProfile: () -> Unit) {
    val context = LocalContext.current
    var liked by remember { mutableStateOf(false) }
    var saved by remember { mutableStateOf(false) }
    var muted by remember { mutableStateOf(true) }
    val fileUrl = post.files.firstOrNull()
    val isVideo = post.type == "video" && !fileUrl.isNullOrBlank()

    val player = remember(fileUrl) {
        if (isVideo) ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(Uri.parse(fileUrl)))
            repeatMode = Player.REPEAT_MODE_ONE
            volume = 0f
            prepare()
        } else null
    }

    DisposableEffect(player) { onDispose { player?.release() } }
    LaunchedEffect(isActive, player) {
        if (isActive) player?.play() else player?.pause()
    }
    LaunchedEffect(muted, player) { player?.volume = if (muted) 0f else 1f }

    Box(Modifier.fillMaxSize().background(Color.Black)) {
        if (isVideo && player != null) {
            AndroidView(
                factory = {
                    PlayerView(it).apply {
                        useController = false
                        this.player = player
                    }
                },
                modifier = Modifier.fillMaxSize().clickable { muted = !muted }
            )
        } else {
            AsyncImage(
                model = fileUrl,
                contentDescription = null,
                contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }

        // gradient + info
        Column(
            Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp)
                .padding(bottom = 90.dp)
                .fillMaxWidth(0.75f)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { onOpenProfile() }) {
                SquareAvatar(profile.avatar, 34.dp)
                Spacer(Modifier.width(8.dp))
                Column {
                    NameWithBadge(profile.displayName, profile.verified)
                    Text(profile.handle, fontSize = 10.sp, color = Color.White.copy(alpha = 0.7f))
                }
            }
            Spacer(Modifier.height(6.dp))
            if (post.caption.isNotBlank()) {
                Text(post.caption, fontSize = 12.sp, color = Color.White.copy(alpha = 0.92f))
            }
        }

        Column(
            Modifier.align(Alignment.BottomEnd).padding(16.dp).padding(bottom = 90.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            ZAction(
                icon = if (liked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                tint = if (liked) LikeRed else Color.White,
                label = "${post.likes + if (liked) 1 else 0}"
            ) { liked = !liked }
            ZAction(
                icon = if (saved) Icons.Filled.Favorite else Icons.Outlined.BookmarkBorder,
                tint = if (saved) MaterialTheme.colorScheme.primary else Color.White,
                label = "Save"
            ) { saved = !saved }
            ZAction(icon = Icons.Filled.Send, tint = Color.White, label = "Share") {}
        }
    }
}

@Composable
private fun ZAction(icon: androidx.compose.ui.graphics.vector.ImageVector, tint: Color, label: String, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { onClick() }) {
        Icon(icon, contentDescription = label, tint = tint, modifier = Modifier.size(26.dp))
        Spacer(Modifier.height(2.dp))
        Text(label, fontSize = 10.sp, color = Color.White.copy(alpha = 0.85f))
    }
}
