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

    /**
     * 修改用户名：检查新用户名不重复后，更新 user_info 表的 username 字段，
     * 并重命名该用户的数据库文件（db/db-shm/db-wal）。
     * 返回是否修改成功。
     */
    suspend fun updateUsername(oldUsername: String, newUsername: String): Boolean {
        return try {
            if (newUsername.isBlank() || newUsername == oldUsername) return false
            // 新用户名对应的数据库文件已存在则视为重复
            if (databaseFactory.userExists(newUsername)) return false
            val database = databaseFactory.createDatabase(oldUsername)
            database.userInfoDao().updateUsername(oldUsername, newUsername)
            // 改用户名需重命名数据库文件，否则下次登录会创建空库
            databaseFactory.renameUser(oldUsername, newUsername)
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * 修改密码：更新指定用户的 passwordHash 字段。
     * 返回是否修改成功。
     */
    suspend fun updatePassword(username: String, newPassword: String): Boolean {
        return try {
            val database = databaseFactory.createDatabase(username)
            val userInfo = database.userInfoDao().findByUsername(username) ?: return false
            val updated = userInfo.copy(passwordHash = passwordManager.hashPassword(newPassword))
            database.userInfoDao().update(updated)
            true
        } catch (e: Exception) {
            false
        }
    }

    /** 校验明文密码是否匹配指定用户。 */
    suspend fun verifyUser(username: String, password: String): Boolean {
        return try {
            val database = databaseFactory.createDatabase(username)
            val userInfo = database.userInfoDao().findByUsername(username)
            userInfo != null && passwordManager.verifyPassword(password, userInfo.passwordHash)
        } catch (e: Exception) {
            false
        }
    }
}