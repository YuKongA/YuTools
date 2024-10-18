package top.yukonga.yutools.ui.components

import android.content.Context
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.graphics.Typeface
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import top.yukonga.miuix.kmp.theme.MiuixTheme
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

const val ALPHA = 0.98f

@Composable
fun LevelMeter() {
    val context = LocalContext.current
    val sensor = Sensor.TYPE_ACCELEROMETER
    val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    val accelerometer = sensorManager.getDefaultSensor(sensor)

    var pitch by remember { mutableFloatStateOf(0f) }
    var roll by remember { mutableFloatStateOf(0f) }
    var tilt by remember { mutableFloatStateOf(0f) }
    var tiltAngle by remember { mutableFloatStateOf(0f) }
    val bgColor = MiuixTheme.colorScheme.onBackground

    val filteredValues = remember { FloatArray(3) }

    val sensorEventListener = remember {
        object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                if (event.sensor.type == sensor) {
                    for (i in event.values.indices) {
                        filteredValues[i] = ALPHA * filteredValues[i] + (1 - ALPHA) * event.values[i]
                    }
                    roll = (atan2(filteredValues[0], filteredValues[2]) * 180 / Math.PI).toFloat()
                    pitch = (atan2(filteredValues[1], filteredValues[2]) * 180 / Math.PI).toFloat()
                    tilt = (atan2(filteredValues[1], filteredValues[0]) * 180 / Math.PI).toFloat()
                    tiltAngle = Math.toDegrees(atan2(filteredValues[1].toDouble(), filteredValues[0].toDouble())).toFloat()
                }
            }

            override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}
        }
    }

    DisposableEffect(Unit) {
        sensorManager.registerListener(sensorEventListener, accelerometer, SensorManager.SENSOR_DELAY_FASTEST)
        onDispose {
            sensorManager.unregisterListener(sensorEventListener)
        }
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        val centerX = size.width / 2
        val centerY = size.height / 2

        val circle1X = centerX + roll * 50
        val circle1Y = centerY - pitch * 50
        val circle2X = centerX - roll * 50
        val circle2Y = centerY + pitch * 50

        val largerValue = if (abs(pitch) > abs(roll)) "${pitch.toInt()}°" else "${roll.toInt()}°"
        val textBounds = Rect()

        val angle = Math.toRadians(tiltAngle.toDouble())

        Log.d("LevelMeter", "pitch: $pitch, roll: $roll, tilt: $tilt")
        val startX = centerX + size.width * sin(angle).toFloat()//+ horizontalOffset
        val endX = centerX - size.width * sin(angle).toFloat()//+ horizontalOffset
        val startY = centerY + size.width * cos(angle).toFloat()// + verticalOffset
        val endY = centerY - size.width * cos(angle).toFloat()// + verticalOffset

        val saveLayer = drawContext.canvas.nativeCanvas.saveLayer(null, null)

        val paint = Paint().apply {
            isAntiAlias = true
            color = bgColor.toArgb()
            xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_OUT)
            strokeWidth = 5f
            textSize = 200f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            getTextBounds(largerValue, 0, largerValue.length, textBounds)
        }
        drawContext.canvas.nativeCanvas.drawCircle(circle1X, circle1Y, 297f, paint)
        drawContext.canvas.nativeCanvas.drawCircle(circle2X, circle2Y, 303f, paint)
        drawContext.canvas.nativeCanvas.drawText(largerValue, centerX, centerY + textBounds.height() / 2, paint)
        drawContext.canvas.nativeCanvas.drawLine(startX, startY, endX, endY, paint)

        drawContext.canvas.nativeCanvas.restoreToCount(saveLayer)
    }
}