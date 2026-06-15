package com.acycycy.travelog

import android.content.Intent
import android.location.Geocoder
import android.os.Bundle
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

class MapPickerActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mapView: MapView
    private lateinit var btnConfirm: Button
    private lateinit var tvHint: TextView
    private lateinit var etSearch: EditText
    private var currentMap: GoogleMap? = null
    private val markers = mutableListOf<Marker>()
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map_picker)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "위치 선택"

        mapView = findViewById(R.id.mapPickerView)
        btnConfirm = findViewById(R.id.btnConfirmLocation)
        tvHint = findViewById(R.id.tvPickerHint)
        etSearch = findViewById(R.id.etLocationSearch)
        val btnSearch = findViewById<Button>(R.id.btnSearchLocation)

        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)

        btnSearch.setOnClickListener { performSearch() }
        etSearch.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) { performSearch(); true } else false
        }

        btnConfirm.setOnClickListener {
            val result = Intent().apply {
                putExtra("locations", markers.joinToString("|") {
                    "${it.position.latitude},${it.position.longitude}"
                })
            }
            setResult(RESULT_OK, result)
            finish()
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        currentMap = googleMap

        // 기존 위치들 복원
        val existingLocations = intent.getStringExtra("locations")
        if (!existingLocations.isNullOrBlank()) {
            existingLocations.split("|").forEachIndexed { idx, part ->
                try {
                    val coords = part.trim().split(",")
                    if (coords.size == 2) {
                        val latLng = LatLng(coords[0].toDouble(), coords[1].toDouble())
                        val marker = googleMap.addMarker(
                            MarkerOptions().position(latLng).title("위치 ${idx + 1}")
                        )
                        if (marker != null) markers.add(marker)
                    }
                } catch (e: Exception) { }
            }
            if (markers.isNotEmpty()) {
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(markers[0].position, 10f))
                updateHint()
                btnConfirm.isEnabled = true
            }
        } else {
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(36.5, 127.5), 7f))
        }

        // 탭 → 핀 추가
        googleMap.setOnMapClickListener { latLng ->
            val marker = googleMap.addMarker(
                MarkerOptions().position(latLng).title("위치 ${markers.size + 1}")
            )
            if (marker != null) {
                markers.add(marker)
                updateHint()
                btnConfirm.isEnabled = true
            }
        }

        // 핀 탭 → 삭제
        googleMap.setOnMarkerClickListener { marker ->
            marker.remove()
            markers.remove(marker)
            // 번호 재정렬
            markers.forEachIndexed { idx, m -> m.title = "위치 ${idx + 1}" }
            updateHint()
            btnConfirm.isEnabled = markers.isNotEmpty()
            true
        }
    }

    private fun updateHint() {
        tvHint.text = when (markers.size) {
            0 -> "지도를 탭하여 위치를 추가하세요"
            1 -> "1개 선택됨 · 탭하여 추가, 핀을 탭하면 삭제"
            else -> "${markers.size}개 선택됨 · 탭하여 추가, 핀을 탭하면 삭제"
        }
    }

    private fun performSearch() {
        val query = etSearch.text.toString().trim()
        if (query.isBlank()) return

        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(etSearch.windowToken, 0)

        scope.launch {
            val latLng = withContext(Dispatchers.IO) {
                try {
                    @Suppress("DEPRECATION")
                    val results = Geocoder(this@MapPickerActivity, Locale.KOREAN)
                        .getFromLocationName(query, 1)
                    if (!results.isNullOrEmpty()) LatLng(results[0].latitude, results[0].longitude)
                    else null
                } catch (e: Exception) { null }
            }
            if (latLng != null) {
                currentMap?.let { map ->
                    val marker = map.addMarker(MarkerOptions().position(latLng).title(query))
                    if (marker != null) {
                        markers.add(marker)
                        map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 12f))
                        updateHint()
                        btnConfirm.isEnabled = true
                    }
                }
            } else {
                Toast.makeText(this@MapPickerActivity, "위치를 찾을 수 없습니다", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onResume() { super.onResume(); mapView.onResume() }
    override fun onPause() { super.onPause(); mapView.onPause() }
    override fun onDestroy() { super.onDestroy(); mapView.onDestroy(); scope.cancel() }
    override fun onLowMemory() { super.onLowMemory(); mapView.onLowMemory() }
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView.onSaveInstanceState(outState)
    }

    override fun onSupportNavigateUp(): Boolean { finish(); return true }
}
