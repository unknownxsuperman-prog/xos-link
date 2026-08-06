package com.x0s.link.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Contrast
import androidx.compose.material.icons.filled.IosShare
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.x0s.link.data.FeedItem
import com.x0s.link.data.XosRepository
import com.x0s.link.ui.components.XosBottomBar
import com.x0s.link.ui.components.XosToast
import com.x0s.link.ui.screens.DrawerOverlay
import com.x0s.link.ui.screens.EssentialOverlay
import com.x0s.link.ui.screens.FeedScreen
import com.x0s.link.ui.screens.MessagesOverlay
import com.x0s.link.ui.screens.SearchOverlay
import com.x0s.link.ui.screens.ZynxOverlay
import com.x0s.link.ui.theme.XosBackground
import com.x0s.link.ui.theme.XosCard
import com.x0s.link.ui.theme.XosWhite
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun AppRoot() {
    val state = remember { AppState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        val profiles = XosRepository.loadProfiles()
        val colleges = XosRepository.loadColleges()
        state.profiles = profiles
        state.colleges = colleges
        state.activeUser = profiles.values.firstOrNull()
        state.feed.clear()
        state.feed.addAll(profiles.values.flatMap { author -> author.posts.map { FeedItem(it, author) } })
        state.conversations.clear()
        state.conversations.addAll(XosRepository.mockConversations())
    }

    // ── back press: topmost overlay closes first, matching popstate handling in index.html ──
    BackHandler(enabled = state.activeConversation != null) { state.activeConversation = null }
    BackHandler(enabled = state.activeConversation == null && state.overlay != Overlay.NONE) { state.overlay = Overlay.NONE }
    BackHandler(enabled = state.overlay == Overlay.NONE && state.drawerOpen) { state.closeDrawer() }

    Box(Modifier.fillMaxSize().background(XosBackground)) {
        FeedScreen(state)

        Column(Modifier.fillMaxSize()) {
            Spacer(Modifier.weight(1f))
            XosBottomBar(
                active = when (state.overlay) {
                    Overlay.SEARCH, Overlay.ESSENTIAL -> "search"
                    Overlay.ZYNX -> "zynx"
                    Overlay.MESSAGES -> "messages"
                    Overlay.NONE -> "home"
                },
                onHome = { state.overlay = Overlay.NONE },
                onOpenSearch = { state.overlay = Overlay.SEARCH },
                onOpenEssential = { state.overlay = Overlay.ESSENTIAL },
                onZynx = { state.overlay = Overlay.ZYNX },
                onMessages = { state.overlay = Overlay.MESSAGES },
                onNotif = { state.toastMsg = "No new notifications" }
            )
        }

        // top-right dropdown (Get Badge / Share / Appearance)
        AnimatedVisibility(
            visible = state.topDropdownOpen,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.TopEnd).padding(top = 54.dp, end = 16.dp)
        ) {
            Column(
                Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(XosCard)
                    .padding(6.dp)
            ) {
                TopDropdownItem(Icons.Filled.WorkspacePremium, "Get Badge") { state.topDropdownOpen = false }
                TopDropdownItem(Icons.Filled.IosShare, "Share") {
                    state.topDropdownOpen = false
                    state.toastMsg = "Link copied!"
                    scope.launch { delay(2200); state.toastMsg = null }
                }
                TopDropdownItem(Icons.Filled.Contrast, "Appearance") {
                    state.topDropdownOpen = false
                    state.darkMode = !state.darkMode
                }
            }
        }

        DrawerOverlay(state)
        SearchOverlay(state)
        EssentialOverlay(state)
        ZynxOverlay(state)
        MessagesOverlay(state)

        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
            XosToast(state.toastMsg)
        }
    }

    // dismiss the dropdown / toast by tapping elsewhere handled implicitly via overlay clicks
}

@Composable
private fun TopDropdownItem(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, onClick: () -> Unit) {
    Row(
        Modifier
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = XosWhite, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
        Spacer(Modifier.width(20.dp))
        Icon(icon, contentDescription = null, tint = com.x0s.link.ui.theme.XosAccent, modifier = Modifier.size(16.dp))
    }
}
