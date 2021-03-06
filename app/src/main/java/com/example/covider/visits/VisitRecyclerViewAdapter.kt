package com.example.covider.visits

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView

import com.example.covider.databinding.FragmentVisitBinding
import com.example.covider.models.Visit
import com.google.firebase.Timestamp
import java.util.*

class VisitRecyclerViewAdapter(
        private val values: List<Visit>)
    : RecyclerView.Adapter<VisitRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

    return ViewHolder(FragmentVisitBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = values[position]
        holder.idView.text = getTime(item.startTime!!) + "  to  " + getTime(item.endTime!!)
        holder.contentView.text = item.buildingID
    }

    override fun getItemCount(): Int = values.size

    inner class ViewHolder(binding: FragmentVisitBinding) : RecyclerView.ViewHolder(binding.root) {
        val idView: TextView = binding.timeframe
        val contentView: TextView = binding.buildingName

        override fun toString(): String {
            return super.toString() + " '" + contentView.text + "'"
        }
    }

    private fun getTime(ts: Timestamp): String {
        val date = ts.toDate()
        val hours = date.hours.toString()
        val minutes = date.minutes.toString()
        if (minutes.length < 2) {
            return hours + ":0" + minutes
        } else {
            return hours + ":" + minutes
        }
    }

}