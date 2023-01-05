package com.example.mapwidgetdemo.utils

/**
 * Created by Priyanka.
 */
sealed class EitherOr<out L, out M, out R> {

    data class Left<out L>(val a: L) : EitherOr<L, Nothing, Nothing>()

    data class Middle<out M>(val b: M) : EitherOr<Nothing, M, Nothing>()

    data class Right<out R>(val c: R) : EitherOr<Nothing, Nothing, R>()

    suspend fun either(
        fnL: suspend (L) -> Any,
        fnM: suspend (M) -> Any,
        fnR: suspend (R) -> Any
    ): Any =
        when (this) {
            is Left -> fnL(a)
            is Middle -> fnM(b)
            is Right -> fnR(c)
        }
}