package com.x0s.link.ui.feed

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Comment
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.x0s.link.data.model.XosPost
import com.x0s.link.data.model.XosProfile
import com.x0s.link.ui.components.NameWithBadge
import com.x0s.link.ui.components.SquareAvatar
import com.x0s.link.ui.theme.LikeRed
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedScreen(
    vm: FeedViewModel,
    myAvatar: String?,
    onOpenDrawer: () -> Unit,
    onOpenProfile: (String) -> Unit,
    onOpenPost: (Int) -> Unit
) {
    LaunchedEffect(Unit) { vm.load() }
    val posts by vm.posts.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                navigationIcon = {
                    IconButton(onClick = onOpenDrawer) {
                        SquareAvatar(myAvatar, 30.dp)
                    }
                },
                title = { Text("x0s.link", fontWeight = FontWeight.Bold, fontSize = 19.sp) },
                actions = { IconButton(onClick = {}) { Icon(Icons.Outlined.Menu, null) } },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        if (vm.loading && posts.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            itemsIndexed(posts) { index, (profile, post) ->
                PostCard(
                    profile = profile,
                    post = post,
                    postKey = "${profile.userid}_$index",
                    vm = vm,
                    onOpenProfile = { onOpenProfile(profile.userid) },
                    onOpenPost = { onOpenPost(index) }
                )
                Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
            }
        }
    }
}

@Composable
fun PostCard(
    profile: XosProfile,
    post: XosPost,
    postKey: String,
    vm: FeedViewModel,
    onOpenProfile: () -> Unit,
    onOpenPost: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var liked by remember(postKey) { mutableStateOf(false) }
    var saved by remember(postKey) { mutableStateOf(false) }
    LaunchedEffect(postKey) { liked = vm.isLiked(postKey) }

    Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { onOpenProfile() }) {
            SquareAvatar(profile.avatar, 40.dp)
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                NameWithBadge(profile.displayName, profile.verified)
                Text(
                    "${profile.handle} · ${post.time}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(Icons.Filled.MoreVert, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Spacer(Modifier.height(10.dp))
        Box(
            Modifier
                .fillMaxWidth()
                .aspectRatio(3f / 4f)
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surface)
                .clickable { onOpenPost() }
        ) {
            AsyncImage(
                model = post.files.firstOrNull(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            if (post.type == "video") {
                Box(
                    Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color.Black.copy(alpha = 0.55f))
                        .padding(horizontal = 7.dp, vertical = 3.dp)
                ) {
                    Text("VIDEO", fontSize = 9.sp, color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
        if (post.caption.isNotBlank()) {
            Spacer(Modifier.height(10.dp))
            Text(post.caption, fontSize = 13.sp, color = MaterialTheme.colorScheme.onBackground)
        }
        Spacer(Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable {
                    scope.launch {
                        liked = vm.toggleLike(postKey)
                    }
                }
            ) {
                Icon(
                    if (liked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                    contentDescription = "Like",
                    tint = if (liked) LikeRed else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(5.dp))
                Text("${post.likes + if (liked) 1 else 0}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(Modifier.weight(1f))
            Icon(Icons.Filled.Send, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(16.dp))
            Icon(
                if (saved) Icons.Filled.Favorite else Icons.Outlined.BookmarkBorder,
                contentDescription = "Save",
                tint = if (saved) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp).clickable {
                    scope.launch { saved = vm.toggleSave(postKey) }
                }
            )
        }
    }
}
