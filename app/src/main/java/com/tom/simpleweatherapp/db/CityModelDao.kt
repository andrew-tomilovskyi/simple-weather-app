package com.tom.simpleweatherapp.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface CityModelDao {

    @Insert
    fun insert(cityModel: CityModel)

    @Query("SELECT city_name FROM cities WHERE city_name = :cityName")
    fun getCity(cityName: String) : LiveData<String>

    @Query("SELECT temperature FROM cities WHERE city_name = :cityName")
    fun getTemperature(cityName: String) : LiveData<Float>

    @Query("SELECT weather_conditions FROM cities WHERE city_name = :cityName")
    fun getWeatherConditions(cityName: String) : LiveData<String>

    @Query("DELETE FROM cities")
    fun deleteAll()

}