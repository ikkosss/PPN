package com.adguard.wireguardhotspotbridge.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "wireguard_profiles")
data class WireGuardProfileEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val createdAtEpochMs: Long,
    val updatedAtEpochMs: Long,

    // Stored without secrets (for UI / audit)
    val wgQuickRedacted: String,

    // References to secrets stored in EncryptedSharedPreferences (Jetpack Security + Keystore).
    val privateKeyRef: String,
    val presharedKeyRef: String?,
)

