package com.x0s.link.data.remote

import com.x0s.link.data.model.ItunesSearchResponse
import com.x0s.link.data.model.JikanSearchResponse
import com.x0s.link.data.model.TmdbSearchResponse
import com.x0s.link.data.model.TvMazeSearchResult
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Url

interface ItunesApi {
    @GET("search")
    suspend fun search(
        @Query("term") term: String,
        @Query("entity") entity: String, // song | album | movie
        @Query("media") media: String = "all",
        @Query("limit") limit: Int = 12
    ): ItunesSearchResponse
}

/**
 * TMDB requires an API key. If you have one, put it in [TmdbConfig.API_KEY] - if left blank,
 * the app automatically falls back to the free/public sources (iTunes for movies, TVMaze for
 * series, Jikan for anime), so the app works out of the box with zero configuration.
 */
object TmdbConfig {
    const val API_KEY: String = "" // <-- put your TMDB v3 API key here (optional)
    fun hasKey() = API_KEY.isNotBlank()
}

interface TmdbApi {
    @GET("search/movie")
    suspend fun searchMovie(@Query("api_key") apiKey: String, @Query("query") query: String): TmdbSearchResponse

    @GET("search/tv")
    suspend fun searchTv(@Query("api_key") apiKey: String, @Query("query") query: String): TmdbSearchResponse
}

interface JikanApi {
    @GET("anime")
    suspend fun searchAnime(@Query("q") q: String, @Query("limit") limit: Int = 10): JikanSearchResponse
}

interface TvMazeApi {
    @GET("search/shows")
    suspend fun searchShows(@Query("q") q: String): List<TvMazeSearchResult>
}

/** Fetches arbitrary raw text (used to pull profiles.js / colleges.js from GitHub Pages). */
interface RawTextApi {
    @GET
    suspend fun fetchRaw(@Url url: String): okhttp3.ResponseBody
}
