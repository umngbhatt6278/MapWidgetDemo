package com.example.mapwidgetdemo.utils

import io.ktor.client.features.*

/**
 * Created by Priyanka.
 */
sealed class Failure {

    object NetworkConnection : Failure()
    class ServerError(val ex: Exception) : Failure()
    class BadRequest(val message: String) : Failure()
    object CustomError : Failure()

    abstract class FeatureFailure : Failure()
    class HttpErrorInternalServerError(exception: ServerResponseException) : Failure()
    class IOExceptionError(error: String) : Failure()
    class HttpErrorBadRequest(exception: ClientRequestException) : Failure()
    class HttpErrorUnauthorized(exception: ClientRequestException) : Failure()
    class HttpErrorForbidden(exception: ClientRequestException) : Failure()
    class HttpErrorNotFound(exception: ClientRequestException) : Failure()
    class HttpError(exception: Exception) : Failure()
    class GenericError(exception: Exception) : Failure()
    class MethodNotAllowed(clientRequestException: ClientRequestException) : Failure()
    class Error() : Failure()
}

