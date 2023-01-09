package com.app.ktorcrud.apicall

import com.example.mapwidgetdemo.request.LoginRequestModel
import com.example.mapwidgetdemo.request.RegisterRequestModel
import com.example.mapwidgetdemo.request.SaveVideoModel
import com.example.mapwidgetdemo.response.LoginResponse
import com.example.mapwidgetdemo.response.SaveVidoResponse
import com.example.mapwidgetdemo.utils.Either

/**
 * Created by Priyanka.
 */
interface ApiServiceClass {
    suspend fun login(loginRequestModel: LoginRequestModel): Either<String, LoginResponse>
    suspend fun register(loginRequestModel: RegisterRequestModel): Either<String, LoginResponse>
    suspend fun saveVideo(loginRequestModel: SaveVideoModel): Either<String, SaveVidoResponse>
}