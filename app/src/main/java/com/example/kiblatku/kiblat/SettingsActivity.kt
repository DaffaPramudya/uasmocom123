package com.example.kiblatku.kiblat

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kiblatku.helper.DataManager
import com.example.kiblatku.helper.LocationHelper
import com.example.kiblatku.ui.theme.EmeraldDark
import com.example.kiblatku.ui.theme.ForestGreen
import com.example.kiblatku.ui.theme.ForestGreenDark
import com.example.kiblatku.ui.theme.MintBackground
import com.example.kiblatku.ui.theme.MintBackgroundDark
import com.example.kiblatku.ui.theme.TextScale

class SettingsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val context = LocalContext.current
            val prefs = remember { DataManager(context) }

            var isNoGPSMode by remember { mutableStateOf(prefs.isNoGPSMode) }
            var locationName by remember { mutableStateOf(prefs.locationName) }

            // Launcher for the Map Activity
            val displayLabel = if (locationName.isEmpty()) "Select Location" else locationName

            val mapLauncher = rememberLauncherForActivityResult(
                ActivityResultContracts.StartActivityForResult()
            ) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val data = result.data
                    val label = data?.getStringExtra("label") ?: ""
                    val lat = data?.getDoubleExtra("lat", 0.0) ?: 0.0
                    val lon = data?.getDoubleExtra("lon", 0.0) ?: 0.0

                    // 1. Save to Disk Instantly
                    prefs.locationName = label
                    prefs.lat = lat.toFloat()
                    prefs.lon = lon.toFloat()

                    // 2. Update Local UI state instantly
                    locationName= label
                }
            }

            CompassTheme(prefs) {
                SettingsScreen(
                    prefs = prefs,
                    onBack = { finish() },
                    onOpenPermissions = {
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = Uri.fromParts("package", context.packageName, null)
                        }
                        context.startActivity(intent)
                    },
                    onOpenMap = {
                        val intent = Intent(context, LocationHelper::class.java).apply {
                            putExtra("start_lat", prefs.lat)
                            putExtra("start_lon", prefs.lon)
                        }
                        mapLauncher.launch(intent)
                    },
                    locationName
                )
            }
        }
    }
}

fun textScaleFor(size: String): TextScale =
    when (size) {
        "Small" -> TextScale(
            title = 14.sp,
            subtitle = 11.sp
        )
        "Large" -> TextScale(
            title = 18.sp,
            subtitle = 14.sp
        )
        "Very Large" -> TextScale(
            title = 20.sp,
            subtitle = 16.sp
        )
        else -> TextScale( // Normal
            title = 16.sp,
            subtitle = 12.sp
        )
    }


