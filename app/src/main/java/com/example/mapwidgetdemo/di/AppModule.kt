package com.example.mapwidgetdemo.di

import android.app.Application
import com.example.mapwidgetdemo.apicall.ApiService
import com.example.mapwidgetdemo.apicall.ApiServiceImpl
import com.example.mapwidgetdemo.viewmodel.LoginViewModel
import org.koin.dsl.module

/**
 * Created by Priyanka.
 */

val appModule = module {
    single { provideApiService() }
    single { provideApiServiceImpl(get()) }
    factory { LoginViewModel(get()) }
}


fun provideApiService() = ApiService(ktorHttpClient)
fun provideApiServiceImpl(apiService: ApiService) = ApiServiceImpl(apiService)
fun provideViewModel(apiService: ApiServiceImpl, context: Application) =
    LoginViewModel(apiService)

