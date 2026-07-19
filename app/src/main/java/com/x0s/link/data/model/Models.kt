package com.x0s.link.data.model

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

/**
 * Mirrors the shape produced by profiles.js -> window.XOS_PROFILES
 * (see anush-decodes.html / profiles.js on x0s.link)
 */
@Serializable
data class XosProfile(
    val userid: String = "",
    val handle: String = "",
    val displayName: String = "",
    val verified: String = "none", // "gold" | "blue" | "green" | "none"
    val avatar: String = "",
    val banner: String = "",
    val bio: String = "",
    val following: List<String> = emptyList(),
    val followers: List<String> = emptyList(),
    val links: List<XosLink> = emptyList(),
    val audioTrack: XosTrack? = null,
    val posts: List<XosPost> = emptyList()
) {
    /** See [XosLink.sanitize] - call immediately after every Gson decode of profiles.js. */
    fun sanitize(): XosProfile = copy(
        userid = userid ?: "",
        handle = handle ?: "",
        displayName = displayName ?: "Unknown",
        verified = verified ?: "none",
        avatar = avatar ?: "",
        banner = banner ?: "",
        bio = bio ?: "",
        following = following?.filterNotNull() ?: emptyList(),
        followers = followers?.filterNotNull() ?: emptyList(),
        links = links?.filterNotNull()?.map { it.sanitize() } ?: emptyList(),
        audioTrack = audioTrack?.sanitize(),
        posts = posts?.filterNotNull()?.map { it.sanitize() } ?: emptyList()
    )
}

@Serializable
data class XosLink(
    val label: String = "",
    val url: String = "",
    val icon: String = "",
    val color: String = ""
) {
    /**
     * Gson instantiates data classes via reflection, bypassing the Kotlin constructor -
     * so a field missing from the source JSON is left as a raw JVM `null` even though the
     * Kotlin type says it can't be. Call this right after every Gson decode to patch those
     * "impossible" nulls back to the declared defaults before anything else touches the object.
     */
    fun sanitize(): XosLink = copy(
        label = label ?: "",
        url = url ?: "",
        icon = icon ?: "",
        color = color ?: ""
    )
}

@Serializable
data class XosTrack(
    val title: String = "",
    val artist: String = "",
    val artworkUrl: String = "",
    val streamUrl: String = ""
) {
    fun sanitize(): XosTrack = copy(
        title = title ?: "",
        artist = artist ?: "",
        artworkUrl = artworkUrl ?: "",
        streamUrl = streamUrl ?: ""
    )
}

@Serializable
data class XosPost(
    val type: String = "image", // "image" | "video"
    val files: List<String> = emptyList(),
    val caption: String = "",
    val likes: Int = 0,
    val time: String = "",
    val comments: Int = 0
) {
    fun sanitize(): XosPost = copy(
        type = type ?: "image",
        files = files?.filterNotNull() ?: emptyList(),
        caption = caption ?: "",
        time = time ?: ""
    )
}

/**
 * Mirrors window.XOS_COLLEGES from colleges.js
 */
@Serializable
data class XosCollege(
    val id: String = "",
    val name: String = "",
    val pfp: String = "",
    val pill1: String = "",
    val pill2: String = ""
) {
    fun sanitize(): XosCollege = copy(
        id = id ?: "",
        name = name ?: "",
        pfp = pfp ?: "",
        pill1 = pill1 ?: "",
        pill2 = pill2 ?: ""
    )
}

/** A simple demo conversation model for the Messages tab. */
data class XosConversation(
    val id: Int,
    val name: String,
    val handle: String,
    val avatar: String,
    var preview: String,
    val time: String,
    var unread: Boolean,
    val online: Boolean,
    val messages: MutableList<XosChatMessage>
)

data class XosChatMessage(
    val text: String,
    val out: Boolean,
    val time: String,
    val isImage: Boolean = false
)

/** Favourite entries for the Favourites screen (music / album / movie / series / anime). */
data class FavouriteType(val key: String, val label: String)

val FAV_TYPES = listOf(
    FavouriteType("music", "Track"),
    FavouriteType("album", "Album"),
    FavouriteType("movie", "Movie"),
    FavouriteType("series", "Series"),
    FavouriteType("anime", "Anime")
)

// ---- iTunes Search API response models ----
data class ItunesSearchResponse(
    @SerializedName("resultCount") val resultCount: Int = 0,
    @SerializedName("results") val results: List<ItunesResult> = emptyList()
)

data class ItunesResult(
    @SerializedName("trackName") val trackName: String? = null,
    @SerializedName("collectionName") val collectionName: String? = null,
    @SerializedName("artistName") val artistName: String? = null,
    @SerializedName("artworkUrl100") val artworkUrl100: String? = null,
    @SerializedName("previewUrl") val previewUrl: String? = null,
    @SerializedName("releaseDate") val releaseDate: String? = null,
    @SerializedName("primaryGenreName") val primaryGenreName: String? = null,
    @SerializedName("trackCount") val trackCount: Int? = null,
    @SerializedName("trackTimeMillis") val trackTimeMillis: Long? = null,
    @SerializedName("contentAdvisoryRating") val contentAdvisoryRating: String? = null
)

// ---- TMDB ----
data class TmdbSearchResponse(
    @SerializedName("results") val results: List<TmdbResult> = emptyList()
)

data class TmdbResult(
    @SerializedName("title") val title: String? = null,
    @SerializedName("name") val name: String? = null,
    @SerializedName("overview") val overview: String? = null,
    @SerializedName("poster_path") val posterPath: String? = null,
    @SerializedName("release_date") val releaseDate: String? = null,
    @SerializedName("first_air_date") val firstAirDate: String? = null,
    @SerializedName("vote_average") val voteAverage: Double? = null
)

// ---- Jikan (anime) ----
data class JikanSearchResponse(
    @SerializedName("data") val data: List<JikanAnime> = emptyList()
)

data class JikanAnime(
    @SerializedName("title") val title: String? = null,
    @SerializedName("title_english") val titleEnglish: String? = null,
    @SerializedName("synopsis") val synopsis: String? = null,
    @SerializedName("images") val images: JikanImages? = null,
    @SerializedName("year") val year: Int? = null,
    @SerializedName("score") val score: Double? = null
)

data class JikanImages(@SerializedName("jpg") val jpg: JikanImageUrl? = null)
data class JikanImageUrl(@SerializedName("image_url") val imageUrl: String? = null)

// ---- TVMaze (series) ----
data class TvMazeSearchResult(
    @SerializedName("show") val show: TvMazeShow
)

data class TvMazeShow(
    @SerializedName("name") val name: String? = null,
    @SerializedName("genres") val genres: List<String> = emptyList(),
    @SerializedName("premiered") val premiered: String? = null,
    @SerializedName("image") val image: TvMazeImage? = null,
    @SerializedName("rating") val rating: TvMazeRating? = null
)

data class TvMazeImage(@SerializedName("medium") val medium: String? = null, @SerializedName("original") val original: String? = null)
data class TvMazeRating(@SerializedName("average") val average: Double? = null)

/** Unified lightweight model used by the Favourites UI regardless of source API. */
data class FavItem(
    val title: String,
    val subtitle: String = "",
    val artwork: String = "",
    val year: String = "",
    val rating: String = "",
    val streamUrl: String = "",
    val meta: String = ""
)
