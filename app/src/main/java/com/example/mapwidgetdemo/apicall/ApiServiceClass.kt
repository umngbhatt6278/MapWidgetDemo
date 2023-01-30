package com.app.ktorcrud.apicall

import com.example.mapwidgetdemo.request.EditMarkerRequestModel
import com.example.mapwidgetdemo.request.LoginRequestModel
import com.example.mapwidgetdemo.request.RegisterRequestModel
import com.example.mapwidgetdemo.request.SaveVideoModel
import com.example.mapwidgetdemo.response.CommonErrorResponse
import com.example.mapwidgetdemo.response.GetVideoResponse
import com.example.mapwidgetdemo.response.LoginResponse
import com.example.mapwidgetdemo.response.SaveVidoResponse
import com.example.mapwidgetdemo.utils.Either

/**
 * Created by Priyanka.
 */
interface ApiServiceClass {
    suspend fun login(loginRequestModel: LoginRequestModel): Either<String, LoginResponse>
    suspend fun editMarker(loginRequestModel: EditMarkerRequestModel): Either<String, CommonErrorResponse>
    suspend fun forgotpassword(loginRequestModel: LoginRequestModel): Either<String, CommonErrorResponse>
    suspend fun register(loginRequestModel: RegisterRequestModel): Either<String, LoginResponse>
    suspend fun saveVideo(loginRequestModel: SaveVideoModel): Either<String, SaveVidoResponse>
    suspend fun getVideo(): Either<String, GetVideoResponse>
}