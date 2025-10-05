// ThemeViewModel.kt
package com.example.onlyone.theme

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

// ThemeViewModel.kt (use the real initial, and start eagerly)
@HiltViewModel
class ThemeViewModel @Inject constructor(
    private val store: ThemeStore
) : ViewModel() {

    // Read once, synchronously, to avoid flicker
    private val initial: ThemeId = store.blockingInitialThemeId()

    val id: StateFlow<ThemeId> = store.themeIdFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly, // start immediately
            initialValue = initial              // <- key: NOT LIGHT hardcoded
        )

    val tokens: StateFlow<ThemeTokens> = id
        .map { ThemeRegistry.tokens(it) }
        .stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            ThemeRegistry.tokens(initial)      // <- match the above
        )

    fun select(newId: ThemeId) = viewModelScope.launch { store.setTheme(newId) }
}



