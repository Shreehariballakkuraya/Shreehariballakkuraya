package com.hari.docuvault

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class HomeActivity : AppCompatActivity() {

    private lateinit var uploadButton: Button
    private lateinit var viewButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.homepage)

        uploadButton = findViewById(R.id.uploadButton)
        viewButton = findViewById(R.id.ViewButton)

        uploadButton.setOnClickListener {
            // Navigate to UploadActivity
            val intent = Intent(this, UploadMenuActivity::class.java)
            startActivity(intent)
        }

        viewButton.setOnClickListener {
            // Navigate to ViewActivity
            val intent = Intent(this, ViewActivity::class.java)
            startActivity(intent)
        }
    }
}
