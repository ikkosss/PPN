package com.adguard.wireguardhotspotbridge.diagnostics

import android.content.Context
import android.net.VpnService
import android.os.Build
import com.adguard.wireguardhotspotbridge.hotspot.HotspotRuntimeState
import com.adguard.wireguardhotspotbridge.vpn.VpnRuntimeState

object DiagnosticsReport {
    fun build(
        context: Context,
        vpn: VpnRuntimeState,
        hotspot: HotspotRuntimeState,
        tetheredTrafficViaVpn: String,
    ): String {
        val vpnPrepared = VpnService.prepare(context) == null
        return buildString {
            appendLine("AdGuard WireGuard Hotspot Bridge — Диагностика")
            appendLine("Device: ${Build.MANUFACTURER} ${Build.MODEL}")
            appendLine("Android: ${Build.VERSION.RELEASE} (SDK ${Build.VERSION.SDK_INT})")
            appendLine("VPN permission prepared: ${if (vpnPrepared) "yes" else "no"}")
            appendLine("VPN state: ${vpn.vpnState}")
            appendLine("Active profile id: ${vpn.activeProfileId ?: "-"}")
            appendLine("Hotspot control method: ${hotspot.method}")
            appendLine("Hotspot requested: ${hotspot.requestedOn}")
            appendLine("Tethered traffic via VPN: $tetheredTrafficViaVpn")
            if (!hotspot.lastError.isNullOrBlank()) appendLine("Hotspot error: ${hotspot.lastError}")
            if (!vpn.lastError.isNullOrBlank()) appendLine("VPN error: ${vpn.lastError}")
            appendLine()
            appendLine("Note: без root/привилегий ОС нельзя гарантировать hotspot->VPN на всех устройствах.")
        }.trimEnd()
    }
}

