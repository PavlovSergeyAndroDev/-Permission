package com.gmail.pavlovsv93.permission.content.provider.db

import android.app.Application
import androidx.room.Room

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    companion object {
        private var instance: App? = null
        private var db: DB? = null
        private const val DB_NAME = "DataBase"

        fun getDao(): DAO {
            if (db == null) {
                synchronized(DB::class.java) {
                    if (db == null) {
                        if (instance == null) {
                            throw IllegalAccessException("Application is null")
                        }
                        db = Room.databaseBuilder(instance!!.applicationContext, DB::class.java, DB_NAME)
                            .allowMainThreadQueries()
                            .build()
                    }
                }
            }
            return db!!.dao()
        }
    }
}