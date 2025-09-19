package com.nuvoco.riskmanagement

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SignupActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val name = findViewById<EditText>(R.id.editName)
        val email = findViewById<EditText>(R.id.editEmail)
        val password = findViewById<EditText>(R.id.editPassword)
        val isManager = findViewById<CheckBox>(R.id.checkManager)
        val signupBtn = findViewById<Button>(R.id.btnSignup)

        signupBtn.setOnClickListener {
            val userName = name.text.toString().trim()
            val userEmail = email.text.toString().trim()
            val userPassword = password.text.toString().trim()
            val managerFlag = isManager.isChecked

            if (userName.isEmpty() || userEmail.isEmpty() || userPassword.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.createUserWithEmailAndPassword(userEmail, userPassword)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val uid = auth.currentUser?.uid ?: return@addOnCompleteListener

                        val userMap = hashMapOf(
                            "uid" to uid,
                            "name" to userName,
                            "email" to userEmail,
                            "role" to if (managerFlag) "Manager" else "Worker",
                            "isManager" to managerFlag
                        )

                        db.collection("users").document(uid).set(userMap)
                            .addOnSuccessListener {
                                Toast.makeText(this, "Signup successful", Toast.LENGTH_SHORT).show()
                                startActivity(Intent(this, DashboardActivity::class.java))
                                finish()
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                    } else {
                        Toast.makeText(this, "Signup failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }
}
