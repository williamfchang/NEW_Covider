package com.example.covider.visits

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView

import com.example.covider.databinding.FragmentVisitBinding

class VisitRecyclerViewAdapter(
        private val values: List<Visit>)
    : RecyclerView.Adapter<VisitRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

    return ViewHolder(FragmentVisitBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = values[position]
        holder.idView.text = item.getStartTime().toString() + " to " +
                item.getEndTime().toString()
        holder.contentView.text = item.getBuilding()
    }

    override fun getItemCount(): Int = values.size

    inner class ViewHolder(binding: FragmentVisitBinding) : RecyclerView.ViewHolder(binding.root) {
        val idView: TextView = binding.timeframe
        val contentView: TextView = binding.buildingName

        override fun toString(): String {
            return super.toString() + " '" + contentView.text + "'"
        }
    }

}