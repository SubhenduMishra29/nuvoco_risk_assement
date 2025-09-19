package com.example.riskmanagement

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class DashboardActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var emptyText: TextView

    private val jobList = mutableListOf<JobModel>()
    private lateinit var adapter: JobAdapter

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        recyclerView = findViewById(R.id.recyclerViewJobs)
        progressBar = findViewById(R.id.progressBar)
        emptyText = findViewById(R.id.emptyText)

        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = JobAdapter(jobList)
        recyclerView.adapter = adapter

        fetchJobs()
    }

    private fun fetchJobs() {
        val userId = auth.currentUser?.uid ?: return

        progressBar.visibility = View.VISIBLE

        // Get user role
        db.collection("users").document(userId).get().addOnSuccessListener { doc ->
            val role = doc.getString("role") ?: "worker"
            val isManager = doc.getBoolean("isManager") ?: false

            var query: Query = db.collection("jobs")

            when {
                role == "admin" -> {
                    query = query.orderBy("createdAt", Query.Direction.DESCENDING)
                }
                isManager -> {
                    query = query.whereEqualTo("assignedTo", userId)
                }
                else -> {
                    query = query.whereEqualTo("createdBy", userId)
                }
            }

            query.addSnapshotListener { snapshots, e ->
                progressBar.visibility = View.GONE
                if (e != null || snapshots == null) {
                    emptyText.visibility = View.VISIBLE
                    emptyText.text = "Error loading jobs"
                    return@addSnapshotListener
                }

                jobList.clear()
                for (docSnap in snapshots.documents) {
                    val job = docSnap.toObject(JobModel::class.java)
                    if (job != null) jobList.add(job)
                }

                if (jobList.isEmpty()) {
                    emptyText.visibility = View.VISIBLE
                    emptyText.text = "No jobs found"
                } else {
                    emptyText.visibility = View.GONE
                }

                adapter.notifyDataSetChanged()
            }

        }
    }
}
