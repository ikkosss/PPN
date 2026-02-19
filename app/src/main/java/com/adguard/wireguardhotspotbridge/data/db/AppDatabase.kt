package com.adguard.wireguardhotspotbridge.data.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        WireGuardProfileEntity::class,
        Ikev2ProfileEntity::class,
    ],
    version = 3,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun wireGuardProfileDao(): WireGuardProfileDao
    abstract fun ikev2ProfileDao(): Ikev2ProfileDao
}

