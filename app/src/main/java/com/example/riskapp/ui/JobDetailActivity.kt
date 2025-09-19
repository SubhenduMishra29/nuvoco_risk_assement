package com.example.riskapp.ui

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.riskapp.R
import com.example.riskapp.Utils
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_job_detail.*
import java.text.SimpleDateFormat
import java.util.*

class JobDetailActivity : AppCompatActivity() {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private var jobId: String? = null
    private var myUid: String? = null
    private var jobDoc: com.google.firebase.firestore.DocumentSnapshot? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_job_detail)
        myUid = auth.currentUser?.uid
        jobId = intent.getStringExtra("jobId")
        jobId ?: run { finish(); return }
        loadJob()

        btnApprove.setOnClickListener { updateStatus("approved") }
        btnReject.setOnClickListener { updateStatus("rejected") }
        btnStart.setOnClickListener { startJob() }
        btnClose.setOnClickListener { closeJob() }
    }

    private fun loadJob() {
        db.collection("jobs").document(jobId!!).get()
            .addOnSuccessListener { doc ->
                jobDoc = doc
                val title = doc.getString("title") ?: ""
                val desc = doc.getString("description") ?: ""
                val risks = doc.get("risks") as? List<*> ?: emptyList<Any>()
                val extra = doc.getString("extraRisks") ?: ""
                val assigned = doc.get("assignedTo") as? Map<*, *>
                val status = doc.getString("status") ?: "open"
                val createdAt = (doc.getTimestamp("createdAt"))?.toDate()
                val validity = (doc.getTimestamp("validity"))?.toDate()

                tvTitle.text = title
                tvDesc.text = desc
                tvRisks.text = "Risks: ${risks.joinToString(", ")} ${if (extra.isNotEmpty()) " • $extra" else ""}"
                tvAssigned.text = "Assigned to: ${assigned?.get("name") ?: "N/A"}"
                tvStatus.text = "Status: $status"
                val df = SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault())
                tvCreatedAt.text = "Created: ${createdAt?.let{df.format(it)} ?: "N/A"}"
                tvValidity.text = "Valid until: ${validity?.let{df.format(it)} ?: "N/A"}"

                // Show/hide buttons based on role and status
                val assignedUid = assigned?.get("uid") as? String
                if (myUid != null && myUid == assignedUid && status == "open") {
                    btnApprove.visibility = android.view.View.VISIBLE
                    btnReject.visibility = android.view.View.VISIBLE
                } else {
                    btnApprove.visibility = android.view.View.GONE
                    btnReject.visibility = android.view.View.GONE
                }

                // If job is approved, allow worker to start/close if they are creator
                val created = doc.get("createdBy") as? Map<*, *>
                val creatorUid = created?.get("uid") as? String
                if (status == "approved" && myUid == creatorUid) {
                    btnStart.visibility = android.view.View.VISIBLE
                    btnClose.visibility = android.view.View.GONE
                } else {
                    btnStart.visibility = android.view.View.GONE
                }

                // If job started and you are creator, show Close
                val started = doc.getTimestamp("startedAt")
                if (started != null && myUid == creatorUid) {
                    btnClose.visibility = android.view.View.VISIBLE
                    btnStart.visibility = android.view.View.GONE
                }
            }
    }

    private fun updateStatus(newStatus: String) {
        db.collection("jobs").document(jobId!!).update("status", newStatus)
            .addOnSuccessListener {
                Toast.makeText(this, "Status updated: $newStatus", Toast.LENGTH_SHORT).show()
                loadJob()
            }.addOnFailureListener { e -> Toast.makeText(this,"Error: ${e.message}",Toast.LENGTH_LONG).show() }
    }

    private fun startJob() {
        db.collection("jobs").document(jobId!!).update("startedAt", Timestamp.now(), "status", "in_progress")
            .addOnSuccessListener {
                Toast.makeText(this,"Job started", Toast.LENGTH_SHORT).show()
                loadJob()
            }.addOnFailureListener { e -> Toast.makeText(this,"Error: ${e.message}",Toast.LENGTH_LONG).show()}
    }

    private fun closeJob() {
        db.collection("jobs").document(jobId!!).update("closedAt", Timestamp.now(), "status", "closed")
            .addOnSuccessListener {
                Toast.makeText(this,"Job closed", Toast.LENGTH_SHORT).show()
                loadJob()
            }.addOnFailureListener { e -> Toast.makeText(this,"Error: ${e.message}",Toast.LENGTH_LONG).show()}
    }
}
