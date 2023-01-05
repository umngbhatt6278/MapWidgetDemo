package com.example.mapwidgetdemo.response

import kotlinx.serialization.Serializable


@Serializable
data class LoginResponse(val data: Datam, val message: String, val status: Boolean)

@Serializable
data class Datam(val created_at: String, val email: String, val id: Int, val name: String, val token: String, val updated_at: String)