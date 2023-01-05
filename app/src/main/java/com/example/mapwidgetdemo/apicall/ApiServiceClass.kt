package com.app.ktorcrud.apicall

import com.example.mapwidgetdemo.request.LoginRequestModel
import com.example.mapwidgetdemo.request.UpdateUserRequest
import com.example.mapwidgetdemo.response.LoginResponse
import com.example.mapwidgetdemo.response.UpdateUserResponse
import com.example.mapwidgetdemo.response.UsersListResponse
import com.example.mapwidgetdemo.utils.Either
import com.google.gson.JsonObject

/**
 * Created by Priyanka.
 */
interface ApiServiceClass {
    suspend fun login(loginRequestModel: LoginRequestModel): Either<String, LoginResponse>
    suspend fun getUserList(page:Int): Either<String, UsersListResponse>
    suspend fun updateUser(page: Int, updateUserRequest: UpdateUserRequest): Either<String, UpdateUserResponse>
    suspend fun deleteUser(page: Int): Either<String, JsonObject>
}