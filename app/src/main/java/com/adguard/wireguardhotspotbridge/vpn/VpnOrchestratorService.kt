package com.adguard.wireguardhotspotbridge.vpn

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.adguard.wireguardhotspotbridge.R
import com.adguard.wireguardhotspotbridge.ServiceLocator
import com.wireguard.config.Config
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.io.ByteArrayInputStream

class VpnOrchestratorService : Service() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        ensureChannel()
    }

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?) = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                val profileId = intent.getLongExtra(EXTRA_PROFILE_ID, -1L)
                val wantHotspot = intent.getBooleanExtra(EXTRA_ENABLE_HOTSPOT, true)
                if (profileId <= 0) {
                    stopSelf()
                    return START_NOT_STICKY
                }
                persistActiveProfile(profileId)
                startForeground(NOTIF_ID, buildNotification("Подключение…"))
                scope.launch { connect(profileId, wantHotspot) }
                return START_STICKY
            }
            ACTION_STOP -> {
                clearActiveProfile()
                scope.launch { disconnectAndStop() }
                return START_NOT_STICKY
            }
            else -> {
                // Restart after kill: try to restore active profile if any.
                val restored = readActiveProfile()
                if (restored != null) {
                    startForeground(NOTIF_ID, buildNotification("Восстановление…"))
                    scope.launch { connect(restored, wantHotspot = false) }
                    return START_STICKY
                }
                return START_NOT_STICKY
            }
        }
    }

    private suspend fun connect(profileId: Long, wantHotspot: Boolean) {
        VpnStateStore.update(VpnRuntimeState(vpnState = VpnState.CONNECTING, activeProfileId = profileId))
        try {
            val wgQuick = ServiceLocator.profiles.buildConfigTextForTunnel(profileId)
            val config = Config.parse(ByteArrayInputStream(wgQuick.toByteArray(Charsets.UTF_8)))
            ServiceLocator.vpn.connect(config)

            VpnStateStore.update(VpnRuntimeState(vpnState = VpnState.CONNECTED, activeProfileId = profileId))
            updateNotification("VPN подключен")

            if (wantHotspot) {
                ServiceLocator.hotspot.startBestEffort()
            }
        } catch (t: Throwable) {
            val msg = ServiceLocator.vpn.readableError(t)
            VpnStateStore.update(VpnRuntimeState(vpnState = VpnState.ERROR, activeProfileId = profileId, lastError = msg))
            updateNotification("Ошибка: $msg")
        }
    }

    private suspend fun disconnectAndStop() {
        try {
            ServiceLocator.vpn.disconnect()
        } catch (_: Throwable) {
        } finally {
            VpnStateStore.update(VpnRuntimeState(vpnState = VpnState.DISCONNECTED))
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
        }
    }

    private fun updateNotification(text: String) {
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.notify(NOTIF_ID, buildNotification(text))
    }

    private fun buildNotification(text: String) =
        NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_vpn_notification)
            .setContentTitle(getString(R.string.app_name))
            .setContentText(text)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .build()

    private fun ensureChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.createNotificationChannel(
            NotificationChannel(
                CHANNEL_ID,
                "VPN",
                NotificationManager.IMPORTANCE_LOW,
            ),
        )
    }

    private fun persistActiveProfile(profileId: Long) {
        getSharedPreferences(PREFS, MODE_PRIVATE).edit().putLong(KEY_ACTIVE_PROFILE, profileId).apply()
    }

    private fun clearActiveProfile() {
        getSharedPreferences(PREFS, MODE_PRIVATE).edit().remove(KEY_ACTIVE_PROFILE).apply()
    }

    private fun readActiveProfile(): Long? {
        val v = getSharedPreferences(PREFS, MODE_PRIVATE).getLong(KEY_ACTIVE_PROFILE, -1L)
        return if (v > 0) v else null
    }

    companion object {
        private const val CHANNEL_ID = "vpn"
        private const val NOTIF_ID = 1001

        private const val PREFS = "vpn_orchestrator"
        private const val KEY_ACTIVE_PROFILE = "active_profile_id"

        const val ACTION_START = "com.adguard.wireguardhotspotbridge.action.START"
        const val ACTION_STOP = "com.adguard.wireguardhotspotbridge.action.STOP"

        const val EXTRA_PROFILE_ID = "profile_id"
        const val EXTRA_ENABLE_HOTSPOT = "enable_hotspot"
    }
}

