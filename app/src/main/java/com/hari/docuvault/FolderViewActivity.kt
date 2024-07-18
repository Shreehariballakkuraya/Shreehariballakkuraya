package com.hari.docuvault

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ListResult

class FileViewActivity : AppCompatActivity() {

    private lateinit var fileRecyclerView: RecyclerView
    private lateinit var fileAdapter: FileAdapter
    private val fileList = mutableListOf<String>()
    private var selectedFolder: String = "others" // Default folder type

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view)

        fileRecyclerView = findViewById(R.id.fileRecyclerView)
        fileRecyclerView.layoutManager = LinearLayoutManager(this)

        // Initialize the FileAdapter with onItemClick parameter
        fileAdapter = FileAdapter(fileList) { fileUrl ->
            openFile(fileUrl)
        }
        fileRecyclerView.adapter = fileAdapter

        val vehicleButton: Button = findViewById(R.id.vehicleButton)
        val personalButton: Button = findViewById(R.id.personalButton)
        val otherButton: Button = findViewById(R.id.otherButton)

        vehicleButton.setOnClickListener {
            selectedFolder = "vehicle"
            loadFiles()
        }

        personalButton.setOnClickListener {
            selectedFolder = "personal"
            loadFiles()
        }

        otherButton.setOnClickListener {
            selectedFolder = "others"
            loadFiles()
        }

        // Load files for the default folder when the activity is created
        loadFiles()
    }

    private fun loadFiles() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val storageRef = FirebaseStorage.getInstance().reference.child("user_files/$userId/$selectedFolder")

        storageRef.listAll()
            .addOnSuccessListener { listResult: ListResult ->
                fileList.clear()
                for (item in listResult.items) {
                    fileList.add(item.downloadUrl.toString()) // Use downloadUrl to get the file URL
                }
                fileAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Failed to load files: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun openFile(fileUrl: String) {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.setDataAndType(Uri.parse(fileUrl), "application/pdf") // Change MIME type as needed
        intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_CLEAR_TOP

        try {
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, "No application available to open this file type", Toast.LENGTH_SHORT).show()
        }
    }
}
