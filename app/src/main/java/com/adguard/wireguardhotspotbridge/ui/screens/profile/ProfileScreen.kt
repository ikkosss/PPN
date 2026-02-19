package com.adguard.wireguardhotspotbridge.ui.screens.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.adguard.wireguardhotspotbridge.domain.VpnMode

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
        Text("Профиль VPN")

        Row(verticalAlignment = Alignment.CenterVertically) {
            RadioButton(selected = st.mode == VpnMode.WIREGUARD, onClick = { vm.setMode(VpnMode.WIREGUARD) })
            Text("WireGuard (wg-quick)")
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            RadioButton(selected = st.mode == VpnMode.SYSTEM_IKEV2, onClick = { vm.setMode(VpnMode.SYSTEM_IKEV2) })
            Text("System IKEv2 (manual)")
        }

        HorizontalDivider()

        OutlinedTextField(
            value = st.name,
            onValueChange = vm::onNameChange,
            label = { Text("Имя профиля") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )

        if (st.mode == VpnMode.WIREGUARD) {
            OutlinedTextField(
                value = st.wgQuick,
                onValueChange = vm::onConfigChange,
                label = { Text("Вставьте wg-quick конфиг целиком") },
                minLines = 10,
                modifier = Modifier.fillMaxWidth(),
            )
        } else {
            Text("IKEv2 будет настраиваться в системных настройках VPN. Здесь — хранение/копирование полей.")
            OutlinedTextField(
                value = st.ikev2Username,
                onValueChange = vm::onIkev2UsernameChange,
                label = { Text("Имя пользователя") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = st.ikev2Password,
                onValueChange = vm::onIkev2PasswordChange,
                label = { Text("Пароль") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = st.ikev2ServerAddress,
                onValueChange = vm::onIkev2ServerAddressChange,
                label = { Text("IP-адрес / Host") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = st.ikev2ServerId,
                onValueChange = vm::onIkev2ServerIdChange,
                label = { Text("ID сервера (IPSec ID)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = st.ikev2CertificateHint,
                onValueChange = vm::onIkev2CertificateHintChange,
                label = { Text("Сертификат (имя файла, опционально)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = vm::validate) { Text("Проверить") }
            Button(onClick = vm::save) { Text("Сохранить") }
            if (st.message != null) Text(st.message!!)
            if (st.parsedSummary != null) {
                Text("Сводка:")
                Text(st.parsedSummary!!)
            }
            if (st.mode == VpnMode.WIREGUARD) {
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
}

