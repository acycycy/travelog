package com.acycycy.travelog

import android.graphics.BitmapFactory
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.ProgressBar
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TravelAdapter(
    private val travelList: List<Travel>,
    private val onClick: (Travel) -> Unit,
    private val onEditClick: (Travel) -> Unit,
    private val onLongClick: (Travel) -> Unit
) : RecyclerView.Adapter<TravelAdapter.TravelViewHolder>() {

    class TravelViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        val tvDate: TextView = itemView.findViewById(R.id.tvDate)
        val tvMemo: TextView = itemView.findViewById(R.id.tvMemo)
        val imageTravel: ImageView = itemView.findViewById(R.id.imageTravel)
        val progressImage: ProgressBar = itemView.findViewById(R.id.progressImage)
        val ratingBar: RatingBar = itemView.findViewById(R.id.ratingBar)
        var imageJob: Job? = null
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TravelViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_travel, parent, false)
        return TravelViewHolder(view)
    }

    override fun onBindViewHolder(holder: TravelViewHolder, position: Int) {
        val travel = travelList[position]

        holder.tvTitle.text = travel.title
        holder.tvDate.text = travel.date
        holder.tvMemo.text = travel.memo
        holder.ratingBar.rating = travel.rating.toFloat()

        holder.imageJob?.cancel()
        holder.imageTravel.visibility = View.GONE
        holder.progressImage.visibility = View.GONE

        if (!travel.imageUri.isNullOrBlank()) {
            holder.progressImage.visibility = View.VISIBLE
            holder.imageJob = CoroutineScope(Dispatchers.Main).launch {
                val bitmap = withContext(Dispatchers.IO) {
                    try {
                        val uri = Uri.parse(travel.imageUri)
                        holder.itemView.context.contentResolver.openInputStream(uri)?.use {
                            BitmapFactory.decodeStream(it)
                        }
                    } catch (e: Exception) { null }
                }
                holder.progressImage.visibility = View.GONE
                if (bitmap != null) {
                    holder.imageTravel.setImageBitmap(bitmap)
                    holder.imageTravel.visibility = View.VISIBLE
                }
            }
        }

        holder.itemView.setOnClickListener { onClick(travel) }

        holder.itemView.setOnLongClickListener {
            val popup = PopupMenu(holder.itemView.context, holder.itemView)
            popup.menu.add("수정")
            popup.menu.add("삭제")
            popup.setOnMenuItemClickListener { menuItem ->
                when (menuItem.title) {
                    "수정" -> { onEditClick(travel); true }
                    "삭제" -> { onLongClick(travel); true }
                    else -> false
                }
            }
            popup.show()
            true
        }
    }

    override fun onViewRecycled(holder: TravelViewHolder) {
        super.onViewRecycled(holder)
        holder.imageJob?.cancel()
    }

    override fun getItemCount() = travelList.size
}
