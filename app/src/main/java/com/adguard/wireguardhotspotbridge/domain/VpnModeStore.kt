package com.adguard.wireguardhotspotbridge.domain

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class VpnModeStore(appContext: Context) {
    private val prefs = appContext.getSharedPreferences("vpn_mode", Context.MODE_PRIVATE)

    private val _mode = MutableStateFlow(read())
    val mode: StateFlow<VpnMode> = _mode.asStateFlow()

    fun set(mode: VpnMode) {
        prefs.edit().putString("mode", mode.name).apply()
        _mode.value = mode
    }

    private fun read(): VpnMode {
        val raw = prefs.getString("mode", null)
        return runCatching { if (raw == null) VpnMode.WIREGUARD else VpnMode.valueOf(raw) }.getOrDefault(VpnMode.WIREGUARD)
    }
}

