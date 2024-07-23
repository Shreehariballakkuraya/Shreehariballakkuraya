package com.hari.docuvault


import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity


class UploadMenuActivity : AppCompatActivity() {

    private lateinit var autoButton: Button
    private lateinit var manualButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_upload_menu)

        autoButton = findViewById(R.id.autoButton)
        manualButton = findViewById(R.id.manualButton)

        autoButton.setOnClickListener {
            // Navigate to Auto Upload Activity
            startActivity(Intent(this, AutoUploadActivity::class.java))
        }

        manualButton.setOnClickListener {
            // Navigate to Manual Upload Activity
            startActivity(Intent(this, UploadActivity::class.java))
        }
    }
}
