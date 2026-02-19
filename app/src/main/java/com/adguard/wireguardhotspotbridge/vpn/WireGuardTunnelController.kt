package com.adguard.wireguardhotspotbridge.vpn

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import com.wireguard.android.backend.BackendException
import com.wireguard.android.backend.GoBackend
import com.wireguard.android.backend.Tunnel
import com.wireguard.config.Config

class WireGuardTunnelController(appContext: Context) {
    private val backend = GoBackend(appContext)
    private val tunnel = SimpleTunnel("adguard")

    init {
        GoBackend.setAlwaysOnCallback {
            ContextCompat.startForegroundService(
                appContext,
                Intent(appContext, VpnOrchestratorService::class.java),
            )
        }
    }

    fun currentState(): Tunnel.State = backend.getState(tunnel)

    @Throws(Exception::class)
    fun connect(config: Config) {
        backend.setState(tunnel, Tunnel.State.UP, config)
    }

    @Throws(Exception::class)
    fun disconnect() {
        backend.setState(tunnel, Tunnel.State.DOWN, null)
    }

    fun readableError(t: Throwable): String {
        return when (t) {
            is BackendException -> t.reason.toString()
            else -> t.message ?: t::class.java.simpleName
        }
    }
}

private class SimpleTunnel(private val name: String) : Tunnel {
    @Volatile
    private var lastState: Tunnel.State = Tunnel.State.DOWN

    override fun getName(): String = name

    override fun onStateChange(newState: Tunnel.State) {
        lastState = newState
    }

    @Suppress("unused")
    fun lastState(): Tunnel.State = lastState
}

