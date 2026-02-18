package com.adguard.wireguardhotspotbridge.ui.screens.control

import android.app.Activity
import android.net.VpnService
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.adguard.wireguardhotspotbridge.ServiceLocator
import com.adguard.wireguardhotspotbridge.vpn.VpnState

@Composable
fun ControlScreen(contentPadding: PaddingValues, vm: ControlViewModel = viewModel()) {
    val st by vm.state.collectAsState()
    val context = LocalContext.current

    var pendingStart by remember { mutableStateOf(false) }
    val vpnPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
    ) { result ->
        pendingStart = false
        if (result.resultCode == Activity.RESULT_OK) {
            vm.startVpnPlusHotspot()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Управление")

        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("VPN + Hotspot")
            val checked = st.vpnState == VpnState.CONNECTED || st.vpnState == VpnState.CONNECTING
            Switch(
                checked = checked,
                onCheckedChange = { on ->
                    if (on) {
                        vm.refreshProfile()
                        if (!st.hasProfile || st.activeProfileId == null) return@Switch
                        val prepare = VpnService.prepare(context)
                        if (prepare != null) {
                            pendingStart = true
                            vpnPermissionLauncher.launch(prepare)
                        } else {
                            vm.startVpnPlusHotspot()
                        }
                    } else {
                        vm.stopVpn()
                    }
                },
            )
        }

        HorizontalDivider()

        Text("VPN: ${st.vpnState}")
        Text("Hotspot: ${if (st.hotspotRequestedOn) "requested ON" else "OFF/unknown"} (${st.hotspotMethod})")
        Text(
            "Tethered traffic via VPN: ${
                when (st.tetheredTrafficViaVpn) {
                    TetheredTrafficViaVpn.GUARANTEED -> "Guaranteed"
                    TetheredTrafficViaVpn.NOT_GUARANTEED -> "Not guaranteed"
                    TetheredTrafficViaVpn.UNKNOWN -> "Unknown"
                }
            }",
        )
        Text("Важно: без root/привилегий ОС нельзя гарантировать VPN для клиентов хотспота на всех устройствах.")

        if (pendingStart) {
            Text("Ожидается разрешение VPN…")
        }
        if (st.lastError != null) {
            Text("Последняя ошибка: ${st.lastError}")
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(onClick = { ServiceLocator.hotspot.openSystemHotspotSettings() }) {
                Text("Открыть настройки раздачи")
            }
        }
    }
}

