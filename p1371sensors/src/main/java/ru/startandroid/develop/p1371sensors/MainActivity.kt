package ru.startandroid.develop.p1371sensors

import android.hardware.*
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.annotation.RequiresApi

//Получим список сенсоров и отдельно считаем информацию о датчике освещенности
class MainActivity : AppCompatActivity() {
    lateinit var textView: TextView
    lateinit var sensorManager: SensorManager
    lateinit var sensors: List<Sensor>
    lateinit var sensorLight: Sensor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        textView = findViewById(R.id.tvText)
        /*
            В onCreate получаем SensorManager. У него запрашиваем полный список сенсоров, используя
            метод getSensorList и передавая туда тип сенсора TYPE_ALL.
         */
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        sensors = sensorManager.getSensorList(Sensor.TYPE_ALL)
        /*
            Чтобы получить конкретный сенсор (Sensor), вызываем метод getDefaultSensor. Передаем
                тип TYPE_LIGHT и получаем сенсор света.
         */
        sensorLight = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)
    }

    fun onClickSensList(view: View) {
        sensorManager.unregisterListener(listenerLight, sensorLight)
        var info = ""

        for(sensor in sensors) {
            info = """name = ${sensor.name}, type = ${sensor.type}
                |vendor = ${sensor.vendor}, version = ${sensor.version}
                |max = ${sensor.maximumRange}, resolution = ${sensor.resolution}
                |----------------------------------------
            """.trimMargin()
            Log.d("myLogs", info)
        }

    }

    /*
        В методе onClickSensLight мы используем метод registerListener, чтобы на ранее полученный
            сенсор света (sensorLight) повесить своего слушателя listenerLight. Третий параметр
            метода – скорость получения новых данных. Т.е. насколько часто вам необходимо получать
            данные от сенсора. Есть 4 скорости в порядке убывания: SENSOR_DELAY_NORMAL,
            SENSOR_DELAY_UI,  SENSOR_DELAY_GAME,  SENSOR_DELAY_FASTEST.
     */
    fun onClickSensLight(view: View) {
        sensorManager.registerListener(listenerLight, sensorLight, SensorManager.SENSOR_DELAY_NORMAL)
    }

    /*
        В onPause мы отписываем своего слушателя от сенсора света. Тут, как обычно, рекомендуется
            отписываться как только данные вам не нужны, чтобы не расходовать зря батарею.
     */
    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(listenerLight, sensorLight)
    }

    //listenerLight – слушатель, реализует интерфейс SensorEventListener. У него два метода:
    @RequiresApi(Build.VERSION_CODES.KITKAT)
    val listenerLight = object : SensorEventListener {
        //onSensorChanged – здесь то мы и получаем данные от сенсора в объекте SensorEvent.
        override fun onSensorChanged(event: SensorEvent?) {
            textView.text = event?.values?.get(0).toString()
        }

        /*
            onAccuracyChanged – вызывается, когда меняется точность данных сенсора и в начале
                получения данных. Дает нам объект-сенсор и уровень точности:
                SENSOR_STATUS_ACCURACY_HIGH – максимально возможная точность
                SENSOR_STATUS_ACCURACY_MEDIUM – средняя точность, калибровка могла бы улучшить
                результат
                SENSOR_STATUS_ACCURACY_LOW – низкая точность, необходима калибровка
                SENSOR_STATUS_UNRELIABLE – данные сенсора совсем ни о чем. Либо нужна калибровка,
                либо невозможно чтение данных.
         */
        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
    }
}