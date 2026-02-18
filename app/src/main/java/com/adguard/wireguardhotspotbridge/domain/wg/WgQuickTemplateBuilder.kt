package com.adguard.wireguardhotspotbridge.domain.wg

object WgQuickTemplateBuilder {
    private const val PRIVATE_PLACEHOLDER = "__PRIVATE_KEY__"
    private const val PSK_PLACEHOLDER = "__PRESHARED_KEY__"

    fun build(wgQuickConfig: String): WgQuickTemplate {
        var section: String? = null
        var privateKey: String? = null
        var presharedKey: String? = null

        val out = StringBuilder()
        val lines = wgQuickConfig.splitToSequence("\n")
        for (rawLine in lines) {
            val lineNoCr = rawLine.removeSuffix("\r")
            val trimmed = lineNoCr.trim()
            val lower = trimmed.lowercase()

            if (lower.startsWith("[") && lower.endsWith("]")) {
                section = lower
                out.append(lineNoCr).append('\n')
                continue
            }

            val eqIdx = lineNoCr.indexOf('=')
            if (eqIdx == -1 || section == null) {
                out.append(lineNoCr).append('\n')
                continue
            }

            val key = lineNoCr.substring(0, eqIdx).trim()
            val valueAndComment = lineNoCr.substring(eqIdx + 1)
            val value = valueAndComment.substringBefore('#').trim()

            when {
                section.equals("[interface]", ignoreCase = true) && key.equals("PrivateKey", ignoreCase = true) -> {
                    require(privateKey == null) { "Multiple PrivateKey entries are not supported in MVP" }
                    require(value.isNotBlank()) { "PrivateKey is empty" }
                    privateKey = value
                    out.append(lineNoCr.substring(0, eqIdx + 1))
                        .append(' ')
                        .append(PRIVATE_PLACEHOLDER)
                        .append('\n')
                }
                section.equals("[peer]", ignoreCase = true) && key.equals("PresharedKey", ignoreCase = true) -> {
                    require(presharedKey == null) { "Multiple PresharedKey entries are not supported in MVP" }
                    require(value.isNotBlank()) { "PresharedKey is empty" }
                    presharedKey = value
                    out.append(lineNoCr.substring(0, eqIdx + 1))
                        .append(' ')
                        .append(PSK_PLACEHOLDER)
                        .append('\n')
                }
                else -> out.append(lineNoCr).append('\n')
            }
        }

        val pk = requireNotNull(privateKey) { "PrivateKey not found in [Interface]" }
        return WgQuickTemplate(
            templateText = out.toString().trimEnd(),
            privateKey = pk,
            presharedKey = presharedKey,
        )
    }

    fun fill(templateText: String, privateKey: String, presharedKey: String?): String {
        var filled = templateText.replace(PRIVATE_PLACEHOLDER, privateKey)
        if (filled.contains(PSK_PLACEHOLDER)) {
            require(!presharedKey.isNullOrBlank()) { "Template needs PresharedKey, but it is missing" }
            filled = filled.replace(PSK_PLACEHOLDER, presharedKey)
        }
        return filled
    }
}

