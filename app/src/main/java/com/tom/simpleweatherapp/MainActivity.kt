package com.tom.simpleweatherapp

import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.*
import com.tom.simpleweatherapp.db.MyDatabase

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