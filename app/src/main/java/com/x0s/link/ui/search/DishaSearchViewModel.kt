package com.x0s.link.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.x0s.link.data.model.XosCollege
import com.x0s.link.data.model.XosProfile
import com.x0s.link.data.repository.ProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class DishaMessage {
    data class UserBubble(val text: String) : DishaMessage()
    data class ProfileResult(val profile: XosProfile) : DishaMessage()
    data class CollegeResult(val college: XosCollege) : DishaMessage()
    data class NotFound(val query: String) : DishaMessage()
}

class DishaSearchViewModel(private val repo: ProfileRepository) : ViewModel() {

    private val _messages = MutableStateFlow<List<DishaMessage>>(emptyList())
    val messages = _messages.asStateFlow()

    private val _suggestions = MutableStateFlow<List<Any>>(emptyList())
    val suggestions = _suggestions.asStateFlow()

    fun onInputChanged(query: String) {
        viewModelScope.launch {
            repo.ensureLoaded()
            if (query.isBlank()) {
                _suggestions.value = emptyList()
                return@launch
            }
            val users = repo.searchProfiles(query).take(3)
            val colleges = repo.searchColleges(query).take(3)
            _suggestions.value = users + colleges
        }
    }

    fun search(query: String) {
        if (query.isBlank()) return
        viewModelScope.launch {
            repo.ensureLoaded()
            _messages.value = _messages.value + DishaMessage.UserBubble(query)

            val q = query.trim()
            when {
                q.startsWith("@") -> {
                    val user = repo.searchProfiles(q.removePrefix("@")).firstOrNull()
                    _messages.value = _messages.value + (
                        user?.let { DishaMessage.ProfileResult(it) } ?: DishaMessage.NotFound(q)
                        )
                }
                q.startsWith("#") -> {
                    val college = repo.searchColleges(q.removePrefix("#")).firstOrNull()
                    _messages.value = _messages.value + (
                        college?.let { DishaMessage.CollegeResult(it) } ?: DishaMessage.NotFound(q)
                        )
                }
                else -> {
                    val user = repo.searchProfiles(q).firstOrNull()
                    val college = repo.searchColleges(q).firstOrNull()
                    val results = mutableListOf<DishaMessage>()
                    if (college != null) results.add(DishaMessage.CollegeResult(college))
                    if (user != null) results.add(DishaMessage.ProfileResult(user))
                    if (results.isEmpty()) results.add(DishaMessage.NotFound(q))
                    _messages.value = _messages.value + results
                }
            }
        }
    }
}
