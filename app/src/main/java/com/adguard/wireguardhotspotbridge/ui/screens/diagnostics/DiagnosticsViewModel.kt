package com.adguard.wireguardhotspotbridge.ui.screens.diagnostics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adguard.wireguardhotspotbridge.ServiceLocator
import com.adguard.wireguardhotspotbridge.diagnostics.DiagnosticsReport
import com.adguard.wireguardhotspotbridge.hotspot.HotspotStateStore
import com.adguard.wireguardhotspotbridge.vpn.VpnStateStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class DiagnosticsUiState(
    val report: String = "",
)

class DiagnosticsViewModel : ViewModel() {
    private val _state = MutableStateFlow(DiagnosticsUiState())
    val state: StateFlow<DiagnosticsUiState> = _state.asStateFlow()

    fun refresh(tetheredTrafficViaVpn: String) {
        viewModelScope.launch {
            val vpn = VpnStateStore.state.value
            val hs = HotspotStateStore.state.value
            _state.value = DiagnosticsUiState(
                report = DiagnosticsReport.build(
                    context = ServiceLocator.appContext,
                    vpn = vpn,
                    hotspot = hs,
                    vpnMode = ServiceLocator.vpnMode.mode.value.name,
                    systemVpnActive = ServiceLocator.systemVpn.state.value.active,
                    tetheredTrafficViaVpn = tetheredTrafficViaVpn,
                ),
            )
        }
    }
}

