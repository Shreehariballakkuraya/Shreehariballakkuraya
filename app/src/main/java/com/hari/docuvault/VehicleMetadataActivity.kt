package com.hari.docuvault

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage

class VehicleMetadataActivity : AppCompatActivity() {

    private lateinit var vehicleNameEditText: EditText
    private lateinit var documentTypeEditText: EditText
    private lateinit var expiryDateEditText: EditText
    private lateinit var vehicleNumberEditText: EditText
    private lateinit var uploadButton: Button
    private lateinit var selectFileButton: Button
    private lateinit var selectedFileImageView: ImageView

    private var selectedFileUri: Uri? = null
    private val selectFileLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                selectedFileUri = result.data?.data
                selectedFileUri?.let { uri ->
                    val fileName = getFileName(uri)
                    selectedFileImageView.setImageURI(uri)
                    Toast.makeText(this, "Selected file: $fileName", Toast.LENGTH_SHORT).show()
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_vehicle_metadata)

        vehicleNameEditText = findViewById(R.id.vehicleNameEditText)
        documentTypeEditText = findViewById(R.id.documentTypeEditText)
        expiryDateEditText = findViewById(R.id.expiryDateEditText)
        vehicleNumberEditText = findViewById(R.id.vehicleNumberEditText)
        uploadButton = findViewById(R.id.uploadButton)
        selectFileButton = findViewById(R.id.selectFileButton)
        selectedFileImageView = findViewById(R.id.selectedFileImageView)

        selectFileButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "*/*"
            }
            selectFileLauncher.launch(intent)
        }

        uploadButton.setOnClickListener {
            val vehicleName = vehicleNameEditText.text.toString().trim()
            val documentType = documentTypeEditText.text.toString().trim()
            val expiryDate = expiryDateEditText.text.toString().trim()
            val vehicleNumber = vehicleNumberEditText.text.toString().trim()

            if (vehicleName.isEmpty() || documentType.isEmpty()) {
                Toast.makeText(this, "Vehicle name and document type are required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            selectedFileUri?.let { uri ->
                uploadFile(uri, vehicleName, documentType, expiryDate, vehicleNumber)
            } ?: run {
                Toast.makeText(this, "No file selected", Toast.LENGTH_SHORT).show()
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

    private fun uploadFile(fileUri: Uri, vehicleName: String, documentType: String, expiryDate: String, vehicleNumber: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val fileName = getFileName(fileUri) ?: "unknown_file"
        val storageRef = FirebaseStorage.getInstance().reference.child("user_files/$userId/vehicle/$fileName")

        storageRef.putFile(fileUri)
            .addOnSuccessListener { uploadTask ->
                storageRef.downloadUrl
                    .addOnSuccessListener { uri ->
                        saveMetadata(vehicleName, documentType, expiryDate, vehicleNumber, fileName, uri.toString())
                    }
                    .addOnFailureListener { exception ->
                        Toast.makeText(this, "Failed to get download URL: ${exception.message}", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Upload failed: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun saveMetadata(vehicleName: String, documentType: String, expiryDate: String, vehicleNumber: String, fileName: String, fileUrl: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        val metadata = hashMapOf(
            "vehicleName" to vehicleName,
            "documentType" to documentType,
            "expiryDate" to expiryDate,
            "vehicleNumber" to vehicleNumber,
            "fileName" to fileName,
            "fileUrl" to fileUrl
        )

        val databaseRef: DatabaseReference = FirebaseDatabase.getInstance().getReference("user_files").child(userId).child("vehicle_metadata").child(fileName)

        databaseRef.setValue(metadata)
            .addOnSuccessListener {
                Toast.makeText(this, "Metadata saved successfully", Toast.LENGTH_SHORT).show()
                selectedFileImageView.setImageURI(null)
                selectedFileUri = null
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to save metadata: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
