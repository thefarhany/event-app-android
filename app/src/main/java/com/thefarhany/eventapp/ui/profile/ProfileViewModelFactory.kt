package com.thefarhany.eventapp.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.thefarhany.eventapp.data.repository.UserRepository

class ProfileViewModelFactory(
    private val repository: UserRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
            return ProfileViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}