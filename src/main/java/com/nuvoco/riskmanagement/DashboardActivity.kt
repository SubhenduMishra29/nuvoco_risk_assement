package com.nuvoco.riskmanagement

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.nuvoco.riskmanagement.models.Job

class DashboardActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: JobAdapter
    private val jobList = mutableListOf<Job>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        recyclerView = findViewById(R.id.recyclerJobs)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = JobAdapter(jobList)
        recyclerView.adapter = adapter

        val createJobBtn = findViewById<Button>(R.id.btnCreateJob)
        createJobBtn.setOnClickListener {
            startActivity(Intent(this, CreateJobActivity::class.java))
        }

        loadJobs()
    }

    private fun loadJobs() {
        val currentUser = auth.currentUser ?: return
        val userId = currentUser.uid

        // Fetch current user's profile to decide what jobs to show
        db.collection("users").document(userId).get()
            .addOnSuccessListener { doc ->
                if (doc != null && doc.exists()) {
                    val isManager = doc.getBoolean("isManager") ?: false
                    val role = doc.getString("role") ?: "Worker"

                    var query = db.collection("jobs")

                    if (!isManager && role != "Admin") {
                        // Workers see only their own jobs
                        query = query.whereEqualTo("createdBy", userId)
                    } else if (isManager && role != "Admin") {
                        // Managers see jobs assigned to them
                        query = query.whereEqualTo("assignedTo", userId)
                    }
                    // Admin sees all jobs

                    query.addSnapshotListener { snapshot, e ->
                        if (e != null) {
                            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                            return@addSnapshotListener
                        }
                        if (snapshot != null) {
                            jobList.clear()
                            for (docSnap in snapshot) {
                                jobList.add(docToJob(docSnap))
                            }
                            adapter.notifyDataSetChanged()
                        }
                    }
                }
            }
    }

    private fun docToJob(doc: QueryDocumentSnapshot): Job {
        return Job(
            id = doc.id,
            title = doc.getString("title") ?: "",
            description = doc.getString("description") ?: "",
            status = doc.getString("status") ?: "open",
            createdBy = doc.getString("createdBy") ?: "",
            assignedTo = doc.getString("assignedTo") ?: "",
            validity = doc.getString("validity") ?: ""
        )
    }
}

