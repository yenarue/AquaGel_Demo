package com.example.aquagel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel

class WoundSharedViewModel(private val savedStateHandle: SavedStateHandle) : ViewModel() {
    var photoUri: String? by mutableStateOf(savedStateHandle[KEY_PHOTO_URI])
        set(value) {
            field = value
            savedStateHandle[KEY_PHOTO_URI] = value
        }

    var woundInfo: WoundInfo? by mutableStateOf(savedStateHandle[KEY_WOUND_INFO])
        set(value) {
            field = value
            savedStateHandle[KEY_WOUND_INFO] = value
        }

    companion object {
        private const val KEY_PHOTO_URI = "photoUri"
        private const val KEY_WOUND_INFO = "woundInfo"
    }
}
