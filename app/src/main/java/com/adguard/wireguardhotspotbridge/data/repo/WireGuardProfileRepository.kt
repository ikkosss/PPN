package com.adguard.wireguardhotspotbridge.data.repo

import com.adguard.wireguardhotspotbridge.data.db.WireGuardProfileDao
import com.adguard.wireguardhotspotbridge.data.db.WireGuardProfileEntity
import com.adguard.wireguardhotspotbridge.data.secrets.SecretsStorage
import com.adguard.wireguardhotspotbridge.domain.wg.WgQuickTemplate
import com.adguard.wireguardhotspotbridge.domain.wg.WgQuickTemplateBuilder
import com.wireguard.config.BadConfigException
import com.wireguard.config.Config
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.io.ByteArrayInputStream
import java.util.UUID

class WireGuardProfileRepository(
    private val dao: WireGuardProfileDao,
    private val secrets: SecretsStorage,
) {
    suspend fun save(name: String, wgQuickConfig: String): Long {
        parse(wgQuickConfig) // validate
        val template: WgQuickTemplate = WgQuickTemplateBuilder.build(wgQuickConfig)

        val now = System.currentTimeMillis()
        val secretId = UUID.randomUUID().toString()
        val privateRef = "private_$secretId"
        val pskRef = template.presharedKey?.let { "psk_$secretId" }
        val entity = WireGuardProfileEntity(
            id = 0,
            name = name.ifBlank { "AdGuard" },
            createdAtEpochMs = now,
            updatedAtEpochMs = now,
            wgQuickRedacted = template.templateText,
            privateKeyRef = privateRef,
            presharedKeyRef = pskRef,
        )
        val id = dao.upsert(entity)
        secrets.put(privateRef, template.privateKey)
        if (template.presharedKey != null && pskRef != null) secrets.put(pskRef, template.presharedKey)
        return id
    }

    suspend fun getLatest(): WireGuardProfile? = dao.getLatest()?.toDomain()

    suspend fun getById(id: Long): WireGuardProfile? = dao.getById(id)?.toDomain()

    fun observeAll(): Flow<List<WireGuardProfile>> =
        dao.observeAll().map { list -> list.map { it.toDomain() } }

    suspend fun buildConfigTextForTunnel(profileId: Long): String {
        val entity = dao.getById(profileId) ?: error("Profile not found")
        val privateKey = secrets.get(entity.privateKeyRef) ?: error("PrivateKey secret missing")
        val presharedKey = entity.presharedKeyRef?.let { secrets.get(it) }
        return WgQuickTemplateBuilder.fill(
            templateText = entity.wgQuickRedacted,
            privateKey = privateKey,
            presharedKey = presharedKey,
        )
    }

    fun parse(wgQuickConfig: String): Config {
        try {
            val bytes = wgQuickConfig.toByteArray(Charsets.UTF_8)
            return Config.parse(ByteArrayInputStream(bytes))
        } catch (e: BadConfigException) {
            throw IllegalArgumentException(e.message ?: "Bad WireGuard config", e)
        }
    }

    private fun WireGuardProfileEntity.toDomain(): WireGuardProfile =
        WireGuardProfile(
            id = id,
            name = name,
            wgQuickRedacted = wgQuickRedacted,
            updatedAtEpochMs = updatedAtEpochMs,
        )
}

data class WireGuardProfile(
    val id: Long,
    val name: String,
    val wgQuickRedacted: String,
    val updatedAtEpochMs: Long,
)

