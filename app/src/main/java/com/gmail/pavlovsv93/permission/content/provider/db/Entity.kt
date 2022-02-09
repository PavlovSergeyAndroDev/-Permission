package com.gmail.pavlovsv93.permission.content.provider.db

import androidx.room.Entity
import androidx.room.PrimaryKey

const val UID = "uid"
const val TITLE = "title"
const val MASS = "mass"

@Entity
data class Entity(
    @PrimaryKey(autoGenerate = true)
    val uid: Long = 0,
    val title: String = "",
    val mass: String = ""
)
