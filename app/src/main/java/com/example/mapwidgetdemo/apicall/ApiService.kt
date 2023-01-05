package com.example.mapwidgetdemo.apicall

import com.example.mapwidgetdemo.request.LoginRequestModel
import com.example.mapwidgetdemo.request.UpdateUserRequest
import com.example.mapwidgetdemo.response.LoginResponse
import com.example.mapwidgetdemo.response.UpdateUserResponse
import com.example.mapwidgetdemo.response.UsersListResponse
import com.google.gson.JsonObject
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.http.*

/**
 * Created by Priyanka.
 */
class ApiService(private val client: HttpClient) {

    suspend fun getUsers(page: Int): UsersListResponse = client.get {
        url(ApiRoutes.USERS)
        parameter("page", page)
        header(HttpHeaders.ContentType, ContentType.Application.Json)
    }

    suspend fun updateUsers(id: Int, updateUserRequest: UpdateUserRequest): UpdateUserResponse =
        client.put {
            url(ApiRoutes.UUSERS)
            parameter("user_id", id)
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            body = updateUserRequest
        }

    suspend fun deleteUsers(id: Int): JsonObject = client.delete() {
        url(ApiRoutes.DUSERS)
        parameter("user_id", id)
        header(HttpHeaders.ContentType, ContentType.Application.Json)
    }

    suspend fun login(loginRequestModel: LoginRequestModel): LoginResponse = client.post {
        url(ApiRoutes.LOGIN)
        header(HttpHeaders.ContentType, ContentType.Application.Json)
        body = loginRequestModel
    }


    /* suspend fun login(loginRequestModel: LoginRequestModel): LoginResponse =
         client.submitForm(url = ApiRoutes.LOGIN, formParameters = Parameters.build {
             append("email", loginRequestModel.email)
             append("password", loginRequestModel.password)
         })*/
}