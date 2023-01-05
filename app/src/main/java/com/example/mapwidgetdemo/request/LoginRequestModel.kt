package com.example.mapwidgetdemo.request

@kotlinx.serialization.Serializable
data class LoginRequestModel(val email: String, val password: String)