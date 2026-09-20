package com.rhinepereira.faithflow.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

/**
 * Collects flow updates only while [visible] so hidden keep-alive tabs do not recompose
 * on every Room emission from another tab or background sync.
 */
@Composable
fun <T> StateFlow<T>.collectAsStateWhenVisible(visible: Boolean): T {
    var state by remember { mutableStateOf(value) }
    LaunchedEffect(visible) {
        if (visible) {
            state = value
            collect { state = it }
        }
    }
    return state
}

@Composable
fun <T> Flow<T>.collectAsStateWhenVisible(visible: Boolean, initial: T): T {
    var state by remember { mutableStateOf(initial) }
    LaunchedEffect(visible) {
        if (visible) {
            collect { state = it }
        }
    }
    return state
}
