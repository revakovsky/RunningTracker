package com.revakovskyi.core.database.di

import androidx.room.Room
import com.revakovskyi.core.database.RoomLocalRunDataSource
import com.revakovskyi.core.database.RunDatabase
import com.revakovskyi.core.database.dao.RunDao
import com.revakovskyi.core.domain.run.LocalRunDataSource
import org.koin.android.ext.koin.androidApplication
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val databaseModule = module {

    single<RunDatabase> {
        Room
            .databaseBuilder(
                context = androidApplication(),
                klass = RunDatabase::class.java,
                name = "runs.db"
            )
            .build()
    }

    single<RunDao> { get<RunDatabase>().ranDao }

    singleOf(::RoomLocalRunDataSource).bind<LocalRunDataSource>()

}