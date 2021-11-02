package ru.startandroid.develop.p1373orientation

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Surface
import android.view.WindowManager
import android.widget.TextView
import androidx.annotation.RequiresApi
import java.util.*
import kotlin.concurrent.timerTask

/*
    Ориентация
        Теперь попробуем использовать данные сенсора ускорения и добавим к ним данные сенсора
        магнитного поля. Эти два набора данных при определенных манипуляциях дадут нам углы наклона
        устройства. Угла будет три, по одному для каждой оси.
 */
class MainActivity : AppCompatActivity() {
    lateinit var textView: TextView
    lateinit var sensorManager: SensorManager
    lateinit var sensorAccel: Sensor
    lateinit var sensorMagnet: Sensor
    lateinit var timer: Timer

    var rotation = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        textView = findViewById(R.id.tvText)
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        //получаем сенсор акселерометра
        sensorAccel = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        //получаем сенсор магнитного поля
        sensorMagnet = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
    }

    /*
        В onResume вешаем слушателя и запускаем таймер, который каждые 400 мсек будет определять
            ориентацию девайса в пространстве и выводить эту инфу на экран. В переменную rotation
            получаем значение текущей ориентации экрана. Это нам понадобиться для корректного
            определения ориентации девайса.
     */
    @RequiresApi(Build.VERSION_CODES.R)
    override fun onResume() {
        super.onResume()
        sensorManager.run {
            registerListener(listener, sensorAccel, SensorManager.SENSOR_DELAY_NORMAL)
            registerListener(listener, sensorMagnet, SensorManager.SENSOR_DELAY_NORMAL)
        }

        timer = Timer()
        val task = timerTask {
            runOnUiThread {
                getDeviceOrientation()
                getActualDeviceOrientation()
                showInfo()
            }
        }

        timer.schedule(task, 0, 400)
        rotation = display?.rotation ?: 0
    }

    //В onPause отключаем слушателя и таймер.
    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(listener)
        timer.cancel()
    }

    private fun format(values: FloatArray) : String {
        return String.format("%1$.1f\t\t%2$.1f\t\t%3$.1f", values[0], values[1], values[2])
    }

    private fun showInfo() {
        val message = """Orientation: ${format(valuesResult)}
            |Orientation2; ${format(valuesResult2)}
        """.trimMargin()
        textView.text = message
    }

    /*
        Метод getDeviceOrientation определяет текущую ориентацию девайса в пространстве без учета
            поворота экрана. Для этого мы сначала вызваем метод getRotationMatrix, который берет
            данные ускорения и магнитного поля и формирует из них матрицу данных в переменную r.
            Далее метод getOrientation из этой матрицы позволяет получить массив значений
            (в радианах) поворота трех осей. Остается перевести радианы в градусы методом
            toDegrees и у нас есть готовый массив с углами наклона девайса.
     */
    private val r = FloatArray(9)
    private fun getDeviceOrientation() {
        SensorManager.getRotationMatrix(r, null, valuesAccel, valuesMagnet)
        SensorManager.getOrientation(r, valuesResult)

        valuesResult[0] = Math.toDegrees(valuesResult[0].toDouble()).toFloat()
        valuesResult[1] = Math.toDegrees(valuesResult[1].toDouble()).toFloat()
        valuesResult[2] = Math.toDegrees(valuesResult[2].toDouble()).toFloat()
    }

    /*
        Метод getActualDeviceOrientation аналогичен методу getDeviceOrientation, но он позволяет
            учесть ориентацию экрана. Для этого мы дополнительно вызываем метод
            remapCoordinateSystem, который пересчитает нам матрицу. С помощью переменных x_axis и
            y_axis мы передаем в этот метод данные о том, как оси поменялись местами при повороте
            экрана.
     */
    val inR = FloatArray(9)
    val outR = FloatArray(9)
    private fun getActualDeviceOrientation() {
        SensorManager.getRotationMatrix(inR, null, valuesAccel, valuesMagnet)
        var axisX = SensorManager.AXIS_X
        val axisY = when(rotation) {
            Surface.ROTATION_90 -> {
                axisX = SensorManager.AXIS_Y
                SensorManager.AXIS_MINUS_X
            }
            Surface.ROTATION_180 -> SensorManager.AXIS_MINUS_Y
            Surface.ROTATION_270 -> {
                axisX = SensorManager.AXIS_MINUS_Y
                SensorManager.AXIS_X
            }
            else -> SensorManager.AXIS_Y
        }
        SensorManager.remapCoordinateSystem(inR, axisX, axisY, outR)
        SensorManager.getOrientation(outR, valuesResult2)
        valuesResult2[0] = Math.toDegrees(valuesResult2[0].toDouble()).toFloat()
        valuesResult2[1] = Math.toDegrees(valuesResult2[1].toDouble()).toFloat()
        valuesResult2[2] = Math.toDegrees(valuesResult2[2].toDouble()).toFloat()
    }

    val valuesAccel = FloatArray(3)
    val valuesMagnet = FloatArray(3)
    val valuesResult = FloatArray(3)
    val valuesResult2 = FloatArray(3)

    private val listener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent?) {
            if (event != null) {
                when(event.sensor.type) {
                    Sensor.TYPE_ACCELEROMETER -> {
                        for (i in 0 until 3) {
                            valuesAccel[i] = event.values[i]
                        }
                    }

                    Sensor.TYPE_MAGNETIC_FIELD -> {
                        for (i in 0 until 3) {
                            valuesMagnet[i] = event.values[i]
                        }
                    }
                }
            }
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
    }
}