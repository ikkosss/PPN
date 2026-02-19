package com.adguard.wireguardhotspotbridge.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface Ikev2ProfileDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: Ikev2ProfileEntity): Long

    @Query("SELECT * FROM ikev2_profiles ORDER BY updatedAtEpochMs DESC LIMIT 1")
    suspend fun getLatest(): Ikev2ProfileEntity?

    @Query("SELECT * FROM ikev2_profiles ORDER BY updatedAtEpochMs DESC")
    fun observeAll(): Flow<List<Ikev2ProfileEntity>>
}