@Composable
fun CompassTheme(
    prefs: DataManager,
    content: @Composable () -> Unit) {

    var darkModeSetting by remember {
        mutableStateOf(prefs.darkModeSetting)
    }

    // Determine if we should use Dark Mode
    val useDarkMode = when (darkModeSetting) {
        "On" -> true
        "Off" -> false
        else -> isSystemInDarkTheme() // "Auto" follows system
    }

    val colorScheme = if (useDarkMode) {
        darkColorScheme(
            primary = Color(0xFF81C784), // Lighter green for dark mode readability
            background = Color(0xFF121212),
            surface = Color(0xFF1E1E1E),
            onSurface = Color.Red
        )
    } else {
        lightColorScheme(
            primary = Color(0xFF2E7D32),
            background = Color(0xFFF1F8E9),
            surface = Color.White,
            onSurface = Color.Black
        )
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    prefs: DataManager,
    onBack: () -> Unit,
    onOpenPermissions: () -> Unit,
    onOpenMap: () -> Unit,
    locationName: String
) {
    // --- STATE ---
    var isNoGPSMode by remember { mutableStateOf(prefs.isNoGPSMode) }
    var darkMode by remember { mutableStateOf(prefs.darkModeSetting) }
    var compassType by remember { mutableStateOf(prefs.compassType) }
    var textSize by remember { mutableStateOf(prefs.textSize) }
    var isSmooth by remember { mutableStateOf(prefs.isSmoothAnimation) }
    var accuracyStyle by remember { mutableStateOf(prefs.AccuracyStyle) }
    var isSoundEnabled by remember { mutableStateOf(prefs.isSoundEnabled) }
    var isVibrationEnabled by remember { mutableStateOf(prefs.isVibrationEnabled) }

    val useDarkMode = when (darkMode) {
        "On" -> true
        "Off" -> false
        else -> isSystemInDarkTheme() // "Auto" follows system
    }
    Scaffold(
        topBar = {
            Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
                // Modern Glass Back Button
                GlassBackButton(onClick = onBack, darkMode)

                // Centered Title
                Text(
                    text = "Settings",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = if (useDarkMode) ForestGreenDark else ForestGreen,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        },
        containerColor = if(!useDarkMode) MintBackground else MintBackgroundDark
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // 1. LOCATION PERMISSION
            SettingsSection(title = "Location", darkMode, textSize) {
                SettingsToggle(
                    "No-GPS Mode",
                    "Disable GPS and use manual location",
                    isNoGPSMode,
                    darkMode,
                    textSize,
                    ) {
                        isNoGPSMode = it
                        prefs.isNoGPSMode = isNoGPSMode
                    }

                SettingsClickable(
                    title = "System Permissions",
                    subtitle = "Manage location access in Android settings",
                    enabled = !isNoGPSMode,
                    darkMode,
                    textSize,
                    onClick = onOpenPermissions
                )

                SettingsClickable(
                    title = "Select Location",
                    subtitle = if (isNoGPSMode) locationName else "Turn on No-GPS Mode to edit",
                    enabled = isNoGPSMode,
                    darkMode,
                    textSize,
                    onClick = onOpenMap,
                )
            }

            // 2. DISPLAY PREFERENCES
            SettingsSection(title = "Display Preferences", darkMode, textSize) {
                SettingsDropdown(
                    "Dark Mode", darkMode, listOf(
                        "On", "Off", "Auto"
                    ),
                    darkMode,
                    textSize
                ) { newValue ->
                    darkMode = newValue
                    prefs.darkModeSetting = newValue }
                SettingsDropdown("Compass Style", compassType, listOf(
                    "Analog", "Digital Style"
                ),
                    darkMode,
                    textSize
                ) { compassType = it }
                SettingsDropdown("Text Size", textSize, listOf(
                    "Small", "Normal", "Large", "Very Large"
                ),
                    darkMode,
                    textSize
                ) {
                    textSize = it
                    prefs.textSize = textSize
                }

                SettingsToggle(
                    "Rotational Animation",
                    if (isSmooth) "Smooth Animation" else "Accurate (Might be unstable)",
                    isSmooth,
                    darkMode,
                    textSize
                ) {
                    isSmooth = it
                    prefs.isSmoothAnimation = isSmooth
                }

                SettingsDropdown(
                    "Accuracy Indicator",
                    accuracyStyle,
                    listOf("Numbers", "Color Code"),
                    darkMode,
                    textSize
                ) { accuracyStyle = it }
            }

            // 3. SOUND AND VIBRATION
            SettingsSection(title = "Alerts", darkMode, textSize) {
                SettingsToggle(
                    "Sound",
                    "Play SFX when hitting Qibla",
                    isSoundEnabled,
                    darkMode,
                    textSize
                )
                { isSoundEnabled = it }
                SettingsToggle(
                    "Vibration",
                    "Haptic feedback when hitting Qibla",
                    isVibrationEnabled,
                    darkMode,
                    textSize
                )
                { isVibrationEnabled = it }
            }

            // 4. ABOUT APP
            SettingsSection(title = "About App", darkMode, textSize) {
                AboutRow(
                    "App Version",
                    "1.0.4-Build",
                    darkMode,
                    textSize
                )
                AboutRow(
                    "Last Calibration",
                    "42 seconds ago",
                    darkMode,
                    textSize
                    )
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

// --- UI COMPONENTS ---

@Composable
fun GlassBackButton(onClick: () -> Unit, darkMode: String) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (isPressed) 0.9f else 1f, label = "scale")

    val useDarkMode = when (darkMode) {
        "On" -> true
        "Off" -> false
        else -> isSystemInDarkTheme() // "Auto" follows system
    }
    Surface(
        onClick = onClick,
        interactionSource = interactionSource,
        shape = CircleShape,
        color = if(!useDarkMode) Color.White.copy(alpha = 0.6f) else Color(0xFF1F2A24).copy(alpha = 0.37f),
        modifier = Modifier
            .size(48.dp)
            .graphicsLayer(scaleX = scale, scaleY = scale)
            .border(1.dp, if(!useDarkMode) Color.White else Color(0xFF81C784).copy(alpha = 0.52f), CircleShape),
        shadowElevation = 4.dp
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = if(useDarkMode) ForestGreenDark else ForestGreen,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
fun SettingsSection(title: String, darkMode: String, textsize: String, content: @Composable ColumnScope.() -> Unit) {
    val textscale = textScaleFor(textsize)

    val useDarkMode = when (darkMode) {
        "On" -> true
        "Off" -> false
        else -> isSystemInDarkTheme() // "Auto" follows system
    }
    Column {
        Text(
            text = title.uppercase(),
            style = MaterialTheme.typography.labelLarge,
            fontSize = textscale.title,
            color = if (useDarkMode) ForestGreenDark else ForestGreen,
            modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
        )
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = if (useDarkMode) Color.Black else Color.White,
            shadowElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column { content() }
        }
    }
}

@Composable
fun SettingsToggle(title: String, subtitle: String, checked: Boolean, darkMode: String, textsize: String, onCheckedChange: (Boolean) -> Unit) {
    val textscale = textScaleFor(textsize)

    // Determine if we should use Dark Mode
    val useDarkMode = when (darkMode) {
        "On" -> true
        "Off" -> false
        else -> isSystemInDarkTheme() // "Auto" follows system
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.SemiBold, fontSize = textscale.title,
                color = if (useDarkMode) Color.White else Color.Black
                )
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray,
                fontSize = textscale.subtitle
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(checkedThumbColor = ForestGreen, checkedTrackColor = ForestGreen.copy(alpha = 0.3f))
        )
    }
}

@Composable
fun SettingsClickable(title: String, subtitle: String, enabled: Boolean, darkMode: String, textsize: String, onClick: () -> Unit) {
    val textscale = textScaleFor(textsize)

    val useDarkMode = when (darkMode) {
        "On" -> true
        "Off" -> false
        else -> isSystemInDarkTheme() // "Auto" follows system
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled) { onClick() }
            .padding(16.dp)
            .alpha(if (enabled) 1f else 0.38f),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.SemiBold, fontSize = textscale.title,
                color = if (useDarkMode) Color.White else Color.Black
            )
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = Color.Gray, fontSize = textscale.subtitle)
        }
    }
}

