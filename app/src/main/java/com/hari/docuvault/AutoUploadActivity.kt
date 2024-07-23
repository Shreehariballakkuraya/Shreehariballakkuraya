package com.hari.docuvault

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.objects.ObjectDetection
import com.google.mlkit.vision.objects.defaults.ObjectDetectorOptions
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.io.IOException

class AutoUploadActivity : AppCompatActivity() {

    private lateinit var textRecognizer: com.google.mlkit.vision.text.TextRecognizer
    private lateinit var objectDetector: com.google.mlkit.vision.objects.ObjectDetector
    private lateinit var storage: FirebaseStorage

    private lateinit var selectFileLauncher: ActivityResultLauncher<Intent>
    private var selectedUri: Uri? = null

    private lateinit var fileNameTextView: TextView
    private lateinit var fileSizeTextView: TextView
    private lateinit var fileTypeTextView: TextView
    private lateinit var uploadButton: Button
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auto_upload)

        // Initialize Firebase Storage
        storage = Firebase.storage

        // Initialize ML Kit components
        textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        objectDetector = ObjectDetection.getClient(
            ObjectDetectorOptions.Builder()
                .setDetectorMode(ObjectDetectorOptions.STREAM_MODE)
                .enableMultipleObjects()
                .enableClassification()  // Optional
                .build()
        )

        // Initialize UI components
        fileNameTextView = findViewById(R.id.file_name)
        fileSizeTextView = findViewById(R.id.file_size)
        fileTypeTextView = findViewById(R.id.file_type)
        uploadButton = findViewById(R.id.upload_button)
        progressBar = findViewById(R.id.progress_bar)

        // Set up file selection launcher
        selectFileLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == RESULT_OK) {
                    result.data?.data?.let { uri ->
                        selectedUri = uri
                        extractAndDisplayMetadata(uri)
                    }
                }
            }

        // Set up button listeners
        findViewById<Button>(R.id.select_file_button).setOnClickListener {
            openFilePicker()
        }

        uploadButton.setOnClickListener {
            selectedUri?.let { uri ->
                handleDocumentUpload(uri)
            }
        }
    }

    private fun openFilePicker() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "*/*" // You can specify more specific MIME types if needed
        }
        selectFileLauncher.launch(intent)
    }

    private fun extractAndDisplayMetadata(uri: Uri) {
        progressBar.visibility = View.VISIBLE // Show progress bar
        val fileType = contentResolver.getType(uri)
        if (fileType != null && fileType.startsWith("image")) {
            try {
                val image = InputImage.fromFilePath(this, uri)

                // Process the image with text recognition
                textRecognizer.process(image)
                    .addOnSuccessListener { visionText ->
                        val text = visionText.text
                        val documentType = classifyDocument(text)
                        val fileName = sanitizeFileName(getFileName(uri))
                        val fileSize = getFileSize(uri)

                        fileNameTextView.text = "File Name: $fileName"
                        fileSizeTextView.text = "File Size: $fileSize bytes"
                        fileTypeTextView.text = "Document Type: $documentType"

                        uploadButton.visibility = View.VISIBLE

                        val metadata = mapOf(
                            "type" to documentType,
                            "uri" to uri.toString(),
                            "name" to fileName,
                            "size" to fileSize
                        )

                        uploadFileAndStoreMetadata(uri, documentType, metadata)
                    }
                    .addOnFailureListener { e ->
                        Log.e("TextRecognition", "Error: $e")
                    }
                    .addOnCompleteListener {
                        Toast.makeText(this, "Document processing completed.", Toast.LENGTH_SHORT).show()
                    }
            } catch (e: IOException) {
                e.printStackTrace()
                progressBar.visibility = View.GONE // Hide progress bar on error
                Toast.makeText(this, "Error processing the file.", Toast.LENGTH_SHORT).show()
            }
        } else {
            // Handle non-image files
            Log.e("FileType", "Selected file is not an image")
            fileNameTextView.text = "Selected file is not an image. Please select an image file."
            fileSizeTextView.text = ""
            fileTypeTextView.text = ""
            uploadButton.visibility = View.GONE
            progressBar.visibility = View.GONE // Hide progress bar
        }
    }

    private fun handleDocumentUpload(uri: Uri) {
        try {
            val image = InputImage.fromFilePath(this, uri)

            // Optionally process the image with object detection
            objectDetector.process(image)
                .addOnSuccessListener { detectedObjects ->
                    for (detectedObject in detectedObjects) {
                        val boundingBox = detectedObject.boundingBox
                        val trackingId = detectedObject.trackingId
                        for (label in detectedObject.labels) {
                            val text = label.text
                            val confidence = label.confidence
                            Log.d(
                                "ObjectDetection",
                                "Detected object: $text with confidence: $confidence"
                            )
                        }
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("ObjectDetection", "Error: $e")
                }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun classifyDocument(text: String): String {
        return when {
            text.contains("Invoice", true) -> "Invoice"
            text.contains("Receipt", true) -> "Receipt"
            text.contains("Report", true) -> "Report"
            text.contains("Certificate", true) -> "Certificate"
            text.contains("License", true) -> "License"
            text.contains("Driving License", true) -> "Driving License"
            text.contains("uidai", true) -> "Aadhaar Card"
            text.contains("PAN", true) -> "PAN Card"
            text.contains("Agreement", true) -> "Agreement"
            text.contains("Contract", true) -> "Contract"
            text.contains("Bill", true) -> "Bill"
            text.contains("Statement", true) -> "Statement"
            text.contains("Policy", true) -> "Policy"
            text.contains("Application", true) -> "Application"
            text.contains("Form", true) -> "Form"
            text.contains("Resume", true) -> "Resume"
            text.contains("CV", true) -> "CV"
            text.contains("Offer Letter", true) -> "Offer Letter"
            text.contains("ID Card", true) -> "ID Card"
            text.contains("Passport", true) -> "Passport"
            text.contains("Insurance", true) -> "Insurance"
            text.contains("Vehicle Registration", true) -> "Vehicle Registration (RC)"
            text.contains("Driver's License", true) -> "Driver's License"
            text.contains("Insurance Paper", true) -> "Insurance Paper"
            text.contains("Pollution", true) -> "Pollution Under Control (PUC) Certificate"
            text.contains("Vehicle Fitness", true) -> "Vehicle Fitness Certificate"
            text.contains("Service Record", true) -> "Service Record"
            text.contains("Challan", true) -> "Challan/Fine"
            text.contains("Loan Document", true) -> "Loan Document"
            text.contains("Lease Agreement", true) -> "Lease Agreement"
            text.contains("Warranty Document", true) -> "Warranty Document"
            text.contains("Voter ID", true) -> "Voter ID Card"
            text.contains("Bank Statement", true) -> "Bank Statement"
            text.contains("Health Insurance", true) -> "Health Insurance Card"
            text.contains("Tax Return", true) -> "Tax Return (ITR)"
            text.contains("Birth Certificate", true) -> "Birth Certificate"
            text.contains("Marriage Certificate", true) -> "Marriage Certificate"
            else -> "Unknown"
        }
    }

    private fun sanitizeFileName(fileName: String): String {
        return fileName.replace(Regex("[.#$\\[\\]]"), "_")
    }

    private fun uploadFileAndStoreMetadata(
        uri: Uri,
        documentType: String,
        metadata: Map<String, Any?>
    ) {
        // Determine the correct folder based on document type
        val folder = when (documentType) {
            "Vehicle Registration (RC)", "Driver's License", "Insurance Paper", "Pollution Under Control (PUC) Certificate", "Vehicle Fitness Certificate", "Service Record", "Challan/Fine", "Loan Document", "Lease Agreement", "Warranty Document" -> "vehicle"
            "Aadhaar Card", "PAN Card", "Passport", "Voter ID Card", "Bank Statement", "Health Insurance Card", "Tax Return (ITR)", "Birth Certificate", "Marriage Certificate" -> "personal"
            else -> "others"
        }

        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val storageReference = storage.reference.child("user_files/$userId/$folder/${sanitizeFileName(getFileName(uri))}")

        storageReference.putFile(uri)
            .addOnSuccessListener { taskSnapshot ->
                taskSnapshot.metadata?.reference?.downloadUrl?.addOnSuccessListener { downloadUrl ->
                    val databaseReference = Firebase.database.reference
                        .child("user_files")
                        .child(userId)
                        .child(folder)
                        .child(sanitizeFileName(getFileName(uri)))

                    databaseReference.setValue(
                        metadata + mapOf("fileurl" to downloadUrl.toString())
                    )
                        .addOnSuccessListener {
                            progressBar.visibility = View.GONE
                            Toast.makeText(this, "File uploaded and metadata saved successfully.", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener { e ->
                            progressBar.visibility = View.GONE
                            Toast.makeText(this, "Failed to save metadata: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                }
            }
            .addOnFailureListener { e ->
                progressBar.visibility = View.GONE
                Toast.makeText(this, "Upload failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun getFileName(uri: Uri): String {
        var fileName = "Unknown"
        uri.let {
            val cursor = contentResolver.query(it, null, null, null, null)
            cursor?.use {
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (nameIndex >= 0 && cursor.moveToFirst()) {
                    fileName = cursor.getString(nameIndex)
                }
            }
        }
        return fileName
    }

    private fun getFileSize(uri: Uri): Long {
        var fileSize: Long = 0
        uri.let {
            val cursor = contentResolver.query(it, null, null, null, null)
            cursor?.use {
                val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                if (sizeIndex >= 0 && cursor.moveToFirst()) {
                    fileSize = cursor.getLong(sizeIndex)
                }
            }
        }
        return fileSize
    }
}
