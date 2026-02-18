package com.adguard.wireguardhotspotbridge.ui.screens.control

import android.content.Intent
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adguard.wireguardhotspotbridge.ServiceLocator
import com.adguard.wireguardhotspotbridge.hotspot.HotspotStateStore
import com.adguard.wireguardhotspotbridge.vpn.VpnOrchestratorService
import com.adguard.wireguardhotspotbridge.vpn.VpnState
import com.adguard.wireguardhotspotbridge.vpn.VpnStateStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class TetheredTrafficViaVpn {
    GUARANTEED,
    NOT_GUARANTEED,
    UNKNOWN,
}

data class ControlUiState(
    val hasProfile: Boolean = false,
    val activeProfileId: Long? = null,
    val vpnState: VpnState = VpnState.DISCONNECTED,
    val hotspotRequestedOn: Boolean = false,
    val hotspotMethod: String = "Settings fallback",
    val tetheredTrafficViaVpn: TetheredTrafficViaVpn = TetheredTrafficViaVpn.UNKNOWN,
    val lastError: String? = null,
)

class ControlViewModel : ViewModel() {
    private val _state = MutableStateFlow(ControlUiState())
    val state: StateFlow<ControlUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val profile = ServiceLocator.profiles.getLatest()
            _state.value = _state.value.copy(hasProfile = profile != null, activeProfileId = profile?.id)
        }
        viewModelScope.launch {
            VpnStateStore.state.collect { vpn ->
                _state.value = _state.value.copy(
                    vpnState = vpn.vpnState,
                    activeProfileId = vpn.activeProfileId ?: _state.value.activeProfileId,
                    lastError = vpn.lastError,
                    tetheredTrafficViaVpn = when (vpn.vpnState) {
                        VpnState.CONNECTED -> TetheredTrafficViaVpn.NOT_GUARANTEED
                        else -> TetheredTrafficViaVpn.UNKNOWN
                    },
                )
            }
        }
        viewModelScope.launch {
            HotspotStateStore.state.collect { hs ->
                _state.value = _state.value.copy(
                    hotspotRequestedOn = hs.requestedOn,
                    hotspotMethod = hs.method.name,
                    lastError = hs.lastError ?: _state.value.lastError,
                )
            }
        }
    }

    fun refreshProfile() {
        viewModelScope.launch {
            val profile = ServiceLocator.profiles.getLatest()
            _state.value = _state.value.copy(hasProfile = profile != null, activeProfileId = profile?.id)
        }
    }

    fun startVpnPlusHotspot() {
        val profileId = state.value.activeProfileId ?: return
        val intent = Intent(ServiceLocator.appContext, VpnOrchestratorService::class.java)
            .setAction(VpnOrchestratorService.ACTION_START)
            .putExtra(VpnOrchestratorService.EXTRA_PROFILE_ID, profileId)
            .putExtra(VpnOrchestratorService.EXTRA_ENABLE_HOTSPOT, true)
        ContextCompat.startForegroundService(ServiceLocator.appContext, intent)
    }

    fun stopVpn() {
        val intent = Intent(ServiceLocator.appContext, VpnOrchestratorService::class.java)
            .setAction(VpnOrchestratorService.ACTION_STOP)
        ServiceLocator.appContext.startService(intent)
    }
}

