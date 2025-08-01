package com.example.myapplication1.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication1.data.User
import com.example.myapplication1.data.UserDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@HiltViewModel
class UserViewModel @Inject constructor(
    private val userDao: UserDao
) : ViewModel() {
    
    val allUsers = userDao.getAllUsers()
    val currentUser = userDao.getCurrentUser()
    
    fun initializeDefaultUsers() {
        viewModelScope.launch {
            val existingUser = userDao.getCurrentUser()
            if (existingUser == null) {
                // 创建默认用户
                val defaultUser = User(
                    id = UUID.randomUUID().toString(),
                    name = "我",
                    isCurrentUser = true
                )
                userDao.insertUser(defaultUser)
            }
        }
    }
    
    fun addUser(name: String) {
        viewModelScope.launch {
            val newUser = User(
                id = UUID.randomUUID().toString(),
                name = name,
                isCurrentUser = false
            )
            userDao.insertUser(newUser)
        }
    }
    
    fun updateUser(user: User) {
        viewModelScope.launch {
            userDao.updateUser(user)
        }
    }
    
    fun deleteUser(user: User) {
        viewModelScope.launch {
            userDao.deleteUser(user)
        }
    }
    
    fun setCurrentUser(userId: String) {
        viewModelScope.launch {
            userDao.clearCurrentUser()
            userDao.setCurrentUser(userId)
        }
    }
}