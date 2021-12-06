package com.tom.simpleweatherapp.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cities")
data class CityModel(
    @ColumnInfo(name = "city_name")
    val cityName: String,
    @ColumnInfo(name = "temperature")
    val temperature: Float?,
    @ColumnInfo(name = "weather_conditions")
    val weatherConditions: String?
) {
        @PrimaryKey(autoGenerate = true)
        var id: Int = 0
}