package com.example.riskapp.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.riskapp.R
import com.example.riskapp.adapters.JobAdapter
import com.example.riskapp.models.Job
import com.example.riskapp.models.User
import com.example.riskapp.Utils
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private var me: User? = null
    private val jobs = mutableListOf<Job>()
    private lateinit var adapter: JobAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        adapter = JobAdapter(this, jobs)
        rvJobs.layoutManager = LinearLayoutManager(this)
        rvJobs.adapter = adapter

        btnCreateJob.setOnClickListener {
            startActivity(Intent(this, CreateJobActivity::class.java))
        }

        val uid = auth.currentUser?.uid
        if (uid == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish(); return
        }

        // load profile
        db.collection("users").document(uid).get()
            .addOnSuccessListener { doc ->
                me = doc.toObject(User::class.java)
                startListeningForJobs()
                // Setup listener for manager assignments (show notification when open jobs assigned)
                listenForAssignedOpenJobs(uid)
            }
            .addOnFailureListener { e -> Toast.makeText(this, "Failed to fetch profile: ${e.message}", Toast.LENGTH_LONG).show() }
    }

    private fun startListeningForJobs() {
        val currentUser = me ?: return
        val col = db.collection("jobs")
        // Choose query based on role:
        val query = when {
            currentUser.role == "admin" || currentUser.role == "safety" -> col.orderBy("createdAt")
            currentUser.isManager -> col.whereEqualTo("assignedTo.uid", currentUser.uid)
            else -> col.whereEqualTo("createdBy.uid", currentUser.uid)
        }
        query.addSnapshotListener { snap, err ->
            if (err != null) return@addSnapshotListener
            if (snap != null) {
                jobs.clear()
                for (doc in snap.documents) {
                    val job = doc.toObject(Job::class.java) ?: continue
                    job.id = doc.id
                    jobs.add(job)
                }
                adapter.notifyDataSetChanged()
            }
        }
    }

    private fun listenForAssignedOpenJobs(uid: String) {
        // Show a simple local notification when a new open job assigned to manager appears
        val q = db.collection("jobs")
            .whereEqualTo("assignedTo.uid", uid)
            .whereEqualTo("status", "open")
        q.addSnapshotListener { snap, err ->
            if (err != null) return@addSnapshotListener
            snap?.documentChanges?.forEach { change ->
                if (change.type.name == "ADDED") {
                    val title = change.document.getString("title") ?: "New job assigned"
                    val createdByName = (change.document.get("createdBy") as? Map<*, *>)?.get("name") ?: "Someone"
                    Utils.showNotification(this, "Job assigned: $title", "Created by $createdByName")
                }
            }
        }
    }
}
