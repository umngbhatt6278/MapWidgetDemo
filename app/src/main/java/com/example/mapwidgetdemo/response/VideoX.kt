package com.example.mapwidgetdemo.response

import kotlinx.serialization.Serializable

@Serializable
data class VideoX(val id: Int, val lat: Double, val long: Double, val name: String, val video: String)