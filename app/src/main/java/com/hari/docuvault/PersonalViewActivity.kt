package com.hari.docuvault

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.File

class PersonalViewActivity : AppCompatActivity() {

    private lateinit var listView: ListView
    private lateinit var storageRef: StorageReference
    private lateinit var databaseRef: DatabaseReference

    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            // Permission granted, proceed with the download
            fileToDownload?.let { downloadFile(it) }
        } else {
            // Permission denied, show a message
            Toast.makeText(this, "Storage permission is required to download files", Toast.LENGTH_SHORT).show()
        }
    }

    private var fileToDownload: StorageReference? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_personal_view)

        // Initialize views
        listView = findViewById(R.id.listView)

        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            storageRef = FirebaseStorage.getInstance().reference.child("user_files").child(userId).child("personal")
            databaseRef = FirebaseDatabase.getInstance().getReference("user_files").child(userId).child("personal_metadata")
            listFiles()
        } else {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show()
        }
    }

    private fun listFiles() {
        databaseRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val items = mutableListOf<Pair<String, StorageReference>>()
                for (data in snapshot.children) {
                    val metadata = data.getValue(PersonalMetadata::class.java)
                    val fileName = metadata?.fileName ?: "Unknown"
                    val fileUrl = metadata?.fileUrl ?: ""
                    val fileRef = storageRef.child(fileName)
                    val metadataText = "Name: ${metadata?.fileName ?: "N/A"}, Type: ${metadata?.documentType ?: "N/A"}, Expiry: ${metadata?.expiryDate ?: "N/A"}"
                    items.add(Pair(metadataText, fileRef))
                }

                val adapter = object : ArrayAdapter<Pair<String, StorageReference>>(this@PersonalViewActivity, R.layout.item_list_file, items) {
                    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.item_list_file, parent, false)
                        val metadataTextView: TextView = view.findViewById(R.id.metadataTextView)
                        val downloadButton: Button = view.findViewById(R.id.downloadButton)

                        val (metadata, fileRef) = getItem(position) ?: return view
                        metadataTextView.text = metadata

                        downloadButton.setOnClickListener {
                            fileToDownload = fileRef
                            if (ContextCompat.checkSelfPermission(this@PersonalViewActivity, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                                // Permission already granted
                                fileToDownload?.let { downloadFile(it) }
                            } else {
                                // Request permission
                                requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                            }
                        }
                        return view
                    }
                }
                listView.adapter = adapter
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@PersonalViewActivity, "Failed to load data: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun downloadFile(fileRef: StorageReference) {
        val localFile = File(getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), fileRef.name)

        fileRef.getFile(localFile)
            .addOnSuccessListener {
                // File downloaded successfully
                Log.d("PersonalViewActivity", "File downloaded to ${localFile.absolutePath}")
                Toast.makeText(this, "File downloaded to ${localFile.absolutePath}", Toast.LENGTH_LONG).show()
                openFile(localFile)
            }
            .addOnFailureListener { exception ->
                // Handle any errors
                Toast.makeText(this, "Download failed: ${exception.message}", Toast.LENGTH_SHORT).show()
                Log.e("PersonalViewActivity", "Download failed", exception)
            }
    }

    private fun openFile(file: File) {
        if (file.exists()) {
            val fileUri = FileProvider.getUriForFile(this, "${applicationContext.packageName}.fileprovider", file)
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(fileUri, when {
                    file.extension.equals("pdf", ignoreCase = true) -> "application/pdf"
                    file.extension.equals("jpg", ignoreCase = true) ||
                            file.extension.equals("jpeg", ignoreCase = true) ||
                            file.extension.equals("png", ignoreCase = true) -> "image/*"
                    else -> "*/*" // For other file types
                })
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            try {
                startActivity(intent)
            } catch (e: Exception) {
                Toast.makeText(this, "No application to open this file.", Toast.LENGTH_SHORT).show()
                Log.e("PersonalViewActivity", "Error opening file", e)
            }
        } else {
            Toast.makeText(this, "File does not exist.", Toast.LENGTH_SHORT).show()
        }
    }
}
