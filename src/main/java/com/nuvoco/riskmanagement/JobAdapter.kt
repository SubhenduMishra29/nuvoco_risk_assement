package com.nuvoco.riskmanagement

import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.nuvoco.riskmanagement.models.Job

class JobAdapter(private val jobs: List<Job>) :
    RecyclerView.Adapter<JobAdapter.JobViewHolder>() {

    class JobViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val container: LinearLayout = view.findViewById(R.id.jobContainer)
        val title: TextView = view.findViewById(R.id.textTitle)
        val status: TextView = view.findViewById(R.id.textStatus)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): JobViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_job, parent, false)
        return JobViewHolder(view)
    }

    override fun onBindViewHolder(holder: JobViewHolder, position: Int) {
        val job = jobs[position]
        holder.title.text = job.title
        holder.status.text = job.status

        // Status color
        when (job.status.lowercase()) {
            "open" -> holder.container.setBackgroundColor(Color.parseColor("#FFCDD2"))   // Red-ish
            "approved" -> holder.container.setBackgroundColor(Color.parseColor("#FFF9C4")) // Yellow-ish
            "closed" -> holder.container.setBackgroundColor(Color.parseColor("#C8E6C9"))  // Green-ish
        }

        holder.itemView.setOnClickListener {
            val intent = Intent(holder.itemView.context, JobDetailsActivity::class.java)
            intent.putExtra("jobId", job.id)
            holder.itemView.context.startActivity(intent)
        }
    }

    override fun getItemCount() = jobs.size
}
