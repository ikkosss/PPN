package com.adguard.wireguardhotspotbridge.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adguard.wireguardhotspotbridge.ServiceLocator
import com.adguard.wireguardhotspotbridge.domain.VpnMode
import com.wireguard.config.Config
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ProfileUiState(
    val mode: VpnMode = VpnMode.WIREGUARD,

    val name: String = "AdGuard",
    val wgQuick: String = "",
    val parsedSummary: String? = null,
    val ikev2ServerAddress: String = "",
    val ikev2ServerId: String = "",
    val ikev2Username: String = "",
    val ikev2Password: String = "",
    val ikev2CertificateHint: String = "",

    val message: String? = null,
    val savedProfileId: Long? = null,
)

class ProfileViewModel : ViewModel() {
    private val _state = MutableStateFlow(ProfileUiState())
    val state: StateFlow<ProfileUiState> = _state.asStateFlow()

    init {
        _state.value = _state.value.copy(mode = ServiceLocator.vpnMode.mode.value)
        viewModelScope.launch {
            ServiceLocator.vpnMode.mode.collect { m ->
                _state.value = _state.value.copy(mode = m, message = null)
            }
        }
    }

    fun setMode(mode: VpnMode) {
        ServiceLocator.vpnMode.set(mode)
    }

    fun onNameChange(v: String) {
        _state.value = _state.value.copy(name = v, message = null)
    }

    fun onConfigChange(v: String) {
        _state.value = _state.value.copy(wgQuick = v, message = null, parsedSummary = null, savedProfileId = null)
    }

    fun onIkev2ServerAddressChange(v: String) {
        _state.value = _state.value.copy(ikev2ServerAddress = v, message = null)
    }

    fun onIkev2ServerIdChange(v: String) {
        _state.value = _state.value.copy(ikev2ServerId = v, message = null)
    }

    fun onIkev2UsernameChange(v: String) {
        _state.value = _state.value.copy(ikev2Username = v, message = null)
    }

    fun onIkev2PasswordChange(v: String) {
        _state.value = _state.value.copy(ikev2Password = v, message = null)
    }

    fun onIkev2CertificateHintChange(v: String) {
        _state.value = _state.value.copy(ikev2CertificateHint = v, message = null)
    }

    fun validate() {
        val st = state.value
        viewModelScope.launch {
            try {
                when (st.mode) {
                    VpnMode.WIREGUARD -> {
                        val cfg = ServiceLocator.profiles.parse(st.wgQuick)
                        _state.value = _state.value.copy(
                            parsedSummary = summarize(cfg),
                            message = "Ок: WireGuard конфиг валиден",
                        )
                    }
                    VpnMode.SYSTEM_IKEV2 -> {
                        require(st.ikev2ServerAddress.isNotBlank()) { "IP-адрес/хост пуст" }
                        require(st.ikev2ServerId.isNotBlank()) { "ID сервера пуст" }
                        require(st.ikev2Username.isNotBlank()) { "Имя пользователя пусто" }
                        require(st.ikev2Password.isNotBlank()) { "Пароль пуст" }
                        _state.value = _state.value.copy(message = "Ок: IKEv2 поля заполнены")
                    }
                }
            } catch (t: Throwable) {
                _state.value = _state.value.copy(message = "Ошибка: ${t.message}")
            }
        }
    }

    fun save() {
        val st = state.value
        viewModelScope.launch {
            try {
                val id = when (st.mode) {
                    VpnMode.WIREGUARD -> ServiceLocator.profiles.save(st.name, st.wgQuick)
                    VpnMode.SYSTEM_IKEV2 -> ServiceLocator.ikev2Profiles.save(
                        name = st.name,
                        serverAddress = st.ikev2ServerAddress,
                        serverId = st.ikev2ServerId,
                        username = st.ikev2Username,
                        password = st.ikev2Password,
                        certificateHint = st.ikev2CertificateHint.ifBlank { null },
                    )
                }
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

