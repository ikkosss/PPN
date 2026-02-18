package com.adguard.wireguardhotspotbridge.ui.screens.diagnostics

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.adguard.wireguardhotspotbridge.vpn.VpnState
import com.adguard.wireguardhotspotbridge.vpn.VpnStateStore

@Composable
fun DiagnosticsScreen(contentPadding: PaddingValues, vm: DiagnosticsViewModel = viewModel()) {
    val st by vm.state.collectAsState()
    val context = LocalContext.current
    val vpn by VpnStateStore.state.collectAsState()
    val tetheredText = remember(vpn.vpnState) {
        if (vpn.vpnState == VpnState.CONNECTED) "Not guaranteed" else "Unknown"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Диагностика")
        Button(onClick = { vm.refresh(tetheredTrafficViaVpn = tetheredText) }) {
            Text("Сформировать отчёт")
        }
        Button(
            onClick = {
                val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                cm.setPrimaryClip(ClipData.newPlainText("report", st.report))
            },
            enabled = st.report.isNotBlank(),
        ) {
            Text("Скопировать отчёт")
        }
        if (st.report.isNotBlank()) {
            Text(st.report)
        }
    }
}

