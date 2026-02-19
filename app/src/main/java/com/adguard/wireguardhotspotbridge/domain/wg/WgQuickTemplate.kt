package com.adguard.wireguardhotspotbridge.domain.wg

data class WgQuickTemplate(
    val templateText: String,
    val privateKey: String,
    val presharedKey: String?,
)

