package com.acycycy.travelog

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import android.net.Uri
import android.widget.ImageView

class TravelAdapter(
    private val travelList: List<Travel>,
    private val onClick: (Travel) -> Unit,
    private val onLongClick: (Travel) -> Unit
) : RecyclerView.Adapter<TravelAdapter.TravelViewHolder>() {
    class TravelViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        val tvDate: TextView = itemView.findViewById(R.id.tvDate)
        val tvMemo: TextView = itemView.findViewById(R.id.tvMemo)

        val imageTravel = itemView.findViewById<ImageView>(R.id.imageTravel)


    }


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): TravelViewHolder {

        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_travel, parent, false)

        return TravelViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: TravelViewHolder,
        position: Int

    ) {

        val travel = travelList[position]

        holder.tvTitle.text = travel.title
        holder.tvDate.text = travel.date
        holder.tvMemo.text = travel.memo

        if (travel.imageUri != null) {
            holder.imageTravel.setImageURI(
                Uri.parse(travel.imageUri)
            )
        }

        holder.itemView.setOnClickListener {
            onClick(travel)
        }

        holder.itemView.setOnLongClickListener {
            onLongClick(travel)
            true
        }
    }

    override fun getItemCount(): Int {
        return travelList.size
    }
}