package com.stencilla.app

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

@HiltViewModel
class RootViewModel @Inject constructor() : ViewModel() {
    val isLoggedIn = flowOf(true)
}
