package com.acycycy.travelog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.content.Intent
import android.widget.Button

class HomeFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerViewTravel)

        val btnAddTravel = view.findViewById<Button>(R.id.btnAddTravel)

        btnAddTravel.setOnClickListener {
            val intent = Intent(requireContext(), AddTravelActivity::class.java)
            startActivity(intent)
        }

        val dbHelper = TravelDatabaseHelper(requireContext())
        val travelList = dbHelper.getAllTravels()

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = TravelAdapter(travelList)

        return view
    }
}