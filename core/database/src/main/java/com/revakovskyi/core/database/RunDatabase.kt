package com.revakovskyi.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.revakovskyi.core.database.dao.RunDao
import com.revakovskyi.core.database.dao.RunPendingSyncDao
import com.revakovskyi.core.database.entity.DeletedRunSyncEntity
import com.revakovskyi.core.database.entity.RunEntity
import com.revakovskyi.core.database.entity.RunPendingSyncEntity

@Database(
    entities = [
        RunEntity::class,
        RunPendingSyncEntity::class,
        DeletedRunSyncEntity::class,
    ],
    version = 1
)
abstract class RunDatabase : RoomDatabase() {

    abstract val ranDao: RunDao
    abstract val runPendingSyncDao: RunPendingSyncDao

}
