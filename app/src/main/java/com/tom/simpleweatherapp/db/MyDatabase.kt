package com.tom.simpleweatherapp.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [CityModel::class], version = 1, exportSchema = false)
abstract class MyDatabase : RoomDatabase() {
    abstract fun cityModelDao(): CityModelDao

    companion object {
        @Volatile
        private var instance: MyDatabase? = null

        private val LOCK = Any()

        operator fun invoke(context: Context) = instance ?: synchronized(LOCK) {
            instance ?: createDatabase(context).also { instance = it }
        }

        private fun createDatabase(context: Context) =
            Room.databaseBuilder(context.applicationContext,
                MyDatabase::class.java, "WeatherDB.db")
                .fallbackToDestructiveMigration()
                .build()
    }

}