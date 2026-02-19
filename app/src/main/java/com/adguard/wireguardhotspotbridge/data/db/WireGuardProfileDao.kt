package com.adguard.wireguardhotspotbridge.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface WireGuardProfileDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: WireGuardProfileEntity): Long

    @Query("SELECT * FROM wireguard_profiles ORDER BY updatedAtEpochMs DESC")
    fun observeAll(): Flow<List<WireGuardProfileEntity>>

    @Query("SELECT * FROM wireguard_profiles ORDER BY updatedAtEpochMs DESC LIMIT 1")
    suspend fun getLatest(): WireGuardProfileEntity?

    @Query("SELECT * FROM wireguard_profiles WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): WireGuardProfileEntity?
}

