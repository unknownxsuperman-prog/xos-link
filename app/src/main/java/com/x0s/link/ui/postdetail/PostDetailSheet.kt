package com.x0s.link.ui.postdetail

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.outlined.Comment
import androidx.compose.material.icons.outlined.FavoriteBorder
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
fun PostDetailSheet(
    profile: XosProfile,
    post: XosPost,
    liked: Boolean,
    onToggleLike: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        val pagerState = rememberPagerState(pageCount = { post.files.size.coerceAtLeast(1) })
        Box(Modifier.fillMaxWidth().aspectRatio(3f / 4f).background(Color.Black)) {
            HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
                AsyncImage(
                    model = post.files.getOrNull(page),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
            IconButton(onClick = onDismiss, modifier = Modifier.align(Alignment.TopStart).padding(8.dp)) {
                Box(Modifier.clip(CircleShape).background(Color.Black.copy(alpha = 0.5f))) {
                    Icon(Icons.Filled.Close, contentDescription = "Close", tint = Color.White, modifier = Modifier.padding(6.dp))
                }
            }
            if (post.files.size > 1) {
                Row(
                    Modifier.align(Alignment.BottomCenter).padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    repeat(post.files.size) { i ->
                        Box(
                            Modifier
                                .size(if (i == pagerState.currentPage) 14.dp else 5.dp, 5.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(if (i == pagerState.currentPage) Color.White else Color.White.copy(alpha = 0.35f))
                        )
                    }
                }
            }
        }
        Column(Modifier.fillMaxWidth().padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                SquareAvatar(profile.avatar, 34.dp)
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    NameWithBadge(profile.displayName, profile.verified)
                    Text(profile.handle, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Text(post.time, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (post.caption.isNotBlank()) {
                Spacer(Modifier.height(10.dp))
                Text(post.caption, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { onToggleLike() }) {
                    Icon(
                        if (liked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "Like",
                        tint = if (liked) LikeRed else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.width(6.dp))
                    Text("${post.likes + if (liked) 1 else 0}", fontSize = 12.sp)
                }
                Spacer(Modifier.width(20.dp))
                Icon(Icons.Outlined.Comment, contentDescription = "Comments", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.width(6.dp))
                Text("${post.comments}", fontSize = 12.sp)
                Spacer(Modifier.weight(1f))
                Icon(Icons.Filled.Send, contentDescription = "Share", tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
