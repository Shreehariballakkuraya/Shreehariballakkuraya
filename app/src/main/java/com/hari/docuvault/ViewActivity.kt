package com.hari.docuvault

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class ViewActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view)

        val vehicleButton: Button = findViewById(R.id.vehicleButton)
        val personalButton: Button = findViewById(R.id.personalButton)
        val otherButton: Button = findViewById(R.id.otherButton)

        vehicleButton.setOnClickListener {
            val intent = Intent(this, VehicleViewActivity::class.java)
            startActivity(intent)
        }

        personalButton.setOnClickListener {
            val intent = Intent(this, PersonalViewActivity::class.java)
            startActivity(intent)
        }

        otherButton.setOnClickListener {
            val intent = Intent(this, OtherViewActivity::class.java)
            startActivity(intent)
        }
    }
}
