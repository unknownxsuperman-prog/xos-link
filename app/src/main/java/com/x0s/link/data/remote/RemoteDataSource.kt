package com.x0s.link.data.remote

import android.content.Context
import com.x0s.link.data.model.XosCollege
import com.x0s.link.data.model.XosProfile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import java.io.IOException

/**
 * Pulls the live x0s.link data files (profiles.js, colleges.js) straight from the
 * GitHub Pages deployment used by the web app, parses the embedded JS object literal into
 * JSON via [JsObjectParser], then decodes it with kotlinx.serialization.
 *
 * IMPORTANT: this deliberately uses kotlinx.serialization rather than Gson for this path.
 * Gson builds Kotlin data classes via unsafe reflection, bypassing the constructor entirely -
 * so any field missing from the source JS (very likely here, since profiles.js is
 * hand-maintained and doesn't always populate every optional field) is left as a raw `null`
 * at the JVM level even though the Kotlin type says it can never be null. The instant that
 * null field is touched (e.g. `profile.posts.map { ... }` in the feed), the app crashes.
 * kotlinx.serialization goes through the real constructor, so Kotlin's default values
 * (`= emptyList()`, `= ""`, etc.) are correctly applied whenever a field is absent.
 *
 * On top of that, every decoded object is also run through its `.sanitize()` function as a
 * second line of defense in case the upstream JS ever sends an explicit `null` instead of
 * simply omitting the field.
 *
 * Falls back to bundled JSON assets (mirroring the same schema) if the network is
 * unavailable or parsing fails for any reason, so the app always has data to show and the
 * Feed screen never blocks or crashes on startup.
 */
class RemoteDataSource(private val context: Context) {

    private val json = Json {
        ignoreUnknownKeys = true   // profiles.js may contain fields this app doesn't model
        isLenient = true
        coerceInputValues = true  // treat an explicit `null` for a non-null field as "use the default"
    }

    companion object {
        // Same host referenced across anush-decodes.html / x0s-favourites.html
        const val BASE_URL = "https://unknownxsuperman-prog.github.io/anushdecodes/"
        const val PROFILES_JS = BASE_URL + "profiles.js"
        const val COLLEGES_JS = BASE_URL + "colleges.js"
    }

    // ---- Fast, local, always-available data (used for instant first paint) ----

    suspend fun loadFallbackProfiles(): Map<String, XosProfile> = withContext(Dispatchers.IO) {
        loadAsset("profiles_fallback.json") { text ->
            json.decodeFromString(MapSerializer(String.serializer(), XosProfile.serializer()), text)
                .mapValues { it.value.sanitize() }
        } ?: emptyMap()
    }

    suspend fun loadFallbackColleges(): List<XosCollege> = withContext(Dispatchers.IO) {
        loadAsset("colleges_fallback.json") { text ->
            json.decodeFromString(ListSerializer(XosCollege.serializer()), text).map { it.sanitize() }
        } ?: emptyList()
    }

    // ---- Live network data (best-effort, may return null) ----

    suspend fun fetchLiveProfiles(): Map<String, XosProfile>? = withContext(Dispatchers.IO) {
        try {
            val raw = fetchRawText(PROFILES_JS) ?: return@withContext null
            val jsonText = JsObjectParser.extractAsJson(raw, "XOS_PROFILES") ?: return@withContext null
            json.decodeFromString(MapSerializer(String.serializer(), XosProfile.serializer()), jsonText)
                .mapValues { it.value.sanitize() }
                .takeIf { it.isNotEmpty() }
        } catch (e: Exception) {
            null
        }
    }

    suspend fun fetchLiveColleges(): List<XosCollege>? = withContext(Dispatchers.IO) {
        try {
            val raw = fetchRawText(COLLEGES_JS) ?: return@withContext null
            val jsonText = JsObjectParser.extractAsJson(raw, "XOS_COLLEGES") ?: return@withContext null
            json.decodeFromString(ListSerializer(XosCollege.serializer()), jsonText)
                .map { it.sanitize() }
                .takeIf { it.isNotEmpty() }
        } catch (e: Exception) {
            null
        }
    }

    private suspend fun fetchRawText(url: String): String? {
        return try {
            NetworkModule.rawTextApi.fetchRaw(url).string()
        } catch (e: IOException) {
            null
        } catch (e: Exception) {
            null
        }
    }

    private fun <T> loadAsset(name: String, parse: (String) -> T): T? {
        return try {
            val text = context.assets.open(name).bufferedReader().use { it.readText() }
            parse(text)
        } catch (e: Exception) {
            null
        }
    }
}
