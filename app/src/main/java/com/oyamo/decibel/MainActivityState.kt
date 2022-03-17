package com.oyamo.decibel

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.*


data class MainActivityUiState (
    var listening: Boolean = false,
    var decibel: Double = 0.00
    )