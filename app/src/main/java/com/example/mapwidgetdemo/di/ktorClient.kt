package com.example.mapwidgetdemo.di

import android.util.Log
import com.example.mapwidgetdemo.apicall.ApiRoutes
import com.example.mapwidgetdemo.utils.SharedPreferenceUtils
import com.example.mapwidgetdemo.utils.AppConstants.SharedPreferenceKeys.F_TOKEN
import io.ktor.client.*
import io.ktor.client.engine.android.*
import io.ktor.client.features.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import io.ktor.client.features.logging.*
import io.ktor.client.features.observer.*
import io.ktor.client.request.*
import io.ktor.http.*

/**
 * Created by Priyanka.
 */
private const val TIME_OUT = 60_00000

val ktorHttpClient = HttpClient(Android) {

    defaultRequest {
        host = ApiRoutes.BASE_URL
        url {
            protocol = URLProtocol.HTTP
        }
    }

    install(JsonFeature) {
        serializer = KotlinxSerializer(kotlinx.serialization.json.Json {
            prettyPrint = true
            isLenient = true
            ignoreUnknownKeys = true
        })

        engine {
            connectTimeout = TIME_OUT
            socketTimeout = TIME_OUT
        }
    }

    install(Logging) {
        logger = object : Logger {
            override fun log(message: String) {
                Log.v("Logger Ktor =>", message)
            }

        }
        level = LogLevel.ALL
    }

    install(ResponseObserver) {
        onResponse { response ->
            Log.d("HTTP status:", "${response.status.value}")
        }
    }

    install(DefaultRequest) {
        header(
            HttpHeaders.Authorization, "Bearer " + if (SharedPreferenceUtils.preferenceGetString(F_TOKEN).toString().isEmpty()) "" else SharedPreferenceUtils.preferenceGetString(F_TOKEN).toString()
        )
    }
}
