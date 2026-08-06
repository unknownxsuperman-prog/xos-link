package com.x0s.link.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.x0s.link.data.ChatMsg
import com.x0s.link.data.MsgConversation
import com.x0s.link.ui.AppState
import com.x0s.link.ui.Overlay
import com.x0s.link.ui.components.Avatar
import com.x0s.link.ui.theme.*

@Composable
fun MessagesOverlay(state: AppState) {
    AnimatedVisibility(
        visible = state.overlay == Overlay.MESSAGES,
        enter = slideInVertically(tween(320)) { it },
        exit = slideOutVertically(tween(280)) { it }
    ) {
        Box(Modifier.fillMaxSize().background(XosBackground)) {
            Column(Modifier.fillMaxSize()) {
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
                    Spacer(Modifier.width(14.dp))
                    Text("Messages", color = XosWhite, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
                }

                LazyColumn(Modifier.fillMaxSize()) {
                    items(state.conversations, key = { it.id }) { conv ->
                        ConversationRow(conv) {
                            conv.unread = false
                            state.activeConversation = conv
                        }
                        HorizontalDivider(color = XosBorder.copy(alpha = .5f))
                    }
                }
            }

            AnimatedVisibility(
                visible = state.activeConversation != null,
                enter = slideInHorizontally(tween(300)) { it },
                exit = slideOutHorizontally(tween(260)) { it }
            ) {
                state.activeConversation?.let { conv -> ChatDetail(state, conv) }
            }
        }
    }
}

@Composable
private fun ConversationRow(conv: MsgConversation, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clickable { onClick() }.padding(20.dp, 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box {
            Avatar(conv.avatar, 50.dp, CircleShape)
            if (conv.online) {
                Box(
                    Modifier
                        .align(Alignment.BottomEnd)
                        .size(11.dp)
                        .clip(CircleShape)
                        .background(XosWhite)
                )
            }
        }
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(conv.name, color = XosWhite, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyLarge)
            Text(
                conv.preview,
                color = if (conv.unread) XosWhite.copy(alpha = .7f) else XosMuted,
                fontWeight = if (conv.unread) FontWeight.Medium else FontWeight.Normal,
                style = MaterialTheme.typography.bodyMedium
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(conv.time, color = XosMuted, style = MaterialTheme.typography.labelSmall)
            if (conv.unread) {
                Spacer(Modifier.height(6.dp))
                Box(Modifier.size(8.dp).clip(CircleShape).background(XosWhite))
            }
        }
    }
}

@Composable
private fun ChatDetail(state: AppState, conv: MsgConversation) {
    Column(Modifier.fillMaxSize().background(XosBackground)) {
        Row(
            Modifier.fillMaxWidth().padding(16.dp, 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(XosCard)
                    .clickable { state.activeConversation = null }
                    .padding(9.dp)
            ) { Icon(Icons.Filled.ArrowBack, contentDescription = null, tint = XosWhite, modifier = Modifier.size(18.dp)) }
            Spacer(Modifier.width(10.dp))
            Avatar(conv.avatar, 36.dp, CircleShape)
            Spacer(Modifier.width(10.dp))
            Column {
                Text(conv.name, color = XosWhite, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                Text(if (conv.online) "\u25CF Active now" else conv.handle, color = XosMuted, style = MaterialTheme.typography.labelSmall)
            }
        }

        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(conv.messages) { msg -> ChatBubbleRow(msg) }
        }

        Row(
            Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(XosCard)
                .padding(horizontal = 14.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(Modifier.weight(1f)) {
                if (state.chatInput.isEmpty()) {
                    Text("Message\u2026", color = XosMuted, style = MaterialTheme.typography.bodyMedium)
                }
                BasicTextField(
                    value = state.chatInput,
                    onValueChange = { state.chatInput = it },
                    textStyle = MaterialTheme.typography.bodyMedium.copy(color = XosWhite),
                    cursorBrush = androidx.compose.ui.graphics.SolidColor(XosAccent)
                )
            }
            if (state.chatInput.isNotBlank()) {
                Box(
                    Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(XosWhite)
                        .clickable {
                            conv.messages.add(ChatMsg(state.chatInput.trim(), out = true, time = "now"))
                            conv.preview = state.chatInput.trim()
                            state.chatInput = ""
                        },
                    contentAlignment = Alignment.Center
                ) { Icon(Icons.Filled.ArrowUpward, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp)) }
            }
        }
    }
}

@Composable
private fun ChatBubbleRow(msg: ChatMsg) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = if (msg.out) Arrangement.End else Arrangement.Start) {
        Box(
            Modifier
                .clip(
                    RoundedCornerShape(
                        topStart = 18.dp, topEnd = 18.dp,
                        bottomStart = if (msg.out) 18.dp else 4.dp,
                        bottomEnd = if (msg.out) 4.dp else 18.dp
                    )
                )
                .background(if (msg.out) XosWhite else XosCard)
                .padding(horizontal = 14.dp, vertical = 10.dp)
                .widthIn(max = 260.dp)
        ) {
            Text(
                msg.text,
                color = if (msg.out) Color.Black else XosWhite.copy(alpha = .85f),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
