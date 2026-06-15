package com.acycycy.travelog

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class PlanFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyPlanState: View
    private lateinit var dbHelper: TravelDatabaseHelper

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_plan, container, false)

        recyclerView = view.findViewById(R.id.recyclerViewPlan)
        emptyPlanState = view.findViewById(R.id.emptyPlanState)
        val btnAddPlan = view.findViewById<View>(R.id.btnAddPlan)

        dbHelper = TravelDatabaseHelper(requireContext())
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        btnAddPlan.setOnClickListener {
            startActivity(Intent(requireContext(), AddPlanActivity::class.java))
        }

        loadPlans(orderByIdDesc = false)
        return view
    }

    override fun onResume() {
        super.onResume()
        if (::dbHelper.isInitialized) loadPlans(orderByIdDesc = false)
    }

    fun loadPlansLatest() = loadPlans(orderByIdDesc = true)
    fun loadPlansOldest() = loadPlans(orderByIdDesc = false)

    private fun loadPlans(orderByIdDesc: Boolean = false) {
        val plans = dbHelper.getAllPlans(orderByIdDesc)
        if (plans.isEmpty()) {
            recyclerView.visibility = View.GONE
            emptyPlanState.visibility = View.VISIBLE
        } else {
            recyclerView.visibility = View.VISIBLE
            emptyPlanState.visibility = View.GONE
        }
        recyclerView.adapter = PlanAdapter(
            plans,
            { plan -> openDetailScreen(plan) },
            { plan -> openEditPlan(plan) },
            { plan -> showDeleteDialog(plan) }
        )
    }

    private fun openDetailScreen(plan: Plan) {
        val intent = Intent(requireContext(), PlanDetailActivity::class.java).apply {
            putExtra("id", plan.id)
            putExtra("destination", plan.destination)
            putExtra("plannedDate", plan.plannedDate)
            putExtra("memo", plan.memo)
            putExtra("location", plan.location)
        }
        startActivity(intent)
    }

    private fun openEditPlan(plan: Plan) {
        val intent = Intent(requireContext(), AddPlanActivity::class.java).apply {
            putExtra("mode", "edit")
            putExtra("id", plan.id)
            putExtra("destination", plan.destination)
            putExtra("plannedDate", plan.plannedDate)
            putExtra("memo", plan.memo)
            putExtra("location", plan.location)
        }
        startActivity(intent)
    }

    private fun showDeleteDialog(plan: Plan) {
        AlertDialog.Builder(requireContext())
            .setTitle("삭제")
            .setMessage("${plan.destination} 계획을 삭제하시겠습니까?")
            .setPositiveButton("예") { _, _ ->
                dbHelper.deletePlan(plan.id)
                loadPlans()
            }
            .setNegativeButton("아니오", null)
            .show()
    }
}
