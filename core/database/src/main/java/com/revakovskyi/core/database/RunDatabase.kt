package com.revakovskyi.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.revakovskyi.core.database.dao.RunDao
import com.revakovskyi.core.database.entity.RunEntity

@Database(
    entities = [RunEntity::class],
    version = 1
)
abstract class RunDatabase : RoomDatabase() {

    abstract val ranDao: RunDao

}
