package com.example.mapwidgetdemo.viewmodel


import androidx.databinding.ObservableField
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mapwidgetdemo.R
import com.example.mapwidgetdemo.apicall.ApiServiceImpl
import com.example.mapwidgetdemo.request.LoginRequestModel
import com.example.mapwidgetdemo.request.RegisterRequestModel
import com.example.mapwidgetdemo.request.SaveVideoModel
import com.example.mapwidgetdemo.response.*
import com.example.mapwidgetdemo.utils.AllEvents
import com.example.mapwidgetdemo.utils.validateEmail
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.io.File

/**
 * Created by Priyanka.
 */
class LoginViewModel(private val apiServiceImpl: ApiServiceImpl) : ViewModel() {

    val isNetworkAvailable = MutableLiveData<Boolean>()
    var email = ObservableField<String>()
    var password = ObservableField<String>()
    var name = ObservableField<String>()
    var confirmPassword = ObservableField<String>()
    private val eventsChannel = Channel<AllEvents>()
    val allEventsFlow = eventsChannel.receiveAsFlow()
    private val _loginResponse = MutableLiveData<LoginResponse?>()
    private val _SaveVideoResponse = MutableLiveData<SaveVidoResponse?>()
    val loginResponse get() = _loginResponse
    val saveVideoResponse get() = _SaveVideoResponse

    private val _userListResponse = MutableLiveData<ArrayList<Data>?>()
    private val _videoListResponse = MutableLiveData<GetVideoResponse>()
    val userListResponse get() = _userListResponse
    val videoListResponse get() = _videoListResponse

    private val _commonResponse = MutableLiveData<CommonErrorResponse?>()
    val commonResponse get() = _commonResponse

    fun login() {

        val emailString = email.get()
        val passString = password.get()

        viewModelScope.launch {
            when {/* !isNetworkAvailable.value!! -> {
                    eventsChannel.send(AllEvents.StringResource(R.string.noInternet))
                }*/
                emailString.isNullOrBlank() -> {
                    eventsChannel.send(
                        AllEvents.StringResource(R.string.enter_email, null)
                    )
                }
                emailString.validateEmail() -> {
                    eventsChannel.send(AllEvents.StringResource(R.string.error_email_invalid))
                }
                passString.isNullOrBlank() -> {
                    eventsChannel.send(AllEvents.StringResource(R.string.error_password_empty))
                }
                else -> {
                    eventsChannel.send(AllEvents.Loading(true))
                    apiServiceImpl.login(LoginRequestModel(emailString, passString)).either({
                        eventsChannel.send(AllEvents.Loading(false))
                        eventsChannel.send(AllEvents.DynamicError(it))
                    }, {
                        loginResponse.postValue(it)
                        eventsChannel.send(AllEvents.Loading(false))
                        eventsChannel.send(AllEvents.SuccessBool(true, 1))
                        eventsChannel.send(AllEvents.Success(it))
                    })
                }
            }
        }
    }

    fun forgotPassword() {

        val emailString = email.get()

        viewModelScope.launch {
            when {/* !isNetworkAvailable.value!! -> {
                    eventsChannel.send(AllEvents.StringResource(R.string.noInternet))
                }*/
                emailString.isNullOrBlank() -> {
                    eventsChannel.send(
                        AllEvents.StringResource(R.string.enter_email, null)
                    )
                }
                emailString.validateEmail() -> {
                    eventsChannel.send(AllEvents.StringResource(R.string.error_email_invalid))
                }
                else -> {
                    eventsChannel.send(AllEvents.Loading(true))
                    apiServiceImpl.forgotpassword(LoginRequestModel(email = emailString)).either({
                        eventsChannel.send(AllEvents.Loading(false))
                        eventsChannel.send(AllEvents.DynamicError(it))
                    }, {
                        _commonResponse.postValue(it)
                        eventsChannel.send(AllEvents.Loading(false))
                        eventsChannel.send(AllEvents.SuccessBool(true, 1))
                        eventsChannel.send(AllEvents.Success(it))
                    })
                }
            }
        }
    }

    fun register() {
        val nameString = name.get()
        val emailString = email.get()
        val passString = password.get()
        val confirmpassString = confirmPassword.get()

        viewModelScope.launch {
            when {/* !isNetworkAvailable.value!! -> {
                     eventsChannel.send(AllEvents.StringResource(R.string.noInternet))
                 }*/
                nameString.isNullOrBlank() -> {
                    eventsChannel.send(
                        AllEvents.StringResource(R.string.enter_name, null)
                    )
                }
                emailString.isNullOrBlank() -> {
                    eventsChannel.send(
                        AllEvents.StringResource(R.string.enter_email, null)
                    )
                }
                emailString.validateEmail() -> {
                    eventsChannel.send(AllEvents.StringResource(R.string.error_email_invalid))
                }
                passString.isNullOrBlank() -> {
                    eventsChannel.send(AllEvents.StringResource(R.string.error_password_empty))
                }
                confirmpassString.isNullOrBlank() -> {
                    eventsChannel.send(AllEvents.StringResource(R.string.error_conf_password_empty))
                }
                !confirmpassString.equals(passString) -> {
                    eventsChannel.send(AllEvents.StringResource(R.string.error_conf_password_not_match))
                }
                else -> {
                    eventsChannel.send(AllEvents.Loading(true))
                    apiServiceImpl.register(RegisterRequestModel(nameString, emailString, passString, confirmpassString)).either({
                        eventsChannel.send(AllEvents.Loading(false))
                        eventsChannel.send(AllEvents.DynamicError(it))
                    }, {
                        loginResponse.postValue(it)
                        eventsChannel.send(AllEvents.Loading(false))
                        eventsChannel.send(AllEvents.SuccessBool(true, 1))
                    })
                }
            }
        }
    }

    fun saveVideo(currentLatitude: Double, currentLongitude: Double, name: String, filepath: String) {
        viewModelScope.launch {
            when {
                else -> {
                    eventsChannel.send(AllEvents.Loading(false))
                    apiServiceImpl.saveVideo(SaveVideoModel(currentLatitude.toString(), currentLongitude.toString(), name, filepath)).either({
                        eventsChannel.send(AllEvents.Loading(false))
                        eventsChannel.send(AllEvents.DynamicError(it))
                    }, {
                        saveVideoResponse.postValue(it)
                        eventsChannel.send(AllEvents.Loading(false))
                        eventsChannel.send(AllEvents.SuccessBool(true, 1))
                    })
                }
            }
        }
    }

    fun getVideosFromApi() {
        viewModelScope.launch {
            when {
                else -> {
                    apiServiceImpl.getVideo().either({
                        eventsChannel.send(AllEvents.DynamicError(it))
                    }, {
                        videoListResponse.postValue(it)
                        eventsChannel.send(AllEvents.Loading(false))
                        eventsChannel.send(AllEvents.SuccessBool(true, 1))
                        eventsChannel.send(AllEvents.Success(it))
                    })
                }
            }
        }
    }

}