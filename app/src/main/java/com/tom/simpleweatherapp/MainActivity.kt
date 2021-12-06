package com.tom.simpleweatherapp

import android.location.Address
import android.location.Geocoder
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import com.tom.simpleweatherapp.api.WeatherResponse
import com.tom.simpleweatherapp.api.WeatherService
import com.tom.simpleweatherapp.db.CityModel
import com.tom.simpleweatherapp.db.MyDatabase
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*

class MainActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener {

    private var currentLocation: TextView? = null
    private var currentCity: String = ""
    private var tvResultsTemperature: TextView? = null
    private var tvResultsWeather: TextView? = null
    private var locationManager: LocationManager? = null
    private lateinit var database: MyDatabase
    private lateinit var spinner: Spinner

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // initializing views
        currentLocation = findViewById(R.id.tvCurrentLocation)
        tvResultsTemperature = findViewById(R.id.tvResultsTemperature)
        tvResultsWeather = findViewById(R.id.tvResultsWeatherConditions)
        spinner = findViewById(R.id.cities_spinner)
        val button = findViewById<Button>(R.id.btnShowWeather)

        database = MyDatabase.invoke(this)

        // creating a reference for LocationManager
        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager?

        // defining spinner view to implement a dropdown list
        ArrayAdapter.createFromResource(
            this, R.array.cities_array, android.R.layout.simple_spinner_item
        )
            .also { adapter ->
                adapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item)
                spinner.adapter = adapter
            }
        spinner.onItemSelectedListener = this


        // trying to get the current location
        // p.s. the app will crash if the location permission is not granted
        getCurrentLocation()

        // setting the button to get weather info for the selected city
        button.setOnClickListener {
            btnListener()
        }

    }

    // at first, it searches through the DB for data, if nothing is found then the API is called.
    private fun btnListener() {
        if (spinner.selectedItem == "Current location") {
            var city: String?
            database.cityModelDao().getCity(currentCity).observe(this, { retrievedCity ->
                city = retrievedCity
                if (retrievedCity != null) {
                    getWeatherInfoFromDB(city, database)
                } else {
                    getWeatherInfoFromApi(currentCity, database)
                }
            })
        } else {
            val selectedCity = spinner.selectedItem.toString()
            var city: String?
            database.cityModelDao().getCity(selectedCity).observe(this, { retrievedCity ->
                city = retrievedCity
                if (retrievedCity != null) {
                    getWeatherInfoFromDB(city, database)
                } else {
                    getWeatherInfoFromApi(selectedCity, database)
                }
            })
        }
    }

    private fun getWeatherInfoFromDB(cityName: String?, database: MyDatabase) {

        var temperature: String
        var weather: String

        if (cityName != null) {
            database.cityModelDao().getTemperature(cityName)
                .observe(this, { retrievedTemperature ->
                    temperature = retrievedTemperature.toString()
                    tvResultsTemperature?.text = getString(R.string.tv_temperature_results, temperature)
                })
        }

        if (cityName != null) {
            database.cityModelDao().getWeatherConditions(cityName)
                .observe(this, { retrievedWeather ->
                    weather = retrievedWeather
                    tvResultsWeather?.text = getString(R.string.tv_weather_results, weather)
                })
        }
    }

    private fun getWeatherInfoFromApi(city: String, database: MyDatabase) {
        val retrofit = Retrofit.Builder()
            .baseUrl(BaseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val service = retrofit.create(WeatherService::class.java)
        val call = service.getCurrentWeatherData(city, units, AppId)
        var responseTemperature: Float
        var responseWeather: String? = ""

        call.enqueue(object : Callback<WeatherResponse> {
            override fun onResponse(
                call: Call<WeatherResponse>,
                response: Response<WeatherResponse>
            ) {
                if (response.code() == 200) {
                    val weatherResponse = response.body()!!

                    responseTemperature = weatherResponse.main!!.temp
                    responseWeather = weatherResponse.weather[0].main

                    // putting a new CityModel class into our database
                    // including: city name, temperature, weather conditions
                    Thread {
                        database.cityModelDao()
                            .insert(CityModel(city, responseTemperature, responseWeather))
                    }.start()
                }
            }

            override fun onFailure(call: Call<WeatherResponse>, t: Throwable) {
                tvResultsTemperature!!.text = t.message
            }
        })
    }

    companion object {
        var BaseUrl = "https://api.openweathermap.org/"
        var AppId = "52287f86c593537704d9fa13a7e5d640"
        var units = "metric"
    }

    private fun getCurrentLocation() {
        var latitude: Double = 0.0
        var longitude: Double = 0.0

        try {
            latitude =
                locationManager?.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)?.latitude!!
            longitude =
                locationManager?.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)?.longitude!!
        } catch (ex: SecurityException) {
            Log.d("myLocationTag", "No location available")
        }

        // setting Geocoder to display current city and country
        // for those who don't know what latitude & longitude are
        val geocoder = Geocoder(this, Locale.getDefault())
        val addresses: MutableList<Address> = geocoder.getFromLocation(latitude, longitude, 1)
        val cityName = addresses[0].locality
        val countryName = addresses[0].countryName

        val stringBuilder = "$cityName, $countryName" +
                " \n${latitude}:${longitude}"

        currentLocation?.text = stringBuilder
        currentCity = cityName
    }

    // clearing the results field when selecting another city
    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        tvResultsTemperature?.text = ""
        tvResultsWeather?.text = ""
    }

    // setting the spinner to "Current location" when no item from the list is selected
    override fun onNothingSelected(parent: AdapterView<*>?) {
        parent?.setSelection(0)
    }

    override fun onStop() {
        super.onStop()
        Thread {
            database.clearAllTables()
        }.start()

        database.close()
    }
}