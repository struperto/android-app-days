package com.struperto.androidappdays.bootstrap

import com.struperto.androidappdays.data.repository.AreaKernelBootstrapState
import com.struperto.androidappdays.data.repository.AreaKernelRepository
import com.struperto.androidappdays.data.repository.GoalRepository
import com.struperto.androidappdays.data.repository.SourceCapabilityRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

data class AppBootstrapState(
    val isRunning: Boolean = false,
    val isReady: Boolean = false,
    val lastErrorMessage: String? = null,
    val areaBootstrapState: AreaKernelBootstrapState? = null,
)

interface AppBootstrapCoordinator {
    val state: StateFlow<AppBootstrapState>

    suspend fun ensureBootstrapped()
}

class DefaultAppBootstrapCoordinator(
    private val areaKernelRepository: AreaKernelRepository,
    private val goalRepository: GoalRepository,
    private val sourceCapabilityRepository: SourceCapabilityRepository,
) : AppBootstrapCoordinator {
    private val bootstrapMutex = Mutex()
    private val mutableState = MutableStateFlow(AppBootstrapState())

    override val state: StateFlow<AppBootstrapState> = mutableState.asStateFlow()

    override suspend fun ensureBootstrapped() {
        if (mutableState.value.isReady) return
        bootstrapMutex.withLock {
            if (mutableState.value.isReady) return
            mutableState.value = mutableState.value.copy(
                isRunning = true,
                lastErrorMessage = null,
            )
            try {
                val areaBootstrapState = areaKernelRepository.ensureStartBootstrap()
                goalRepository.ensureSeeded()
                sourceCapabilityRepository.ensureSeeded()
                mutableState.value = AppBootstrapState(
                    isRunning = false,
                    isReady = true,
                    lastErrorMessage = null,
                    areaBootstrapState = areaBootstrapState,
                )
            } catch (error: Throwable) {
                mutableState.value = mutableState.value.copy(
                    isRunning = false,
                    isReady = false,
                    lastErrorMessage = error.message ?: error::class.java.simpleName,
                )
            }
        }
    }
}
