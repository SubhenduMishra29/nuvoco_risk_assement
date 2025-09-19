package com.example.riskapp.ui

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.CheckBox
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.riskapp.R
import com.example.riskapp.models.Job
import com.example.riskapp.models.User
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_create_job.*
import java.util.*

class CreateJobActivity : AppCompatActivity() {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private var selectedValidity: Calendar? = null
    private val managers = mutableListOf<User>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_job)

        // Predefined risks:
        val predefined = listOf("Working at height", "Electrical", "Confined space", "Hot work", "Chemical", "Mechanical", "Slip/Trip")
        for (r in predefined) {
            val cb = CheckBox(this)
            cb.text = r
            llRisks.addView(cb)
        }

        // Load managers from Firestore
        db.collection("users").whereEqualTo("isManager", true).get()
            .addOnSuccessListener { snap ->
                managers.clear()
                for (doc in snap.documents) {
                    val u = doc.toObject(User::class.java)
                    if (u != null) managers.add(u)
                }
                val names = managers.map { it.name }
                spinnerManagers.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, names)
            }

        btnPickDate.setOnClickListener {
            pickDateTime()
        }

        btnSave.setOnClickListener {
            val title = etTitle.text.toString().trim()
            val desc = etDescription.text.toString().trim()
            val extra = etExtraRisk.text.toString().trim()
            val selectedRisks = mutableListOf<String>()
            for (i in 0 until llRisks.childCount) {
                val v = llRisks.getChildAt(i)
                if (v is CheckBox && v.isChecked) selectedRisks.add(v.text.toString())
            }
            if (title.isEmpty()) { Toast.makeText(this,"Title required",Toast.LENGTH_SHORT).show(); return@setOnClickListener }
            val uid = auth.currentUser?.uid ?: return@setOnClickListener
            // createdBy
            db.collection("users").document(uid).get().addOnSuccessListener { doc ->
                val createdBy = hashMapOf(
                    "uid" to uid,
                    "name" to (doc.getString("name") ?: ""),
                    "email" to (doc.getString("email") ?: "")
                )
                val managerIndex = spinnerManagers.selectedItemPosition
                if (managerIndex < 0 || managerIndex >= managers.size) {
                    Toast.makeText(this,"Select manager",Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }
                val assigned = managers[managerIndex]
                val assignedMap = hashMapOf("uid" to assigned.uid, "name" to assigned.name, "email" to assigned.email)
                val payload = hashMapOf(
                    "title" to title,
                    "description" to desc,
                    "risks" to selectedRisks,
                    "extraRisks" to extra,
                    "createdBy" to createdBy,
                    "assignedTo" to assignedMap,
                    "status" to "open",
                    "createdAt" to Timestamp.now(),
                    "validity" to (selectedValidity?.let { Timestamp(it.time) } ?: Timestamp.now())
                )
                db.collection("jobs").add(payload)
                    .addOnSuccessListener {
                        Toast.makeText(this,"Job saved",Toast.LENGTH_SHORT).show()
                        finish()
                    }
                    .addOnFailureListener { e -> Toast.makeText(this,"Save failed: ${e.message}",Toast.LENGTH_LONG).show() }
            }
        }
    }

    private fun pickDateTime() {
        val now = Calendar.getInstance()
        DatePickerDialog(this, { _, y, m, d ->
            val c = Calendar.getInstance()
            c.set(y, m, d)
            TimePickerDialog(this, { _, hour, minute ->
                c.set(Calendar.HOUR_OF_DAY, hour)
                c.set(Calendar.MINUTE, minute)
                selectedValidity = c
                tvValidity.text = "${d}/${m+1}/${y} ${hour}:${String.format("%02d", minute)}"
            }, now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE), true).show()
        }, now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH)).show()
    }
}
