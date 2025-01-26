package com.hari.docuvault

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class VehicleViewActivity : BaseFileViewActivity() {

    private lateinit var listView: ListView
    private lateinit var storageRef: StorageReference
    private lateinit var databaseRef: DatabaseReference
    private var valueEventListener: ValueEventListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_vehicle_view)

        // Initialize views
        listView = findViewById(R.id.listView)
        progressBar = findViewById(R.id.progressBar)

        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            storageRef = FirebaseStorage.getInstance().reference.child("user_files").child(userId).child("vehicle")
            databaseRef = FirebaseDatabase.getInstance().getReference("user_files").child(userId).child("vehicle")
            listFiles()
        } else {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show()
        }
    }

    private fun listFiles() {
        showProgressBar()
        valueEventListener = databaseRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val items = mutableListOf<Pair<String, StorageReference>>()
                for (data in snapshot.children) {
                    val metadata = data.getValue(VehicleMetadata::class.java)
                    val fileName = metadata?.fileName ?: "Unknown"
                    val fileRef = storageRef.child(fileName)
                    val metadataText = "Name: $fileName, Type: ${metadata?.documentType ?: "N/A"}, Expiry: ${metadata?.expiryDate ?: "N/A"}"
                    items.add(Pair(metadataText, fileRef))
                }
                updateListView(items, snapshot)
                hideProgressBar()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@VehicleViewActivity, "Failed to load data: ${error.message}", Toast.LENGTH_SHORT).show()
                hideProgressBar()
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        valueEventListener?.let { databaseRef.removeEventListener(it) }
    }
}
