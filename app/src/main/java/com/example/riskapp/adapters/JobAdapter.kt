package com.example.riskapp.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.riskapp.R
import com.example.riskapp.models.Job
import com.example.riskapp.ui.JobDetailActivity
import kotlinx.android.synthetic.main.item_job.view.*
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.*

class JobAdapter(private val context: Context, private val list: List<Job>) : RecyclerView.Adapter<JobAdapter.VH>() {

    inner class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(job: Job) {
            itemView.tvTitle.text = job.title
            val assignedName = job.assignedTo["name"] ?: "Unassigned"
            val createdName = job.createdBy["name"] ?: "Unknown"
            val createdAt = job.createdAt?.toDate()?.let { SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault()).format(it) } ?: ""
            itemView.tvMeta.text = "Created by $createdName • Assigned to $assignedName • $createdAt"
            itemView.tvStatus.text = job.status.uppercase(Locale.getDefault())
            // status coloring
            when(job.status) {
                "open" -> itemView.tvStatus.setTextColor(context.resources.getColor(android.R.color.holo_red_dark))
                "approved" -> itemView.tvStatus.setTextColor(context.resources.getColor(android.R.color.holo_orange_dark))
                "closed" -> itemView.tvStatus.setTextColor(context.resources.getColor(android.R.color.holo_green_dark))
                else -> itemView.tvStatus.setTextColor(context.resources.getColor(android.R.color.black))
            }

            itemView.setOnClickListener {
                val intent = Intent(context, JobDetailActivity::class.java)
                intent.putExtra("jobId", job.id)
                context.startActivity(intent)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_job, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(list[position])
    }

    override fun getItemCount(): Int = list.size
}
