package com.example.kiblatku

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.*
import com.example.kiblatku.helper.DataManager
import com.example.kiblatku.kiblat.KiblatCalculator
import com.example.kiblatku.kiblat.KiblatHomeScreen
import com.example.kiblatku.kiblat.LoadingScreen
import com.example.kiblatku.kiblat.LocationDisabledDialog
import com.example.kiblatku.kiblat.SettingsActivity
import com.example.kiblatku.location.LocationProvider
import com.example.kiblatku.location.PermissionHelper
import com.example.kiblatku.sensor.CompassSensor
import com.example.kiblatku.ui.theme.KiblatkuTheme
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {

    private lateinit var compass: CompassSensor
    private lateinit var locationProvider: LocationProvider
    private lateinit var prefs: DataManager

    private var arahKiblat by mutableStateOf<Double?>(null)
    private var isLoading by mutableStateOf(true)
    private var currentLat by mutableStateOf(0.0)
    private var currentLon by mutableStateOf(0.0)

    private val locationPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (isGranted) {
                ambilLokasi()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        prefs = DataManager(this)
        compass = CompassSensor(this)
        locationProvider = LocationProvider(this)

        val isNoGPSMode = prefs.isNoGPSMode

        // Initialize location based on mode
        if (!isNoGPSMode) {
            if (PermissionHelper.hasLocationPermission(this)) {
                ambilLokasi()
            } else {
                locationPermissionLauncher.launch(
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            }
        } else {
            ambilLokasidariPrefs()
        }

        setContent {
            KiblatkuTheme {
                // Show loading screen for 2 seconds, then main screen
                if (isLoading) {
                    LoadingScreen()

                    // Auto-hide loading after 2 seconds
                    LaunchedEffect(Unit) {
                        delay(2000)
                        isLoading = false
                    }
                } else {
                    val userLocation = if (currentLat != 0.0 && currentLon != 0.0) {
                        Pair(currentLat, currentLon)
                    } else {
                        null
                    }

                    var showLocationDialog by remember { mutableStateOf(false) }

                    if (showLocationDialog) {
                        LocationDisabledDialog(
                            onDismiss = { showLocationDialog = false },
                            onEnable = {
                                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                                startActivity(intent)
                                showLocationDialog = false
                            }
                        )
                    }

                    KiblatHomeScreen(
                        compass = compass,
                        arahKiblat = arahKiblat,
                        userLocation = userLocation,
                        isDarkMode = isSystemInDarkTheme(),
                        onSettingsClick = {
                            val intent = Intent(this@MainActivity, SettingsActivity::class.java)
                            startActivity(intent)
                        },
                        onLocationDisabled = {
                            showLocationDialog = true
                        }
                    )
                }
            }
        }
    }

    private fun ambilLokasi() {
        locationProvider.startLocationUpdates { lat, lon ->
            currentLat = lat
            currentLon = lon
            arahKiblat = KiblatCalculator.hitungArahKiblat(lat, lon)
        }
    }

    private fun ambilLokasidariPrefs() {
        val lat = prefs.lat.toDouble()
        val lon = prefs.lon.toDouble()

        if (lat != 0.0 && lon != 0.0) {
            currentLat = lat
            currentLon = lon
            arahKiblat = KiblatCalculator.hitungArahKiblat(lat, lon)
        }
    }

    override fun onResume() {
        super.onResume()
        compass.start()
    }

    override fun onPause() {
        super.onPause()
        compass.stop()
        locationProvider.stopLocationUpdates()
    }
}
