package com.x0s.link.ui.favourites

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.x0s.link.data.model.FavItem
import com.x0s.link.data.repository.FavouritesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class FavouritesViewModel(private val repo: FavouritesRepository) : ViewModel() {

    private val _items = MutableStateFlow<Map<String, List<Pair<String, FavItem>>>>(emptyMap())
    val items = _items.asStateFlow()

    private val _searchResults = MutableStateFlow<List<FavItem>>(emptyList())
    val searchResults = _searchResults.asStateFlow()

    var searching by androidx.compose.runtime.mutableStateOf(false)
        private set

    fun loadAll(userId: String) {
        viewModelScope.launch {
            val types = listOf("music", "album", "movie", "series", "anime")
            val result = mutableMapOf<String, List<Pair<String, FavItem>>>()
            types.forEach { type ->
                val names = repo.getFavouriteNames(userId, type)
                val infos = repo.resolveAll(type, names)
                result[type] = names.zip(infos)
            }
            _items.value = result
        }
    }

    fun search(type: String, query: String) {
        viewModelScope.launch {
            searching = true
            _searchResults.value = if (query.isBlank()) emptyList() else repo.search(type, query)
            searching = false
        }
    }

    fun addFavourite(userId: String, type: String, item: FavItem) {
        viewModelScope.launch {
            repo.addFavourite(userId, type, item.title, item)
            loadAll(userId)
        }
    }

    fun removeFavourite(userId: String, type: String, name: String) {
        viewModelScope.launch {
            repo.removeFavourite(userId, type, name)
            loadAll(userId)
        }
    }

    fun isSaved(type: String, title: String): Boolean =
        _items.value[type]?.any { it.first.equals(title, true) } == true
}
