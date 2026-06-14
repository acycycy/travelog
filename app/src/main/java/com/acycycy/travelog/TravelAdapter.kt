package com.acycycy.travelog

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import android.net.Uri
import android.widget.ImageView
import android.widget.PopupMenu

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

        if (!travel.imageUri.isNullOrBlank()) {
            try {
                val uri = Uri.parse(travel.imageUri)

                val inputStream =
                    holder.itemView.context.contentResolver.openInputStream(uri)

                val bitmap =
                    android.graphics.BitmapFactory.decodeStream(inputStream)

                holder.imageTravel.visibility = View.VISIBLE
                holder.imageTravel.setImageBitmap(bitmap)

                inputStream?.close()
            } catch (e: Exception) {
                holder.imageTravel.visibility = View.GONE
            }
        } else {
            holder.imageTravel.visibility = View.GONE
        }

        holder.itemView.setOnClickListener {
            onClick(travel)
        }

        holder.itemView.setOnLongClickListener {

            val popupMenu = PopupMenu(holder.itemView.context, holder.itemView)

            popupMenu.menu.add("수정")
            popupMenu.menu.add("삭제")

            popupMenu.setOnMenuItemClickListener { menuItem ->

                when (menuItem.title) {
                    "수정" -> {
                        onClick(travel)
                        true
                    }

                    "삭제" -> {
                        onLongClick(travel)
                        true
                    }

                    else -> false
                }
            }

            popupMenu.show()
            true
        }
    }

    override fun getItemCount(): Int {
        return travelList.size
    }
}