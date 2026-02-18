package com.adguard.wireguardhotspotbridge.data.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [WireGuardProfileEntity::class],
    version = 2,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun wireGuardProfileDao(): WireGuardProfileDao
}

