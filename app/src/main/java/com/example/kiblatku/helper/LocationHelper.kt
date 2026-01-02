package com.example.kiblatku.helper

import android.app.Activity
import android.content.Intent
import android.location.Geocoder
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import java.util.Locale

class LocationHelper : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Configuration.getInstance().userAgentValue = packageName

        // Get starting position from Intent
        val startLat = intent.getFloatExtra("start_lat", -6.2000f).toDouble()
        val startLon = intent.getFloatExtra("start_lon", 106.8166f).toDouble()

        setContent {
            var selectedPoint by remember { mutableStateOf(GeoPoint(startLat, startLon)) }
            var locationLabel by remember { mutableStateOf("Tap to select location") }

            Column(modifier = Modifier.fillMaxSize()) {
                AndroidView(
                    factory = { context ->
                        MapView(context).apply {
                            setMultiTouchControls(true)
                            controller.setZoom(15.0)
                            controller.setCenter(selectedPoint)

                            val marker = Marker(this)
                            marker.position = selectedPoint
                            overlays.add(marker)

                            overlays.add(MapEventsOverlay(object : MapEventsReceiver {
                                override fun singleTapConfirmedHelper(p: GeoPoint): Boolean {
                                    selectedPoint = p
                                    marker.position = p
                                    invalidate()

                                    val geocoder = Geocoder(context, Locale.getDefault())
                                    try {
                                        val addresses =
                                            geocoder.getFromLocation(p.latitude, p.longitude, 1)
                                        if (!addresses.isNullOrEmpty()) {
                                            val city =
                                                addresses[0].locality ?: addresses[0].subAdminArea
                                                ?: "Unknown City"
                                            val country =
                                                addresses[0].countryName ?: "Unknown Country"
                                            locationLabel = "$country, $city"
                                        }
                                    } catch (e: Exception) {
                                        locationLabel = "Custom Location"
                                    }
                                    return true
                                }

                                override fun longPressHelper(p: GeoPoint): Boolean = false
                            }))
                        }
                    },
                    modifier = Modifier.weight(1f)
                )
                // Themed Confirm Panel
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color.White,
                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                    shadowElevation = 8.dp
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "SELECTED LOCATION",
                            style = MaterialTheme.typography.labelLarge,
                            color = Color(0xFF2E7D32)
                        )
                        Text(
                            locationLabel,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )

                        Button(
                            onClick = {
                                val resultIntent = Intent().apply {
                                    putExtra("lat", selectedPoint.latitude)
                                    putExtra("lon", selectedPoint.longitude)
                                    putExtra("label", locationLabel)
                                }
                                setResult(Activity.RESULT_OK, resultIntent)
                                finish()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                            modifier = Modifier.fillMaxWidth().padding(16.dp)
                        ) { Text("Confirm Selection") }
                    }
                }
            }
        }
    }
}