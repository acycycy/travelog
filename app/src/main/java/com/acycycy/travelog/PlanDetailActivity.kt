package com.acycycy.travelog

import android.app.AlertDialog
import android.content.Intent
import android.location.Geocoder
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

class PlanDetailActivity : AppCompatActivity() {

    private lateinit var mapView: MapView
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_plan_detail)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "계획 상세"

        val tvDestination = findViewById<TextView>(R.id.tvPlanDetailDestination)
        val tvDate = findViewById<TextView>(R.id.tvPlanDetailDate)
        val tvMemo = findViewById<TextView>(R.id.tvPlanDetailMemo)
        val btnEdit = findViewById<Button>(R.id.btnPlanDetailEdit)
        val btnComplete = findViewById<Button>(R.id.btnPlanComplete)
        val btnDelete = findViewById<Button>(R.id.btnPlanDetailDelete)
        mapView = findViewById(R.id.planDetailMapView)

        val id = intent.getIntExtra("id", -1)
        val destination = intent.getStringExtra("destination") ?: ""
        val plannedDate = intent.getStringExtra("plannedDate") ?: ""
        val memo = intent.getStringExtra("memo") ?: ""
        val location = intent.getStringExtra("location")

        tvDestination.text = destination
        tvDate.text = plannedDate
        tvMemo.text = memo

        mapView.onCreate(savedInstanceState)
        loadMapLocation(location, destination)

        btnEdit.setOnClickListener {
            val editIntent = Intent(this, AddPlanActivity::class.java).apply {
                putExtra("mode", "edit")
                putExtra("id", id)
                putExtra("destination", destination)
                putExtra("plannedDate", plannedDate)
                putExtra("memo", memo)
                putExtra("location", location)
            }
            startActivity(editIntent)
            finish()
        }

        btnComplete.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("여행 완료")
                .setMessage("\"$destination\" 여행을 완료하고 기록에 추가할까요?")
                .setPositiveButton("추가") { _, _ ->
                    val dbHelper = TravelDatabaseHelper(this)
                    val result = dbHelper.insertTravel(destination, plannedDate, memo, null, 0, location)
                    if (result != -1L) {
                        Toast.makeText(this, "여행 기록에 추가되었습니다!", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "추가에 실패했습니다", Toast.LENGTH_SHORT).show()
                    }
                    finish()
                }
                .setNegativeButton("취소", null)
                .show()
        }

        btnDelete.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("삭제")
                .setMessage("$destination 계획을 삭제하시겠습니까?")
                .setPositiveButton("예") { _, _ ->
                    TravelDatabaseHelper(this).deletePlan(id)
                    finish()
                }
                .setNegativeButton("아니오", null)
                .show()
        }
    }

    private fun loadMapLocation(location: String?, destination: String) {
        scope.launch {
            val points = withContext(Dispatchers.IO) {
                if (!location.isNullOrBlank()) {
                    location.split("|").mapNotNull { part ->
                        try {
                            val coords = part.trim().split(",")
                            if (coords.size == 2) LatLng(coords[0].toDouble(), coords[1].toDouble()) else null
                        } catch (e: Exception) { null }
                    }
                } else {
                    try {
                        @Suppress("DEPRECATION")
                        val results = Geocoder(this@PlanDetailActivity, Locale.KOREAN)
                            .getFromLocationName(destination, 1)
                        if (!results.isNullOrEmpty()) listOf(LatLng(results[0].latitude, results[0].longitude))
                        else emptyList()
                    } catch (e: Exception) { emptyList() }
                }
            }
            if (points.isEmpty()) { mapView.visibility = View.GONE; return@launch }
            mapView.getMapAsync { googleMap ->
                googleMap.uiSettings.isScrollGesturesEnabled = false
                googleMap.uiSettings.isZoomGesturesEnabled = false
                googleMap.uiSettings.isRotateGesturesEnabled = false
                googleMap.uiSettings.isTiltGesturesEnabled = false
                googleMap.uiSettings.isZoomControlsEnabled = points.size > 1
                points.forEachIndexed { idx, latLng ->
                    googleMap.addMarker(MarkerOptions().position(latLng).title(
                        if (points.size == 1) destination else "위치 ${idx + 1}"
                    ))
                }
                if (points.size == 1) {
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(points[0], 12f))
                } else {
                    val bounds = LatLngBounds.Builder().apply { points.forEach { include(it) } }.build()
                    mapView.post {
                        try { googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 80)) }
                        catch (e: Exception) { googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(points[0], 8f)) }
                    }
                }
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

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
