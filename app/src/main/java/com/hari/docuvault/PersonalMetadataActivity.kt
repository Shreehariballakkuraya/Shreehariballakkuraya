package com.hari.docuvault

import DatePickerFragment
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.util.Log
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

class PersonalMetadataActivity : AppCompatActivity() {

    private lateinit var documentTitleEditText: EditText
    private lateinit var documentTypeSpinner: Spinner
    private lateinit var issueDateEditText: EditText
    private lateinit var expiryDateEditText: EditText
    private lateinit var submitMetadataButton: Button
    private lateinit var selectFileButton: Button
    private lateinit var selectedFileImageView: ImageView

    private var selectedFileUri: Uri? = null
    private val selectFileLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                selectedFileUri = result.data?.data
                selectedFileUri?.let { uri ->
                    val fileName = getFileName(uri)
                    Log.d("PersonalMetadata", "Selected file: $fileName")
                    selectedFileImageView.setImageURI(uri)
                    Toast.makeText(this, "Selected file: $fileName", Toast.LENGTH_SHORT).show()
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_personal_metadata)

        documentTitleEditText = findViewById(R.id.documentTitleEditText)
        documentTypeSpinner = findViewById(R.id.documentTypeSpinner)
        issueDateEditText = findViewById(R.id.issueDateEditText)
        expiryDateEditText = findViewById(R.id.expiryDateEditText)
        submitMetadataButton = findViewById(R.id.submitMetadataButton)
        selectFileButton = findViewById(R.id.selectFileButton)
        selectedFileImageView = findViewById(R.id.selectedFileImageView)

        // Populate document types spinner
        val personalDocumentTypes = arrayOf(
            "Aadhaar Card",
            "PAN Card",
            "Passport",
            "Driver's License",
            "Voter ID Card",
            "Bank Statement",
            "Health Insurance Card",
            "Tax Return (ITR)",
            "Birth Certificate",
            "Marriage Certificate"
        )
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, personalDocumentTypes)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        documentTypeSpinner.adapter = adapter

        // Issue date picker
        issueDateEditText.setOnClickListener {
            showDatePickerDialog(issueDateEditText)
        }

        // Expiry date picker
        expiryDateEditText.setOnClickListener {
            showDatePickerDialog(expiryDateEditText)
        }

        selectFileButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "*/*"
            }
            selectFileLauncher.launch(intent)
        }

        submitMetadataButton.setOnClickListener {
            val documentTitle = documentTitleEditText.text.toString().trim()
            val documentType = personalDocumentTypes[documentTypeSpinner.selectedItemPosition]
            val issueDate = issueDateEditText.text.toString().trim()
            val expiryDate = expiryDateEditText.text.toString().trim()

            if (documentTitle.isEmpty() || issueDate.isEmpty() || expiryDate.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            selectedFileUri?.let { uri ->
                uploadFile(uri, documentTitle, documentType, issueDate, expiryDate)
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

    private fun uploadFile(fileUri: Uri, documentTitle: String, documentType: String, issueDate: String, expiryDate: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val rawFileName = getFileName(fileUri) ?: "unknown_file"
        val fileName = sanitizeFileName(rawFileName)
        Log.d("PersonalMetadata", "Sanitized file name: $fileName")
        val storageRef = FirebaseStorage.getInstance().reference.child("user_files/$userId/personal/$fileName")

        storageRef.putFile(fileUri)
            .addOnSuccessListener {
                Log.d("PersonalMetadata", "File uploaded successfully: $fileName")
                saveMetadata(documentTitle, documentType, issueDate, expiryDate, fileName)
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Upload failed: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun saveMetadata(documentTitle: String, documentType: String, issueDate: String, expiryDate: String, fileName: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        val metadata = hashMapOf(
            "documentTitle" to documentTitle,
            "documentType" to documentType,
            "issueDate" to issueDate,
            "expiryDate" to expiryDate,
            "fileName" to sanitizeFileName(fileName)
        )

        val sanitizedFileName = sanitizeFileName(fileName)
        Log.d("PersonalMetadata", "Saving metadata for file: $sanitizedFileName")

        val databaseRef: DatabaseReference = FirebaseDatabase.getInstance().getReference("user_files").child(userId).child("personal_metadata").child(sanitizedFileName)

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

    private fun sanitizeFileName(fileName: String): String {
        val sanitizedFileName = fileName.replace(Regex("[.#$\\[\\]]"), "_")
        Log.d("PersonalMetadata", "Sanitized file name inside method: $sanitizedFileName")
        return sanitizedFileName
    }

    private fun showDatePickerDialog(editText: EditText) {
        val datePicker = DatePickerFragment { day, month, year ->
            val calendar = Calendar.getInstance()
            calendar.set(year, month, day)
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val formattedDate = sdf.format(calendar.time)
            editText.setText(formattedDate)
        }
        datePicker.show(supportFragmentManager, "datePicker")
    }
}
