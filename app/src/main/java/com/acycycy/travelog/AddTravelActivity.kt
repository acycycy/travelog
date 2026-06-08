package com.acycycy.travelog

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class AddTravelActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_travel)

        val etTitle = findViewById<EditText>(R.id.etTitle)
        val etDate = findViewById<EditText>(R.id.etDate)
        val etMemo = findViewById<EditText>(R.id.etMemo)
        val btnSave = findViewById<Button>(R.id.btnSave)

        val dbHelper = TravelDatabaseHelper(this)

        val mode = intent.getStringExtra("mode")
        val travelId = intent.getIntExtra("id", -1)

        if (mode == "edit") {
            etTitle.setText(intent.getStringExtra("title"))
            etDate.setText(intent.getStringExtra("date"))
            etMemo.setText(intent.getStringExtra("memo"))

            btnSave.text = "수정"
        }

        btnSave.setOnClickListener {
            val title = etTitle.text.toString()
            val date = etDate.text.toString()
            val memo = etMemo.text.toString()

            if (title.isBlank() || date.isBlank() || memo.isBlank()) {
                Toast.makeText(this, "모든 항목을 입력해주세요", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (mode == "edit" && travelId != -1) {
                val result = dbHelper.updateTravel(travelId, title, date, memo)

                if (result > 0) {
                    Toast.makeText(this, "여행 기록이 수정되었습니다", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this, "수정에 실패했습니다", Toast.LENGTH_SHORT).show()
                }
            } else {
                val result = dbHelper.insertTravel(title, date, memo)

                if (result != -1L) {
                    Toast.makeText(this, "여행 기록이 저장되었습니다", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this, "저장에 실패했습니다", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}