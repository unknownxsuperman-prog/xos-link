package com.x0s.link.ui.favourites

import android.media.MediaPlayer
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.x0s.link.data.model.FAV_TYPES
import com.x0s.link.data.model.FavItem
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavouritesScreen(vm: FavouritesViewModel, userId: String, onBack: () -> Unit) {
    LaunchedEffect(userId) { vm.loadAll(userId) }
    val items by vm.items.collectAsState()
    var tab by remember { mutableStateOf(0) } // 0 = Music, 1 = Shows
    var showsSub by remember { mutableStateOf("movie") }
    var searchOpen by remember { mutableStateOf(false) }
    var searchType by remember { mutableStateOf("music") }

    val context = LocalContext.current
    var player by remember { mutableStateOf<MediaPlayer?>(null) }
    var playingTitle by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    fun playPreview(item: FavItem) {
        if (item.streamUrl.isBlank()) return
        if (playingTitle == item.title) {
            player?.release(); player = null; playingTitle = null
            return
        }
        player?.release()
        playingTitle = item.title
        player = MediaPlayer().apply {
            setDataSource(item.streamUrl)
            setOnPreparedListener { start() }
            setOnCompletionListener { playingTitle = null }
            prepareAsync()
        }
    }

    DisposableEffect(Unit) { onDispose { player?.release() } }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, null) } },
                title = { Text("Favourites", fontWeight = FontWeight.Bold) }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                searchType = if (tab == 0) "music" else showsSub
                searchOpen = true
            }) { Icon(Icons.Filled.Add, contentDescription = "Add favourite") }
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            TabRow(selectedTabIndex = tab) {
                Tab(selected = tab == 0, onClick = { tab = 0 }, text = { Text("Music") })
                Tab(selected = tab == 1, onClick = { tab = 1 }, text = { Text("Shows") })
            }
            if (tab == 0) {
                LazyColumn(Modifier.fillMaxSize().padding(16.dp)) {
                    item0Header("Tracks")
                    itemGrid(items["music"].orEmpty(), poster = false, onRemove = { vm.removeFavourite(userId, "music", it) }, onPlay = { name ->
                        items["music"]?.find { it.first == name }?.second?.let(::playPreview)
                    }, playingTitle = playingTitle)
                    item0Header("Albums")
                    itemGrid(items["album"].orEmpty(), poster = false, onRemove = { vm.removeFavourite(userId, "album", it) })
                }
            } else {
                Column(Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("movie" to "Movies", "series" to "Series", "anime" to "Anime").forEach { (key, label) ->
                            FilterChip(
                                selected = showsSub == key,
                                onClick = { showsSub = key },
                                label = { Text(label, fontSize = 11.sp) }
                            )
                        }
                    }
                }
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    val list = items[showsSub].orEmpty()
                    if (list.isEmpty()) {
                        item(span = { GridItemSpan(2) }) { EmptyState("No ${showsSub}s yet · tap + to add") }
                    }
                    items(list) { (name, info) ->
                        ShowCard(info) { vm.removeFavourite(userId, showsSub, name) }
                    }
                }
            }
        }

        if (playingTitle != null) {
            val playingItem = (items["music"].orEmpty()).find { it.first == playingTitle }?.second
            if (playingItem != null) {
                MiniPlayerBar(playingItem, onStop = {
                    player?.release(); player = null; playingTitle = null
                })
            }
        }
    }

    if (searchOpen) {
        AddFavouriteSheet(
            vm = vm,
            userId = userId,
            initialType = searchType,
            onDismiss = { searchOpen = false }
        )
    }
}

private fun androidx.compose.foundation.lazy.LazyListScope.item0Header(text: String) {
    item {
        Text(
            text.uppercase(),
            fontSize = 10.sp,
            letterSpacing = 2.sp,
            color = androidx.compose.ui.graphics.Color.Gray,
            modifier = Modifier.padding(vertical = 10.dp)
        )
    }
}

private fun androidx.compose.foundation.lazy.LazyListScope.itemGrid(
    list: List<Pair<String, FavItem>>,
    poster: Boolean,
    onRemove: (String) -> Unit,
    onPlay: ((String) -> Unit)? = null,
    playingTitle: String? = null
) {
    item {
        if (list.isEmpty()) {
            EmptyState("Nothing here yet · tap + to add")
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier.fillMaxWidth().heightIn(max = 1200.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                userScrollEnabled = false
            ) {
                items(list) { (name, info) ->
                    TrackCard(info, isPlaying = playingTitle == info.title, onPlay = onPlay?.let { { it(name) } }) { onRemove(name) }
                }
            }
        }
    }
}

