package com.x0s.link.ui.colleges

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.x0s.link.data.model.XosCollege
import com.x0s.link.data.repository.ProfileRepository
import com.x0s.link.ui.components.AccentPill
import com.x0s.link.ui.components.CircleAvatar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollegeSearchScreen(repo: ProfileRepository, onBack: () -> Unit, onSelect: (XosCollege) -> Unit) {
    var query by remember { mutableStateOf("") }
    var results by remember { mutableStateOf(listOf<XosCollege>()) }
    LaunchedEffect(query) {
        repo.ensureLoaded()
        results = if (query.isBlank()) emptyList() else repo.searchColleges(query)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, null) } },
                title = {
                    OutlinedTextField(
                        value = query,
                        onValueChange = { query = it },
                        placeholder = { Text("Search college name…", fontSize = 13.sp) },
                        leadingIcon = { Icon(Icons.Filled.Search, null, modifier = Modifier.size(16.dp)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            )
        }
    ) { padding ->
        if (results.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text(
                    if (query.isBlank()) "Type to search · #college to see all" else "No colleges found",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 13.sp
                )
            }
            return@Scaffold
        }
        LazyColumn(Modifier.fillMaxSize().padding(padding)) {
            items(results) { c ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clickable { onSelect(c) }
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircleAvatar(c.pfp, 42.dp)
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(c.name.substringBefore(","), fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                        Text(c.id, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Icon(Icons.Filled.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollegeProfileScreen(college: XosCollege?, onBack: () -> Unit, onFollowCollege: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, null) } },
                title = { Text("College") }
            )
        }
    ) { padding ->
        if (college == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("College not found", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            return@Scaffold
        }
        Column(Modifier.fillMaxSize().padding(padding).padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(28.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(22.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircleAvatar(college.pfp, 60.dp)
                    Spacer(Modifier.width(14.dp))
                    Column {
                        Text(college.name.substringBefore(","), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Text(
                            college.name.substringAfter(",", "").ifBlank { "Engineering College" },
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(6.dp))
                        Row {
                            if (college.pill1.isNotBlank()) AccentPill(college.pill1)
                            Spacer(Modifier.width(6.dp))
                            if (college.pill2.isNotBlank()) AccentPill(college.pill2)
                        }
                    }
                }
                Spacer(Modifier.height(20.dp))
                Divider(color = MaterialTheme.colorScheme.outline)
                Spacer(Modifier.height(18.dp))
                Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🏛️", fontSize = 22.sp)
                    Spacer(Modifier.height(8.dp))
                    Text("College details coming soon", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Posts, events, and the full college profile will be available here shortly.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
                Spacer(Modifier.height(20.dp))
                Divider(color = MaterialTheme.colorScheme.outline)
                Spacer(Modifier.height(18.dp))
                Button(onClick = onFollowCollege, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(30.dp)) {
                    Text("Follow College")
                }
            }
        }
    }
}
