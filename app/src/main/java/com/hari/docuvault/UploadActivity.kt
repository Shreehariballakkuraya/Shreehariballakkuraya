package com.hari.docuvault

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.hari.docuvault.databinding.ActivityUploadBinding

class UploadActivity : AppCompatActivity() {

    private var selectedFileUri: Uri? = null
    private lateinit var binding: ActivityUploadBinding
    private var selectedFolder: String = "others"  // Default folder

    private val selectFileLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                selectedFileUri = result.data?.data
                selectedFileUri?.let { uri ->
                    val fileName = getFileName(uri)
                    Toast.makeText(this, "Selected file: $fileName", Toast.LENGTH_SHORT).show()
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUploadBinding.inflate(layoutInflater)
        setContentView(binding.root)

        authenticateUser()

        binding.selectFileButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "*/*" // Use MIME type if needed, e.g., "image/*"
            }
            selectFileLauncher.launch(intent)
        }

        binding.uploadButton.setOnClickListener {
            selectedFileUri?.let { uri ->
                uploadFile(uri)
            } ?: run {
                Toast.makeText(this, "No file selected", Toast.LENGTH_SHORT).show()
            }
        }

        // Folder selection logic
        binding.radioGroup.setOnCheckedChangeListener { _, checkedId ->
            selectedFolder = when (checkedId) {
                R.id.radio_vehicle -> "vehicle"
                R.id.radio_personal -> "personal"
                R.id.radio_others -> "others"
                else -> "others"
            }
        }
    }

    private fun authenticateUser() {
        FirebaseAuth.getInstance().signInAnonymously()
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Authentication successful", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Authentication failed.", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun getFileName(uri: Uri): String? {
        var fileName: String? = null
        contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (cursor.moveToFirst()) {
                fileName = cursor.getString(nameIndex)
            }
        }
        return fileName
    }

    private fun uploadFile(fileUri: Uri) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val fileName = getFileName(fileUri) ?: "unknown_file"
        val storageRef = FirebaseStorage.getInstance().reference.child("user_files/$userId/$selectedFolder/$fileName")

        storageRef.putFile(fileUri)
            .addOnSuccessListener {
                Toast.makeText(this, "File uploaded successfully", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Upload failed: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
