package ru.startandroid.develop.p1381location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.core.app.ActivityCompat
import java.util.*

//Напишем простое приложение, которое будет запрашивать и отображать координаты.
class MainActivity : AppCompatActivity() {
    lateinit var tvEnabledGPS: TextView
    lateinit var tvStatusGPS: TextView
    lateinit var tvLocationGPS: TextView
    lateinit var tvEnabledNet: TextView
    lateinit var tvStatusNet: TextView
    lateinit var tvLocationNet: TextView

    lateinit var locationManager: LocationManager

    /*
        В onCreate определяем TextView-компоненты и получаем LocationManager, через который и
            будем работать.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvEnabledGPS = findViewById(R.id.tvEnabledGPS)
        tvStatusGPS = findViewById(R.id.tvStatusGPS)
        tvLocationGPS = findViewById(R.id.tvLocationGPS)
        tvEnabledNet = findViewById(R.id.tvEnabledNet)
        tvStatusNet = findViewById(R.id.tvStatusNet)
        tvLocationNet = findViewById(R.id.tvLocationNet)

        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
    }

    //В onResume вешаем слушателя с помощью метода requestLocationUpdates.
    override fun onResume() {
        super.onResume()
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        /*
            На вход передаем:
                - тип провайдера: GPS_PROVIDER или NETWORK_PROVIDER
                - минимальное время (в миллисекундах) между получением данных. Я укажу здесь 10
                секунд, мне этого вполне хватит. Если хотите получать координаты без задержек –
                передавайте 0. Но учитывайте, что это только минимальное время. Реальное ожидание
                может быть дольше.
                - минимальное расстояние (в метрах). Т.е. если ваше местоположение изменилось на
                указанное кол-во метров, то вам придут новые координаты.
                - слушатель, объект locationListener
         */
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000 * 10,
            10F, locationListener)
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000 * 10,
            10F, locationListener)
        //Также здесь обновляем на экране инфу о включенности провайдеров.
        checkEnabled()
    }

    //В onPause отключаем слушателя методом removeUpdates.
    override fun onPause() {
        super.onPause()
        locationManager.removeUpdates(locationListener)
    }

    //locationListener – слушатель, реализует интерфейс LocationListener с методами:
    private val locationListener = object : LocationListener {
        /*
            onLocationChanged – новые данные о местоположении, объект Location. Здесь мы вызываем
                свой метод showLocation, который на экране отобразит данные о местоположении.
         */
        override fun onLocationChanged(location: Location) {
            showLocation(location)
        }

        /*
            onProviderDisabled – указанный провайдер был отключен юзером. В этом методе вызываем
                свой метод checkEnabled, который на экране обновит текущие статусы провайдеров.
         */
        override fun onProviderDisabled(provider: String) {
            checkEnabled()
        }

        /*
            onProviderEnabled – указанный провайдер был включен юзером. Тут также вызываем
                checkEnabled. Далее методом getLastKnownLocation (он может вернуть null)
                запрашиваем последнее доступное местоположение от включенного провайдера и
                отображаем его. Оно может быть вполне актуальным, если вы до этого использовали
                какое-либо приложение с определением местоположения.
         */
        override fun onProviderEnabled(provider: String) {
            checkEnabled()
            if (ActivityCompat.checkSelfPermission(
                    this@MainActivity,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    this@MainActivity,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) return
            locationManager.getLastKnownLocation(provider)?.let { showLocation(it) }
        }

        /*
            onStatusChanged – изменился статус указанного провайдера. В поле status могут быть
                значения OUT_OF_SERVICE (данные будут недоступны долгое время),
                TEMPORARILY_UNAVAILABLE (данные временно недоступны), AVAILABLE (все ок, данные
                доступны). В этом методе мы просто выводим новый статус на экран.
         */
        @SuppressLint("SetTextI18n")
        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
            when(provider) {
                LocationManager.GPS_PROVIDER -> tvStatusGPS.text = "Status: $status"
                LocationManager.NETWORK_PROVIDER -> tvStatusNet.text = "Status: $status"
            }
        }
    }

    /*
        showLocation на вход берет Location, определяет его провайдера методом getProvider и
        отображает координаты в соответствующем текстовом поле.
     */
    private fun showLocation(location: Location) {
        when(location.provider) {
            LocationManager.GPS_PROVIDER -> tvLocationGPS.text = formatLocation(location)
            LocationManager.NETWORK_PROVIDER -> tvLocationNet.text = formatLocation(location)
        }
    }

    /*
        formatLocation на вход берет Location, читает из него данные и форматирует из них строку.
            Какие данные он берет: getLatitude – широта, getLongitude – долгота, getTime – время
            определения.
     */
    private fun formatLocation(location: Location) : String {
        return String.format("Coordinates: lat = %1\$.4f, lon = %2\$.4f, time = %3\$tF %3\$tT",
            location.latitude, location.longitude, Date().time)
    }

    @SuppressLint("SetTextI18n")
    private fun checkEnabled() {
        tvEnabledGPS.text = "Enabled: ${locationManager.isProviderEnabled(LocationManager.
            GPS_PROVIDER)}"
        tvEnabledNet.text = "Enabled: ${locationManager.isProviderEnabled(LocationManager.
            NETWORK_PROVIDER)}"

    }

    /*
        Метод onClickLocationSettings срабатывает по нажатию кнопки Location settings и открывает
            настройки, чтобы пользователь мог включить или выключить провайдер. Для этого
            используется Intent с action = ACTION_LOCATION_SOURCE_SETTINGS.
     */
    fun onClickLocationSettings(view: View) {
        startActivity(Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS))
    }
}