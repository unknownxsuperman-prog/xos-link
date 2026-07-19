package com.x0s.link.ui.nav

object Dest {
    const val FEED = "feed"
    const val SEARCH = "search"
    const val ZYNX = "zynx"
    const val MESSAGES = "messages"
    const val CHAT = "chat/{convId}"
    const val NOTIFICATIONS = "notifications"
    const val PROFILE = "profile/{userId}"
    const val EDIT_PROFILE = "edit_profile"
    const val COLLEGE_SEARCH = "college_search"
    const val COLLEGE_PROFILE = "college_profile/{collegeId}"
    const val FAVOURITES = "favourites/{userId}"

    fun chat(convId: Int) = "chat/$convId"
    fun profile(userId: String) = "profile/$userId"
    fun collegeProfile(collegeId: String) = "college_profile/$collegeId"
    fun favourites(userId: String) = "favourites/$userId"
}

data class BottomTab(val route: String, val label: String)

val BOTTOM_TABS = listOf(
    BottomTab(Dest.FEED, "Home"),
    BottomTab(Dest.SEARCH, "Disha"),
    BottomTab(Dest.ZYNX, "Zynx"),
    BottomTab(Dest.MESSAGES, "Messages"),
    BottomTab(Dest.NOTIFICATIONS, "Alerts")
)
