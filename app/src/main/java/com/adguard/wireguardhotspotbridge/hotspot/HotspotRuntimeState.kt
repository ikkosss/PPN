package com.adguard.wireguardhotspotbridge.hotspot

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class HotspotControlMethod {
    TETHERING_MANAGER,
    SETTINGS_FALLBACK,
}

data class HotspotRuntimeState(
    val requestedOn: Boolean = false,
    val method: HotspotControlMethod = HotspotControlMethod.SETTINGS_FALLBACK,
    val lastError: String? = null,
)

object HotspotStateStore {
    private val _state = MutableStateFlow(HotspotRuntimeState())
    val state: StateFlow<HotspotRuntimeState> = _state.asStateFlow()

    fun update(newState: HotspotRuntimeState) {
        _state.value = newState
    }
}

