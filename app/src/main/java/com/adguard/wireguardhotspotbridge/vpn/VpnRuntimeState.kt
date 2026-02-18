package com.adguard.wireguardhotspotbridge.vpn

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class VpnState {
    DISCONNECTED,
    CONNECTING,
    CONNECTED,
    ERROR,
}

data class VpnRuntimeState(
    val vpnState: VpnState = VpnState.DISCONNECTED,
    val activeProfileId: Long? = null,
    val lastError: String? = null,
)

object VpnStateStore {
    private val _state = MutableStateFlow(VpnRuntimeState())
    val state: StateFlow<VpnRuntimeState> = _state.asStateFlow()

    fun update(newState: VpnRuntimeState) {
        _state.value = newState
    }
}

