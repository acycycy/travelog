package com.acycycy.travelog

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class HomeFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var etSearch: EditText
    private lateinit var dbHelper: TravelDatabaseHelper

    private var currentList: List<Travel> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        recyclerView = view.findViewById(R.id.recyclerViewTravel)
        etSearch = view.findViewById(R.id.etSearch)
        val btnAddTravel = view.findViewById<Button>(R.id.btnAddTravel)

        dbHelper = TravelDatabaseHelper(requireContext())

        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        btnAddTravel.setOnClickListener {
            val intent = Intent(requireContext(), AddTravelActivity::class.java)
            startActivity(intent)
        }

        setupSearch()
        loadTravelsLatest()

        return view
    }

    override fun onResume() {
        super.onResume()
        if (::dbHelper.isInitialized) {
            loadTravelsLatest()
        }
    }

    private fun setAdapter(list: List<Travel>) {
        recyclerView.adapter = TravelAdapter(
            list,
            { travel -> openEditScreen(travel) },
            { travel -> showDeleteDialog(travel) }
        )
    }

    private fun openEditScreen(travel: Travel) {
        val intent = Intent(requireContext(), AddTravelActivity::class.java)

        intent.putExtra("mode", "edit")
        intent.putExtra("id", travel.id)
        intent.putExtra("title", travel.title)
        intent.putExtra("date", travel.date)
        intent.putExtra("memo", travel.memo)
        intent.putExtra("imageUri", travel.imageUri)

        startActivity(intent)
    }

    private fun showDeleteDialog(travel: Travel) {
        AlertDialog.Builder(requireContext())
            .setTitle("삭제")
            .setMessage("${travel.title} 을(를) 삭제하시겠습니까?")
            .setPositiveButton("예") { _, _ ->
                dbHelper.deleteTravel(travel.id)
                loadTravelsLatest()
            }
            .setNegativeButton("아니오", null)
            .show()
    }

    private fun setupSearch() {
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {}

            override fun onTextChanged(
                s: CharSequence?,
                start: Int,
                before: Int,
                count: Int
            ) {
                val keyword = s.toString().lowercase()

                val filteredList = currentList.filter {
                    it.title.lowercase().contains(keyword) ||
                            it.memo.lowercase().contains(keyword)
                }

                setAdapter(filteredList)
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    fun loadTravelsLatest() {
        currentList = dbHelper.getAllTravels()
        setAdapter(currentList)
    }

    fun loadTravelsOldest() {
        currentList = dbHelper.getAllTravels().reversed()
        setAdapter(currentList)
    }
}