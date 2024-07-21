package com.hari.docuvault

import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import java.text.SimpleDateFormat
import java.util.*

class VehicleMetadataActivity : AppCompatActivity() {

    private lateinit var vehicleNameEditText: EditText
    private lateinit var documentTypeSpinner: Spinner
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

    private lateinit var datePickerDialog: DatePickerDialog
    private val calendar: Calendar = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_vehicle_metadata)

        // Initialize views
        vehicleNameEditText = findViewById(R.id.vehicleNameEditText)
        documentTypeSpinner = findViewById(R.id.documentTypeSpinner)
        expiryDateEditText = findViewById(R.id.expiryDateEditText)
        vehicleNumberEditText = findViewById(R.id.vehicleNumberEditText)
        uploadButton = findViewById(R.id.uploadButton)
        selectFileButton = findViewById(R.id.selectFileButton)
        selectedFileImageView = findViewById(R.id.selectedFileImageView)

        // Populate document types spinner
        val vehicleDocumentTypes = arrayOf(
            "Vehicle Registration (RC)",
            "Driver's License",
            "Insurance Paper",
            "Pollution Under Control (PUC) Certificate",
            "Vehicle Fitness Certificate",
            "Service Record",
            "Challan/Fine",
            "Loan Document",
            "Lease Agreement",
            "Warranty Document"
        )
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, vehicleDocumentTypes)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        documentTypeSpinner.adapter = adapter

        // Set up date picker for expiry date
        expiryDateEditText.setOnClickListener {
            showDatePickerDialog()
        }

        // Set up file selection
        selectFileButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "*/*" // Select any file type
            }
            selectFileLauncher.launch(intent)
        }

        // Set up file upload
        uploadButton.setOnClickListener {
            val vehicleName = vehicleNameEditText.text.toString().trim()
            val documentType = vehicleDocumentTypes[documentTypeSpinner.selectedItemPosition]
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

    // Show date picker dialog
    private fun showDatePickerDialog() {
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        datePickerDialog = DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDay ->
                calendar.set(selectedYear, selectedMonth, selectedDay)
                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                expiryDateEditText.setText(dateFormat.format(calendar.time))
            },
            year, month, day
        )
        datePickerDialog.show()
    }

    // Get file name from URI
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

    // Upload file to Firebase Storage
    private fun uploadFile(fileUri: Uri, vehicleName: String, documentType: String, expiryDate: String, vehicleNumber: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val fileName = getFileName(fileUri) ?: "unknown_file"
        val sanitizedFileName = sanitizeFileName(fileName)
        val storageRef = FirebaseStorage.getInstance().reference.child("user_files/$userId/vehicle/$sanitizedFileName")

        storageRef.putFile(fileUri)
            .addOnSuccessListener {
                // Get the download URL after successful upload
                storageRef.downloadUrl
                    .addOnSuccessListener { uri ->
                        saveMetadata(vehicleName, documentType, expiryDate, vehicleNumber, sanitizedFileName, uri.toString())
                    }
                    .addOnFailureListener { exception ->
                        Toast.makeText(this, "Failed to get download URL: ${exception.message}", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Upload failed: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    // Save metadata to Firebase Realtime Database
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

    // Sanitize file name to be used as a key in Firebase
    private fun sanitizeFileName(fileName: String): String {
        // Replace invalid characters in Firebase Realtime Database key
        return fileName.replace(".", "_")
            .replace("#", "_")
            .replace("$", "_")
            .replace("[", "_")
            .replace("]", "_")
    }
}
