package com.example.mapwidgetdemo.request

import java.io.File


@kotlinx.serialization.Serializable
data class LoginRequestModel(val email: String, val password: String = "")

@kotlinx.serialization.Serializable
data class RegisterRequestModel(val name: String, val email: String, val password: String, val password_confirmation: String)

@kotlinx.serialization.Serializable
data class SaveVideoModel(val currentLatitude: String, val currentLongitude: String, val name: String, val filepath: String)

@kotlinx.serialization.Serializable
data class EditMarkerRequestModel(val id: String, val name: String = "", val lat: String = "", val long: String = "")