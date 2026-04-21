package com.example.arenafitness

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

class HistoryAdapter(private val historyList: List<CheckInRecord>) :
    RecyclerView.Adapter<HistoryAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvDateFull: TextView = view.findViewById(R.id.tvHistoryDateFull)
        val tvType: TextView = view.findViewById(R.id.tvHistoryType)
        val tvStartTime: TextView = view.findViewById(R.id.tvHistoryStartTime)
        val tvEndTime: TextView = view.findViewById(R.id.tvHistoryEndTime)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_history, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val record = historyList[position]
        
        // Format tanggal: Senin, 21 Apr 2025
        holder.tvDateFull.text = formatDate(record.date)
        
        // Dummy data untuk tipe latihan sesuai gambar
        val types = arrayOf("Yoga Morning", "Cardio Mandiri", "HIIT Sore", "Strength Training", "Zumba")
        val randomType = types[record.id % types.size]
        holder.tvType.text = "$randomType - 1j 30m"
        
        holder.tvStartTime.text = record.time
        
        // Dummy end time
        holder.tvEndTime.text = "s/d --:--" 
    }

    private fun formatDate(dateStr: String): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val outputFormat = SimpleDateFormat("EEEE, d MMM yyyy", Locale("id", "ID"))
            val date = inputFormat.parse(dateStr)
            outputFormat.format(date ?: Date())
        } catch (e: Exception) {
            dateStr
        }
    }

    override fun getItemCount(): Int = historyList.size
}