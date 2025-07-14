package com.example.onlyone.viewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.onlyone.data.User
import com.example.onlyone.repos.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class UserViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _user = MutableLiveData<User>()
    val user: LiveData<User> get() = _user

    /*fun loadUser(uid: String) {
        userRepository.getUser(uid).addOnSuccessListener {
            _user.value = it.toObject(User::class.java)
        }
    }

    fun updateMood(mood: String) {
        _user.value?.uid?.let {
            userRepository.updateMood(it, mood)
        }
    }

    fun blockUser(blockedUid: String) {
        _user.value?.uid?.let {
            userRepository.blockUser(it, blockedUid)
        }
    }*/
}
