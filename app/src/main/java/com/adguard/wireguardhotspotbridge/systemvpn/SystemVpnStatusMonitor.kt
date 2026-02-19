package com.adguard.wireguardhotspotbridge.systemvpn

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class SystemVpnState(
    val active: Boolean = false,
)

class SystemVpnStatusMonitor(appContext: Context) {
    private val cm = appContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    private val _state = MutableStateFlow(SystemVpnState(active = isVpnActiveNow()))
    val state: StateFlow<SystemVpnState> = _state.asStateFlow()

    private val callback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) = update()
        override fun onLost(network: Network) = update()
        override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) = update()

        private fun update() {
            _state.value = SystemVpnState(active = isVpnActiveNow())
        }
    }

    init {
        runCatching { cm.registerDefaultNetworkCallback(callback) }
    }

    private fun isVpnActiveNow(): Boolean {
        val networks = cm.allNetworks ?: return false
        for (n in networks) {
            val caps = cm.getNetworkCapabilities(n) ?: continue
            if (caps.hasTransport(NetworkCapabilities.TRANSPORT_VPN)) return true
        }
        return false
    }
}

