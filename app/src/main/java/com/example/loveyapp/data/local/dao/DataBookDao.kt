package com.example.loveyapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.loveyapp.data.local.entity.DataBook

@Dao
interface DataBookDao {
    @Insert
    suspend fun insert(dataBook: DataBook): Long

    @Query("SELECT * FROM data_book ORDER BY notebook_name, attribute_name")
    suspend fun getAll(): List<DataBook>

    @Query("SELECT * FROM data_book WHERE notebook_name = :notebookName ORDER BY attribute_name")
    suspend fun findByNotebookName(notebookName: String): List<DataBook>

    @Query("SELECT * FROM data_book WHERE attribute_name = :attributeName ORDER BY notebook_name")
    suspend fun findByAttributeName(attributeName: String): List<DataBook>

    @Query("SELECT * FROM data_book WHERE id = :id LIMIT 1")
    suspend fun findById(id: Long): DataBook?

    @Update
    suspend fun update(dataBook: DataBook)

    @Query("DELETE FROM data_book WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM data_book WHERE notebook_name = :notebookName")
    suspend fun deleteByNotebookName(notebookName: String)

    @Insert
    suspend fun insertAll(dataBooks: List<DataBook>)

    @Query("DELETE FROM data_book")
    suspend fun deleteAll()

    @Query("DELETE FROM data_book WHERE id IN (:ids)")
    suspend fun deleteByIds(ids: List<Long>)
}