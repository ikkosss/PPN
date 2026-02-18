package com.adguard.wireguardhotspotbridge.hotspot

import android.content.Context
import android.content.Intent
import android.net.TetheringManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import androidx.annotation.RequiresApi
import java.util.concurrent.Executors

class HotspotController(private val appContext: Context) {
    private val executor = Executors.newSingleThreadExecutor()

    fun startBestEffort() {
        HotspotStateStore.update(HotspotRuntimeState(requestedOn = true, method = HotspotControlMethod.SETTINGS_FALLBACK))
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val ok = startViaTetheringManager()
            if (ok) return
        }
        HotspotStateStore.update(
            HotspotRuntimeState(
                requestedOn = true,
                method = HotspotControlMethod.SETTINGS_FALLBACK,
                lastError = "Нельзя включить программно без привилегий ОС — включите вручную в настройках",
            ),
        )
    }

    fun openSystemHotspotSettings(context: Context = appContext) {
        val intent = Intent(Settings.ACTION_TETHER_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun startViaTetheringManager(): Boolean {
        return try {
            val tetheringManager = appContext.getSystemService(TetheringManager::class.java) ?: return false

            val callback = object : TetheringManager.StartTetheringCallback() {
                override fun onTetheringStarted() {
                    HotspotStateStore.update(
                        HotspotRuntimeState(
                            requestedOn = true,
                            method = HotspotControlMethod.TETHERING_MANAGER,
                        ),
                    )
                }

                override fun onTetheringFailed(error: Int) {
                    HotspotStateStore.update(
                        HotspotRuntimeState(
                            requestedOn = true,
                            method = HotspotControlMethod.SETTINGS_FALLBACK,
                            lastError = "TetheringManager: не удалось (ошибка $error) — включите вручную",
                        ),
                    )
                }
            }

            val startMethod = tetheringManager::class.java.methods
                .firstOrNull { m -> m.name == "startTethering" && m.parameterTypes.isNotEmpty() }
                ?: return false

            HotspotStateStore.update(
                HotspotRuntimeState(
                    requestedOn = true,
                    method = HotspotControlMethod.TETHERING_MANAGER,
                ),
            )

            val handler = Handler(Looper.getMainLooper())
            when (startMethod.parameterTypes.size) {
                4 -> startMethod.invoke(tetheringManager, TetheringManager.TETHERING_WIFI, executor, callback, handler)
                3 -> startMethod.invoke(tetheringManager, TetheringManager.TETHERING_WIFI, executor, callback)
                else -> return false
            }
            true
        } catch (t: Throwable) {
            Log.w("HotspotController", "startViaTetheringManager failed", t)
            false
        }
    }
}

