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

        recyclerView.adapter = TravelAdapter(
            travelList,

            { travel ->
                val intent = Intent(requireContext(), AddTravelActivity::class.java)

                intent.putExtra("mode", "edit")
                intent.putExtra("id", travel.id)
                intent.putExtra("title", travel.title)
                intent.putExtra("date", travel.date)
                intent.putExtra("memo", travel.memo)
                intent.putExtra("imageUri", travel.imageUri)

                startActivity(intent)
            },

            { travel ->
                android.app.AlertDialog.Builder(requireContext())
                    .setTitle("삭제")
                    .setMessage("${travel.title} 을(를) 삭제하시겠습니까?")
                    .setPositiveButton("예") { _, _ ->

                        dbHelper.deleteTravel(travel.id)

                        parentFragmentManager.beginTransaction()
                            .replace(R.id.fragment_container, HomeFragment())
                            .commit()
                    }
                    .setNegativeButton("아니오", null)
                    .show()
            }
        )

        return view
    }

    override fun onResume() {
        super.onResume()

        val recyclerView = view?.findViewById<RecyclerView>(R.id.recyclerViewTravel)
        val dbHelper = TravelDatabaseHelper(requireContext())
        val travelList = dbHelper.getAllTravels()

        recyclerView?.adapter = TravelAdapter(
            travelList,

            { travel ->
                val intent = Intent(requireContext(), AddTravelActivity::class.java)

                intent.putExtra("mode", "edit")
                intent.putExtra("id", travel.id)
                intent.putExtra("title", travel.title)
                intent.putExtra("date", travel.date)
                intent.putExtra("memo", travel.memo)
                intent.putExtra("imageUri", travel.imageUri)

                startActivity(intent)
            },

            { travel ->
                android.app.AlertDialog.Builder(requireContext())
                    .setTitle("삭제")
                    .setMessage("${travel.title} 을(를) 삭제하시겠습니까?")
                    .setPositiveButton("예") { _, _ ->

                        dbHelper.deleteTravel(travel.id)

                        parentFragmentManager.beginTransaction()
                            .replace(R.id.fragment_container, HomeFragment())
                            .commit()
                    }
                    .setNegativeButton("아니오", null)
                    .show()
            }
        )
    }
}