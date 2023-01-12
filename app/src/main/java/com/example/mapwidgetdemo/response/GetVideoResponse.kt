package com.example.mapwidgetdemo.response

import kotlinx.serialization.Serializable

@Serializable
data class GetVideoResponse(val data: DataX, val message: String, val status: Boolean)