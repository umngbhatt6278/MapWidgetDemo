package com.example.mapwidgetdemo.response

import kotlinx.serialization.Serializable

@Serializable
data class DataX(
    val videos: List<VideoX>
)