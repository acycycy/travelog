package com.acycycy.travelog

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class PlanAdapter(
    private val planList: List<Plan>,
    private val onClick: (Plan) -> Unit,
    private val onEditClick: (Plan) -> Unit,
    private val onDeleteClick: (Plan) -> Unit
) : RecyclerView.Adapter<PlanAdapter.PlanViewHolder>() {

    inner class PlanViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvDestination: TextView = itemView.findViewById(R.id.tvPlanDestination)
        val tvDate: TextView = itemView.findViewById(R.id.tvPlanDate)
        val tvMemo: TextView = itemView.findViewById(R.id.tvPlanMemo)
        val tvDDay: TextView = itemView.findViewById(R.id.tvDDay)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlanViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_plan, parent, false)
        return PlanViewHolder(view)
    }

    override fun onBindViewHolder(holder: PlanViewHolder, position: Int) {
        val plan = planList[position]
        holder.tvDestination.text = plan.destination
        holder.tvDate.text = plan.plannedDate
        holder.tvMemo.text = plan.memo

        val dDay = calculateDDay(plan.plannedDate)
        if (dDay.isNotEmpty()) {
            holder.tvDDay.text = dDay
            holder.tvDDay.visibility = View.VISIBLE
            holder.tvDDay.setTextColor(
                when {
                    dDay == "D-DAY" -> 0xFFEF5350.toInt()   // 빨간색
                    dDay.startsWith("D-") -> 0xFF26A69A.toInt() // 민트 (미래)
                    else -> 0xFF9E9E9E.toInt()               // 회색 (지난 여행)
                }
            )
        } else {
            holder.tvDDay.visibility = View.GONE
        }

        holder.itemView.setOnClickListener { onClick(plan) }

        holder.itemView.setOnLongClickListener { view ->
            val popup = PopupMenu(view.context, view)
            popup.menu.add(0, 0, 0, "수정")
            popup.menu.add(0, 1, 1, "삭제")
            popup.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    0 -> onEditClick(plan)
                    1 -> onDeleteClick(plan)
                }
                true
            }
            popup.show()
            true
        }
    }

    private fun calculateDDay(dateStr: String): String {
        return try {
            val startDateStr = if (dateStr.contains(" ~ ")) {
                dateStr.split(" ~ ")[0].trim()
            } else {
                dateStr.trim()
            }
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val startDate = sdf.parse(startDateStr) ?: return ""

            val today = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.time

            val diff = ((startDate.time - today.time) / (1000L * 60 * 60 * 24)).toInt()
            when {
                diff > 0 -> "D-$diff"
                diff == 0 -> "D-DAY"
                else -> "D+${-diff}"
            }
        } catch (e: Exception) { "" }
    }

    override fun getItemCount() = planList.size
}
