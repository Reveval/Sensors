package ru.startandroid.develop.p1391googlemaps

import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory

import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

const val LOG_TAG = "myLogs"

//В onCreate мы находим наш фрагмент с картой и получаем от него объект GoogleMap методом getMap.
class MainActivity : AppCompatActivity(), OnMapReadyCallback {
    lateinit var mapFragment: SupportMapFragment
    lateinit var map: GoogleMap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    //реализуем метод OnMapReadyCallback
    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        //устнавливаем тип карты
        findViewById<Button>(R.id.btnTest).setOnClickListener {
            //map.mapType = GoogleMap.MAP_TYPE_SATELLITE
            //задаем объект с координатами Сиднея
            val sydney = LatLng(-34.0, 151.0)
            //добавляем маркер на карту
            map.addMarker(MarkerOptions()
                .position(sydney)
                .title("Marker in Sydney"))
            map.moveCamera(CameraUpdateFactory.newLatLng(sydney))
        }
    }
}