package com.example.kiblatku.kiblat

import android.os.Build
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kiblatku.R
import com.example.kiblatku.sensor.CompassSensor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KiblatHomeScreen(
    compass: CompassSensor,
    arahKiblat: Double?,
    userLocation: Pair<Double, Double>?,
    isDarkMode: Boolean = isSystemInDarkTheme(),
    onSettingsClick: () -> Unit,
    onLocationDisabled: () -> Unit
) {
    val azimuth by remember {
        derivedStateOf { compass.azimuth }
    }

    val accuracy by remember {
        derivedStateOf { compass.accuracy }
    }

    // Check if location is available
    LaunchedEffect(userLocation) {
        if (userLocation == null) {
            onLocationDisabled()
        }
    }

    val qiblaBearing = arahKiblat ?: 0.0

    // Check if facing Qibla (within ±5°)
// ✅ CORRECT: Calculate difference
    val angleDifference = arahKiblat?.let { qibla ->
        ((qibla - azimuth + 360) % 360)
    } ?: 0.0

// ✅ CORRECT: Compare difference
    val isQiblaCorrect = arahKiblat?.let { _ ->
        angleDifference <= 1.5 || angleDifference >= 358.5  // Use difference!
    } ?: false

    // Color scheme based on theme
    val backgroundColor = if (isDarkMode) {
        Color(0xFF2D5F3F) // Dark green
    } else {
        Color(0xFFB5E0D1) // Light green
    }

    val cardBackgroundColor = if (isDarkMode) {
        Color(0xFF1B4D32) // Darker green for cards
    } else {
        Color(0xFFC8F0E0) // Lighter green
    }

    val textColor = if (isDarkMode) {
        Color.White
    } else {
        Color(0xFF1B4D32)
    }

    val secondaryTextColor = if (isDarkMode) {
        Color(0xFFB0B0B0)
    } else {
        Color(0xFF666666)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        // Header with Settings
        TopAppBar(
            title = {
                Text(
                    "KiblatKu",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = textColor
                )
            },
            actions = {
                IconButton(onClick = onSettingsClick) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = textColor,
                        modifier = Modifier.size(24.dp)
                    )
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp),
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = if (isDarkMode) {
                    Color(0xFF1B4D32)
                } else {
                    Color(0xFF7FAEA8)
                }
            )
        )

        // Main Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 80.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Status Text
            Text(
                text = if (isQiblaCorrect) "Kiblat sesuai" else "Kiblat belum sesuai",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = if (isQiblaCorrect) Color(0xFF4CAF50) else textColor,
                modifier = Modifier.padding(top = 24.dp)
            )

            // Bearing Text
            Text(
                text = "${azimuth.toInt()}°",
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                color = textColor,
                modifier = Modifier.padding(top = 8.dp)
            )

            // Compass Circle with Rotating Needle
            Box(
                modifier = Modifier
                    .size(280.dp)
                    .padding(top = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.red_indicator_line),
                    contentDescription = "Red Indicator Line",
                    modifier = Modifier
                        .size(60.dp)
                        .offset(y = -(125.dp))
                )

                // Compass Background Circle
                Image(
                    painter = painterResource(id = R.drawable.compass_circle_white),
                    contentDescription = "Compass Circle",
                    modifier = Modifier
                        .fillMaxSize()
                        .rotate(azimuth * -1)
                )

//                // Center Logo/Indicator
//                Image(
//                    painter = painterResource(id = R.drawable.ic_kaaba),
//                    contentDescription = "Kaaba",
//                    modifier = Modifier.size(60.dp)
//                )

                // Qibla Arrow (rotating based on relative angle)
                Image(
                    painter = painterResource(id = R.drawable.qibla_arrow),
                    contentDescription = "Qibla Arrow",
                    modifier = Modifier
                        .size(80.dp)
//                        .align(Alignment.TopCenter)
//                        .offset(y = 0.dp)
                        .rotate(angleDifference.toFloat())
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Cardinal Direction & Accuracy
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = getCardinalDirection(azimuth),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )

                Text(
                    text = "Akurasi: ${getAccuracyStatus(accuracy)} ✓",
                    fontSize = 14.sp,
                    color = secondaryTextColor
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Location Display
            if (userLocation != null) {
                Text(
                    text = "Lokasi anda:",
                    fontSize = 14.sp,
                    color = secondaryTextColor,
                    modifier = Modifier.padding(top = 8.dp)
                )

                Text(
                    text = "%.1f, %.1f".format(userLocation.first, userLocation.second),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = textColor
                )
            }
        }
    }
}

@Composable
fun LoadingScreen(isDarkMode: Boolean = isSystemInDarkTheme()) {
    val backgroundColor = if (isDarkMode) {
        Color(0xFF2D5F3F)
    } else {
        Color(0xFF7FAEA8)
    }

    val textColor = if (isDarkMode) {
        Color.White
    } else {
        Color.White
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Logo
            Image(
                painter = painterResource(id = R.drawable.logo_kiblatku_white),
                contentDescription = "KiblatKu Logo",
                modifier = Modifier.size(120.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // App Name
            Text(
                text = "KiblatKu",
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                color = textColor
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Loading Spinner
            CircularProgressIndicator(
                color = textColor,
                modifier = Modifier.size(48.dp),
                strokeWidth = 4.dp
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Menginisialisasi...",
                fontSize = 14.sp,
                color = textColor.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
fun LocationDisabledDialog(
    onDismiss: () -> Unit,
    onEnable: () -> Unit,
    isDarkMode: Boolean = isSystemInDarkTheme()
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Lokasi Dibutuhkan",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Text(
                text = "Nyalakan fitur lokasi / GPS untuk melihat arah kiblat dengan akurat."
            )
        },
        confirmButton = {
            Button(onClick = onEnable) {
                Text("Buka Pengaturan")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Nanti")
            }
        },
        modifier = Modifier.background(
            if (isDarkMode) Color(0xFF2D2D2D) else Color.White,
            shape = RoundedCornerShape(8.dp)
        )
    )
}

// Helper function to get cardinal direction
private fun getCardinalDirection(azimuth: Float): String {
    return when {
        azimuth in 348.75f..360f || azimuth in 0f..11.25f -> "N"
        azimuth in 11.25f..33.75f -> "NNE"
        azimuth in 33.75f..56.25f -> "NE"
        azimuth in 56.25f..78.75f -> "ENE"
        azimuth in 78.75f..101.25f -> "E"
        azimuth in 101.25f..123.75f -> "ESE"
        azimuth in 123.75f..146.25f -> "SE"
        azimuth in 146.25f..168.75f -> "SSE"
        azimuth in 168.75f..191.25f -> "S"
        azimuth in 191.25f..213.75f -> "SSW"
        azimuth in 213.75f..236.25f -> "SW"
        azimuth in 236.25f..258.75f -> "WSW"
        azimuth in 258.75f..281.25f -> "W"
        azimuth in 281.25f..303.75f -> "WNW"
        azimuth in 303.75f..326.25f -> "NW"
        azimuth in 326.25f..348.75f -> "NNW"
        else -> "N"
    }
}

// Helper function to get accuracy status
private fun getAccuracyStatus(accuracy: Int): String {
    return when (accuracy) {
        3 -> "Sangat Baik"
        2 -> "Baik"
        1 -> "Sedang"
        else -> "Buruk"
    }
}