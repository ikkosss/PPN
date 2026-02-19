package com.adguard.wireguardhotspotbridge.hotspot

import android.content.Context
import android.content.Intent
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
        val intent = Intent(ACTION_TETHER_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun startViaTetheringManager(): Boolean {
        return try {
            val tmClass = Class.forName("android.net.TetheringManager")
            val tetheringManager = appContext.getSystemService(tmClass) ?: return false

            val tetheringTypeWifi = runCatching { tmClass.getField("TETHERING_WIFI").getInt(null) }.getOrNull() ?: 0

            val callbackObj: Any? = runCatching {
                val cbClass = Class.forName("android.net.TetheringManager\$StartTetheringCallback")
                cbClass.getDeclaredConstructor().newInstance()
            }.getOrNull()

            val startMethod = tmClass.methods.firstOrNull { m -> m.name == "startTethering" } ?: return false

            HotspotStateStore.update(
                HotspotRuntimeState(
                    requestedOn = true,
                    method = HotspotControlMethod.TETHERING_MANAGER,
                ),
            )

            val handler = Handler(Looper.getMainLooper())
            runCatching {
                when (startMethod.parameterTypes.size) {
                    4 -> startMethod.invoke(tetheringManager, tetheringTypeWifi, executor, callbackObj, handler)
                    3 -> startMethod.invoke(tetheringManager, tetheringTypeWifi, executor, callbackObj)
                    else -> return false
                }
            }.getOrElse { return false }
            true
        } catch (t: Throwable) {
            Log.w("HotspotController", "startViaTetheringManager failed", t)
            false
        }
    }

    private companion object {
        // Use literal to avoid compile-time API differences.
        private const val ACTION_TETHER_SETTINGS = "android.settings.TETHER_SETTINGS"
    }
}

