package com.example.loveyapp.data.repository

import com.example.loveyapp.data.local.database.LoveYaDatabaseFactory
import com.example.loveyapp.data.local.entity.UserInfo
import com.example.loveyapp.security.PasswordManager
import javax.inject.Inject

class UserRepository @Inject constructor(
    private val databaseFactory: LoveYaDatabaseFactory,
    private val passwordManager: PasswordManager
) {
    suspend fun register(username: String, password: String): Boolean {
        return try {
            val passwordHash = passwordManager.hashPassword(password)
            val database = databaseFactory.createDatabase(username)
            database.userInfoDao().insert(
                UserInfo(
                    username = username,
                    passwordHash = passwordHash,
                    userBirthday = "",
                    userGender = "",
                    loverName = "",
                    loverBirthday = "",
                    loverGender = "",
                    anniversaryDate = "",
                    createdAt = System.currentTimeMillis()
                )
            )
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun login(username: String, password: String): Boolean {
        return try {
            val database = databaseFactory.createDatabase(username)
            val userInfo = database.userInfoDao().findByUsername(username)
            userInfo != null && passwordManager.verifyPassword(password, userInfo.passwordHash)
        } catch (e: Exception) {
            false
        }
    }

    suspend fun getUserInfo(username: String): UserInfo? {
        return try {
            val database = databaseFactory.createDatabase(username)
            database.userInfoDao().findByUsername(username)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun updateUserInfo(username: String, userInfo: UserInfo): Boolean {
        return try {
            val database = databaseFactory.createDatabase(username)
            database.userInfoDao().update(userInfo)
            true
        } catch (e: Exception) {
            false
        }
    }
}