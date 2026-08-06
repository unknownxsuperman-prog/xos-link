package com.x0s.link.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.x0s.link.ui.AppState
import com.x0s.link.ui.ChatBubble
import com.x0s.link.ui.Overlay
import com.x0s.link.ui.theme.*

/** Disha AI search — mirrors #es-overlay in index.html. */
@Composable
fun EssentialOverlay(state: AppState) {
    AnimatedVisibility(
        visible = state.overlay == Overlay.ESSENTIAL,
        enter = slideInVertically(tween(320)) { it },
        exit = slideOutVertically(tween(280)) { it }
    ) {
        Column(Modifier.fillMaxSize().background(XosBackground)) {
            Row(
                Modifier.fillMaxWidth().padding(16.dp, 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(XosCard)
                        .clickable { state.overlay = Overlay.NONE }
                        .padding(9.dp)
                ) { Icon(Icons.Filled.ArrowBack, contentDescription = null, tint = XosWhite, modifier = Modifier.size(18.dp)) }
                Spacer(Modifier.width(12.dp))
                Icon(Icons.Filled.AutoAwesome, contentDescription = null, tint = XosAccent, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("Disha", color = XosWhite, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            }

            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                if (state.essentialMessages.isEmpty()) {
                    item {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(top = 30.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                Modifier
                                    .size(58.dp)
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(XosAccent.copy(alpha = .1f)),
                                contentAlignment = Alignment.Center
                            ) { Icon(Icons.Filled.AutoAwesome, contentDescription = null, tint = XosAccent) }
                            Spacer(Modifier.height(14.dp))
                            Text(
                                "Hey, ${state.activeUser?.displayName?.substringBefore(" ") ?: "Guest"} \uD83D\uDC4B",
                                color = XosWhite, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge
                            )
                            Spacer(Modifier.height(4.dp))
                            Text("I am Disha, how can I help you?", color = XosMuted, style = MaterialTheme.typography.bodyMedium)
                            Text("Searching someone or something?", color = XosMuted, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
                items(state.essentialMessages) { bubble -> EssentialBubble(bubble) }
            }

            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(XosGlass)
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Filled.Search, contentDescription = null, tint = XosMuted, modifier = Modifier.size(15.dp))
                Spacer(Modifier.width(8.dp))
                Box(Modifier.weight(1f)) {
                    if (state.essentialInput.isEmpty()) {
                        Text("Search name, @id, college\u2026", color = XosMuted, style = MaterialTheme.typography.bodyMedium)
                    }
                    BasicTextField(
                        value = state.essentialInput,
                        onValueChange = { state.essentialInput = it },
                        textStyle = MaterialTheme.typography.bodyMedium.copy(color = XosWhite),
                        singleLine = true,
                        cursorBrush = androidx.compose.ui.graphics.SolidColor(XosAccent)
                    )
                }
                if (state.essentialInput.isNotBlank()) {
                    Box(
                        Modifier
                            .size(28.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(XosAccent)
                            .clickable { runEssentialSearch(state) },
                        contentAlignment = Alignment.Center
                    ) { Icon(Icons.Filled.ArrowUpward, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp)) }
                }
            }
        }
    }
}

private fun runEssentialSearch(state: AppState) {
    val q = state.essentialInput.trim()
    if (q.isBlank()) return
    state.essentialInput = ""
    state.essentialMessages.add(ChatBubble(fromUser = true, text = q))

    val ql = q.lowercase().replace(" ", "")
    val user = state.profiles.values.find {
        it.displayName.lowercase().contains(ql) || it.handle.lowercase().contains(ql) || it.userid.lowercase() == ql
    }
    val college = state.colleges.find { it.name.lowercase().replace(" ", "").contains(ql) }

    when {
        user != null -> state.essentialMessages.add(ChatBubble(false, "${user.displayName} \u2014 ${user.handle}", isCard = true, cardSubtitle = "${user.followers.size} followers"))
        college != null -> state.essentialMessages.add(ChatBubble(false, college.name.substringBefore(","), isCard = true, cardSubtitle = college.pill1))
        else -> state.essentialMessages.add(ChatBubble(false, "No results for \"$q\". Try a different name or @id."))
    }
}

@Composable
private fun EssentialBubble(bubble: ChatBubble) {
    if (bubble.fromUser) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            Box(
                Modifier
                    .clip(RoundedCornerShape(18.dp, 18.dp, 4.dp, 18.dp))
                    .background(XosAccent)
                    .padding(horizontal = 16.dp, vertical = 10.dp)
                    .widthIn(max = 260.dp)
            ) { Text(bubble.text, color = Color.White, style = MaterialTheme.typography.bodyMedium) }
        }
    } else if (bubble.isCard) {
        Column(
            Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(XosGlass)
                .padding(16.dp)
                .fillMaxWidth(0.85f)
        ) {
            Text(bubble.text, color = XosWhite, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
            if (bubble.cardSubtitle.isNotBlank()) {
                Spacer(Modifier.height(4.dp))
                Text(bubble.cardSubtitle, color = XosMuted, style = MaterialTheme.typography.bodyMedium)
            }
        }
    } else {
        Box(
            Modifier
                .clip(RoundedCornerShape(18.dp, 18.dp, 18.dp, 4.dp))
                .background(XosCard)
                .padding(horizontal = 16.dp, vertical = 10.dp)
                .fillMaxWidth(0.85f)
        ) { Text(bubble.text, color = XosMuted, style = MaterialTheme.typography.bodyMedium) }
    }
}
