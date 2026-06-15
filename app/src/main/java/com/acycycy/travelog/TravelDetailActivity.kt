package com.acycycy.travelog

import android.content.Intent
import android.graphics.BitmapFactory
import android.location.Geocoder
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.RatingBar
import android.widget.TextView
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

class TravelDetailActivity : AppCompatActivity() {

    private var travelTitle = ""
    private var travelDate = ""
    private var travelMemo = ""
    private var travelRating = 0
    private var travelLocation: String? = null
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private lateinit var mapView: MapView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_travel_detail)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "여행 상세"

        val tvTitle = findViewById<TextView>(R.id.tvDetailTitle)
        val tvDate = findViewById<TextView>(R.id.tvDetailDate)
        val tvMemo = findViewById<TextView>(R.id.tvDetailMemo)
        val ratingBarDetail = findViewById<RatingBar>(R.id.ratingBarDetail)
        val btnEdit = findViewById<Button>(R.id.btnDetailEdit)
        val photoContainer = findViewById<LinearLayout>(R.id.photoContainer)
        mapView = findViewById(R.id.mapView)

        val id = intent.getIntExtra("id", -1)
        travelTitle = intent.getStringExtra("title") ?: ""
        travelDate = intent.getStringExtra("date") ?: ""
        travelMemo = intent.getStringExtra("memo") ?: ""
        travelRating = intent.getIntExtra("rating", 0)
        travelLocation = intent.getStringExtra("location")
        val imageUri = intent.getStringExtra("imageUri")

        tvTitle.text = travelTitle
        tvDate.text = travelDate
        tvMemo.text = travelMemo
        ratingBarDetail.rating = travelRating.toFloat()

        // 사진 로드
        val dbHelper = TravelDatabaseHelper(this)
        val photos = dbHelper.getPhotosForTravel(id)
        if (photos.isNotEmpty()) {
            for (photo in photos) addPhotoDetailView(photoContainer, photo.imageUri, photo.comment)
        } else if (!imageUri.isNullOrBlank()) {
            addPhotoDetailView(photoContainer, imageUri, "")
        }

        // 지도 초기화
        mapView.onCreate(savedInstanceState)
        loadMapLocation()

        btnEdit.setOnClickListener {
            startActivity(Intent(this, AddTravelActivity::class.java).apply {
                putExtra("mode", "edit")
                putExtra("id", id)
                putExtra("title", travelTitle)
                putExtra("date", travelDate)
                putExtra("memo", travelMemo)
                putExtra("rating", travelRating)
                putExtra("imageUri", imageUri)
                putExtra("location", travelLocation)
            })
            finish()
        }
    }

    private fun loadMapLocation() {
        scope.launch {
            val points = withContext(Dispatchers.IO) {
                if (!travelLocation.isNullOrBlank()) {
                    // 저장된 위치 파싱 (단일 or 다중 "lat,lng|lat,lng")
                    travelLocation!!.split("|").mapNotNull { part ->
                        try {
                            val coords = part.trim().split(",")
                            if (coords.size == 2) LatLng(coords[0].toDouble(), coords[1].toDouble()) else null
                        } catch (e: Exception) { null }
                    }
                } else {
                    // Geocoder 폴백
                    try {
                        @Suppress("DEPRECATION")
                        val results = Geocoder(this@TravelDetailActivity, Locale.KOREAN)
                            .getFromLocationName(travelTitle, 1)
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
                        if (points.size == 1) travelTitle else "위치 ${idx + 1}"
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

    private fun addPhotoDetailView(container: LinearLayout, uriString: String, comment: String) {
        val view = LayoutInflater.from(this).inflate(R.layout.item_photo_detail, container, false)
        val ivPhoto = view.findViewById<ImageView>(R.id.ivPhotoDetail)
        val progress = view.findViewById<ProgressBar>(R.id.progressPhotoDetail)
        val tvComment = view.findViewById<TextView>(R.id.tvPhotoComment)

        if (comment.isNotBlank()) {
            tvComment.text = comment
            tvComment.visibility = View.VISIBLE
        }

        progress.visibility = View.VISIBLE
        scope.launch {
            val bitmap = withContext(Dispatchers.IO) {
                try {
                    contentResolver.openInputStream(Uri.parse(uriString))?.use {
                        BitmapFactory.decodeStream(it)
                    }
                } catch (e: Exception) { null }
            }
            progress.visibility = View.GONE
            if (bitmap != null) {
                ivPhoto.setImageBitmap(bitmap)
                ivPhoto.visibility = View.VISIBLE
            }
        }

        container.addView(view)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_detail, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_share -> {
                val shareText = buildString {
                    append("📍 $travelTitle\n")
                    append("📅 $travelDate\n")
                    if (travelMemo.isNotBlank()) append("📝 $travelMemo\n")
                    if (travelRating > 0) append("⭐ ${"★".repeat(travelRating)}")
                }
                startActivity(Intent.createChooser(
                    Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, shareText)
                        putExtra(Intent.EXTRA_SUBJECT, travelTitle)
                    }, "여행 공유"
                ))
                true
            }
            android.R.id.home -> { finish(); true }
            else -> super.onOptionsItemSelected(item)
        }
    }

    // MapView 라이프사이클 — 빠뜨리면 크래시
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
