package com.acycycy.travelog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment

class StatsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val view = inflater.inflate(
            R.layout.fragment_stats,
            container,
            false
        )

        val tvTravelCount =
            view.findViewById<TextView>(R.id.tvTravelCount)

        val tvRecentTravel =
            view.findViewById<TextView>(R.id.tvRecentTravel)

        val dbHelper = TravelDatabaseHelper(requireContext())

        val count = dbHelper.getTravelCount()
        val travelList = dbHelper.getAllTravels()

        tvTravelCount.text = "총 여행 수 : $count"

        if (travelList.isNotEmpty()) {
            tvRecentTravel.text =
                "최근 여행 : ${travelList[0].title}"
        } else {
            tvRecentTravel.text =
                "최근 여행 : 없음"
        }

        return view
    }
}