@Composable
private fun EmptyState(text: String) {
    Box(Modifier.fillMaxWidth().padding(vertical = 40.dp), contentAlignment = Alignment.Center) {
        Text(text, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun TrackCard(item: FavItem, isPlaying: Boolean, onPlay: (() -> Unit)?, onRemove: () -> Unit) {
    Column(
        Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Box(Modifier.aspectRatio(1f)) {
            AsyncImage(model = item.artwork, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
            if (onPlay != null) {
                IconButton(
                    onClick = onPlay,
                    modifier = Modifier.align(Alignment.BottomEnd).padding(6.dp).size(30.dp)
                        .clip(CircleShape).background(androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.55f))
                ) {
                    Icon(
                        if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        contentDescription = "Play",
                        tint = androidx.compose.ui.graphics.Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            IconButton(
                onClick = onRemove,
                modifier = Modifier.align(Alignment.TopEnd).padding(4.dp).size(22.dp)
                    .clip(CircleShape).background(androidx.compose.ui.graphics.Color(0xFFFF3B30))
            ) {
                Icon(Icons.Filled.Close, contentDescription = "Remove", tint = androidx.compose.ui.graphics.Color.White, modifier = Modifier.size(12.dp))
            }
        }
        Column(Modifier.padding(8.dp)) {
            Text(item.title, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
            Text(item.subtitle, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
        }
    }
}

@Composable
private fun ShowCard(item: FavItem, onRemove: () -> Unit) {
    Column(
        Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Box(Modifier.aspectRatio(2f / 3f)) {
            AsyncImage(model = item.artwork, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
            Box(
                Modifier.align(Alignment.BottomStart).padding(6.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.55f))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) { Text("⭐ ${item.rating}", fontSize = 9.sp, color = androidx.compose.ui.graphics.Color.White) }
            IconButton(
                onClick = onRemove,
                modifier = Modifier.align(Alignment.TopEnd).padding(4.dp).size(22.dp)
                    .clip(CircleShape).background(androidx.compose.ui.graphics.Color(0xFFFF3B30))
            ) {
                Icon(Icons.Filled.Close, contentDescription = "Remove", tint = androidx.compose.ui.graphics.Color.White, modifier = Modifier.size(12.dp))
            }
        }
        Column(Modifier.padding(8.dp)) {
            Text(item.title, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
            Text(item.year, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun MiniPlayerBar(item: FavItem, onStop: () -> Unit) {
    Box(Modifier.fillMaxSize()) {
        Row(
            Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = item.artwork, contentDescription = null, contentScale = ContentScale.Crop,
                modifier = Modifier.size(40.dp).clip(RoundedCornerShape(10.dp))
            )
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(item.title, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
                Text(item.subtitle, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
            }
            IconButton(onClick = onStop) { Icon(Icons.Filled.Pause, contentDescription = "Pause") }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddFavouriteSheet(vm: FavouritesViewModel, userId: String, initialType: String, onDismiss: () -> Unit) {
    var type by remember { mutableStateOf(initialType) }
    var query by remember { mutableStateOf("") }
    val results by vm.searchResults.collectAsState()
    val scope = rememberCoroutineScope()

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(Modifier.fillMaxWidth().padding(16.dp).heightIn(min = 400.dp)) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it; vm.search(type, it) },
                placeholder = { Text("Search…") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                FAV_TYPES.forEach { t ->
                    FilterChip(
                        selected = type == t.key,
                        onClick = { type = t.key; vm.search(type, query) },
                        label = { Text(t.label, fontSize = 11.sp) }
                    )
                }
            }
            Spacer(Modifier.height(10.dp))
            if (vm.searching) {
                Box(Modifier.fillMaxWidth().padding(30.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            } else {
                LazyColumn(Modifier.fillMaxWidth()) {
                    items(results) { r ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .clickable { vm.addFavourite(userId, type, r) }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AsyncImage(
                                model = r.artwork, contentDescription = null, contentScale = ContentScale.Crop,
                                modifier = Modifier.size(46.dp).clip(RoundedCornerShape(10.dp))
                            )
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(r.title, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
                                Text(r.subtitle.ifBlank { r.year }, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                            }
                            Icon(Icons.Filled.AddCircleOutline, contentDescription = "Add")
                        }
                    }
                }
            }
        }
    }
}
