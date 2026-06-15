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
        val view = inflater.inflate(R.layout.fragment_stats, container, false)

        val tvTravelCount = view.findViewById<TextView>(R.id.tvTravelCount)
        val tvThisMonthCount = view.findViewById<TextView>(R.id.tvThisMonthCount)
        val tvRecentTravel = view.findViewById<TextView>(R.id.tvRecentTravel)

        val dbHelper = TravelDatabaseHelper(requireContext())

        tvTravelCount.text = dbHelper.getTravelCount().toString()
        tvThisMonthCount.text = dbHelper.getThisMonthCount().toString()

        val travelList = dbHelper.getAllTravels()
        tvRecentTravel.text = if (travelList.isNotEmpty()) travelList[0].title else "없음"

        return view
    }
}
