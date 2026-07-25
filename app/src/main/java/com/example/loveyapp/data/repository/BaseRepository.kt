package com.example.loveyapp.data.repository

import com.example.loveyapp.data.local.database.LoveYaDatabaseFactory
import javax.inject.Inject

open class BaseRepository @Inject constructor(
    protected val databaseFactory: LoveYaDatabaseFactory,
    protected val currentUsername: String
)