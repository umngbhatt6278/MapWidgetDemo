package com.example.mapwidgetdemo.response

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

/**
 * Created by Priyanka.
 */
@Serializable
data class CommonErrorResponse(
    @SerializedName("status") var status: String? = null,
    @SerializedName("message") var message: String? = null,
)
