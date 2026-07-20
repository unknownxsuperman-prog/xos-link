package com.x0s.link.ui.feed

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.x0s.link.data.model.XosPost
import com.x0s.link.data.model.XosProfile
import com.x0s.link.data.repository.ProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class FeedViewModel(private val repo: ProfileRepository, private val meUserId: () -> String) : ViewModel() {

    private val _posts = MutableStateFlow<List<Pair<XosProfile, XosPost>>>(emptyList())
    val posts = _posts.asStateFlow()

    var loading by mutableStateOf(true)
        private set

    fun load() {
        viewModelScope.launch {
            loading = true
            repo.ensureLoaded()
            _posts.value = repo.feedPosts()
            loading = false
        }
    }

    fun refresh() {
        viewModelScope.launch {
            loading = true
            repo.refresh()
            _posts.value = repo.feedPosts()
            loading = false
        }
    }

    suspend fun isLiked(postKey: String) = repo.prefs.isLiked(meUserId(), postKey)
    suspend fun toggleLike(postKey: String) = repo.prefs.toggleLike(meUserId(), postKey)
    suspend fun toggleSave(postKey: String) = repo.prefs.toggleSave(meUserId(), postKey)
}
