package com.x0s.link.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items as lazyColumnItems
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.x0s.link.data.XosCollege
import com.x0s.link.data.XosProfile
import com.x0s.link.ui.AppState
import com.x0s.link.ui.Overlay
import com.x0s.link.ui.components.Avatar
import com.x0s.link.ui.components.BadgeDot
import com.x0s.link.ui.components.PillButton
import com.x0s.link.ui.theme.*

@Composable
fun SearchOverlay(state: AppState) {
    AnimatedVisibility(
        visible = state.overlay == Overlay.SEARCH,
        enter = slideInVertically(tween(320)) { it },
        exit = slideOutVertically(tween(280)) { it }
    ) {
        Column(Modifier.fillMaxSize().background(XosBackground)) {
            // top search bar
            Row(
                Modifier.fillMaxWidth().padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(XosCard)
                        .clickable { state.overlay = Overlay.NONE; state.searchQuery = "" }
                        .padding(9.dp)
                ) { Icon(Icons.Filled.ArrowBack, contentDescription = null, tint = XosWhite, modifier = Modifier.size(18.dp)) }
                Spacer(Modifier.width(10.dp))
                Row(
                    Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .background(XosGlass)
                        .padding(horizontal = 14.dp, vertical = 11.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.Search, contentDescription = null, tint = XosMuted, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    Box(Modifier.weight(1f)) {
                        if (state.searchQuery.isEmpty()) {
                            Text("Search people, colleges\u2026", color = XosMuted, style = MaterialTheme.typography.bodyLarge)
                        }
                        BasicTextField(
                            value = state.searchQuery,
                            onValueChange = { state.searchQuery = it },
                            textStyle = MaterialTheme.typography.bodyLarge.copy(color = XosWhite),
                            singleLine = true,
                            cursorBrush = androidx.compose.ui.graphics.SolidColor(XosAccent)
                        )
                    }
                }
            }

            Text(
                "EXPLORE",
                color = XosMuted,
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(Modifier.height(10.dp))

            Row(Modifier.padding(horizontal = 16.dp)) {
                listOf("People", "Colleges").forEachIndexed { i, label ->
                    Box(
                        Modifier
                            .padding(end = 10.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (state.searchTab == i) XosWhite else XosCard)
                            .clickable { state.searchTab = i }
                            .padding(horizontal = 18.dp, vertical = 8.dp)
                    ) {
                        Text(label, color = if (state.searchTab == i) Color.Black else XosWhite, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
            Spacer(Modifier.height(14.dp))

            if (state.searchTab == 0) {
                val people = state.profiles.values.filter {
                    state.searchQuery.isBlank() ||
                        it.displayName.contains(state.searchQuery, true) ||
                        it.handle.contains(state.searchQuery, true)
                }
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(people) { p -> PersonCard(p) }
                }
            } else {
                val colleges = state.colleges.filter {
                    state.searchQuery.isBlank() || it.name.replace(" ", "").contains(state.searchQuery.replace(" ", ""), true)
                }
                LazyColumnColleges(colleges)
            }
        }
    }
}

@Composable
private fun LazyColumnColleges(colleges: List<XosCollege>) {
    androidx.compose.foundation.lazy.LazyColumn(Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        lazyColumnItems(colleges) { c -> CollegeRow(c) }
    }
}

@Composable
private fun PersonCard(p: XosProfile) {
    Column(Modifier.clip(RoundedCornerShape(18.dp)).background(XosCard)) {
        AsyncImage(
            model = p.avatar,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxWidth().aspectRatio(1f).background(XosBorder)
        )
        Column(Modifier.padding(11.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(p.displayName, color = XosWhite, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                Spacer(Modifier.width(4.dp))
                BadgeDot(p.verified, 13.dp)
            }
            Text(p.handle, color = XosMuted, style = MaterialTheme.typography.bodyMedium)
            Text("${p.followers.size} followers", color = XosMuted, style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(8.dp))
            PillButton("Follow", onClick = {}, modifier = Modifier.fillMaxWidth())
        }
    }
}

@Composable
private fun CollegeRow(c: XosCollege) {
    val parts = c.name.split(",")
    Row(
        Modifier.fillMaxWidth().padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Avatar(c.pfp, 44.dp, androidx.compose.foundation.shape.CircleShape)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(parts.first().trim(), color = XosWhite, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyLarge)
            Text(parts.drop(1).joinToString(",").trim().ifBlank { "Engineering College" }, color = XosMuted, style = MaterialTheme.typography.bodyMedium)
            Row(Modifier.padding(top = 4.dp)) {
                listOf(c.pill1, c.pill2).filter { it.isNotBlank() }.forEach {
                    Box(
                        Modifier
                            .padding(end = 5.dp)
                            .clip(RoundedCornerShape(99.dp))
                            .background(XosAccent.copy(alpha = .12f))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) { Text(it, color = XosAccent, style = MaterialTheme.typography.labelSmall) }
                }
            }
        }
    }
}
