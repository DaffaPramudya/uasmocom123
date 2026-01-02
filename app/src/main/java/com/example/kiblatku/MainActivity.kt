package com.example.kiblatku

import android.Manifest
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.runtime.remember
import kotlin.math.roundToInt
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kiblatku.helper.DataManager
import com.example.kiblatku.kiblat.KiblatCalculator
import com.example.kiblatku.kiblat.SettingsActivity
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

        val prefs = DataManager(this)

        var isNoGPSMode by mutableStateOf(prefs.isNoGPSMode)

        compass = CompassSensor(this)
        locationProvider = LocationProvider(this)

        // PERMISSION FLOW (modern)
        if (!isNoGPSMode) {
            if (PermissionHelper.hasLocationPermission(this)) {
                ambilLokasi()
            } else {
                locationPermissionLauncher.launch(
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            }
        } else {
            ambilLokasidariPrefs(prefs)
        }

        setContent {
            KiblatkuTheme {
                KiblatScreen(
                    compass = compass,
                    arahKiblat = arahKiblat,
                    prefs= prefs
                )
            }
        }
    }

    private fun ambilLokasi() {
        locationProvider.startLocationUpdates { lat, lon ->
            arahKiblat = KiblatCalculator.hitungArahKiblat(lat, lon)
        }
    }

    private fun ambilLokasidariPrefs(prefs: DataManager) {
        val lat = prefs.lat.toDouble()
        val lon = prefs.lon.toDouble()

        locationProvider.startLocationNoGPSUpdates(lat, lon) { lat, lon ->
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
    arahKiblat: Double?,
    prefs: DataManager
) {
    val azimuth by remember {
        derivedStateOf { compass.azimuth }
    }

    val arahPanah = arahKiblat?.let {
        val diff = ((it - azimuth + 360) % 360)
        if (diff < 1 || diff > 359) 0 else diff.roundToInt()
    }

    Scaffold(modifier = Modifier.fillMaxSize()) { padding ->
        Text(
            modifier = Modifier.padding(padding),
            text = buildString {
                append("Arah HP: ${azimuth.toInt()}°\n")
                append("Arah Kiblat: ${arahKiblat?.toInt() ?: "--"}°\n")
                append("Putar ke: ${arahPanah ?: "--"}°\n\n")

                // Calibration hint (important)
                append("Jika arah tidak stabil,\n")
                append("gerakkan HP membentuk angka 8 📱")
            }
        )
        SettingsButton(prefs)
    }
}

@Composable
fun SettingsButton(pref: DataManager) {
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 48.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        Button(
            onClick = {
                val intent = Intent(context, SettingsActivity::class.java)
                context.startActivity(intent)
            },
            shape = RoundedCornerShape(24.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.White.copy(alpha = 0.15f) // Glass effect
            ),
            modifier = Modifier
                .height(64.dp)
                .fillMaxWidth(0.6f)
                .border(
                    width = 1.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(Color.White.copy(alpha = 0.5f), Color.Transparent)
                    ),
                    shape = RoundedCornerShape(24.dp)
                ),
            contentPadding = PaddingValues(horizontal = 24.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = null,
                    tint = Color(0xFFC8E6C9)
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    "PREFERENCES",
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White,
                    letterSpacing = 2.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

