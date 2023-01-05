package com.example.mapwidgetdemo.apicall

import com.app.ktorcrud.apicall.ApiServiceClass
import com.example.mapwidgetdemo.request.LoginRequestModel
import com.example.mapwidgetdemo.request.UpdateUserRequest
import com.example.mapwidgetdemo.response.CommonErrorResponse
import com.example.mapwidgetdemo.response.LoginResponse
import com.example.mapwidgetdemo.response.UpdateUserResponse
import com.example.mapwidgetdemo.response.UsersListResponse
import com.example.mapwidgetdemo.utils.Either
import com.example.mapwidgetdemo.utils.Failure
import com.google.gson.Gson
import com.google.gson.JsonObject
import io.ktor.client.features.*
import io.ktor.client.statement.*
import io.ktor.utils.io.charsets.*

/**
 * Created by Priyanka.
 */
class ApiServiceImpl(private val apiService: ApiService) : ApiServiceClass {

    override suspend fun login(loginRequestModel: LoginRequestModel): Either<String, LoginResponse> {
        return try {
            Either.Right(apiService.login(loginRequestModel))
        } catch (ex: Exception) {
            Either.Left(ex.errorMessage())
        }
    }

    override suspend fun getUserList(page:Int): Either<String, UsersListResponse> {
        return try {
            Either.Right(apiService.getUsers(page))
        } catch (ex: Exception) {
            Either.Left(ex.errorMessage())
        }
    }

    override suspend fun updateUser(page: Int, updateUserRequest: UpdateUserRequest): Either<String, UpdateUserResponse> {
        return try {
            Either.Right(apiService.updateUsers(page,updateUserRequest))
        } catch (ex: Exception) {
            Either.Left(ex.errorMessage())
        }
    }

    override suspend fun deleteUser(page: Int): Either<String, JsonObject> {
        return try {
            Either.Right(apiService.deleteUsers(page))
        } catch (ex: Exception) {
            Either.Left(ex.errorMessage())
        }
    }

    private suspend fun Exception.errorMessage() =
        when (this) {
            is ResponseException -> {
                Gson().fromJson(
                    response.readText(Charset.defaultCharset()),
                    CommonErrorResponse::class.java
                ).error!!
            }
            else -> {
                localizedMessage!!
            }
        }
}

fun Exception.toCustomExceptions() = when (this) {
    is ServerResponseException -> Failure.HttpErrorInternalServerError(this)
    is ClientRequestException ->
        when (this.response.status.value) {
            400 -> Failure.HttpErrorBadRequest(this)
            401 -> Failure.HttpErrorUnauthorized(this)
            403 -> Failure.HttpErrorForbidden(this)
            404 -> Failure.HttpErrorNotFound(this)
            405 -> Failure.MethodNotAllowed(this)
            else -> Failure.HttpError(this)
        }
    is RedirectResponseException -> Failure.HttpError(this)
    else -> Failure.GenericError(this)
}
