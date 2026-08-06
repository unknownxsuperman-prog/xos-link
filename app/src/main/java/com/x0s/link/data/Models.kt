package com.x0s.link.data

enum class Badge { NONE, GOLD, BLUE, GREEN }

fun Badge.toKey(): String = when (this) {
    Badge.GOLD -> "gold"
    Badge.BLUE -> "blue"
    Badge.GREEN -> "green"
    Badge.NONE -> "none"
}

fun badgeFromKey(k: String?): Badge = when (k) {
    "gold" -> Badge.GOLD
    "blue" -> Badge.BLUE
    "green" -> Badge.GREEN
    else -> Badge.NONE
}

data class XosPost(
    val type: String,          // "image" | "video"
    val files: List<String>,
    val caption: String,
    val likes: Int,
    val time: String
)

data class XosProfile(
    val userid: String,
    val handle: String,
    val displayName: String,
    val verified: Badge,
    val avatar: String,
    val following: List<String>,
    val followers: List<String>,
    val posts: List<XosPost>
)

data class XosCollege(
    val id: String,
    val name: String,
    val pfp: String,
    val pill1: String,
    val pill2: String
)

/** A post flattened with its author, matching how the feed builds FEED_POSTS in index.html */
data class FeedItem(
    val post: XosPost,
    val author: XosProfile
)

data class MsgConversation(
    val id: Int,
    val name: String,
    val handle: String,
    val avatar: String,
    var preview: String,
    val time: String,
    var unread: Boolean,
    val online: Boolean,
    val messages: MutableList<ChatMsg>
)

data class ChatMsg(
    val text: String,
    val out: Boolean,
    val time: String,
    val isImage: Boolean = false
)
