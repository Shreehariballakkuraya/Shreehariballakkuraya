package com.hari.docuvault

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
import com.google.firebase.storage.StorageReference
import java.io.File
import java.io.IOException

class ViewActivity : AppCompatActivity() {

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
            downloadAndOpenFile(fileUrl)
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
            .addOnSuccessListener { listResult ->
                fileList.clear()
                for (item in listResult.items) {
                    fileList.add(item.path)
                }
                fileAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Failed to load files: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun downloadAndOpenFile(fileUrl: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val storageRef = FirebaseStorage.getInstance().reference.child(fileUrl)

        val localFile = File.createTempFile("tempFile", ".pdf") // Create a temporary file

        storageRef.getFile(localFile)
            .addOnSuccessListener {
                // File downloaded successfully
                openFile(localFile)
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Failed to download file: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun openFile(file: File) {
        val intent = Intent(this, FileViewActivity::class.java).apply {
            putExtra("file_path", file.absolutePath)
        }
        startActivity(intent)
    }
}
