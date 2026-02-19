package com.adguard.wireguardhotspotbridge.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ikev2_profiles")
data class Ikev2ProfileEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val createdAtEpochMs: Long,
    val updatedAtEpochMs: Long,

    val serverAddress: String,
    val serverId: String,
    val username: String,
    val passwordRef: String,
    val certificateHint: String?,
)

