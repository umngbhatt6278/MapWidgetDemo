package com.example.mapwidgetdemo.response

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

@Serializable
data class SaveVidoResponse(val data: VideoData, val message: String, val status: Boolean)

@Serializable
data class VideoData(@SerializedName("video") var video: Mvideo? = null)

@Serializable
data class Mvideo(@SerializedName("id") var id: Int? = null, @SerializedName("name") var name: String? = null, @SerializedName("lat") var lat: String? = null, @SerializedName("long") var long: String? = null, @SerializedName("video") var video: String? = null)