package com.acycycy.travelog

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.util.Pair as AndroidPair
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.textfield.TextInputEditText
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class AddPlanActivity : AppCompatActivity() {

    private lateinit var tvSelectedLocation: TextView
    private var selectedLocation: String? = null

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
        setContentView(R.layout.activity_add_plan)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "여행 계획"

        val etDestination = findViewById<TextInputEditText>(R.id.etPlanDestination)
        val etDate = findViewById<TextInputEditText>(R.id.etPlanDate)
        val etMemo = findViewById<TextInputEditText>(R.id.etPlanMemo)
        val btnSave = findViewById<Button>(R.id.btnSavePlan)
        val btnPickLocation = findViewById<Button>(R.id.btnPickPlanLocation)
        tvSelectedLocation = findViewById(R.id.tvPlanSelectedLocation)

        val dbHelper = TravelDatabaseHelper(this)
        val mode = intent.getStringExtra("mode")
        val planId = intent.getIntExtra("id", -1)

        etDate.setOnClickListener { showDateRangePicker(etDate) }

        btnPickLocation.setOnClickListener {
            val pickIntent = Intent(this, MapPickerActivity::class.java)
            if (!selectedLocation.isNullOrBlank()) pickIntent.putExtra("locations", selectedLocation)
            mapPickerLauncher.launch(pickIntent)
        }

        if (mode == "edit") {
            etDestination.setText(intent.getStringExtra("destination"))
            etDate.setText(intent.getStringExtra("plannedDate"))
            etMemo.setText(intent.getStringExtra("memo"))
            btnSave.text = "수정"

            val existingLocation = intent.getStringExtra("location")
            if (!existingLocation.isNullOrBlank()) {
                selectedLocation = existingLocation
                val count = existingLocation.split("|").size
                tvSelectedLocation.text = "${count}곳 선택됨"
                tvSelectedLocation.setTextColor(0xFF26A69A.toInt())
            }
        }

        btnSave.setOnClickListener {
            val destination = etDestination.text.toString().trim()
            val date = etDate.text.toString().trim()
            val memo = etMemo.text.toString().trim()

            if (destination.isBlank() || date.isBlank()) {
                Toast.makeText(this, "여행지와 날짜를 입력해주세요", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (mode == "edit" && planId != -1) {
                val result = dbHelper.updatePlan(planId, destination, date, memo, selectedLocation)
                if (result > 0) {
                    Toast.makeText(this, "계획이 수정되었습니다", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this, "수정에 실패했습니다", Toast.LENGTH_SHORT).show()
                }
            } else {
                val result = dbHelper.insertPlan(destination, date, memo, selectedLocation)
                if (result != -1L) {
                    Toast.makeText(this, "계획이 저장되었습니다", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this, "저장에 실패했습니다", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun showDateRangePicker(field: TextInputEditText) {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }

        val builder = MaterialDatePicker.Builder.dateRangePicker()
            .setTitleText("여행 기간 선택")

        val existing = field.text.toString()
        if (existing.contains(" ~ ")) {
            try {
                val parts = existing.split(" ~ ")
                if (parts.size == 2) {
                    val start = sdf.parse(parts[0].trim())?.time
                    val end = sdf.parse(parts[1].trim())?.time
                    if (start != null && end != null) {
                        builder.setSelection(AndroidPair(start, end))
                    }
                }
            } catch (e: Exception) { }
        }

        val picker = builder.build()
        picker.addOnPositiveButtonClickListener { selection ->
            val startStr = sdf.format(Date(selection.first))
            val endStr = sdf.format(Date(selection.second))
            field.setText("$startStr ~ $endStr")
        }
        picker.show(supportFragmentManager, "plan_date_range")
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
