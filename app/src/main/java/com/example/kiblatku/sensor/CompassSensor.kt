package com.example.kiblatku.sensor

import android.content.Context
import android.hardware.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlin.math.abs

class CompassSensor(context: Context) : SensorEventListener {

    private val sensorManager =
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    private val accelerometer =
        sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    private val magnetometer =
        sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

    // Raw sensor buffers
    private val gravity = FloatArray(3)
    private val geomagnetic = FloatArray(3)

    // Output (filtered)
    var azimuth by mutableStateOf(0f)
        private set

    var accuracy by mutableStateOf(SensorManager.SENSOR_STATUS_UNRELIABLE)
        private set

    // --- FILTER CONFIG ---
    private var filteredAzimuth = 0f
    private val alpha = 0.1f   // 0.05 = lebih halus | 0.1 = rekomendasi | 0.2 = lebih responsif

    fun start() {
        sensorManager.registerListener(
            this,
            accelerometer,
            SensorManager.SENSOR_DELAY_UI
        )

        sensorManager.registerListener(
            this,
            magnetometer,
            SensorManager.SENSOR_DELAY_UI
        )
    }

    fun stop() {
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent) {

        when (event.sensor.type) {
            Sensor.TYPE_ACCELEROMETER ->
                System.arraycopy(event.values, 0, gravity, 0, 3)

            Sensor.TYPE_MAGNETIC_FIELD ->
                System.arraycopy(event.values, 0, geomagnetic, 0, 3)
        }

        val R = FloatArray(9)
        val I = FloatArray(9)

        if (!SensorManager.getRotationMatrix(R, I, gravity, geomagnetic)) {
            return
        }

        val orientation = FloatArray(3)
        SensorManager.getOrientation(R, orientation)

        // Raw azimuth (0..360)
        val rawAzimuth =
            (Math.toDegrees(orientation[0].toDouble()).toFloat() + 360f) % 360f

        // --- WRAP-AWARE LOW PASS FILTER ---
        // Menghindari masalah 359° -> 0°
        val delta = ((rawAzimuth - filteredAzimuth + 540f) % 360f) - 180f

        filteredAzimuth = (filteredAzimuth + alpha * delta + 360f) % 360f

        azimuth = filteredAzimuth
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        if (sensor?.type == Sensor.TYPE_MAGNETIC_FIELD) {
            this.accuracy = accuracy
        }
    }
}
