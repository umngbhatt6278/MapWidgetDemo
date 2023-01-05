package com.example.mapwidgetdemo.response

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

/**
 * Created by Priyanka.
 */
@Serializable
data class CommonErrorResponse(
    @SerializedName("error") var error: String? = null,
)
