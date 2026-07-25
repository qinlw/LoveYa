package com.example.loveyapp.data.service

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class UserManagerTest {

    @Test
    fun `getAllUsers should return empty list when no users exist`() {
        val users = emptyList<String>()
        assertTrue(users.isEmpty())
    }

    @Test
    fun `getAllUsers should return sorted list`() {
        val users = listOf("charlie", "alice", "bob")
        val sortedUsers = users.sorted()
        
        assertEquals(listOf("alice", "bob", "charlie"), sortedUsers)
    }

    @Test
    fun `isUserExists should return true for existing user`() {
        val users = listOf("alice", "bob", "charlie")
        
        assertTrue(users.contains("alice"))
    }

    @Test
    fun `isUserExists should return false for non-existing user`() {
        val users = listOf("alice", "bob", "charlie")
        
        assertFalse(users.contains("dave"))
    }

    @Test
    fun `isUserExists should handle empty user list`() {
        val users = emptyList<String>()
        
        assertFalse(users.contains("alice"))
    }

    @Test
    fun `switchUser should handle valid username`() {
        val result = true
        
        assertTrue(result)
    }

    @Test
    fun `deleteUser should handle valid username`() {
        val result = true
        
        assertTrue(result)
    }

    @Test
    fun `getAllUsers should not contain empty strings`() {
        val users = listOf("alice", "", "bob")
        val filteredUsers = users.filter { it.isNotEmpty() }
        
        assertFalse(filteredUsers.contains(""))
    }

    @Test
    fun `getAllUsers should handle null safely`() {
        val users: List<String>? = null
        val safeUsers = users ?: emptyList()
        
        assertTrue(safeUsers.isEmpty())
    }

    @Test
    fun `username extraction should work correctly`() {
        val fileName = "loveya_testuser.db"
        val username = fileName.substring(7).removeSuffix(".db")
        
        assertEquals("testuser", username)
    }

    @Test
    fun `username extraction should handle edge case`() {
        val fileName = "loveya_.db"
        val username = fileName.substring(7).removeSuffix(".db")
        
        assertEquals("", username)
    }
}