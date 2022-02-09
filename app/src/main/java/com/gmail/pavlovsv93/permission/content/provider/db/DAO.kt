package com.gmail.pavlovsv93.permission.content.provider.db

import android.database.Cursor
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface DAO {

    @Query("DELETE FROM Entity WHERE uid = :id")
    fun deleteById(id: Long)

    @Query("SELECT uid, title FROM Entity")
    fun getEntityCursor(): Cursor

    @Query("SELECT uid, title FROM Entity WHERE uid = :id")
    fun getEntityCursorId(id: Long): Cursor

    @Insert
    fun insert(vararg entity: Entity)

    @Update
    fun update(entity: Entity)
}