package dev.robocode.tankroyale.bootstrap.model

import kotlinx.serialization.Serializable

@Serializable
data class BotInfo(
        val name: String,
        val version: String,
        val author: String,
        val description: String? = null,
        val countryCode: String? = null,
        val platform: String? = null,
        val programmingLang: String? = null,
        val gameTypes: Set<String>
)