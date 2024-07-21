package com.hari.docuvault

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.startActivity
import androidx.core.content.FileProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.lang.Exception

class VehicleViewActivity : AppCompatActivity() {

    private lateinit var containerView: ViewGroup
    private lateinit var databaseRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_vehicle_view)

        containerView = findViewById(R.id.containerView) // This should be a LinearLayout or similar in your XML

        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            databaseRef = FirebaseDatabase.getInstance().getReference("user_files").child(userId).child("vehicle_metadata")
            fetchVehicleMetadata()
        } else {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show()
        }
    }

    private fun fetchVehicleMetadata() {
        databaseRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                containerView.removeAllViews() // Clear previous content

                for (data in snapshot.children) {
                    val metadata = data.getValue(VehicleMetadata::class.java)
                    metadata?.let { addMetadataView(it) }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@VehicleViewActivity, "Failed to load data: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun addMetadataView(metadata: VehicleMetadata) {
        val view = LayoutInflater.from(this).inflate(R.layout.item_vehicle_metadata, containerView, false)
        val vehicleNameTextView: TextView = view.findViewById(R.id.vehicleNameTextView)
        val documentTypeTextView: TextView = view.findViewById(R.id.documentTypeTextView)
        val expiryDateTextView: TextView = view.findViewById(R.id.expiryDateTextView)
        val vehicleNumberTextView: TextView = view.findViewById(R.id.vehicleNumberTextView)
        val fileImageView: ImageView = view.findViewById(R.id.fileImageView)

        vehicleNameTextView.text = metadata.vehicleName
        documentTypeTextView.text = metadata.documentType
        expiryDateTextView.text = metadata.expiryDate
        vehicleNumberTextView.text = metadata.vehicleNumber

        // Load image preview if the file is an image
        if (metadata.fileName.endsWith(".jpg") || metadata.fileName.endsWith(".jpeg") || metadata.fileName.endsWith(".png")) {
            fileImageView.setImageURI(Uri.parse(metadata.fileUrl))
        } else {
            fileImageView.setImageResource(R.drawable.ic_file) // Placeholder for non-image files
        }

        fileImageView.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse(metadata.fileUrl)
                type = if (metadata.fileName.endsWith(".pdf")) "application/pdf" else "image/*"
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            try {
                startActivity(intent)
            } catch (e: Exception) {
                Toast.makeText(this@VehicleViewActivity, "No application to open this file.", Toast.LENGTH_SHORT).show()
            }
        }

        containerView.addView(view)
    }
}
