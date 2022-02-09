package com.gmail.pavlovsv93.permission.content.provider.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [Entity::class], version = 1)
abstract class DB() : RoomDatabase() {
    abstract fun dao(): DAO
}