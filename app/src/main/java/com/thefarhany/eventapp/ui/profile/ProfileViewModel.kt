package com.thefarhany.eventapp.ui.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thefarhany.eventapp.data.model.request.PatchUserRequest
import com.thefarhany.eventapp.data.model.request.UpdateUserRequest
import com.thefarhany.eventapp.data.model.response.UserProfile
import com.thefarhany.eventapp.data.repository.UserRepository
import com.thefarhany.eventapp.utils.Resource
import kotlinx.coroutines.launch

class ProfileViewModel(private val repository: UserRepository) : ViewModel() {
    private val _userProfile = MutableLiveData<Resource<UserProfile>>()
    val userProfile: LiveData<Resource<UserProfile>> = _userProfile

    private val _updateResult = MutableLiveData<Resource<UserProfile>?>()
    val updateResult: MutableLiveData<Resource<UserProfile>?> = _updateResult

    /**
     * Load current user profile
     */
    fun loadUserProfile() {
        _userProfile.value = Resource.Loading()

        viewModelScope.launch {
            val result = repository.getMyProfile()
            _userProfile.postValue(result)
        }
    }

    /**
     * Update user profile (PUT - full update)
     */
    fun updateProfile(request: UpdateUserRequest) {
        _updateResult.value = Resource.Loading()

        viewModelScope.launch {
            val result = repository.updateProfile(request)
            _updateResult.postValue(result)

            // Refresh profile after successful update
            if (result is Resource.Success) {
                _userProfile.postValue(result)
            }
        }
    }

    /**
     * Patch user profile (PATCH - partial update)
     */
    fun patchProfile(request: PatchUserRequest) {
        _updateResult.value = Resource.Loading()

        viewModelScope.launch {
            val result = repository.patchProfile(request)
            _updateResult.postValue(result)

            // Refresh profile after successful update
            if (result is Resource.Success) {
                _userProfile.postValue(result)
            }
        }
    }

    /**
     * Clear update result after handling
     */
    fun clearUpdateResult() {
        _updateResult.value = null
    }
}