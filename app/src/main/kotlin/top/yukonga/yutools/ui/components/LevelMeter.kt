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

        val circle1X = centerX + roll * 40
        val circle1Y = centerY - pitch * 40
        val circle2X = centerX - roll * 40
        val circle2Y = centerY + pitch * 40

        val largerValue = if (abs(pitch) > abs(roll)) "${pitch.toInt()}°" else "${roll.toInt()}°"
        val textBounds = Rect()

        val angle = Math.toRadians(tiltAngle.toDouble())
        val verticalOffset = if (pitch > 0f) (pitch - 90) * 45 else (90 + pitch) * 40
        val horizontalOffset = if (roll > 0f) (90 - roll) * 45 else (-90 - roll) * 40

        var startX = centerX + size.width * sin(angle).toFloat()
        var endX = centerX - size.width * sin(angle).toFloat()
        var startY = centerY + size.width * cos(angle).toFloat() + verticalOffset
        var endY = centerY - size.width * cos(angle).toFloat() + verticalOffset

        if (((tilt > -90 && tilt < 90) && (roll > 45 && roll < 135)) || ((tilt > 90 || tilt < -90) && (roll > -135 && roll < -45))) {
            startX = centerX + size.height * sin(angle).toFloat() + horizontalOffset
            endX = centerX - size.height * sin(angle).toFloat() + horizontalOffset
            startY = centerY + size.height * cos(angle).toFloat()
            endY = centerY - size.height * cos(angle).toFloat()
        }

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

        if (!(abs(pitch) > 60 || abs(roll) > 60)) {
            drawContext.canvas.nativeCanvas.drawText(largerValue, centerX, centerY + textBounds.height() / 2, paint)
        } else {
            val crossSize = 20f
            drawContext.canvas.nativeCanvas.drawLine(centerX - crossSize, centerY, centerX + crossSize, centerY, paint)
            drawContext.canvas.nativeCanvas.drawLine(centerX, centerY - crossSize, centerX, centerY + crossSize, paint)
            drawContext.canvas.nativeCanvas.drawLine(centerX, 0f, centerX, 150f, paint)
            drawContext.canvas.nativeCanvas.drawLine(centerX, size.height, centerX, size.height - 320f, paint)
            drawContext.canvas.nativeCanvas.drawLine(0f, centerY, 50f, centerY, paint)
            drawContext.canvas.nativeCanvas.drawLine(size.width, centerY, size.width - 50f, centerY, paint)

        }

        if (abs(pitch) > 45 || abs(roll) > 45 || abs(tilt) > 45) {
            drawContext.canvas.nativeCanvas.drawLine(startX, startY, endX, endY, paint)
        }


        drawContext.canvas.nativeCanvas.restoreToCount(saveLayer)
    }
}

// tilt 为 90 / pitch 为 90 / roll 为 0 时手机竖屏垂直放置，手机头部向上
// 此次 tilt < 90 时为手机向左倾斜，tilt > 90 时为手机向右倾斜

// tilt 为 -90 / pitch 为 -90 / roll 为 0 时手机竖屏垂直放置，手机底部向上
// 此次 tilt < -90 时为手机向左倾斜，tilt > -90 时为手机向右倾斜

// tilt 为 0 / pitch 为 0 / roll 为 0 时手机屏幕正面朝上水平放置
// 此时 roll > 0 时为手机向左倾斜，roll < 0 时为手机向右倾斜， pitch > 0 时为手机向前倾斜，pitch < 0 时为手机向后倾斜

// tilt 为 0 / pitch 为 180 / roll 为 180 时手机屏幕反面朝下放置
// 此时 roll > -180 时为手机向左倾斜，roll < 180 时为手机向右倾斜， pitch < 180 时为手机向前倾斜，pitch > -180  时为手机向后倾斜

// tilt 为 0 / pitch 为 0 / roll 为 90 时手机横屏垂直放置，手机头部向左，底部向右
// 此时 pitch < 0 时为手机向左倾斜，pitch > 0 时为手机向右倾斜

// tilt 为 180 / pitch 为 0 / roll 为 -90 时手机横屏垂直放置，手机头部向右，底部向左
// 此时 pitch > 0 时为手机向左倾斜，pitch < 0 时为手机向右倾斜
