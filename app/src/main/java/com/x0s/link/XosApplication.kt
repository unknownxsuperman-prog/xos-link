package com.x0s.link

import android.app.Application
import com.x0s.link.data.repository.FavouritesRepository
import com.x0s.link.data.repository.ProfileRepository

class XosApplication : Application() {
    lateinit var profileRepository: ProfileRepository
        private set
    lateinit var favouritesRepository: FavouritesRepository
        private set

    override fun onCreate() {
        super.onCreate()
        profileRepository = ProfileRepository(this)
        favouritesRepository = FavouritesRepository(this)
    }
}
