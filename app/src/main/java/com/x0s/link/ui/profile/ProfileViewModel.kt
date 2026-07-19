package com.x0s.link.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.x0s.link.data.model.XosCollege
import com.x0s.link.data.model.XosProfile
import com.x0s.link.data.repository.ProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ProfileEdits(
    val displayName: String? = null,
    val bio: String? = null,
    val avatar: String? = null,
    val banner: String? = null
)

class ProfileViewModel(private val repo: ProfileRepository) : ViewModel() {

    private val gson = Gson()

    private val _profile = MutableStateFlow<XosProfile?>(null)
    val profile = _profile.asStateFlow()

    private val _edits = MutableStateFlow(ProfileEdits())
    val edits = _edits.asStateFlow()

    private val _linkedCollege = MutableStateFlow<XosCollege?>(null)
    val linkedCollege = _linkedCollege.asStateFlow()

    private val _isFollowing = MutableStateFlow(false)
    val isFollowing = _isFollowing.asStateFlow()

    fun load(userId: String) {
        viewModelScope.launch {
            repo.ensureLoaded()
            _profile.value = repo.getProfile(userId) ?: repo.getProfile(repo.defaultUserId())
            val realId = _profile.value?.userid ?: userId
            _edits.value = ProfileEdits(
                displayName = repo.prefs.getString("edit_name_$realId").ifBlank { null },
                bio = repo.prefs.getString("edit_bio_$realId").ifBlank { null },
                avatar = repo.prefs.getString("edit_avatar_$realId").ifBlank { null },
                banner = repo.prefs.getString("edit_banner_$realId").ifBlank { null }
            )
            val linkedId = repo.prefs.getLinkedCollege(realId)
            _linkedCollege.value = linkedId?.let { repo.getCollege(it) }
            _isFollowing.value = repo.prefs.isFollowing(realId)
        }
    }

    fun toggleFollow() {
        val userId = _profile.value?.userid ?: return
        viewModelScope.launch {
            _isFollowing.value = repo.prefs.toggleFollow(userId)
        }
    }

    fun linkCollege(college: XosCollege) {
        val userId = _profile.value?.userid ?: return
        viewModelScope.launch {
            repo.prefs.setLinkedCollege(userId, college.id)
            _linkedCollege.value = college
        }
    }

    fun unlinkCollege() {
        val userId = _profile.value?.userid ?: return
        viewModelScope.launch {
            repo.prefs.setLinkedCollege(userId, null)
            _linkedCollege.value = null
        }
    }

    fun saveEdits(name: String, bio: String, avatar: String?, banner: String?) {
        val userId = _profile.value?.userid ?: return
        viewModelScope.launch {
            repo.prefs.putString("edit_name_$userId", name)
            repo.prefs.putString("edit_bio_$userId", bio)
            avatar?.let { repo.prefs.putString("edit_avatar_$userId", it) }
            banner?.let { repo.prefs.putString("edit_banner_$userId", it) }
            _edits.value = ProfileEdits(name, bio, avatar ?: _edits.value.avatar, banner ?: _edits.value.banner)
        }
    }

    fun displayName(): String = edits.value.displayName ?: profile.value?.displayName ?: ""
    fun bio(): String = edits.value.bio ?: profile.value?.bio ?: ""
    fun avatar(): String? = edits.value.avatar ?: profile.value?.avatar
    fun banner(): String? = edits.value.banner ?: profile.value?.banner

    fun followers(): List<XosProfile> = profile.value?.let { repo.followersOf(it.userid) } ?: emptyList()
    fun following(): List<XosProfile> = profile.value?.let { repo.followingOf(it.userid) } ?: emptyList()
}
