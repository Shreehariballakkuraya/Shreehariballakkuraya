package com.hari.docuvault

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.io.InputStream

class ExtractInfoActivity : AppCompatActivity() {

    private lateinit var fileUri: Uri

    private lateinit var nameEditText: EditText
    private lateinit var typeEditText: EditText
    private lateinit var additionalInfoEditText: EditText
    private lateinit var extractButton: Button
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_extract_info)

        // Initialize UI components
        nameEditText = findViewById(R.id.name_edit_text)
        typeEditText = findViewById(R.id.type_edit_text)
        additionalInfoEditText = findViewById(R.id.additional_info_edit_text)
        extractButton = findViewById(R.id.extract_button)
        progressBar = findViewById(R.id.progress_bar)

        // Retrieve file URI from the intent
        fileUri = intent.getParcelableExtra("fileUri")!!

        // Extract text from the file
        extractTextFromFile()

        extractButton.setOnClickListener {
            if (validateInput()) {
                val resultIntent = Intent().apply {
                    putExtra("documentType", typeEditText.text.toString())
                    putExtra("fileName", getFileName(fileUri))  // Updated method to get file name
                    putExtra("fileSize", getFileSize(fileUri))
                    putExtra("fileUri", fileUri.toString())
                }
                setResult(Activity.RESULT_OK, resultIntent)
                finish()
            } else {
                Toast.makeText(this, "Please enter all required information.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun extractTextFromFile() {
        progressBar.visibility = View.VISIBLE

        val inputStream: InputStream? = contentResolver.openInputStream(fileUri)
        val bitmap: Bitmap? = BitmapFactory.decodeStream(inputStream)

        if (bitmap != null) {
            val recognizer: TextRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
            val image = com.google.mlkit.vision.common.InputImage.fromBitmap(bitmap, 0)

            recognizer.process(image)
                .addOnSuccessListener { visionText ->
                    handleExtractedText(visionText.text)
                }
                .addOnFailureListener { e ->
                    Log.e("ExtractInfoActivity", "Text extraction failed", e)
                    Toast.makeText(this, "Failed to extract text.", Toast.LENGTH_SHORT).show()
                }
                .addOnCompleteListener {
                    progressBar.visibility = View.GONE
                }
        } else {
            progressBar.visibility = View.GONE
            Toast.makeText(this, "Failed to load image.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun handleExtractedText(text: String) {
        // Process extracted text to identify relevant fields
        // For simplicity, we'll just set the whole text in the additional info field
        additionalInfoEditText.setText(text)
    }

    private fun validateInput(): Boolean {
        return nameEditText.text.isNotEmpty() && typeEditText.text.isNotEmpty() && additionalInfoEditText.text.isNotEmpty()
    }

    private fun getFileSize(uri: Uri): Long {
        val cursor = contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
            if (sizeIndex != -1) {
                return cursor.getLong(sizeIndex)
            }
        }
        return 0
    }

    private fun getFileName(uri: Uri): String {
        var fileName = "Unknown"
        val cursor = contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (nameIndex != -1 && cursor.moveToFirst()) {
                fileName = cursor.getString(nameIndex)
            }
        }
        return fileName
    }
}
