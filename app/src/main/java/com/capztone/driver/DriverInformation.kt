package com.capztone.driver

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.capztone.driver.R
import com.capztone.driver.databinding.ActivityDriverInformationBinding
import com.capztone.driver.databinding.ActivityMainBinding
import com.capztone.utils.FirebaseAuthUtil
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class DriverInformation : AppCompatActivity() {
    private lateinit var binding:  ActivityDriverInformationBinding
    private lateinit var mAuth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var googleSignInClient: GoogleSignInClient
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDriverInformationBinding.inflate(layoutInflater)
        setContentView(binding.root)


        // Initialize Firebase Database
        mAuth = FirebaseAuthUtil.auth
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_Id)) // Your web client ID
            .requestEmail()
            .build()
        database = FirebaseDatabase.getInstance().reference
        // Retrieve the Bitmap from Intent
        val profileImageBitmap = intent.getParcelableExtra<Bitmap>("profile_image")

        binding.backbutton.setOnClickListener {
            finish()
        }
        // Set Bitmap to an ImageView
        val imageView: ImageView = findViewById(R.id.profileImageView)
        profileImageBitmap?.let {
            imageView.setImageBitmap(it)
        }

        loadDriverInformation()
        // Initialize GoogleSignInClient
        googleSignInClient = GoogleSignIn.getClient(this, gso)
        val logoutImageView: Button = findViewById(R.id.logout)
        logoutImageView.setOnClickListener {
            showLogoutConfirmationDialog()
        }
    }
    private fun loadDriverInformation() {
        val currentUserId = mAuth.currentUser?.uid
        if (currentUserId == null) {
            Log.e("DriverInformation", "User not logged in.")
            return
        }

        Log.d("DriverInformation", "Current User ID: $currentUserId")

        val databaseReference = FirebaseDatabase.getInstance().getReference("Riders Details").child(currentUserId)

        databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val userName = snapshot.child("userName").value?.toString()
                    val email = snapshot.child("email").value?.toString()
                    val phone = snapshot.child("phone").value?.toString()

                    // Log the retrieved data
                    Log.d("DriverInformation", "Retrieved Data:")
                    Log.d("DriverInformation", "Name: ${userName ?: "N/A"}")
                    Log.d("DriverInformation", "Email: ${email ?: "N/A"}")
                    Log.d("DriverInformation", "Phone: ${phone ?: "N/A"}")

                    // Set data to UI
                    binding.nameTextView.text = userName ?: "N/A"
                    binding.mailTextView.setText(email ?: "N/A")
                    binding.phoneNumberTextView.setText(phone ?: "N/A")
                } else {
                    Log.e("DriverInformation", "No user data found.")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("DriverInformation", "Database error: ${error.message}")
            }
        })
    }



    private fun showLogoutConfirmationDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_logout_confirmation, null)
        val dialogBuilder = AlertDialog.Builder(this)
            .setView(dialogView)

        val alertDialog = dialogBuilder.create()

        dialogView.findViewById<View>(R.id.btnDialogYes).setOnClickListener {
            // Perform logout action
            logout()
            alertDialog.dismiss()
        }

        dialogView.findViewById<View>(R.id.btnDialogNo).setOnClickListener {
            // Dismiss the dialog
            alertDialog.dismiss()
        }

        alertDialog.show()
        alertDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
    }
    private fun logout() {
        // Sign out from Firebase
        mAuth.signOut()

        // Sign out from Google
        googleSignInClient.signOut().addOnCompleteListener(this) {
            // After signing out from Google, redirect to login screen
            startActivity(Intent(this, LoginActivity::class.java))
            finishAffinity()
        }
    }
}