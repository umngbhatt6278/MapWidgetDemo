package com.example.mapwidgetdemo.response

/**
 * Created by Priyanka.
 */
@kotlinx.serialization.Serializable
data class UpdateUserResponse(
    val name: String,
    val job: String,
    val updatedAt: String
)
