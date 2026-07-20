package com.x0s.link.ui.profile

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(vm: ProfileViewModel, onBack: () -> Unit) {
    val profile by vm.profile.collectAsState()
    var name by remember(profile) { mutableStateOf(vm.displayName()) }
    var bio by remember(profile) { mutableStateOf(vm.bio()) }
    var avatarUri by remember(profile) { mutableStateOf(vm.avatar()) }
    var bannerUri by remember(profile) { mutableStateOf(vm.banner()) }

    val avatarPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { avatarUri = it.toString() }
    }
    val bannerPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { bannerUri = it.toString() }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, null) } },
                title = { Text("Edit Profile") },
                actions = {
                    TextButton(onClick = { vm.saveEdits(name, bio, avatarUri, bannerUri); onBack() }) {
                        Text("Save")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(18.dp)
        ) {
            SectionLabel("Banner")
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(90.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .clickable { bannerPicker.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                if (bannerUri != null) {
                    AsyncImage(model = bannerUri, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                } else {
                    Icon(Icons.Filled.CameraAlt, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Spacer(Modifier.height(22.dp))
            SectionLabel("Profile Photo")
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .size(72.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .clickable { avatarPicker.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    if (avatarUri != null) {
                        AsyncImage(model = avatarUri, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                    } else {
                        Icon(Icons.Filled.CameraAlt, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Spacer(Modifier.width(14.dp))
                Text(
                    "Tap to upload · square photos work best",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.height(22.dp))
            SectionLabel("Display Name")
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(Modifier.height(16.dp))
            SectionLabel("Bio")
            OutlinedTextField(
                value = bio,
                onValueChange = { bio = it },
                modifier = Modifier.fillMaxWidth().height(110.dp),
                maxLines = 5
            )
            Spacer(Modifier.height(40.dp))
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text.uppercase(),
        fontSize = 10.sp,
        letterSpacing = 2.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
        modifier = Modifier.padding(bottom = 10.dp)
    )
}
