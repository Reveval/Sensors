package ru.startandroid.develop.p1372acceleration

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import java.util.*
import kotlin.concurrent.timerTask

//датчик акселерометра
class MainActivity : AppCompatActivity() {
    lateinit var textView: TextView
    lateinit var sensorManager: SensorManager
    lateinit var sensorAccel: Sensor
    lateinit var sensorLinAccel: Sensor
    lateinit var sensorGravity: Sensor
    lateinit var timer: Timer

    /*
        В onCreate мы получаем три сенсора:
            TYPE_ACCELEROMETER – ускорение, включая гравитацию
            TYPE_LINEAR_ACCELERATION – ускорение (чистое, без гравитации)
            TYPE_GRAVITY - гравитация
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        textView = findViewById(R.id.tvText)
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        sensorAccel = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        sensorLinAccel = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)
        sensorGravity = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY)
    }

    /*
        В onResume регистрируем один слушатель listener на все три сенсора. И запускаем таймер,
            который будет каждые 400 мсек отображать данные в TextView.
     */
    override fun onResume() {
        super.onResume()
        sensorManager.run {
            registerListener(listener, sensorAccel, SensorManager.SENSOR_DELAY_NORMAL)
            registerListener(listener, sensorLinAccel, SensorManager.SENSOR_DELAY_NORMAL)
            registerListener(listener, sensorGravity, SensorManager.SENSOR_DELAY_NORMAL)
        }

        timer = Timer()
        val task = timerTask {
            runOnUiThread {
                showInfo()
            }
        }
        timer.schedule(task, 0, 400)
    }

    /*
        В onPause отписываем слушателя от всех сенсоров, вызывая метод unregisterListener, но не
            указывая конкретный сенсор. И отключаем таймер.
     */
    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(listener)
        timer.cancel()
    }

    private fun format(values: Array<Float?>) : String {
        return String.format("%1$.1f\t\t%2$.1f\t\t%3$.1f", values[0], values[1], values[2])
    }

    private fun showInfo() {
        val message = """Accelerometer: ${format(valuesAccel)}
            |LinAccel: ${format(valuesLinAccel)}
            |Gravity: ${format(valuesGravity)}
        """.trimMargin()
        textView.text = message
    }

    //valuesAccel – массив для данных с сенсора ускорения (включая гравитацию)
    val valuesAccel = arrayOfNulls<Float>(3)
    //valuesLinAccel – массив для данных с сенсора ускорения без гравитации
    val valuesLinAccel = arrayOfNulls<Float>(3)
    //valuesGravity – массив для данных с сенсора гравитации
    val valuesGravity = arrayOfNulls<Float>(3)

    /*
        В слушателе listener в методе onSensorChanged мы определяем тип сенсора и пишем данные в
            соответствующие массивы:
     */
    private val listener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent?) {
            if (event != null) {
                when(event.sensor.type) {
                    Sensor.TYPE_ACCELEROMETER -> {
                        for (i in 0 until 3) {
                            valuesAccel[i] = event.values[i]
                        }
                    }

                    Sensor.TYPE_LINEAR_ACCELERATION -> {
                        for (i in 0 until 3) {
                            valuesLinAccel[i] = event.values[i]
                        }
                    }

                    Sensor.TYPE_GRAVITY -> {
                        for (i in 0 until 3) {
                            valuesGravity[i] = event.values[i]
                        }
                    }
                }
            }
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
    }
}