package com.kylecorry.survival_aid.navigator.compass

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import com.kylecorry.survival_aid.navigator.normalizeAngle
import java.util.*
import kotlin.math.abs
import kotlin.math.floor

/**
 * A compass sensor
 */
class Compass (ctx: Context) : SensorEventListener, Observable() {

    private var sensorManager: SensorManager = ctx.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelReading = FloatArray(3)
    private val magnetReading = FloatArray(3)

    private val rotation = FloatArray(9)
    private val orientation = FloatArray(3)

    private var oldAngle = 0f

    private var started = false


    private val RAW_SMOOTHING = 0.95f
    private val OUTPUT_SMOOTHING = 0.5f
    private val OUTPUT_SMOOTHING_THRESHOLD = 30f

    /**
     * The direction in degrees that the compass is facing (called azimuth)
     */
    val azimuth: Float
        get() {
            updateOrientation()
            val newAngle = (Math.toDegrees(orientation[0].toDouble()).toFloat() + 360) % 360
            if (!closeTo(oldAngle, newAngle, OUTPUT_SMOOTHING_THRESHOLD)) {
                oldAngle = newAngle
            } else {
                oldAngle += OUTPUT_SMOOTHING * deltaAngle(oldAngle, newAngle)
            }
            return normalizeAngle(oldAngle + declination)
        }

    /**
     * The cardinal/inter-cardinal direction that the compass is facing
     */
    val direction: CompassDirection
        get(){
            val directions = CompassDirection.values()
            val a = azimuth
            directions.forEach {
                if (closeTo(a, it.azimuth, 22.5f)){
                    return it
                }
            }
            return CompassDirection.NORTH
        }

    /**
     * The declination in degrees to apply to the azimuth
     */
    var declination: Float = 0f

    /**
     * Start the compass sensor
     */
    fun start(){
        if (started) return
        sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY)?.also { accelerometer ->
            sensorManager.registerListener(
                this,
                accelerometer,
                SensorManager.SENSOR_DELAY_GAME
            )
        }
        sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)?.also { magneticField ->
            sensorManager.registerListener(
                this,
                magneticField,
                SensorManager.SENSOR_DELAY_GAME
            )
        }
        started = true
    }

    /**
     * Stop the compass sensor
     */
    fun stop(){
        if (!started) return
        sensorManager.unregisterListener(this)
        started = false
    }


    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Empty
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_GRAVITY) {
            applySmoothing(event.values, accelReading, RAW_SMOOTHING)
        } else if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
            applySmoothing(event.values, magnetReading, RAW_SMOOTHING)
        }
        setChanged()
        notifyObservers()
    }

    // Private helpers

    /**
     * Apply smoothing to an array
     */
    private fun applySmoothing(input: FloatArray, output: FloatArray, smoothingFactor: Float) {
        if (output.size != input.size) return
        for (i in input.indices){
            output[i] = smoothingFactor * output[i] + (1 - smoothingFactor) * input[i]
        }
    }

    /**
     * Update the orientation values
     */
    private fun updateOrientation(){
        SensorManager.getRotationMatrix(rotation, null, accelReading, magnetReading)

        var largestAccelAxis = 0
        for (i in accelReading.indices){
            if (abs(accelReading[i]) > abs(accelReading[largestAccelAxis])){
                largestAccelAxis = i
            }
        }

        // If the device is vertical, change the compass orientation to a different axis
        if (largestAccelAxis == 1) {
            SensorManager.remapCoordinateSystem(
                rotation,
                SensorManager.AXIS_X, SensorManager.AXIS_Z,
                rotation
            )
        }


        SensorManager.getOrientation(rotation, orientation)
    }

    /**
     * Detect if two angles are close to each other
     */
    private fun closeTo(current: Float, target: Float, within: Float): Boolean{
        val delta = deltaAngle(current, target)
        return abs(delta) <= within
    }

    /**
     * Get the difference between 2 angles
     */
    private fun deltaAngle(angle1: Float, angle2: Float): Float {
        var delta = angle2 - angle1
        delta += 180
        delta -= floor(delta / 360) * 360
        delta -= 180
        if (abs(abs(delta) - 180) <= Float.MIN_VALUE){
            delta = 180f
        }
        return delta
    }
}