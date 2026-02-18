package com.adguard.wireguardhotspotbridge

import android.content.Context
import androidx.room.Room
import com.adguard.wireguardhotspotbridge.data.db.AppDatabase
import com.adguard.wireguardhotspotbridge.data.repo.WireGuardProfileRepository
import com.adguard.wireguardhotspotbridge.data.secrets.SecretsStorage
import com.adguard.wireguardhotspotbridge.hotspot.HotspotController
import com.adguard.wireguardhotspotbridge.vpn.WireGuardTunnelController

object ServiceLocator {
    @Volatile
    private var initialized = false

    lateinit var appContext: Context
        private set

    lateinit var db: AppDatabase
        private set

    lateinit var secrets: SecretsStorage
        private set

    lateinit var profiles: WireGuardProfileRepository
        private set

    lateinit var vpn: WireGuardTunnelController
        private set

    lateinit var hotspot: HotspotController
        private set

    fun init(context: Context) {
        if (initialized) return
        synchronized(this) {
            if (initialized) return
            appContext = context.applicationContext
            secrets = SecretsStorage(appContext)
            db = Room.databaseBuilder(appContext, AppDatabase::class.java, "app.db")
                .fallbackToDestructiveMigration()
                .build()
            profiles = WireGuardProfileRepository(db.wireGuardProfileDao(), secrets)
            vpn = WireGuardTunnelController(appContext)
            hotspot = HotspotController(appContext)
            initialized = true
        }
    }
}

