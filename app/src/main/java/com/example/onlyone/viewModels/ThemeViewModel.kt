// ThemeViewModel.kt
package com.example.onlyone.theme

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ThemeViewModel @Inject constructor(
    private val store: ThemeStore
) : ViewModel() {
    val id: StateFlow<ThemeId> = store.themeIdFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ThemeId.LIGHT)

    val tokens: StateFlow<ThemeTokens> =
        id.map { ThemeRegistry.tokens(it) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ThemeRegistry.tokens(ThemeId.LIGHT))

    fun select(newId: ThemeId) = viewModelScope.launch { store.setTheme(newId) }
}


