package com.x0s.link.ui.search

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.x0s.link.data.model.XosCollege
import com.x0s.link.data.model.XosProfile
import com.x0s.link.ui.components.AccentPill
import com.x0s.link.ui.components.CircleAvatar
import com.x0s.link.ui.components.NameWithBadge
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DishaSearchScreen(
    vm: DishaSearchViewModel,
    myFirstName: String,
    onOpenProfile: (String) -> Unit,
    onOpenCollege: (String) -> Unit
) {
    var query by remember { mutableStateOf("") }
    val messages by vm.messages.collectAsState()
    val suggestions by vm.suggestions.collectAsState()
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) listState.animateScrollToItem(messages.size)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Disha")
                    }
                }
            )
        },
        bottomBar = {
            Column {
                if (query.isNotBlank() && suggestions.isNotEmpty()) {
                    LazyColumn(Modifier.heightIn(max = 220.dp).background(MaterialTheme.colorScheme.surface)) {
                        items(suggestions) { s ->
                            SuggestionRow(s, onClick = {
                                query = ""
                                when (s) {
                                    is XosProfile -> vm.search("@${s.displayName}")
                                    is XosCollege -> vm.search("#${s.name}")
                                }
                            })
                        }
                    }
                }
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = query,
                        onValueChange = { query = it; vm.onInputChanged(it) },
                        placeholder = { Text("Search name, @id, #college…", fontSize = 13.sp) },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = androidx.compose.ui.graphics.Color.Transparent,
                            focusedBorderColor = androidx.compose.ui.graphics.Color.Transparent
                        ),
                        singleLine = true
                    )
                    IconButton(onClick = {
                        val q = query
                        query = ""
                        scope.launch { vm.search(q) }
                    }) {
                        Icon(Icons.Filled.Send, contentDescription = "Send", tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    ) { padding ->
        if (messages.isEmpty()) {
            Column(
                Modifier.fillMaxSize().padding(padding).padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(Icons.Filled.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(48.dp))
                Spacer(Modifier.height(14.dp))
                Text("Hey, $myFirstName 👋", fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(6.dp))
                Text("I am Disha, how can I help you?", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Searching someone or something?", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            return@Scaffold
        }
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(messages) { msg ->
                when (msg) {
                    is DishaMessage.UserBubble -> UserBubble(msg.text)
                    is DishaMessage.ProfileResult -> ProfileResultCard(msg.profile) { onOpenProfile(msg.profile.userid) }
                    is DishaMessage.CollegeResult -> CollegeResultCard(msg.college) { onOpenCollege(msg.college.id) }
                    is DishaMessage.NotFound -> Text(
                        "No results for \"${msg.query}\". Try a different name or @id.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun UserBubble(text: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
        Box(
            Modifier
                .clip(RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomStart = 18.dp, bottomEnd = 4.dp))
                .background(MaterialTheme.colorScheme.primary)
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            Text(text, color = androidx.compose.ui.graphics.Color.White, fontSize = 13.sp)
        }
    }
}

@Composable
private fun ProfileResultCard(profile: XosProfile, onClick: () -> Unit) {
    Column(
        Modifier
            .fillMaxWidth(0.86f)
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            CircleAvatar(profile.avatar, 44.dp)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                NameWithBadge(profile.displayName, profile.verified)
                Text(profile.handle, fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                Text(
                    "${profile.followers.size} followers · ${profile.following.size} following",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun CollegeResultCard(college: XosCollege, onClick: () -> Unit) {
    Column(
        Modifier
            .fillMaxWidth(0.86f)
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            CircleAvatar(college.pfp, 44.dp)
            Spacer(Modifier.width(12.dp))
            Column {
                Text(college.name.substringBefore(","), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Text(college.name.substringAfter(",", ""), fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Spacer(Modifier.height(8.dp))
        Row {
            if (college.pill1.isNotBlank()) AccentPill(college.pill1)
            Spacer(Modifier.width(6.dp))
            if (college.pill2.isNotBlank()) AccentPill(college.pill2)
        }
    }
}

@Composable
private fun SuggestionRow(item: Any, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        when (item) {
            is XosProfile -> {
                CircleAvatar(item.avatar, 30.dp)
                Spacer(Modifier.width(10.dp))
                Column {
                    Text(item.displayName, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    Text(item.handle, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            is XosCollege -> {
                CircleAvatar(item.pfp, 30.dp)
                Spacer(Modifier.width(10.dp))
                Column {
                    Text(item.name.substringBefore(","), fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    Text(item.pill1, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}
