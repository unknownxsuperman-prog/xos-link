package com.x0s.link.ui.profile

import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.x0s.link.data.model.XosLink
import com.x0s.link.data.model.XosProfile
import com.x0s.link.ui.components.SquareAvatar
import com.x0s.link.ui.components.VerifiedBadge

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    userId: String,
    vm: ProfileViewModel,
    meUserId: String,
    darkTheme: Boolean,
    onBack: (() -> Unit)?,
    onEditProfile: () -> Unit,
    onOpenFavourites: () -> Unit,
    onOpenCollegeSearch: () -> Unit,
    onOpenCollegeProfile: (String) -> Unit,
    onOpenPost: (Int) -> Unit,
    onOpenProfile: (String) -> Unit,
    onToggleDarkTheme: (Boolean) -> Unit
) {
    LaunchedEffect(userId) { vm.load(userId) }
    val profile by vm.profile.collectAsState()
    val linkedCollege by vm.linkedCollege.collectAsState()
    val isFollowing by vm.isFollowing.collectAsState()
    var menuOpen by remember { mutableStateOf(false) }
    var avatarViewerOpen by remember { mutableStateOf(false) }
    var followSheet by remember { mutableStateOf<String?>(null) } // "followers" | "following" | null
    val context = LocalContext.current

    val p = profile ?: run {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        return
    }
    val isMe = p.userid == meUserId

    // ── bio music preview player ──
    var isPlayingBioTrack by remember { mutableStateOf(false) }
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }
    DisposableEffect(userId) {
        onDispose { mediaPlayer?.release(); mediaPlayer = null; isPlayingBioTrack = false }
    }
    fun toggleBioTrack() {
        val stream = p.audioTrack?.streamUrl
        if (stream.isNullOrBlank()) return
        if (isPlayingBioTrack) {
            mediaPlayer?.pause(); isPlayingBioTrack = false
            return
        }
        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer().apply {
                setDataSource(stream)
                setOnPreparedListener { start(); isPlayingBioTrack = true }
                setOnCompletionListener { isPlayingBioTrack = false }
                isLooping = true
                prepareAsync()
            }
        } else {
            mediaPlayer?.start(); isPlayingBioTrack = true
        }
    }

    Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            // ── Wallpaper / banner ──
            Box(Modifier.fillMaxWidth().heightIn(min = 260.dp, max = 340.dp).aspectRatio(1.4f)) {
                AsyncImage(
                    model = vm.banner(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    Modifier.fillMaxSize().background(
                        Brush.verticalGradient(
                            0f to Color.Transparent,
                            0.5f to Color.Transparent,
                            0.78f to MaterialTheme.colorScheme.background.copy(alpha = 0.55f),
                            1f to MaterialTheme.colorScheme.background
                        )
                    )
                )
                Row(
                    Modifier.fillMaxWidth().statusBarsPadding().padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    if (onBack != null) RoundIconBtn(Icons.Filled.ArrowBack) { onBack() }
                    else Spacer(Modifier.size(36.dp))

                    Box {
                        RoundIconBtn(Icons.Filled.MoreVert) { menuOpen = true }
                        DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                            DropdownMenuItem(
                                text = { Text("Share") },
                                leadingIcon = { Icon(Icons.Filled.Share, null) },
                                onClick = {
                                    menuOpen = false
                                    val send = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(Intent.EXTRA_TEXT, "${vm.displayName()} | x0s.link")
                                    }
                                    context.startActivity(Intent.createChooser(send, null))
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("My Favourites") },
                                leadingIcon = { Icon(Icons.Filled.Favorite, null) },
                                onClick = { menuOpen = false; onOpenFavourites() }
                            )
                            if (isMe) {
                                DropdownMenuItem(
                                    text = { Text("Edit Profile") },
                                    leadingIcon = { Icon(Icons.Filled.Edit, null) },
                                    onClick = { menuOpen = false; onEditProfile() }
                                )
                            }
                            DropdownMenuItem(
                                text = { Text("Appearance") },
                                leadingIcon = { Icon(Icons.Filled.Brightness6, null) },
                                onClick = { menuOpen = false; onToggleDarkTheme(!darkTheme) }
                            )
                            DropdownMenuItem(
                                text = { Text("Link College") },
                                leadingIcon = { Icon(Icons.Outlined.AccountBalance, null) },
                                onClick = { menuOpen = false; onOpenCollegeSearch() }
                            )
                        }
                    }
                }
            }

            // ── Floating profile card, overlapping the banner ──
            Column(
                Modifier
                    .padding(horizontal = 16.dp)
                    .offset(y = (-92).dp)
                    .clip(RoundedCornerShape(28.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(20.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box {
                        SquareAvatar(
                            vm.avatar(),
                            76.dp,
                            modifier = Modifier.clickable { avatarViewerOpen = true }
                        )
                        if (p.verified != "none") {
                            Box(
                                Modifier
                                    .align(Alignment.BottomEnd)
                                    .offset(x = 3.dp, y = 3.dp)
                                    .background(MaterialTheme.colorScheme.surface, CircleShape)
                                    .padding(1.dp)
                            ) {
                                VerifiedBadge(p.verified, size = 15.dp)
                            }
                        }
                    }
                    Spacer(Modifier.width(14.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            vm.displayName(),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                        Text(p.handle, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    if (isMe) {
                        OutlinedButton(onClick = onEditProfile, shape = RoundedCornerShape(99.dp)) {
                            Icon(Icons.Filled.Edit, null, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Edit", fontSize = 12.sp)
                        }
                    } else {
                        Button(
                            onClick = { vm.toggleFollow() },
                            shape = RoundedCornerShape(99.dp),
                            colors = if (isFollowing) ButtonDefaults.outlinedButtonColors() else ButtonDefaults.buttonColors()
                        ) { Text(if (isFollowing) "Following" else "Follow", fontSize = 12.sp) }
                    }
                }
                Spacer(Modifier.height(16.dp))
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(vertical = 14.dp)
                ) {
                    val followerCount = p.followers.size + if (isFollowing && !isMe) 1 else 0
                    StatCell("Followers", followerCount.toString(), Modifier.weight(1f).clickable { followSheet = "followers" })
                    VDivider()
                    StatCell("Following", p.following.size.toString(), Modifier.weight(1f).clickable { followSheet = "following" })
                    VDivider()
                    StatCell("Posts", p.posts.size.toString(), Modifier.weight(1f))
                }
                Spacer(Modifier.height(12.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable {
                        val id = linkedCollege?.id
                        if (id != null) onOpenCollegeProfile(id) else onOpenCollegeSearch()
                    }
                ) {
                    Icon(
                        Icons.Outlined.AccountBalance,
                        contentDescription = null,
                        tint = if (linkedCollege != null) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        linkedCollege?.name?.substringBefore(",") ?: "+ Link College",
                        fontSize = 12.sp,
                        maxLines = 1,
                        color = if (linkedCollege != null) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.basicMarquee(iterations = Int.MAX_VALUE)
                    )
                }
            }

            Column(Modifier.padding(horizontal = 20.dp).offset(y = (-56).dp)) {
                if (vm.bio().isNotBlank()) {
                    Text(vm.bio(), fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 20.sp)
                    Spacer(Modifier.height(14.dp))
                }

                // ── Bio music row ──
                if (p.audioTrack != null && (p.audioTrack.title.isNotBlank())) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 16.dp)) {
                        Icon(
                            imageVector = if (isPlayingBioTrack) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                            contentDescription = "Play",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp).clickable { toggleBioTrack() }
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "${p.audioTrack.title} • ${p.audioTrack.artist}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1
                        )
                        Spacer(Modifier.width(8.dp))
                        EqBars(playing = isPlayingBioTrack)
                    }
                }

                // ── Social links row ──
                if (p.links.isNotEmpty()) {
                    Row(
                        Modifier.horizontalScroll(rememberScrollState()).padding(bottom = 18.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        p.links.forEach { link -> LinkPill(link, context) }
                    }
                }

                Text(
                    "POSTS",
                    fontSize = 10.sp,
                    letterSpacing = 2.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(10.dp))
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier.fillMaxWidth().heightIn(max = 3000.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    userScrollEnabled = false
                ) {
                    items(p.posts.size) { i ->
                        val post = p.posts[i]
                        Box(
                            Modifier
                                .aspectRatio(3f / 4f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .clickable { onOpenPost(i) }
                        ) {
                            AsyncImage(
                                model = post.files.firstOrNull(),
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                            if (post.type == "video") {
                                Icon(
                                    Icons.Filled.PlayCircleFilled,
                                    contentDescription = null,
                                    tint = Color.White.copy(alpha = 0.85f),
                                    modifier = Modifier.align(Alignment.TopEnd).padding(6.dp).size(16.dp)
                                )
                            }
                        }
                    }
                }
                Spacer(Modifier.height(80.dp))
            }
        }
    }

    // ── Full-screen avatar viewer ──
    if (avatarViewerOpen) {
        Dialog(onDismissRequest = { avatarViewerOpen = false }, properties = DialogProperties(usePlatformDefaultWidth = false)) {
            Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.92f)), contentAlignment = Alignment.Center) {
                AsyncImage(
                    model = vm.avatar(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth(0.86f)
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(28.dp))
                )
                RoundIconBtn(
                    Icons.Filled.ArrowBack,
                    modifier = Modifier.align(Alignment.TopStart).statusBarsPadding().padding(16.dp)
                ) { avatarViewerOpen = false }
            }
        }
    }

    // ── Followers / Following bottom sheet ──
    if (followSheet != null) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        var tab by remember(followSheet) { mutableStateOf(followSheet) }
        ModalBottomSheet(onDismissRequest = { followSheet = null }, sheetState = sheetState) {
            Column(Modifier.fillMaxWidth().heightIn(min = 400.dp)) {
                TabRow(selectedTabIndex = if (tab == "followers") 0 else 1) {
                    Tab(selected = tab == "followers", onClick = { tab = "followers" }, text = { Text("Followers") })
                    Tab(selected = tab == "following", onClick = { tab = "following" }, text = { Text("Following") })
                }
                val list = if (tab == "followers") vm.followers() else vm.following()
                if (list.isEmpty()) {
                    Box(Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                        Text("Nothing here yet", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    LazyColumn(Modifier.fillMaxWidth()) {
                        items(list) { u: XosProfile ->
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .clickable { followSheet = null; onOpenProfile(u.userid) }
                                    .padding(horizontal = 16.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                SquareAvatar(u.avatar, 46.dp)
                                Spacer(Modifier.width(12.dp))
                                Column(Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(u.displayName, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
                                        if (u.verified != "none") {
                                            Spacer(Modifier.width(4.dp))
                                            VerifiedBadge(u.verified, size = 12.dp)
                                        }
                                    }
                                    Text(u.handle, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
                Spacer(Modifier.height(20.dp))
            }
        }
    }
}

@Composable
private fun LinkPill(link: XosLink, context: android.content.Context) {
    val (icon, label) = remember(link.url, link.label) {
        val l = link.url.lowercase()
        when {
            "instagram" in l -> Icons.Filled.PhotoCamera to (link.label.ifBlank { "Instagram" })
            "github" in l -> Icons.Filled.Code to (link.label.ifBlank { "GitHub" })
            "x.com" in l || "twitter" in l -> Icons.Filled.Tag to (link.label.ifBlank { "X" })
            "linkedin" in l -> Icons.Filled.Work to (link.label.ifBlank { "LinkedIn" })
            "discord" in l -> Icons.Filled.Chat to (link.label.ifBlank { "Discord" })
            "spotify" in l -> Icons.Filled.MusicNote to (link.label.ifBlank { "Spotify" })
            "youtube" in l -> Icons.Filled.PlayArrow to (link.label.ifBlank { "YouTube" })
            else -> Icons.Outlined.Link to (link.label.ifBlank { "Link" })
        }
    }
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable {
                if (link.url.isNotBlank()) {
                    val url = if (link.url.startsWith("http")) link.url else "https://${link.url}"
                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                }
            }
            .padding(horizontal = 13.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(13.dp))
        Spacer(Modifier.width(6.dp))
        Text(label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

/** Small animated equalizer, mirrors .bio-music-eq's 4 pulsing bars (only animates while playing). */
@Composable
private fun EqBars(playing: Boolean) {
    val transition = rememberInfiniteTransition(label = "eq")
    val bars = remember { listOf(0, 150, 300, 100) } // stagger delays in ms
    Row(horizontalArrangement = Arrangement.spacedBy(2.5.dp), verticalAlignment = Alignment.Bottom) {
        bars.forEach { delay ->
            val h by transition.animateFloat(
                initialValue = 2f,
                targetValue = if (playing) 10f else 2f,
                animationSpec = infiniteRepeatable(
                    animation = tween(700, delayMillis = delay, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "barHeight"
            )
            Box(
                Modifier
                    .width(2.5.dp)
                    .height(h.dp)
                    .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = if (playing) 0.6f else 0.2f), RoundedCornerShape(2.dp))
            )
        }
    }
}

@Composable
private fun RoundIconBtn(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier
            .size(36.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(Color.Black.copy(alpha = 0.38f))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
    }
}

@Composable
private fun StatCell(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontSize = 17.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(4.dp))
        Text(label.uppercase(), fontSize = 9.sp, letterSpacing = 1.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun VDivider() {
    Box(
        Modifier
            .width(1.dp)
            .height(28.dp)
            .background(MaterialTheme.colorScheme.outline)
    )
}
