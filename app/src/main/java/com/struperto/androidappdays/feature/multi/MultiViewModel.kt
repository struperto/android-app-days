package com.struperto.androidappdays.feature.multi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.struperto.androidappdays.AppContainer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class MultiViewModel : ViewModel() {
    private val staticState = MutableStateFlow(MultiUiState())

    val state = staticState.asStateFlow()

    companion object {
        fun factory(appContainer: AppContainer): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                MultiViewModel()
            }
        }
    }
}
