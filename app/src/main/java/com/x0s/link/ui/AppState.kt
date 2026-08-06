package com.x0s.link.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import com.x0s.link.data.FeedItem
import com.x0s.link.data.MsgConversation
import com.x0s.link.data.XosCollege
import com.x0s.link.data.XosProfile

enum class Overlay { NONE, SEARCH, ESSENTIAL, ZYNX, MESSAGES }

class ChatBubble(val fromUser: Boolean, val text: String, val isCard: Boolean = false, val cardSubtitle: String = "")

class AppState {
    // ── data ──
    var profiles by mutableStateOf<Map<String, XosProfile>>(emptyMap())
    var colleges by mutableStateOf<List<XosCollege>>(emptyList())
    var activeUser by mutableStateOf<XosProfile?>(null)
    val feed = mutableStateListOf<FeedItem>()

    // ── top-level overlays ──
    var drawerOpen by mutableStateOf(false)
    var overlay by mutableStateOf(Overlay.NONE)
    var topDropdownOpen by mutableStateOf(false)
    var darkMode by mutableStateOf(true)

    // ── feed / tabs ──
    var selectedTab by mutableStateOf(0) // 0 = For You, 1 = Following
    val liked = mutableStateMapOf<Int, Boolean>()
    val saved = mutableStateMapOf<Int, Boolean>()
    val muted = mutableStateMapOf<Int, Boolean>()

    // ── search overlay ──
    var searchQuery by mutableStateOf("")
    var searchTab by mutableStateOf(0) // 0 = People, 1 = Colleges

    // ── essential (Disha) overlay ──
    val essentialMessages = mutableStateListOf<ChatBubble>()
    var essentialInput by mutableStateOf("")

    // ── zynx overlay ──
    var zynxCurrent by mutableStateOf(0)

    // ── messages overlay ──
    val conversations = mutableStateListOf<MsgConversation>()
    var activeConversation by mutableStateOf<MsgConversation?>(null)
    var chatInput by mutableStateOf("")

    // ── toast ──
    var toastMsg by mutableStateOf<String?>(null)

    fun openDrawer() { drawerOpen = true }
    fun closeDrawer() { drawerOpen = false }

    fun closeAllOverlays() {
        overlay = Overlay.NONE
        activeConversation = null
    }
}
