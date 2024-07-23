package com.hari.docuvault

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage

class OtherMetadataActivity : AppCompatActivity() {

    private lateinit var documentTitleEditText: EditText
    private lateinit var documentTypeEditText: EditText
    private lateinit var additionalInfoEditText: EditText
    private lateinit var categoryEditText: EditText
    private lateinit var tagsEditText: EditText
    private lateinit var dateAddedEditText: EditText
    private lateinit var submitMetadataButton: Button
    private lateinit var selectFileButton: Button
    private lateinit var selectedFileImageView: ImageView
    private lateinit var progressBar: ProgressBar

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
        setContentView(R.layout.activity_other_metadata)

        documentTitleEditText = findViewById(R.id.documentTitleEditText)
        documentTypeEditText = findViewById(R.id.documentTypeEditText)
        additionalInfoEditText = findViewById(R.id.additionalInfoEditText)
        categoryEditText = findViewById(R.id.categoryEditText)
        tagsEditText = findViewById(R.id.tagsEditText)
        dateAddedEditText = findViewById(R.id.dateAddedEditText)
        submitMetadataButton = findViewById(R.id.submitMetadataButton)
        selectFileButton = findViewById(R.id.selectFileButton)
        selectedFileImageView = findViewById(R.id.selectedFileImageView)
        progressBar = findViewById(R.id.progressBar)

        selectFileButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "*/*"
            }
            selectFileLauncher.launch(intent)
        }

        submitMetadataButton.setOnClickListener {
            val documentTitle = documentTitleEditText.text.toString().trim()
            val documentType = documentTypeEditText.text.toString().trim()
            val additionalInfo = additionalInfoEditText.text.toString().trim()
            val category = categoryEditText.text.toString().trim()
            val tags = tagsEditText.text.toString().trim()
            val dateAdded = dateAddedEditText.text.toString().trim()

            if (documentTitle.isEmpty() || documentType.isEmpty()) {
                Toast.makeText(this, "Document title and document type are required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            selectedFileUri?.let { uri ->
                showProgressBar()
                uploadFile(uri, documentTitle, documentType, additionalInfo, category, tags, dateAdded)
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

    private fun uploadFile(fileUri: Uri, documentTitle: String, documentType: String, additionalInfo: String, category: String, tags: String, dateAdded: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val rawFileName = getFileName(fileUri) ?: "unknown_file"
        val fileName = sanitizeFileName(rawFileName)
        val storageRef = FirebaseStorage.getInstance().reference.child("user_files/$userId/others/$fileName")

        storageRef.putFile(fileUri)
            .addOnSuccessListener {
                // Get the download URL
                storageRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                    saveMetadata(documentTitle, documentType, additionalInfo, category, tags, dateAdded, fileName, downloadUrl.toString())
                }
            }
            .addOnFailureListener { exception ->
                hideProgressBar()
                Toast.makeText(this, "Upload failed: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun saveMetadata(documentTitle: String, documentType: String, additionalInfo: String, category: String, tags: String, dateAdded: String, fileName: String, fileUrl: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        val metadata = hashMapOf(
            "documentTitle" to documentTitle,
            "documentType" to documentType,
            "additionalInfo" to additionalInfo,
            "category" to category,
            "tags" to tags,
            "dateAdded" to dateAdded,
            "fileUrl" to fileUrl
        )

        val sanitizedFileName = sanitizeFileName(fileName)
        val databaseRef: DatabaseReference = FirebaseDatabase.getInstance().getReference("user_files").child(userId).child("others").child(sanitizedFileName)

        databaseRef.setValue(metadata)
            .addOnSuccessListener {
                hideProgressBar()
                Toast.makeText(this, "Metadata saved successfully", Toast.LENGTH_SHORT).show()
                selectedFileImageView.setImageURI(null)
                selectedFileUri = null
                finish()
            }
            .addOnFailureListener { e ->
                hideProgressBar()
                Toast.makeText(this, "Failed to save metadata: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun sanitizeFileName(fileName: String): String {
        return fileName.replace(Regex("[.#$\\[\\]]"), "_")
    }

    private fun showProgressBar() {
        progressBar.visibility = android.view.View.VISIBLE
    }

    private fun hideProgressBar() {
        progressBar.visibility = android.view.View.GONE
    }
}
