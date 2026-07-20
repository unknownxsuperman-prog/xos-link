package com.x0s.link.data.repository

import android.content.Context
import com.x0s.link.data.local.Prefs
import com.x0s.link.data.model.XosCollege
import com.x0s.link.data.model.XosProfile
import com.x0s.link.data.remote.RemoteDataSource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withTimeoutOrNull

/**
 * Single source of truth for profiles + colleges.
 *
 * Startup strategy (important for a snappy, crash-proof first paint):
 *  1. Load the bundled fallback JSON assets first - this is a fast local file read, so the
 *     Feed screen always has real data to render on the very first frame, with or without
 *     network access.
 *  2. Kick off a best-effort fetch of the live profiles.js / colleges.js in the background
 *     with a short timeout. If it succeeds, swap the in-memory data for the live version.
 *     If it's slow, unreachable, or fails to parse, the bundled fallback simply stays in
 *     place - the user never sees a blank screen, an error page, or a hang.
 */
class ProfileRepository(context: Context) {

    private val remote = RemoteDataSource(context.applicationContext)
    val prefs = Prefs(context.applicationContext)

    private val _profiles = MutableStateFlow<Map<String, XosProfile>>(emptyMap())
    val profiles = _profiles.asStateFlow()

    private val _colleges = MutableStateFlow<List<XosCollege>>(emptyList())
    val colleges = _colleges.asStateFlow()

    private val _loading = MutableStateFlow(true)
    val loading = _loading.asStateFlow()

    private var loaded = false

    suspend fun ensureLoaded() {
        if (loaded) return
        _loading.value = true

        // 1) Instant local data so the UI never blocks.
        _profiles.value = remote.loadFallbackProfiles()
        _colleges.value = remote.loadFallbackColleges()
        _loading.value = false
        loaded = true

        // 2) Best-effort background refresh from the live site (max ~6s, never blocks caller).
        withTimeoutOrNull(6000) {
            val liveProfiles = remote.fetchLiveProfiles()
            val liveColleges = remote.fetchLiveColleges()
            if (liveProfiles != null) _profiles.value = liveProfiles
            if (liveColleges != null) _colleges.value = liveColleges
        }
    }

    suspend fun refresh() {
        loaded = false
        ensureLoaded()
    }

    fun defaultUserId(): String = _profiles.value.keys.firstOrNull() ?: "guest"

    fun getProfile(userId: String): XosProfile? = _profiles.value[userId]

    fun allProfiles(): List<XosProfile> = _profiles.value.values.toList()

    fun searchProfiles(query: String): List<XosProfile> {
        val q = query.trim().lowercase()
        if (q.isEmpty()) return emptyList()
        return _profiles.value.values.filter {
            it.displayName.lowercase().contains(q) ||
                it.handle.lowercase().contains(q) ||
                it.userid.lowercase().contains(q)
        }
    }

    fun searchColleges(query: String): List<XosCollege> {
        val q = query.trim().lowercase().replace(" ", "")
        if (q.isEmpty()) return emptyList()
        if (q == "#college" || q == "#colleges") return _colleges.value
        return _colleges.value.filter { it.name.lowercase().replace(" ", "").contains(q) }
    }

    fun getCollege(id: String): XosCollege? = _colleges.value.find { it.id == id }

    fun followerCount(userId: String) = getProfile(userId)?.followers?.size ?: 0
    fun followingCount(userId: String) = getProfile(userId)?.following?.size ?: 0

    fun followersOf(userId: String): List<XosProfile> =
        getProfile(userId)?.followers?.mapNotNull { getProfile(it) } ?: emptyList()

    fun followingOf(userId: String): List<XosProfile> =
        getProfile(userId)?.following?.mapNotNull { getProfile(it) } ?: emptyList()

    /** All posts across all profiles, newest-ish first (mirrors renderFeed in the web app). */
    fun feedPosts(): List<Pair<XosProfile, com.x0s.link.data.model.XosPost>> {
        return _profiles.value.values.flatMap { p -> p.posts.map { p to it } }
    }
}
