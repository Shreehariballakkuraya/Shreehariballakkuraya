package com.hari.docuvault

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.squareup.picasso.Picasso

class HomeActivity : AppCompatActivity() {

    private lateinit var uploadButton: Button
    private lateinit var viewButton: Button
    private lateinit var profileImageView: ImageView
    private lateinit var welcomeTextView: TextView

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.homepage)

        // Initialize views
        uploadButton = findViewById(R.id.uploadButton)
        viewButton = findViewById(R.id.ViewButton)
        profileImageView = findViewById(R.id.profileImageView)
        welcomeTextView = findViewById(R.id.welcomeTextView)

        // Set button click listeners
        uploadButton.setOnClickListener {
            // Navigate to UploadActivity
            val intent = Intent(this, UploadMenuActivity::class.java)
            startActivity(intent)
        }

        viewButton.setOnClickListener {
            // Navigate to ViewActivity
            val intent = Intent(this, ViewActivity::class.java)
            startActivity(intent)
        }

        // Display user profile
        displayUserProfile()

        // Add subtle animations
        animateUI()
    }

    private fun displayUserProfile() {
        val user = FirebaseAuth.getInstance().currentUser

        if (user != null) {
            val displayName = user.displayName ?: "User"
            val photoUrl = user.photoUrl

            // Display welcome message
            welcomeTextView.text = "Welcome, $displayName!"

            if (photoUrl != null) {
                // Load the user's profile picture
                Picasso.get().load(photoUrl).into(profileImageView)
                profileImageView.visibility = ImageView.VISIBLE
            } else {
                profileImageView.setImageResource(R.drawable.ic_profile_placeholder) // Default profile icon
                profileImageView.visibility = ImageView.VISIBLE
            }
        } else {
            // Handle case where user is not logged in
            welcomeTextView.text = "Welcome, Guest!"
            profileImageView.setImageResource(R.drawable.ic_profile_placeholder)
            profileImageView.visibility = ImageView.VISIBLE
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
        }
    }

    private fun animateUI() {
        // Fade in animation for profile picture
        profileImageView.alpha = 0f
        profileImageView.animate().alpha(1f).setDuration(1000).start()

        // Slide in animation for buttons
        uploadButton.translationY = 200f
        viewButton.translationY = 200f
        uploadButton.animate().translationY(0f).setDuration(800).start()
        viewButton.animate().translationY(0f).setDuration(800).start()
    }
}
