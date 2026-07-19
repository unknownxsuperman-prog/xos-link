package com.x0s.link.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.x0s.link.ui.components.NameWithBadge
import com.x0s.link.ui.components.SquareAvatar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    userId: String,
    vm: ProfileViewModel,
    meUserId: String,
    onBack: (() -> Unit)?,
    onEditProfile: () -> Unit,
    onOpenFavourites: () -> Unit,
    onOpenCollegeSearch: () -> Unit,
    onOpenCollegeProfile: (String) -> Unit,
    onOpenPost: (Int) -> Unit
) {
    LaunchedEffect(userId) { vm.load(userId) }
    val profile by vm.profile.collectAsState()
    val linkedCollege by vm.linkedCollege.collectAsState()
    val isFollowing by vm.isFollowing.collectAsState()
    var menuOpen by remember { mutableStateOf(false) }

    val p = profile ?: run {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        return
    }
    val isMe = p.userid == meUserId

    Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            // Banner
            Box(Modifier.fillMaxWidth().height(220.dp)) {
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
                            0.78f to MaterialTheme.colorScheme.background.copy(alpha = 0.6f),
                            1f to MaterialTheme.colorScheme.background
                        )
                    )
                )
                Row(
                    Modifier.fillMaxWidth().padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    if (onBack != null) {
                        RoundIconBtn(Icons.Filled.ArrowBack) { onBack() }
                    } else Spacer(Modifier.size(36.dp))
                    Box {
                        RoundIconBtn(Icons.Filled.MoreVert) { menuOpen = true }
                        DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                            if (isMe) {
                                DropdownMenuItem(text = { Text("Edit Profile") }, onClick = { menuOpen = false; onEditProfile() })
                            }
                            DropdownMenuItem(text = { Text("My Favourites") }, onClick = { menuOpen = false; onOpenFavourites() })
                            DropdownMenuItem(text = { Text("Link College") }, onClick = { menuOpen = false; onOpenCollegeSearch() })
                        }
                    }
                }
            }

            // Floating card
            Column(
                Modifier
                    .padding(horizontal = 16.dp)
                    .offset(y = (-64).dp)
                    .clip(RoundedCornerShape(28.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(20.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    SquareAvatar(vm.avatar(), 76.dp)
                    Spacer(Modifier.width(14.dp))
                    Column(Modifier.weight(1f)) {
                        Text(vm.displayName(), fontSize = 20.sp, fontWeight = FontWeight.Bold)
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
                    StatCell("Followers", followerCount.toString(), Modifier.weight(1f))
                    VDivider()
                    StatCell("Following", p.following.size.toString(), Modifier.weight(1f))
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
                        color = if (linkedCollege != null) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Column(Modifier.padding(horizontal = 20.dp).offset(y = (-40).dp)) {
                if (vm.bio().isNotBlank()) {
                    Text(vm.bio(), fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 20.sp)
                    Spacer(Modifier.height(16.dp))
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
                    modifier = Modifier.fillMaxWidth().heightIn(max = 2000.dp),
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
                        }
                    }
                }
                Spacer(Modifier.height(80.dp))
            }
        }
    }
}

@Composable
private fun RoundIconBtn(icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    Box(
        Modifier
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
