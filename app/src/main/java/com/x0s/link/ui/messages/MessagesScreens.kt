package com.x0s.link.ui.messages

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.x0s.link.data.model.XosChatMessage
import com.x0s.link.ui.components.CircleAvatar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessagesListScreen(vm: MessagesViewModel, onOpenChat: (Int) -> Unit) {
    var query by remember { mutableStateOf("") }
    val conversations by vm.conversations.collectAsState()
    val shown = remember(query, conversations) { vm.search(query) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Messages", fontWeight = FontWeight.Bold, fontSize = 22.sp) }) }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                placeholder = { Text("Search messages", fontSize = 13.sp) },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                singleLine = true
            )
            LazyColumn(Modifier.fillMaxSize()) {
                items(shown, key = { it.id }) { conv ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clickable { onOpenChat(conv.id) }
                            .padding(horizontal = 20.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box {
                            CircleAvatar(conv.avatar, 50.dp)
                            if (conv.online) {
                                Box(
                                    Modifier
                                        .align(Alignment.BottomEnd)
                                        .size(12.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.background)
                                        .padding(2.dp)
                                        .clip(CircleShape)
                                        .background(androidx.compose.ui.graphics.Color(0xFF2DD465))
                                )
                            }
                        }
                        Spacer(Modifier.width(14.dp))
                        Column(Modifier.weight(1f)) {
                            Text(conv.name, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                            Text(
                                conv.preview,
                                fontSize = 12.sp,
                                maxLines = 1,
                                color = if (conv.unread) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = if (conv.unread) FontWeight.Medium else FontWeight.Normal
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(conv.time, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            if (conv.unread) {
                                Spacer(Modifier.height(6.dp))
                                Box(Modifier.size(8.dp).clip(CircleShape).background(MaterialTheme.colorScheme.onSurface))
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(vm: MessagesViewModel, convId: Int, onBack: () -> Unit) {
    LaunchedEffect(convId) { vm.markRead(convId) }
    val conversations by vm.conversations.collectAsState()
    val conv = conversations.find { it.id == convId } ?: run {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Conversation not found") }
        return
    }
    var input by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, null) } },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircleAvatar(conv.avatar, 32.dp)
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Text(conv.name, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                            Text(
                                if (conv.online) "● Active now" else conv.handle,
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            )
        },
        bottomBar = {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
                    .clip(androidx.compose.foundation.shape.RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = input,
                    onValueChange = { input = it },
                    placeholder = { Text("Message…", fontSize = 13.sp) },
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = androidx.compose.ui.graphics.Color.Transparent,
                        focusedBorderColor = androidx.compose.ui.graphics.Color.Transparent
                    ),
                    singleLine = true
                )
                IconButton(onClick = {
                    if (input.isNotBlank()) { vm.sendMessage(convId, input); input = "" }
                }) {
                    Icon(Icons.Filled.Send, contentDescription = "Send", tint = MaterialTheme.colorScheme.primary)
                }
            }
        }
    ) { padding ->
        val listState = androidx.compose.foundation.lazy.rememberLazyListState()
        LaunchedEffect(conv.messages.size) {
            if (conv.messages.isNotEmpty()) listState.animateScrollToItem(conv.messages.size - 1)
        }
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(conv.messages) { m -> ChatBubble(m) }
        }
    }
}

@Composable
private fun ChatBubble(m: XosChatMessage) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = if (m.out) Arrangement.End else Arrangement.Start) {
        Column(horizontalAlignment = if (m.out) Alignment.End else Alignment.Start) {
            Box(
                Modifier
                    .clip(
                        androidx.compose.foundation.shape.RoundedCornerShape(
                            topStart = 18.dp, topEnd = 18.dp,
                            bottomStart = if (m.out) 18.dp else 4.dp,
                            bottomEnd = if (m.out) 4.dp else 18.dp
                        )
                    )
                    .background(if (m.out) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.surfaceVariant)
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Text(
                    m.text,
                    fontSize = 13.sp,
                    color = if (m.out) MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(Modifier.height(3.dp))
            Text(m.time, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
        }
    }
}
