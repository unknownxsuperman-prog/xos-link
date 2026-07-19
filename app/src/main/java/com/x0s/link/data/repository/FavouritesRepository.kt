package com.x0s.link.data.repository

import android.content.Context
import com.x0s.link.data.local.Prefs
import com.x0s.link.data.model.FavItem
import com.x0s.link.data.remote.NetworkModule
import com.x0s.link.data.remote.TmdbConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext

/**
 * Mirrors xos-favourites.html: tracks + albums via iTunes Search API, movies/series/anime
 * via TMDB (if a key is configured) with automatic fallback to free public APIs
 * (iTunes for movies, TVMaze for series, Jikan for anime) - exactly like the web app's
 * tmdbFind() -> publicFind() fallback chain.
 */
class FavouritesRepository(context: Context) {

    private val prefs = Prefs(context.applicationContext)

    suspend fun getFavouriteNames(userId: String, type: String) = prefs.getFavouriteNames(userId, type)

    suspend fun addFavourite(userId: String, type: String, name: String, item: FavItem) {
        prefs.addFavouriteName(userId, type, name)
        prefs.cacheFavItem(type, name, item)
    }

    suspend fun removeFavourite(userId: String, type: String, name: String) {
        prefs.removeFavouriteName(userId, type, name)
    }

    /** Resolve a saved favourite name into full display info, using cache first. */
    suspend fun resolve(type: String, name: String): FavItem {
        prefs.getCachedFavItem(type, name)?.let { return it }
        val info = lookup(type, name) ?: FavItem(title = name)
        prefs.cacheFavItem(type, name, info)
        return info
    }

    suspend fun resolveAll(type: String, names: List<String>): List<FavItem> = coroutineScope {
        names.map { n -> async { resolve(type, n) } }.map { it.await() }
    }

    private suspend fun lookup(type: String, name: String): FavItem? = withContext(Dispatchers.IO) {
        try {
            when (type) {
                "music" -> lookupTrack(name)
                "album" -> lookupAlbum(name)
                "movie", "series", "anime" -> lookupShow(name, type)
                else -> null
            }
        } catch (e: Exception) {
            null
        }
    }

    private suspend fun lookupTrack(name: String): FavItem? {
        val res = NetworkModule.itunesApi.search(name, entity = "song", media = "music", limit = 1)
        val r = res.results.firstOrNull() ?: return null
        return FavItem(
            title = r.trackName ?: name,
            subtitle = r.artistName ?: "Unknown artist",
            artwork = (r.artworkUrl100 ?: "").replace("100x100", "400x400"),
            year = r.releaseDate?.take(4) ?: "",
            meta = r.primaryGenreName ?: "",
            streamUrl = r.previewUrl ?: ""
        )
    }

    private suspend fun lookupAlbum(name: String): FavItem? {
        val res = NetworkModule.itunesApi.search(name, entity = "album", media = "music", limit = 1)
        val r = res.results.firstOrNull() ?: return null
        return FavItem(
            title = r.collectionName ?: name,
            subtitle = r.artistName ?: "Unknown artist",
            artwork = (r.artworkUrl100 ?: "").replace("100x100", "400x400"),
            year = r.releaseDate?.take(4) ?: "",
            meta = if ((r.trackCount ?: 0) > 0) "${r.trackCount} tracks" else "Album"
        )
    }

    private suspend fun lookupShow(name: String, type: String): FavItem? {
        // Try TMDB first if configured, else public fallback (matches web app logic).
        if (TmdbConfig.hasKey()) {
            try {
                val res = if (type == "movie") {
                    NetworkModule.tmdbApi.searchMovie(TmdbConfig.API_KEY, name)
                } else {
                    NetworkModule.tmdbApi.searchTv(TmdbConfig.API_KEY, name)
                }
                val r = res.results.firstOrNull()
                if (r != null) {
                    val title = r.title ?: r.name ?: name
                    val year = (r.releaseDate ?: r.firstAirDate ?: "").take(4)
                    return FavItem(
                        title = title,
                        subtitle = r.overview?.take(60).orEmpty(),
                        artwork = r.posterPath?.let { "https://image.tmdb.org/t/p/w342$it" } ?: "",
                        year = year,
                        rating = r.voteAverage?.let { String.format("%.1f", it) } ?: "—"
                    )
                }
            } catch (e: Exception) { /* fall through to public sources */ }
        }
        return when (type) {
            "movie" -> lookupMovieViaItunes(name)
            "anime" -> lookupAnimeViaJikan(name)
            else -> lookupSeriesViaTvMaze(name)
        }
    }

    private suspend fun lookupMovieViaItunes(name: String): FavItem? {
        val res = NetworkModule.itunesApi.search(name, entity = "movie", media = "all", limit = 1)
        val r = res.results.firstOrNull() ?: return null
        return FavItem(
            title = r.trackName ?: name,
            subtitle = r.primaryGenreName ?: "",
            artwork = (r.artworkUrl100 ?: "").replace("100x100", "400x600"),
            year = r.releaseDate?.take(4) ?: "",
            rating = r.contentAdvisoryRating ?: "—"
        )
    }

