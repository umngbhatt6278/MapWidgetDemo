package com.example.mapwidgetdemo.apicall

import com.example.mapwidgetdemo.request.LoginRequestModel
import com.example.mapwidgetdemo.request.RegisterRequestModel
import com.example.mapwidgetdemo.request.SaveVideoModel
import com.example.mapwidgetdemo.response.LoginResponse
import com.example.mapwidgetdemo.response.SaveVidoResponse
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.http.*
import java.io.File

/**
 * Created by Priyanka.
 */
class ApiService(private val client: HttpClient) {

    suspend fun login(loginRequestModel: LoginRequestModel): LoginResponse = client.post {
        url(ApiRoutes.LOGIN)
        header(HttpHeaders.ContentType, ContentType.Application.Json)
        body = loginRequestModel
    }

    suspend fun register(loginRequestModel: RegisterRequestModel): LoginResponse = client.post {
        url(ApiRoutes.REGISTER)
        header(HttpHeaders.ContentType, ContentType.Application.Json)
        body = loginRequestModel
    }


    suspend fun saveVideo(loginRequestModel: SaveVideoModel): SaveVidoResponse =
        client.submitFormWithBinaryData(url = "api/video", formData = formData {
            append("name", "Sample 1")
            append("lat", loginRequestModel.currentLatitude)
            append("long", loginRequestModel.currentLongitude)
            append("file", File(loginRequestModel.filepath).readBytes(), Headers.build {
                append(HttpHeaders.ContentType, "multipart/form-data; boundary=boundary")
                append(HttpHeaders.Authorization,"Bearer " + "28|i8WyBIEsdhpJYMorOifLb8GR9ri88LX9LyFdDMBB")
                append(HttpHeaders.ContentDisposition, "filename=${File(loginRequestModel.filepath).name}")
            })
        })



}