package com.tom.simpleweatherapp

import android.location.Address
import android.location.Geocoder
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import com.tom.simpleweatherapp.db.MyDatabase
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
}