@Composable
fun SettingsDropdown(label: String, selectedValue: String, options: List<String>, darkMode: String, textsize: String, onSelect: (String) -> Unit) {
    val textscale = textScaleFor(textsize)

    val useDarkMode = when (darkMode) {
        "On" -> true
        "Off" -> false
        else -> isSystemInDarkTheme() // "Auto" follows system
    }
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxWidth().clickable { expanded = true }.padding(16.dp)) {
        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Text(label, fontWeight = FontWeight.SemiBold,
                color = if (useDarkMode) Color.White else Color.Black,
                fontSize = textscale.title
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(selectedValue, color = ForestGreen, fontSize = textscale.subtitle)
                Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = ForestGreen)
            }
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            containerColor = if (!useDarkMode) Color.White else Color.Black
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option,
                        color = if (!useDarkMode) Color.Black else Color.White,
                        fontSize = textscale.subtitle
                    ) },
                    onClick = {
                        onSelect(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun AboutRow(label: String, value: String, darkMode: String, textsize: String) {
    val textscale = textScaleFor(textsize)

    val useDarkMode = when (darkMode) {
        "On" -> true
        "Off" -> false
        else -> isSystemInDarkTheme() // "Auto" follows system
    }
    Row(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = Color.Gray, fontSize = textscale.title)
        Text(value, fontWeight = FontWeight.Bold, color = if(!useDarkMode) EmeraldDark else ForestGreen, fontSize = textscale.subtitle)
    }
}