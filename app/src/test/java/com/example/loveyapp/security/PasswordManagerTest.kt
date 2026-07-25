package com.example.loveyapp.security

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class PasswordManagerTest {

    private lateinit var passwordManager: PasswordManager

    @Before
    fun setup() {
        passwordManager = PasswordManager()
    }

    @Test
    fun `hashPassword should generate consistent hash for same password`() {
        val password = "testPassword123"
        val hash1 = passwordManager.hashPassword(password)
        val hash2 = passwordManager.hashPassword(password)
        
        assertEquals(hash1, hash2)
    }

    @Test
    fun `hashPassword should generate different hashes for different passwords`() {
        val password1 = "password1"
        val password2 = "password2"
        
        val hash1 = passwordManager.hashPassword(password1)
        val hash2 = passwordManager.hashPassword(password2)
        
        assertNotEquals(hash1, hash2)
    }

    @Test
    fun `hashPassword should not be empty for valid password`() {
        val password = "test"
        val hash = passwordManager.hashPassword(password)
        
        assertFalse(hash.isEmpty())
    }

    @Test
    fun `hashPassword should handle empty string`() {
        val password = ""
        val hash = passwordManager.hashPassword(password)
        
        assertFalse(hash.isEmpty())
    }

    @Test
    fun `hashPassword should handle special characters`() {
        val password = "!@#$%^&*()_+-=[]{}|;':\",./<>?"
        val hash = passwordManager.hashPassword(password)
        
        assertFalse(hash.isEmpty())
    }

    @Test
    fun `hashPassword should handle unicode characters`() {
        val password = "密码测试测试"
        val hash = passwordManager.hashPassword(password)
        
        assertFalse(hash.isEmpty())
    }

    @Test
    fun `verifyPassword should return true for correct password`() {
        val password = "correctPassword"
        val hash = passwordManager.hashPassword(password)
        
        val result = passwordManager.verifyPassword(password, hash)
        
        assertTrue(result)
    }

    @Test
    fun `verifyPassword should return false for incorrect password`() {
        val correctPassword = "correctPassword"
        val wrongPassword = "wrongPassword"
        val hash = passwordManager.hashPassword(correctPassword)
        
        val result = passwordManager.verifyPassword(wrongPassword, hash)
        
        assertFalse(result)
    }

    @Test
    fun `verifyPassword should return false for empty input`() {
        val password = "password"
        val hash = passwordManager.hashPassword(password)
        
        val result = passwordManager.verifyPassword("", hash)
        
        assertFalse(result)
    }

    @Test
    fun `verifyPassword should return false for mismatched hash`() {
        val password = "password"
        val hash = passwordManager.hashPassword("differentPassword")
        
        val result = passwordManager.verifyPassword(password, hash)
        
        assertFalse(result)
    }
}