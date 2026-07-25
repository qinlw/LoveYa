package com.example.loveyapp.security

import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.Base64
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PasswordManager @Inject constructor() {
    fun hashPassword(password: String): String {
        return try {
            val digest = MessageDigest.getInstance("SHA-256")
            val hash = digest.digest(password.toByteArray(Charsets.UTF_8))
            Base64.getEncoder().encodeToString(hash)
        } catch (e: NoSuchAlgorithmException) {
            throw RuntimeException("SHA-256 algorithm not found", e)
        }
    }

    fun verifyPassword(inputPassword: String, storedHash: String): Boolean {
        val inputHash = hashPassword(inputPassword)
        return inputHash == storedHash
    }
}