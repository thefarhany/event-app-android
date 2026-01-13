package com.thefarhany.eventapp.ui.auth.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thefarhany.eventapp.data.model.request.LoginRequest
import com.thefarhany.eventapp.data.model.request.RegisterRequest
import com.thefarhany.eventapp.data.repository.AuthRepository
import com.thefarhany.eventapp.utils.Resource
import kotlinx.coroutines.launch

class AuthViewModel(private val repository: AuthRepository) : ViewModel() {

    private val _registerResult = MutableLiveData<Resource<String>>()
    val registerResult: LiveData<Resource<String>> = _registerResult

    private val _loginResult = MutableLiveData<Resource<Unit>>()
    val loginResult: LiveData<Resource<Unit>> = _loginResult

    fun register(
        firstName: String,
        lastName: String,
        userName: String,
        email: String,
        phoneNumber: String,
        password: String,
        confirmPassword: String
    ) {
        _registerResult.value = Resource.Loading()

        viewModelScope.launch {
            val request = RegisterRequest(
                firstName = firstName,
                lastName = lastName,
                userName = userName,
                email = email,
                phoneNumber = phoneNumber,
                password = password,
                confirmPassword = confirmPassword
            )

            val result = repository.register(request)
            _registerResult.postValue(result)
        }
    }

    fun login(email: String, password: String) {
        _loginResult.value = Resource.Loading()

        viewModelScope.launch {
            val request = LoginRequest(email, password)
            val result = repository.login(request)
            _loginResult.postValue(result)
        }
    }
}