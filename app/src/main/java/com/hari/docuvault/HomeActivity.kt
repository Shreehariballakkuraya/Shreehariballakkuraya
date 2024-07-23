package com.hari.docuvault

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.squareup.picasso.Picasso

class HomeActivity : AppCompatActivity() {

    private lateinit var uploadButton: Button
    private lateinit var viewButton: Button
    private lateinit var profileImageView: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.homepage)

        uploadButton = findViewById(R.id.uploadButton)
        viewButton = findViewById(R.id.ViewButton)
        profileImageView = findViewById(R.id.profileImageView)

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

        displayUserProfile()
    }

    private fun displayUserProfile() {
        val user = FirebaseAuth.getInstance().currentUser

        if (user != null) {
            val photoUrl = user.photoUrl
            if (photoUrl != null) {
                // Load the user's profile picture into the ImageView
                Picasso.get().load(photoUrl).into(profileImageView)
                profileImageView.visibility = ImageView.VISIBLE
            } else {
                profileImageView.visibility = ImageView.GONE
            }
        } else {
            // Handle the case where the user is not logged in
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            profileImageView.visibility = ImageView.GONE
        }
    }
}
