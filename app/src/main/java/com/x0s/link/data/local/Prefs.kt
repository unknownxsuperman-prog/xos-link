package com.x0s.link.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.x0s.link.data.model.FavItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "xos_prefs")

/**
 * Lightweight key-value store for everything the web app kept in localStorage:
 * liked/saved post ids, favourite names per type, cached lookups, linked college,
 * and locally-edited profile fields (name/bio/avatar/banner/links/track).
 */
class Prefs(private val context: Context) {

    private val gson = Gson()

    private fun key(k: String) = stringPreferencesKey(k)
    private fun setKey(k: String) = stringSetPreferencesKey(k)

    suspend fun putString(k: String, v: String) {
        context.dataStore.edit { it[key(k)] = v }
    }

    suspend fun getString(k: String, default: String = ""): String {
        return context.dataStore.data.map { it[key(k)] ?: default }.first()
    }

    fun getStringFlow(k: String, default: String = ""): Flow<String> =
        context.dataStore.data.map { it[key(k)] ?: default }

    suspend fun putBool(k: String, v: Boolean) = putString(k, v.toString())
    suspend fun getBool(k: String, default: Boolean = false): Boolean =
        getString(k, default.toString()).toBoolean()

    suspend fun putStringSet(k: String, v: Set<String>) {
        context.dataStore.edit { it[setKey(k)] = v }
    }

    suspend fun getStringSet(k: String): Set<String> {
        return context.dataStore.data.map { it[setKey(k)] ?: emptySet() }.first()
    }

    fun getStringSetFlow(k: String): Flow<Set<String>> =
        context.dataStore.data.map { it[setKey(k)] ?: emptySet() }

    // ---- Favourites: names list per type, scoped by user ----
    suspend fun getFavouriteNames(userId: String, type: String): List<String> {
        val raw = getString("fav_${userId}_$type", "[]")
        return try {
            gson.fromJson(raw, object : TypeToken<List<String>>() {}.type)
        } catch (e: Exception) { emptyList() }
    }

    suspend fun addFavouriteName(userId: String, type: String, name: String) {
        val current = getFavouriteNames(userId, type).toMutableList()
        if (current.none { it.equals(name, true) }) {
            current.add(name)
            putString("fav_${userId}_$type", gson.toJson(current))
        }
    }

    suspend fun removeFavouriteName(userId: String, type: String, name: String) {
        val current = getFavouriteNames(userId, type).toMutableList()
        current.removeAll { it.equals(name, true) }
        putString("fav_${userId}_$type", gson.toJson(current))
    }

    // ---- Cache of resolved FavItem info per (type,name), 7 day soft TTL ignored for simplicity ----
    suspend fun cacheFavItem(type: String, name: String, item: FavItem) {
        putString("favcache_${type}_${name.lowercase()}", gson.toJson(item))
    }

    suspend fun getCachedFavItem(type: String, name: String): FavItem? {
        val raw = getString("favcache_${type}_${name.lowercase()}", "")
        if (raw.isBlank()) return null
        return try { gson.fromJson(raw, FavItem::class.java) } catch (e: Exception) { null }
    }

    // ---- Linked college ----
    suspend fun setLinkedCollege(userId: String, collegeId: String?) {
        putString("linked_college_$userId", collegeId ?: "")
    }

    suspend fun getLinkedCollege(userId: String): String? {
        val v = getString("linked_college_$userId", "")
        return v.ifBlank { null }
    }

    // ---- Post like/save state, per user scope ----
    suspend fun toggleLike(userId: String, postKey: String): Boolean {
        val liked = getStringSet("liked_$userId").toMutableSet()
        val nowLiked = !liked.contains(postKey)
        if (nowLiked) liked.add(postKey) else liked.remove(postKey)
        putStringSet("liked_$userId", liked)
        return nowLiked
    }

    suspend fun isLiked(userId: String, postKey: String): Boolean =
        getStringSet("liked_$userId").contains(postKey)

    suspend fun toggleSave(userId: String, postKey: String): Boolean {
        val saved = getStringSet("saved_$userId").toMutableSet()
        val nowSaved = !saved.contains(postKey)
        if (nowSaved) saved.add(postKey) else saved.remove(postKey)
        putStringSet("saved_$userId", saved)
        return nowSaved
    }

    // ---- Follow state for demo (whether the local "me" follows a given userid) ----
    suspend fun toggleFollow(targetUserId: String): Boolean {
        val following = getStringSet("i_follow").toMutableSet()
        val nowFollowing = !following.contains(targetUserId)
        if (nowFollowing) following.add(targetUserId) else following.remove(targetUserId)
        putStringSet("i_follow", following)
        return nowFollowing
    }

    suspend fun isFollowing(targetUserId: String): Boolean =
        getStringSet("i_follow").contains(targetUserId)
}
