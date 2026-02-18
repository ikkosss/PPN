package com.adguard.wireguardhotspotbridge.ui.screens.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun ProfileScreen(contentPadding: PaddingValues, vm: ProfileViewModel = viewModel()) {
    val st by vm.state.collectAsState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("WireGuard профиль (wg-quick)")

        OutlinedTextField(
            value = st.name,
            onValueChange = vm::onNameChange,
            label = { Text("Имя профиля") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )

        OutlinedTextField(
            value = st.wgQuick,
            onValueChange = vm::onConfigChange,
            label = { Text("Вставьте wg-quick конфиг целиком") },
            minLines = 10,
            modifier = Modifier.fillMaxWidth(),
        )

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = vm::validate) { Text("Проверить") }
            Button(onClick = vm::save) { Text("Сохранить") }
            if (st.message != null) Text(st.message!!)
            if (st.parsedSummary != null) {
                Text("Сводка:")
                Text(st.parsedSummary!!)
            }
            TextButton(onClick = {
                vm.onConfigChange(
                    """
                    [Interface]
                    PrivateKey = <PASTE_PRIVATE_KEY>
                    Address = 10.0.0.2/32
                    DNS = 94.140.14.14

                    [Peer]
                    PublicKey = <PASTE_PUBLIC_KEY>
                    PresharedKey = <OPTIONAL_PRESHARED_KEY>
                    AllowedIPs = 0.0.0.0/0, ::/0
                    Endpoint = 1.2.3.4:51820
                    PersistentKeepalive = 25
                    """.trimIndent(),
                )
            }) { Text("Вставить пример") }
        }
    }
}

