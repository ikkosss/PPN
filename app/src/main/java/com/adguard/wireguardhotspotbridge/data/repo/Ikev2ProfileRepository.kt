package com.adguard.wireguardhotspotbridge.data.repo

import com.adguard.wireguardhotspotbridge.data.db.Ikev2ProfileDao
import com.adguard.wireguardhotspotbridge.data.db.Ikev2ProfileEntity
import com.adguard.wireguardhotspotbridge.data.secrets.SecretsStorage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

class Ikev2ProfileRepository(
    private val dao: Ikev2ProfileDao,
    private val secrets: SecretsStorage,
) {
    suspend fun save(
        name: String,
        serverAddress: String,
        serverId: String,
        username: String,
        password: String,
        certificateHint: String?,
    ): Long {
        require(serverAddress.isNotBlank()) { "IP-адрес/хост пуст" }
        require(serverId.isNotBlank()) { "ID сервера пуст" }
        require(username.isNotBlank()) { "Имя пользователя пусто" }
        require(password.isNotBlank()) { "Пароль пуст" }

        val now = System.currentTimeMillis()
        val secretId = UUID.randomUUID().toString()
        val passRef = "ikev2_pass_$secretId"

        val entity = Ikev2ProfileEntity(
            id = 0,
            name = name.ifBlank { "AdGuard IKEv2" },
            createdAtEpochMs = now,
            updatedAtEpochMs = now,
            serverAddress = serverAddress.trim(),
            serverId = serverId.trim(),
            username = username.trim(),
            passwordRef = passRef,
            certificateHint = certificateHint?.trim()?.ifBlank { null },
        )
        val id = dao.upsert(entity)
        secrets.put(passRef, password)
        return id
    }

    suspend fun getLatest(): Ikev2Profile? = dao.getLatest()?.toDomain()

    fun observeAll(): Flow<List<Ikev2Profile>> =
        dao.observeAll().map { list -> list.map { it.toDomain() } }

    suspend fun getPassword(profile: Ikev2Profile): String? = secrets.get(profile.passwordRef)

    private fun Ikev2ProfileEntity.toDomain(): Ikev2Profile =
        Ikev2Profile(
            id = id,
            name = name,
            serverAddress = serverAddress,
            serverId = serverId,
            username = username,
            passwordRef = passwordRef,
            certificateHint = certificateHint,
            updatedAtEpochMs = updatedAtEpochMs,
        )
}

data class Ikev2Profile(
    val id: Long,
    val name: String,
    val serverAddress: String,
    val serverId: String,
    val username: String,
    val passwordRef: String,
    val certificateHint: String?,
    val updatedAtEpochMs: Long,
)

