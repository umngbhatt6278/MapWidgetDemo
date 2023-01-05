package com.example.mapwidgetdemo.viewmodel


import androidx.databinding.ObservableField
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mapwidgetdemo.R
import com.example.mapwidgetdemo.apicall.ApiServiceImpl
import com.example.mapwidgetdemo.request.LoginRequestModel
import com.example.mapwidgetdemo.response.Data
import com.example.mapwidgetdemo.response.LoginResponse
import com.example.mapwidgetdemo.utils.AllEvents
import com.example.mapwidgetdemo.utils.validateEmail
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

/**
 * Created by Priyanka.
 */
class LoginViewModel(private val apiServiceImpl: ApiServiceImpl) : ViewModel() {

    val isNetworkAvailable = MutableLiveData<Boolean>()
    var email = ObservableField<String>()
    var password = ObservableField<String>()
    var name = ObservableField<String>()
    var job = ObservableField<String>()
    private val eventsChannel = Channel<AllEvents>()
    val allEventsFlow = eventsChannel.receiveAsFlow()
    private val _loginResponse = MutableLiveData<LoginResponse?>()
    val loginResponse get() = _loginResponse

    private val _userListResponse = MutableLiveData<ArrayList<Data>?>()
    val userListResponse get() = _userListResponse

    fun login() {

        val emailString = email.get()
        val passString = password.get()
        viewModelScope.launch {
            when {
               /* !isNetworkAvailable.value!! -> {
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
                    apiServiceImpl.login(LoginRequestModel(emailString, passString))
                        .either(
                            {
                                eventsChannel.send(AllEvents.Loading(false))
                                eventsChannel.send(AllEvents.DynamicError(it))
                            },
                            {
                                loginResponse.postValue(it)
                                eventsChannel.send(AllEvents.SuccessBool(true, 1))
                            })
                }
            }
        }
    }
}