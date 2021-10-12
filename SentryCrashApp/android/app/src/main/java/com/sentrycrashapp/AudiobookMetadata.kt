package com.sentrycrashapp

import kotlinx.serialization.Serializable

@Serializable
data class AudiobookMetadata(
        val contentId: String,
        val mockingjayId: String,
        val title: String,
        val author: String,
        val coverImageUrl: String? = null,
        val downloadStatus: String? = null
)