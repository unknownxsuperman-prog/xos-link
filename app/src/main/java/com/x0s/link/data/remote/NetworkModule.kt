package com.x0s.link.data.remote

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object NetworkModule {

    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BASIC
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(logging)
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    private fun retrofit(baseUrl: String): Retrofit = Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val itunesApi: ItunesApi by lazy {
        retrofit("https://itunes.apple.com/").create(ItunesApi::class.java)
    }

    val tmdbApi: TmdbApi by lazy {
        retrofit("https://api.themoviedb.org/3/").create(TmdbApi::class.java)
    }

    val jikanApi: JikanApi by lazy {
        retrofit("https://api.jikan.moe/v4/").create(JikanApi::class.java)
    }

    val tvMazeApi: TvMazeApi by lazy {
        retrofit("https://api.tvmaze.com/").create(TvMazeApi::class.java)
    }

    // baseUrl is irrelevant here since every call passes a fully-qualified @Url
    val rawTextApi: RawTextApi by lazy {
        retrofit("https://raw.githubusercontent.com/").create(RawTextApi::class.java)
    }
}
