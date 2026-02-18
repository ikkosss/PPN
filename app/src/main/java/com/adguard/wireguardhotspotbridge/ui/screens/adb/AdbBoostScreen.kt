package com.adguard.wireguardhotspotbridge.ui.screens.adb

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
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import com.adguard.wireguardhotspotbridge.BuildConfig

@Composable
fun AdbBoostScreen(contentPadding: PaddingValues) {
    val pkg = BuildConfig.APPLICATION_ID
    val clipboard = LocalClipboardManager.current

    val commands = listOf(
        "adb shell cmd deviceidle whitelist +$pkg",
        "adb shell appops set $pkg RUN_ANY_IN_BACKGROUND allow",
        "adb shell appops set $pkg WAKE_LOCK allow",
        "",
        "# (Опционально) WRITE_SECURE_SETTINGS (если доступно на вашем устройстве/прошивке)",
        "adb shell pm grant $pkg android.permission.WRITE_SECURE_SETTINGS",
        "",
        "# (Опционально) Nearby/Wi‑Fi (Android 13+)",
        "adb shell pm grant $pkg android.permission.NEARBY_WIFI_DEVICES",
        "adb shell pm grant $pkg android.permission.POST_NOTIFICATIONS",
    ).joinToString("\n")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("ADB усиления")
        Text("Это улучшает стабильность (фон/Doze), но НЕ превращает hotspot->VPN в 100% на всех устройствах.")
        Text(commands)
        Button(onClick = { clipboard.setText(AnnotatedString(commands)) }) {
            Text("Скопировать команды")
        }
    }
}

