package com.adguard.wireguardhotspotbridge.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adguard.wireguardhotspotbridge.ServiceLocator
import com.wireguard.config.Config
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ProfileUiState(
    val name: String = "AdGuard",
    val wgQuick: String = "",
    val parsedSummary: String? = null,
    val message: String? = null,
    val savedProfileId: Long? = null,
)

class ProfileViewModel : ViewModel() {
    private val _state = MutableStateFlow(ProfileUiState())
    val state: StateFlow<ProfileUiState> = _state.asStateFlow()

    fun onNameChange(v: String) {
        _state.value = _state.value.copy(name = v, message = null)
    }

    fun onConfigChange(v: String) {
        _state.value = _state.value.copy(wgQuick = v, message = null, parsedSummary = null, savedProfileId = null)
    }

    fun validate() {
        val text = state.value.wgQuick
        viewModelScope.launch {
            try {
                val cfg = ServiceLocator.profiles.parse(text)
                _state.value = _state.value.copy(
                    parsedSummary = summarize(cfg),
                    message = "Ок: конфиг валиден",
                )
            } catch (t: Throwable) {
                _state.value = _state.value.copy(message = "Ошибка: ${t.message}")
            }
        }
    }

    fun save() {
        val st = state.value
        viewModelScope.launch {
            try {
                val id = ServiceLocator.profiles.save(st.name, st.wgQuick)
                _state.value = _state.value.copy(
                    savedProfileId = id,
                    message = "Сохранено: профиль #$id",
                )
            } catch (t: Throwable) {
                _state.value = _state.value.copy(message = "Ошибка сохранения: ${t.message}")
            }
        }
    }

    private fun summarize(cfg: Config): String {
        val iface = cfg.`interface`
        val addresses = iface.addresses.joinToString { it.toString() }
        val dns = iface.dnsServers.joinToString { it.hostAddress }
        val peer = cfg.peers.firstOrNull()
        val endpoint = peer?.endpoint?.orElse(null)?.toString() ?: "-"
        val allowed = peer?.allowedIps?.joinToString { it.toString() } ?: "-"
        return buildString {
            appendLine("[Interface]")
            appendLine("Address: $addresses")
            if (dns.isNotBlank()) appendLine("DNS: $dns")
            appendLine()
            appendLine("[Peer]")
            appendLine("Endpoint: $endpoint")
            appendLine("AllowedIPs: $allowed")
        }.trimEnd()
    }
}