    private suspend fun lookupAnimeViaJikan(name: String): FavItem? {
        val res = NetworkModule.jikanApi.searchAnime(name, limit = 1)
        val a = res.data.firstOrNull() ?: return null
        val synopsis = a.synopsis?.take(60) ?: ""
        return FavItem(
            title = a.titleEnglish ?: a.title ?: name,
            subtitle = synopsis,
            artwork = a.images?.jpg?.imageUrl ?: "",
            year = a.year?.toString() ?: "",
            rating = a.score?.let { String.format("%.1f", it) } ?: "—"
        )
    }

    private suspend fun lookupSeriesViaTvMaze(name: String): FavItem? {
        val res = NetworkModule.tvMazeApi.searchShows(name)
        val first = res.firstOrNull() ?: return null
        val show = first.show
        return FavItem(
            title = show.name ?: name,
            subtitle = show.genres.take(2).joinToString(", "),
            artwork = show.image?.medium ?: show.image?.original ?: "",
            year = show.premiered?.take(4) ?: "",
            rating = show.rating?.average?.let { String.format("%.1f", it) } ?: "—"
        )
    }

    /** Live search results (not yet saved) for the add-favourite search screen. */
    suspend fun search(type: String, query: String): List<FavItem> = withContext(Dispatchers.IO) {
        try {
            when (type) {
                "music" -> NetworkModule.itunesApi.search(query, "song", "music", 12).results.map {
                    FavItem(
                        title = it.trackName ?: "—",
                        subtitle = it.artistName ?: "—",
                        artwork = (it.artworkUrl100 ?: "").replace("100x100", "400x400"),
                        year = it.releaseDate?.take(4) ?: "",
                        meta = it.primaryGenreName ?: "",
                        streamUrl = it.previewUrl ?: ""
                    )
                }
                "album" -> NetworkModule.itunesApi.search(query, "album", "music", 12).results.map {
                    FavItem(
                        title = it.collectionName ?: "—",
                        subtitle = it.artistName ?: "—",
                        artwork = (it.artworkUrl100 ?: "").replace("100x100", "400x400"),
                        year = it.releaseDate?.take(4) ?: "",
                        meta = if ((it.trackCount ?: 0) > 0) "${it.trackCount} tracks" else "Album"
                    )
                }
                "movie" -> searchMovies(query)
                "anime" -> NetworkModule.jikanApi.searchAnime(query, 12).data.map {
                    FavItem(
                        title = it.titleEnglish ?: it.title ?: "—",
                        subtitle = it.synopsis?.take(40) ?: "",
                        artwork = it.images?.jpg?.imageUrl ?: "",
                        year = it.year?.toString() ?: "",
                        rating = it.score?.let { s -> String.format("%.1f", s) } ?: "—"
                    )
                }
                "series" -> searchSeries(query)
                else -> emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    private suspend fun searchMovies(query: String): List<FavItem> {
        if (TmdbConfig.hasKey()) {
            try {
                val r = NetworkModule.tmdbApi.searchMovie(TmdbConfig.API_KEY, query)
                if (r.results.isNotEmpty()) {
                    return r.results.take(12).map {
                        FavItem(
                            title = it.title ?: "—",
                            subtitle = it.overview?.take(40) ?: "",
                            artwork = it.posterPath?.let { p -> "https://image.tmdb.org/t/p/w342$p" } ?: "",
                            year = it.releaseDate?.take(4) ?: "",
                            rating = it.voteAverage?.let { v -> String.format("%.1f", v) } ?: "—"
                        )
                    }
                }
            } catch (e: Exception) { /* fall through */ }
        }
        return NetworkModule.itunesApi.search(query, "movie", "all", 12).results.map {
            FavItem(
                title = it.trackName ?: "—",
                subtitle = it.primaryGenreName ?: "",
                artwork = (it.artworkUrl100 ?: "").replace("100x100", "400x600"),
                year = it.releaseDate?.take(4) ?: "",
                rating = it.contentAdvisoryRating ?: "—"
            )
        }
    }

    private suspend fun searchSeries(query: String): List<FavItem> {
        if (TmdbConfig.hasKey()) {
            try {
                val r = NetworkModule.tmdbApi.searchTv(TmdbConfig.API_KEY, query)
                if (r.results.isNotEmpty()) {
                    return r.results.take(12).map {
                        FavItem(
                            title = it.name ?: "—",
                            subtitle = it.overview?.take(40) ?: "",
                            artwork = it.posterPath?.let { p -> "https://image.tmdb.org/t/p/w342$p" } ?: "",
                            year = it.firstAirDate?.take(4) ?: "",
                            rating = it.voteAverage?.let { v -> String.format("%.1f", v) } ?: "—"
                        )
                    }
                }
            } catch (e: Exception) { /* fall through */ }
        }
        return NetworkModule.tvMazeApi.searchShows(query).take(12).map {
            FavItem(
                title = it.show.name ?: "—",
                subtitle = it.show.genres.take(2).joinToString(", "),
                artwork = it.show.image?.medium ?: it.show.image?.original ?: "",
                year = it.show.premiered?.take(4) ?: "",
                rating = it.show.rating?.average?.let { v -> String.format("%.1f", v) } ?: "—"
            )
        }
    }
}
