package com.hari.docuvault

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class UploadActivity : AppCompatActivity() {

    private lateinit var vehicleButton: Button
    private lateinit var personalButton: Button
    private lateinit var otherButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_upload)

        // Initialize buttons
        vehicleButton = findViewById(R.id.vehicleButton)
        personalButton = findViewById(R.id.personalButton)
        otherButton = findViewById(R.id.otherButton)

        // Set button click listeners
        vehicleButton.setOnClickListener {
            startMetadataActivity(VehicleMetadataActivity::class.java)
        }

        personalButton.setOnClickListener {
            startMetadataActivity(PersonalMetadataActivity::class.java)
        }

        otherButton.setOnClickListener {
            startMetadataActivity(OtherMetadataActivity::class.java)
        }
    }

    private fun startMetadataActivity(activityClass: Class<*>) {
        val intent = Intent(this, activityClass)
        startActivity(intent)
    }
}
