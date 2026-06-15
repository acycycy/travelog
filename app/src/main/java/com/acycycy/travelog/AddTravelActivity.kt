package com.acycycy.travelog

import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.RatingBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.util.Pair as AndroidPair
import androidx.exifinterface.media.ExifInterface
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class AddTravelActivity : AppCompatActivity() {

    private lateinit var photoContainer: LinearLayout
    private lateinit var tvSelectedLocation: TextView
    private val photoEntries = mutableListOf<Uri>()
    private var selectedLocation: String? = null
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    private val imagePickerLauncher =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            if (uri != null) {
                try {
                    contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    photoEntries.add(uri)
                    addPhotoView(uri, "")
                    // 위치 미설정 시 사진 EXIF GPS 자동 추출
                    if (selectedLocation.isNullOrBlank()) extractGpsFromPhoto(uri)
                } catch (e: Exception) {
                    Toast.makeText(this, "사진을 불러올 수 없습니다", Toast.LENGTH_SHORT).show()
                }
            }
        }

    private val mapPickerLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val locations = result.data?.getStringExtra("locations") ?: return@registerForActivityResult
                selectedLocation = locations
                val count = locations.split("|").size
                tvSelectedLocation.text = "${count}곳 선택됨"
                tvSelectedLocation.setTextColor(0xFF26A69A.toInt())
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_travel)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "여행 기록"

        val etTitle = findViewById<TextInputEditText>(R.id.etTitle)
        val etDate = findViewById<TextInputEditText>(R.id.etDate)
        val etMemo = findViewById<TextInputEditText>(R.id.etMemo)
        val ratingBarInput = findViewById<RatingBar>(R.id.ratingBarInput)
        val btnSave = findViewById<Button>(R.id.btnSave)
        val btnAddPhoto = findViewById<Button>(R.id.btnAddPhoto)
        val btnPickLocation = findViewById<Button>(R.id.btnPickLocation)
        tvSelectedLocation = findViewById(R.id.tvSelectedLocation)
        photoContainer = findViewById(R.id.photoContainer)

        etDate.setOnClickListener { showDateRangePicker(etDate) }
        btnAddPhoto.setOnClickListener { imagePickerLauncher.launch(arrayOf("image/*")) }

        btnPickLocation.setOnClickListener {
            val intent = Intent(this, MapPickerActivity::class.java)
            if (!selectedLocation.isNullOrBlank()) intent.putExtra("locations", selectedLocation)
            mapPickerLauncher.launch(intent)
        }

        val dbHelper = TravelDatabaseHelper(this)
        val mode = intent.getStringExtra("mode")
        val travelId = intent.getIntExtra("id", -1)

        if (mode == "edit" && travelId != -1) {
            etTitle.setText(intent.getStringExtra("title"))
            etDate.setText(intent.getStringExtra("date"))
            etMemo.setText(intent.getStringExtra("memo"))
            ratingBarInput.rating = intent.getIntExtra("rating", 0).toFloat()
            btnSave.text = "수정"

            val existingLocation = intent.getStringExtra("location")
            if (!existingLocation.isNullOrBlank()) {
                selectedLocation = existingLocation
                val count = existingLocation.split("|").size
                tvSelectedLocation.text = "${count}곳 선택됨"
                tvSelectedLocation.setTextColor(0xFF26A69A.toInt())
            }

            val existingPhotos = dbHelper.getPhotosForTravel(travelId)
            if (existingPhotos.isNotEmpty()) {
                for (photo in existingPhotos) {
                    try {
                        val uri = Uri.parse(photo.imageUri)
                        photoEntries.add(uri)
                        addPhotoView(uri, photo.comment)
                    } catch (e: Exception) { }
                }
            } else {
                val oldUri = intent.getStringExtra("imageUri")
                if (!oldUri.isNullOrBlank()) {
                    try {
                        val uri = Uri.parse(oldUri)
                        photoEntries.add(uri)
                        addPhotoView(uri, "")
                    } catch (e: Exception) { }
                }
            }
        }

        btnSave.setOnClickListener {
            val title = etTitle.text.toString().trim()
            val date = etDate.text.toString().trim()
            val memo = etMemo.text.toString().trim()
            val rating = ratingBarInput.rating.toInt()
            val firstPhotoUri = if (photoEntries.isNotEmpty()) photoEntries[0].toString() else null

            if (title.isBlank() || date.isBlank() || memo.isBlank()) {
                Toast.makeText(this, "모든 항목을 입력해주세요", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (mode == "edit" && travelId != -1) {
                val result = dbHelper.updateTravel(travelId, title, date, memo, firstPhotoUri, rating, selectedLocation)
                if (result > 0) {
                    dbHelper.deletePhotosForTravel(travelId)
                    savePhotos(dbHelper, travelId)
                    Toast.makeText(this, "여행 기록이 수정되었습니다", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this, "수정에 실패했습니다", Toast.LENGTH_SHORT).show()
                }
            } else {
                val newId = dbHelper.insertTravel(title, date, memo, firstPhotoUri, rating, selectedLocation)
                if (newId != -1L) {
                    savePhotos(dbHelper, newId.toInt())
                    Toast.makeText(this, "여행 기록이 저장되었습니다", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this, "저장에 실패했습니다", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun savePhotos(dbHelper: TravelDatabaseHelper, travelId: Int) {
        for (i in photoEntries.indices) {
            val view = photoContainer.getChildAt(i) ?: continue
            val comment = view.findViewById<TextInputEditText>(R.id.etPhotoComment)
                ?.text?.toString()?.trim() ?: ""
            dbHelper.insertPhoto(travelId, photoEntries[i].toString(), comment)
        }
    }

    private fun addPhotoView(uri: Uri, existingComment: String) {
        val view = LayoutInflater.from(this).inflate(R.layout.item_photo_edit, photoContainer, false)
        val ivPhoto = view.findViewById<ImageView>(R.id.ivPhotoEdit)
        val progress = view.findViewById<ProgressBar>(R.id.progressPhotoEdit)
        val btnRemove = view.findViewById<Button>(R.id.btnRemovePhoto)
        val etComment = view.findViewById<TextInputEditText>(R.id.etPhotoComment)

        etComment.setText(existingComment)
        btnRemove.setOnClickListener {
            val idx = photoContainer.indexOfChild(view)
            if (idx >= 0 && idx < photoEntries.size) {
                photoEntries.removeAt(idx)
                photoContainer.removeView(view)
            }
        }

        progress.visibility = View.VISIBLE
        scope.launch {
            val bitmap = withContext(Dispatchers.IO) {
                try { contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it) } }
                catch (e: Exception) { null }
            }
            progress.visibility = View.GONE
            if (bitmap != null) { ivPhoto.setImageBitmap(bitmap); ivPhoto.visibility = View.VISIBLE }
        }
        photoContainer.addView(view)
    }

    private fun showDateRangePicker(field: TextInputEditText) {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        val builder = MaterialDatePicker.Builder.dateRangePicker().setTitleText("여행 기간 선택")
        val existing = field.text.toString()
        if (existing.contains(" ~ ")) {
            try {
                val parts = existing.split(" ~ ")
                if (parts.size == 2) {
                    val start = sdf.parse(parts[0].trim())?.time
                    val end = sdf.parse(parts[1].trim())?.time
                    if (start != null && end != null) builder.setSelection(AndroidPair(start, end))
                }
            } catch (e: Exception) { }
        }
        val picker = builder.build()
        picker.addOnPositiveButtonClickListener { selection ->
            field.setText("${sdf.format(Date(selection.first))} ~ ${sdf.format(Date(selection.second))}")
        }
        picker.show(supportFragmentManager, "travel_date_range")
    }

    private fun extractGpsFromPhoto(uri: Uri) {
        scope.launch {
            val latLng = withContext(Dispatchers.IO) {
                try {
                    contentResolver.openInputStream(uri)?.use { stream ->
                        val exif = ExifInterface(stream)
                        val latLong = FloatArray(2)
                        if (exif.getLatLong(latLong)) LatLng(latLong[0].toDouble(), latLong[1].toDouble())
                        else null
                    }
                } catch (e: Exception) { null }
            }
            if (latLng != null) {
                selectedLocation = "${latLng.latitude},${latLng.longitude}"
                tvSelectedLocation.text = "📍 사진에서 위치 감지됨"
                tvSelectedLocation.setTextColor(0xFF26A69A.toInt())
            }
        }
    }

    override fun onDestroy() { super.onDestroy(); scope.cancel() }
    override fun onSupportNavigateUp(): Boolean { finish(); return true }
}
