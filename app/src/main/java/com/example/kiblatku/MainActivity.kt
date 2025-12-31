package com.example.kiblatku

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.kiblatku.kiblat.KiblatCalculator
import com.example.kiblatku.location.LocationProvider
import com.example.kiblatku.location.PermissionHelper
import com.example.kiblatku.sensor.CompassSensor
import com.example.kiblatku.ui.theme.KiblatkuTheme

class MainActivity : ComponentActivity() {

    private lateinit var compass: CompassSensor
    private lateinit var locationProvider: LocationProvider

    private var arahKiblat by mutableStateOf<Double?>(null)

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

        compass = CompassSensor(this)
        locationProvider = LocationProvider(this)

        // PERMISSION FLOW (modern)
        if (PermissionHelper.hasLocationPermission(this)) {
            ambilLokasi()
        } else {
            locationPermissionLauncher.launch(
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        }

        setContent {
            KiblatkuTheme {
                KiblatScreen(
                    compass = compass,
                    arahKiblat = arahKiblat
                )
            }
        }
    }

    private fun ambilLokasi() {
        locationProvider.startLocationUpdates { lat, lon ->
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

@Composable
fun KiblatScreen(
    compass: CompassSensor,
    arahKiblat: Double?
) {
    // Recompose when compass changes
    val azimuth by remember {
        derivedStateOf { compass.azimuth }
    }

    val arahPanah = arahKiblat?.let {
        (it - azimuth + 360) % 360
    }

    Scaffold(modifier = Modifier.fillMaxSize()) { padding ->
        Text(
            modifier = Modifier.padding(padding),
            text = buildString {
                append("Arah HP: ${azimuth.toInt()}°\n")
                append("Arah Kiblat: ${arahKiblat?.toInt() ?: "--"}°\n")
                append("Putar ke: ${arahPanah?.toInt() ?: "--"}°\n\n")

                // Calibration hint (important)
                append("Jika arah tidak stabil,\n")
                append("gerakkan HP membentuk angka 8 📱")
            }
        )
    }
}

